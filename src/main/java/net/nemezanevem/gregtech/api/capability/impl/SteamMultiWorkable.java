package net.nemezanevem.gregtech.api.capability.impl;


import net.nemezanevem.gregtech.api.blockentity.multiblock.ParallelLogicType;
import net.nemezanevem.gregtech.api.blockentity.multiblock.RecipeTypeSteamMultiblockController;
import net.nemezanevem.gregtech.common.datagen.recipe.builder.GTRecipeBuilder;

import javax.annotation.Nonnull;


/**
 * General Recipe Handler for Steam Multiblocks.
 * Will do up to the passed value of items in one process.
 * Not recommended to use this Handler if you do not
 * need multi-recipe logic for your Multi.
 */
public class SteamMultiWorkable extends SteamMultiblockRecipeLogic {

    public SteamMultiWorkable(RecipeTypeSteamMultiblockController tileEntity, double conversionRate) {
        super(tileEntity, tileEntity.recipeMap, tileEntity.getSteamFluidTank(), conversionRate);
    }

    @Nonnull
    @Override
    public ParallelLogicType getParallelLogicType() {
        return ParallelLogicType.APPEND_ITEMS;
    }

    @Override
    public void applyParallelBonus(@Nonnull GTRecipeBuilder<?> builder) {
        int currentRecipeEU = builder.getEUt();
        int currentRecipeDuration = builder.getDuration() / getParallelLimit();
        builder.setEUt((int) Math.min(32.0, Math.ceil(currentRecipeEU) * 1.33))
                .setBaseDuration((int) (currentRecipeDuration * 1.5));
    }
}
