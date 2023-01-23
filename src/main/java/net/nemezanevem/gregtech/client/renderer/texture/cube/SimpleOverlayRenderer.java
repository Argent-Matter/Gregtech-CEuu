package net.nemezanevem.gregtech.client.renderer.texture.cube;

import codechicken.lib.render.BlockRenderer;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.client.renderer.ICubeRenderer;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;

import javax.annotation.Nullable;

public class SimpleOverlayRenderer implements ICubeRenderer {

    private final String basePath;

    private TextureAtlasSprite sprite;

    @Nullable
    private TextureAtlasSprite spriteEmissive;

    public SimpleOverlayRenderer(String basePath) {
        this.basePath = basePath;
        Textures.CUBE_RENDERER_REGISTRY.put(basePath, this);
        Textures.iconRegisters.add(this);
    }

    @Override
    public void registerIcons(TextureMap textureMap) {
        String modID = GregTech.MODID;
        String basePath = this.basePath;
        String[] split = this.basePath.split(":");
        if (split.length == 2) {
            modID = split[0];
            basePath = split[1];
        }
        this.sprite = textureMap.registerSprite(new ResourceLocation(modID, "blocks/" + basePath));
        ResourceLocation emissiveLocation = new ResourceLocation(modID, "blocks/" + basePath + "_emissive");
        if (ResourceHelper.isTextureExist(emissiveLocation)) {
            this.spriteEmissive = textureMap.registerSprite(emissiveLocation);
        }
    }

    @Override
    public void renderOrientedState(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 bounds, Direction frontFacing, boolean isActive, boolean isWorkingEnabled) {
        Textures.renderFace(renderState, translation, pipeline, frontFacing, bounds, sprite, RenderType.cutoutMipped());
        if (spriteEmissive != null) {
            if (ConfigHolder.client.machinesEmissiveTextures) {
                IVertexOperation[] lightPipeline = ArrayUtils.add(pipeline, new LightMapOperation(240, 240));
                Textures.renderFace(renderState, translation, lightPipeline, frontFacing, bounds, spriteEmissive, BloomEffectUtil.getRealBloomLayer());
            } else Textures.renderFace(renderState, translation, pipeline, frontFacing, bounds, spriteEmissive, BlockRenderLayer.CUTOUT_MIPPED);
        }
    }

    @Override
    public TextureAtlasSprite getParticleSprite() {
        return sprite;
    }

}