package net.nemezanevem.gregtech.api.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.nemezanevem.gregtech.api.gui.IRenderContext;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.util.Position;
import net.nemezanevem.gregtech.api.util.Size;

import java.util.Collections;
import java.util.List;

public class LabelWidget extends Widget {

    protected boolean xCentered;
    protected boolean yCentered;
    protected int width;

    protected final String text;
    protected final Object[] formatting;
    private final int color;
    private boolean dropShadow;
    private List<Component> texts;

    public LabelWidget(int xPosition, int yPosition, String text, Object... formatting) {
        this(xPosition, yPosition, text, 0x404040, formatting);
    }

    public LabelWidget(int xPosition, int yPosition, String text, int color) {
        this(xPosition, yPosition, text, color, new Object[0]);
    }

    public LabelWidget(int xPosition, int yPosition, String text, int color, Object[] formatting) {
        super(new Position(xPosition, yPosition), Size.ZERO);
        this.text = text;
        this.color = color;
        this.formatting = formatting;
        if (isClientSide()) {
            texts = Collections.singletonList(getResultText());
        }
        recomputeSize();
    }

    public LabelWidget setShadow(boolean dropShadow){
        this.dropShadow = dropShadow;
        return this;
    }

    public LabelWidget setWidth(int width) {
        this.width = width;
        if (isClientSide()) {
            texts = Collections.singletonList(getResultText());
        }
        return this;
    }

    public LabelWidget setYCentered(boolean yCentered) {
        this.yCentered = yCentered;
        return this;
    }

    private Component getResultText() {
        return Component.translatable(text, formatting);
    }

    private void recomputeSize() {
        if (isClientSide()) {
            Font fontRenderer = Minecraft.getInstance().font;
            Component resultText = getResultText();
            setSize(new Size(fontRenderer.width(resultText), fontRenderer.lineHeight));
            if (uiAccess != null) {
                uiAccess.notifySizeChange();
            }
        }
    }

    public LabelWidget setXCentered(boolean xCentered) {
        this.xCentered = xCentered;
        return this;
    }

    @Override
    public void drawInBackground(PoseStack poseStack, int mouseY, int mouseX, float partialTicks, IRenderContext context) {
        Font fontRenderer = Minecraft.getInstance().font;
        Position pos = getPosition();
        int height = fontRenderer.lineHeight * texts.size();
        for (int i = 0; i < texts.size(); i++) {
            Component resultText = texts.get(i);
            int width = fontRenderer.width(resultText);
            float x = pos.x - (xCentered ? width / 2f : 0);
            float y = pos.y - (yCentered ? height / 2f : 0) + i * fontRenderer.lineHeight;
            if(dropShadow) fontRenderer.drawShadow(poseStack, resultText, x, y, color);
            else fontRenderer.draw(poseStack, resultText, x, y, color);
        }
    }

}
