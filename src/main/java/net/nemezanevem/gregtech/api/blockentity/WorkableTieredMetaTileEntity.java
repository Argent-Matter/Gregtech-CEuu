package net.nemezanevem.gregtech.api.blockentity;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.blockentity.multiblock.ICleanroomProvider;
import net.nemezanevem.gregtech.api.blockentity.multiblock.ICleanroomReceiver;
import net.nemezanevem.gregtech.api.capability.impl.*;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.client.renderer.ICubeRenderer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class WorkableTieredMetaTileEntity extends TieredMetaTileEntity implements IDataInfoProvider, ICleanroomReceiver {

    protected final RecipeLogicEnergy workable;
    protected final GTRecipeType<?> recipeMap;
    protected final ICubeRenderer renderer;

    private final Function<Integer, Integer> tankScalingFunction;

    public final boolean handlesRecipeOutputs;

    private ICleanroomProvider cleanroom;

    public WorkableTieredMetaTileEntity(ResourceLocation metaTileEntityId, GTRecipeType<?> recipeMap, ICubeRenderer renderer, int tier,
                                        Function<Integer, Integer> tankScalingFunction) {
        this(metaTileEntityId, recipeMap, renderer, tier, tankScalingFunction, true);
    }

    public WorkableTieredMetaTileEntity(ResourceLocation metaTileEntityId, GTRecipeType<?> recipeMap, ICubeRenderer renderer, int tier,
                                        Function<Integer, Integer> tankScalingFunction, boolean handlesRecipeOutputs) {
        super(metaTileEntityId, tier);
        this.renderer = renderer;
        this.handlesRecipeOutputs = handlesRecipeOutputs;
        this.workable = createWorkable(recipeMap);
        this.recipeMap = recipeMap;
        this.tankScalingFunction = tankScalingFunction;
        initializeInventory();
        reinitializeEnergyContainer();
    }

    protected RecipeLogicEnergy createWorkable(GTRecipeType<?> recipeMap) {
        return new RecipeLogicEnergy(this, recipeMap, () -> energyContainer);
    }

    @Override
    protected void reinitializeEnergyContainer() {
        long tierVoltage = GTValues.V[getTier()];
        if (isEnergyEmitter()) {
            this.energyContainer = EnergyContainerHandler.emitterContainer(this,
                    tierVoltage * 64L, tierVoltage, getMaxInputOutputAmperage());
        } else this.energyContainer = new EnergyContainerHandler(this, tierVoltage * 64L, tierVoltage, 2, 0L, 0L) {
            @Override
            public long getInputAmperage() {
                if (getEnergyCapacity() / 2 > getEnergyStored() && workable.isActive()) {
                    return 2;
                }
                return 1;
            }
        };
    }

    @Override
    protected long getMaxInputOutputAmperage() {
        return 2L;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        renderer.renderOrientedState(renderState, translation, pipeline, getFrontFacing(), workable.isActive(), workable.isWorkingEnabled());
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        if (workable == null) return new ItemStackHandler(0);
        return new NotifiableItemStackHandler(workable.getRecipeType().getMaxInputs(), this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        if (workable == null) return new ItemStackHandler(0);
        return new NotifiableItemStackHandler(workable.getRecipeType().getMaxOutputs(), this, true);
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        if (workable == null) return new FluidTankList(false);
        NotifiableFluidTank[] fluidImports = new NotifiableFluidTank[workable.getRecipeType().getMaxFluidInputs()];
        for (int i = 0; i < fluidImports.length; i++) {
            NotifiableFluidTank filteredFluidHandler = new NotifiableFluidTank(this.tankScalingFunction.apply(this.getTier()), this, false);
            fluidImports[i] = filteredFluidHandler;
        }
        return new FluidTankList(false, fluidImports);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        if (workable == null) return new FluidTankList(false);
        FluidTank[] fluidExports = new FluidTank[workable.getRecipeType().getMaxFluidOutputs()];
        for (int i = 0; i < fluidExports.length; i++) {
            fluidExports[i] = new NotifiableFluidTank(this.tankScalingFunction.apply(this.getTier()), this, true);
        }
        return new FluidTankList(false, fluidExports);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level player, List<Component> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(Component.translatable("gregtech.universal.tooltip.voltage_in", energyContainer.getInputVoltage(), GTValues.VNF[getTier()]));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        if (workable.getRecipeType().getMaxFluidInputs() != 0)
            tooltip.add(Component.translatable("gregtech.universal.tooltip.fluid_storage_capacity", this.tankScalingFunction.apply(getTier())));
    }

    public Function<Integer, Integer> getTankScalingFunction() {
        return tankScalingFunction;
    }

    public boolean isActive() {
        return workable.isActive() && workable.isWorkingEnabled();
    }

    @Override
    public SoundEvent getSound() {
        return workable.getRecipeType().getSound();
    }

    @Nonnull
    @Override
    public List<Component> getDataInfo() {
        List<Component> list = new ArrayList<>();

        if (workable != null) {
            list.add(Component.translatable("behavior.tricorder.workable_progress",
                    Component.translatable(Util.formatNumbers(workable.getProgress() / 20)).withStyle(ChatFormatting.GREEN),
                    Component.translatable(Util.formatNumbers(workable.getMaxProgress() / 20)).withStyle(ChatFormatting.YELLOW)
            ));

            if (energyContainer != null) {
                list.add(Component.translatable("behavior.tricorder.workable_stored_energy",
                        Component.translatable(Util.formatNumbers(energyContainer.getEnergyStored())).withStyle(ChatFormatting.GREEN),
                        Component.translatable(Util.formatNumbers(energyContainer.getEnergyCapacity())).withStyle(ChatFormatting.YELLOW)
                ));
            }
            // multi amp recipes: change 0 ? 0 : 1 to 0 ? 0 : amperage
            if (workable.getRecipeEUt() > 0) {
                list.add(Component.translatable("behavior.tricorder.workable_consumption",
                        Component.translatable(Util.formatNumbers(workable.getRecipeEUt())).withStyle(ChatFormatting.RED),
                        Component.translatable(Util.formatNumbers(workable.getRecipeEUt() == 0 ? 0 : 1)).withStyle(ChatFormatting.RED)
                ));
            } else {
                list.add(Component.translatable("behavior.tricorder.workable_production",
                        Component.translatable(Util.formatNumbers(workable.getRecipeEUt() * -1)).withStyle(ChatFormatting.RED),
                        Component.translatable(Util.formatNumbers(workable.getRecipeEUt() == 0 ? 0 : 1)).withStyle(ChatFormatting.RED)
                ));
            }
        }

        return list;
    }

    @Nullable
    @Override
    public ICleanroomProvider getCleanroom() {
        return this.cleanroom;
    }

    @Override
    public void setCleanroom(ICleanroomProvider provider) {
        this.cleanroom = provider;
    }
}
