package net.nemezanevem.gregtech.common.network.packets;

import codechicken.lib.vec.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.nemezanevem.gregtech.api.block.ICustomParticleBlock;

import java.util.function.Supplier;

public class PacketBlockParticle {

    private BlockPos blockPos;
    private Vector3 entityPos;
    private int particlesAmount;

    public PacketBlockParticle(BlockPos blockPos, Vector3 entityPos, int particlesAmount) {
        this.blockPos = blockPos;
        this.entityPos = entityPos;
        this.particlesAmount = particlesAmount;
    }

    public static void encode(PacketBlockParticle packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.blockPos);
        buf.writeFloat((float) packet.entityPos.x);
        buf.writeFloat((float) packet.entityPos.y);
        buf.writeFloat((float) packet.entityPos.z);
        buf.writeVarInt(packet.particlesAmount);
    }

    public static PacketBlockParticle decode(FriendlyByteBuf buf) {
        var blockPos = buf.readBlockPos();
        var entityPos = new Vector3(buf.readFloat(), buf.readFloat(), buf.readFloat());
        var particlesAmount = buf.readVarInt();
        return new PacketBlockParticle(blockPos, entityPos, particlesAmount);
    }

    public static void handle(PacketBlockParticle packet, Supplier<NetworkEvent.Context> handler) {
        handler.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Level world = Minecraft.getInstance().level;
                BlockState blockState = world.getBlockState(packet.blockPos);
                ParticleEngine particleManager = Minecraft.getInstance().particleEngine;
                ((ICustomParticleBlock) blockState.getBlock()).handleCustomParticle(world, packet.blockPos, particleManager, packet.entityPos, packet.particlesAmount);
            });
        });
        handler.get().setPacketHandled(true);
    }
}
