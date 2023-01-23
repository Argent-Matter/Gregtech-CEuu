package net.nemezanevem.gregtech.api.gui;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

/**
 * Native widget is widget wrapping native Slot
 * That means controls are delegated to vanilla {@link net.minecraft.client.gui.screens.inventory.InventoryScreen}
 * Rendering is still handled by widget via helpers in {@link net.nemezanevem.gregtech.api.gui.IRenderContext}
 */
public interface INativeWidget {
    /**
     * You should return MC slot handle instance you created earlier
     *
     * @return MC slot
     */
    SlotItemHandler getHandle();

    /**
     * @return true if this slot belongs to player inventory
     */
    SlotLocationInfo getSlotLocationInfo();

    /**
     * @return true when this slot is valid for double click merging
     */
    boolean canTakeItemForPickAll(ItemStack stack);

    /**
     * Called when item is taken from the slot
     * Simulated take is used to compute slot merging behavior
     * This method should not modify slot state if it is simulated
     */
    default ItemStack onItemTake(Player player, ItemStack stack, boolean simulate) {
        return stack;
    }

    /**
     * Called when slot is clicked in Container
     * Return null to fallback to vanilla logic
     */
    ItemStack slotClick(int dragType, ClickType clickTypeIn, Player player);

    class SlotLocationInfo {
        public final boolean isPlayerInventory;
        public final boolean isHotbarSlot;

        public SlotLocationInfo(boolean isPlayerInventory, boolean isHotbarSlot) {
            this.isPlayerInventory = isPlayerInventory;
            this.isHotbarSlot = isHotbarSlot;
        }
    }
}
