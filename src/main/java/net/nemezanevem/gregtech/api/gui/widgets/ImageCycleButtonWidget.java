package net.nemezanevem.gregtech.api.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.nemezanevem.gregtech.api.gui.IRenderContext;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.gui.resources.SizedTextureArea;
import net.nemezanevem.gregtech.api.gui.resources.TextureArea;
import net.nemezanevem.gregtech.api.util.Position;
import net.nemezanevem.gregtech.api.util.Size;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class ImageCycleButtonWidget extends Widget {

    protected TextureArea buttonTexture;
    private final IntSupplier currentOptionSupplier;
    private final IntConsumer setOptionExecutor;
    private final int optionCount;
    private static final int RIGHT_MOUSE = 1;
    protected int currentOption;
    protected Function<Integer, String> tooltipHoverString;

    public ImageCycleButtonWidget(int xPosition, int yPosition, int width, int height, TextureArea buttonTexture, int optionCount, IntSupplier currentOptionSupplier, IntConsumer setOptionExecutor) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        this.buttonTexture = buttonTexture;
        this.currentOptionSupplier = currentOptionSupplier;
        this.setOptionExecutor = setOptionExecutor;
        this.optionCount = optionCount;
        this.currentOption = currentOptionSupplier.getAsInt();
    }


    public ImageCycleButtonWidget(int xPosition, int yPosition, int width, int height, TextureArea buttonTexture, BooleanSupplier supplier, BooleanConsumer updater) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        this.buttonTexture = buttonTexture;
        this.currentOptionSupplier = () -> supplier.getAsBoolean() ? 1 : 0;
        this.setOptionExecutor = (value) -> updater.accept(value >= 1);
        this.optionCount = 2;
        this.currentOption = currentOptionSupplier.getAsInt();
    }

    public ImageCycleButtonWidget setTooltipHoverString(String hoverString) {
        this.tooltipHoverString = val -> hoverString;
        return this;
    }

    public ImageCycleButtonWidget setTooltipHoverString(Function<Integer, String> hoverString) {
        this.tooltipHoverString = hoverString;
        return this;
    }

    public ImageCycleButtonWidget setButtonTexture(TextureArea texture) {
        this.buttonTexture = texture;
        return this;
    }

    @Override
    public void drawInBackground(PoseStack poseStack, int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position pos = getPosition();
        Size size = getSize();
        if (buttonTexture instanceof SizedTextureArea) {
            ((SizedTextureArea) buttonTexture).drawHorizontalCutSubArea(pos.x, pos.y, size.width, size.height, (float) currentOption / optionCount, (float) 1 / optionCount);
        } else {
            buttonTexture.drawSubArea(pos.x, pos.y, size.width, size.height, 0, (float) currentOption / optionCount, 1, (float) 1 / optionCount);
        }
    }

    @Override
    public void drawInForeground(PoseStack poseStack, int mouseX, int mouseY) {
        if (isMouseOverElement(mouseX, mouseY) && tooltipHoverString != null) {
            List<Component> hoverList = List.of(Component.translatable(tooltipHoverString.apply(currentOption)));
            drawHoveringText(poseStack, ItemStack.EMPTY, hoverList, 300, mouseX, mouseY);
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
            setOptionExecutor.accept(currentOption);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (isMouseOverElement(mouseX, mouseY)) {
            //Allow only the RMB to reverse cycle
            if (button == RIGHT_MOUSE) {
                //Wrap from the first option to the last if needed
                this.currentOption = currentOption == 0 ? optionCount - 1 : currentOption - 1;
            } else {
                this.currentOption = (currentOption + 1) % optionCount;
            }
            setOptionExecutor.accept(currentOption);
            writeClientAction(1, buf -> buf.writeVarInt(currentOption));
            //writeUpdateInfo(1, buf -> buf.writeVarInt(currentOption));
            playButtonClickSound();
            return true;
        }
        return false;
    }


    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            this.currentOption = Mth.clamp(buffer.readVarInt(), 0, optionCount);
            setOptionExecutor.accept(currentOption);
        }
    }

}
