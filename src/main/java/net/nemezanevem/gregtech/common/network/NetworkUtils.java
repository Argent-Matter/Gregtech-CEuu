package net.nemezanevem.gregtech.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

public class NetworkUtils {

    public static void writePacketBuffer(FriendlyByteBuf writeTo, FriendlyByteBuf writeFrom) {
        writeTo.writeVarInt(writeFrom.readableBytes());
        writeTo.writeBytes(writeFrom);
    }

    public static FriendlyByteBuf readPacketBuffer(FriendlyByteBuf buf) {
        ByteBuf directSliceBuffer = buf.readBytes(buf.readVarInt());
        ByteBuf copiedDataBuffer = Unpooled.copiedBuffer(directSliceBuffer);
        directSliceBuffer.release();
        return new FriendlyByteBuf(copiedDataBuffer);
    }

    public static BlockEntity getTileEntityServer(ResourceKey<Level> dimension, BlockPos pos) {
        return ServerLifecycleHooks.getCurrentServer().getLevel(dimension).getBlockEntity(pos);
    }

    public static BlockState getBlockStateServer(ResourceKey<Level> dimension, BlockPos pos) {
        return ServerLifecycleHooks.getCurrentServer().getLevel(dimension).getBlockState(pos);
    }

    public static PacketDistributor.PacketTarget blockPoint(Level world, BlockPos blockPos) {

        return PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 128.0, world.dimension()));
    }
}
