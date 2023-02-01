package net.nemezanevem.gregtech.api.capability.impl;

import net.minecraft.util.Tuple;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.nemezanevem.gregtech.api.blockentity.IVoidable;
import net.nemezanevem.gregtech.api.blockentity.multiblock.RecipeTypeMultiblockController;
import net.nemezanevem.gregtech.api.capability.IHeatingCoil;
import net.nemezanevem.gregtech.api.capability.IMultipleTankHandler;
import net.nemezanevem.gregtech.api.recipe.GTRecipe;
import net.nemezanevem.gregtech.api.recipe.GTRecipeType;
import net.nemezanevem.gregtech.api.recipe.property.IRecipePropertyStorage;
import net.nemezanevem.gregtech.api.recipe.property.TemperatureProperty;
import net.nemezanevem.gregtech.common.datagen.recipe.builder.GTRecipeBuilder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import static net.nemezanevem.gregtech.api.recipe.logic.OverclockingLogic.heatingCoilOverclockingLogic;

/**
 * RecipeLogic for multiblocks that use temperature for raising speed and lowering energy usage
 * Used with RecipeTypes that run recipes using the {@link TemperatureProperty}
 */
public class HeatingCoilRecipeLogic extends MultiblockRecipeLogic {

    public HeatingCoilRecipeLogic(RecipeTypeMultiblockController metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    protected int[] runOverclockingLogic(@Nonnull IRecipePropertyStorage propertyStorage, int recipeEUt, long maxVoltage, int duration, int amountOC) {
        // apply maintenance penalties
        Tuple<Integer, Double> maintenanceValues = getMaintenanceValues();

        return heatingCoilOverclockingLogic(
                Math.abs(recipeEUt),
                maxVoltage,
                (int) Math.round(duration * maintenanceValues.getB()),
                amountOC,
                ((IHeatingCoil) metaTileEntity).getCurrentTemperature(),
                propertyStorage.getRecipePropertyValue(TemperatureProperty.getInstance(), 0)
        );
    }

    @Override
    public void applyParallelBonus(@NotNull GTRecipeBuilder<?> builder) {
        super.applyParallelBonus(builder);
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
}
