package net.nemezanevem.gregtech.api.capability.impl;

import net.minecraftforge.fluids.FluidStack;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.capability.IMultipleTankHandler;

import java.util.function.Supplier;

public abstract class NotifiableFluidTankFromList extends NotifiableFluidTank {

    private final int index;

    public NotifiableFluidTankFromList(int capacity, MetaTileEntity entityToNotify, boolean isExport, int index) {
        super(capacity, entityToNotify, isExport);
        this.index = index;
    }

    public abstract Supplier<IMultipleTankHandler> getFluidTankList();

    public int getIndex() {
        return index;
    }

    @Override
    public int fill(FluidStack resource, FluidAction doFill) {
        IMultipleTankHandler tanks = getFluidTankList().get();
        if (!tanks.allowSameFluidFill()) {
            int fillIndex = tanks.getIndexOfFluid(resource);
            if (fillIndex != getIndex() && fillIndex != -1) return 0;
        }
        return super.fill(resource, doFill);
    }
}
