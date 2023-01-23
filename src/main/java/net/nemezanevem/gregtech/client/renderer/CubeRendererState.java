package net.nemezanevem.gregtech.client.renderer;

import codechicken.lib.vec.Cuboid6;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;

import java.util.Arrays;

public class CubeRendererState {
    public final RenderType layer;
    public final boolean[] sideMask;
    public final BlockGetter world;
    public static boolean[] PASS_MASK = new boolean[Direction.values().length];

    static {
        Arrays.fill(PASS_MASK, true);
    }

    public CubeRendererState(RenderType layer, boolean[] sideMask, BlockGetter world) {
        this.layer = layer;
        this.sideMask = sideMask;
        this.world = world;
    }

    public boolean shouldSideBeRendered(Direction face, Cuboid6 bounds) {
        if (!sideMask[face.get3DDataValue()]) { // check if the side is unnecessary be rendered
            if (bounds == Cuboid6.full) {
                return false;
            }
            switch (face) {
                case DOWN:
                    if (bounds.min.y <= 0) return false;
                    break;
                case UP:
                    if (bounds.max.y >= 1) return false;
                    break;
                case NORTH:
                    if (bounds.min.z <= 0) return false;
                    break;
                case SOUTH:
                    if (bounds.max.z >= 1) return false;
                    break;
                case WEST:
                    if (bounds.min.x <= 0) return false;
                    break;
                case EAST:
                    if (bounds.max.x >= 1) return false;
                    break;
            }
        }
        return true;
    }
}
