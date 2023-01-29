package net.nemezanevem.gregtech.common.metatileentities.electric;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.RecipeLogicEnergy;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeType;
import gregtech.api.recipes.RecipeTypes;
import gregtech.api.recipes.recipeproperties.GasCollectorDimensionProperty;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import it.unimi.dsi.fastutil.ints.IntLists;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class MetaTileEntityGasCollector extends SimpleMachineMetaTileEntity {

    public MetaTileEntityGasCollector(ResourceLocation metaTileEntityId, RecipeType<?> recipeMap, ICubeRenderer renderer, int tier, boolean hasFrontFacing,
                                      Function<Integer, Integer> tankScalingFunction) {
        super(metaTileEntityId, recipeMap, renderer, tier, hasFrontFacing, tankScalingFunction);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityGasCollector(this.metaTileEntityId, RecipeTypes.GAS_COLLECTOR_RECIPES,
                Textures.GAS_COLLECTOR_OVERLAY, this.getTier(), this.hasFrontFacing(), this.getTankScalingFunction());
    }

    @Override
    protected RecipeLogicEnergy createWorkable(RecipeType<?> recipeMap) {
        return new GasCollectorRecipeLogic(this, recipeMap, () -> energyContainer);
    }

    protected boolean checkRecipe(@Nonnull Recipe recipe) {
        for (int dimension : recipe.getProperty(GasCollectorDimensionProperty.getInstance(), IntLists.EMPTY_LIST)) {
            if (dimension == this.getWorld().provider.getDimension()) {
                return true;
            }
        }
        return false;
    }

    private static class GasCollectorRecipeLogic extends RecipeLogicEnergy {

        public GasCollectorRecipeLogic(MetaTileEntity metaTileEntity, RecipeType<?> recipeMap, Supplier<IEnergyContainer> energyContainer) {
            super(metaTileEntity, recipeMap, energyContainer);
        }

        @Override
        protected boolean checkRecipe(@Nonnull Recipe recipe) {
            return ((MetaTileEntityGasCollector) metaTileEntity).checkRecipe(recipe) && super.checkRecipe(recipe);
        }
    }
}
