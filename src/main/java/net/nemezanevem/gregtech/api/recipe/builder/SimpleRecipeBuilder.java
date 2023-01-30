package net.nemezanevem.gregtech.api.recipe.builder;

import net.nemezanevem.gregtech.api.recipe.GTRecipe;
import net.nemezanevem.gregtech.api.recipe.GTRecipeTypeImpl;
import net.nemezanevem.gregtech.common.datagen.recipe.builder.GTRecipeBuilder;

public class SimpleRecipeBuilder extends GTRecipeBuilder<SimpleRecipeBuilder> {

    public SimpleRecipeBuilder() {

    }

    public SimpleRecipeBuilder(GTRecipe recipe, GTRecipeTypeImpl<SimpleRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public SimpleRecipeBuilder(SimpleRecipeBuilder recipeBuilder) {
        super(recipeBuilder);
    }

    public SimpleRecipeBuilder copy() {
        return new SimpleRecipeBuilder(this);
    }

}
