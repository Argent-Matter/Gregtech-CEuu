package net.nemezanevem.gregtech.api.pipenet.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.nemezanevem.gregtech.api.pipenet.block.BlockPipe;
import net.nemezanevem.gregtech.api.pipenet.block.IPipeType;
import net.nemezanevem.gregtech.api.unification.material.Material;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface IPipeTile<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType> {

    Level getPipeWorld();

    BlockPos getPipePos();

    default long getTickTimer() {
        return getPipeWorld().getGameTime();
    }

    BlockPipe<PipeType, NodeDataType, ?> getPipeBlock();

    void transferDataFrom(IPipeTile<PipeType, NodeDataType> sourceTile);

    int getPaintingColor();

    void setPaintingColor(int paintingColor);

    boolean isPainted();

    int getDefaultPaintingColor();

    int getConnections();

    boolean isConnected(Direction side);

    void setConnection(Direction side, boolean connected, boolean fromNeighbor);

    // if a face is blocked it will still render as connected, but it won't be able to receive stuff from that direction
    default boolean canHaveBlockedFaces() {
        return true;
    }

    int getBlockedConnections();

    boolean isFaceBlocked(Direction side);

    void setFaceBlocked(Direction side, boolean blocked);

    int getVisualConnections();

    PipeType getPipeType();

    NodeDataType getNodeData();

    PipeCoverableImplementation getCoverableImplementation();

    @Nullable
    Material getFrameMaterial();

    boolean supportsTicking();

    IPipeTile<PipeType, NodeDataType> setSupportsTicking();

    boolean canPlaceCoverOnSide(Direction side);

    <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side);

    <T> LazyOptional<T> getCapabilityInternal(Capability<T> capability, Direction side);

    void notifyBlockUpdate();

    void writeCoverCustomData(int id, Consumer<FriendlyByteBuf> writer);

    void markAsDirty();

    boolean isValidTile();

    void scheduleChunkForRenderUpdate();
}
