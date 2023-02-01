package net.nemezanevem.gregtech.api.capability.impl;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class FluidHandlerProxy implements IFluidHandler {

    public IFluidHandler input;
    public IFluidHandler output;

    public FluidHandlerProxy(IFluidHandler input, IFluidHandler output) {
        reinitializeHandler(input, output);
    }

    public void reinitializeHandler(IFluidHandler input, IFluidHandler output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public int getTanks() {
        return 2;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return tank == 0 ? input.getFluidInTank(0) : output.getFluidInTank(0);
    }

    @Override
    public int getTankCapacity(int tank) {
        return 0;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction doFill) {
        return input.fill(resource, doFill);
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, FluidAction doDrain) {
        return output.drain(resource, doDrain);
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, FluidAction doDrain) {
        return output.drain(maxDrain, doDrain);
    }
}
