package net.nemezanevem.gregtech.common.pipelike.cable.net;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.nemezanevem.gregtech.api.capability.GregtechCapabilities;
import net.nemezanevem.gregtech.api.capability.IEnergyContainer;
import net.nemezanevem.gregtech.common.pipelike.cable.tile.TileEntityCable;

public class RoutePath {
    private final BlockPos destPipePos;
    private final Direction destFacing;
    private final int distance;
    private final TileEntityCable[] path;
    private final long maxLoss;

    public RoutePath(BlockPos destPipePos, Direction destFacing, TileEntityCable[] path, int distance, long maxLoss) {
        this.destPipePos = destPipePos;
        this.destFacing = destFacing;
        this.path = path;
        this.distance = distance;
        this.maxLoss = maxLoss;
    }

    public int getDistance() {
        return distance;
    }

    public long getMaxLoss() {
        return maxLoss;
    }

    public TileEntityCable[] getPath() {
        return path;
    }

    public BlockPos getPipePos() {
        return destPipePos;
    }

    public Direction getFaceToHandler() {
        return destFacing;
    }

    public BlockPos getHandlerPos() {
        return destPipePos.offset(destFacing.getNormal());
    }

    public IEnergyContainer getHandler(Level world) {
        BlockEntity tile = world.getBlockEntity(getHandlerPos());
        if (tile != null) {
            return tile.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, destFacing.getOpposite()).resolve().get();
        }
        return null;
    }
}
