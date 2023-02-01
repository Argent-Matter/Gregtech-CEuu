package net.nemezanevem.gregtech.api.capability;


public interface IMultipleRecipeTypes {

    /**
     * Used to get all possible RecipeTypes a Multiblock can run
     * @return array of RecipeTypes
     */
    GTRecipeType<?>[] getAvailableRecipeTypes();

    /**
     *
     * @return the currently selected RecipeType
     */
    GTRecipeType<?> getCurrentRecipeType();
}
