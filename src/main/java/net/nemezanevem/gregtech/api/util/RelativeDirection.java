package net.nemezanevem.gregtech.api.util;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

import java.util.function.Function;

/**
 * Relative direction when facing horizontally
 */
public enum RelativeDirection {
    UP(f -> Direction.UP),
    DOWN(f -> Direction.DOWN),
    LEFT(Direction::getCounterClockWise),
    RIGHT(Direction::getClockWise),
    FRONT(Function.identity()),
    BACK(Direction::getOpposite);

    final Function<Direction, Direction> actualFacing;

    RelativeDirection(Function<Direction, Direction> actualFacing) {
        this.actualFacing = actualFacing;
    }

    public Direction getActualFacing(Direction facing) {
        return actualFacing.apply(facing);
    }

    public Direction apply(Direction facing) {
        return actualFacing.apply(facing);
    }

    public Vec3i applyVec3i(Direction facing) {
        return apply(facing).getNormal();
    }
}
