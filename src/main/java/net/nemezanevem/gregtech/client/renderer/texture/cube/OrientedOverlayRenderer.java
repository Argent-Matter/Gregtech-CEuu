package net.nemezanevem.gregtech.client.renderer.texture.cube;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.AtlasRegistrar;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Rotation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.gui.resources.ResourceHelper;
import net.nemezanevem.gregtech.client.renderer.ICubeRenderer;
import net.nemezanevem.gregtech.client.renderer.cclop.LightMapOperation;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import net.nemezanevem.gregtech.client.util.RenderUtil;
import net.nemezanevem.gregtech.common.ConfigHolder;
import org.apache.commons.lang3.ArrayUtils;

import java.util.EnumMap;
import java.util.Map;

public class OrientedOverlayRenderer implements ICubeRenderer {

    public enum OverlayFace {
        FRONT, BACK, TOP, BOTTOM, SIDE;

        public static OverlayFace bySide(Direction side, Direction frontFacing) {
            if (side == frontFacing) {
                return FRONT;
            } else if (side.getOpposite() == frontFacing) {
                return BACK;
            } else if (side == Direction.UP) {
                return TOP;
            } else if (side == Direction.DOWN) {
                return BOTTOM;
            } else return SIDE;
        }
    }

    protected final String basePath;
    protected final OverlayFace[] faces;

    public Map<OverlayFace, ActivePredicate> sprites;

    public static class ActivePredicate {

        private final TextureAtlasSprite normalSprite;
        private final TextureAtlasSprite activeSprite;
        private final TextureAtlasSprite pausedSprite;

        private final TextureAtlasSprite normalSpriteEmissive;
        private final TextureAtlasSprite activeSpriteEmissive;
        private final TextureAtlasSprite pausedSpriteEmissive;

        public ActivePredicate(TextureAtlasSprite normalSprite,
                               TextureAtlasSprite activeSprite,
                               TextureAtlasSprite pausedSprite,
                               TextureAtlasSprite normalSpriteEmissive,
                               TextureAtlasSprite activeSpriteEmissive,
                               TextureAtlasSprite pausedSpriteEmissive) {

            this.normalSprite = normalSprite;
            this.activeSprite = activeSprite;
            this.pausedSprite = pausedSprite;
            this.normalSpriteEmissive = normalSpriteEmissive;
            this.activeSpriteEmissive = activeSpriteEmissive;
            this.pausedSpriteEmissive = pausedSpriteEmissive;
        }

        public TextureAtlasSprite getSprite(boolean active, boolean workingEnabled) {
            if (active) {
                if (workingEnabled) {
                    return activeSprite;
                } else if (pausedSprite != null) {
                    return pausedSprite;
                }
            }
            return normalSprite;
        }

        public TextureAtlasSprite getEmissiveSprite(boolean active, boolean workingEnabled) {
            if (active) {
                if (workingEnabled) {
                    return activeSpriteEmissive;
                } else if (pausedSpriteEmissive != null) {
                    return pausedSpriteEmissive;
                }
            }
            return normalSpriteEmissive;
        }
    }

    public OrientedOverlayRenderer(String basePath, OverlayFace... faces) {
        this.basePath = basePath;
        this.faces = faces;
        Textures.CUBE_RENDERER_REGISTRY.put(basePath, this);
        Textures.iconRegisters.add(this);
    }


