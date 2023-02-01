package net.nemezanevem.gregtech.api.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.gui.IRenderContext;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.gui.resources.SizedTextureArea;
import net.nemezanevem.gregtech.api.gui.resources.TextureArea;
import net.nemezanevem.gregtech.api.util.Position;
import net.nemezanevem.gregtech.api.util.Size;

import java.util.Arrays;
import java.util.List;
import java.util.function.IntConsumer;

public class IncrementButtonWidget extends Widget {

    private TextureArea buttonTexture = GuiTextures.VANILLA_BUTTON.getSubArea(0.0, 0.0, 1.0, 0.5);
    private final int increment;
    private final int incrementShift;
    private final int incrementCtrl;
    private final int incrementShiftCtrl;
    private final IntConsumer updater;
    private int clickValue;
    private boolean shouldClientCallback;
    private String tooltip;
    protected long hoverStartTime = -1L;
    protected boolean isMouseHovered;
    protected float textScale = 1;


    public IncrementButtonWidget(int x, int y, int width, int height, int increment, int incrementShift, int incrementCtrl, int incrementShiftCtrl, IntConsumer updater) {
        super(x, y, width, height);
        this.increment = increment;
        this.incrementShift = incrementShift;
        this.incrementCtrl = incrementCtrl;
        this.incrementShiftCtrl = incrementShiftCtrl;
        this.updater = updater;
        this.clickValue = increment;
    }

    public IncrementButtonWidget setButtonTexture(TextureArea buttonTexture) {
        this.buttonTexture = buttonTexture;
        return this;
    }

    public IncrementButtonWidget setShouldClientCallback(boolean shouldClientCallback) {
        this.shouldClientCallback = shouldClientCallback;
        return this;
    }

    public IncrementButtonWidget setDefaultTooltip() {
        this.tooltip = "gui.widget.incrementButton.default_tooltip";
        return this;
    }

    public IncrementButtonWidget setTooltip(String tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public IncrementButtonWidget setTextScale(float textScale) {
        this.textScale = textScale;
        return this;
    }

    @Override
    public void updateScreen() {
        this.clickValue = getClickValue();
    }

    @Override
    public void drawInBackground(PoseStack poseStack, int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(poseStack, mouseX, mouseY, partialTicks, context);
        Position position = getPosition();
        Size size = getSize();
        if (buttonTexture instanceof SizedTextureArea) {
            ((SizedTextureArea) buttonTexture).drawHorizontalCutSubArea(position.x, position.y, size.width, size.height, 0.0, 1.0);
        } else {
            buttonTexture.drawSubArea(position.x, position.y, size.width, size.height, 0.0, 0.0, 1.0, 1.0);
        }

        Font fontRenderer = Minecraft.getInstance().font;.
        String text = String.valueOf(clickValue);
        if(clickValue >= 0)
            text = "+" + text;
        drawText(text,
                position.x + size.width / 2f - (fontRenderer.width(text) / 2f) * textScale,
                position.y + size.height / 2f - (fontRenderer.lineHeight / 2f) * textScale, textScale, 0xFFFFFF);
    }

    @Override
    public void drawInForeground(PoseStack poseStack, double mouseX, double mouseY) {
        boolean isHovered = isMouseOverElement(mouseX, mouseY);
        boolean wasHovered = isMouseHovered;
        if (isHovered && !wasHovered) {
            this.isMouseHovered = true;
            this.hoverStartTime = System.currentTimeMillis();
        } else if (!isHovered && wasHovered) {
            this.isMouseHovered = false;
            this.hoverStartTime = 0L;
        } else if (isHovered) {
            long timeSinceHover = System.currentTimeMillis() - hoverStartTime;
            if (timeSinceHover > 750L && tooltip != null) {
                List<String> hoverList = Arrays.asList(Component.translatable(tooltip).split("/n"));
                drawHoveringText(ItemStack.EMPTY, hoverList, 300, mouseX, mouseY);
            }
        }
    }

    private int getClickValue() {
        if (isShiftDown()) {
            if (isCtrlDown())
                return incrementShiftCtrl;
            else
                return incrementShift;
        } else if (isCtrlDown())
            return incrementCtrl;
        return increment;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            if(shouldClientCallback)
                updater.accept(clickValue);
            writeClientAction(-1, buf -> buf.writeInt(clickValue));
            playButtonClickSound();
            return true;
        }
        return false;
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        if (id == -1) {
            updater.accept(buffer.readInt());
        }
    }
}
