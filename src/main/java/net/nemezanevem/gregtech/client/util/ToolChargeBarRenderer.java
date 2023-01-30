package net.nemezanevem.gregtech.client.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.nemezanevem.gregtech.api.capability.GregtechCapabilities;
import net.nemezanevem.gregtech.api.capability.IElectricItem;
import net.nemezanevem.gregtech.api.item.metaitem.stats.IItemDurabilityManager;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.awt.*;

// Thanks to EnderIO, slightly modified
public final class ToolChargeBarRenderer {

    private static final double BAR_W = 12d;

    private static final Color colorShadow = new Color(0, 0, 0, 255);
    private static final Color colorBG = new Color(0x0E, 0x01, 0x16, 255);

    private static final Color colorBarLeftEnergy = new Color(0, 101, 178, 255);
    private static final Color colorBarRightEnergy = new Color(217, 238, 255, 255);

    private static final Color colorBarLeftDurability = new Color(20, 124, 0, 255);
    private static final Color colorBarRightDurability = new Color(115, 255, 89, 255);

    private static final Color colorBarLeftDepleted = new Color(122, 0, 0, 255);
    private static final Color colorBarRightDepleted = new Color(255, 27, 27, 255);

    public static void render(double level, int xPosition, int yPosition, int offset, boolean shadow, Color left, Color right, boolean doDepletedColor) {
        double width = level * BAR_W;
        if (doDepletedColor && level <= 0.25) {
            left = colorBarLeftDepleted;
            right = colorBarRightDepleted;
        }

        RenderSystem.enableColorLogicOp();
        RenderSystem.disableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.disableBlend();
        GL11.glShadeModel(GL11.GL_SMOOTH);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder worldrenderer = tessellator.getBuilder();
        worldrenderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        drawShadow(worldrenderer, xPosition + 2, yPosition + 13 - offset, 13, shadow ? 2 : 1);
        drawGrad(worldrenderer, xPosition + 2, yPosition + 13 - offset, (BAR_W + width) / 2, left, right);
        drawBG(worldrenderer, xPosition + 2 + (int) BAR_W, yPosition + 13 - offset, BAR_W - width);
        if (offset == 2) {
            overpaintVanillaRenderBug(worldrenderer, xPosition, yPosition);
        }
        tessellator.end();
        GL11.glShadeModel(GL11.GL_FLAT);
        RenderSystem.enableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.enableDepthTest();
    }

    private static void drawGrad(BufferBuilder renderer, int x, int y, double width, Color left, Color right) {
        renderer.vertex(x, y, 0.0D).color(left.getRed(), left.getGreen(), left.getBlue(), left.getAlpha()).endVertex();
        renderer.vertex(x, y + (double) 1, 0.0D).color(left.getRed(), left.getGreen(), left.getBlue(), left.getAlpha()).endVertex();
        renderer.vertex(x + width, y + (double) 1, 0.0D).color(right.getRed(), right.getGreen(), right.getBlue(), right.getAlpha()).endVertex();
        renderer.vertex(x + width, y, 0.0D).color(right.getRed(), right.getGreen(), right.getBlue(), right.getAlpha()).endVertex();
    }

    private static void drawShadow(BufferBuilder renderer, int x, int y, double width, double height) {
        renderer.vertex(x, y, 0.0D).color(colorShadow.getRed(), colorShadow.getGreen(), colorShadow.getBlue(), colorShadow.getAlpha()).endVertex();
        renderer.vertex(x, y + height, 0.0D).color(colorShadow.getRed(), colorShadow.getGreen(), colorShadow.getBlue(), colorShadow.getAlpha()).endVertex();
        renderer.vertex(x + width, y + height, 0.0D).color(colorShadow.getRed(), colorShadow.getGreen(), colorShadow.getBlue(), colorShadow.getAlpha()).endVertex();
        renderer.vertex(x + width, y, 0.0D).color(colorShadow.getRed(), colorShadow.getGreen(), colorShadow.getBlue(), colorShadow.getAlpha()).endVertex();
    }

