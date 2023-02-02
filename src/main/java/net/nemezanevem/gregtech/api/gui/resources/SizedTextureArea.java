package net.nemezanevem.gregtech.api.gui.resources;

import net.minecraft.resources.ResourceLocation;
import net.nemezanevem.gregtech.GregTech;

public class SizedTextureArea extends TextureArea {

    public final float pixelImageWidth;
    public final float pixelImageHeight;

    public SizedTextureArea(ResourceLocation imageLocation, float offsetX, float offsetY, float width, float height, float pixelImageWidth, float pixelImageHeight) {
        super(imageLocation, offsetX, offsetY, width, height);
        this.pixelImageWidth = pixelImageWidth;
        this.pixelImageHeight = pixelImageHeight;
    }

    @Override
    public SizedTextureArea getSubArea(float offsetX, float offsetY, float width, float height) {
        return new SizedTextureArea(imageLocation,
                this.offsetX + (imageWidth * offsetX),
                this.offsetY + (imageHeight * offsetY),
                this.imageWidth * width,
                this.imageHeight * height,
                this.pixelImageWidth * width,
                this.pixelImageHeight * height);
    }

    public static SizedTextureArea fullImage(String imageLocation, int imageWidth, int imageHeight) {
        return new SizedTextureArea(new ResourceLocation(GregTech.MODID, imageLocation), 0.0f, 0.0f, 1.0f, 1.0f, imageWidth, imageHeight);
    }

    public void drawHorizontalCutArea(int x, int y, int width, int height) {
        drawHorizontalCutSubArea(x, y, width, height, 0.0f, 1.0f);
    }

    public void drawVerticalCutArea(int x, int y, int width, int height) {
        drawVerticalCutSubArea(x, y, width, height, 0.0f, 1.0f);
    }

    public void drawHorizontalCutSubArea(int x, int y, int width, int height, float drawnV, float drawnHeight) {
        float drawnWidth = width / 2.0f / pixelImageWidth;
        drawSubArea(x, y, width / 2, height, 0.0f, drawnV, drawnWidth, drawnHeight);
        drawSubArea(x + width / 2f, y, width / 2, height, 1.0f - drawnWidth, drawnV, drawnWidth, drawnHeight);
    }

    public void drawVerticalCutSubArea(int x, int y, int width, int height, float drawnU, float drawnWidth) {
        float drawnHeight = height / 2.0f / pixelImageHeight;
        drawSubArea(x, y, width, height / 2, drawnU, 0.0f, drawnWidth, drawnHeight);
        drawSubArea(x, y + height / 2f, width, height / 2, drawnU, 1.0f - drawnHeight, drawnWidth, drawnHeight);
    }

}
