package net.nemezanevem.gregtech.client.renderer;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.IIconRegister;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

public interface ICubeRenderer extends IIconRegister {

    default void render(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        render(renderState, translation, pipeline, Cuboid6.full);
    }

    TextureAtlasSprite getParticleSprite();

    default void render(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 bounds) {
        for (Direction side : Direction.values()) {
            renderSided(side, bounds, renderState, pipeline, translation);
        }
    }

    default void renderSided(Direction side, CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        renderSided(side, Cuboid6.full, renderState, pipeline, translation);
    }

    default void renderSided(Direction side, Cuboid6 bounds, CCRenderState renderState, IVertexOperation[] pipeline, Matrix4 translation) {
        renderOrientedState(renderState, translation, pipeline, bounds, side, false, false);
    }

    default void renderOriented(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 bounds, Direction frontFacing) {
        renderOrientedState(renderState, translation, pipeline, bounds, frontFacing, false, false);
    }

    default void renderOriented(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Direction frontFacing) {
        renderOriented(renderState, translation, pipeline, Cuboid6.full, frontFacing);
    }

    void renderOrientedState(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 bounds, Direction frontFacing, boolean isActive, boolean isWorkingEnabled);

    default void renderOrientedState(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Direction frontFacing, boolean isActive, boolean isWorkingEnabled) {
        renderOrientedState(renderState, translation, pipeline, Cuboid6.full, frontFacing, isActive, isWorkingEnabled);
    }

}
