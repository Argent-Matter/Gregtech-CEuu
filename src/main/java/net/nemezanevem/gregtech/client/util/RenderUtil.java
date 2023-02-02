package net.nemezanevem.gregtech.client.util;

import codechicken.lib.texture.TextureUtils;
import codechicken.lib.vec.Matrix4;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Quaternion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.nemezanevem.gregtech.api.gui.resources.TextureArea;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;

public class RenderUtil {

    private static final Stack<int[]> scissorFrameStack = new Stack<>();

    public static void useScissor(int x, int y, int width, int height, Runnable codeBlock) {
        pushScissorFrame(x, y, width, height);
        try {
            codeBlock.run();
        } finally {
            popScissorFrame();
        }
    }

    private static int[] peekFirstScissorOrFullScreen() {
        int[] currentTopFrame = scissorFrameStack.isEmpty() ? null : scissorFrameStack.peek();
        if (currentTopFrame == null) {
            Minecraft minecraft = Minecraft.getInstance();
            return new int[]{0, 0, minecraft.screen.width, minecraft.screen.height};
        }
        return currentTopFrame;
    }

    public static void pushScissorFrame(int x, int y, int width, int height) {
        int[] parentScissor = peekFirstScissorOrFullScreen();
        int parentX = parentScissor[0];
        int parentY = parentScissor[1];
        int parentWidth = parentScissor[2];
        int parentHeight = parentScissor[3];

        boolean pushedFrame = false;
        if (x <= parentX + parentWidth && y <= parentY + parentHeight) {
            int newX = Math.max(x, parentX);
            int newY = Math.max(y, parentY);
            int newWidth = width - (newX - x);
            int newHeight = height - (newY - y);
            if (newWidth > 0 && newHeight > 0) {
                int maxWidth = parentWidth - (x - parentX);
                int maxHeight = parentHeight - (y - parentY);
                newWidth = Math.min(maxWidth, newWidth);
                newHeight = Math.min(maxHeight, newHeight);
                applyScissor(newX, newY, newWidth, newHeight);
                scissorFrameStack.push(new int[]{newX, newY, newWidth, newHeight});
                pushedFrame = true;
            }
        }
        if (!pushedFrame) {
            if (scissorFrameStack.isEmpty()) {
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
            }
            scissorFrameStack.push(new int[]{parentX, parentY, parentWidth, parentHeight});
        }
    }

    public static void popScissorFrame() {
        scissorFrameStack.pop();
        int[] parentScissor = peekFirstScissorOrFullScreen();
        int parentX = parentScissor[0];
        int parentY = parentScissor[1];
        int parentWidth = parentScissor[2];
        int parentHeight = parentScissor[3];
        applyScissor(parentX, parentY, parentWidth, parentHeight);
        if (scissorFrameStack.isEmpty()) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    }

    //applies scissor with gui-space coordinates and sizes
    private static void applyScissor(int x, int y, int w, int h) {
        //translate upper-left to bottom-left
        var window = Minecraft.getInstance().getWindow();
        int s = Minecraft.getInstance().options.guiScale().get();
        int translatedY = window.getGuiScaledHeight() - y - h;
        RenderSystem.enableScissor(x * s, translatedY * s, w * s, h * s);
    }

    /***
     * used to render pixels in stencil mask. (e.g. Restrict rendering results to be displayed only in Monitor Screens)
     * if you want to do the similar things in Gui(2D) not World(3D), plz consider using the {@link #useScissor(int, int, int, int, Runnable)}
     * that you don't need to draw mask to build a rect mask easily.
     * @param mask draw mask
     * @param renderInMask rendering in the mask
     * @param shouldRenderMask should mask be rendered too
     */
    public static void useStencil(Runnable mask, Runnable renderInMask, boolean shouldRenderMask) {
        GL11.glStencilMask(0xFF);
        GL11.glClearStencil(0);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glEnable(GL11.GL_STENCIL_TEST);

        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);

        if (!shouldRenderMask) {
            GL11.glColorMask(false, false, false, false);
            GL11.glDepthMask(false);
        }

        mask.run();

        if (!shouldRenderMask) {
            GL11.glColorMask(true, true, true, true);
            GL11.glDepthMask(true);
        }

