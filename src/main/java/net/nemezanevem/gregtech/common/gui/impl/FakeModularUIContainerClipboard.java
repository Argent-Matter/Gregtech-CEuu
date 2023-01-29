package net.nemezanevem.gregtech.common.gui.impl;

import com.google.common.collect.Lists;
import io.netty.buffer.Unpooled;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Tuple;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.gui.impl.FakeModularGuiContainer;
import net.nemezanevem.gregtech.common.metatileentities.MetaTileEntityClipboard;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_UI;

public class FakeModularUIContainerClipboard extends FakeModularGuiContainer {
    private final NonNullList<ItemStack> inventoryItemStacks = NonNullList.create();
    public final List<Slot> inventorySlots = Lists.newArrayList();
    public int windowId;
    public MetaTileEntityClipboard clipboard;

    public FakeModularUIContainerClipboard(ModularUI modularUI, MetaTileEntityClipboard clipboard) {
        super(modularUI);
        this.clipboard = clipboard;
    }

    protected void addSlotToContainer(Slot slotIn) {
        slotIn.slotNumber = this.inventorySlots.size();
        this.inventorySlots.add(slotIn);
        this.inventoryItemStacks.add(ItemStack.EMPTY);
    }

    public void handleSlotUpdate(FriendlyByteBuf updateData) {
        try {
            int size = updateData.readVarInt();
            for (int i = 0; i < size; i++) {
                inventorySlots.get(updateData.readVarInt()).set(updateData.readItem());
            }
        } catch (Exception ignored) {

        }
    }

    public void handleClientAction(FriendlyByteBuf buffer) {
        int windowId = buffer.readVarInt();
        if (windowId == this.windowId) {
            Widget widget = modularUI.guiWidgets.get(buffer.readVarInt());
            if (widget != null) {
                widget.handleClientAction(buffer.readVarInt(), buffer);
            }
        }
    }

    @Override
    public boolean detectSyncedPacket(FriendlyByteBuf buffer) {
        return this.windowId == buffer.readVarInt();
    }

    public void detectAndSendChanges() {
        List<Tuple<Integer, ItemStack>> toUpdate = new ArrayList<>();
        for (int i = 0; i < this.inventorySlots.size(); ++i) {
            ItemStack real = this.inventorySlots.get(i).getItem();
            ItemStack fake = this.inventoryItemStacks.get(i);

            if (!ItemStack.matches(fake, real)) {
                boolean clientStackChanged = !ItemStack.isSameItemSameTags(fake, real);
                fake = real.isEmpty() ? ItemStack.EMPTY : real.copy();
                this.inventoryItemStacks.set(i, fake);

                if (clientStackChanged) {
                    toUpdate.add(new Tuple<>(i, fake));
                }
            }
        }
        modularUI.guiWidgets.values().forEach(Widget::detectAndSendChanges);
    }

    @Override
    public void writeClientAction(Widget widget, int updateId, Consumer<FriendlyByteBuf> payloadWriter) {
        int widgetId = modularUI.guiWidgets.inverse().get(widget);
        FriendlyByteBuf packetBuffer = new FriendlyByteBuf(Unpooled.buffer());
        packetBuffer.writeVarInt(windowId);
        packetBuffer.writeVarInt(widgetId);
        packetBuffer.writeVarInt(updateId);
        payloadWriter.accept(packetBuffer);
        GregTech.NETWORK_HANDLER.sendToServer(new PacketClipboardUIWidgetUpdate(
                this.clipboard.getWorld().provider.getDimension(),
                this.clipboard.getPos(),
                updateId, packetBuffer));
    }

    @Override
    public void writeUpdateInfo(Widget widget, int updateId, Consumer<FriendlyByteBuf> payloadWriter) {
        this.clipboard.writeCustomData(UPDATE_UI + modularUI.guiWidgets.inverse().get(widget), buf -> {
            buf.writeVarInt(windowId);
            buf.writeVarInt(modularUI.guiWidgets.inverse().get(widget));
            buf.writeVarInt(updateId);
            payloadWriter.accept(buf);
        });
    }
}
