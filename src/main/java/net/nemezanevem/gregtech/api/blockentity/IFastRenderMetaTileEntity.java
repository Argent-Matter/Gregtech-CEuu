package net.nemezanevem.gregtech.api.blockentity;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Matrix4;
import net.minecraft.world.phys.AABB;

public interface IFastRenderMetaTileEntity {

    int RENDER_PASS_NORMAL = 0;
    int RENDER_PASS_TRANSLUCENT = 1;

    default void renderMetaTileEntityFast(CCRenderState renderState, Matrix4 translation, float partialTicks) {}

    default void renderMetaTileEntity(double x, double y, double z, float partialTicks) {

    }

    AABB getRenderBoundingBox();

    default boolean shouldRenderInPass(int pass) {
        return pass == RENDER_PASS_NORMAL;
    }

    default boolean isGlobalRenderer() {
        return false;
    }
}
