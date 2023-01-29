package net.nemezanevem.gregtech.common.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.common.metatileentities.MetaTileEntityClipboard;
import net.nemezanevem.gregtech.common.network.NetworkUtils;

import java.util.function.Supplier;

public class PacketClipboardUIWidgetUpdate {

    private ResourceKey<Level> dimension;
    private BlockPos pos;
    private int id;
    private FriendlyByteBuf updateData;

    public PacketClipboardUIWidgetUpdate(ResourceKey<Level> dimension, BlockPos pos, int id, FriendlyByteBuf updateData) {
        this.dimension = dimension;
        this.pos = pos;
        this.id = id;
        this.updateData = updateData;
    }

    public static void encode(PacketClipboardUIWidgetUpdate packet, FriendlyByteBuf buf) {
        NetworkUtils.writeFriendlyByteBuf(buf, packet.updateData);
        buf.writeResourceKey(packet.dimension);
        buf.writeBlockPos(packet.pos);
        buf.writeVarInt(packet.id);
    }

    public void decode(FriendlyByteBuf buf) {
        this.updateData = NetworkUtils.readFriendlyByteBuf(buf);
        this.dimension = buf.readResourceKey(Registry.DIMENSION_REGISTRY);
        this.pos = buf.readBlockPos();
        this.id = buf.readVarInt();
    }

    // TODO This could still be cleaned up
    public static void handle(PacketClipboardUIWidgetUpdate packet, Supplier<NetworkEvent.Context> handler) {
        handler.get().enqueueWork(() -> {
            BlockEntity te = NetworkUtils.getBlockEntityServer(packet.dimension, packet.pos);
            if (te instanceof IGregTechTileEntity && ((IGregTechTileEntity) te).getMetaTileEntity() instanceof MetaTileEntityClipboard) {
                ((MetaTileEntityClipboard) ((IGregTechTileEntity) te).getMetaTileEntity()).readUIAction(handler.get().getSender(), packet.id, packet.updateData);
            }
        });
        handler.get().setPacketHandled(true);
    }
}
