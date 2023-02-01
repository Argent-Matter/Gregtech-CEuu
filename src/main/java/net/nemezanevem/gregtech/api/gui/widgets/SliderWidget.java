package net.nemezanevem.gregtech.api.gui.widgets;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.gui.IRenderContext;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.gui.resources.TextureArea;
import net.nemezanevem.gregtech.api.util.Position;
import net.nemezanevem.gregtech.api.util.Size;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiFunction;

public class SliderWidget extends Widget {

    public static final BiFunction<String, Float, Component> DEFAULT_TEXT_SUPPLIER = (name, value) -> Component.translatable(name, value.intValue());

    private int sliderWidth = 8;
    private TextureArea backgroundArea = GuiTextures.SLIDER_BACKGROUND;
    private TextureArea sliderIcon = GuiTextures.SLIDER_ICON;
    private final BiFunction<String, Float, Component> textSupplier = DEFAULT_TEXT_SUPPLIER;
    private int textColor = 0xFFFFFF;

    private final float min;
    private final float max;
    private final String name;

    private final FloatConsumer responder;
    private boolean isPositionSent;

    private Component displayString;
    private float sliderPosition;
    public boolean isMouseDown;

    public SliderWidget(String name, int xPosition, int yPosition, int width, int height, float min, float max, float currentValue, FloatConsumer responder) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        Preconditions.checkNotNull(responder, "responder");
        Preconditions.checkNotNull(name, "name");
        this.min = min;
        this.max = max;
        this.name = name;
        this.responder = responder;
        this.sliderPosition = (currentValue - min) / (max - min);
    }

    public SliderWidget setSliderIcon(@Nonnull TextureArea sliderIcon) {
        Preconditions.checkNotNull(sliderIcon, "sliderIcon");
        this.sliderIcon = sliderIcon;
        return this;
    }

    public SliderWidget setBackground(@Nullable TextureArea background) {
        this.backgroundArea = background;
        return this;
    }

    public SliderWidget setSliderWidth(int sliderWidth) {
        this.sliderWidth = sliderWidth;
        return this;
    }

    public SliderWidget setTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    @Override
    public void detectAndSendChanges() {
        if (!isPositionSent) {
            writeUpdateInfo(1, buffer -> buffer.writeFloat(sliderPosition));
            this.isPositionSent = true;
        }
    }

    public float getSliderValue() {
        return this.min + (this.max - this.min) * this.sliderPosition;
    }

    protected Component getDisplayString() {
        return textSupplier.apply(name, getSliderValue());
    }

    @Override
    public void drawInBackground(PoseStack poseStack, int mouseY, int mouseX, float partialTicks, IRenderContext context) {
        Position pos = getPosition();
        Size size = getSize();
        if (backgroundArea != null) {
            backgroundArea.draw(poseStack, pos.x, pos.y, size.width, size.height);
        }
        if (displayString == null) {
            this.displayString = getDisplayString();
        }
        sliderIcon.draw(poseStack, pos.x + (int) (this.sliderPosition * (float) (size.width - 8)), pos.y, sliderWidth, size.height);
        Font fontRenderer = Minecraft.getInstance().font;
        fontRenderer.draw(poseStack, displayString,
                pos.x + size.width / 2f - fontRenderer.width(displayString) / 2f,
                pos.y + size.height / 2f - fontRenderer.lineHeight / 2f, textColor);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int pButton, double dragX, double dragY) {
        if (this.isMouseDown) {
            Position pos = getPosition();
            Size size = getSize();
            this.sliderPosition = (float) (mouseX - (pos.x + 4)) / (float) (size.width - 8);

            if (this.sliderPosition < 0.0F) {
                this.sliderPosition = 0.0F;
            }

            if (this.sliderPosition > 1.0F) {
                this.sliderPosition = 1.0F;
            }

            this.displayString = this.getDisplayString();
            writeClientAction(1, buffer -> buffer.writeFloat(sliderPosition));
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            Position pos = getPosition();
            Size size = getSize();
            this.sliderPosition = (float) (mouseX - (pos.x + 4)) / (float) (size.width - 8);

            if (this.sliderPosition < 0.0F) {
                this.sliderPosition = 0.0F;
            }

            if (this.sliderPosition > 1.0F) {
                this.sliderPosition = 1.0F;
            }
            this.displayString = this.getDisplayString();
            writeClientAction(1, buffer -> buffer.writeFloat(sliderPosition));
            this.isMouseDown = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.isMouseDown = false;
        return false;
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        if (id == 1) {
            this.sliderPosition = buffer.readFloat();
            this.sliderPosition = Mth.clamp(sliderPosition, 0.0f, 1.0f);
            this.responder.accept(getSliderValue());
        }
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 1) {
            this.sliderPosition = buffer.readFloat();
            this.sliderPosition = Mth.clamp(sliderPosition, 0.0f, 1.0f);
            this.displayString = getDisplayString();
        }
    }
}
