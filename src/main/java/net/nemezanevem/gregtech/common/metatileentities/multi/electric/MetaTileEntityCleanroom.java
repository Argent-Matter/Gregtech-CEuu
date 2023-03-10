package net.nemezanevem.gregtech.common.metatileentities.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.blockentity.IDataInfoProvider;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntityHolder;
import net.nemezanevem.gregtech.api.blockentity.SimpleGeneratorMetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.blockentity.multiblock.*;
import net.nemezanevem.gregtech.api.capability.*;
import net.nemezanevem.gregtech.api.capability.impl.CleanroomLogic;
import net.nemezanevem.gregtech.api.capability.impl.EnergyContainerList;
import net.nemezanevem.gregtech.api.pattern.*;
import net.nemezanevem.gregtech.api.util.BlockInfo;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.client.renderer.ICubeRenderer;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import net.nemezanevem.gregtech.client.util.TooltipHelper;
import net.nemezanevem.gregtech.common.ConfigHolder;
import net.nemezanevem.gregtech.common.block.BlockCleanroomCasing;
import net.nemezanevem.gregtech.common.block.BlockGlassCasing;
import net.nemezanevem.gregtech.common.block.MetaBlocks;
import net.nemezanevem.gregtech.common.metatileentities.MetaTileEntities;
import net.nemezanevem.gregtech.common.metatileentities.multi.MetaTileEntityCokeOven;
import net.nemezanevem.gregtech.common.metatileentities.multi.MetaTileEntityPrimitiveBlastFurnace;
import net.nemezanevem.gregtech.common.metatileentities.multi.MetaTileEntityPrimitiveWaterPump;
import net.nemezanevem.gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityCentralMonitor;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class MetaTileEntityCleanroom extends MultiblockWithDisplayBase implements ICleanroomProvider, IWorkable, IDataInfoProvider {

    public static final int CLEAN_AMOUNT_THRESHOLD = 90;
    public static final int MIN_CLEAN_AMOUNT = 0;

    public static final int MIN_RADIUS = 2;
    public static final int MIN_DEPTH = 4;

    private int lDist = 0;
    private int rDist = 0;
    private int bDist = 0;
    private int fDist = 0;
    private int hDist = 0;

    private CleanroomType cleanroomType = null;
    private int cleanAmount;

    private IEnergyContainer energyContainer;

    private final CleanroomLogic cleanroomLogic;
    private final HashSet<ICleanroomReceiver> cleanroomReceivers = new HashSet<>();

    public MetaTileEntityCleanroom(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.cleanroomLogic = new CleanroomLogic(this, GTValues.LV);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCleanroom(metaTileEntityId);
    }

    protected void initializeAbilities() {
        this.energyContainer = new EnergyContainerList(getAbilities(GtMultiblockAbilities.INPUT_ENERGY.get()));
    }

    private void resetTileAbilities() {
        this.energyContainer = new EnergyContainerList(new ArrayList<>());
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeAbilities();
        Object type = context.get("FilterType");
        if (type instanceof BlockCleanroomCasing.CasingType) {
            BlockCleanroomCasing.CasingType casingType = (BlockCleanroomCasing.CasingType) type;

            if (casingType.equals(BlockCleanroomCasing.CasingType.FILTER_CASING)) {
                this.cleanroomType = CleanroomType.CLEANROOM;
            } else if (casingType.equals(BlockCleanroomCasing.CasingType.FILTER_CASING_STERILE)) {
                this.cleanroomType = CleanroomType.STERILE_CLEANROOM;
            }
        }
        // max progress is based on the dimensions of the structure: (x^3)-(x^2)
        // taller cleanrooms take longer than wider ones
        // minimum of 100 is a 5x5x5 cleanroom: 125-25=100 ticks
        this.cleanroomLogic.setMaxProgress(Math.max(100, ((lDist + rDist + 1) * (bDist + fDist + 1) * hDist) - ((lDist + rDist + 1) * (bDist + fDist + 1))));
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();
        this.cleanroomLogic.invalidate();
        this.cleanAmount = MIN_CLEAN_AMOUNT;
        cleanroomReceivers.forEach(receiver -> receiver.setCleanroom(null));
        cleanroomReceivers.clear();
    }

    @Override
    protected void updateFormedValid() {
        if (!getWorld().isClientSide) {
            this.cleanroomLogic.updateLogic();
            if (this.cleanroomLogic.wasActiveAndNeedsUpdate()) {
                this.cleanroomLogic.setWasActiveAndNeedsUpdate(false);
                this.cleanroomLogic.setActive(false);
            }
        }
    }

    @Override
    public void checkStructurePattern() {
        if (!this.isStructureFormed()) {
            reinitializeStructurePattern();
        }
        super.checkStructurePattern();
    }

    /**
     * Scans for blocks around the controller to update the dimensions
     */
    public boolean updateStructureDimensions() {
        Level world = getWorld();
        Direction front = getFrontFacing();
        Direction back = front.getOpposite();
        Direction left = front.getCounterClockWise();
        Direction right = left.getOpposite();

        BlockPos.MutableBlockPos lPos = getPos().mutable();
        BlockPos.MutableBlockPos rPos = getPos().mutable();
        BlockPos.MutableBlockPos fPos = getPos().mutable();
        BlockPos.MutableBlockPos bPos = getPos().mutable();
        BlockPos.MutableBlockPos hPos = getPos().mutable();

        // find the distances from the controller to the plascrete blocks on one horizontal axis and the Y axis
        // repeatable aisles take care of the second horizontal axis
        int lDist = 0;
        int rDist = 0;
        int bDist = 0;
        int fDist = 0;
        int hDist = 0;

        // find the left, right, back, and front distances for the structure pattern
        // maximum size is 15x15x15 including walls, so check 7 block radius around the controller for blocks
        for (int i = 1; i < 8; i++) {
            if (lDist == 0 && isBlockEdge(world, lPos, left)) lDist = i;
            if (rDist == 0 && isBlockEdge(world, rPos, right)) rDist = i;
            if (bDist == 0 && isBlockEdge(world, bPos, back)) bDist = i;
            if (fDist == 0 && isBlockEdge(world, fPos, front)) fDist = i;
            if (lDist != 0 && rDist != 0 && bDist != 0 && fDist != 0) break;
        }

        // height is diameter instead of radius, so it needs to be done separately
        for (int i = 1; i < 15; i++) {
            if (isBlockFloor(world, hPos, Direction.DOWN)) hDist = i;
            if (hDist != 0) break;
        }

        if (lDist < MIN_RADIUS || rDist < MIN_RADIUS || bDist < MIN_RADIUS || fDist < MIN_RADIUS || hDist < MIN_DEPTH) {
            invalidateStructure();
            return false;
        }

        this.lDist = lDist;
        this.rDist = rDist;
        this.bDist = bDist;
        this.fDist = fDist;
        this.hDist = hDist;

        writeCustomData(GregtechDataCodes.UPDATE_STRUCTURE_SIZE, buf -> {
            buf.writeInt(this.lDist);
            buf.writeInt(this.rDist);
            buf.writeInt(this.bDist);
            buf.writeInt(this.fDist);
            buf.writeInt(this.hDist);
        });
        return true;
    }

    /**
     * @param world     the world to check
     * @param pos       the pos to check and move
     * @param direction the direction to move
     * @return if a block is a valid wall block at pos moved in direction
     */
    public boolean isBlockEdge(@Nonnull Level world, @Nonnull BlockPos.MutableBlockPos pos, @Nonnull Direction direction) {
        return world.getBlockState(pos.move(direction)) == MetaBlocks.CLEANROOM_CASING.get().getState(BlockCleanroomCasing.CasingType.PLASCRETE);
    }

    /**
     * @param world     the world to check
     * @param pos       the pos to check and move
     * @param direction the direction to move
     * @return if a block is a valid floor block at pos moved in direction
     */
    public boolean isBlockFloor(@Nonnull Level world, @Nonnull BlockPos.MutableBlockPos pos, @Nonnull Direction direction) {
        return isBlockEdge(world, pos, direction) || world.getBlockState(pos) == MetaBlocks.TRANSPARENT_CASING.get().getState(BlockGlassCasing.CasingType.CLEANROOM_GLASS);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        if (getWorld() != null && !updateStructureDimensions()) {
            return null;
        }

        // these can sometimes get set to 0 when loading the game, breaking JEI
        if (lDist < MIN_RADIUS) lDist = MIN_RADIUS;
        if (rDist < MIN_RADIUS) rDist = MIN_RADIUS;
        if (bDist < MIN_RADIUS) bDist = MIN_RADIUS;
        if (fDist < MIN_RADIUS) fDist = MIN_RADIUS;
        if (hDist < MIN_DEPTH) hDist = MIN_DEPTH;

        if (this.frontFacing == Direction.EAST || this.frontFacing == Direction.WEST) {
            int tmp = lDist;
            lDist = rDist;
            rDist = tmp;
        }

        // build each row of the structure
        StringBuilder borderBuilder = new StringBuilder();     // BBBBB
        StringBuilder wallBuilder = new StringBuilder();       // BXXXB
        StringBuilder insideBuilder = new StringBuilder();     // X   X
        StringBuilder roofBuilder = new StringBuilder();       // BFFFB
        StringBuilder controllerBuilder = new StringBuilder(); // BFSFB
        StringBuilder centerBuilder = new StringBuilder();     // BXKXB

        // everything to the left of the controller
        for (int i = 0; i < lDist; i++) {
            borderBuilder.append("B");
            if (i == 0) {
                wallBuilder.append("B");
                insideBuilder.append("X");
                roofBuilder.append("B");
                controllerBuilder.append("B");
                centerBuilder.append("B");
            } else {
                insideBuilder.append(" ");
                wallBuilder.append("X");
                roofBuilder.append("F");
                controllerBuilder.append("F");
                centerBuilder.append("X");
            }
        }

        // everything in-line with the controller
        borderBuilder.append("B");
        wallBuilder.append("X");
        insideBuilder.append(" ");
        roofBuilder.append("F");
        controllerBuilder.append("S");
        centerBuilder.append("K");

        // everything to the right of the controller
        for (int i = 0; i < rDist; i++) {
            borderBuilder.append("B");
            if (i == rDist - 1) {
                wallBuilder.append("B");
                insideBuilder.append("X");
                roofBuilder.append("B");
                controllerBuilder.append("B");
                centerBuilder.append("B");
            } else {
                insideBuilder.append(" ");
                wallBuilder.append("X");
                roofBuilder.append("F");
                controllerBuilder.append("F");
                centerBuilder.append("X");
            }
        }

        // build each slice of the structure
        String[] wall = new String[hDist + 1]; // "BBBBB", "BXXXB", "BXXXB", "BXXXB", "BBBBB"
        Arrays.fill(wall, wallBuilder.toString());
        wall[0] = borderBuilder.toString();
        wall[wall.length - 1] = borderBuilder.toString();

        String[] slice = new String[hDist + 1]; // "BXXXB", "X   X", "X   X", "X   X", "BFFFB"
        Arrays.fill(slice, insideBuilder.toString());
        slice[0] = wallBuilder.toString();
        slice[slice.length - 1] = roofBuilder.toString();

        String[] center = Arrays.copyOf(slice, slice.length); // "BXKXB", "X   X", "X   X", "X   X", "BFSFB"
        if (this.frontFacing == Direction.NORTH || this.frontFacing == Direction.SOUTH) {
            center[0] = centerBuilder.reverse().toString();
            center[center.length - 1] = controllerBuilder.reverse().toString();
        } else {
            center[0] = centerBuilder.toString();
            center[center.length - 1] = controllerBuilder.toString();
        }

        TraceabilityPredicate wallPredicate = states(getCasingState(), getGlassState());
        TraceabilityPredicate basePredicate = autoAbilities().or(abilities(GtMultiblockAbilities.INPUT_ENERGY.get())
                .setMinGlobalLimited(1).setMaxGlobalLimited(3));

        // layer the slices one behind the next
        return FactoryBlockPattern.start()
                .aisle(wall)
                .aisle(slice).setRepeatable(bDist - 1)
                .aisle(center)
                .aisle(slice).setRepeatable(fDist - 1)
                .aisle(wall)
                .where('S', selfPredicate())
                .where('B', states(getCasingState()).or(basePredicate))
                .where('X', wallPredicate.or(basePredicate)
                        .or(doorPredicate().setMaxGlobalLimited(8))
                        .or(abilities(GtMultiblockAbilities.PASSTHROUGH_HATCH.get()).setMaxGlobalLimited(30)))
                .where('K', wallPredicate) // the block beneath the controller must only be a casing for structure dimension checks
                .where('F', filterPredicate())
                .where(' ', innerPredicate())
                .build();
    }

    @Nonnull
    protected TraceabilityPredicate filterPredicate() {
        return new TraceabilityPredicate(blockWorldState -> {
            BlockState blockState = blockWorldState.getBlockState();
            Block block = blockState.getBlock();
            if (block instanceof BlockCleanroomCasing) {
                BlockCleanroomCasing.CasingType casingType = ((BlockCleanroomCasing) blockState.getBlock()).getState(blockState);
                if (casingType.equals(BlockCleanroomCasing.CasingType.PLASCRETE)) return false;

                Object currentFilter = blockWorldState.getMatchContext().getOrPut("FilterType", casingType);
                if (!currentFilter.toString().equals(casingType.getSerializedName())) {
                    blockWorldState.setError(new PatternStringError("gregtech.multiblock.pattern.error.filters"));
                    return false;
                }
                blockWorldState.getMatchContext().getOrPut("VABlock", new LinkedList<>()).add(blockWorldState.getPos());
                return true;
            }
            return false;
        }, () -> ArrayUtils.addAll(
                Arrays.stream(BlockCleanroomCasing.CasingType.values())
                        .filter(type -> !type.equals(BlockCleanroomCasing.CasingType.PLASCRETE))
                        .map(type -> new BlockInfo(MetaBlocks.CLEANROOM_CASING.get().getState(type), null))
                        .toArray(BlockInfo[]::new)))
                .addTooltips(Component.translatable("gregtech.multiblock.pattern.error.filters"));
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.PLASCRETE;
    }

    // protected to allow easy addition of addon "cleanrooms"
    @Nonnull
    protected BlockState getCasingState() {
        return MetaBlocks.CLEANROOM_CASING.get().getState(BlockCleanroomCasing.CasingType.PLASCRETE);
    }

    @Nonnull
    protected BlockState getGlassState() {
        return MetaBlocks.TRANSPARENT_CASING.get().getState(BlockGlassCasing.CasingType.CLEANROOM_GLASS);
    }

    @Nonnull
    protected static TraceabilityPredicate doorPredicate() {
        return new TraceabilityPredicate(blockWorldState -> blockWorldState.getBlockState().getBlock() instanceof DoorBlock);
    }

    @Nonnull
    protected TraceabilityPredicate innerPredicate() {
        return new TraceabilityPredicate(blockWorldState -> {
            // all non-MetaTileEntities are allowed inside by default
            BlockEntity tileEntity = blockWorldState.getBlockEntity();
            if (!(tileEntity instanceof MetaTileEntityHolder)) return true;

            MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();

            // always ban other cleanrooms, can cause problems otherwise
            if (metaTileEntity instanceof ICleanroomProvider)
                return false;

            if (isMachineBanned(metaTileEntity))
                return false;

            // the machine does not need a cleanroom, so do nothing more
            if (!(metaTileEntity instanceof ICleanroomReceiver)) return true;

            // give the machine this cleanroom if it doesn't have this one
            ICleanroomReceiver cleanroomReceiver = (ICleanroomReceiver) metaTileEntity;
            if (cleanroomReceiver.getCleanroom() != this) {
                cleanroomReceiver.setCleanroom(this);
                cleanroomReceivers.add(cleanroomReceiver);
            }
            return true;
        });
    }

    protected boolean isMachineBanned(MetaTileEntity metaTileEntity) {
        // blacklisted machines: mufflers and all generators, miners/drills, primitives
        if (metaTileEntity instanceof IMufflerHatch) return true;
        if (metaTileEntity instanceof SimpleGeneratorMetaTileEntity) return true;
        if (metaTileEntity instanceof FuelMultiblockController) return true;
        if (metaTileEntity instanceof MetaTileEntityLargeMiner) return true;
        if (metaTileEntity instanceof MetaTileEntityFluidDrill) return true;
        if (metaTileEntity instanceof MetaTileEntityCentralMonitor) return true;
        if (metaTileEntity instanceof MetaTileEntityCleanroom) return true;
        if (metaTileEntity instanceof MetaTileEntityCokeOven) return true;
        if (metaTileEntity instanceof MetaTileEntityPrimitiveBlastFurnace) return true;
        return metaTileEntity instanceof MetaTileEntityPrimitiveWaterPump;
    }

    @Override
    protected void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isStructureFormed()) {
            if (energyContainer != null && energyContainer.getEnergyCapacity() > 0) {
                long maxVoltage = Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage());
                String voltageName = GTValues.VNF[Util.getTierByVoltage(maxVoltage)];
                textList.add(Component.translatable("gregtech.multiblock.max_energy_per_tick", maxVoltage, voltageName));
            }

            if (!cleanroomLogic.isWorkingEnabled()) {
                textList.add(Component.translatable("gregtech.multiblock.work_paused"));
            } else if (cleanroomLogic.isActive()) {
                textList.add(Component.translatable("gregtech.multiblock.running"));
                textList.add(Component.translatable("gregtech.multiblock.progress", getProgressPercent()));
            } else {
                textList.add(Component.translatable("gregtech.multiblock.idling"));
            }

            if (!drainEnergy(true)) {
                textList.add(Component.translatable("gregtech.multiblock.not_enough_energy").setStyle(new Style().setColor(TextFormatting.RED)));
            }

            if (isClean()) textList.add(Component.translatable("gregtech.multiblock.cleanroom.clean_state"));
            else textList.add(Component.translatable("gregtech.multiblock.cleanroom.dirty_state"));
            textList.add(Component.translatable("gregtech.multiblock.cleanroom.clean_amount", this.cleanAmount));
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level player, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.machine.cleanroom.tooltip.1"));
        tooltip.add(Component.translatable("gregtech.machine.cleanroom.tooltip.2"));
        tooltip.add(Component.translatable("gregtech.machine.cleanroom.tooltip.3"));
        tooltip.add(Component.translatable("gregtech.machine.cleanroom.tooltip.4"));

        if (TooltipHelper.isCtrlDown()) {
            tooltip.add("");
            tooltip.add(Component.translatable("gregtech.machine.cleanroom.tooltip.5"));
            tooltip.add(Component.translatable("gregtech.machine.cleanroom.tooltip.6"));
            tooltip.add(Component.translatable("gregtech.machine.cleanroom.tooltip.7"));
            tooltip.add(Component.translatable("gregtech.machine.cleanroom.tooltip.8"));
            tooltip.add(Component.translatable("gregtech.machine.cleanroom.tooltip.9"));
            if (Loader.isModLoaded(GTValues.MODID_APPENG)) {
                tooltip.add(Component.translatable(AEConfig.instance().isFeatureEnabled(AEFeature.CHANNELS) ? "gregtech.machine.cleanroom.tooltip.ae2.channels" : "gregtech.machine.cleanroom.tooltip.ae2.no_channels"));
            }
            tooltip.add("");
        } else {
            tooltip.add(Component.translatable("gregtech.machine.cleanroom.tooltip.hold_ctrl"));
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), isActive(), isWorkingEnabled());
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.CLEANROOM_OVERLAY;
    }

    @Override
    public Set<CleanroomType> getTypes() {
        return Sets.newHashSet(this.cleanroomType);
    }

    @Override
    public void setCleanAmount(int amount) {
        this.cleanAmount = amount;
    }

    @Override
    public void adjustCleanAmount(int amount) {
        // do not allow negative cleanliness nor cleanliness above 100
        this.cleanAmount = Mth.clamp(this.cleanAmount + amount, 0, 100);
    }

    @Override
    public boolean isClean() {
        return this.cleanAmount >= CLEAN_AMOUNT_THRESHOLD;
    }

    @Nonnull
    @Override
    public List<Component> getDataInfo() {
        return Collections.singletonList(Component.translatable(isClean() ? "gregtech.multiblock.cleanroom.clean_state" : "gregtech.multiblock.cleanroom.dirty_state"));
    }

    @Override
    public boolean isActive() {
        return super.isActive() && this.cleanroomLogic.isActive();
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.cleanroomLogic.isWorkingEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        if (!isActivationAllowed) // pausing sets not clean
            setCleanAmount(MIN_CLEAN_AMOUNT);
        this.cleanroomLogic.setWorkingEnabled(isActivationAllowed);
    }

    @Override
    public int getProgress() {
        return cleanroomLogic.getProgressTime();
    }

    @Override
    public int getMaxProgress() {
        return cleanroomLogic.getMaxProgress();
    }

    public int getProgressPercent() {
        return cleanroomLogic.getProgressPercent();
    }

    @Override
    public int getEnergyTier() {
        if (energyContainer == null) return GTValues.LV;
        return Math.max(GTValues.LV, Util.getFloorTierByVoltage(energyContainer.getInputVoltage()));
    }

    @Override
    public long getEnergyInputPerSecond() {
        return energyContainer.getInputPerSec();
    }

    public boolean drainEnergy(boolean simulate) {
        long energyToDrain = isClean() ? (long) Math.min(4, Math.pow(4, getEnergyTier())) : GTValues.VA[getEnergyTier()];
        long resultEnergy = energyContainer.getEnergyStored() - energyToDrain;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.changeEnergy(-energyToDrain);
            return true;
        }
        return false;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_WORKABLE)
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this);
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE)
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        return super.getCapability(capability, side);
    }

    @Override
    public void receiveCustomData(int dataId, FriendlyByteBuf buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.UPDATE_STRUCTURE_SIZE) {
            this.lDist = buf.readInt();
            this.rDist = buf.readInt();
            this.hDist = buf.readInt();
        } else if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            this.cleanroomLogic.setActive(buf.readBoolean());
            scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.cleanroomLogic.setWorkingEnabled(buf.readBoolean());
            scheduleRenderUpdate();
        }
    }

    @Override
    public CompoundTag writeToNBT(@Nonnull CompoundTag data) {
        super.writeToNBT(data);
        data.putInt("lDist", this.lDist);
        data.putInt("rDist", this.rDist);
        data.putInt("bDist", this.fDist);
        data.putInt("fDist", this.bDist);
        data.putInt("hDist", this.hDist);
        data.putInt("cleanAmount", this.cleanAmount);
        return this.cleanroomLogic.writeToNBT(data);
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        this.lDist = data.hasKey("lDist") ? data.getInt("lDist") : this.lDist;
        this.rDist = data.hasKey("rDist") ? data.getInt("rDist") : this.rDist;
        this.hDist = data.hasKey("hDist") ? data.getInt("hDist") : this.hDist;
        this.bDist = data.hasKey("bDist") ? data.getInt("bDist") : this.bDist;
        this.fDist = data.hasKey("fDist") ? data.getInt("fDist") : this.fDist;
        this.cleanAmount = data.getInt("cleanAmount");
        this.cleanroomLogic.readFromNBT(data);
    }

    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.lDist);
        buf.writeInt(this.rDist);
        buf.writeInt(this.bDist);
        buf.writeInt(this.fDist);
        buf.writeInt(this.hDist);
        buf.writeInt(this.cleanAmount);
        this.cleanroomLogic.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        super.receiveInitialSyncData(buf);
        this.lDist = buf.readInt();
        this.rDist = buf.readInt();
        this.bDist = buf.readInt();
        this.fDist = buf.readInt();
        this.hDist = buf.readInt();
        this.cleanAmount = buf.readInt();
        this.cleanroomLogic.receiveInitialSyncData(buf);
    }

    @Override
    public void getSubItems(CreativeModeTab creativeTab, NonNullList<ItemStack> subItems) {
        if (ConfigHolder.machines.enableCleanroom) {
            super.getSubItems(creativeTab, subItems);
        }
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
        MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder()
                .aisle("XXXXX", "XIHLX", "XXDXX", "XXXXX", "XXXXX")
                .aisle("XXXXX", "X   X", "G   G", "X   X", "XFFFX")
                .aisle("XXXXX", "X   X", "G   G", "X   X", "XFSFX")
                .aisle("XXXXX", "X   X", "G   G", "X   X", "XFFFX")
                .aisle("XMXEX", "XXOXX", "XXRXX", "XXXXX", "XXXXX")
                .where('X', MetaBlocks.CLEANROOM_CASING.getState(BlockCleanroomCasing.CasingType.PLASCRETE))
                .where('G', MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.CLEANROOM_GLASS))
                .where('S', MetaTileEntities.CLEANROOM, Direction.SOUTH)
                .where(' ', Blocks.AIR.getDefaultState())
                .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.LV], Direction.SOUTH)
                .where('I', MetaTileEntities.PASSTHROUGH_HATCH_ITEM, Direction.NORTH)
                .where('L', MetaTileEntities.PASSTHROUGH_HATCH_FLUID, Direction.NORTH)
                .where('H', MetaTileEntities.HULL[GTValues.HV], Direction.NORTH)
                .where('D', MetaTileEntities.DIODES[GTValues.HV], Direction.NORTH)
                .where('M', () -> ConfigHolder.machines.enableMaintenance ? MetaTileEntities.MAINTENANCE_HATCH : MetaBlocks.CLEANROOM_CASING.getState(BlockCleanroomCasing.CasingType.PLASCRETE), Direction.SOUTH)
                .where('O', Blocks.IRON_DOOR.getDefaultState().withProperty(BlockDoor.FACING, Direction.NORTH).withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.LOWER))
                .where('R', Blocks.IRON_DOOR.getDefaultState().withProperty(BlockDoor.FACING, Direction.NORTH).withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER));

        Arrays.stream(BlockCleanroomCasing.CasingType.values())
                .filter(casingType -> !casingType.equals(BlockCleanroomCasing.CasingType.PLASCRETE))
                .forEach(casingType -> shapeInfo.add(builder.where('F', MetaBlocks.CLEANROOM_CASING.getState(casingType)).build()));
        return shapeInfo;
    }

    @Override
    protected boolean shouldShowVoidingModeButton() {
        return false;
    }
}
