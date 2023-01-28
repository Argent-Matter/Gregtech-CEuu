package net.nemezanevem.gregtech.api.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.netty.util.CharsetUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.nemezanevem.gregtech.api.gui.IRenderContext;
import net.nemezanevem.gregtech.api.gui.Widget;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * @author brachy84
 */
public class TextFieldWidget2 extends Widget {

    // all positive whole numbers
    public static final Pattern NATURAL_NUMS = Pattern.compile("[0-9]*");
    // all positive and negative numbers
    public static final Pattern WHOLE_NUMS = Pattern.compile("-?[0-9]*");
    public static final Pattern DECIMALS = Pattern.compile("[0-9]*(\\.[0-9]*)?");
    public static final Pattern LETTERS = Pattern.compile("[a-zA-Z]*");

    private String text;
    private Component localisedPostFix;
    private final Supplier<String> supplier;
    private final Consumer<String> setter;
    private Consumer<TextFieldWidget2> onFocus;
    private Pattern regex;
    private Function<String, String> validator = s -> s;
    private boolean initialised = false;
    private boolean centered;
    private int textColor = 0xFFFFFF;
    private int markedColor = 0x2F72A8;
    private boolean postFixRight = false;
    private int maxLength = 32;
    private float scale = 1;

    private boolean focused;
    private int cursorPos;
    private int cursorPos2;

    private int clickTime = 20;
    private int cursorTime = 0;
    private boolean drawCursor = true;

    public TextFieldWidget2(int x, int y, int width, int height, Supplier<String> supplier, Consumer<String> setter) {
        super(x, y, width, height);
        this.supplier = supplier;
        this.setter = setter;
        this.text = supplier.get();
    }

    @Override
    public void containerTick() {
        clickTime++;
        if (++cursorTime == 10) {
            cursorTime = 0;
            drawCursor = !drawCursor;
        }
    }

    @Override
    public void detectAndSendChanges() {
        String t = supplier.get();
        if (!initialised || (!focused && !text.equals(t))) {
            text = t;
            writeUpdateInfo(-2, buf -> buf.writeCharSequence(text, CharsetUtil.US_ASCII));
            initialised = true;
        }
    }

    private int getTextX() {
        if (centered) {
            Font fontRenderer = Minecraft.getInstance().font;
            int w = getSize().width;
            float textW = fontRenderer.width(text) * scale;
            if (localisedPostFix != null)
                textW += 3 + fontRenderer.width(localisedPostFix) * scale;
            return (int) (w / 2f - textW / 2f + getPosition().x);
        }
        return getPosition().x + 1;
    }

    @Override
    public void drawInBackground(PoseStack poseStack, int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(poseStack, mouseX, mouseY, partialTicks, context);
        Font fontRenderer = Minecraft.getInstance().font;
        int y = getPosition().y;
        int textX = getTextX();
        RenderSystem.disableBlend();
        poseStack.pushPose();
        poseStack.scale(scale, scale, 0);
        float scaleFactor = 1 / scale;
        y *= scaleFactor;
        if (cursorPos != cursorPos2) {
            // render marked text background
            float startX = fontRenderer.width(text.substring(0, Math.min(cursorPos, cursorPos2))) * scale + textX;
            String marked = getSelectedText();
            float width = fontRenderer.width(marked);
            drawSelectionBox(startX * scaleFactor, y, width);
        }
        fontRenderer.draw(poseStack, text, (int) (textX * scaleFactor), y, textColor);
        if (localisedPostFix != null) {
            // render postfix
            int x = postFixRight && !centered ?
                    getPosition().x + getSize().width - (fontRenderer.width(localisedPostFix) + 1) :
                    textX + fontRenderer.width(text) + 3;
            x *= scaleFactor;
            fontRenderer.draw(poseStack, localisedPostFix, x, y, textColor);
        }
        if (focused && drawCursor) {
            // render cursor
            String sub = text.substring(0, cursorPos);
            float x = fontRenderer.width(sub) * scale + textX;
            x *= scaleFactor;
            drawCursor(x, y);
        }
        poseStack.popPose();
        RenderSystem.enableBlend();
    }

    private void drawCursor(float x, float y) {
        x -= 0.9f;
        y -= 1;
        float endX = x + 0.5f * (1 / scale);
        float endY = y + 9;
        float red = (float) (textColor >> 16 & 255) / 255.0F;
        float green = (float) (textColor >> 8 & 255) / 255.0F;
        float blue = (float) (textColor & 255) / 255.0F;
        float alpha = (float) (textColor >> 24 & 255) / 255.0F;
        if (alpha == 0)
            alpha = 1f;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.setShaderColor(red, green, blue, alpha);
        RenderSystem.disableTexture();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(x, endY, 0.0D).endVertex();
        bufferbuilder.vertex(endX, endY, 0.0D).endVertex();
        bufferbuilder.vertex(endX, y, 0.0D).endVertex();
        bufferbuilder.vertex(x, y, 0.0D).endVertex();
        tesselator.end();
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
    }

