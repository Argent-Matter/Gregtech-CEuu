package net.nemezanevem.gregtech.api.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.capability.IMultipleTankHandler;
import net.nemezanevem.gregtech.api.recipe.ingredient.ExtendedIngredient;
import net.nemezanevem.gregtech.api.recipe.ingredient.FluidIngredient;

import java.util.List;

public interface GTRecipeType<T extends Recipe<?>> extends RecipeType<T> {

    IChanceFunction DEFAULT_CHANCE_FUNCTION = (baseChance, boostPerTier, baseTier, machineTier) -> {
        int tierDiff = machineTier - baseTier;
        if (tierDiff <= 0) return baseChance; // equal or invalid tiers do not boost at all
        if (baseTier == GTValues.ULV) tierDiff--; // LV does not boost over ULV
        return baseChance + (boostPerTier * tierDiff);
    };

    IChanceFunction getChanceFunction();

    GTRecipe findRecipe(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs, int outputFluidTankCapacity);

    GTRecipe findRecipe(long voltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, int outputFluidTankCapacity);

    @FunctionalInterface
    interface IChanceFunction {

        /**
         * @param baseChance   the base chance of the recipe
         * @param boostPerTier the amount the chance is changed per tier over the base
         * @param baseTier     the lowest tier used to obtain un-boosted chances
         * @param boostTier    the tier the chance should be calculated at
         * @return the chance
         */
        int chanceFor(int baseChance, int boostPerTier, int baseTier, int boostTier);
    }
}
