package net.nemezanevem.gregtech.common.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.gui.UIFactory;
import net.nemezanevem.gregtech.api.registry.gui.UIFactoryRegistry;
import net.nemezanevem.gregtech.common.network.NetworkUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketUIOpen {

    private UIFactory<?> uiFactory;
    private FriendlyByteBuf serializedHolder;
    private int windowId;
    private List<PacketUIWidgetUpdate> initialWidgetUpdates;

    public PacketUIOpen(UIFactory<?> uiFactory, FriendlyByteBuf serializedHolder, int windowId, List<PacketUIWidgetUpdate> initialWidgetUpdates) {
        this.uiFactory = uiFactory;
        this.serializedHolder = serializedHolder;
        this.windowId = windowId;
        this.initialWidgetUpdates = initialWidgetUpdates;
    }

    public static void encode(PacketUIOpen packet, FriendlyByteBuf buf) {
        NetworkUtils.writePacketBuffer(buf, packet.serializedHolder);
        buf.writeRegistryId(UIFactoryRegistry.UI_FACTORIES_BUILTIN.get(), packet.uiFactory);
        buf.writeVarInt(packet.windowId);
        buf.writeVarInt(packet.initialWidgetUpdates.size());
        for (PacketUIWidgetUpdate packet_ : packet.initialWidgetUpdates) {
            PacketUIWidgetUpdate.encode(packet_, buf);
        }
    }

    public static PacketUIOpen decode(FriendlyByteBuf buf) {
        var serializedHolder = NetworkUtils.readPacketBuffer(buf);
        UIFactory<?> uiFactory = buf.readRegistryId();
        var windowId = buf.readVarInt();
        var initialWidgetUpdates = new ArrayList<>();

        int packetsToRead = buf.readVarInt();
        List<PacketUIWidgetUpdate> initialWidgetUpdates_ = new ArrayList<>();
        for (int i = 0; i < packetsToRead; i++) {
            PacketUIWidgetUpdate packet_ = PacketUIWidgetUpdate.decode(buf);
            initialWidgetUpdates_.add(packet_);
        }
        return new PacketUIOpen(uiFactory, serializedHolder, windowId, initialWidgetUpdates_);
    }

    public static void handle(PacketUIOpen packet, Supplier<NetworkEvent.Context> handler) {
        handler.get().enqueueWork(() -> {
            UIFactory<?> uiFactory = packet.uiFactory;
            if (uiFactory == null) {
                GregTech.LOGGER.warn("Couldn't find UI Factory with id '{}'", uiFactory);
            } else {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> uiFactory.initClientUI(packet.serializedHolder, packet.windowId, packet.initialWidgetUpdates));
            }
        });
        handler.get().setPacketHandled(true);
    }
}
