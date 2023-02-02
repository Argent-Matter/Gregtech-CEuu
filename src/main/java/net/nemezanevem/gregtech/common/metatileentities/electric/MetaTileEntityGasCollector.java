package net.nemezanevem.gregtech.common.metatileentities.electric;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.SimpleMachineMetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.capability.IEnergyContainer;
import net.nemezanevem.gregtech.api.capability.impl.RecipeLogicEnergy;
import net.nemezanevem.gregtech.api.recipe.GTRecipe;
import net.nemezanevem.gregtech.api.recipe.GTRecipeType;
import net.nemezanevem.gregtech.api.recipe.GtRecipeTypes;
import net.nemezanevem.gregtech.api.recipe.property.GasCollectorDimensionProperty;
import net.nemezanevem.gregtech.client.renderer.ICubeRenderer;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.Supplier;

public class MetaTileEntityGasCollector extends SimpleMachineMetaTileEntity {

    public MetaTileEntityGasCollector(ResourceLocation metaTileEntityId, GTRecipeType<?> recipeMap, ICubeRenderer renderer, int tier, boolean hasFrontFacing,
                                      Function<Integer, Integer> tankScalingFunction) {
        super(metaTileEntityId, recipeMap, renderer, tier, hasFrontFacing, tankScalingFunction);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityGasCollector(this.metaTileEntityId, GtRecipeTypes.GAS_COLLECTOR_RECIPES.get(),
                Textures.GAS_COLLECTOR_OVERLAY, this.getTier(), this.hasFrontFacing(), this.getTankScalingFunction());
    }

    @Override
    protected RecipeLogicEnergy createWorkable(GTRecipeType<?> recipeMap) {
        return new GasCollectorRecipeLogic(this, recipeMap, () -> energyContainer);
    }

    protected boolean checkRecipe(@Nonnull GTRecipe recipe) {
        for (DimensionType dimension : recipe.getProperty(GasCollectorDimensionProperty.getInstance(), new DimensionType[0])) {
            if (dimension == this.getWorld().dimensionType()) {
                return true;
            }
        }
        return false;
    }

    private static class GasCollectorRecipeLogic extends RecipeLogicEnergy {

        public GasCollectorRecipeLogic(MetaTileEntity metaTileEntity, GTRecipeType<?> recipeMap, Supplier<IEnergyContainer> energyContainer) {
            super(metaTileEntity, recipeMap, energyContainer);
        }

        @Override
        protected boolean checkRecipe(@Nonnull GTRecipe recipe) {
            return ((MetaTileEntityGasCollector) metaTileEntity).checkRecipe(recipe) && super.checkRecipe(recipe);
        }
    }
}