        GL11.glStencilMask(0x00);
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);

        renderInMask.run();

        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

    public static void moveToFace(PoseStack poseStack, double x, double y, double z, Direction face) {
        poseStack.translate(x + 0.5 + face.getStepX() * 0.5, y + 0.5 + face.getStepY() * 0.5, z + 0.5 + face.getStepZ() * 0.5);
    }

    public static void rotateToFace(PoseStack poseStack, Direction face, @Nullable Direction spin) {
        int angle = spin == Direction.EAST ? 90 : spin == Direction.SOUTH ? 180 : spin == Direction.WEST ? -90 : 0;
        switch (face) {
            case UP:
                poseStack.scale(1.0f, -1.0f, 1.0f);
                poseStack.mulPose(new Quaternion(90.0f, 1.0f, 0.0f, 0.0f));
                poseStack.mulPose(new Quaternion(angle, 0, 0, 1));
                break;
            case DOWN:
                poseStack.scale(1.0f, -1.0f, 1.0f);
                poseStack.mulPose(new Quaternion(-90.0f, 1.0f, 0.0f, 0.0f));
                poseStack.mulPose(new Quaternion(spin == Direction.EAST ? 90 : spin == Direction.NORTH ? 180 : spin == Direction.WEST ? -90 : 0, 0, 0, 1));
                break;
            case EAST:
                poseStack.scale(-1.0f, -1.0f, -1.0f);
                poseStack.mulPose(new Quaternion(-90.0f, 0.0f, 1.0f, 0.0f));
                poseStack.mulPose(new Quaternion(angle, 0, 0, 1));
                break;
            case WEST:
                poseStack.scale(-1.0f, -1.0f, -1.0f);
                poseStack.mulPose(new Quaternion(90.0f, 0.0f, 1.0f, 0.0f));
                poseStack.mulPose(new Quaternion(angle, 0, 0, 1));
                break;
            case NORTH:
                poseStack.scale(-1.0f, -1.0f, -1.0f);
                poseStack.mulPose(new Quaternion(angle, 0, 0, 1));
                break;
            case SOUTH:
                poseStack.scale(-1.0f, -1.0f, -1.0f);
                poseStack.mulPose(new Quaternion(180.0f, 0.0f, 1.0f, 0.0f));
                poseStack.mulPose(new Quaternion(angle, 0, 0, 1));
                break;
            default:
                break;
        }
    }

    private static final Map<TextureAtlasSprite, Integer> textureMap = new HashMap<>();

    public static void bindTextureAtlasSprite(TextureAtlasSprite textureAtlasSprite) {
        if (textureAtlasSprite == null) {
            return;
        }
        if (textureMap.containsKey(textureAtlasSprite)) {
            RenderSystem.bindTexture(textureMap.get(textureAtlasSprite));
            return;
        }

        int glTextureId = -1;

        final int iconWidth = textureAtlasSprite.getWidth();
        final int iconHeight = textureAtlasSprite.getHeight();
        final int frameCount = textureAtlasSprite.getFrameCount();
        if (iconWidth <= 0 || iconHeight <= 0 || frameCount <= 0) {
            return;
        }

        BufferedImage bufferedImage = new BufferedImage(iconWidth, iconHeight * frameCount, BufferedImage.TYPE_4BYTE_ABGR);
        for (int i = 0; i < frameCount; i++) {
            int[] imagePixelData = new int[iconHeight * iconWidth];
            for(int x = 0; x < iconWidth; ++x) {
                for(int y = 0; y < iconHeight; ++y) {
                    imagePixelData[y + x * iconWidth + iconWidth] = textureAtlasSprite.getPixelRGBA(i, x, y);
                }
            }
            bufferedImage.setRGB(0, i * iconHeight, iconWidth, iconHeight, imagePixelData, 0, iconWidth);
        }
        glTextureId = TextureUtil.generateTextureId();
        if (glTextureId != -1) {
            TextureUtil.prepareImage(glTextureId, bufferedImage.getWidth(), bufferedImage.getHeight());
            textureMap.put(textureAtlasSprite, glTextureId);
            RenderSystem.bindTexture(textureMap.get(textureAtlasSprite));
        }
    }

    /***
     * avoid z-fighting. not familiar with the CCL, its a trick.
     * //TODO could DisableDepthMask in the CCL?
     * @param translation origin
     * @param side facing
     * @param layer level
     * @return adjust
     */
    public static Matrix4 adjustTrans(Matrix4 translation, Direction side, int layer) {
        Matrix4 trans = translation.copy();
        switch (side) {
            case DOWN:
                trans.translate(0 , -0.0005D * layer,0);
                break;
            case UP:
                trans.translate(0 , 0.0005D * layer,0);
                break;
            case NORTH:
                trans.translate(0 , 0,-0.0005D * layer);
                break;
            case SOUTH:
                trans.translate(0 , 0,0.0005D * layer);
                break;
            case EAST:
                trans.translate(0.0005D * layer, 0,0);
                break;
            case WEST:
                trans.translate(-0.0005D * layer, 0,0);
                break;
        }
        return trans;
    }

    public static Function<Float, Integer> colorInterpolator(int color1, int color2) {
        int a = color1 >> 24 & 255;
        int r = color1 >> 16 & 255;
        int g = color1 >> 8 & 255;
        int b = color1 & 255;

        int a2 = color2 >> 24 & 255;
        int r2 = color2 >> 16 & 255;
        int g2 = color2 >> 8 & 255;
        int b2 = color2 & 255;
        return (f)->{
            int A = (int) (a * (1 - f) + a2 * (f));
            int R = (int) (r * (1 - f) + r2 * (f));
            int G = (int) (g * (1 - f) + g2 * (f));
            int B = (int) (b * (1 - f) + b2 * (f));
            return A << 24 | R << 16 | G << 8 | B;
        };
    }

    public static void renderRect(float x, float y, float width, float height, float z, int color) {
        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(f, f1, f2, f3);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        buffer.vertex(x + width, y, z).endVertex();
        buffer.vertex(x, y, z).endVertex();
        buffer.vertex(x, y + height, z).endVertex();
        buffer.vertex(x + width, y + height, z).endVertex();
        tesselator.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        RenderSystem.setShaderColor(1,1,1,1);
    }

    public static void renderGradientRect(float x, float y, float width, float height, float z, int startColor, int endColor, boolean horizontal) {
        float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;
        float startRed = (float) (startColor >> 16 & 255) / 255.0F;
        float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
        float startBlue = (float) (startColor & 255) / 255.0F;
        float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;
        float endRed = (float) (endColor >> 16 & 255) / 255.0F;
        float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
        float endBlue = (float) (endColor & 255) / 255.0F;
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.defaultBlendFunc();
        GL11.glShadeModel(GL11.GL_SMOOTH);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        if (horizontal) {
            buffer.vertex(x + width, y, z).color(endRed, endGreen, endBlue, endAlpha).endVertex();
            buffer.vertex(x, y, z).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            buffer.vertex(x, y + height, z).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            buffer.vertex(x + width, y + height, z).color(endRed, endGreen, endBlue, endAlpha).endVertex();
            tesselator.end();
        } else {
            buffer.vertex(x + width, y, z).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            buffer.vertex(x, y, z).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            buffer.vertex(x, y + height, z).color(endRed, endGreen, endBlue, endAlpha).endVertex();
            buffer.vertex(x + width, y + height, z).color(endRed, endGreen, endBlue, endAlpha).endVertex();
            tesselator.end();
        }
        GL11.glShadeModel(GL11.GL_FLAT);
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    public static void renderText(PoseStack poseStack, float x, float y, float z, float scale, int color, final Component renderedText, boolean centered) {
        poseStack.pushPose();
        final Font fr = Minecraft.getInstance().font;
        final int width = fr.width(renderedText.getVisualOrderText());
        poseStack.translate(x, y - scale * 4, z);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.5f * (centered? 1:0)*width, 0.0f, 0.5f );

        fr.draw(poseStack, renderedText, 0, 0, color);
        poseStack.popPose();
    }

    public static void renderText(PoseStack poseStack, float x, float y, float z, float scale, int color, final String renderedText, boolean centered) {
        poseStack.pushPose();
        final Font fr = Minecraft.getInstance().font;
        final int width = fr.width(renderedText);
        poseStack.translate(x, y - scale * 4, z);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.5f * (centered? 1:0)*width, 0.0f, 0.5f );

        fr.draw(poseStack, renderedText, 0, 0, color);
        poseStack.popPose();
    }

    public static void renderItemOverLay(PoseStack poseStack, float x, float y, float z, float scale, ItemStack itemStack) {
        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
        poseStack.pushPose();
        poseStack.scale(scale, scale, 0.0001f);
        poseStack.translate(x * 16, y * 16, z * 16);
        ItemRenderer renderItem = Minecraft.getInstance().getItemRenderer();
        renderItem.renderAndDecorateFakeItem(itemStack, 0, 0);
        poseStack.popPose();
        Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
    }

    public static void renderFluidOverLay(float x, float y, float width, float height, float z, FluidStack fluidStack, float alpha) {
        if (fluidStack != null) {
            var extensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
            int color = extensions.getTintColor(fluidStack);
            float r = (color >> 16 & 255) / 255.0f;
            float g = (color >> 8 & 255) / 255.0f;
            float b = (color & 255) / 255.0f;
            TextureAtlasSprite sprite = TextureUtils.getTexture(extensions.getStillTexture());
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
            Minecraft.getInstance().getTextureManager().getTexture(InventoryMenu.BLOCK_ATLAS);
            Tesselator tess = Tesselator.getInstance();
            BufferBuilder buf = tess.getBuilder();

            buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            float uMin = sprite.getU(16D - width * 16D), uMax = sprite.getU(width * 16D);
            float vMin = sprite.getV1(), vMax = sprite.getV(height * 16D);
            buf.vertex(x, y, z).uv(uMin, vMin).color(r, g, b, alpha).endVertex();
            buf.vertex(x, y + height, z).uv(uMin, vMax).color(r, g, b, alpha).endVertex();
            buf.vertex(x + width, y + height, z).uv(uMax, vMax).color(r, g, b, alpha).endVertex();
            buf.vertex(x + width, y, z).uv(uMax, vMin).color(r, g, b, alpha).endVertex();
            tess.end();

            Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        }
    }

    public static void renderTextureArea(TextureArea textureArea, float x, float y, float width, float height, float z) {
        float imageU = textureArea.offsetX;
        float imageV = textureArea.offsetY;
        float imageWidth = textureArea.imageWidth;
        float imageHeight = textureArea.imageHeight;
        Minecraft.getInstance().textureManager.bindForSetup(textureArea.imageLocation);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(x, y + height, z).uv(imageU, imageV + imageHeight).endVertex();
        bufferbuilder.vertex(x + width, y + height, z).uv(imageU + imageWidth, imageV + imageHeight).endVertex();
        bufferbuilder.vertex(x + width, y, z).uv(imageU + imageWidth, imageV).endVertex();
        bufferbuilder.vertex(x, y, z).uv(imageU, imageV).endVertex();
        tesselator.end();
    }

    public static void renderLineChart(List<Long> data, long max, float x, float y, float width, float height, float lineWidth, int color) {
        float durX = data.size() > 1 ? width / (data.size() - 1) : 0;
        float hY = max > 0 ? height / max : 0;

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_DST_ALPHA);
        RenderSystem.setShaderColor(((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f, ((color >> 24) & 0xFF) / 255f);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        float last_x = x + 0 * durX;
        float last_y = y - data.get(0) * hY;
        for (int i = 0; i < data.size(); i++) {
            float _x = x + i * durX;
            float _y = y - data.get(i) * hY;
            // draw lines
            if (i != 0) {
                bufferbuilder.vertex(last_x, last_y - lineWidth, 0.01D).endVertex();
                bufferbuilder.vertex(last_x, last_y + lineWidth, 0.01D).endVertex();
                bufferbuilder.vertex(_x, _y + lineWidth, 0.01D).endVertex();
                bufferbuilder.vertex(_x, _y - lineWidth, 0.01D).endVertex();
                last_x = _x;
                last_y = _y;
            }
            // draw points
            bufferbuilder.vertex(_x - 3 * lineWidth, _y, 0.01D).endVertex();
            bufferbuilder.vertex(_x, _y + 3 * lineWidth, 0.01D).endVertex();
            bufferbuilder.vertex(_x + 3 * lineWidth, _y, 0.01D).endVertex();
            bufferbuilder.vertex(_x, _y - 3 * lineWidth, 0.01D).endVertex();
        }
        tessellator.end();

        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    public static void renderLine(float x1, float y1, float x2, float y2, float lineWidth, int color) {
        float hypo = (float) Math.sqrt((y1 - y2) * (y1 - y2) + (x1 - x2) * (x1 - x2));
        float w = (x2 - x1) / hypo * lineWidth;
        float h = (y1 - y2) / hypo * lineWidth;

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f, ((color >> 24) & 0xFF) / 255f);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        if (w * h > 0) {
            bufferbuilder.vertex(x1 - w, y1 - h, 0.01D).endVertex();
            bufferbuilder.vertex(x1 + w, y1 + h, 0.01D).endVertex();
            bufferbuilder.vertex(x2 + w, y2 + h, 0.01D).endVertex();
            bufferbuilder.vertex(x2 - w, y2 - h, 0.01D).endVertex();
        } else {
            h = (y2 - y1) / hypo * lineWidth;
            bufferbuilder.vertex(x1 + w, y1 - h, 0.01D).endVertex();
            bufferbuilder.vertex(x1 - w, y1 + h, 0.01D).endVertex();
            bufferbuilder.vertex(x2 - w, y2 + h, 0.01D).endVertex();
            bufferbuilder.vertex(x2 + w, y2 - h, 0.01D).endVertex();
        }
        tessellator.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        RenderSystem.setShaderColor(1,1,1,1);
    }

    public static void drawFluidTexture(double xCoord, double yCoord, TextureAtlasSprite textureSprite, int maskTop, int maskRight, double zLevel) {
        float uMin = textureSprite.getU0();
        float uMax = textureSprite.getU1();
        float vMin = textureSprite.getV0();
        float vMax = textureSprite.getV1();
        uMax = uMax - maskRight / 16.0f * (uMax - uMin);
        vMax = vMax - maskTop / 16.0f * (vMax - vMin);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(xCoord, yCoord + 16, zLevel).uv(uMin, vMax).endVertex();
        buffer.vertex(xCoord + 16 - maskRight, yCoord + 16, zLevel).uv(uMax, vMax).endVertex();
        buffer.vertex(xCoord + 16 - maskRight, yCoord + maskTop, zLevel).uv(uMax, vMin).endVertex();
        buffer.vertex(xCoord, yCoord + maskTop, zLevel).uv(uMin, vMin).endVertex();
        tessellator.end();
    }

    public static void drawFluidForGui(FluidStack contents, int tankCapacity, int startX, int startY, int widthT, int heightT) {
        widthT--;
        heightT--;
        Fluid fluid = contents.getFluid();
        var extensions = IClientFluidTypeExtensions.of(fluid);
        ResourceLocation fluidStill = extensions.getStillTexture();
        TextureAtlasSprite fluidStillSprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidStill);
        int fluidColor = extensions.getTintColor(contents);
        int scaledAmount;
        if (contents.getAmount() == tankCapacity) {
            scaledAmount = heightT;
        } else {
            scaledAmount = contents.getAmount() * heightT / tankCapacity;
        }
        if (contents.getAmount() > 0 && scaledAmount < 1) {
            scaledAmount = 1;
        }
        if (scaledAmount > heightT) {
            scaledAmount = heightT;
        }
        RenderSystem.enableBlend();
        Minecraft.getInstance().textureManager.bindForSetup(InventoryMenu.BLOCK_ATLAS);
        // fluid is RGBA for GT guis, despite MC's fluids being ARGB
        setGlColorFromInt(fluidColor, 0xFF);

        final int xTileCount = widthT / 16;
        final int xRemainder = widthT - xTileCount * 16;
        final int yTileCount = scaledAmount / 16;
        final int yRemainder = scaledAmount - yTileCount * 16;

        final int yStart = startY + heightT;

        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                int width = xTile == xTileCount ? xRemainder : 16;
                int height = yTile == yTileCount ? yRemainder : 16;
                int x = startX + xTile * 16;
                int y = yStart - (yTile + 1) * 16;
                if (width > 0 && height > 0) {
                    int maskTop = 16 - height;
                    int maskRight = 16 - width;

                    drawFluidTexture(x, y, fluidStillSprite, maskTop, maskRight, 0.0);
                }
            }
        }
        RenderSystem.disableBlend();
    }

    public static int packColor(int red, int green, int blue, int alpha) {
        return (red & 0xFF) << 24 | (green & 0xFF) << 16 | (blue & 0xFF) << 8 | (alpha & 0xFF);
    }

    public static void setGlColorFromInt(int colorValue, int opacity) {
        int i = (colorValue & 0xFF0000) >> 16;
        int j = (colorValue & 0xFF00) >> 8;
        int k = (colorValue & 0xFF);
        RenderSystem.setShaderColor(i / 255.0f, j / 255.0f, k / 255.0f, opacity / 255.0f);
    }

    public static void setGlClearColorFromInt(int colorValue, int opacity) {
        int i = (colorValue & 0xFF0000) >> 16;
        int j = (colorValue & 0xFF00) >> 8;
        int k = (colorValue & 0xFF);
        RenderSystem.clearColor(i / 255.0f, j / 255.0f, k / 255.0f, opacity / 255.0f);
    }

    public static int getFluidColor(FluidStack fluidStack) {
        if (fluidStack.getFluid() == Fluids.WATER)
            return 0x3094CF;
        else if (fluidStack.getFluid() == Fluids.LAVA)
            return 0xFFD700;
        return IClientFluidTypeExtensions.of(fluidStack.getFluid()).getTintColor(fluidStack);
    }
}
