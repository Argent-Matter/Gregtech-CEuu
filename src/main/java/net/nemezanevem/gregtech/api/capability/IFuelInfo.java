package net.nemezanevem.gregtech.api.capability;

import net.minecraft.network.chat.Component;

/**
 * Information about fuel
 */
public interface IFuelInfo {
    /**
     * @return the fuel
     */
    Component getFuelName();

    /**
     * @return the amount of fuel remaining
     */
    int getFuelRemaining();

    /**
     * @return the fuel capacity
     */
    int getFuelCapacity();

    /**
     * @return the minimum fuel that can be consumed
     */
    int getFuelMinConsumed();

    /**
     * @return the estimated amount of time in ticks for burning the remaining fuel
     */
    long getFuelBurnTimeLong();
}
