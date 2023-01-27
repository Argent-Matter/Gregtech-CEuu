package net.nemezanevem.gregtech.api.capability.impl;

import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class FilteredFluidHandler extends FluidTank {

    private Predicate<FluidStack> fillPredicate;

    public FilteredFluidHandler(int capacity) {
        super(capacity);
    }

    public FilteredFluidHandler(@Nullable FluidStack fluidStack, int capacity) {
        super(capacity, (val) -> val.equals(fluidStack));
    }

    public FilteredFluidHandler(Fluid fluid, int amount, int capacity) {
        super(capacity, (val) -> val.equals(new FluidStack(fluid, amount)));
        var fluidStack = new FluidStack(fluid, amount);
        this.setFluid(fluidStack);
    }

    public FilteredFluidHandler setFillPredicate(Predicate<FluidStack> predicate) {
        this.fillPredicate = predicate;
        return this;
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid) {
        return canFill() && (fillPredicate == null || fillPredicate.test(fluid));
    }
}
