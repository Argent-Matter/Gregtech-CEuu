package net.nemezanevem.gregtech.api.gui.resources;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;

import java.awt.*;

public class ColorRectTexture implements IGuiTexture {
    public int color;

    public ColorRectTexture(int color) {
        this.color = color;
    }

    public ColorRectTexture(Color color) {
        this.color = color.getRGB();
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    @Override
    public void draw(PoseStack poseStack, double x, double y, int width, int height) {
        double j;
        double right = x + width;
        double bottom = y + height;
        if (x < right) {
            j = x;
            x = right;
            right = j;
        }

        if (y < bottom) {
            j = y;
            y = bottom;
            bottom = j;
        }
        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(f, f1, f2, f3);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(x, bottom, 0.0D).endVertex();
        bufferbuilder.vertex(right, bottom, 0.0D).endVertex();
        bufferbuilder.vertex(right, y, 0.0D).endVertex();
        bufferbuilder.vertex(x, y, 0.0D).endVertex();
        tesselator.end();
        RenderSystem.enableTexture();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }
}
