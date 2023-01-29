package net.nemezanevem.gregtech.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Objects;

public class FacingPos {
    private final BlockPos pos;
    private final Direction facing;

    public FacingPos(BlockPos pos, Direction facing) {
        this.pos = pos;
        this.facing = facing;
    }

    public Direction getFacing() {
        return facing;
    }

    public BlockPos getPos() {
        return pos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FacingPos facingPos = (FacingPos) o;
        return Util.arePosEqual(facingPos.pos, pos) && facing == facingPos.facing;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, facing);
    }
}
