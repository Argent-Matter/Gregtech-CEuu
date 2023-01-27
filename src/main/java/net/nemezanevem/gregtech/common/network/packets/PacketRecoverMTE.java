package net.nemezanevem.gregtech.common.network.packets;

import codechicken.lib.util.ServerUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.nemezanevem.gregtech.api.block.machine.BlockMachine;
import net.nemezanevem.gregtech.api.registry.tileentity.MetaTileEntityRegistry;
import net.nemezanevem.gregtech.api.tileentity.interfaces.IGregTechTileEntity;

import java.util.function.Supplier;

import static net.nemezanevem.gregtech.api.capability.GregtechDataCodes.INITIALIZE_MTE;

public class PacketRecoverMTE {

    private ResourceKey<Level> dimension;
    private BlockPos pos;

    public PacketRecoverMTE(ResourceKey<Level> dimension, BlockPos pos) {
        this.dimension = dimension;
        this.pos = pos;
    }

    public static void encode(PacketRecoverMTE packet, FriendlyByteBuf buf) {
        buf.writeResourceKey(packet.dimension);
        buf.writeBlockPos(packet.pos);
    }

    public static PacketRecoverMTE decode(FriendlyByteBuf buf) {
        var dimension = buf.readResourceKey(Registry.DIMENSION_REGISTRY);
        var pos = buf.readBlockPos();
        return new PacketRecoverMTE(dimension, pos);
    }

    public static void handle(PacketRecoverMTE packet, Supplier<NetworkEvent.Context> handler) {
        handler.get().enqueueWork(() -> {
            Level world = ServerUtils.getServer().getLevel(packet.dimension);
            BlockEntity be = world.getBlockEntity(packet.pos);
            if (be instanceof IGregTechTileEntity holder && holder.isValid()) {
                holder.writeCustomData(INITIALIZE_MTE, buffer -> {
                    buffer.writeRegistryId(MetaTileEntityRegistry.META_TILE_ENTITIES_BUILTIN.get(), holder.getMetaTileEntity());
                    holder.getMetaTileEntity().writeInitialSyncData(buffer);
                });
            } else if (!(world.getBlockState(packet.pos).getBlock() instanceof BlockMachine)) {
                handler.get().getSender().connection.send(new ClientboundBlockUpdatePacket(world, packet.pos));
            }
        });
        handler.get().setPacketHandled(true);
    }
}
