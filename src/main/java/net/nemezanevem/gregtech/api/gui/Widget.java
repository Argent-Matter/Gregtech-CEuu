package net.nemezanevem.gregtech.api.gui;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.fml.loading.FMLLoader;
import net.nemezanevem.gregtech.api.gui.widgets.WidgetUIAccess;
import net.nemezanevem.gregtech.api.util.Position;
import net.nemezanevem.gregtech.api.util.Size;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Widget is functional element of ModularUI
 * It can draw, perform actions, react to key press and mouse
 * It's information is also synced to client
 */
public abstract class Widget {

    protected transient ModularUI gui;
    protected transient ISizeProvider sizes;
    protected transient WidgetUIAccess uiAccess;
    private transient Position parentPosition = Position.ORIGIN;
    private transient Position selfPosition;
    private transient Position position;
    private transient Size size;
    private transient boolean isVisible;
    private transient boolean isActive;

    public Widget(Position selfPosition, Size size) {
        Preconditions.checkNotNull(selfPosition, "selfPosition");
        Preconditions.checkNotNull(size, "size");
        this.selfPosition = selfPosition;
        this.size = size;
        this.position = this.parentPosition.add(selfPosition);
        this.isVisible = true;
        this.isActive = true;
    }

    public Widget(int x, int y, int width, int height) {
        this(new Position(x, y), new Size(width, height));
    }

    public void setGui(ModularUI gui) {
        this.gui = gui;
    }

    public void setSizes(ISizeProvider sizes) {
        this.sizes = sizes;
    }

    public void setUiAccess(WidgetUIAccess uiAccess) {
        this.uiAccess = uiAccess;
    }

    public void setParentPosition(Position parentPosition) {
        Preconditions.checkNotNull(parentPosition, "parentPosition");
        this.parentPosition = parentPosition;
        recomputePosition();
    }

    public void setSelfPosition(Position selfPosition) {
        Preconditions.checkNotNull(selfPosition, "selfPosition");
        this.selfPosition = selfPosition;
        recomputePosition();
    }

    public Position addSelfPosition(int addX, int addY) {
        this.selfPosition = new Position(selfPosition.x + addX, selfPosition.y + addY);
        recomputePosition();
        return this.selfPosition;
    }

    public Position getSelfPosition() {
        return selfPosition;
    }

    public void setSize(Size size) {
        Preconditions.checkNotNull(size, "size");
        this.size = size;
        onSizeUpdate();
    }

    public final Position getPosition() {
        return position;
    }

    public final Size getSize() {
        return size;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Rect2i toRectangleBox() {
        Position pos = getPosition();
        Size size = getSize();
        return new Rect2i(pos.x, pos.y, size.width, size.height);
    }

    protected void recomputePosition() {
        this.position = this.parentPosition.add(selfPosition);
        onPositionUpdate();
    }

    protected void onPositionUpdate() {
    }

    protected void onSizeUpdate() {
    }

    public boolean isMouseOverElement(int mouseX, int mouseY) {
        Position position = getPosition();
        Size size = getSize();
        return isMouseOver(position.x, position.y, size.width, size.height, mouseX, mouseY);
    }

    public static boolean isMouseOver(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && x + width > mouseX && y + height > mouseY;
    }

    /**
     * Called on both sides to initialize widget data
     */
    public void initWidget() {
    }

    /**
     * Called on serverside to detect changes and synchronize them with clients
     */
    public void detectAndSendChanges() {
    }

    /**
     * Called clientside every tick with this modular UI open
     */
    public void containerTick() {
    }

    /**
     * Called clientside approximately every 1/60th of a second with this modular UI open
     */
    public void updateScreenOnFrame() {
    }

    /**
     * Called each draw tick to draw this widget in GUI
     */
    public void drawInForeground(PoseStack poseStack, int mouseX, int mouseY) {
    }

    /**
     * Called each draw tick to draw this widget in GUI
     */
    public void drawInBackground(PoseStack poseStack, int mouseX, int mouseY, float partialTicks, IRenderContext context) {
    }

    /**
     * Called when mouse wheel is moved in GUI
     * For some -redacted- reason mouseX position is relative against GUI not game window as in other mouse events
     */
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        return false;
    }

