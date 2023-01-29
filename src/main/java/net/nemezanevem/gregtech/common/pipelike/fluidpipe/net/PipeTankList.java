package net.nemezanevem.gregtech.common.pipelike.fluidpipe.net;

import net.minecraft.core.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.nemezanevem.gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import net.nemezanevem.gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipeTickable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Iterator;

public class PipeTankList implements IFluidHandler, Iterable<FluidTank> {

    private final TileEntityFluidPipeTickable pipe;
    private final FluidTank[] tanks;
    private final Direction facing;

    public PipeTankList(TileEntityFluidPipe pipe, Direction facing, FluidTank... fluidTanks) {
        this.tanks = fluidTanks;
        this.pipe = (TileEntityFluidPipeTickable) pipe;
        this.facing = facing;
    }

    private int findChannel(FluidStack stack) {
        if (stack == null || tanks == null)
            return -1;
        int empty = -1;
        for (int i = tanks.length - 1; i >= 0; i--) {
            FluidStack f = tanks[i].getFluid();
            if (f == null)
                empty = i;
            else if (f.isFluidEqual(stack))
                return i;
        }
        return empty;
    }

    @Override
    public int fill(FluidStack resource, FluidAction doFill) {
        int channel;
        if (pipe.isFaceBlocked(facing) || resource == null || resource.getAmount() <= 0 || (channel = findChannel(resource)) < 0)
            return 0;

        return fill(resource, doFill, channel);
    }

    private int fullCapacity() {
        return tanks.length * pipe.getCapacityPerTank();
    }

    private int fill(FluidStack resource, FluidAction doFill, int channel) {
        if (channel >= tanks.length) return 0;
        FluidTank tank = tanks[channel];
        FluidStack currentFluid = tank.getFluid();

        if (currentFluid == null || currentFluid.getAmount() <= 0) {
            FluidStack newFluid = resource.copy();
            newFluid.setAmount(Math.min(pipe.getCapacityPerTank(), newFluid.getAmount()));
            if (doFill == FluidAction.EXECUTE) {
                tank.setFluid(newFluid);
                pipe.receivedFrom(facing);
                pipe.checkAndDestroy(newFluid);
            }
            return newFluid.getAmount();
        }
        if (currentFluid.isFluidEqual(resource)) {
            int toAdd = Math.min(tank.getCapacity() - currentFluid.getAmount(), resource.getAmount());
            if (toAdd > 0) {
                if (doFill == FluidAction.EXECUTE) {
                    currentFluid.setAmount(currentFluid.getAmount() + toAdd);
                    pipe.receivedFrom(facing);
                    pipe.checkAndDestroy(currentFluid);
                }
                return toAdd;
            }
        }

        return 0;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, FluidAction doDrain) {
        if (maxDrain <= 0) return null;
        for (FluidTank tank : tanks) {
            FluidStack drained = tank.drain(maxDrain, doDrain);
            if (drained != null) return drained;
        }
        return null;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack fluidStack, FluidAction doDrain) {
        if (fluidStack == null || fluidStack.getAmount() <= 0) return null;
        fluidStack = fluidStack.copy();
        for (FluidTank tank : tanks) {
            FluidStack drained = tank.drain(fluidStack, doDrain);
            if (drained != null) return drained;
        }
        return null;
    }

    @Override
    @Nonnull
    public Iterator<FluidTank> iterator() {
        return Arrays.stream(tanks).iterator();
    }

    @Override
    public int getTanks() {
        return tanks.length;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return tanks[tank].getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return tanks[tank].getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return tanks[tank].isFluidValid(stack);
    }
}
