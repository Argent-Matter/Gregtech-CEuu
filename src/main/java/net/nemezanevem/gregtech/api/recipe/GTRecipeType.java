package net.nemezanevem.gregtech.api.recipe;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.capability.IMultipleTankHandler;
import net.nemezanevem.gregtech.common.datagen.recipe.builder.GTRecipeBuilder;

import java.util.List;

public interface GTRecipeType<T extends GTRecipeBuilder<T, R>, R extends GTRecipe> extends RecipeType<R> {

    IChanceFunction DEFAULT_CHANCE_FUNCTION = (baseChance, boostPerTier, baseTier, machineTier) -> {
        int tierDiff = machineTier - baseTier;
        if (tierDiff <= 0) return baseChance; // equal or invalid tiers do not boost at all
        if (baseTier == GTValues.ULV) tierDiff--; // LV does not boost over ULV
        return baseChance + (boostPerTier * tierDiff);
    };

    IChanceFunction getChanceFunction();

    R findRecipe(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs, int outputFluidTankCapacity);

    R findRecipe(long voltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, int outputFluidTankCapacity);

    ResourceLocation getId();

    GTRecipeBuilder<T, R> recipeBuilder();

    int getMinInputs();
    int getMaxInputs();
    int getMinOutputs();
    int getMaxOutputs();
    int getMinFluidInputs();
    int getMaxFluidInputs();
    int getMinFluidOutputs();
    int getMaxFluidOutputs();

    SoundEvent getSound();

    Component getLocalizedName();
    String getUnlocalizedName();

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