    private static void drawBG(BufferBuilder renderer, int x, int y, double width) {
        renderer.vertex(x - width, y, 0.0D).color(colorBG.getRed(), colorBG.getGreen(), colorBG.getBlue(), colorBG.getAlpha()).endVertex();
        renderer.vertex(x - width, y + (double) 1, 0.0D).color(colorBG.getRed(), colorBG.getGreen(), colorBG.getBlue(), colorBG.getAlpha()).endVertex();
        renderer.vertex(x, y + (double) 1, 0.0D).color(colorBG.getRed(), colorBG.getGreen(), colorBG.getBlue(), colorBG.getAlpha()).endVertex();
        renderer.vertex(x, y, 0.0D).color(colorBG.getRed(), colorBG.getGreen(), colorBG.getBlue(), colorBG.getAlpha()).endVertex();
    }

    private static void overpaintVanillaRenderBug(BufferBuilder worldrenderer, int xPosition, int yPosition) {
        drawShadow(worldrenderer, xPosition + 2 + 12, yPosition + 13, 1, 1);
    }

    public static void renderBarsTool(IGTTool tool, ItemStack stack, int xPosition, int yPosition) {
        boolean renderedDurability = false;
        CompoundTag tag = GTUtility.getOrCreateNbtCompound(stack);
        if (!tag.getBoolean(ToolHelper.UNBREAKABLE_KEY)) {
            renderedDurability = renderDurabilityBar(stack.getItem().getDurabilityForDisplay(stack), xPosition, yPosition);
        }
        if (tool.isElectric()) {
            renderElectricBar(tool.getCharge(stack), tool.getMaxCharge(stack), xPosition, yPosition, renderedDurability);
        }
    }

    public static void renderBarsItem(MetaItem<?> metaItem, ItemStack stack, int xPosition, int yPosition) {
        boolean renderedDurability = false;
        MetaItem<?>.MetaValueItem valueItem = metaItem.getItem(stack);
        if (valueItem != null && valueItem.getDurabilityManager() != null) {
            renderedDurability = renderDurabilityBar(stack, valueItem.getDurabilityManager(), xPosition, yPosition);
        }

        IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem != null) {
            renderElectricBar(electricItem.getCharge(), electricItem.getMaxCharge(), xPosition, yPosition, renderedDurability);
        }
    }

    private static void renderElectricBar(long charge, long maxCharge, int xPosition, int yPosition, boolean renderedDurability) {
        if (charge > 0 && maxCharge > 0) {
            double level = (double) charge / (double) maxCharge;
            render(level, xPosition, yPosition, renderedDurability ? 2 : 0, true, colorBarLeftEnergy, colorBarRightEnergy, true);
        }
    }

    private static boolean renderDurabilityBar(ItemStack stack, IItemDurabilityManager manager, int xPosition, int yPosition) {
        double level = manager.getDurabilityForDisplay(stack);
        if (level == 0.0 && !manager.showEmptyBar(stack)) return false;
        if (level == 1.0 && !manager.showFullBar(stack)) return false;
        Pair<Color, Color> colors = manager.getDurabilityColorsForDisplay(stack);
        boolean doDepletedColor = manager.doDamagedStateColors(stack);
        Color left = colors != null ? colors.getLeft() : colorBarLeftDurability;
        Color right = colors != null ? colors.getRight() : colorBarRightDurability;
        render(level, xPosition, yPosition, 0, true, left, right, doDepletedColor);
        return true;
    }

    private static boolean renderDurabilityBar(double level, int xPosition, int yPosition) {
        render(level, xPosition, yPosition, 0, true, colorBarLeftDurability, colorBarRightDurability, true);
        return true;
    }

    private ToolChargeBarRenderer() {
    }
}
