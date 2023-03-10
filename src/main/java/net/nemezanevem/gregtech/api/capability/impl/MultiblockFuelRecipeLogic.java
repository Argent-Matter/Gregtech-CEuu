package net.nemezanevem.gregtech.api.capability.impl;

import net.minecraftforge.items.IItemHandlerModifiable;
import net.nemezanevem.gregtech.api.blockentity.IVoidable;
import net.nemezanevem.gregtech.api.blockentity.multiblock.GtMultiblockAbilities;
import net.nemezanevem.gregtech.api.blockentity.multiblock.MultiblockWithDisplayBase;
import net.nemezanevem.gregtech.api.blockentity.multiblock.ParallelLogicType;
import net.nemezanevem.gregtech.api.blockentity.multiblock.RecipeTypeMultiblockController;
import net.nemezanevem.gregtech.api.capability.IMaintenanceHatch;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.metatileentity.multiblock.RecipeTypeMultiblockController;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.recipeproperties.IRecipePropertyStorage;
import gregtech.common.ConfigHolder;
import net.nemezanevem.gregtech.api.capability.IMultipleTankHandler;
import net.nemezanevem.gregtech.api.recipe.GTRecipe;
import net.nemezanevem.gregtech.api.recipe.GTRecipeType;
import net.nemezanevem.gregtech.api.recipe.property.IRecipePropertyStorage;
import net.nemezanevem.gregtech.common.ConfigHolder;
import net.nemezanevem.gregtech.common.datagen.recipe.builder.GTRecipeBuilder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class MultiblockFuelRecipeLogic extends MultiblockRecipeLogic {

    protected long totalContinuousRunningTime;

    public MultiblockFuelRecipeLogic(RecipeTypeMultiblockController tileEntity) {
        super(tileEntity);
    }

    @Override
    protected int[] runOverclockingLogic(@Nonnull IRecipePropertyStorage propertyStorage, int recipeEUt, long maxVoltage, int recipeDuration, int amountOC) {
        // apply maintenance penalties
        MultiblockWithDisplayBase displayBase = this.metaTileEntity instanceof MultiblockWithDisplayBase ? (MultiblockWithDisplayBase) metaTileEntity : null;
        int numMaintenanceProblems = displayBase == null ? 0 : displayBase.getNumMaintenanceProblems();

        int[] overclock = null;
        if (displayBase != null && ConfigHolder.machines.enableMaintenance && displayBase.hasMaintenanceMechanics()) {
            IMaintenanceHatch hatch = displayBase.getAbilities(GtMultiblockAbilities.MAINTENANCE_HATCH.get()).get(0);
            double durationMultiplier = hatch.getDurationMultiplier();
            if (durationMultiplier != 1.0) {
                overclock = new int[]{recipeEUt * -1, (int) Math.round(recipeDuration / durationMultiplier)};
            }
        }
        if (overclock == null) {
            overclock = new int[]{recipeEUt * -1, recipeDuration};
        }

        overclock[1] = (int) (overclock[1] * (1 - 0.1 * numMaintenanceProblems));

        // no overclocking happens other than parallelization,
        // so return the recipe's values, with EUt made positive for it to be made negative later
        return overclock;
    }

    @Nonnull
    @Override
    public Enum<ParallelLogicType> getParallelLogicType() {
        return ParallelLogicType.MULTIPLY; //TODO APPEND_FLUIDS
    }

    @Override
    protected boolean hasEnoughPower(@Nonnull int[] resultOverclock) {
        // generators always have enough power to run recipes
        return true;
    }

    @Override
    public void applyParallelBonus(@Nonnull GTRecipeBuilder<?> builder) {
        // the builder automatically multiplies by -1, so nothing extra is needed here
        builder.setEUt(builder.getEUt());
    }

    @Override
    public GTRecipeBuilder<?> findMultipliedParallelRecipe(@NotNull GTRecipeType<?> recipeMap, @NotNull GTRecipe currentRecipe, @NotNull IItemHandlerModifiable inputs, @NotNull IMultipleTankHandler fluidInputs, @NotNull IItemHandlerModifiable outputs, @NotNull IMultipleTankHandler fluidOutputs, int parallelLimit, long maxVoltage, @NotNull IVoidable voidable) {
        return super.findMultipliedParallelRecipe(recipeMap, currentRecipe, inputs, fluidInputs, outputs, fluidOutputs, parallelLimit, maxVoltage, voidable);
    }

    @Override
    public GTRecipeBuilder<?> findAppendedParallelItemRecipe(@NotNull GTRecipeType<?> recipeMap, @NotNull IItemHandlerModifiable inputs, @NotNull IItemHandlerModifiable outputs, int parallelLimit, long maxVoltage, @NotNull IVoidable voidable) {
        return super.findAppendedParallelItemRecipe(recipeMap, inputs, outputs, parallelLimit, maxVoltage, voidable);
    }

    @Override
    public GTRecipe findParallelRecipe(@NotNull AbstractRecipeLogic logic, @NotNull GTRecipe currentRecipe, @NotNull IItemHandlerModifiable inputs, @NotNull IMultipleTankHandler fluidInputs, @NotNull IItemHandlerModifiable outputs, @NotNull IMultipleTankHandler fluidOutputs, long maxVoltage, int parallelLimit) {
        return super.findParallelRecipe(logic, currentRecipe, inputs, fluidInputs, outputs, fluidOutputs, maxVoltage, parallelLimit);
    }

    @Override
    public void tick() {
        super.tick();
        if (workingEnabled && isActive && progressTime > 0) {
                totalContinuousRunningTime ++;
        } else {
            totalContinuousRunningTime = 0;
        }
    }

    @Override
    public int getParallelLimit() {
        // parallel is limited by voltage
        return Integer.MAX_VALUE;
    }

    protected long boostProduction(long production) {
        return production;
    }

    @Override
    protected boolean drawEnergy(int recipeEUt, boolean simulate) {
        long euToDraw = boostProduction(recipeEUt);
        long resultEnergy = getEnergyStored() - euToDraw;
        if (resultEnergy >= 0L && resultEnergy <= getEnergyCapacity()) {
            if (!simulate) getEnergyContainer().changeEnergy(-euToDraw);
            return true;
        } else return false;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        totalContinuousRunningTime = 0;
    }
}
