package net.nemezanevem.gregtech.api.capability.impl;

import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;

/**
 * Fluid Fuel information
 */
public class FluidFuelInfo extends AbstractFuelInfo {

    private final FluidStack fluidStack;

    public FluidFuelInfo(final FluidStack fluidStack, final int fuelRemaining, final int fuelCapacity, final int fuelMinConsumed, final long fuelBurnTime) {
        super(fuelRemaining, fuelCapacity, fuelMinConsumed, fuelBurnTime);
        this.fluidStack = fluidStack;
    }

    public Component getFuelName() {
        return fluidStack.getTranslationKey();
    }
}
