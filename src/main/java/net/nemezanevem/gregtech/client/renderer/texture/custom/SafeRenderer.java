package net.nemezanevem.gregtech.client.renderer.texture.custom;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.AtlasRegistrar;
import codechicken.lib.texture.IIconRegister;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Rotation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;

import java.util.Arrays;
import java.util.List;

public class SafeRenderer implements IIconRegister {

    private static final Cuboid6 mainBoxOuter = new Cuboid6(3 / 16.0, 0 / 16.0, 3 / 16.0, 13 / 16.0, 14 / 16.0, 13 / 16.0);
    private static final Cuboid6 mainBoxInner = new Cuboid6(4 / 16.0, 1 / 16.0, 3 / 16.0, 12 / 16.0, 13 / 16.0, 12 / 16.0);
    private static final Cuboid6 doorBox = new Cuboid6(4 / 16.0, 1 / 16.0, 3 / 16.0, 12 / 16.0, 13 / 16.0, 4 / 16.0);
    private static final List<Direction> rotations = Arrays.asList(Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST);

    private final String basePath;

    private TextureAtlasSprite[] textures;

    public SafeRenderer(String basePath) {
        this.basePath = basePath;
        Textures.iconRegisters.add(this);
    }

    public TextureAtlasSprite getParticleTexture() {
        return textures[1];
    }

    @Override
    public void registerIcons(AtlasRegistrar textureMap) {
        String formattedBase = GregTech.MODID + ":blocks/" + basePath;
        this.textures = new TextureAtlasSprite[7];
        textureMap.registerSprite(new ResourceLocation(formattedBase + "/base_bottom"), val -> this.textures[0] = val);
        textureMap.registerSprite(new ResourceLocation(formattedBase + "/base_top"), val -> this.textures[1] = val);
        textureMap.registerSprite(new ResourceLocation(formattedBase + "/base_side"), val -> this.textures[2] = val);
        textureMap.registerSprite(new ResourceLocation(formattedBase + "/base_front"), val -> this.textures[3] = val);

        textureMap.registerSprite(new ResourceLocation(formattedBase + "/door_side"), val -> this.textures[4] = val);
        textureMap.registerSprite(new ResourceLocation(formattedBase + "/door_back"), val -> this.textures[5] = val);
        textureMap.registerSprite(new ResourceLocation(formattedBase + "/door_front"), val -> this.textures[6] = val);
    }

    public void render(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Direction rotation, float capRotation) {
        translation.translate(0.5, 0.5, 0.5);
        translation.rotate(Math.toRadians(90.0 * rotations.indexOf(rotation)), Rotation.axes[1]);
        translation.translate(-0.5, -0.5, -0.5);

        for (Direction renderSide : Direction.values()) {
            TextureAtlasSprite baseSprite = renderSide.getAxis() == Direction.Axis.Y ?
                    textures[renderSide.ordinal()] :
                    renderSide == Direction.NORTH ? textures[3] : textures[2];
            Textures.renderFace(renderState, translation, pipeline, renderSide, mainBoxOuter, baseSprite, RenderType.cutoutMipped());
            if (renderSide == Direction.NORTH) continue;
            Textures.renderFace(renderState, translation, pipeline, renderSide, mainBoxInner, baseSprite, RenderType.cutoutMipped());
        }

        translation.translate(4 / 16.0, 7 / 16.0, 3 / 16.0);
        translation.rotate(Math.toRadians(capRotation), Rotation.axes[1]);
        translation.translate(-4 / 16.0, -7 / 16.0, -3 / 16.0);

        for (Direction renderSide : Direction.values()) {
            TextureAtlasSprite doorSprite =
                    renderSide == Direction.NORTH ? textures[6] :
                            renderSide == Direction.SOUTH ? textures[5] : textures[4];
            Textures.renderFace(renderState, translation, pipeline, renderSide, doorBox, doorSprite, RenderType.cutoutMipped());
        }
    }
}
