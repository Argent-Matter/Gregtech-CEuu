package net.nemezanevem.gregtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.RecipeTypePrimitiveMultiblockController;
import gregtech.api.recipes.RecipeType;
import gregtech.api.recipes.recipeproperties.IRecipePropertyStorage;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.blockentity.multiblock.RecipeTypePrimitiveMultiblockController;
import net.nemezanevem.gregtech.api.recipe.property.IRecipePropertyStorage;

import javax.annotation.Nonnull;

import static gregtech.api.recipes.logic.OverclockingLogic.standardOverclockingLogic;
import static net.nemezanevem.gregtech.api.recipe.logic.OverclockingLogic.standardOverclockingLogic;

/**
 * Recipe Logic for a Multiblock that does not require power.
 */
public class PrimitiveRecipeLogic extends AbstractRecipeLogic {

    public PrimitiveRecipeLogic(RecipeTypePrimitiveMultiblockController tileEntity, GTRecipeType<?> recipeMap) {
        super(tileEntity, recipeMap);
    }

    @Override
    protected long getEnergyInputPerSecond() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected long getEnergyStored() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected long getEnergyCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected boolean drawEnergy(int recipeEUt, boolean simulate) {
        return true; // spoof energy being drawn
    }

    @Override
    protected long getMaxVoltage() {
        return GTValues.LV;
    }

    @Override
    protected int[] runOverclockingLogic(@Nonnull IRecipePropertyStorage propertyStorage, int recipeEUt, long maxVoltage, int recipeDuration, int amountOC) {
        return standardOverclockingLogic(
                1,
                getMaxVoltage(),
                recipeDuration,
                amountOC,
                getOverclockingDurationDivisor(),
                getOverclockingVoltageMultiplier()

        );
    }

    @Override
    public long getMaximumOverclockVoltage() {
        return GTValues.V[GTValues.LV];
    }

    /**
     * Used to reset cached values in the Recipe Logic on structure deform
     */
    public void invalidate() {
        previousRecipe = null;
        progressTime = 0;
        maxProgressTime = 0;
        recipeEUt = 0;
        fluidOutputs = null;
        itemOutputs = null;
        setActive(false); // this marks dirty for us
    }
}
