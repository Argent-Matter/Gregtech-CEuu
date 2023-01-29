package net.nemezanevem.gregtech.common.pipelike.itempipe.tile;

import net.minecraft.util.ITickable;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

public class TileEntityItemPipeTickable extends TileEntityItemPipe implements BlockEntityTicker<TileEntityItemPipeTickable> {

    private int transferredItems = 0;
    private long timer = 0;

    @Override
    public void tick() {
        getCoverableImplementation().update();
        if (++timer % 20 == 0) {
            transferredItems = 0;
        }
    }

    @Override
    public boolean supportsTicking() {
        return true;
    }

    public void transferItems(int amount) {
        transferredItems += amount;
    }

    public int getTransferredItems() {
        return transferredItems;
    }
}
