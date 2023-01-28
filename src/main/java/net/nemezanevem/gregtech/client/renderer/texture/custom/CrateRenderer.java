package net.nemezanevem.gregtech.client.renderer.texture.custom;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.AtlasRegistrar;
import codechicken.lib.texture.IIconRegister;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import org.apache.commons.lang3.ArrayUtils;

public class CrateRenderer implements IIconRegister {
    private final String basePath;

    private TextureAtlasSprite sideSprite;

    public CrateRenderer(String basePath) {
        this.basePath = basePath;
        Textures.iconRegisters.add(this);
    }

    @Override
    public void registerIcons(AtlasRegistrar textureMap) {
        textureMap.registerSprite(new ResourceLocation(GregTech.MODID, "blocks/" + basePath), val -> this.sideSprite = val);
    }

    public void render(CCRenderState renderState, Matrix4 translation, int baseColor, IVertexOperation[] pipeline) {

        IVertexOperation[] basePipeline = ArrayUtils.add(pipeline, new ColourMultiplier(baseColor));

        for (Direction renderSide : Direction.values()) {
            Textures.renderFace(renderState, translation, basePipeline, renderSide, Cuboid6.full, sideSprite, RenderType.cutoutMipped());
        }
    }

    public TextureAtlasSprite getParticleTexture() {
        return sideSprite;
    }
}