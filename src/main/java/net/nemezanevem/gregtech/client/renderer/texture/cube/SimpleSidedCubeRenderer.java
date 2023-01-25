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

public class SimpleSidedCubeRenderer implements ICubeRenderer {

    public enum RenderSide {
        TOP, BOTTOM, SIDE;

        public static RenderSide bySide(Direction side) {
            if (side == Direction.UP) {
                return TOP;
            } else if (side == Direction.DOWN) {
                return BOTTOM;
            } else return SIDE;
        }
    }

    protected final String basePath;

    protected Map<RenderSide, TextureAtlasSprite> sprites;

    protected Map<RenderSide, TextureAtlasSprite> spritesEmissive;

    public SimpleSidedCubeRenderer(String basePath) {
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
        this.sprites = new EnumMap<>(RenderSide.class);
        this.spritesEmissive = new EnumMap<>(RenderSide.class);
        for (RenderSide overlayFace : RenderSide.values()) {
            String faceName = overlayFace.name().toLowerCase();
            ResourceLocation resourceLocation = new ResourceLocation(modID, String.format("blocks/%s/%s", basePath, faceName));
            textureMap.registerSprite(resourceLocation, val -> sprites.put(overlayFace, val));
            ResourceLocation emissiveLocation = new ResourceLocation(modID, String.format("blocks/%s/%s_emissive", basePath, faceName));
            if (ResourceHelper.isTextureExist(emissiveLocation)) {
                textureMap.registerSprite(emissiveLocation, val -> sprites.put(overlayFace, val));
            }
        }
    }

    public TextureAtlasSprite getSpriteOnSide(RenderSide renderSide) {
        return sprites.get(renderSide);
    }

    @Override
    public TextureAtlasSprite getParticleSprite() {
        return getSpriteOnSide(RenderSide.TOP);
    }

    @Override
    public void renderOrientedState(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 bounds, Direction frontFacing, boolean isActive, boolean isWorkingEnabled) {
        RenderSide overlayFace = RenderSide.bySide(frontFacing);
        TextureAtlasSprite renderSprite = sprites.get(overlayFace);
        Textures.renderFace(renderState, translation, pipeline, frontFacing, bounds, renderSprite, RenderType.cutoutMipped());
        TextureAtlasSprite spriteEmissive = spritesEmissive.get(overlayFace);
        if (spriteEmissive != null) {
            if (ConfigHolder.ClientConfig.machinesEmissiveTextures) {
                IVertexOperation[] lightPipeline = ArrayUtils.add(pipeline, new LightMapOperation(240, 240));
                Textures.renderFace(renderState, translation, lightPipeline, frontFacing, bounds, spriteEmissive, BloomEffectUtil.getRealBloomLayer());
            } else Textures.renderFace(renderState, translation, pipeline, frontFacing, bounds, spriteEmissive, RenderType.cutoutMipped());
        }
    }
}