    private void drawSelectionBox(float x, float y, float width) {
        float endX = x + width;
        y -= 1;
        float endY = y + Minecraft.getInstance().font.lineHeight;
        float red = (float) (markedColor >> 16 & 255) / 255.0F;
        float green = (float) (markedColor >> 8 & 255) / 255.0F;
        float blue = (float) (markedColor & 255) / 255.0F;
        float alpha = (float) (markedColor >> 24 & 255) / 255.0F;
        if (alpha == 0)
            alpha = 1f;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.setShaderColor(red, green, blue, alpha);
        RenderSystem.disableTexture();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(x, endY, 0.0D).endVertex();
        bufferbuilder.vertex(endX, endY, 0.0D).endVertex();
        bufferbuilder.vertex(endX, y, 0.0D).endVertex();
        bufferbuilder.vertex(x, y, 0.0D).endVertex();
        tesselator.end();
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {

            focused = true;
            if (onFocus != null) {
                onFocus.accept(this);
            }
            if (clickTime < 5) {
                cursorPos = text.length();
                cursorPos2 = 0;
            } else {
                cursorPos = getCursorPosFromMouse(mouseX);
                cursorPos2 = cursorPos;
            }
            clickTime = 0;
        } else
            unFocus();
        return focused;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (focused && button == 0) {
            if (mouseX < getPosition().x) {
                cursorPos = 0;
                return true;
            }
            cursorPos = getCursorPosFromMouse(mouseX);
        }
        return focused;
    }

    private int getCursorPosFromMouse(int mouseX) {
        int base = mouseX - getTextX();
        float x = 1;
        int i = 0;
        while (x < base) {
            if (i == text.length())
                break;
            x += (Minecraft.getInstance().font.width(String.valueOf(text.charAt(i)))) * scale;
            i++;
        }
        return i;
    }

    public String getSelectedText() {
        return text.substring(Math.min(cursorPos, cursorPos2), Math.max(cursorPos, cursorPos2));
    }

