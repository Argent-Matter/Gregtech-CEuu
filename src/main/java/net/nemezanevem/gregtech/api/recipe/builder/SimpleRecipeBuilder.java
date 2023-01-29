package net.nemezanevem.gregtech.api.recipe.builder;

import net.nemezanevem.gregtech.api.recipe.GTRecipe;
import net.nemezanevem.gregtech.api.recipe.GTRecipeType;
import net.nemezanevem.gregtech.common.datagen.recipe.builder.GTRecipeBuilder;

public class SimpleRecipeBuilder extends GTRecipeBuilder<SimpleRecipeBuilder, GTRecipe> {

    public SimpleRecipeBuilder(GTRecipeType<GTRecipe> type) {
        super(type);
    }

    public SimpleRecipeBuilder(GTRecipe recipe, GTRecipeType<GTRecipe> recipeMap) {
        super(recipe, recipeMap);
    }

    public SimpleRecipeBuilder(GTRecipeBuilder<SimpleRecipeBuilder, GTRecipe> recipeBuilder) {
        super(recipeBuilder);
    }

    public SimpleRecipeBuilder copy() {
        return new SimpleRecipeBuilder(this);
    }

}
