package net.nemezanevem.gregtech.client.renderer.texture.custom;

import codechicken.lib.render.CCRenderState;
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

public class DrumRenderer implements IIconRegister {
    private final String basePath;

    private TextureAtlasSprite[] textures;

    public DrumRenderer(String basePath) {
        this.basePath = basePath;
        Textures.iconRegisters.add(this);
    }

    @Override
    public void registerIcons(AtlasRegistrar textureMap) {
        String formattedBase = GregTech.MODID + ":blocks/" + basePath;
        this.textures = new TextureAtlasSprite[3];
        textureMap.registerSprite(new ResourceLocation(formattedBase + "/top"), val -> this.textures[0] = val);
        textureMap.registerSprite(new ResourceLocation(formattedBase + "/side"), val -> this.textures[1] = val);
        textureMap.registerSprite(new ResourceLocation(formattedBase + "/bottom"), val -> this.textures[3] = val);
    }

    public void render(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Direction rotation) {

        for (Direction renderSide : Direction.values()) {
            TextureAtlasSprite baseSprite = renderSide == Direction.UP ? textures[0] : renderSide == Direction.DOWN ? textures[2] : textures[1];
            Textures.renderFace(renderState, translation, pipeline, renderSide, Cuboid6.full, baseSprite, RenderType.cutoutMipped());
        }
    }

    public TextureAtlasSprite getParticleTexture() {
        return textures[0];
    }

}