    @Override
    public boolean keyTyped(char charTyped, int keyCode) {
        if (focused) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                unFocus();
                return false;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                unFocus();
                return true;
            }
            if (Screen.hasControlDown() && keyCode == GLFW.GLFW_KEY_A) {
                cursorPos = text.length();
                cursorPos2 = 0;
                return true;
            }
            if (Screen.hasControlDown() && keyCode == GLFW.GLFW_KEY_C) {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getSelectedText());
                return true;
            } else if (Screen.hasControlDown() && keyCode == GLFW.GLFW_KEY_V) {
                String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
                if (text.length() + clip.length() > maxLength || !isAllowed(clip))
                    return true;
                replaceMarkedText(clip);
                return true;
            } else if (Screen.hasControlDown() && keyCode == GLFW.GLFW_KEY_X) {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getSelectedText());
                replaceMarkedText(null);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_LEFT && cursorPos > 0) {
                int amount = 1;
                int pos = cursorPos;
                if (isCtrlDown()) {
                    for (int i = pos - 1; i >= 0; i--) {
                        if (i == 0 || text.charAt(i) == ' ') {
                            amount = pos - i;
                            break;
                        }
                    }
                }
                cursorPos -= amount;
                if (cursorPos < 0)
                    cursorPos = 0;
                if (!isShiftDown()) {
                    cursorPos2 = cursorPos;
                }
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_RIGHT && cursorPos < text.length()) {
                int amount = 1;
                int pos = cursorPos;
                if (isCtrlDown()) {
                    for (int i = pos + 1; i < text.length(); i++) {
                        if (i == text.length() - 1 || text.charAt(i) == ' ') {
                            amount = i - pos;
                            break;
                        }
                    }
                }
                cursorPos += amount;
                if (cursorPos > text.length())
                    cursorPos = text.length();
                if (!isShiftDown()) {
                    cursorPos2 = cursorPos;
                }
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && text.length() > 0) {
                if (cursorPos != cursorPos2) {
                    replaceMarkedText(null);
                } else if (cursorPos > 0) {
                    String t1 = text.substring(0, cursorPos - 1);
                    String t2 = text.substring(cursorPos);
                    text = t1 + t2;
                    cursorPos--;
                    cursorPos2 = cursorPos;
                }
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_DELETE && text.length() > 0) {
                if (cursorPos != cursorPos2) {
                    replaceMarkedText(null);
                } else if (cursorPos < text.length()) {
                    String t1 = text.substring(0, cursorPos);
                    String t2 = text.substring(cursorPos + 1);
                    text = t1 + t2;
                    cursorPos2 = cursorPos;
                }
            }
            if (charTyped != Character.MIN_VALUE && text.length() < maxLength) {
                int min = Math.min(cursorPos, cursorPos2);
                int max = Math.max(cursorPos, cursorPos2);
                String t1 = text.substring(0, min);
                String t2 = text.substring(max);
                t1 += charTyped;
                if (isAllowed(t1 + t2)) {
                    text = t1 + t2;
                    cursorPos = t1.length();
                    cursorPos2 = cursorPos;
                    return true;
                }
            }
        }
        return focused;
    }

    private boolean isAllowed(String t) {
        return regex == null || regex.matcher(t).matches();
    }

    private void replaceMarkedText(String replacement) {
        int min = Math.min(cursorPos, cursorPos2);
        int max = Math.max(cursorPos, cursorPos2);
        String t1 = text.substring(0, min);
        String t2 = text.substring(max);
        if (replacement != null) {
            if (t1.length() + t2.length() + replacement.length() > maxLength)
                return;
        }
        if (replacement == null) {
            text = t1 + t2;
            cursorPos = min;
        } else {
            text = t1 + replacement + t2;
            cursorPos = t1.length() + replacement.length();
        }
        cursorPos2 = cursorPos;
    }

    public String getText() {
        return text;
    }

    public void unFocus() {
        if (!focused) return;
        cursorPos2 = cursorPos;
        text = validator.apply(text);
        setter.accept(text);
        focused = false;
        writeClientAction(-1, buf -> {
            buf.writeUtf(text);
        });
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        if (id == -1) {
            text = buffer.readUtf(maxLength);
            setter.accept(text);
        }
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == -2) {
            text = buffer.readUtf(maxLength);
            setter.accept(text);
            initialised = true;
            if (cursorPos > text.length()) {
                cursorPos = text.length();
            }
            if (cursorPos2 > text.length()) {
                cursorPos2 = text.length();
            }
        }
    }

    /**
     * @param textColor text color. Default is 0xFFFFFF (white)
     */
    public TextFieldWidget2 setTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    /**
     * If a key is pressed, the new string will be matched against this pattern.
     * If it doesn't match, the char will not be typed.
     *
     * @param regex pattern
     */
    public TextFieldWidget2 setAllowedChars(Pattern regex) {
        this.regex = regex;
        return this;
    }

    /**
     * Called after unfocusing (press enter or click anywhere, but the field) the field
     *
     * @param validator determines whether the entered string is valid. Returns true by default
     */
    public TextFieldWidget2 setValidator(Function<String, String> validator) {
        this.validator = validator;
        return this;
    }

    /**
     * A predefined validator to only accept integer numbers
     *
     * @param min minimum accepted value
     * @param max maximum accepted value
     */
    public TextFieldWidget2 setNumbersOnly(int min, int max) {
        if (this.regex == null) {
            if (min < 0)
                regex = WHOLE_NUMS;
            else
                regex = NATURAL_NUMS;
        }
        setValidator(val -> {
            if (val.isEmpty()) {
                return String.valueOf(min);
            }
            for (int i = 0; i < val.length(); i++) {
                char c = val.charAt(i);
                if (c == '-' && (min >= 0 || i != 0)) {
                    return String.valueOf(min);
                }

            }
            int num;
            try {
                num = Integer.parseInt(val);
            } catch (NumberFormatException ignored) {
                return String.valueOf(max);
            }
            if (num < min) {
                return String.valueOf(min);
            }
            if (num > max) {
                return String.valueOf(max);
            }
            return val;
        });
        return this;
    }

    /**
     * @param centered whether to center the text and post fix in the x axis. Default is false
     */
    public TextFieldWidget2 setCentered(boolean centered) {
        this.centered = centered;
        return this;
    }

    /**
     * @param postFix a string that will be rendered after the editable text
     */
    public TextFieldWidget2 setPostFix(Component postFix) {
        this.localisedPostFix = postFix;
        return this;
    }

    /**
     * @param markedColor background color of marked text. Default is 0x2F72A8 (lapis lazuli blue)
     */
    public TextFieldWidget2 setMarkedColor(int markedColor) {
        this.markedColor = markedColor;
        return this;
    }

    /**
     * @param postFixRight whether to bind the post fix to the right side. Default is false
     */
    public TextFieldWidget2 bindPostFixToRight(boolean postFixRight) {
        this.postFixRight = postFixRight;
        return this;
    }

    /**
     * Will scale the text, the marked background and the cursor. f.e. 0.5 is half the size
     *
     * @param scale scale factor
     */
    public TextFieldWidget2 setScale(float scale) {
        this.scale = scale;
        return this;
    }

    public TextFieldWidget2 setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    /**
     * Called when the text field gets focused. Only called on client.
     * Use it to un focus other text fields.
     * Optimally this should be done automatically, but that is not really possible with the way Modular UI is made
     */
    public TextFieldWidget2 setOnFocus(Consumer<TextFieldWidget2> onFocus) {
        this.onFocus = onFocus;
        return this;
    }
}
