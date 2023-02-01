package net.nemezanevem.gregtech.api.capability.impl;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class FluidHandlerDelegate implements IFluidHandler {

    public final IFluidHandler delegate;

    public FluidHandlerDelegate(IFluidHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public int fill(FluidStack resource, FluidAction doFill) {
        return delegate.fill(resource, doFill);
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, FluidAction doDrain) {
        return delegate.drain(resource, doDrain);
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, FluidAction doDrain) {
        return delegate.drain(maxDrain, doDrain);
    }

    @Override
    public int getTanks() {
        return delegate.getTanks();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return delegate.getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return delegate.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return delegate.isFluidValid(tank, stack);
    }
}
