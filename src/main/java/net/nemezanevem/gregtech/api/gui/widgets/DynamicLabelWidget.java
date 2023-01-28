package net.nemezanevem.gregtech.api.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.util.Position;
import net.nemezanevem.gregtech.api.util.Size;

import java.util.function.Supplier;

/**
 * Represents a label with text, dynamically obtained
 * from supplied getter in constructor
 * Note that this DOESN'T DO SYNC and calls getter on client side only
 * if you're looking for server-side controlled text field, see {@link AdvancedTextWidget}
 */
public class DynamicLabelWidget extends Widget {

    protected final Supplier<String> textSupplier;
    private String lastTextValue = "";
    private final int color;

    public DynamicLabelWidget(int xPosition, int yPosition, Supplier<String> text) {
        this(xPosition, yPosition, text, 0x404040);
    }

    public DynamicLabelWidget(int xPosition, int yPosition, Supplier<String> text, int color) {
        super(new Position(xPosition, yPosition), Size.ZERO);
        this.textSupplier = text;
        this.color = color;
    }

    private void updateSize() {
        Font fontRenderer = Minecraft.getInstance().font;
        String resultText = lastTextValue;
        setSize(new Size(fontRenderer.width(resultText), fontRenderer.lineHeight));
        if (uiAccess != null) {
            uiAccess.notifySizeChange();
        }
    }

    @Override
    public void drawInForeground(PoseStack poseStack, int mouseX, int mouseY) {
        String suppliedText = textSupplier.get();
        if (!suppliedText.equals(lastTextValue)) {
            this.lastTextValue = suppliedText;
            updateSize();
        }
        String[] split = textSupplier.get().split("\n");
        Font fontRenderer = Minecraft.getInstance().font;
        Position position = getPosition();
        for (int i = 0; i < split.length; i++) {
            fontRenderer.drawShadow(poseStack, split[i], position.x, position.y + (i * (fontRenderer.lineHeight + 2)), color);
        }
    }

}
