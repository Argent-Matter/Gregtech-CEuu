package net.nemezanevem.gregtech.client.renderer.texture.cube;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.AtlasRegistrar;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.gui.resources.ResourceHelper;
import net.nemezanevem.gregtech.client.renderer.ICubeRenderer;
import net.nemezanevem.gregtech.client.renderer.cclop.LightMapOperation;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import net.nemezanevem.gregtech.common.ConfigHolder;
import org.apache.commons.lang3.ArrayUtils;

import java.util.EnumMap;
import java.util.Map;

public class SimpleOrientedCubeRenderer implements ICubeRenderer {

    private final String basePath;

    private Map<CubeSide, TextureAtlasSprite> sprites;

    private Map<CubeSide, TextureAtlasSprite> spritesEmissive;

    private enum CubeSide {
        FRONT, BACK, RIGHT, LEFT, TOP, BOTTOM
    }

    public SimpleOrientedCubeRenderer(String basePath) {
        this.basePath = basePath;
        Textures.CUBE_RENDERER_REGISTRY.put(basePath, this);
        Textures.iconRegisters.add(this);
    }

    @Override
    public void registerIcons(AtlasRegistrar textureMap) {
        String modID = GregTech.MODID;
        String basePath = this.basePath;
        String[] split = this.basePath.split(":");
        if (split.length == 2) {
            modID = split[0];
            basePath = split[1];
        }
        this.sprites = new EnumMap<>(CubeSide.class);
        this.spritesEmissive = new EnumMap<>(CubeSide.class);
        for (CubeSide cubeSide : CubeSide.values()) {
            String fullPath = String.format("blocks/%s/%s", basePath, cubeSide.name().toLowerCase());
            textureMap.registerSprite(new ResourceLocation(modID, fullPath), val -> this.sprites.put(cubeSide, val));
            ResourceLocation emissiveLocation = new ResourceLocation(modID, fullPath + "_emissive");
            if (ResourceHelper.isTextureExist(emissiveLocation)) {
                textureMap.registerSprite(emissiveLocation, val -> this.spritesEmissive.put(cubeSide, val));
            }
        }
    }

    public TextureAtlasSprite getParticleSprite() {
        return sprites.get(CubeSide.FRONT);
    }

    @Override
    public void renderOrientedState(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 bounds, Direction frontFacing, boolean isActive, boolean isWorkingEnabled) {
        Textures.renderFace(renderState, translation, pipeline, Direction.UP, bounds, sprites.get(CubeSide.TOP), RenderType.cutoutMipped());
        Textures.renderFace(renderState, translation, pipeline, Direction.DOWN, bounds, sprites.get(CubeSide.BOTTOM), RenderType.cutoutMipped());

        Textures.renderFace(renderState, translation, pipeline, frontFacing, bounds, sprites.get(CubeSide.FRONT), RenderType.cutoutMipped());
        Textures.renderFace(renderState, translation, pipeline, frontFacing.getOpposite(), bounds, sprites.get(CubeSide.BACK), RenderType.cutoutMipped());

        Textures.renderFace(renderState, translation, pipeline, frontFacing.getClockWise(), bounds, sprites.get(CubeSide.LEFT), RenderType.cutoutMipped());
        Textures.renderFace(renderState, translation, pipeline, frontFacing.getCounterClockWise(), bounds, sprites.get(CubeSide.RIGHT), RenderType.cutoutMipped());

        IVertexOperation[] lightPipeline = ConfigHolder.ClientConfig.machinesEmissiveTextures ?
                ArrayUtils.add(pipeline, new LightMapOperation(240, 240)) : pipeline;

        if (spritesEmissive.containsKey(CubeSide.TOP)) Textures.renderFace(renderState, translation, lightPipeline, Direction.UP, bounds, sprites.get(CubeSide.TOP), BloomEffectUtil.getRealBloomLayer());
        if (spritesEmissive.containsKey(CubeSide.BOTTOM)) Textures.renderFace(renderState, translation, lightPipeline, Direction.DOWN, bounds, sprites.get(CubeSide.BOTTOM), BloomEffectUtil.getRealBloomLayer());

        if (spritesEmissive.containsKey(CubeSide.FRONT)) Textures.renderFace(renderState, translation, lightPipeline, frontFacing, bounds, sprites.get(CubeSide.FRONT), BloomEffectUtil.getRealBloomLayer());
        if (spritesEmissive.containsKey(CubeSide.BACK)) Textures.renderFace(renderState, translation, lightPipeline, frontFacing.getOpposite(), bounds, sprites.get(CubeSide.BACK), BloomEffectUtil.getRealBloomLayer());

        if (spritesEmissive.containsKey(CubeSide.LEFT)) Textures.renderFace(renderState, translation, lightPipeline, frontFacing.getClockWise(), bounds, sprites.get(CubeSide.LEFT), BloomEffectUtil.getRealBloomLayer());
        if (spritesEmissive.containsKey(CubeSide.RIGHT)) Textures.renderFace(renderState, translation, lightPipeline, frontFacing.getCounterClockWise(), bounds, sprites.get(CubeSide.RIGHT), BloomEffectUtil.getRealBloomLayer());
    }

}
