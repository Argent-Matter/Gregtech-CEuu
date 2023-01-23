package net.nemezanevem.gregtech.common.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.impl.ModularUIGui;
import net.nemezanevem.gregtech.common.network.NetworkUtils;

import java.util.function.Supplier;

public class PacketUIWidgetUpdate {

    public int windowId;
    public int widgetId;
    public FriendlyByteBuf updateData;

    public PacketUIWidgetUpdate(int windowId, int widgetId, FriendlyByteBuf updateData) {
        this.windowId = windowId;
        this.widgetId = widgetId;
        this.updateData = updateData;
    }

    public static void encode(PacketUIWidgetUpdate packet, FriendlyByteBuf buf) {
        NetworkUtils.writePacketBuffer(buf, packet.updateData);
        buf.writeVarInt(packet.windowId);
        buf.writeVarInt(packet.widgetId);
    }

    public static PacketUIWidgetUpdate decode(FriendlyByteBuf buf) {
        var updateData = NetworkUtils.readPacketBuffer(buf);
        var windowId = buf.readVarInt();
        var widgetId = buf.readVarInt();
        return new PacketUIWidgetUpdate(windowId, widgetId, updateData);
    }

    public static void handle(PacketUIWidgetUpdate packet, Supplier<NetworkEvent.Context> handler) {
        handler.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Screen currentScreen = Minecraft.getInstance().screen;
                if (currentScreen instanceof ModularUIGui ui) {
                    ui.handleWidgetUpdate(packet);
                }
            });
        });
        handler.get().setPacketHandled(true);
    }
}
