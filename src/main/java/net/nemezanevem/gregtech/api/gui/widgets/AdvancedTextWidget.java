package net.nemezanevem.gregtech.api.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.nemezanevem.gregtech.api.gui.IRenderContext;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.util.Position;
import net.nemezanevem.gregtech.api.util.Size;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
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
            wrapScreen = new WrapScreen();

        return wrapScreen;
    }

    private void resizeWrapScreen() {
        if (sizes != null) {
            getWrapScreen().setWorldAndResolution(Minecraft.getInstance(), sizes.getScreenWidth(), sizes.getScreenHeight());
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
                    buffer.writeString(Component.Serializer.componentToJson(textComponent));
                }
            });
        }
    }

    protected Component getTextUnderMouse(int mouseX, int mouseY) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        Position position = getPosition();
        int selectedLine = (mouseY - position.y) / (fontRenderer.FONT_HEIGHT + 2);
        if (mouseX >= position.x && selectedLine >= 0 && selectedLine < displayText.size()) {
            Component selectedComponent = displayText.get(selectedLine);
            int mouseOffset = mouseX - position.x;
            int currentOffset = 0;
            for (Component lineComponent : selectedComponent) {
                currentOffset += fontRenderer.getStringWidth(lineComponent.getUnformattedComponentText());
                if (currentOffset >= mouseOffset) {
                    return lineComponent;
                }
            }
        }
        return null;
    }

    private void updateComponentTextSize() {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int maxStringWidth = 0;
        int totalHeight = 0;
        for (Component textComponent : displayText) {
            maxStringWidth = Math.max(maxStringWidth, fontRenderer.getStringWidth(textComponent.getFormattedText()));
            totalHeight += fontRenderer.FONT_HEIGHT + 2;
        }
        totalHeight -= 2;
        setSize(new Size(maxStringWidth, totalHeight));
        if (uiAccess != null) {
            uiAccess.notifySizeChange();
        }
    }

    private void formatDisplayText() {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int maxTextWidthResult = maxWidthLimit == 0 ? Integer.MAX_VALUE : maxWidthLimit;
        this.displayText = displayText.stream()
                .flatMap(c -> GuiUtilRenderComponents.splitText(c, maxTextWidthResult, fontRenderer, true, true).stream())
                .collect(Collectors.toList());
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 1) {
            this.displayText.clear();
            int count = buffer.readVarInt();
            for (int i = 0; i < count; i++) {
                String jsonText = buffer.readString(32767);
                this.displayText.add(Component.Serializer.jsonToComponent(jsonText));
            }
            formatDisplayText();
            updateComponentTextSize();
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            ClickData clickData = ClickData.readFromBuf(buffer);
            String componentData = buffer.readString(128);
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
            if (clickEvent.getAction() == Action.OPEN_URL && componentText.startsWith("@!")) {
                String rawText = componentText.substring(2);
                ClickData clickData = new ClickData(Mouse.getEventButton(), isShiftDown(), isCtrlDown());
                writeClientAction(1, buf -> {
                    clickData.writeToBuf(buf);
                    buf.writeString(rawText);
                });
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
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
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
        Font fontRenderer = Minecraft.getInstance().font;
        Position position = getPosition();
        for (int i = 0; i < displayText.size(); i++) {
            fontRenderer.drawShadow(displayText.get(i).getFormattedText(), position.x, position.y + i * (fontRenderer.FONT_HEIGHT + 2), color);
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        super.drawInForeground(mouseX, mouseY);
        Component component = getTextUnderMouse(mouseX, mouseY);
        if (component != null) {
            getWrapScreen().handleComponentHover(component, mouseX, mouseY);
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
            GuiUtils.drawHoveringText(textLines, x, y, width, height, 256, font);
        }
    }
}
