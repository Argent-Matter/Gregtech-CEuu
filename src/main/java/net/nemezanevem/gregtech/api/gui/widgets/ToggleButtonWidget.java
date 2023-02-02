package net.nemezanevem.gregtech.api.gui.widgets;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
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

import java.util.List;
import java.util.function.BooleanSupplier;

public class ToggleButtonWidget extends Widget {

    private BooleanSupplier predicate;
    private boolean isVisible = true;
    protected TextureArea buttonTexture;
    private final BooleanSupplier isPressedCondition;
    private final BooleanConsumer setPressedExecutor;
    private String tooltipText;
    private Object[] tooltipArgs;
    protected boolean isPressed;
    private boolean shouldUseBaseBackground;

    public ToggleButtonWidget(int xPosition, int yPosition, int width, int height, BooleanSupplier isPressedCondition, BooleanConsumer setPressedExecutor) {
        this(xPosition, yPosition, width, height, GuiTextures.VANILLA_BUTTON, isPressedCondition, setPressedExecutor);
    }

    public ToggleButtonWidget(int xPosition, int yPosition, int width, int height, TextureArea buttonTexture,
                              BooleanSupplier isPressedCondition, BooleanConsumer setPressedExecutor) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        Preconditions.checkNotNull(buttonTexture, "texture");
        this.buttonTexture = buttonTexture;
        this.isPressedCondition = isPressedCondition;
        this.setPressedExecutor = setPressedExecutor;
    }

    public ToggleButtonWidget setButtonTexture(TextureArea texture) {
        Preconditions.checkNotNull(texture, "texture");
        this.buttonTexture = texture;
        return this;
    }

    public ToggleButtonWidget setTooltipText(String tooltipText, Object... args) {
        Preconditions.checkNotNull(tooltipText, "tooltipText");
        this.tooltipText = tooltipText;
        this.tooltipArgs = args;
        return this;
    }

    public ToggleButtonWidget setPredicate(BooleanSupplier predicate) {
        this.predicate = predicate;
        this.isVisible = false;
        return this;
    }

    public ToggleButtonWidget shouldUseBaseBackground() {
        this.shouldUseBaseBackground = true;
        return this;
    }

    @Override
    public void drawInBackground(PoseStack poseStack, int mouseY, int mouseX, float partialTicks, IRenderContext context) {
        if (!isVisible) return;
        Position pos = getPosition();
        Size size = getSize();
        if (shouldUseBaseBackground) {
            GuiTextures.TOGGLE_BUTTON_BACK.drawSubArea(pos.x, pos.y, size.width, size.height, 0, isPressed ? 0.5f : 0.0f, 1, 0.5f);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            buttonTexture.draw(poseStack, pos.x, pos.y, size.width, size.height);
        } else {
            if (buttonTexture instanceof SizedTextureArea) {
                ((SizedTextureArea) buttonTexture).drawHorizontalCutSubArea(pos.x, pos.y, size.width, size.height, isPressed ? 0.5f : 0.0f, 0.5f);
            } else {
                buttonTexture.drawSubArea(pos.x, pos.y, size.width, size.height, 0, isPressed ? 0.5f : 0.0f, 1, 0.5f);
            }
        }
    }

    @Override
    public void drawInForeground(PoseStack poseStack, int mouseX, int mouseY) {
        if (!isVisible) return;
        if (isMouseOverElement(mouseX, mouseY) && tooltipText != null) {
            String postfix = isPressed ? ".enabled" : ".disabled";
            String tooltipHoverString = tooltipText + postfix;
            List<Component> hoverList = List.of(Component.translatable(tooltipHoverString, tooltipArgs));
            drawHoveringText(poseStack, ItemStack.EMPTY, hoverList, 300, mouseX, mouseY);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (isPressedCondition.getAsBoolean() != isPressed) {
            this.isPressed = isPressedCondition.getAsBoolean();
            writeUpdateInfo(1, buf -> buf.writeBoolean(isPressed));
        }
        if (predicate != null && predicate.getAsBoolean() != isVisible) {
            this.isVisible = predicate.getAsBoolean();
            writeUpdateInfo(2, buf -> buf.writeBoolean(isVisible));
        }
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 1) {
            this.isPressed = buffer.readBoolean();
        } else if (id == 2) {
            this.isVisible = buffer.readBoolean();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (isVisible && isMouseOverElement(mouseX, mouseY)) {
            this.isPressed = !this.isPressed;
            writeClientAction(1, buf -> buf.writeBoolean(isPressed));
            playButtonClickSound();
            return true;
        }
        return false;
    }


    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            this.isPressed = buffer.readBoolean();
            setPressedExecutor.accept(isPressed);
        }
    }

}
