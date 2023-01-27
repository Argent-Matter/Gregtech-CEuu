package net.nemezanevem.gregtech.api.gui.widgets;

import com.google.common.base.Preconditions;
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
import net.nemezanevem.gregtech.client.util.MouseButtonHelper;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ClickButtonWidget extends Widget {

    protected TextureArea buttonTexture = GuiTextures.VANILLA_BUTTON.getSubArea(0.0, 0.0, 1.0, 0.5);
    protected final String displayText;
    protected int textColor = 0xFFFFFF;
    protected final Consumer<Widget.ClickData> onPressCallback;
    protected boolean shouldClientCallback;
    protected Supplier<Boolean> shouldDisplay;

    private String tooltipText;
    private Object[] tooltipArgs;

    public ClickButtonWidget(int xPosition, int yPosition, int width, int height, String displayText, Consumer<ClickData> onPressed) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        this.displayText = displayText;
        this.onPressCallback = onPressed;
        this.shouldDisplay = () -> true;
    }

    public ClickButtonWidget setShouldClientCallback(boolean shouldClientCallback) {
        this.shouldClientCallback = shouldClientCallback;
        return this;
    }

    public ClickButtonWidget setButtonTexture(TextureArea buttonTexture) {
        this.buttonTexture = buttonTexture;
        return this;
    }

    public ClickButtonWidget setTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    public ClickButtonWidget setDisplayFunction(Supplier<Boolean> displayFunction) {
        this.shouldDisplay = displayFunction;
        return this;
    }

    public ClickButtonWidget setTooltipText(String tooltipText, Object... args) {
        Preconditions.checkNotNull(tooltipText, "tooltipText");
        this.tooltipText = tooltipText;
        this.tooltipArgs = args;
        return this;
    }

    @Override
    public boolean isVisible() {
        return super.isVisible() && shouldDisplay.get();
    }

    @Override
    public boolean isActive() {
         return super.isActive() && shouldDisplay.get();
    }

    @Override
    public void drawInBackground(PoseStack poseStack, int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(poseStack, mouseX, mouseY, partialTicks, context);
        if (!shouldDisplay.get()) return;
        Position position = getPosition();
        Size size = getSize();
        if (buttonTexture instanceof SizedTextureArea) {
            ((SizedTextureArea) buttonTexture).drawHorizontalCutSubArea(position.x, position.y, size.width, size.height, 0.0, 1.0);
        } else {
            buttonTexture.drawSubArea(position.x, position.y, size.width, size.height, 0.0f, 0.0f, 1.0f, 1.0f);
        }
        Font fontRenderer = Minecraft.getInstance().font;
        Component text = Component.translatable(displayText);
        fontRenderer.draw(poseStack, text,
                position.x + size.width / 2f - fontRenderer.width(text) / 2f,
                position.y + size.height / 2f - fontRenderer.lineHeight / 2f, textColor);
    }

    @Override
    public void drawInForeground(PoseStack poseStack, int mouseX, int mouseY) {
        super.drawInForeground(poseStack, mouseX, mouseY);
        if (tooltipText != null && isMouseOverElement(mouseX, mouseY)) {
            List<Component> hoverList = List.of(Component.translatable(tooltipText, tooltipArgs));
            drawHoveringText(poseStack, ItemStack.EMPTY, hoverList, 300, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!shouldDisplay.get()) return false;
        if (isMouseOverElement(mouseX, mouseY)) {
            triggerButton();
            return true;
        }
        return false;
    }

    protected void triggerButton() {
        ClickData clickData = new ClickData(MouseButtonHelper.button, isShiftDown(), isCtrlDown());
        writeClientAction(1, clickData::writeToBuf);
        if (shouldClientCallback) {
           onPressCallback.accept(new ClickData(MouseButtonHelper.button, isShiftDown(), isCtrlDown(), true));
        }
        playButtonClickSound();
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            ClickData clickData = ClickData.readFromBuf(buffer);
            onPressCallback.accept(clickData);
        }
    }
}
