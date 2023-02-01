package net.nemezanevem.gregtech.api.blockentity;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.IActiveOutputSide;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.FuelRecipeLogic;
import gregtech.api.capability.impl.RecipeLogicEnergy;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeType;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.PipelineUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.Player;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class SimpleGeneratorMetaTileEntity extends WorkableTieredMetaTileEntity implements IActiveOutputSide {

    private static final int FONT_HEIGHT = 9; // Minecraft's FontRenderer FONT_HEIGHT value

    public SimpleGeneratorMetaTileEntity(ResourceLocation metaTileEntityId, GTRecipeType<?> recipeMap, ICubeRenderer renderer, int tier,
                                         Function<Integer, Integer> tankScalingFunction) {
        this(metaTileEntityId, recipeMap, renderer, tier, tankScalingFunction, false);
    }

    public SimpleGeneratorMetaTileEntity(ResourceLocation metaTileEntityId, GTRecipeType<?> recipeMap, ICubeRenderer renderer, int tier,
                                         Function<Integer, Integer> tankScalingFunction, boolean handlesRecipeOutputs) {
        super(metaTileEntityId, recipeMap, renderer, tier, tankScalingFunction, handlesRecipeOutputs);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new SimpleGeneratorMetaTileEntity(metaTileEntityId, workable.getRecipeType(), renderer, getTier(), getTankScalingFunction(), handlesRecipeOutputs);
    }

    @Override
    protected RecipeLogicEnergy createWorkable(GTRecipeType<?> recipeMap) {
        return new FuelRecipeLogic(this, recipeMap, () -> energyContainer);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        if (handlesRecipeOutputs)
            return super.createExportFluidHandler();
        return new FluidTankList(false);
    }

    @Override
    protected void reinitializeEnergyContainer() {
        super.reinitializeEnergyContainer();
        ((EnergyContainerHandler) this.energyContainer).setSideOutputCondition(side -> side == getFrontFacing());
    }

    @Override
    public boolean hasFrontFacing() {
        return true;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER) {
            if (fluidInventory.getTankProperties().length > 0) {
                return CapabilityFluidHandler.FLUID_HANDLER.cast(fluidInventory);
            }
            return null;
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER) {
            if (itemInventory.getSlots() > 0) {
                return CapabilityItemHandler.ITEM_HANDLER.cast(itemInventory);
            }
            return null;
        }
        return super.getCapability(capability, side);
    }

    protected ModularUI.Builder createGuiTemplate(Player player) {
        GTRecipeType<?> workableRecipeType = workable.getRecipeType();
        int yOffset = 0;
        if (workableRecipeType.getMaxInputs() >= 6 || workableRecipeType.getMaxFluidInputs() >= 6 ||
                workableRecipeType.getMaxOutputs() >= 6 || workableRecipeType.getMaxFluidOutputs() >= 6)
            yOffset = FONT_HEIGHT;


        ModularUI.Builder builder;
        if (handlesRecipeOutputs) builder = workableRecipeType.createUITemplate(workable::getProgressPercent, importItems, exportItems, importFluids, exportFluids, yOffset);
        else builder = workableRecipeType.createUITemplateNoOutputs(workable::getProgressPercent, importItems, exportItems, importFluids, exportFluids, yOffset);
        builder.widget(new LabelWidget(6, 6, getMetaFullName()))
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, yOffset);

        builder.widget(new CycleButtonWidget(7, 62 + yOffset, 18, 18,
                workable.getAvailableOverclockingTiers(), workable::getOverclockTier, workable::setOverclockTier)
                .setTooltipHoverString("gregtech.gui.overclock.description")
                .setButtonTexture(GuiTextures.BUTTON_OVERCLOCK));

        return builder;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.renderer.renderOrientedState(renderState, translation, pipeline, getFrontFacing(), workable.isActive(), workable.isWorkingEnabled());
        Textures.ENERGY_OUT.renderSided(getFrontFacing(), renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[getTier()]));
    }

    @Override
    protected ModularUI createUI(Player entityPlayer) {
        return createGuiTemplate(entityPlayer).build(getHolder(), entityPlayer);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level player, @Nonnull List<Component> tooltip, boolean advanced) {
        String key = this.metaTileEntityId.getPath().split("\\.")[0];
        String mainKey = String.format("gregtech.machine.%s.tooltip", key);
        if (I18n.hasKey(mainKey)) {
            tooltip.add(1, Component.translatable(mainKey));
        }
        tooltip.add(Component.translatable("gregtech.universal.tooltip.voltage_out", energyContainer.getOutputVoltage(), GTValues.VNF[getTier()]));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        if (recipeMap.getMaxFluidInputs() > 0 || recipeMap.getMaxFluidOutputs() > 0)
            tooltip.add(Component.translatable("gregtech.universal.tooltip.fluid_storage_capacity", this.getTankScalingFunction().apply(getTier())));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable Level world, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(Component.translatable("gregtech.tool_action.wrench.set_facing"));
        tooltip.add(Component.translatable("gregtech.tool_action.soft_mallet.reset"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public boolean isAutoOutputItems() {
        return false;
    }

    @Override
    public boolean isAutoOutputFluids() {
        return false;
    }

    @Override
    public boolean isAllowInputFromOutputSideItems() {
        return false;
    }

    @Override
    public boolean isAllowInputFromOutputSideFluids() {
        return false;
    }

    @Override
    protected long getMaxInputOutputAmperage() {
        return 1L;
    }

    @Override
    protected boolean isEnergyEmitter() {
        return true;
    }

    @Override
    public boolean canVoidRecipeItemOutputs() {
        return !handlesRecipeOutputs;
    }

    @Override
    public boolean canVoidRecipeFluidOutputs() {
        return !handlesRecipeOutputs;
    }
}
