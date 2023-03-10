package net.nemezanevem.gregtech.api.capability.impl;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import javax.annotation.Nonnull;


/**
 * Implements an item that voids fluids
 */
public class VoidFluidHandlerItemStack extends FluidHandlerItemStack {

    /**
     * Voids as much fluid as you can throw at it
     *
     * @param container The container itemStack.
     */
    public VoidFluidHandlerItemStack(@Nonnull ItemStack container) {
        this(container, Integer.MAX_VALUE);
    }

    /**
     * Voids fluid a certain amount at a time
     *
     * @param container The container itemStack.
     * @param capacity  max amount to void in each operation
     */
    public VoidFluidHandlerItemStack(@Nonnull ItemStack container, final int capacity) {
        super(container, capacity);
    }

    @Override
    public FluidStack getFluid() {
        return null;
    }

    @Override
    protected void setFluid(FluidStack fluid) {
        // No NBT tag
    }

    @Override
    public int fill(FluidStack resource, FluidAction doFill) {
        if (resource == null || resource.getAmount() <= 0)
            return 0;
        return Math.min(this.capacity, resource.getAmount());
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction doDrain) {
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction doDrain) {
        return FluidStack.EMPTY;
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid) {
        return true;
    }

    @Override
    public boolean canDrainFluidType(FluidStack fluid) {
        return false;
    }
}
