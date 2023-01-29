package net.nemezanevem.gregtech.common.pipelike.cable.tile;

import net.minecraft.util.ITickable;

public class TileEntityCableTickable extends TileEntityCable implements  {

    public TileEntityCableTickable() {
    }

    @Override
    public void tick() {
        getCoverableImplementation().update();
    }

    @Override
    public boolean supportsTicking() {
        return true;
    }
}
