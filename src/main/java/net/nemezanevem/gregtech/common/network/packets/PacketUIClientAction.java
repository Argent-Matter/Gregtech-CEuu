package net.nemezanevem.gregtech.common.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.impl.ModularUIContainer;
import net.nemezanevem.gregtech.common.network.NetworkUtils;

import java.util.function.Supplier;

public class PacketUIClientAction {

    private int windowId;
    private int widgetId;
    private FriendlyByteBuf updateData;

    public PacketUIClientAction(int windowId, int widgetId, FriendlyByteBuf updateData) {
        this.windowId = windowId;
        this.widgetId = widgetId;
        this.updateData = updateData;
    }

    public void encode(FriendlyByteBuf buf) {
        NetworkUtils.writeFriendlyByteBuf(buf, updateData);
        buf.writeVarInt(windowId);
        buf.writeVarInt(widgetId);
    }

    public void decode(FriendlyByteBuf buf) {
        this.updateData = NetworkUtils.readFriendlyByteBuf(buf);
        this.windowId = buf.readVarInt();
        this.widgetId = buf.readVarInt();
    }

    public void executeServer(PacketUIClientAction packet, Supplier<NetworkEvent.Context> handler) {
        handler.get().enqueueWork(() -> {
            AbstractContainerMenu openContainer = handler.get().getSender().containerMenu;
            if (openContainer instanceof ModularUIContainer container && container.containerId == windowId) {
                ModularUI modularUI = ((ModularUIContainer) openContainer).getModularUI();
                DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> modularUI.guiWidgets.get(widgetId).handleClientAction(updateData.readVarInt(), updateData));
            }
        });
        handler.get().setPacketHandled(true);
    }
}
