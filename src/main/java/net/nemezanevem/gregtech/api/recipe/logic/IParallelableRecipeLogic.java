package net.nemezanevem.gregtech.api.recipe.logic;

import net.minecraftforge.items.IItemHandlerModifiable;
import net.nemezanevem.gregtech.api.blockentity.multiblock.ParallelLogicType;
import net.nemezanevem.gregtech.api.capability.IMultipleTankHandler;
import net.nemezanevem.gregtech.api.capability.impl.AbstractRecipeLogic;
import net.nemezanevem.gregtech.api.recipe.GTRecipe;
import net.nemezanevem.gregtech.api.recipe.GTRecipeType;
import net.nemezanevem.gregtech.api.blockentity.IVoidable;
import net.nemezanevem.gregtech.common.datagen.recipe.builder.GTRecipeBuilder;

import javax.annotation.Nonnull;

public interface IParallelableRecipeLogic {

    /**
     * Method which applies bonuses or penalties to the recipe based on the parallelization factor,
     * such as EU consumption or processing speed.
     *
     * @param builder the recipe builder
     */
    default void applyParallelBonus(@Nonnull GTRecipeBuilder<?> builder) {
    }

    /**
     * Method which finds a recipe which can be parallelized, works by multiplying the recipe by the parallelization factor,
     * and shrinking the recipe till its outputs can fit
     *
     * @param recipeMap     the recipe map
     * @param currentRecipe recipe to be parallelized
     * @param inputs        input item handler
     * @param fluidInputs   input fluid handler
     * @param outputs       output item handler
     * @param fluidOutputs  output fluid handler
     * @param parallelLimit the maximum number of parallel recipes to be performed
     * @param maxVoltage    the voltage limit on the number of parallel recipes to be performed
     * @param voidable      the voidable performing the parallel recipe
     * @return the recipe builder with the parallelized recipe. returns null the recipe can't fit
     */
    default GTRecipeBuilder<?> findMultipliedParallelRecipe(@Nonnull GTRecipeType<?> recipeMap, @Nonnull GTRecipe currentRecipe, @Nonnull IItemHandlerModifiable inputs, @Nonnull IMultipleTankHandler fluidInputs, @Nonnull IItemHandlerModifiable outputs, @Nonnull IMultipleTankHandler fluidOutputs, int parallelLimit, long maxVoltage, @Nonnull IVoidable voidable) {
        return ParallelLogic.doParallelRecipes(
                currentRecipe,
                recipeMap,
                inputs,
                fluidInputs,
                outputs,
                fluidOutputs,
                parallelLimit,
                maxVoltage,
                voidable);
    }

    /**
     * Method which finds a recipe then multiplies it, then appends it to the builds up to the parallelization factor,
     * or filling the output
     *
     * @param recipeMap     the recipe map
     * @param inputs        input item handler
     * @param outputs       output item handler
     * @param parallelLimit the maximum number of parallel recipes to be performed
     * @param maxVoltage    the voltage limit on the number of parallel recipes to be performed
     * @param voidable      the voidable performing the parallel recipe
     * @return the recipe builder with the parallelized recipe. returns null the recipe can't fit
     */
    default GTRecipeBuilder<?> findAppendedParallelItemRecipe(@Nonnull GTRecipeType<?> recipeMap, @Nonnull IItemHandlerModifiable inputs, @Nonnull IItemHandlerModifiable outputs, int parallelLimit, long maxVoltage, @Nonnull IVoidable voidable) {
        return ParallelLogic.appendItemRecipes(
                recipeMap,
                inputs,
                outputs,
                parallelLimit,
                maxVoltage,
                voidable);
    }

    // Recipes passed in here should be already trimmed, if desired
    default GTRecipe findParallelRecipe(@Nonnull AbstractRecipeLogic logic, @Nonnull GTRecipe currentRecipe, @Nonnull IItemHandlerModifiable inputs, @Nonnull IMultipleTankHandler fluidInputs, @Nonnull IItemHandlerModifiable outputs, @Nonnull IMultipleTankHandler fluidOutputs, long maxVoltage, int parallelLimit) {
        if (parallelLimit > 1 && logic.getRecipeType() != null) {
            GTRecipeBuilder<?> parallelBuilder = null;
            if (logic.getParallelLogicType() == ParallelLogicType.MULTIPLY) {
                parallelBuilder = findMultipliedParallelRecipe(logic.getRecipeType(), currentRecipe, inputs, fluidInputs, outputs, fluidOutputs, parallelLimit, maxVoltage, logic.getMetaTileEntity());
            } else if (logic.getParallelLogicType() == ParallelLogicType.APPEND_ITEMS) {
                parallelBuilder = findAppendedParallelItemRecipe(logic.getRecipeType(), inputs, outputs, parallelLimit, maxVoltage, logic.getMetaTileEntity());
            }
            // if the builder returned is null, no recipe was found.
            if (parallelBuilder == null) {
                logic.invalidateInputs();
                return null;
            } else {
                //if the builder returned does not parallel, its outputs are full
                if (parallelBuilder.getParallel() == 0) {
                    logic.invalidateOutputs();
                    return null;
                } else {
                    logic.setParallelRecipesPerformed(parallelBuilder.getParallel());
                    //apply any parallel bonus
                    applyParallelBonus(parallelBuilder);
                    return parallelBuilder.build(currentRecipe.id);
                }
            }
        }
        return currentRecipe;
    }
}
