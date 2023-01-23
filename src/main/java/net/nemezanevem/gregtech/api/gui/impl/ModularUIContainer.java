package net.nemezanevem.gregtech.api.gui.impl;

import io.netty.buffer.Unpooled;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.gui.GtVanillaMenuTypes;
import net.nemezanevem.gregtech.api.gui.INativeWidget;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.gui.widgets.SlotWidget;
import net.nemezanevem.gregtech.api.gui.widgets.WidgetUIAccess;
import net.nemezanevem.gregtech.api.util.PerTickIntCounter;
import net.nemezanevem.gregtech.common.network.packets.PacketUIWidgetUpdate;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ModularUIContainer extends AbstractContainerMenu implements WidgetUIAccess {

    protected final HashMap<Slot, INativeWidget> slotMap = new HashMap<>();
    private final ModularUI modularUI;

    public boolean accumulateWidgetUpdateData = false;
    public final List<PacketUIWidgetUpdate> accumulatedUpdates = new ArrayList<>();

    public ModularUIContainer(int pContainerId, Inventory pPlayerInventory) {
        this(pContainerId, pPlayerInventory, null);
    }

    public ModularUIContainer(int containerId, Inventory playerInv, ModularUI modularUI) {
        super(GtVanillaMenuTypes.GT_MENU.get(), containerId);
        this.modularUI = modularUI;
        modularUI.guiWidgets.values().forEach(widget -> widget.setUiAccess(this));
        modularUI.guiWidgets.values().stream()
                .flatMap(widget -> widget.getNativeWidgets().stream())
                .forEach(nativeWidget -> {
                    Slot slot = nativeWidget.getHandle();
                    slotMap.put(slot, nativeWidget);
                    addSlot(slot);
                });
        modularUI.triggerOpenListeners();
    }

    @Override
    public void notifySizeChange() {
    }

    //WARNING! WIDGET CHANGES SHOULD BE *STRICTLY* SYNCHRONIZED BETWEEN SERVER AND CLIENT,
    //OTHERWISE ID MISMATCH CAN HAPPEN BETWEEN ASSIGNED SLOTS!
    @Override
    public void notifyWidgetChange() {
        List<INativeWidget> nativeWidgets = modularUI.guiWidgets.values().stream()
                .flatMap(widget -> widget.getNativeWidgets().stream()).toList();

        Set<INativeWidget> removedWidgets = new HashSet<>(slotMap.values());
        removedWidgets.removeAll(nativeWidgets);
        if (!removedWidgets.isEmpty()) {
            for (INativeWidget removedWidget : removedWidgets) {
                Slot slotHandle = removedWidget.getHandle();
                this.slotMap.remove(slotHandle);
                //replace removed slot with empty placeholder to avoid list index shift
                EmptySlotPlaceholder emptySlotPlaceholder = new EmptySlotPlaceholder();
                emptySlotPlaceholder.index = slotHandle.index;
                this.slots.set(slotHandle.index, emptySlotPlaceholder);
                this.lastSlots.set(slotHandle.index, ItemStack.EMPTY);
            }
        }

        Set<INativeWidget> addedWidgets = new HashSet<>(nativeWidgets);
        addedWidgets.removeAll(slotMap.values());
        if (!addedWidgets.isEmpty()) {
            int[] emptySlotIndexes = slots.stream()
                    .filter(it -> it instanceof EmptySlotPlaceholder)
                    .mapToInt(slot -> slot.index).toArray();
            int currentIndex = 0;
            for (INativeWidget addedWidget : addedWidgets) {
                Slot slotHandle = addedWidget.getHandle();
                //add or replace empty slot in inventory
                this.slotMap.put(slotHandle, addedWidget);
                if (currentIndex < emptySlotIndexes.length) {
                    int slotIndex = emptySlotIndexes[currentIndex++];
                    slotHandle.index = slotIndex;
                    this.slots.set(slotIndex, slotHandle);
                    this.lastSlots.set(slotIndex, ItemStack.EMPTY);
                } else {
                    slotHandle.index = this.slots.size();
                    this.slots.add(slotHandle);
                    this.lastSlots.add(ItemStack.EMPTY);
                }
            }
        }
    }

    public ModularUI getModularUI() {
        return modularUI;
    }

    @Override
    public void removed(@Nonnull Player playerIn) {
        super.removed(playerIn);
        modularUI.triggerCloseListeners();
    }

    @Override
    public void addSlotListener(@Nonnull ContainerListener listener) {
        super.addSlotListener(listener);
        modularUI.guiWidgets.values().forEach(Widget::detectAndSendChanges);
    }

    @Override
    public void sendSlotUpdate(INativeWidget slot) {
        Slot slotHandle = slot.getHandle();
        for (ContainerListener listener : containerListeners) {
            listener.slotChanged(this, slotHandle.index, slotHandle.getItem());
        }
    }

    @Override
    public void sendHeldItemUpdate() {
        for (ContainerListener listener : containerListeners) {
            if (listener instanceof ServerPlayer player) {
                player.connection.send(new ClientboundContainerSetSlotPacket(this.containerId, -1, -1, player.getInventory().getSelected()));
            }
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (containerListeners.size() > 0) {
            modularUI.guiWidgets.values().forEach(Widget::detectAndSendChanges);
        }
    }

    @Nonnull
    @Override
    public void doClick(int slotId, int dragType, @Nonnull ClickType clickTypeIn, @Nonnull Player player) {
        if (slotId >= 0 && slotId < slots.size()) {
            Slot slot = getSlot(slotId);
            ItemStack result = slotMap.get(slot).slotClick(dragType, clickTypeIn, player);
            if (result == null) {
                super.doClick(slotId, dragType, clickTypeIn, player);
                return;
            }
            return result;
        }
        if (slotId == -999) {
            super.doClick(slotId, dragType, clickTypeIn, player);
        }
        return ItemStack.EMPTY;
    }

    private final PerTickIntCounter transferredPerTick = new PerTickIntCounter(0);

    private List<INativeWidget> getShiftClickSlots(ItemStack itemStack, boolean fromContainer) {
        return slotMap.values().stream()
                .filter(it -> it.canMergeSlot(itemStack))
                .filter(it -> it.getSlotLocationInfo().isPlayerInventory == fromContainer)
                .filter(it -> {
                    if (it.getHandle() instanceof SlotWidget.WidgetSlotItemHandler) {
                        return ((SlotWidget.WidgetSlotItemHandler) it.getHandle()).canInsert();
                    }
                    return true;
                })
                .sorted(Comparator.comparing(s -> (fromContainer ? -1 : 1) * s.getHandle().getSlotIndex()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean attemptMergeStack(ItemStack itemStack, boolean fromContainer, boolean simulate) {
        List<Slot> inventorySlots = getShiftClickSlots(itemStack, fromContainer).stream()
                .map(INativeWidget::getHandle)
                .collect(Collectors.toList());
        return GTUtility.mergeItemStack(itemStack, inventorySlots, simulate);
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(@Nonnull Player player, int index) {
        Slot slot = inventorySlots.get(index);
        if (!slot.mayPickup(player)) {
            return ItemStack.EMPTY;
        }
        if (!slot.hasItem()) {
            //return empty if we can't transfer it
            return ItemStack.EMPTY;
        }
        ItemStack stackInSlot = slot.getItem();
        ItemStack stackToMerge = slotMap.get(slot).onItemTake(player, stackInSlot.copy(), true);
        boolean fromContainer = !slotMap.get(slot).getSlotLocationInfo().isPlayerInventory;
        if (!attemptMergeStack(stackToMerge, fromContainer, true)) {
            return ItemStack.EMPTY;
        }
        int itemsMerged;
        if (stackToMerge.isEmpty() || slotMap.get(slot).canMergeSlot(stackToMerge)) {
            itemsMerged = stackInSlot.getCount() - stackToMerge.getCount();
        } else {
            //if we can't have partial stack merge, we have to use all the stack
            itemsMerged = stackInSlot.getCount();
        }
        int itemsToExtract = itemsMerged;
        itemsMerged += transferredPerTick.get(player.level);
        if (itemsMerged > stackInSlot.getMaxStackSize()) {
            //we can merge at most one stack at a time
            return ItemStack.EMPTY;
        }
        transferredPerTick.increment(player.level, itemsToExtract);
        //otherwise, perform extraction and merge
        ItemStack extractedStack = stackInSlot.split(itemsToExtract);
        if (stackInSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        extractedStack = slotMap.get(slot).onItemTake(player, extractedStack, false);
        ItemStack resultStack = extractedStack.copy();
        if (!attemptMergeStack(extractedStack, fromContainer, false)) {
            resultStack = ItemStack.EMPTY;
        }
        if (!extractedStack.isEmpty()) {
            player.drop(extractedStack, false, false);
            resultStack = ItemStack.EMPTY;
        }
        return resultStack;
    }

    @Override
    public boolean canMergeSlot(@Nonnull ItemStack stack, @Nonnull Slot slotIn) {
        return slotMap.get(slotIn).canMergeSlot(stack);
    }

    @Override
    public boolean canInteractWith(@Nonnull Player playerIn) {
        return playerIn == this.modularUI.player && this.modularUI.holder.isValid();
    }

    @Override
    public void writeClientAction(Widget widget, int updateId, Consumer<FriendlyByteBuf> payloadWriter) {
        int widgetId = modularUI.guiWidgets.inverse().get(widget);
        FriendlyByteBuf packetBuffer = new FriendlyByteBuf(Unpooled.buffer());
        packetBuffer.writeVarInt(updateId);
        payloadWriter.accept(packetBuffer);
        if (modularUI.player instanceof AbstractClientPlayer) {
            GregTech.NETWORK_HANDLER.sendToServer(new PacketUIClientAction(win, widgetId, packetBuffer));
        }
    }

    @Override
    public void writeUpdateInfo(Widget widget, int updateId, Consumer<FriendlyByteBuf> payloadWriter) {
        int widgetId = modularUI.guiWidgets.inverse().get(widget);
        FriendlyByteBuf packetBuffer = new FriendlyByteBuf(Unpooled.buffer());
        packetBuffer.writeVarInt(updateId);
        payloadWriter.accept(packetBuffer);
        if (modularUI.player instanceof ServerPlayer) {
            PacketUIWidgetUpdate widgetUpdate = new PacketUIWidgetUpdate(windowId, widgetId, packetBuffer);
            if (!accumulateWidgetUpdateData) {
                GregTechAPI.networkHandler.sendTo(widgetUpdate, (PlayerMP) modularUI.Player);
            } else {
                accumulatedUpdates.add(widgetUpdate);
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return null;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }

    private static class EmptySlotPlaceholder extends Slot {

        private static final Container EMPTY_INVENTORY = new SimpleContainer(0);

        public EmptySlotPlaceholder() {
            super(EMPTY_INVENTORY, 0, -100000, -100000);
        }

        @Nonnull
        @Override
        public ItemStack getStack() {
            return ItemStack.EMPTY;
        }

        @Override
        public void set(@Nonnull ItemStack stack) {

        }

        @Override
        public boolean mayPlace(@Nonnull ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(@Nonnull Player playerIn) {
            return false;
        }

        @Override
        public boolean isActive() {
            return false;
        }
    }
}
