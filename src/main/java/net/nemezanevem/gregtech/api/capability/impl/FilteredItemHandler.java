package net.nemezanevem.gregtech.api.capability.impl;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public class FilteredItemHandler extends ItemStackHandler {

    public static Predicate<ItemStack> getCapabilityFilter(Capability<?> cap) {
        return stack -> stack.getCapability(cap, null).isPresent();
    }

    private Predicate<ItemStack> fillPredicate;

    public FilteredItemHandler() {
        super(1);
    }

    public FilteredItemHandler(int size) {
        super(size);
    }

    public FilteredItemHandler(NonNullList<ItemStack> stacks) {
        super(stacks);
    }

    public FilteredItemHandler setFillPredicate(Predicate<ItemStack> fillPredicate) {
        this.fillPredicate = fillPredicate;
        return this;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return fillPredicate == null || fillPredicate.test(stack);
    }
}
