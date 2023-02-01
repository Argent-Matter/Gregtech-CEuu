package net.nemezanevem.gregtech.api.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.nemezanevem.gregtech.api.gui.IRenderContext;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.util.Position;
import net.nemezanevem.gregtech.api.util.Size;
import net.nemezanevem.gregtech.client.util.MouseButtonHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Represents a text-component based widget, which obtains
 * text from server and automatically synchronizes it with clients
 */
public class AdvancedTextWidget extends Widget {
    protected int maxWidthLimit;

    private WrapScreen wrapScreen;

    protected final Consumer<List<Component>> textSupplier;
    protected BiConsumer<String, ClickData> clickHandler;
    private List<Component> displayText = new ArrayList<>();
    private final int color;

    public AdvancedTextWidget(int xPosition, int yPosition, Consumer<List<Component>> text, int color) {
        super(new Position(xPosition, yPosition), Size.ZERO);
        this.textSupplier = text;
        this.color = color;
    }

    public static Component withButton(Component textComponent, String componentData) {
        Style style = textComponent.getStyle();
        style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "@!" + componentData));
        if(style.getColor() == null) {
            style.withColor(ChatFormatting.YELLOW);
        }
        return textComponent;
    }

    public static Component withHoverTextTranslate(Component textComponent, String hoverTranslation) {
        Style style = textComponent.getStyle();
        Component translation = Component.translatable(hoverTranslation);
        translation.getStyle().withColor(ChatFormatting.GRAY);
        style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, translation));
        return textComponent;
    }

    public AdvancedTextWidget setMaxWidthLimit(int maxWidthLimit) {
        this.maxWidthLimit = maxWidthLimit;
        if (isClientSide()) {
            updateComponentTextSize();
        }
        return this;
    }

    public AdvancedTextWidget setClickHandler(BiConsumer<String, ClickData> clickHandler) {
        this.clickHandler = clickHandler;
        return this;
    }

    private WrapScreen getWrapScreen() {
        if (wrapScreen == null)
            wrapScreen = new WrapScreen(Component.empty());

        return wrapScreen;
    }

    private void resizeWrapScreen() {
        if (sizes != null) {
            getWrapScreen().resize(Minecraft.getInstance(), sizes.getScreenWidth(), sizes.getScreenHeight());
        }
    }

    @Override
    public void initWidget() {
        super.initWidget();
        if (isClientSide()) {
            resizeWrapScreen();
        }
    }

    @Override
    protected void onPositionUpdate() {
        super.onPositionUpdate();
        if (isClientSide()) {
            resizeWrapScreen();
        }
    }

    @Override
    public void detectAndSendChanges() {
        ArrayList<Component> textBuffer = new ArrayList<>();
        textSupplier.accept(textBuffer);
        if (!displayText.equals(textBuffer)) {
            this.displayText = textBuffer;
            writeUpdateInfo(1, buffer -> {
                buffer.writeVarInt(displayText.size());
                for (Component textComponent : displayText) {
                    buffer.writeUtf(Component.Serializer.toJson(textComponent));
                }
            });
        }
    }

    protected Component getTextUnderMouse(double mouseX, double mouseY) {
        Font fontRenderer = Minecraft.getInstance().font;
        Position position = getPosition();
        int selectedLine = ((int) mouseY - position.y) / (fontRenderer.lineHeight + 2);
        if (mouseX >= position.x && selectedLine >= 0 && selectedLine < displayText.size()) {
            Component selectedComponent = displayText.get(selectedLine);
            double mouseOffset = mouseX - position.x;
            int currentOffset = 0;
            for (Component lineComponent : selectedComponent.getSiblings()) {
                currentOffset += fontRenderer.width(lineComponent.getVisualOrderText());
                if (currentOffset >= mouseOffset) {
                    return lineComponent;
                }
            }
        }
        return null;
    }

    private void updateComponentTextSize() {
        Font fontRenderer = Minecraft.getInstance().font;
        int maxStringWidth = 0;
        int totalHeight = 0;
        for (Component textComponent : displayText) {
            maxStringWidth = Math.max(maxStringWidth, fontRenderer.width(textComponent.getVisualOrderText()));
            totalHeight += fontRenderer.lineHeight + 2;
        }
        totalHeight -= 2;
        setSize(new Size(maxStringWidth, totalHeight));
        if (uiAccess != null) {
            uiAccess.notifySizeChange();
        }
    }

    private void formatDisplayText() {
        Font fontRenderer = Minecraft.getInstance().font;
        int maxTextWidthResult = maxWidthLimit == 0 ? Integer.MAX_VALUE : maxWidthLimit;
        this.displayText = displayText.stream()
                .flatMap(c -> GuiUtilRenderComponents.splitText(c, maxTextWidthResult, fontRenderer, true, true).stream())
                .collect(Collectors.toList());
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 1) {
            this.displayText.clear();
            int count = buffer.readVarInt();
            for (int i = 0; i < count; i++) {
                String jsonText = buffer.readUtf();
                this.displayText.add(Component.Serializer.fromJsonLenient(jsonText));
            }
            formatDisplayText();
            updateComponentTextSize();
        }
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            ClickData clickData = ClickData.readFromBuf(buffer);
            String componentData = buffer.readUtf(128);
            if (clickHandler != null) {
                clickHandler.accept(componentData, clickData);
            }
        }
    }

    private boolean handleCustomComponentClick(Component textComponent) {
        Style style = textComponent.getStyle();
        if (style.getClickEvent() != null) {
            ClickEvent clickEvent = style.getClickEvent();
            String componentText = clickEvent.getValue();
            if (clickEvent.getAction() == ClickEvent.Action.OPEN_URL && componentText.startsWith("@!")) {
                String rawText = componentText.substring(2);
                ClickData clickData = new ClickData(MouseButtonHelper.button, isShiftDown(), isCtrlDown());
                writeClientAction(1, buf -> {
                    clickData.writeToBuf(buf);
                    buf.writeUtf(rawText);
                });
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Component textComponent = getTextUnderMouse(mouseX, mouseY);
        if (textComponent != null) {
            if (handleCustomComponentClick(textComponent) ||
                    getWrapScreen().handleComponentClicked(textComponent.getStyle())) {
                playButtonClickSound();
                return true;
            }
        }
        return false;
    }

    @Override
    public void drawInBackground(PoseStack poseStack, int mouseY, int mouseX, float partialTicks, IRenderContext context) {
        super.drawInBackground(poseStack, mouseY, mouseX, partialTicks, context);
        Font fontRenderer = Minecraft.getInstance().font;
        Position position = getPosition();
        for (int i = 0; i < displayText.size(); i++) {
            fontRenderer.drawShadow(poseStack, displayText.get(i).getVisualOrderText(), position.x, position.y + i * (fontRenderer.lineHeight + 2), color);
        }
    }

    @Override
    public void drawInForeground(PoseStack poseStack, int mouseX, int mouseY) {
        super.drawInForeground(poseStack, mouseX, mouseY);
        Component component = getTextUnderMouse(mouseX, mouseY);
        if (component != null) {
            getWrapScreen().renderComponentHoverEffect(poseStack, component.getStyle(), (int) mouseX, (int) mouseY);
        }
    }

    /**
     * Used to call mc-related chat component handling code,
     * for example component hover rendering and default click handling
     */
    private static class WrapScreen extends Screen {
        protected WrapScreen(Component pTitle) {
            super(pTitle);
        }

        @Override
        public void renderComponentHoverEffect(PoseStack poseStack, @Nullable Style style, int x, int y) {
            super.renderComponentHoverEffect(poseStack, style, x, y);
        }

        @Override
        public boolean handleComponentClicked(@Nullable Style pStyle) {
            return super.handleComponentClicked(pStyle);
        }

        @Override
        public void renderComponentTooltip(PoseStack pPoseStack, @Nonnull List<Component> textLines, int x, int y) {
            minecraft.screen.renderTooltip(pPoseStack, Collections.singletonList(Component.translatable("gui.widget.recipeProgressWidget.default_tooltip")), Optional.empty(), x, y, font);
            //super.renderComponentTooltip(pPoseStack, textLines, x, y);
        }
    }
}
