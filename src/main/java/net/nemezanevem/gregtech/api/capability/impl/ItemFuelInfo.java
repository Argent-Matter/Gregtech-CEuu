package net.nemezanevem.gregtech.api.capability.impl;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Item Fuel information
 */
public class ItemFuelInfo extends AbstractFuelInfo {

    private final ItemStack itemStack;

    public ItemFuelInfo(final ItemStack itemStack, final int fuelRemaining, final int fuelCapacity, final int fuelMinConsumed, final long fuelBurnTime) {
        super(fuelRemaining, fuelCapacity, fuelMinConsumed, fuelBurnTime);
        this.itemStack = itemStack;
    }

    public Component getFuelName() {
        return itemStack.getDisplayName();
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }
}
