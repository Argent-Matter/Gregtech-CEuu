package net.nemezanevem.gregtech.api.gui.resources;

import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.util.Position;
import net.nemezanevem.gregtech.api.util.PositionedRect;
import net.nemezanevem.gregtech.api.util.Size;

/**
 * Represents a texture area of image
 * This representation doesn't take image size in account, so all image variables are
 * 0.0 - 1.0 bounds
 */
public class TextureArea implements IGuiTexture {

    public final ResourceLocation imageLocation;

    public final float offsetX;
    public final float offsetY;

    public final float imageWidth;
    public final float imageHeight;

    public TextureArea(ResourceLocation imageLocation, float offsetX, float offsetY, float width, float height) {
        this.imageLocation = imageLocation;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.imageWidth = width;
        this.imageHeight = height;
    }

    public static TextureArea fullImage(String imageLocation) {
        return new TextureArea(new ResourceLocation(GregTech.MODID, imageLocation), 0.0f, 0.0f, 1.0f, 1.0f);
    }

    public static TextureArea areaOfImage(String imageLocation, int imageSizeX, int imageSizeY, int u, int v, int width, int height) {
        return new TextureArea(new ResourceLocation(imageLocation),
                (float) (u / (imageSizeX * 1.0)),
                (float) (v / (imageSizeY * 1.0)),
                (float) ((u + width) / (imageSizeX * 1.0)),
                (float) ((v + height) / (imageSizeY * 1.0)));
    }

    public TextureArea getSubArea(float offsetX, float offsetY, float width, float height) {
        return new TextureArea(imageLocation,
                this.offsetX + (imageWidth * offsetX),
                this.offsetY + (imageHeight * offsetY),
                this.imageWidth * width,
                this.imageHeight * height);
    }

    public void drawRotated(PoseStack poseStack, int x, int y, Size areaSize, PositionedRect positionedRect, int orientation) {
        Transformation transformation = createOrientation(areaSize, orientation);
        poseStack.pushPose();
        poseStack.translate(x, y, 0.0f);
        transformation.apply(new Vector3(1, 1, 1));
        draw(poseStack, positionedRect.position.x, positionedRect.position.y, positionedRect.size.width, positionedRect.size.height);
        poseStack.popPose();
    }

    public static Transformation createOrientation(Size areaSize, int orientation) {
        Transformation transformation = new Rotation(Math.toRadians(orientation * 90.0), 0.0, 0.0, 1.0)
                .at(new Vector3(areaSize.width / 2.0, areaSize.height / 2.0, 0.0));
        Size orientedSize = transformSize(transformation, areaSize);
        double offsetX = (areaSize.width - orientedSize.width) / 2.0;
        double offsetY = (areaSize.height - orientedSize.height) / 2.0;
        return transformation.with(new Translation(-offsetX, -offsetY, 0.0));
    }

    public static Size transformSize(Transformation transformation, Size position) {
        Vector3 sizeVector = new Vector3(position.width, position.height, 0.0);
        Vector3 zeroVector = new Vector3(0.0, 0.0, 0.0);
        transformation.apply(zeroVector);
        transformation.apply(sizeVector);
        sizeVector.subtract(zeroVector);
        return new Size((int) Math.abs(sizeVector.x), (int) Math.abs(sizeVector.y));
    }

    public static PositionedRect transformRect(Transformation transformation, PositionedRect positionedRect) {
        Position pos1 = transformPos(transformation, positionedRect.position);
        Position pos2 = transformPos(transformation, positionedRect.position.add(positionedRect.size));
        return new PositionedRect(pos1, pos2);
    }

    public static Position transformPos(Transformation transformation, Position position) {
        Vector3 vector = new Vector3(position.x, position.y, 0.0);
        transformation.apply(vector);
        return new Position((int) vector.x, (int) vector.y);
    }

    public void draw(PoseStack poseStack, double x, double y, int width, int height) {
        drawSubArea(x, y, width, height, 0.0f, 0.0f, 1.0f, 1.0f);
    }

    public void drawSubArea(double x, double y, int width, int height, float drawnU, float drawnV, float drawnWidth, float drawnHeight) {
        //sub area is just different width and height
        float imageU = this.offsetX + (this.imageWidth * drawnU);
        float imageV = this.offsetY + (this.imageHeight * drawnV);
        float imageWidth = this.imageWidth * drawnWidth;
        float imageHeight = this.imageHeight * drawnHeight;
        Minecraft.getInstance().textureManager.bindForSetup(imageLocation);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(x, y + height, 0.0D).uv(imageU, imageV + imageHeight).endVertex();
        bufferbuilder.vertex(x + width, y + height, 0.0D).uv(imageU + imageWidth, imageV + imageHeight).endVertex();
        bufferbuilder.vertex(x + width, y, 0.0D).uv(imageU + imageWidth, imageV).endVertex();
        bufferbuilder.vertex(x, y, 0.0D).uv(imageU, imageV).endVertex();
        tesselator.end();
    }
}
