package net.nemezanevem.gregtech.api.capability;


import net.minecraft.world.item.crafting.RecipeType;

public interface IMultipleRecipeMaps {

    /**
     * Used to get all possible RecipeMaps a Multiblock can run
     * @return array of RecipeMaps
     */
    RecipeType<?>[] getAvailableRecipeMaps();

    /**
     *
     * @return the currently selected RecipeMap
     */
    RecipeType<?> getCurrentRecipeMap();
}
