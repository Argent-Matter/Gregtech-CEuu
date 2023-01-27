package net.nemezanevem.gregtech.api.recipe.type;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.nemezanevem.gregtech.api.recipe.ingredient.FluidIngredient;

import java.util.Objects;

public class MapFluidIngredient extends AbstractMapIngredient {

    public final Fluid fluid;
    public final CompoundTag tag;

    public MapFluidIngredient(FluidIngredient fluidInput) {
        FluidStack fluidStack = fluidInput.getFluids()[0];
        this.fluid = fluidStack.getFluid();
        this.tag = fluidStack.getTag();
    }

    public MapFluidIngredient(FluidStack fluidStack) {
        this.fluid = fluidStack.getFluid();
        this.tag = fluidStack.getTag();
    }

    @Override
    protected int hash() {
        //the Fluid registered to the fluidName on game load might not be the same Fluid after loading the world, but will still have the same fluidName.
        int hash = 31 + fluid.getFluidType().hashCode();
        if (tag != null) {
            return 31 * hash + tag.hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            MapFluidIngredient other = (MapFluidIngredient) o;
            //the Fluid registered to the fluidName on game load might not be the same Fluid after loading the world, but will still have the same fluidName.
            if (this.fluid.getFluidType().equals(other.fluid.getFluidType())) {
                return Objects.equals(tag, other.tag);
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "MapFluidIngredient{" +
                "{fluid=" + fluid.getFluidType().toString() + "} {tag=" + tag + "}";
    }
}
