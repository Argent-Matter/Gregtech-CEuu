package net.nemezanevem.gregtech.api.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.gui.IRenderContext;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.gui.resources.SizedTextureArea;
import net.nemezanevem.gregtech.api.gui.resources.TextureArea;
import net.nemezanevem.gregtech.api.util.Position;
import net.nemezanevem.gregtech.api.util.Size;
import net.nemezanevem.gregtech.api.util.Util;

import java.util.List;
import java.util.function.*;

public class CycleButtonWidget extends Widget {

    protected TextureArea buttonTexture = GuiTextures.VANILLA_BUTTON.getSubArea(0.0, 0.0, 1.0, 0.5);
    private final String[] optionNames;
    private int textColor = 0xFFFFFF;
    private final IntSupplier currentOptionSupplier;
    private final IntConsumer setOptionExecutor;
    private final int RIGHT_MOUSE = 1;
    protected int currentOption;
    protected String tooltipHoverString;

    public CycleButtonWidget(int xPosition, int yPosition, int width, int height, String[] optionNames, IntSupplier currentOptionSupplier, IntConsumer setOptionExecutor) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        this.optionNames = optionNames;
        this.currentOptionSupplier = currentOptionSupplier;
        this.setOptionExecutor = setOptionExecutor;
    }

    public <T extends Enum<T> & StringRepresentable> CycleButtonWidget(int xPosition, int yPosition, int width, int height, Class<T> enumClass, Supplier<T> supplier, Consumer<T> updater) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        T[] enumConstantPool = enumClass.getEnumConstants();
        //noinspection RedundantCast
        this.optionNames = Util.mapToString(enumConstantPool, it -> ((StringRepresentable) it).getSerializedName());
        this.currentOptionSupplier = () -> supplier.get().ordinal();
        this.setOptionExecutor = (newIndex) -> updater.accept(enumConstantPool[newIndex]);
    }

    public CycleButtonWidget(int xPosition, int yPosition, int width, int height, BooleanSupplier supplier, BooleanConsumer updater, String... optionNames) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        this.optionNames = optionNames;
        this.currentOptionSupplier = () -> supplier.getAsBoolean() ? 1 : 0;
        this.setOptionExecutor = (value) -> updater.accept(value >= 1);
    }

    public CycleButtonWidget setTooltipHoverString(String hoverString) {
        this.tooltipHoverString = hoverString;
        return this;
    }

    public CycleButtonWidget setButtonTexture(TextureArea texture) {
        this.buttonTexture = texture;
        return this;
    }

    @SuppressWarnings("unused")
    public CycleButtonWidget setTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    @Override
    public void drawInBackground(PoseStack poseStack, int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position pos = getPosition();
        Size size = getSize();
        if (buttonTexture instanceof SizedTextureArea) {
            ((SizedTextureArea) buttonTexture).drawHorizontalCutSubArea(pos.x, pos.y, size.width, size.height, 0.0, 1.0);
        } else {
            buttonTexture.drawSubArea(pos.x, pos.y, size.width, size.height, 0.0f, 0.0f, 1.0f, 1.0f);
        }
        Font fontRenderer = Minecraft.getInstance().font;
        Component text = Component.translatable(optionNames[currentOption]);
        fontRenderer.drawShadow(poseStack, text,
                pos.x + size.width / 2f - fontRenderer.width(text) / 2f,
                pos.y + size.height / 2f - fontRenderer.lineHeight / 2f + 1, textColor);
    }

    @Override
    public void drawInForeground(PoseStack poseStack, int mouseX, int mouseY) {
        if (isMouseOverElement(mouseX, mouseY) && tooltipHoverString != null) {
            List<Component> hoverList = List.of(Component.translatable(tooltipHoverString));
            drawHoveringText(poseStack, ItemStack.EMPTY, hoverList, 300, (int) mouseX, (int) mouseY);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (currentOptionSupplier.getAsInt() != currentOption) {
            this.currentOption = currentOptionSupplier.getAsInt();
            writeUpdateInfo(1, buf -> buf.writeVarInt(currentOption));
        }
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 1) {
            this.currentOption = buffer.readVarInt();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (isMouseOverElement(mouseX, mouseY)) {
            //Allow only the RMB to reverse cycle
            if (button == RIGHT_MOUSE) {
                //Wrap from the first option to the last if needed
                this.currentOption = currentOption == 0 ? optionNames.length - 1 : currentOption - 1;
            } else {
                this.currentOption = (currentOption + 1) % optionNames.length;
            }
            setOptionExecutor.accept(currentOption);
            writeClientAction(1, buf -> buf.writeVarInt(currentOption));
            playButtonClickSound();
            return true;
        }
        return false;
    }


    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            this.currentOption = Mth.clamp(buffer.readVarInt(), 0, optionNames.length);
            setOptionExecutor.accept(currentOption);
        }
    }

}
