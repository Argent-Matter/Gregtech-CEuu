package net.nemezanevem.gregtech.common.metatileentities.electric;

import codechicken.lib.raytracer.VoxelShapeBlockHitResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.TieredMetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.capability.GregtechTileCapabilities;
import net.nemezanevem.gregtech.api.capability.IControllable;
import net.nemezanevem.gregtech.api.capability.impl.EnergyContainerHandler;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.pipenet.tile.TileEntityPipeBase;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

import static net.nemezanevem.gregtech.api.capability.GregtechDataCodes.IS_WORKING;
import static net.nemezanevem.gregtech.api.capability.GregtechDataCodes.SYNC_TILE_MODE;

public class MetaTileEntityWorldAccelerator extends TieredMetaTileEntity implements IControllable {

    private static Class<?> cofhTileClass;

    private static boolean considerTile(BlockEntity tile) {
        // TODO interface for this?
        if (tile instanceof IGregTechTileEntity || tile instanceof TileEntityPipeBase) {
            return false;
        }
        if (cofhTileClass == null) {
            try {
                cofhTileClass = Class.forName("cofh.thermalexpansion.block.device.TileDeviceBase");
            } catch (Exception ignored) {}
        }
        return cofhTileClass == null || !cofhTileClass.isInstance(tile);
    }

    private final long energyPerTick;
    private final int speed;

    private boolean tileMode = false;
    private boolean isActive = false;
    private boolean isPaused = false;
    private int lastTick;
    private Supplier<Iterable<BlockPos>> range;

    public MetaTileEntityWorldAccelerator(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        //consume 8 amps
        this.energyPerTick = GTValues.V[tier] * getMaxInputOutputAmperage();
        this.lastTick = 0;
        this.speed = (int) Math.pow(2, tier);
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityWorldAccelerator(metaTileEntityId, getTier());
    }

