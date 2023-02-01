package net.nemezanevem.gregtech.api.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.nemezanevem.gregtech.api.gui.IRenderContext;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.util.Position;
import net.nemezanevem.gregtech.api.util.Size;

/**
 * @author brachy84
 */
public class DrawableWidget extends Widget {

    private BackgroundDrawer backgroundDrawer;
    private ForegroundDrawer foregroundDrawer;

    public DrawableWidget(Position selfPosition, Size size) {
        super(selfPosition, size);
    }

    public DrawableWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public DrawableWidget setBackgroundDrawer(BackgroundDrawer backgroundDrawer) {
        this.backgroundDrawer = backgroundDrawer;
        return this;
    }

    public DrawableWidget setForegroundDrawer(ForegroundDrawer foregroundDrawer) {
        this.foregroundDrawer = foregroundDrawer;
        return this;
    }

    @Override
    public void drawInBackground(PoseStack poseStack, int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        if(backgroundDrawer != null)
            backgroundDrawer.draw(poseStack, mouseX, mouseY, partialTicks, context, this);
    }

    @Override
    public void drawInForeground(PoseStack poseStack, int mouseX, int mouseY) {
        if(foregroundDrawer != null)
            foregroundDrawer.draw(poseStack, mouseX, mouseY, this);
    }

    @FunctionalInterface
    public interface BackgroundDrawer {
        void draw(PoseStack poseStack, double mouseX, double mouseY, float partialTicks, IRenderContext context, Widget widget);
    }

    @FunctionalInterface
    public interface ForegroundDrawer {
        void draw(PoseStack poseStack, double mouseX, double mouseY, Widget widget);
    }
}