    /**
     * Called when mouse is clicked in GUI
     */
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return false;
    }

    /**
     * Called when mouse is pressed and hold down in GUI
     */
    public boolean mouseDragged(int mouseX, int mouseY, int button, double dragX, double dragY) {
        return false;
    }

    /**
     * Called when mouse is released in GUI
     */
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        return false;
    }

    /**
     * Called when key is typed in GUI
     */
    public boolean keyTyped(char charTyped, int keyCode) {
        return false;
    }

    /**
     * Read data received from server's {@link #writeUpdateInfo}
     */
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
    }

    public void handleClientAction(int id, FriendlyByteBuf buffer) {
    }

    public List<INativeWidget> getNativeWidgets() {
        if (this instanceof INativeWidget) {
            return Collections.singletonList((INativeWidget) this);
        }
        return Collections.emptyList();
    }

    /**
     * Writes data to be sent to client's {@link #readUpdateInfo}
     */
    protected void writeUpdateInfo(int id, Consumer<FriendlyByteBuf> packetBufferWriter) {
        if (uiAccess != null && gui != null) {
            uiAccess.writeUpdateInfo(this, id, packetBufferWriter);
        }
    }

    protected void writeClientAction(int id, Consumer<FriendlyByteBuf> packetBufferWriter) {
        if (uiAccess != null) {
            uiAccess.writeClientAction(this, id, packetBufferWriter);
        }
    }

    public static void drawBorder(PoseStack poseStack, int x, int y, int width, int height, int color, int border) {
        drawSolidRect(poseStack, x - border, y - border, width + 2 * border, border, color);
        drawSolidRect(poseStack, x - border, y + height, width + 2 * border, border, color);
        drawSolidRect(poseStack, x - border, y, border, height, color);
        drawSolidRect(poseStack, x + width, y, border, height, color);
    }

    public void drawHoveringText(PoseStack poseStack, ItemStack itemStack, List<Component> tooltip, int maxTextWidth, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        mc.screen.renderTooltip(poseStack, tooltip, itemStack.getTooltipImage(), mouseX, mouseY, itemStack);
        RenderSystem.disableBlend();
    }

    public static void drawStringSized(PoseStack poseStack, String text, double x, double y, int color, boolean dropShadow, float scale, boolean center) {
        poseStack.pushPose();
        Font fontRenderer = Minecraft.getInstance().font;
        double scaledTextWidth = center ? fontRenderer.width(text) * scale : 0.0;
        poseStack.translate(x - scaledTextWidth / 2.0, y, 0.0f);
        poseStack.scale(scale, scale, scale);
        fontRenderer.draw(poseStack, text, 0, 0, color);
        if(dropShadow) fontRenderer.drawShadow(poseStack, text, 0, 0, color);
        poseStack.popPose();
    }

    public static void drawStringFixedCorner(PoseStack poseStack, String text, double x, double y, int color, boolean dropShadow, float scale) {
        Font fontRenderer = Minecraft.getInstance().font;
        double scaledWidth = fontRenderer.width(text) * scale;
        double scaledHeight = fontRenderer.lineHeight * scale;
        drawStringSized(poseStack, text, x - scaledWidth, y - scaledHeight, color, dropShadow, scale, false);
    }

    public static void drawText(PoseStack poseStack, String text, float x, float y, float scale, int color) {
        drawText(poseStack, text, x, y, scale, color, false);
    }

    public static void drawText(PoseStack poseStack, String text, float x, float y, float scale, int color, boolean shadow) {
        Font fontRenderer = Minecraft.getInstance().font;
        RenderSystem.disableBlend();
        poseStack.pushPose();
        poseStack.scale(scale, scale, 0f);
        float sf = 1 / scale;
        fontRenderer.draw(poseStack, text, x * sf, y * sf, color);
        if(shadow) fontRenderer.drawShadow(poseStack, text, 0, 0, color);
        poseStack.popPose();
        RenderSystem.enableBlend();
    }

    public static void drawItemStack(PoseStack poseStack, ItemStack itemStack, int x, int y, @Nullable String altTxt) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 32.0F);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.enableDepthTest();
        Minecraft mc = Minecraft.getInstance();
        ItemRenderer itemRender = mc.getItemRenderer();
        itemRender.renderAndDecorateItem(itemStack, x, y);
        itemRender.renderGuiItemDecorations(mc.font, itemStack, x, y, altTxt);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        poseStack.popPose();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
    }

    public static List<Component> getItemToolTip(ItemStack itemStack) {
        Minecraft mc = Minecraft.getInstance();
        TooltipFlag flag = mc.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
        List<Component> tooltip = itemStack.getTooltipLines(mc.player, flag);
        for (int i = 0; i < tooltip.size(); ++i) {
            if (i == 0) {
                tooltip.set(i, tooltip.get(i).copy().withStyle(itemStack.getItem().getRarity(itemStack).getStyleModifier()));
            } else {
                tooltip.set(i, tooltip.get(i).copy().withStyle(ChatFormatting.GRAY));
            }
        }
        return tooltip;
    }

    public static void drawSelectionOverlay(int x, int y, int width, int height) {
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        drawGradientRect(x, y, width, height, -2130706433, -2130706433);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
    }

    public static void drawSolidRect(PoseStack poseStack, int x, int y, int width, int height, int color) {
        Gui.fill(poseStack, x, y, x + width, y + height, color);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
    }

    public static void drawRectShadow(int x, int y, int width, int height, int distance) {
        drawGradientRect(x + distance, y + height, width - distance, distance, 0x4f000000, 0, false);
        drawGradientRect(x + width, y + distance, distance, height - distance, 0x4f000000, 0, true);

        float startAlpha = (float) (0x4f) / 255.0F;
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        x += width;
        y += height;
        buffer.vertex(x, y, 0).color(0, 0, 0, startAlpha).endVertex();
        buffer.vertex(x, y + distance, 0).color(0, 0, 0, 0).endVertex();
        buffer.vertex(x + distance, y + distance, 0).color(0, 0, 0, 0).endVertex();

        buffer.vertex(x, y, 0).color(0, 0, 0, startAlpha).endVertex();
        buffer.vertex(x + distance, y + distance, 0).color(0, 0, 0, 0).endVertex();
        buffer.vertex(x + distance, y, 0).color(0, 0, 0, 0).endVertex();
        tesselator.end();
        RenderSystem.enableTexture();
    }

    public static void drawGradientRect(int x, int y, int width, int height, int startColor, int endColor) {
        drawGradientRect(x, y, width, height, startColor, endColor, false);
    }

    public static void drawGradientRect(float x, float y, float width, float height, int startColor, int endColor, boolean horizontal) {
        float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;
        float startRed = (float) (startColor >> 16 & 255) / 255.0F;
        float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
        float startBlue = (float) (startColor & 255) / 255.0F;
        float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;
        float endRed = (float) (endColor >> 16 & 255) / 255.0F;
        float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
        float endBlue = (float) (endColor & 255) / 255.0F;
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        if (horizontal) {
            buffer.vertex(x + width, y, 0).color(endRed, endGreen, endBlue, endAlpha).endVertex();
            buffer.vertex(x, y, 0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            buffer.vertex(x, y + height, 0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            buffer.vertex(x + width, y + height, 0).color(endRed, endGreen, endBlue, endAlpha).endVertex();
            tesselator.end();
        } else {
            buffer.vertex(x + width, y, 0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            buffer.vertex(x, y, 0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            buffer.vertex(x, y + height, 0).color(endRed, endGreen, endBlue, endAlpha).endVertex();
            buffer.vertex(x + width, y + height, 0).color(endRed, endGreen, endBlue, endAlpha).endVertex();
            tesselator.end();
        }
        RenderSystem.enableTexture();
    }

    public static void setColor(int color) { // ARGB
        RenderSystem.setShaderColor((color >> 16 & 255) / 255.0F,
                (color >> 8 & 255) / 255.0F,
                (color & 255) / 255.0F,
                (color >> 24 & 255) / 255.0F);
    }

    public static void drawCircle(float x, float y, float r, int color, int segments) {
        if (color == 0) return;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        setColor(color);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        for (int i = 0; i < segments; i++) {
            bufferbuilder.vertex(x + r * Math.cos(-2 * Math.PI * i / segments), y + r * Math.sin(-2 * Math.PI * i / segments), 0.0D).endVertex();
        }
        tesselator.end();
        RenderSystem.enableTexture();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    public static void drawSector(float x, float y, float r, int color, int segments, int from, int to) {
        if (from > to || from < 0 || color == 0) return;
        if (to > segments) to = segments;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        setColor(color);
        bufferbuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION);
        for (int i = from; i < to; i++) {
            bufferbuilder.vertex(x + r * Math.cos(-2 * Math.PI * i / segments), y + r * Math.sin(-2 * Math.PI * i / segments), 0.0D).endVertex();
            bufferbuilder.vertex(x + r * Math.cos(-2 * Math.PI * (i + 1) / segments), y + r * Math.sin(-2 * Math.PI * (i + 1) / segments), 0.0D).endVertex();
            bufferbuilder.vertex(x, y, 0.0D).endVertex();
        }
        tesselator.end();
        RenderSystem.enableTexture();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    public static void drawTorus(float x, float y, float outer, float inner, int color, int segments, int from, int to) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        setColor(color);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        for (int i = from; i <= to; i++) {
            float angle = (i / (float) segments) * 3.14159f * 2.0f;
            bufferbuilder.vertex(x + inner * Math.cos(-angle), y + inner * Math.sin(-angle), 0).endVertex();
            bufferbuilder.vertex(x + outer * Math.cos(-angle), y + outer * Math.sin(-angle), 0).endVertex();
        }
        tesselator.end();
        RenderSystem.enableTexture();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    public static void drawLines(List<Vec2> points, int startColor, int endColor, float width) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(width);
        if (startColor == endColor) {
            setColor(startColor);
            bufferbuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION);
            for (Vec2 point : points) {
                bufferbuilder.vertex(point.x, point.y, 0).endVertex();
            }
        } else {
            float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;
            float startRed = (float) (startColor >> 16 & 255) / 255.0F;
            float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
            float startBlue = (float) (startColor & 255) / 255.0F;
            float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;
            float endRed = (float) (endColor >> 16 & 255) / 255.0F;
            float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
            float endBlue = (float) (endColor & 255) / 255.0F;
            bufferbuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            int size = points.size();

            for (int i = 0; i < size; i++) {
                float p = i * 1.0f / size;
                bufferbuilder.vertex(points.get(i).x, points.get(i).y, 0)
                        .color(startRed + (endRed - startRed) * p,
                                startGreen + (endGreen - startGreen) * p,
                                startBlue + (endBlue - startBlue) * p,
                                startAlpha + (endAlpha - startAlpha) * p)
                        .endVertex();
            }
        }
        tesselator.end();
        RenderSystem.enableTexture();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    public static void drawTextureRect(double x, double y, double width, double height) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(x, y + height, 0.0D).uv(0, 0).endVertex();
        buffer.vertex(x + width, y + height, 0.0D).uv(1, 0).endVertex();
        buffer.vertex(x + width, y, 0.0D).uv(1, 1).endVertex();
        buffer.vertex(x, y, 0.0D).uv(0, 1).endVertex();
        tesselator.end();
    }

    public static List<Vec2> genBezierPoints(Vec2 from, Vec2 to, boolean horizontal, float u) {
        Vec2 c1;
        Vec2 c2;
        if (horizontal) {
            c1 = new Vec2((from.x + to.x) / 2, from.y);
            c2 = new Vec2((from.x + to.x) / 2, to.y);
        } else {
            c1 = new Vec2(from.x, (from.y + to.y) / 2);
            c2 = new Vec2(to.x, (from.y + to.y) / 2);
        }
        Vec2[] controlPoint = new Vec2[]{from, c1, c2, to};
        int n = controlPoint.length - 1;
        int i, r;
        List<Vec2> bezierPoints = new ArrayList<>();
        for (u = 0; u <= 1; u += 0.01) {
            Vec2[] p = new Vec2[n + 1];
            for (i = 0; i <= n; i++) {
                p[i] = new Vec2(controlPoint[i].x, controlPoint[i].y);
            }
            for (r = 1; r <= n; r++) {
                for (i = 0; i <= n - r; i++) {
                    p[i] = new Vec2((1 - u) * p[i].x + u * p[i + 1].x, (1 - u) * p[i].y + u * p[i + 1].y);
                }
            }
            bezierPoints.add(p[0]);
        }
        return bezierPoints;
    }

    protected void playButtonClickSound() {
        Minecraft.getInstance().getSoundManager().play(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    protected boolean isShiftDown() {
        return Screen.hasShiftDown();
    }

    protected boolean isCtrlDown() {
        return Screen.hasControlDown();
    }

    public boolean isClientSide() {
        return gui.holder.isClientSide();
    }

    protected static boolean isClientSide() {
        return FMLLoader.getDist().isClient();
    }

    public static final class ClickData {
        public final int button;
        public final boolean isShiftClick;
        public final boolean isCtrlClick;
        public final boolean isClient;

        public ClickData(int button, boolean isShiftClick, boolean isCtrlClick) {
            this.button = button;
            this.isShiftClick = isShiftClick;
            this.isCtrlClick = isCtrlClick;
            this.isClient = false;
        }

        public ClickData(int button, boolean isShiftClick, boolean isCtrlClick, boolean isClient) {
            this.button = button;
            this.isShiftClick = isShiftClick;
            this.isCtrlClick = isCtrlClick;
            this.isClient = isClient;
        }

        public void writeToBuf(FriendlyByteBuf buf) {
            buf.writeVarInt(button);
            buf.writeBoolean(isShiftClick);
            buf.writeBoolean(isCtrlClick);
            buf.writeBoolean(isClient);
        }

        public static ClickData readFromBuf(FriendlyByteBuf buf) {
            int button = buf.readVarInt();
            boolean shiftClick = buf.readBoolean();
            boolean ctrlClick = buf.readBoolean();
            boolean isClient = buf.readBoolean();
            return new ClickData(button, shiftClick, ctrlClick, isClient);
        }
    }

    public static final class WheelData {
        public final int wheelDelta;
        public final boolean isShiftClick;
        public final boolean isCtrlClick;
        public final boolean isClient;

        public WheelData(int wheelDelta, boolean isShiftClick, boolean isCtrlClick) {
            this.wheelDelta = wheelDelta;
            this.isShiftClick = isShiftClick;
            this.isCtrlClick = isCtrlClick;
            this.isClient = false;
        }

        public WheelData(int wheelDelta, boolean isShiftClick, boolean isCtrlClick, boolean isClient) {
            this.wheelDelta = wheelDelta;
            this.isShiftClick = isShiftClick;
            this.isCtrlClick = isCtrlClick;
            this.isClient = isClient;
        }

        public void writeToBuf(FriendlyByteBuf buf) {
            buf.writeVarInt(wheelDelta);
            buf.writeBoolean(isShiftClick);
            buf.writeBoolean(isCtrlClick);
            buf.writeBoolean(isClient);
        }

        public static WheelData readFromBuf(FriendlyByteBuf buf) {
            int button = buf.readVarInt();
            boolean shiftClick = buf.readBoolean();
            boolean ctrlClick = buf.readBoolean();
            boolean isClient = buf.readBoolean();
            return new WheelData(button, shiftClick, ctrlClick, isClient);
        }
    }

}