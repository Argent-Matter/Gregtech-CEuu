package net.nemezanevem.gregtech.common.covers.filter;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.ItemStackHandler;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.gui.widgets.LabelWidget;
import net.nemezanevem.gregtech.api.gui.widgets.SlotWidget;
import net.nemezanevem.gregtech.api.util.IDirtyNotifiable;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class ItemFilterContainer implements INBTSerializable<CompoundTag> {

    private final ItemStackHandler filterInventory;
    private final ItemFilterWrapper filterWrapper;
    private int maxStackSizeLimit = 1;
    private int transferStackSize;

    public ItemFilterContainer(IDirtyNotifiable dirtyNotifiable) {
        this.filterWrapper = new ItemFilterWrapper(dirtyNotifiable);
        this.filterWrapper.setOnFilterInstanceChange(this::onFilterInstanceChange);
        this.filterInventory = new ItemStackHandler(1) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return FilterTypeRegistry.getItemFilterForStack(stack) != null;
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            protected void onLoad() {
                onFilterSlotChange(false);
            }

            @Override
            protected void onContentsChanged(int slot) {
                onFilterSlotChange(true);
            }
        };
    }

    public ItemStackHandler getFilterInventory() {
        return filterInventory;
    }

    public ItemFilterWrapper getFilterWrapper() {
        return filterWrapper;
    }

    private void onFilterInstanceChange() {
        this.filterWrapper.setMaxStackSize(getTransferStackSize());
    }

    public int getMaxStackSize() {
        return maxStackSizeLimit;
    }

    public int getTransferStackSize() {
        if (!showGlobalTransferLimitSlider()) {
            return getMaxStackSize();
        }
        return transferStackSize;
    }

    public void setTransferStackSize(int transferStackSize) {
        this.transferStackSize = Mth.clamp(transferStackSize, 1, getMaxStackSize());
        this.filterWrapper.setMaxStackSize(getTransferStackSize());
    }

    public void adjustTransferStackSize(int amount) {
        setTransferStackSize(transferStackSize + amount);
    }

    public void initUI(int y, Consumer<Widget> widgetGroup) {
        widgetGroup.accept(new LabelWidget(10, y, "cover.conveyor.item_filter.title"));
        widgetGroup.accept(new SlotWidget(filterInventory, 0, 10, y + 15)
                .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY));

        this.filterWrapper.initUI(y + 38, widgetGroup);
        this.filterWrapper.blacklistUI(y + 38, widgetGroup, () -> true);
    }

    protected void onFilterSlotChange(boolean notify) {
        ItemStack filterStack = filterInventory.getStackInSlot(0);
        ItemFilter newItemFilter = FilterTypeRegistry.getItemFilterForStack(filterStack);
        ItemFilter currentItemFilter = filterWrapper.getItemFilter();
        if (newItemFilter == null) {
            if (currentItemFilter != null) {
                filterWrapper.setItemFilter(null);
                filterWrapper.setBlacklistFilter(false);
                if (notify) filterWrapper.onFilterInstanceChange();
            }
        } else if (currentItemFilter == null ||
                newItemFilter.getClass() != currentItemFilter.getClass()) {
            filterWrapper.setItemFilter(newItemFilter);
            if (notify) filterWrapper.onFilterInstanceChange();
        }
    }

    public void setMaxStackSize(int maxStackSizeLimit) {
        this.maxStackSizeLimit = maxStackSizeLimit;
        setTransferStackSize(transferStackSize);
    }

    public boolean showGlobalTransferLimitSlider() {
        return getMaxStackSize() > 1 && filterWrapper.showGlobalTransferLimitSlider();
    }

    public int getSlotTransferLimit(Object slotIndex) {
        return filterWrapper.getSlotTransferLimit(slotIndex, getTransferStackSize());
    }

    public Object matchItemStack(ItemStack itemStack) {
        return filterWrapper.matchItemStack(itemStack);
    }

    public Object matchItemStack(ItemStack itemStack, boolean whitelist) {
        return filterWrapper.matchItemStack(itemStack, whitelist);
    }

    public boolean testItemStack(ItemStack itemStack) {
        return matchItemStack(itemStack) != null;
    }

    public boolean testItemStack(ItemStack itemStack, boolean whitelist) {
        return matchItemStack(itemStack, whitelist) != null;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tagCompound = new CompoundTag();
        tagCompound.put("FilterInventory", filterInventory.serializeNBT());
        tagCompound.putBoolean("IsBlacklist", filterWrapper.isBlacklistFilter());
        tagCompound.putInt("MaxStackSize", maxStackSizeLimit);
        tagCompound.putInt("TransferStackSize", transferStackSize);
        if (filterWrapper.getItemFilter() != null) {
            CompoundTag filterInventory = new CompoundTag();
            filterWrapper.getItemFilter().writeToNBT(filterInventory);
            tagCompound.put("Filter", filterInventory);
        }
        return tagCompound;
    }

    @Override
    public void deserializeNBT(CompoundTag tagCompound) {
        this.filterInventory.deserializeNBT(tagCompound.getCompound("FilterInventory"));
        this.filterWrapper.setBlacklistFilter(tagCompound.getBoolean("IsBlacklist"));
        setMaxStackSize(tagCompound.getInt("MaxStackSize"));
        setTransferStackSize(tagCompound.getInt("TransferStackSize"));
        if (filterWrapper.getItemFilter() != null) {
            this.filterWrapper.getItemFilter().readFromNBT(tagCompound.getCompound("Filter"));
        }
    }

}
