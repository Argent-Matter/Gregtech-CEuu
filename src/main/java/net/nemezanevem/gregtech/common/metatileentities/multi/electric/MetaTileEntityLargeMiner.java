package net.nemezanevem.gregtech.common.metatileentities.multi.electric;

import codechicken.lib.raytracer.VoxelShapeBlockHitResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.blockentity.IDataInfoProvider;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.blockentity.multiblock.GtMultiblockAbilities;
import net.nemezanevem.gregtech.api.blockentity.multiblock.IMultiblockPart;
import net.nemezanevem.gregtech.api.blockentity.multiblock.MultiblockWithDisplayBase;
import net.nemezanevem.gregtech.api.capability.*;
import net.nemezanevem.gregtech.api.capability.impl.EnergyContainerList;
import net.nemezanevem.gregtech.api.capability.impl.FluidTankList;
import net.nemezanevem.gregtech.api.capability.impl.ItemHandlerList;
import net.nemezanevem.gregtech.api.capability.impl.miner.MultiblockMinerLogic;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.widgets.AdvancedTextWidget;
import net.nemezanevem.gregtech.api.gui.widgets.ToggleButtonWidget;
import net.nemezanevem.gregtech.api.pattern.BlockPattern;
import net.nemezanevem.gregtech.api.pattern.FactoryBlockPattern;
import net.nemezanevem.gregtech.api.pattern.PatternMatchContext;
import net.nemezanevem.gregtech.api.pattern.TraceabilityPredicate;
import net.nemezanevem.gregtech.api.recipe.GtRecipeTypes;
import net.nemezanevem.gregtech.api.unification.material.GtMaterials;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.client.renderer.ICubeRenderer;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import net.nemezanevem.gregtech.common.block.BlockMetalCasing;
import net.nemezanevem.gregtech.common.block.MetaBlocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static net.nemezanevem.gregtech.api.unification.material.GtMaterials.DrillingFluid;


public class MetaTileEntityLargeMiner extends MultiblockWithDisplayBase implements IMiner, IControllable, IDataInfoProvider {

    private static final int CHUNK_LENGTH = 16;

    private final Material material;
    private final int tier;

    private IEnergyContainer energyContainer;
    protected IMultipleTankHandler inputFluidInventory;
    protected IItemHandlerModifiable outputInventory;

    private boolean silkTouch = false;
    private boolean chunkMode = false;

    private boolean isInventoryFull = false;

    private final int drillingFluidConsumePerTick;
    private final String romanNumeralString;

    private final MultiblockMinerLogic minerLogic;

    public MetaTileEntityLargeMiner(ResourceLocation metaTileEntityId, int tier, int speed, int maximumChunkRadius, int fortune, Material material, int drillingFluidConsumePerTick) {
        super(metaTileEntityId);
        this.material = material;
        this.tier = tier;
        this.drillingFluidConsumePerTick = drillingFluidConsumePerTick;
        this.romanNumeralString = Util.romanNumeralString(fortune);
        this.minerLogic = new MultiblockMinerLogic(this, fortune, speed, maximumChunkRadius * CHUNK_LENGTH, getBaseTexture(null), GtRecipeTypes.MACERATOR_RECIPES.get());
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLargeMiner(metaTileEntityId, this.tier, this.minerLogic.getSpeed(), this.minerLogic.getMaximumRadius() / CHUNK_LENGTH, this.minerLogic.getFortune(), getMaterial(), getDrillingFluidConsumePerTick());
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();
        if (this.minerLogic.isActive())
            this.minerLogic.setActive(false);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeAbilities();
    }

    private void initializeAbilities() {
        this.inputFluidInventory = new FluidTankList(false, getAbilities(GtMultiblockAbilities.IMPORT_FLUIDS.get()));
        this.outputInventory = new ItemHandlerList(getAbilities(GtMultiblockAbilities.EXPORT_ITEMS.get()));
        this.energyContainer = new EnergyContainerList(getAbilities(GtMultiblockAbilities.INPUT_ENERGY.get()));
        this.minerLogic.setVoltageTier(Util.getTierByVoltage(this.energyContainer.getInputVoltage()));
        this.minerLogic.setOverclockAmount(Math.max(1, Util.getTierByVoltage(this.energyContainer.getInputVoltage()) - this.tier));
        this.minerLogic.initPos(getPos(), this.minerLogic.getCurrentRadius());
    }

