package net.nemezanevem.gregtech.api.gui.impl;

import com.google.common.collect.Lists;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.nemezanevem.gregtech.api.gui.INativeWidget;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.gui.widgets.WidgetUIAccess;

import java.util.List;
import java.util.function.Consumer;

public abstract class FakeModularGuiContainer implements WidgetUIAccess {
    protected final NonNullList<ItemStack> lastSlots = NonNullList.create();
    public final List<Slot> inventorySlots = Lists.newArrayList();
    public final ModularUI modularUI;
    protected int windowId;

    public FakeModularGuiContainer(ModularUI modularUI) {
        this.modularUI = modularUI;
        modularUI.initWidgets();
        modularUI.guiWidgets.values().forEach(widget -> widget.setUiAccess(this));
        modularUI.guiWidgets.values().stream().flatMap(widget -> widget.getNativeWidgets().stream()).forEach(nativeWidget -> addSlotToContainer(nativeWidget.getHandle()));
        modularUI.triggerOpenListeners();
    }

    protected void addSlotToContainer(Slot slotIn) {
        slotIn.index = this.inventorySlots.size();
        this.inventorySlots.add(slotIn);
        this.lastSlots.add(ItemStack.EMPTY);
    }

    public void handleSlotUpdate(FriendlyByteBuf updateData) {
        try {
            int size = updateData.readVarInt();
            for (int i = 0; i < size; i++) {
                inventorySlots.get(updateData.readVarInt()).set(updateData.readItem());
            }
        } catch (Exception ignored){

        }
    }

    public void handleClientAction(FriendlyByteBuf buffer) {
        if (detectSyncedPacket(buffer)) {
            Widget widget = modularUI.guiWidgets.get(buffer.readVarInt());
            if (widget != null) {
                widget.handleClientAction(buffer.readVarInt(), buffer);
            }
        }
    }

    // Detects if a client action is germane to a container. THIS MAY MODIFY THE CONTAINER.
    public abstract boolean detectSyncedPacket(FriendlyByteBuf buffer);

    public abstract void detectAndSendChanges();

    @Override
    public void notifySizeChange() {

    }

    @Override
    public void notifyWidgetChange() {

    }

    @Override
    public boolean attemptMergeStack(ItemStack itemStack, boolean b, boolean b1) {
        return false;
    }

    @Override
    public void sendSlotUpdate(INativeWidget iNativeWidget) {
    }

    @Override
    public void sendHeldItemUpdate() {
    }

    @Override
    public abstract void writeClientAction(Widget widget, int updateId, Consumer<FriendlyByteBuf> payloadWriter);

    @Override
    public abstract void writeUpdateInfo(Widget widget, int updateId, Consumer<FriendlyByteBuf> payloadWriter);
}
