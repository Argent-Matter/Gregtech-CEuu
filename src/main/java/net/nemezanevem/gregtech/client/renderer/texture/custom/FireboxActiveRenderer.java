package net.nemezanevem.gregtech.client.renderer.texture.custom;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.nemezanevem.gregtech.client.renderer.cclop.ColourOperation;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import net.nemezanevem.gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import net.nemezanevem.gregtech.client.renderer.texture.cube.SidedCubeRenderer;
import net.nemezanevem.gregtech.common.ConfigHolder;
import org.apache.commons.lang3.ArrayUtils;

public class FireboxActiveRenderer extends SidedCubeRenderer {

    public FireboxActiveRenderer(String basePath, OrientedOverlayRenderer.OverlayFace... faces) {
        super(basePath, faces);
    }

    @Override
    public void renderOrientedState(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 bounds, Direction frontFacing, boolean isActive, boolean isWorkingEnabled) {
        for (Direction facing : Direction.values()) {
            OrientedOverlayRenderer.OverlayFace overlayFace = OrientedOverlayRenderer.OverlayFace.bySide(facing, frontFacing);
            TextureAtlasSprite renderSprite = sprites.get(overlayFace);
            if (renderSprite != null) {
                if (facing == Direction.UP || facing == Direction.DOWN) {
                    Textures.renderFace(renderState, translation, ArrayUtils.add(pipeline, new ColourOperation(0xffffffff)), facing, bounds, renderSprite, RenderType.cutoutMipped());
                } else {
                    Textures.renderFace(renderState, translation, pipeline, facing, bounds, renderSprite, RenderType.cutoutMipped());
                }
                TextureAtlasSprite emissiveSprite = spritesEmissive.get(overlayFace);
                if (emissiveSprite != null && facing != frontFacing && facing != Direction.UP && facing != Direction.DOWN) {
                    if (ConfigHolder.ClientConfig.machinesEmissiveTextures) {
                        Textures.renderFace(renderState, translation, ArrayUtils.add(pipeline, new ColourOperation(0xffffffff)), facing, bounds, emissiveSprite, BloomEffectUtil.getRealBloomLayer());
                    } else {
                        Textures.renderFace(renderState, translation, pipeline, facing, bounds, emissiveSprite, RenderType.cutoutMipped());
                    }
                }
            }
        }
    }
}
