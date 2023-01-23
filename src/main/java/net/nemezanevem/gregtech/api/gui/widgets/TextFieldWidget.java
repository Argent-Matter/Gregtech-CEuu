package net.nemezanevem.gregtech.api.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.nemezanevem.gregtech.api.gui.IRenderContext;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.gui.resources.IGuiTexture;
import net.nemezanevem.gregtech.api.util.Position;
import net.nemezanevem.gregtech.api.util.Size;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class TextFieldWidget extends Widget {

    protected EditBox textField;

    protected int maxStringLength = 32;
    protected Predicate<String> textValidator;
    protected Supplier<String> textSupplier;
    protected Consumer<String> textResponder;
    protected String currentString;
    private IGuiTexture background;
    private boolean enableBackground;
    private boolean isClient;

    public TextFieldWidget(int xPosition, int yPosition, int width, int height, boolean enableBackground, Supplier<String> textSupplier, Consumer<String> textResponder) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        if (isClientSide()) {
            this.enableBackground = enableBackground;
            Font fontRenderer = Minecraft.getInstance().font;
            if (enableBackground) {
                this.textField = new EditBox(fontRenderer, xPosition, yPosition, width, height, CommonComponents.EMPTY);
            } else {
                this.textField = new EditBox(fontRenderer, xPosition + 1, yPosition + (height - fontRenderer.lineHeight) / 2 + 1, width - 2, height, CommonComponents.EMPTY);
            }
            this.textField.setCanLoseFocus(true);
            this.textField.setBordered(enableBackground);
            this.textField.setMaxLength(this.maxStringLength);
            this.textField.setResponder(this::onTextChanged);
        }
        this.textSupplier = textSupplier;
        this.textResponder = textResponder;
    }

    public TextFieldWidget(int xPosition, int yPosition, int width, int height, boolean enableBackground, Supplier<String> textSupplier, Consumer<String> textResponder, int maxStringLength) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        if (isClientSide()) {
            Font fontRenderer = Minecraft.getInstance().font;
            if (enableBackground) {
                this.textField = new EditBox(fontRenderer, xPosition, yPosition, width, height, CommonComponents.EMPTY);
            } else {
                this.textField = new EditBox(fontRenderer, xPosition + 1, yPosition + (height - fontRenderer.lineHeight) / 2 + 1, width - 2, height, CommonComponents.EMPTY);
            }
            this.textField.setCanLoseFocus(true);
            this.textField.setBordered(enableBackground);
            this.textField.setMaxLength(maxStringLength);
            this.maxStringLength = maxStringLength;
            this.textField.setResponder(this::onTextChanged);
        }
        this.textSupplier = textSupplier;
        this.textResponder = textResponder;
    }

    public TextFieldWidget(int xPosition, int yPosition, int width, int height, IGuiTexture background, Supplier<String> textSupplier, Consumer<String> textResponder) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        if (isClientSide()) {
            this.enableBackground = false;
            Font fontRenderer = Minecraft.getInstance().font;
            this.textField = new EditBox(fontRenderer, xPosition + 1, yPosition + (height - fontRenderer.lineHeight) / 2 + 1, width - 2, height, CommonComponents.EMPTY);
            this.textField.setCanLoseFocus(true);
            this.textField.setBordered(false);
            this.textField.setMaxLength(maxStringLength);
            this.textField.setResponder(this::onTextChanged);
        }
        this.background = background;
        this.textSupplier = textSupplier;
        this.textResponder = textResponder;
    }

    public TextFieldWidget doesClientCallback(boolean isClient) {
        this.isClient = isClient;
        return this;
    }

    public TextFieldWidget setTextSupplier(Supplier<String> textSupplier, boolean isClient) {
        this.isClient = isClient;
        this.textSupplier = textSupplier;
        return this;
    }

    public TextFieldWidget setTextResponder(Consumer<String> textResponder, boolean isClient) {
        this.isClient = isClient;
        this.textResponder = textResponder;
        return this;
    }

    public TextFieldWidget setCurrentString(String currentString) {
        this.currentString = currentString;
        this.textField.setValue(currentString);
        return this;
    }

    public String getCurrentString() {
        if (isRemote()) {
            return this.textField.getValue();
        }
        return this.currentString;
    }


    @Override
    protected void onPositionUpdate() {
        if (isClientSide() && textField != null) {
            Font fontRenderer = Minecraft.getInstance().font;
            Position position = getPosition();
            Size size = getSize();
            EditBox textField = this.textField;
            textField.x = enableBackground ? position.x : position.x + 1;
            textField.y = enableBackground ? position.y : position.y + (size.height - fontRenderer.lineHeight) / 2 + 1;
        }
    }

    @Override
    protected void onSizeUpdate() {
        if (isClientSide() && textField != null) {
            Font fontRenderer = Minecraft.getInstance().font;
            Position position = getPosition();
            Size size = getSize();
            EditBox textField = this.textField;
            textField.setWidth(enableBackground ? size.width : size.width - 2);
            textField.setHeight(size.height);
            textField.y = enableBackground ? position.y : position.y + (getSize().height - fontRenderer.lineHeight) / 2 + 1;

        }
    }

    @Override
    public void drawInBackground(PoseStack poseStack, int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(poseStack, mouseX, mouseY, partialTicks, context);
        if (background != null) {
            Position position = getPosition();
            Size size = getSize();
            background.draw(position.x, position.y, size.width, size.height);
        }
        this.textField.render(poseStack, mouseX, mouseY, partialTicks);
        RenderSystem.enableBlend();
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return this.textField.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyTyped(char charTyped, int modifiers) {
        return this.textField.charTyped(charTyped, modifiers);
    }

    @Override
    public void updateScreen() {
        if (textSupplier != null && isClient) {
            this.currentString = textSupplier.get();
            this.textField.setValue(currentString);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (textSupplier != null && !textSupplier.get().equals(currentString)) {
            this.currentString = textSupplier.get();
            writeUpdateInfo(1, buffer -> buffer.writeUtf(currentString));
        }
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 1) {
            this.currentString = buffer.readUtf(Short.MAX_VALUE);
            this.textField.setValue(currentString);
        }
    }

    protected void onTextChanged(String newTextString) {
        if (textValidator.test(newTextString)) {
            if (isClient && textResponder != null) {
                textResponder.accept(newTextString);
            }
            writeClientAction(1, buffer -> buffer.writeUtf(newTextString));
        }
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            String clientText = buffer.readUtf(Short.MAX_VALUE);
            clientText = clientText.substring(0, Math.min(clientText.length(), maxStringLength));
            if (textValidator.test(clientText)) {
                this.currentString = clientText;
                if (textResponder != null) {
                    this.textResponder.accept(clientText);
                }
            }
        }
    }

    public TextFieldWidget setTextColor(int textColor) {
        if (isClientSide()) {
            this.textField.setTextColor(textColor);
        }
        return this;
    }

    public TextFieldWidget setMaxStringLength(int maxStringLength) {
        this.maxStringLength = maxStringLength;
        if (isClientSide()) {
            this.textField.setMaxLength(maxStringLength);
        }
        return this;
    }

    public TextFieldWidget setValidator(Predicate<String> validator) {
        this.textValidator = validator;
        if (isClientSide()) {
            this.textField.setFilter(validator);
        }
        return this;
    }
}
