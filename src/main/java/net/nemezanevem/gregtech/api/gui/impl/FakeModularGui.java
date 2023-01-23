package net.nemezanevem.gregtech.api.gui.impl;

import codechicken.lib.math.MathHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.nemezanevem.gregtech.api.gui.INativeWidget;
import net.nemezanevem.gregtech.api.gui.IRenderContext;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.Widget;

import java.util.List;

public class FakeModularGui implements IRenderContext {
    public final ModularUI modularUI;
    public FakeModularGuiContainer container;
    protected Minecraft mc;
    protected Font fr;

    public FakeModularGui(ModularUI modularUI, FakeModularGuiContainer fakeModularUIContainer){
        this.modularUI = modularUI;
        this.container = fakeModularUIContainer;
        this.modularUI.updateScreenSize(this.modularUI.getWidth(), this.modularUI.getHeight());
        this.mc = Minecraft.getInstance();
        this.fr = mc.font;
    }

    public void updateScreen() {
        modularUI.guiWidgets.values().forEach(Widget::containerTick);
    }

    public void handleWidgetUpdate(int windowId, int widgetId, FriendlyByteBuf updateData) {
        if (windowId == container.windowId) {
            Widget widget = modularUI.guiWidgets.get(widgetId);
            int updateId = updateData.readVarInt();
            if (widget != null) {
                widget.readUpdateInfo(updateId, updateData);
            }
        }
    }

