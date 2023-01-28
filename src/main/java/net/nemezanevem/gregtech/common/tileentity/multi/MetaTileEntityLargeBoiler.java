package net.nemezanevem.gregtech.common.tileentity.multi;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.nemezanevem.gregtech.api.capability.impl.BoilerRecipeLogic;
import net.nemezanevem.gregtech.api.capability.impl.FluidTankList;
import net.nemezanevem.gregtech.api.capability.impl.ItemHandlerList;
import net.nemezanevem.gregtech.api.tileentity.multiblock.MultiblockWithDisplayBase;

import java.util.List;

public class MetaTileEntityLargeBoiler extends MultiblockWithDisplayBase {

    public final BoilerType boilerType;
    protected BoilerRecipeLogic recipeLogic;
    private FluidTankList fluidImportInventory;
    private ItemHandlerList itemImportInventory;
    private FluidTankList steamOutputTank;

    private int throttlePercentage = 100;

    public MetaTileEntityLargeBoiler(ResourceLocation metaTileEntityId, BoilerType boilerType) {
        super(metaTileEntityId);
        this.boilerType = boilerType;
        this.recipeLogic = new BoilerRecipeLogic(this);
        resetTileAbilities();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLargeBoiler(metaTileEntityId, boilerType);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeAbilities();
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();
        this.throttlePercentage = 100;
        this.recipeLogic.invalidate();
    }

    private void initializeAbilities() {
        this.fluidImportInventory = new FluidTankList(true, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.itemImportInventory = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.steamOutputTank = new FluidTankList(true, getAbilities(MultiblockAbility.EXPORT_FLUIDS));
    }

    private void resetTileAbilities() {
        this.fluidImportInventory = new FluidTankList(true);
        this.itemImportInventory = new ItemHandlerList(Collections.emptyList());
        this.steamOutputTank = new FluidTankList(true);
    }

    @Override
    protected void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isStructureFormed()) {
            int efficiency = recipeLogic.getHeatScaled();
            textList.add(new TextComponentTranslation("gregtech.multiblock.large_boiler.efficiency",
                    (efficiency == 0 ? DARK_RED : efficiency <= 40 ? RED : efficiency == 100 ? GREEN : YELLOW).toString() + efficiency + "%"));
            textList.add(new TextComponentTranslation("gregtech.multiblock.large_boiler.steam_output", recipeLogic.getLastTickSteam()));

            ITextComponent throttleText = new TextComponentTranslation("gregtech.multiblock.large_boiler.throttle",
                    AQUA.toString() + getThrottle() + "%");
            withHoverTextTranslate(throttleText, "gregtech.multiblock.large_boiler.throttle.tooltip");
            textList.add(throttleText);

            ITextComponent buttonText = new TextComponentTranslation("gregtech.multiblock.large_boiler.throttle_modify");
            buttonText.appendText(" ");
            buttonText.appendSibling(withButton(new TextComponentString("[-]"), "sub"));
            buttonText.appendText(" ");
            buttonText.appendSibling(withButton(new TextComponentString("[+]"), "add"));
            textList.add(buttonText);
        }
    }

    @Override
    protected void handleDisplayClick(String componentData, ClickData clickData) {
        super.handleDisplayClick(componentData, clickData);
        int result = componentData.equals("add") ? 5 : -5;
        this.throttlePercentage = MathHelper.clamp(throttlePercentage + result, 25, 100);
    }

    @Override
    public boolean isActive() {
        return super.isActive() && recipeLogic.isActive() && recipeLogic.isWorkingEnabled();
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return boilerType == null ? null : FactoryBlockPattern.start()
                .aisle("XXX", "CCC", "CCC", "CCC")
                .aisle("XXX", "CPC", "CPC", "CCC")
                .aisle("XXX", "CSC", "CCC", "CCC")
                .where('S', selfPredicate())
                .where('P', states(boilerType.pipeState))
                .where('X', states(boilerType.fireboxState).setMinGlobalLimited(4)
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMinGlobalLimited(1))
                        .or(abilities(MultiblockAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                        .or(autoAbilities())) // muffler, maintenance
                .where('C', states(boilerType.casingState).setMinGlobalLimited(20)
                        .or(abilities(MultiblockAbility.EXPORT_FLUIDS).setMinGlobalLimited(1)))
                .build();
    }

    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("gregtech.multiblock.large_boiler.description")};
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.multiblock.large_boiler.rate_tooltip", (int) (boilerType.steamPerTick() * 20 * boilerType.runtimeBoost(20) / 20.0)));
        tooltip.add(I18n.format("gregtech.multiblock.large_boiler.heat_time_tooltip", boilerType.getTicksToBoiling() / 20));
        tooltip.add(I18n.format("gregtech.universal.tooltip.base_production_fluid", boilerType.steamPerTick()));
        tooltip.add(TooltipHelper.BLINKING_RED + I18n.format("gregtech.multiblock.large_boiler.explosion_tooltip"));
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), isActive(), recipeLogic.isWorkingEnabled());
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return boilerType.frontOverlay;
    }

    private boolean isFireboxPart(IMultiblockPart sourcePart) {
        return isStructureFormed() && (((MetaTileEntity) sourcePart).getPos().getY() < getPos().getY());
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        if (sourcePart != null && isFireboxPart(sourcePart)) {
            return isActive() ? boilerType.fireboxActiveRenderer : boilerType.fireboxIdleRenderer;
        }
        return boilerType.casingRenderer;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return true;
    }

    @Override
    public SoundEvent getSound() {
        return GTSoundEvents.BOILER;
    }

    @Override
    protected void updateFormedValid() {
        this.recipeLogic.update();
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag data) {
        data.putInt("ThrottlePercentage", throttlePercentage);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        throttlePercentage = data.getInt("ThrottlePercentage");
        super.readFromNBT(data);
    }

    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        super.writeInitialSyncData(buf);
        buf.writeVarInt(throttlePercentage);
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        super.receiveInitialSyncData(buf);
        throttlePercentage = buf.readVarInt();
    }

    public int getThrottle() {
        return throttlePercentage;
    }

    @Override
    public IItemHandlerModifiable getImportItems() {
        return itemImportInventory;
    }

    @Override
    public FluidTankList getImportFluids() {
        return fluidImportInventory;
    }

    @Override
    public FluidTankList getExportFluids() {
        return steamOutputTank;
    }

    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        return !(trait instanceof BoilerRecipeLogic);
    }

    @Override
    protected boolean shouldShowVoidingModeButton() {
        return false;
    }
}
