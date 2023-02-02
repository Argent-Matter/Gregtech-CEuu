package net.nemezanevem.gregtech.api.gui.resources;


import net.minecraft.resources.ResourceLocation;
import net.nemezanevem.gregtech.GregTech;

public class AdoptableTextureArea extends SizedTextureArea {

    private final int pixelCornerWidth;
    private final int pixelCornerHeight;

    public AdoptableTextureArea(ResourceLocation imageLocation, float offsetX, float offsetY, float width, float height, float pixelImageWidth, float pixelImageHeight, int pixelCornerWidth, int pixelCornerHeight) {
        super(imageLocation, offsetX, offsetY, width, height, pixelImageWidth, pixelImageHeight);
        this.pixelCornerWidth = pixelCornerWidth;
        this.pixelCornerHeight = pixelCornerHeight;
    }

    public static AdoptableTextureArea fullImage(String imageLocation, int imageWidth, int imageHeight, int cornerWidth, int cornerHeight) {
        return new AdoptableTextureArea(new ResourceLocation(GregTech.MODID, imageLocation), 0, 0, 1, 1, imageWidth, imageHeight, cornerWidth, cornerHeight);
    }

    @Override
    public void drawSubArea(double x, double y, int width, int height, float drawnU, float drawnV, float drawnWidth, float drawnHeight) {
        //compute relative sizes
        float cornerWidth = pixelCornerWidth / pixelImageWidth;
        float cornerHeight = pixelCornerHeight / pixelImageHeight;
        //draw up corners
        super.drawSubArea(x, y, pixelCornerWidth, pixelCornerHeight, 0, 0.0f, cornerWidth, cornerHeight);
        super.drawSubArea(x + width - pixelCornerWidth, y, pixelCornerWidth, pixelCornerHeight, 1.0f - cornerWidth, 0.0f, cornerWidth, cornerHeight);
        //draw down corners
        super.drawSubArea(x, y + height - pixelCornerHeight, pixelCornerWidth, pixelCornerHeight, 0.0f, 1.0f - cornerHeight, cornerWidth, cornerHeight);
        super.drawSubArea(x + width - pixelCornerWidth, y + height - pixelCornerHeight, pixelCornerWidth, pixelCornerHeight, 1.0f - cornerWidth, 1.0f - cornerHeight, cornerWidth, cornerHeight);
        //draw horizontal connections
        super.drawSubArea(x + pixelCornerWidth, y, width - 2 * pixelCornerWidth, pixelCornerHeight,
                cornerWidth, 0.0f, 1.0f - 2 * cornerWidth, cornerHeight);
        super.drawSubArea(x + pixelCornerWidth, y + height - pixelCornerHeight, width - 2 * pixelCornerWidth, pixelCornerHeight,
                cornerWidth, 1.0f - cornerHeight, 1.0f - 2 * cornerWidth, cornerHeight);
        //draw vertical connections
        super.drawSubArea(x, y + pixelCornerHeight, pixelCornerWidth, height - 2 * pixelCornerHeight,
                0.0f, cornerHeight, cornerWidth, 1.0f - 2 * cornerHeight);
        super.drawSubArea(x + width - pixelCornerWidth, y + pixelCornerHeight, pixelCornerWidth, height - 2 * pixelCornerHeight,
                1.0f - cornerWidth, cornerHeight, cornerWidth, 1.0f - 2 * cornerHeight);
        //draw central body
        super.drawSubArea(x + pixelCornerWidth, y + pixelCornerHeight,
                width - 2 * pixelCornerWidth, height - 2 * pixelCornerHeight,
                cornerWidth, cornerHeight, 1.0f - 2 * cornerWidth, 1.0f - 2 * cornerHeight);
    }
}