    public void drawScreen(PoseStack poseStack, double x, double y, float partialTicks) {
        float halfW = modularUI.getWidth() / 2f;
        float halfH = modularUI.getHeight() / 2f;
        float scale = 0.5f / Math.max(halfW, halfH);
        int mouseX = (int) ((x / scale) + (halfW > halfH? 0: (halfW - halfH)));
        int mouseY = (int) ((y / scale) + (halfH > halfW? 0: (halfH - halfW)));
        poseStack.translate(-scale * halfW, -scale * halfH, 0);
        poseStack.scale(scale, scale, 1);
        RenderSystem.setShaderColor(modularUI.getRColorForOverlay(), modularUI.getGColorForOverlay(), modularUI.getBColorForOverlay(), 1.0F);
        modularUI.backgroundPath.draw(0, 0, modularUI.getWidth(), modularUI.getHeight());
        poseStack.translate(0, 0, 0.001);
        RenderSystem.depthMask(false);

        drawGuiContainerBackgroundLayer(poseStack, partialTicks, mouseX, mouseY);

        for (int i = 0; i < this.container.inventorySlots.size(); ++i) {
            renderSlot(poseStack, this.container.inventorySlots.get(i), fr);
        }

        poseStack.scale(1, 1, 0);
        drawGuiContainerForegroundLayer(poseStack, mouseX, mouseY);

        for (int i = 0; i < this.container.inventorySlots.size(); ++i) {
            Slot slot = this.container.inventorySlots.get(i);
            if (!slot.getItem().isEmpty() && slot.x < mouseX && mouseX < slot.x + 18 && slot.y < mouseY && mouseY < slot.y + 18) {
                renderToolTip(slot.getItem(), slot.x, slot.y);
            }
        }

        RenderSystem.depthMask(true);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void renderSlot(PoseStack poseStack, Slot slot, Font fontRenderer) {
        ItemStack stack = slot.getItem();
        if (!stack.isEmpty() && slot.isActive()) {
            RenderSystem.disablePolygonOffset();
            Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
            poseStack.pushPose();
            poseStack.scale(1, 1, 0.00001f);
            poseStack.translate(slot.x, slot.y, 0);
            ItemRenderer renderItem = Minecraft.getInstance().getItemRenderer();
            renderItem.renderGuiItem(stack, 0, 0);
            renderItem.renderGuiItemDecorations(Minecraft.getInstance().font, stack, 0, 0, null);
            String text = stack.getCount() > 1? Integer.toString(stack.getCount()) : null;

            if (!stack.isEmpty())
            {
                if (stack.getCount() != 1)
                {
                    String s = text == null ? String.valueOf(stack.getCount()) : text;
                    RenderSystem.setShader(GameRenderer::getPositionColorShader);
                    RenderSystem.disableBlend();
                    fontRenderer.draw(poseStack, s, (float)(17 - fontRenderer.width(s)), (float)9, 16777215);
                    fontRenderer.drawShadow(poseStack, s, (float)(17 - fontRenderer.width(s)), (float)9, 16777215);
                    RenderSystem.enableBlend();
                }

                if (stack.getItem().isBarVisible(stack))
                {
                    RenderSystem.setShader(GameRenderer::getPositionColorShader);
                    RenderSystem.disableTexture();
                    RenderSystem.disableBlend();
                    Tesselator tesselator = Tesselator.getInstance();
                    BufferBuilder bufferbuilder = tesselator.getBuilder();
                    double health = stack.getItem().getBarWidth(stack);
                    int rgbfordisplay = stack.getItem().getBarColor(stack);
                    int i = Math.round(13.0F - (float)health * 13.0F);
                    draw(bufferbuilder, 2, 13, 13, 2, 0, 0, 0, 255);
                    draw(bufferbuilder, 2, 13, i, 1, rgbfordisplay >> 16 & 255, rgbfordisplay >> 8 & 255, rgbfordisplay & 255, 255);
                    RenderSystem.enableBlend();
                    RenderSystem.enableTexture();
                }
                LocalPlayer entityplayersp = Minecraft.getInstance().player;
                float f3 = entityplayersp == null ? 0.0F : entityplayersp.getCooldowns().getCooldownPercent(stack.getItem(), Minecraft.getInstance().getPartialTick());

                if (f3 > 0.0F)
                {
                    RenderSystem.setShader(GameRenderer::getPositionColorShader);
                    RenderSystem.disableTexture();
                    Tesselator tessellator = Tesselator.getInstance();
                    BufferBuilder bufferBuilder = tessellator.getBuilder();
                    draw(bufferBuilder, 0, MathHelper.floor(16.0F * (1.0F - f3)), 16, MathHelper.ceil(16.0F * f3), 255, 255, 255, 127);
                    RenderSystem.enableTexture();
                }
            }

            poseStack.popPose();
            Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
        }
    }

    private static void draw(BufferBuilder renderer, int x, int y, int width, int height, int red, int green, int blue, int alpha)
    {
        renderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        renderer.vertex(x, y, 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.vertex((x), y + height, 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.vertex((x + width), y + height, 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.vertex((x + width), y, 0.0D).color(red, green, blue, alpha).endVertex();
        Tesselator.getInstance().end();
    }

    protected void renderToolTip(ItemStack stack, int x, int y) {
        Font font = Minecraft.getInstance().font;
        GuiUtils.preItemToolTip(stack);
        GuiUtils.drawHoveringText(this.getItemToolTip(stack), x, y, modularUI.getScreenWidth(), modularUI.getScreenHeight(), -1, font);
        GuiUtils.postItemToolTip();
    }

    protected List<Component> getItemToolTip(ItemStack itemStack) {
        List<Component> list = itemStack.getTooltipLines(mc.player, mc.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
        list.set(0, list.get(0).copy().withStyle(itemStack.getItem().getRarity(itemStack).getStyleModifier()));
        for (int i = 1; i < list.size(); ++i) {
            list.set(i, list.get(i).copy().withStyle(ChatFormatting.GRAY));
        }
        return list;
    }

    public void drawGuiContainerBackgroundLayer(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(modularUI.getRColorForOverlay(), modularUI.getGColorForOverlay(), modularUI.getBColorForOverlay(), 1.0F);
        for (Widget widget : modularUI.guiWidgets.values()) {
            poseStack.pushPose();
            RenderSystem.setShaderColor(modularUI.getRColorForOverlay(), modularUI.getGColorForOverlay(), modularUI.getBColorForOverlay(), 1.0F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            widget.drawInBackground(poseStack, mouseX, mouseY, partialTicks, this);
            RenderSystem.disableBlend();
            poseStack.popPose();
        }
    }

    public void drawGuiContainerForegroundLayer(PoseStack poseStack, int mouseX, int mouseY) {
        for (Widget widget : modularUI.guiWidgets.values()) {
            poseStack.pushPose();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            widget.drawInForeground(mouseX, mouseY);
            poseStack.popPose();
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        for (int i = modularUI.guiWidgets.size() - 1; i >= 0; i--) {
            Widget widget = modularUI.guiWidgets.get(i);
            if(widget.isVisible() && widget.isActive() && !(widget instanceof INativeWidget) && widget.mouseClicked(mouseX, mouseY, mouseButton)) {
                return;
            }
        }
    }
}
