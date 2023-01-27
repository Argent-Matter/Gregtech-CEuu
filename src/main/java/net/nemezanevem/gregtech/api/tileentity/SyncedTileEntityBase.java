package net.nemezanevem.gregtech.api.tileentity;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.function.Consumer;

public abstract class SyncedTileEntityBase extends BlockEntity {

    public SyncedTileEntityBase(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public abstract void writeInitialSyncData(FriendlyByteBuf buf);

    public abstract void receiveInitialSyncData(FriendlyByteBuf buf);

    public abstract void receiveCustomData(int discriminator, FriendlyByteBuf buf);

    protected final Int2ObjectMap<byte[]> updates = new Int2ObjectArrayMap<>(5);

    public void writeCustomData(int discriminator, Consumer<FriendlyByteBuf> dataWriter) {
        ByteBuf backedBuffer = Unpooled.buffer();
        dataWriter.accept(new FriendlyByteBuf(backedBuffer));
        byte[] updateData = Arrays.copyOfRange(backedBuffer.array(), 0, backedBuffer.writerIndex());
        updates.put(discriminator, updateData);
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 0);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag updateTag = pkt.getTag();
        ListTag listTag = updateTag.getList("d", Tag.TAG_COMPOUND);
        for (Tag entryBase : listTag) {
            CompoundTag entryTag = (CompoundTag) entryBase;
            for (String discriminatorKey : entryTag.getAllKeys()) {
                ByteBuf backedBuffer = Unpooled.copiedBuffer(entryTag.getByteArray(discriminatorKey));
                receiveCustomData(Integer.parseInt(discriminatorKey), new FriendlyByteBuf(backedBuffer));
            }
        }
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag updateTag = super.getUpdateTag();
        ByteBuf backedBuffer = Unpooled.buffer();
        writeInitialSyncData(new FriendlyByteBuf(backedBuffer));
        byte[] updateData = Arrays.copyOfRange(backedBuffer.array(), 0, backedBuffer.writerIndex());
        updateTag.putByteArray("d", updateData);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundTag tag) {
        super.handleUpdateTag(tag); // deserializes Forge data and capabilities
        byte[] updateData = tag.getByteArray("d");
        ByteBuf backedBuffer = Unpooled.copiedBuffer(updateData);
        receiveInitialSyncData(new FriendlyByteBuf(backedBuffer));
    }

}
