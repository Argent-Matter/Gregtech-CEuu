package net.nemezanevem.gregtech.api.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.nemezanevem.gregtech.api.gui.IRenderContext;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.util.Position;
import net.nemezanevem.gregtech.api.util.Size;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Simple one-line text widget with text synced and displayed
 * as the raw string from the server
 */
public class SimpleTextWidget extends Widget {

    protected final String formatLocale;
    protected final int color;
    protected final Supplier<String> textSupplier;
    protected String lastText = "";
    protected boolean isCentered = true;
    protected boolean clientWidget;
    protected boolean isShadow;
    protected float scale = 1;
    protected int width;

    public SimpleTextWidget(int xPosition, int yPosition, String formatLocale, int color, Supplier<String> textSupplier) {
        this(xPosition, yPosition, formatLocale, color, textSupplier, false);
    }

    public SimpleTextWidget(int xPosition, int yPosition, String formatLocale, int color, Supplier<String> textSupplier, boolean clientWidget) {
        super(new Position(xPosition, yPosition), Size.ZERO);
        this.color = color;
        this.formatLocale = formatLocale;
        this.textSupplier = textSupplier;
        this.clientWidget = clientWidget;
    }

    public SimpleTextWidget setWidth(int width) {
        this.width = width;
        return this;
    }

    public SimpleTextWidget(int xPosition, int yPosition, String formatLocale, Supplier<String> textSupplier) {
        this(xPosition, yPosition, formatLocale, 0x404040, textSupplier, false);
    }

    public SimpleTextWidget setShadow(boolean shadow) {
        isShadow = shadow;
        return this;
    }

    public SimpleTextWidget setCenter(boolean isCentered) {
        this.isCentered = isCentered;
        return this;
    }

    public SimpleTextWidget setScale(float scale) {
        this.scale = scale;
        return this;
    }

    private void updateSize() {
        Font fontRenderer = Minecraft.getInstance().font;
        int stringWidth = fontRenderer.width(lastText);
        setSize(new Size(stringWidth, fontRenderer.lineHeight));
        if (uiAccess != null) {
            uiAccess.notifySizeChange();
        }
    }

    @Override
    public void updateScreenOnFrame() {
        super.updateScreenOnFrame();
        if (clientWidget && textSupplier != null) {
            String newString = textSupplier.get();
            if (!newString.equals(lastText)) {
                lastText = newString;
                updateSize();
            }
            lastText = newString;
        }
    }

    @Override
    public void drawInBackground(PoseStack poseStack, int mouseY, int mouseX, float partialTicks, IRenderContext context) {
        Component text = formatLocale.isEmpty() ? Component.translatable(lastText) : Component.translatable(formatLocale, lastText);
        List<FormattedCharSequence> texts;
        if (this.width > 0) {
            texts = Minecraft.getInstance().font.split(text, (int) (width * (1 / scale)));
        } else {
            texts = Collections.singletonList(text.getVisualOrderText());
        }
        Font fontRenderer = Minecraft.getInstance().font;
        Position pos = getPosition();
        float height = fontRenderer.lineHeight * scale * texts.size();
        for (int i = 0; i < texts.size(); i++) {
            FormattedCharSequence resultText = texts.get(i);
            float width = fontRenderer.width(resultText) * scale;
            float x = pos.x - (isCentered ? width / 2f : 0);
            float y = pos.y - (isCentered ? height / 2f : 0) + i * fontRenderer.lineHeight;
            drawText(poseStack, resultText, x, y, scale, color, isShadow);
        }
    }

    @Override
    public void detectAndSendChanges() {
        if (!textSupplier.get().equals(lastText)) {
            this.lastText = textSupplier.get();
            writeUpdateInfo(1, buffer -> buffer.writeUtf(lastText));
        }
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 1) {
            this.lastText = buffer.readUtf();
            updateSize();
        }
    }
}
