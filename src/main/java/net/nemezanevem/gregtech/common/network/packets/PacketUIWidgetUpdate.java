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

    public void encode(FriendlyByteBuf buf) {
        NetworkUtils.writePacketBuffer(buf, updateData);
        buf.writeVarInt(windowId);
        buf.writeVarInt(widgetId);
    }

    public void decode(FriendlyByteBuf buf) {
        this.updateData = NetworkUtils.readPacketBuffer(buf);
        this.windowId = buf.readVarInt();
        this.widgetId = buf.readVarInt();
    }

    public void handle(PacketUIWidgetUpdate packet, Supplier<NetworkEvent.Context> handler) {
        handler.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Screen currentScreen = Minecraft.getInstance().screen;
                if (currentScreen instanceof ModularUIGui ui) {
                    ui.handleWidgetUpdate(this);
                }
            });
        });
        handler.get().setPacketHandled(true);
    }
}