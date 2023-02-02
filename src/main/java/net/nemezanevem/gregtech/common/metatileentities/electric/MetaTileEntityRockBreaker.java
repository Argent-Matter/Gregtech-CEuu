package net.nemezanevem.gregtech.common.metatileentities.electric;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.SimpleMachineMetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.capability.IEnergyContainer;
import net.nemezanevem.gregtech.api.capability.impl.RecipeLogicEnergy;
import net.nemezanevem.gregtech.api.recipe.GTRecipeType;
import net.nemezanevem.gregtech.api.recipe.GtRecipeTypes;
import net.nemezanevem.gregtech.client.renderer.ICubeRenderer;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;

import java.util.function.Supplier;

public class MetaTileEntityRockBreaker extends SimpleMachineMetaTileEntity {

    private boolean hasValidFluids;

    public MetaTileEntityRockBreaker(ResourceLocation metaTileEntityId, GTRecipeType<?> recipeMap, ICubeRenderer renderer, int tier) {
        super(metaTileEntityId, recipeMap, renderer, tier, true);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityRockBreaker(metaTileEntityId, GtRecipeTypes.ROCK_BREAKER_RECIPES.get(), Textures.ROCK_BREAKER_OVERLAY, getTier());
    }

    @Override
    protected RecipeLogicEnergy createWorkable(GTRecipeType<?> recipeMap) {
        return new RockBreakerRecipeLogic(this, GtRecipeTypes.ROCK_BREAKER_RECIPES.get(), () -> energyContainer);
    }

    @Override
    public void onNeighborChanged() {
        super.onNeighborChanged();
        checkAdjacentFluids();
    }

    private void checkAdjacentFluids() {
        if (getWorld() == null) {
            hasValidFluids = true;
            return;
        }
        if (getWorld().isClientSide) {
            hasValidFluids = false;
            return;
        }
        boolean hasLava = false;
        boolean hasWater = false;
        for (Direction side : Direction.values()) {
            if (hasLava && hasWater) {
                break;
            }

            if (side == frontFacing || side.getAxis().isVertical()) {
                continue;
            }

            Fluid fluid = getWorld().getFluidState(getPos().offset(side.getNormal())).getType();
            if (fluid == Fluids.FLOWING_LAVA || fluid == Fluids.LAVA) {
                hasLava = true;
            } else if (fluid == Fluids.FLOWING_WATER || fluid == Fluids.WATER) {
                hasWater = true;
            }
        }
        this.hasValidFluids = hasLava && hasWater;
    }

    @Override
    public <T> void addNotifiedInput(T input) {
        super.addNotifiedInput(input);
        onNeighborChanged();
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.putBoolean("hasValidFluids", hasValidFluids);
        return data;
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        if (data.contains("hasValidFluids")) {
            this.hasValidFluids = data.getBoolean("hasValidFluids");
        }
    }

    protected class RockBreakerRecipeLogic extends RecipeLogicEnergy {

        public RockBreakerRecipeLogic(MetaTileEntity metaTileEntity, GTRecipeType<?> recipeMap, Supplier<IEnergyContainer> energyContainer) {
            super(metaTileEntity, recipeMap, energyContainer);
        }

        @Override
        protected boolean shouldSearchForRecipes() {
            return hasValidFluids && super.shouldSearchForRecipes();
        }
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant(){
        return true;
    }
}
