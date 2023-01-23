package net.nemezanevem.gregtech.api.item.metaitem.stats;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.util.Optional;

public class ItemFluidContainer implements IItemContainerItemProvider {
    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        LazyOptional<IFluidHandlerItem> handler = itemStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null);
        if (handler.isPresent()) {
            var realHandler = handler.resolve().get();
            FluidStack drained = realHandler.drain(1000, IFluidHandler.FluidAction.SIMULATE);
            if (drained.getAmount() != 1000) return ItemStack.EMPTY;
            realHandler.drain(1000, IFluidHandler.FluidAction.EXECUTE);
            return realHandler.getContainer().copy();
        }
        return itemStack;
    }
}
