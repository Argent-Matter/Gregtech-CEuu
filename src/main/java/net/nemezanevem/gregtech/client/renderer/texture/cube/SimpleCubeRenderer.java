package net.nemezanevem.gregtech.client.renderer.texture.cube;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.nemezanevem.gregtech.client.renderer.ICubeRenderer;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;

public class SimpleCubeRenderer implements ICubeRenderer {

    protected final String basePath;

    protected TextureAtlasSprite sprite;

    public SimpleCubeRenderer(String basePath) {
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
        sprite = textureMap.registerSprite(new ResourceLocation(modID, basePath));
    }

    @Override
    public TextureAtlasSprite getParticleSprite() {
        return sprite;
    }

    @Override
    public void renderOrientedState(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 bounds, Direction frontFacing, boolean isActive, boolean isWorkingEnabled) {
        Textures.renderFace(renderState, translation, pipeline, frontFacing, bounds, sprite, RenderType.cutoutMipped());
    }

}
