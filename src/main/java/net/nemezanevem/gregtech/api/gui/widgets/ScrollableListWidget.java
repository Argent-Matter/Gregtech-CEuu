package net.nemezanevem.gregtech.api.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.util.Mth;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.gui.IRenderContext;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.util.Position;
import net.nemezanevem.gregtech.api.util.Size;
import net.nemezanevem.gregtech.client.util.RenderUtil;

import java.util.List;
import java.util.stream.Collectors;

public class ScrollableListWidget extends AbstractWidgetGroup {

    protected int totalListHeight;
    protected int slotHeight;
    protected int scrollOffset;
    protected final int scrollPaneWidth = 10;
    protected double lastMouseX;
    protected double lastMouseY;
    protected boolean draggedOnScrollBar;

    public ScrollableListWidget(int xPosition, int yPosition, int width, int height) {
        super(new Position(xPosition, yPosition), new Size(width, height));
    }

    @Override
    public void addWidget(Widget widget) {
        super.addWidget(widget);
    }

    @Override
    protected boolean recomputeSize() {
        updateElementPositions();
        return false;
    }

    private void addScrollOffset(int offset) {
        this.scrollOffset = Mth.clamp(scrollOffset + offset, 0, totalListHeight - getSize().height);
        updateElementPositions();
    }

    private boolean isOnScrollPane(double mouseX, double mouseY) {
        Position pos = getPosition();
        Size size = getSize();
        return isMouseOver(pos.x + size.width - scrollPaneWidth, pos.y, scrollPaneWidth, size.height, mouseX, mouseY);
    }

    @Override
    protected void onPositionUpdate() {
        updateElementPositions();
    }

    private void updateElementPositions() {
        Position position = getPosition();
        int currentPosY = position.y - scrollOffset;
        int totalListHeight = 0;
        for (Widget widget : widgets) {
            Position childPosition = new Position(position.x, currentPosY);
            widget.setParentPosition(childPosition);
            currentPosY += widget.getSize().getHeight();
            totalListHeight += widget.getSize().getHeight();
        }
        this.totalListHeight = totalListHeight;
        this.slotHeight = widgets.isEmpty() ? 0 : totalListHeight / widgets.size();
    }

    @Override
    public void drawInForeground(PoseStack poseStack, int mouseX, int mouseY) {
        //make sure mouse is not hovered on any element when outside of bounds,
        //since foreground rendering is not scissored,
        //because cut tooltips don't really look nice
        if (!isPositionInsideScissor(mouseX, mouseY)) {
            mouseX = Integer.MAX_VALUE;
            mouseY = Integer.MAX_VALUE;
        }
        super.drawInForeground(poseStack, mouseX, mouseY);
    }

    @Override
    public void drawInBackground(PoseStack poseStack, int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        //make sure mouse is not hovered on any element when outside of bounds
        if (!isPositionInsideScissor(mouseX, mouseY)) {
            mouseX = Integer.MAX_VALUE;
            mouseY = Integer.MAX_VALUE;
        }
        int finalMouseX = mouseX;
        int finalMouseY = mouseY;
        Position position = getPosition();
        Size size = getSize();
        int paneSize = scrollPaneWidth;
        int scrollX = position.x + size.width - paneSize;
        GuiTextures.SLIDER_BACKGROUND_VERTICAL.draw(scrollX + 1, position.y + 1, paneSize - 2, size.height - 2);

        int maxScrollOffset = totalListHeight - getSize().height + 2;
        float scrollPercent = maxScrollOffset == 0 ? 0 : scrollOffset / (maxScrollOffset * 1.0f);
        int scrollSliderHeight = 14;
        int scrollSliderY = Math.round(position.y + (size.height - scrollSliderHeight) * scrollPercent);
        GuiTextures.SLIDER_ICON.draw(scrollX + 1, scrollSliderY + 2, paneSize - 2, scrollSliderHeight);

        RenderUtil.useScissor(position.x, position.y, size.width - paneSize, size.height, () ->
            super.drawInBackground(poseStack, finalMouseX, finalMouseY, partialTicks, context));
    }

    @Override
    public boolean isWidgetClickable(final Widget widget) {
        if (!super.isWidgetClickable(widget)) {
            return false;
        }
        return isWidgetOverlapsScissor(widget);
    }

    private boolean isPositionInsideScissor(double mouseX, double mouseY) {
        return isMouseOverElement(mouseX, mouseY) && !isOnScrollPane(mouseX, mouseY);
    }

    private boolean isWidgetOverlapsScissor(Widget widget) {
        final Position position = widget.getPosition();
        final Size size = widget.getSize();
        final int x0 = position.x;
        final int y0 = position.y;
        final int x1 = position.x + size.width - 1;
        final int y1 = position.y + size.height - 1;
        return isPositionInsideScissor(x0, y0) ||
                isPositionInsideScissor(x0, y1) ||
                isPositionInsideScissor(x1, y0) ||
                isPositionInsideScissor(x1, y1);
    }

    private boolean isBoxInsideScissor(Rect2i rectangle) {
        return isPositionInsideScissor(rectangle.getX(), rectangle.getY()) &&
                isPositionInsideScissor(rectangle.getX() + rectangle.getWidth() - 1, rectangle.getY() + rectangle.getHeight() - 1);
    }

    @Override
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        if (isMouseOverElement(mouseX, mouseY)) {
            int direction = -Mth.clamp(wheelDelta, -1, 1);
            int moveDelta = direction * (slotHeight / 2);
            addScrollOffset(moveDelta);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        if (isOnScrollPane(mouseX, mouseY)) {
            this.draggedOnScrollBar = true;
        }
        if (isPositionInsideScissor(mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        double mouseDelta = (mouseY - lastMouseY);
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        if (draggedOnScrollBar) {
            addScrollOffset((int) mouseDelta);
            return true;
        }
        if (isPositionInsideScissor(mouseX, mouseY)) {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.draggedOnScrollBar = false;
        if (isPositionInsideScissor(mouseX, mouseY)) {
            return super.mouseReleased(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        if (isPositionInsideScissor(mouseX, mouseY)) {
            return super.getIngredientOverMouse(mouseX, mouseY);
        }
        return null;
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object ingredient) {
        //for phantom targets, show only ones who are fully inside scissor box to avoid visual glitches
        return super.getPhantomTargets(ingredient).stream()
                .filter(it -> isBoxInsideScissor(it.getArea()))
                .collect(Collectors.toList());
    }
}
