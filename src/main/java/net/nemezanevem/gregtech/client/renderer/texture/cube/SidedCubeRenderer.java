package net.nemezanevem.gregtech.client.renderer.texture.cube;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.AtlasRegistrar;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.gui.resources.ResourceHelper;
import net.nemezanevem.gregtech.client.renderer.ICubeRenderer;
import net.nemezanevem.gregtech.client.renderer.cclop.LightMapOperation;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import net.nemezanevem.gregtech.client.renderer.texture.cube.OrientedOverlayRenderer.OverlayFace;
import net.nemezanevem.gregtech.common.ConfigHolder;
import org.apache.commons.lang3.ArrayUtils;

import java.util.EnumMap;
import java.util.Map;

public class SidedCubeRenderer implements ICubeRenderer {

    protected final String basePath;
    protected final OrientedOverlayRenderer.OverlayFace[] faces;

    protected Map<OverlayFace, TextureAtlasSprite> sprites;

    protected Map<OverlayFace, TextureAtlasSprite> spritesEmissive;

    public SidedCubeRenderer(String basePath, OverlayFace... faces) {
        this.basePath = basePath;
        this.faces = faces;
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
        this.sprites = new EnumMap<>(OrientedOverlayRenderer.OverlayFace.class);
        this.spritesEmissive = new EnumMap<>(OverlayFace.class);
        for (OverlayFace overlayFace : faces) {
            String faceName = overlayFace.name().toLowerCase();
            ResourceLocation resourceLocation = new ResourceLocation(modID, String.format("blocks/%s/%s", basePath, faceName));
            textureMap.registerSprite(resourceLocation, val -> sprites.put(overlayFace, val));
            ResourceLocation emissiveLocation = new ResourceLocation(modID, String.format("blocks/%s/%s_emissive", basePath, faceName));
            if (ResourceHelper.isTextureExist(emissiveLocation)) {
                textureMap.registerSprite(emissiveLocation, val -> spritesEmissive.put(overlayFace, val));
            }
        }
    }

    @Override
    public TextureAtlasSprite getParticleSprite() {
        return sprites.get(OverlayFace.TOP);
    }

    @Override
    public void renderOrientedState(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 bounds, Direction frontFacing, boolean isActive, boolean isWorkingEnabled) {
        for (Direction facing : Direction.values()) {
            OverlayFace overlayFace = OverlayFace.bySide(facing, frontFacing);
            TextureAtlasSprite renderSprite = sprites.get(overlayFace);
            if (renderSprite != null) {
                Textures.renderFace(renderState, translation, pipeline, facing, bounds, renderSprite, RenderType.cutoutMipped());

                TextureAtlasSprite emissiveSprite = spritesEmissive.get(overlayFace);
                if (emissiveSprite != null) {
                    if (ConfigHolder.ClientConfig.machinesEmissiveTextures) {
                        IVertexOperation[] lightPipeline = ArrayUtils.add(pipeline, new LightMapOperation(240, 240));
                        Textures.renderFace(renderState, translation, lightPipeline, facing, bounds, emissiveSprite, BloomEffectUtil.getRealBloomLayer());
                    } else {
                        Textures.renderFace(renderState, translation, pipeline, facing, bounds, emissiveSprite, RenderType.cutoutMipped());
                    }
                }
            }
        }
    }
}