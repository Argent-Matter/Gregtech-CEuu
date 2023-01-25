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
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.client.renderer.cclop.ColourOperation;
import net.nemezanevem.gregtech.client.renderer.cclop.LightMapOperation;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import org.apache.commons.lang3.ArrayUtils;

public class LargeTurbineRenderer implements IIconRegister {

    private TextureAtlasSprite baseRingSprite;
    private TextureAtlasSprite baseBackgroundSprite;
    private TextureAtlasSprite idleBladeSprite;
    private TextureAtlasSprite activeBladeSprite;

    public LargeTurbineRenderer() {
        Textures.iconRegisters.add(this);
    }

    @Override
    public void registerIcons(AtlasRegistrar textureMap) {
        textureMap.registerSprite(new ResourceLocation(GregTech.MODID, "blocks/multiblock/large_turbine/base_ring"), val -> this.baseRingSprite = val);
        textureMap.registerSprite(new ResourceLocation(GregTech.MODID, "blocks/multiblock/large_turbine/base_bg"), val -> this.baseBackgroundSprite = val);
        textureMap.registerSprite(new ResourceLocation(GregTech.MODID, "blocks/multiblock/large_turbine/rotor_idle"), val -> this.idleBladeSprite = val);
        textureMap.registerSprite(new ResourceLocation(GregTech.MODID, "blocks/multiblock/large_turbine/rotor_spinning"), val -> this.activeBladeSprite = val);
    }

    public void renderSided(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Direction side, boolean hasBase, boolean hasRotor, boolean isActive, int rotorRGB) {
        Matrix4 cornerOffset = null;
        switch (side.getAxis()) {
            case X:
                cornerOffset = translation.copy().translate(0.01 * side.getStepX(), -1.0, -1.0);
                cornerOffset.scale(1.0, 3.0, 3.0);
                break;
            case Z:
                cornerOffset = translation.copy().translate(-1.0, -1.0, 0.01 * side.getStepZ());
                cornerOffset.scale(3.0, 3.0, 1.0);
                break;
            case Y:
                cornerOffset = translation.copy().translate(-1.0, 0.01 * side.getStepY(), -1.0);
                cornerOffset.scale(3.0, 1.0, 3.0);
                break;
        }
        if (hasBase) {
            Textures.renderFace(renderState, cornerOffset, ArrayUtils.addAll(pipeline, new LightMapOperation(240, 240)), side, Cuboid6.full, baseRingSprite, RenderType.cutoutMipped());
            Textures.renderFace(renderState, cornerOffset, ArrayUtils.addAll(pipeline, new LightMapOperation(240, 240), new ColourOperation(0xFFFFFFFF)), side, Cuboid6.full, baseBackgroundSprite, RenderType.cutoutMipped());
        }
        if (hasRotor) {
            TextureAtlasSprite sprite = isActive ? activeBladeSprite : idleBladeSprite;
            IVertexOperation[] color = ArrayUtils.add(pipeline, new ColourMultiplier(Util.convertRGBtoOpaqueRGBA_CL(rotorRGB)));
            Textures.renderFace(renderState, cornerOffset, color, side, Cuboid6.full, sprite, RenderType.cutoutMipped());
        }
    }

}