    @Override
    protected void reinitializeEnergyContainer() {
        long tierVoltage = GTValues.V[getTier()];
        this.energyContainer = EnergyContainerHandler.receiverContainer(this,
                tierVoltage * 256L, tierVoltage, getMaxInputOutputAmperage());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level player, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.machine.world_accelerator.description"));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.voltage_in", energyContainer.getInputVoltage(), GTValues.VNF[getTier()]));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.amperage_in", getMaxInputOutputAmperage()));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        tooltip.add(Component.translatable("gregtech.machine.world_accelerator.working_area"));
        tooltip.add(Component.translatable("gregtech.machine.world_accelerator.working_area_tile"));
        tooltip.add(Component.translatable("gregtech.machine.world_accelerator.working_area_random", getTier() * 2, getTier() * 2));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable Level world, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.tool_action.screwdriver.toggle_mode_covers"));
        tooltip.add(Component.translatable("gregtech.tool_action.wrench.set_facing"));
        tooltip.add(Component.translatable("gregtech.tool_action.soft_mallet.reset"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    protected long getMaxInputOutputAmperage() {
        return 8L;
    }

    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isClientSide) {
            if (isPaused) {
                if (isActive) {
                    setActive(false);
                }
                return;
            }
            if (energyContainer.getEnergyStored() < energyPerTick) {
                if (isActive) {
                    setActive(false);
                }
                return;
            }
            if (!isActive) {
                setActive(true);
            }

            int currentTick = ServerLifecycleHooks.getCurrentServer().getTickCount();
            if (currentTick != lastTick) { // Prevent other tick accelerators from accelerating us
                Level world = getWorld();
                BlockPos currentPos = getPos();
                lastTick = currentTick;
                if (isTEMode()) {
                    energyContainer.removeEnergy(energyPerTick);
                    for (Direction neighbourFace : Direction.values()) {
                        var pos = currentPos.offset(neighbourFace.getNormal());
                        BlockEntity neighbourTile = world.getBlockEntity(pos);
                        BlockState block = world.getBlockState(pos);
                        if (block.getBlock() instanceof EntityBlock entityBlock && !neighbourTile.isRemoved() && considerTile(neighbourTile)) {
                            tickBlockEntity(entityBlock, neighbourTile, block, pos);
                        }
                    }
                } else {
                    energyContainer.removeEnergy(energyPerTick / 2);
                    if (range == null) {
                        int area = getTier() * 2;
                        range = () -> BlockPos.betweenClosed(currentPos.offset(-area, -area, -area), currentPos.offset(area, area, area));
                    }
                    for (BlockPos pos : range.get()) {
                        if (pos.getY() > 256 || pos.getY() < 0) { // Early termination
                            continue;
                        }
                        if (world.isLoaded(pos)) {
                            for (int i = 0; i < speed; i++) {
                                if (GTValues.RNG.nextInt(100) < getTier()) {
                                    // Rongmario:
                                    // randomTick instead of updateTick since some modders can mistake where to put their code.
                                    // Fresh BlockState before every randomTick, this could easily change after every randomTick call
                                    BlockState state = world.getBlockState(pos);
                                    Block block = state.getBlock();
                                    if (block.isRandomlyTicking(state)) {
                                        block.randomTick(state, (ServerLevel) world, pos, world.random);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private <T extends BlockEntity> void tickBlockEntity(EntityBlock block, T neighbourTile, BlockState blockState, BlockPos pos) {
        BlockEntityTicker<T> ticker = block.getTicker(getLevel(), blockState, (BlockEntityType<T>) neighbourTile.getType());
        if(ticker != null) {
            for (int i = 0; i < speed; i++) {
                ticker.tick(getLevel(), pos, blockState, neighbourTile);
            }
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (isTEMode()) {
            Textures.WORLD_ACCELERATOR_TE_OVERLAY.renderOrientedState(renderState, translation, pipeline, getFrontFacing(), isActive, isWorkingEnabled());
        } else {
            Textures.WORLD_ACCELERATOR_OVERLAY.renderOrientedState(renderState, translation, pipeline, getFrontFacing(), isActive, isWorkingEnabled());
        }
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    protected ModularUI createUI(Player entityPlayer) {
        return null;
    }

    @Override
    public boolean onScrewdriverClick(Player playerIn, InteractionHand hand, Direction facing, VoxelShapeBlockHitResult hitResult) {
        if (!getWorld().isClientSide) {
            if (isTEMode()) {
                setTEMode(false);
                playerIn.displayClientMessage(Component.translatable("gregtech.machine.world_accelerator.mode_entity"), false);
            } else {
                setTEMode(true);
                playerIn.displayClientMessage(Component.translatable("gregtech.machine.world_accelerator.mode_tile"), false);
            }
        }
        return true;
    }

    public void setTEMode(boolean inverted) {
        tileMode = inverted;
        if (!getWorld().isClientSide) {
            writeCustomData(SYNC_TILE_MODE, b -> b.writeBoolean(tileMode));
            notifyBlockUpdate();
            markDirty();
        }
    }

    public boolean isTEMode() {
        return tileMode;
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.putBoolean("TileMode", tileMode);
        data.putBoolean("isPaused", isPaused);
        return data;
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        tileMode = data.getBoolean("TileMode");
        isPaused = data.getBoolean("isPaused");
    }

    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(tileMode);
        buf.writeBoolean(isPaused);
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        super.receiveInitialSyncData(buf);
        this.tileMode = buf.readBoolean();
        this.isPaused = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, FriendlyByteBuf buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == IS_WORKING) {
            this.isActive = buf.readBoolean();
            scheduleRenderUpdate();
        }
        if (dataId == SYNC_TILE_MODE) {
            this.tileMode = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    protected void setActive(boolean active) {
        this.isActive = active;
        markDirty();
        if (!getWorld().isClientSide) {
            writeCustomData(IS_WORKING, buf -> buf.writeBoolean(active));
        }
    }

    @Override
    public boolean isWorkingEnabled() {
        return !isPaused;
    }

    @Override
    public void setWorkingEnabled(boolean b) {
        isPaused = !b;
        notifyBlockUpdate();
    }

    LazyOptional<IControllable> controllableLazy = LazyOptional.of(() -> this);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return controllableLazy.cast();
        }
        return super.getCapability(capability, side);
    }
}
