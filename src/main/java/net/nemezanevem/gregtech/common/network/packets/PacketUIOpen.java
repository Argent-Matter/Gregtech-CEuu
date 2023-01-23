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

    public void encode(FriendlyByteBuf buf) {
        NetworkUtils.writePacketBuffer(buf, serializedHolder);
        buf.writeRegistryId(UIFactoryRegistry.UI_FACTORIES_BUILTIN.get(), uiFactory);
        buf.writeVarInt(windowId);
        buf.writeVarInt(initialWidgetUpdates.size());
        for (PacketUIWidgetUpdate packet : initialWidgetUpdates) {
            packet.encode(buf);
        }
    }

    public void decode(FriendlyByteBuf buf) {
        this.serializedHolder = NetworkUtils.readPacketBuffer(buf);
        this.uiFactory = buf.readRegistryId();
        this.windowId = buf.readVarInt();
        this.initialWidgetUpdates = new ArrayList<>();

        int packetsToRead = buf.readVarInt();
        for (int i = 0; i < packetsToRead; i++) {
            PacketUIWidgetUpdate packet = new PacketUIWidgetUpdate();
            packet.decode(buf);
            this.initialWidgetUpdates.add(packet);
        }
    }

    public void handle(PacketUIOpen packet, Supplier<NetworkEvent.Context> handler) {
        handler.get().enqueueWork(() -> {
            UIFactory<?> uiFactory = packet.uiFactory;
            if (uiFactory == null) {
                GregTech.LOGGER.warn("Couldn't find UI Factory with id '{}'", uiFactory);
            } else {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> uiFactory.initClientUI(serializedHolder, windowId, initialWidgetUpdates));
            }
        });
        handler.get().setPacketHandled(true);
    }
}
