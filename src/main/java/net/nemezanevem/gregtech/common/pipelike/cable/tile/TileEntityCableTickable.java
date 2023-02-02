package net.nemezanevem.gregtech.common.pipelike.cable.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityCableTickable extends TileEntityCable {

    public TileEntityCableTickable(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    public void tick() {
        getCoverableImplementation().tick();
    }

    @Override
    public boolean supportsTicking() {
        return true;
    }
}