    @Override
    public void registerIcons(AtlasRegistrar textureMap) {
        this.sprites = new EnumMap<>(OverlayFace.class);
        String modID = GregTech.MODID;
        String basePath = this.basePath;
        String[] split = this.basePath.split(":");
        if (split.length == 2) {
            modID = split[0];
            basePath = split[1];
        }
        for (OverlayFace overlayFace : faces) {
            String faceName = overlayFace.name().toLowerCase();
            var ref = new Object() {
                TextureAtlasSprite pausedSprite = null;
                TextureAtlasSprite normalSprite = null;
                TextureAtlasSprite activeSprite = null;

                TextureAtlasSprite normalSpriteEmissive = null;
                TextureAtlasSprite pausedSpriteEmissive = null;
                TextureAtlasSprite activeSpriteEmissive = null;
            };

            ResourceLocation normalLocation = new ResourceLocation(modID, String.format("blocks/%s/overlay_%s", basePath, faceName));
            if(ResourceHelper.isTextureExist(normalLocation)) {
                textureMap.registerSprite(normalLocation, val -> ref.normalSprite = val);
            }
            ResourceLocation activeLocation = new ResourceLocation(modID, String.format("blocks/%s/overlay_%s_active", basePath, faceName));
            if(ResourceHelper.isTextureExist(activeLocation)) {
                textureMap.registerSprite(activeLocation, val -> ref.activeSprite = val);
            }
            ResourceLocation pausedLocation = new ResourceLocation(modID, String.format("blocks/%s/overlay_%s_paused", basePath, faceName));
            if(ResourceHelper.isTextureExist(pausedLocation)) {
                textureMap.registerSprite(pausedLocation, val -> ref.pausedSprite = val);
            }

            ResourceLocation normalLocationEmissive = new ResourceLocation(modID, String.format("blocks/%s/overlay_%s_emissive", basePath, faceName));
            if(ResourceHelper.isTextureExist(normalLocationEmissive)) {
                textureMap.registerSprite(normalLocationEmissive, val -> ref.normalSpriteEmissive = val);
            }
            ResourceLocation activeLocationEmissive = new ResourceLocation(modID, String.format("blocks/%s/overlay_%s_active_emissive", basePath, faceName));
            if(ResourceHelper.isTextureExist(activeLocationEmissive)) {
                textureMap.registerSprite(activeLocationEmissive, val -> ref.activeSpriteEmissive = val);
            }
            ResourceLocation pausedLocationEmissive = new ResourceLocation(modID, String.format("blocks/%s/overlay_%s_paused_emissive", basePath, faceName));
            if(ResourceHelper.isTextureExist(pausedLocationEmissive)) {
                textureMap.registerSprite(pausedLocationEmissive, val -> ref.pausedSpriteEmissive = val);
            }
            sprites.put(overlayFace, new ActivePredicate(ref.normalSprite, ref.activeSprite, ref.pausedSprite, ref.normalSpriteEmissive, ref.activeSpriteEmissive, ref.pausedSpriteEmissive));
        }
    }

    @Override
    public TextureAtlasSprite getParticleSprite() {
        for (OrientedOverlayRenderer.ActivePredicate predicate : sprites.values()) {
            if (predicate != null) {
                TextureAtlasSprite sprite = predicate.getSprite(false, false);
                if (sprite != null)
                    return sprite;
            }

        }
        return null;
    }

    @Override
    public void renderOrientedState(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 bounds, Direction frontFacing, boolean isActive, boolean isWorkingEnabled) {
        for (Direction renderSide : Direction.values()) {

            ActivePredicate predicate = sprites.get(OverlayFace.bySide(renderSide, frontFacing));
            if (predicate != null) {
                TextureAtlasSprite renderSprite = predicate.getSprite(isActive, isWorkingEnabled);

                // preserve the original translation when not rotating the top and bottom
                Matrix4 renderTranslation = translation.copy();

                // Rotate the top and bottom faces to match front facing
                Rotation rotation = new Rotation(0, 0, 1, 0);
                if (renderSide == Direction.UP || renderSide == Direction.DOWN) {
                    if (frontFacing == Direction.NORTH) {
                        renderTranslation.translate(1, 0, 1);
                        rotation = new Rotation(Math.PI, 0, 1, 0);
                    } else if (frontFacing == Direction.EAST) {
                        renderTranslation.translate(0, 0, 1);
                        rotation = new Rotation(Math.PI / 2, 0, 1, 0);
                    } else if (frontFacing == Direction.WEST) {
                        renderTranslation.translate(1, 0, 0);
                        rotation = new Rotation(-Math.PI / 2, 0, 1, 0);
                    }
                    renderTranslation = RenderUtil.adjustTrans(renderTranslation, renderSide, 1);
                    renderTranslation.apply(rotation);
                }

                Textures.renderFace(renderState, renderTranslation, ArrayUtils.addAll(pipeline, rotation), renderSide, bounds, renderSprite, RenderType.cutoutMipped());

                TextureAtlasSprite emissiveSprite = predicate.getEmissiveSprite(isActive, isWorkingEnabled);
                if (emissiveSprite != null) {
                    if (ConfigHolder.ClientConfig.machinesEmissiveTextures) {
                        IVertexOperation[] lightPipeline = ArrayUtils.addAll(pipeline, new LightMapOperation(240, 240), rotation);
                        Textures.renderFace(renderState, renderTranslation, lightPipeline, renderSide, bounds, emissiveSprite, BloomEffectUtil.getRealBloomLayer());
                    } else {
                        // have to still render both overlays or else textures will be broken
                        Textures.renderFace(renderState, renderTranslation, ArrayUtils.addAll(pipeline, rotation), renderSide, bounds, emissiveSprite, RenderType.cutoutMipped());
                    }
                }
            }
        }
    }

}