    private void resetTileAbilities() {
        this.inputFluidInventory = new FluidTankList(true);
        this.outputInventory = new ItemStackHandler(0);
        this.energyContainer = new EnergyContainerList(Lists.newArrayList());
    }

    public int getEnergyTier() {
        if (energyContainer == null) return this.tier;
        return Math.min(this.tier + 1 , Math.max(this.tier, Util.getFloorTierByVoltage(energyContainer.getInputVoltage())));
    }

    @Override
    public boolean drainEnergy(boolean simulate) {
        long energyToDrain = GTValues.VA[getEnergyTier()];
        long resultEnergy = energyContainer.getEnergyStored() - energyToDrain;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.changeEnergy(-energyToDrain);
            return true;
        }
        return false;
    }

    @Override
    public boolean drainFluid(boolean simulate) {
        FluidStack drillingFluid = DrillingFluid.get().getFluid(this.drillingFluidConsumePerTick * this.minerLogic.getOverclockAmount());
        FluidStack fluidStack = inputFluidInventory.getTankAt(0).getFluid();
        if (fluidStack != null && fluidStack.isFluidEqual(DrillingFluid.get().getFluid(1)) && fluidStack.getAmount() >= drillingFluid.getAmount()) {
            if (!simulate)
                inputFluidInventory.drain(drillingFluid, IFluidHandler.FluidAction.EXECUTE);
            return true;
        }
        return false;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), this.minerLogic.isWorking(), this.isWorkingEnabled());
        minerLogic.renderPipe(renderState, translation, pipeline);
    }

    @Override
    protected void updateFormedValid() {
        this.minerLogic.performMining();
        if (!getWorld().isClientSide && this.minerLogic.wasActiveAndNeedsUpdate()) {
            this.minerLogic.setWasActiveAndNeedsUpdate(false);
            this.minerLogic.setActive(false);
        }
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return material == null ? null : FactoryBlockPattern.start()
                .aisle("XXX", "#F#", "#F#", "#F#", "###", "###", "###")
                .aisle("XXX", "FCF", "FCF", "FCF", "#F#", "#F#", "#F#")
                .aisle("XSX", "#F#", "#F#", "#F#", "###", "###", "###")
                .where('S', selfPredicate())
                .where('X', states(getCasingState())
                        .or(abilities(GtMultiblockAbilities.EXPORT_ITEMS.get()).setMaxGlobalLimited(1).setPreviewCount(1))
                        .or(abilities(GtMultiblockAbilities.IMPORT_FLUIDS.get()).setExactLimit(1).setPreviewCount(1))
                        .or(abilities(GtMultiblockAbilities.INPUT_ENERGY.get()).setMinGlobalLimited(1).setMaxGlobalLimited(3).setPreviewCount(1)))
                .where('C', states(getCasingState()))
                .where('F', getFramePredicate())
                .where('#', any())
                .build();
    }

    @Override
    public Component[] getDescription() {
        return new Component[]{Component.translatable("gregtech.machine.miner.multi.description")};
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level player, @Nonnull List<Component> tooltip, boolean advanced) {
        int workingRadius = this.minerLogic.getCurrentRadius() / CHUNK_LENGTH;
        tooltip.add(Component.translatable("gregtech.machine.miner.multi.modes"));
        tooltip.add(Component.translatable("gregtech.machine.miner.multi.production"));
        tooltip.add(Component.translatable("gregtech.machine.miner.fluid_usage", getDrillingFluidConsumePerTick(), DrillingFluid.get().getLocalizedName()));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.working_area_chunks_max", workingRadius, workingRadius));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.energy_tier_range", GTValues.VNF[this.tier], GTValues.VNF[this.tier + 1]));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable Level world, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.tool_action.screwdriver.toggle_mode_covers"));
        tooltip.add(Component.translatable("gregtech.tool_action.wrench.set_facing"));
        if (getSound() != null) {
            tooltip.add(Component.translatable("gregtech.tool_action.hammer"));
        }
        tooltip.add(Component.translatable("gregtech.tool_action.crowbar"));
    }

    @Override
    protected void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);

        if (this.isStructureFormed()) {
            if (energyContainer != null && energyContainer.getEnergyCapacity() > 0) {
                int energyContainer = getEnergyTier();
                long maxVoltage = GTValues.V[energyContainer];
                String voltageName = GTValues.VNF[energyContainer];
                textList.add(Component.translatable("gregtech.multiblock.max_energy_per_tick", maxVoltage, voltageName));
            }

            textList.add(Component.translatable("gregtech.machine.miner.startx", this.minerLogic.getX().get() == Integer.MAX_VALUE ? 0 : this.minerLogic.getX().get()));
            textList.add(Component.translatable("gregtech.machine.miner.starty", this.minerLogic.getY().get() == Integer.MAX_VALUE ? 0 : this.minerLogic.getY().get()));
            textList.add(Component.translatable("gregtech.machine.miner.startz", this.minerLogic.getZ().get() == Integer.MAX_VALUE ? 0 : this.minerLogic.getZ().get()));
            textList.add(Component.translatable("gregtech.machine.miner.chunkradius", this.minerLogic.getCurrentRadius() / CHUNK_LENGTH));
            if (this.minerLogic.isDone())
                textList.add(Component.translatable("gregtech.multiblock.large_miner.done").withStyle(ChatFormatting.GREEN));
            else if (this.minerLogic.isWorking())
                textList.add(Component.translatable("gregtech.multiblock.large_miner.working").withStyle(ChatFormatting.GOLD));
            else if (!this.isWorkingEnabled())
                textList.add(Component.translatable("gregtech.multiblock.work_paused"));
            if (this.isInventoryFull)
                textList.add(Component.translatable("gregtech.multiblock.large_miner.invfull").withStyle(ChatFormatting.RED));
            if (!drainFluid(true))
                textList.add(Component.translatable("gregtech.multiblock.large_miner.needsfluid").withStyle(ChatFormatting.RED));
            if (!drainEnergy(true))
                textList.add(Component.translatable("gregtech.multiblock.large_miner.needspower").withStyle(ChatFormatting.RED));
        }
    }

    private void addDisplayText2(List<Component> textList) {
        if (this.isStructureFormed()) {
            MutableComponent mCoords = ((MutableComponent) Component.literal("    "))
                .append(Component.translatable("gregtech.machine.miner.minex", this.minerLogic.getMineX().get()))
                .append("\n    ")
                .append(Component.translatable("gregtech.machine.miner.miney", this.minerLogic.getMineY().get()))
                .append("\n    ")
                .append(Component.translatable("gregtech.machine.miner.minez", this.minerLogic.getMineZ().get()));
            textList.add(mCoords);
        }
    }

    public BlockState getCasingState() {
        if (this.material.equals(GtMaterials.Titanium.get()))
            return MetaBlocks.METAL_CASING.get().getState(BlockMetalCasing.MetalCasingType.TITANIUM_STABLE);
        if (this.material.equals(GtMaterials.TungstenSteel.get()))
            return MetaBlocks.METAL_CASING.get().getState(BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST);
        return MetaBlocks.METAL_CASING.get().getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    @Nonnull
    private TraceabilityPredicate getFramePredicate() {
        if (this.material.equals(GtMaterials.Titanium.get()))
            return frames(GtMaterials.Titanium.get());
        if (this.material.equals(GtMaterials.TungstenSteel.get()))
            return frames(GtMaterials.TungstenSteel.get());
        return frames(GtMaterials.Steel.get());
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        if (this.material.equals(GtMaterials.Titanium.get()))
            return Textures.STABLE_TITANIUM_CASING;
        if (this.material.equals(GtMaterials.TungstenSteel.get()))
            return Textures.ROBUST_TUNGSTENSTEEL_CASING;
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.putBoolean("chunkMode", chunkMode);
        data.putBoolean("silkTouch", silkTouch);
        return this.minerLogic.writeToNBT(data);
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        chunkMode = data.getBoolean("chunkMode");
        silkTouch = data.getBoolean("silkTouch");
        this.minerLogic.readFromNBT(data);
    }


    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        super.writeInitialSyncData(buf);
        this.minerLogic.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        super.receiveInitialSyncData(buf);
        this.minerLogic.receiveInitialSyncData(buf);
    }

    @Override
    public void receiveCustomData(int dataId, FriendlyByteBuf buf) {
        super.receiveCustomData(dataId, buf);
        this.minerLogic.receiveCustomData(dataId, buf);
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        if (this.tier == 5)
            return Textures.LARGE_MINER_OVERLAY_ADVANCED;
        if (this.tier == 6)
            return Textures.LARGE_MINER_OVERLAY_ADVANCED_2;
        return Textures.LARGE_MINER_OVERLAY_BASIC;
    }

    public long getMaxVoltage() {
        return GTValues.V[Util.getTierByVoltage(energyContainer.getInputVoltage())];
    }

    @Override
    protected ModularUI createUI(Player entityPlayer) {
        ModularUI.Builder builder = ModularUI.extendedBuilder();
        builder.image(7, 4, 162, 121, GuiTextures.DISPLAY);
        builder.label(11, 9, this.getMetaFullName(), 0xFFFFFF);
        builder.widget((new AdvancedTextWidget(11, 19, this::addDisplayText,
                0xFFFFFF)).setMaxWidthLimit(139).setClickHandler(this::handleDisplayClick));
        builder.widget((new AdvancedTextWidget(63, 30, this::addDisplayText2,
                0xFFFFFF)).setMaxWidthLimit(68).setClickHandler(this::handleDisplayClick));
        builder.bindPlayerInventory(entityPlayer.getInventory(), 134);

        builder.widget(new ToggleButtonWidget(133, 107, 18, 18,
                this.minerLogic::isChunkMode, this.minerLogic::setChunkMode).setButtonTexture(GuiTextures.BUTTON_CHUNK_MODE)
                .setTooltipText("gregtech.gui.chunkmode"));
        builder.widget(new ToggleButtonWidget(151, 107, 18, 18,
                this.minerLogic::isSilkTouchMode, this.minerLogic::setSilkTouchMode).setButtonTexture(GuiTextures.BUTTON_SILK_TOUCH_MODE)
                .setTooltipText("gregtech.gui.silktouch"));

        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public boolean onScrewdriverClick(Player playerIn, InteractionHand hand, Direction facing, VoxelShapeBlockHitResult hitResult) {
        if (getWorld().isClientSide || !this.isStructureFormed())
            return true;

        if (!this.minerLogic.isActive()) {
            int currentRadius = this.minerLogic.getCurrentRadius();
            if (currentRadius - CHUNK_LENGTH == 0)
                this.minerLogic.setCurrentRadius(this.minerLogic.getMaximumRadius());
            else
                this.minerLogic.setCurrentRadius(currentRadius - CHUNK_LENGTH);

            this.minerLogic.resetArea();

            playerIn.sendSystemMessage(Component.translatable("gregtech.multiblock.large_miner.radius", this.minerLogic.getCurrentRadius()));
        } else {
            playerIn.sendSystemMessage(Component.translatable("gregtech.multiblock.large_miner.errorradius"));
        }
        return true;
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @Override
    public boolean isInventoryFull() {
        return this.isInventoryFull;
    }

    @Override
    public void setInventoryFull(boolean isFull) {
        this.isInventoryFull = isFull;
    }

    public Material getMaterial() {
        return material;
    }

    public int getTier() {
        return this.tier;
    }

    public int getDrillingFluidConsumePerTick() {
        return this.drillingFluidConsumePerTick;
    }

    public String getRomanNumeralString() {
        return this.romanNumeralString;
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.minerLogic.isWorkingEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.minerLogic.setWorkingEnabled(isActivationAllowed);
    }

    public int getMaxChunkRadius() {
        return this.minerLogic.getMaximumRadius() / CHUNK_LENGTH;
    }

    LazyOptional<IControllable> controllableLazyOptional = LazyOptional.of(() -> this);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return controllableLazyOptional.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public IItemHandlerModifiable getExportItems() {
        return this.outputInventory;
    }

    @Override
    public SoundEvent getSound() {
        return GTSoundEvents.MINER;
    }

    @Override
    public boolean isActive() {
        return minerLogic.isActive() && isWorkingEnabled();
    }

    @Nonnull
    @Override
    public List<Component> getDataInfo() {
        return Collections.singletonList(Component.translatable("gregtech.multiblock.large_miner.radius", this.minerLogic.getCurrentRadius()));
    }

    @Override
    protected boolean shouldShowVoidingModeButton() {
        return false;
    }
}
