package net.nemezanevem.gregtech.api.gui.widgets;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandlerModifiable;

public class FluidContainerSlotWidget extends SlotWidget {

    private final boolean requireFilledContainer;

    public FluidContainerSlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition, boolean requireFilledContainer) {
        super(itemHandler, slotIndex, xPosition, yPosition, true, true);
        this.requireFilledContainer = requireFilledContainer;
    }

    @Override
    public boolean canPutStack(ItemStack stack) {
        LazyOptional<IFluidHandlerItem> fluidHandlerItem = FluidUtil.getFluidHandler(stack);
        return fluidHandlerItem.isPresent() && (!requireFilledContainer || !fluidHandlerItem.resolve().get().getFluidInTank(0).isEmpty());
    }
}
