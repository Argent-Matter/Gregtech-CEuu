package net.nemezanevem.gregtech.api.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.renderer.RenderSystem;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.nemezanevem.gregtech.api.gui.IRenderContext;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.gui.resources.IGuiTexture;
import net.nemezanevem.gregtech.api.gui.resources.TextureArea;
import net.nemezanevem.gregtech.api.util.Position;
import net.nemezanevem.gregtech.api.util.Size;

import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

public class ImageWidget extends Widget {

    protected IGuiTexture area;

    private BooleanSupplier predicate;
    private boolean isVisible = true;
    private int border;
    private int borderColor;
    private String tooltipText;
    private boolean ignoreColor = false;

    public ImageWidget(int xPosition, int yPosition, int width, int height) {
        super(new Position(xPosition, yPosition), new Size(width, height));
    }

    public ImageWidget(int xPosition, int yPosition, int width, int height, IGuiTexture area) {
        this(xPosition, yPosition, width, height);
        this.area = area;
    }

    public ImageWidget setImage(TextureArea area) {
        this.area = area;
        return this;
    }

    public ImageWidget setBorder(int border, int color) {
        this.border = border;
        this.borderColor = color;
        return this;
    }

    public ImageWidget setPredicate(BooleanSupplier predicate) {
        this.predicate = predicate;
        this.isVisible = false;
        return this;
    }

    public ImageWidget setTooltip(String tooltipText) {
        this.tooltipText = tooltipText;
        return this;
    }

    public ImageWidget setIgnoreColor(boolean ignore) {
        this.ignoreColor = ignore;
        return this;
    }

    @Override
    public void updateScreen() {
        if (area != null) {
            area.updateTick();
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (predicate != null && predicate.getAsBoolean() != isVisible) {
            this.isVisible = predicate.getAsBoolean();
            writeUpdateInfo(1, buf -> buf.writeBoolean(isVisible));
        }
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 1) {
            this.isVisible = buffer.readBoolean();
        }
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        if (!this.isVisible || area == null) return;
        if (ignoreColor) RenderSystem.setShaderColor(1, 1, 1, 1);
        Position position = getPosition();
        Size size = getSize();
        area.draw(position.x, position.y, size.width, size.height);
        if (border > 0) {
            drawBorder(position.x, position.y, size.width, size.height, borderColor, border);
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        if (this.isVisible && tooltipText != null && area != null && isMouseOverElement(mouseX, mouseY)) {
            List<Component> hoverList = Arrays.asList(Component.translatable(tooltipText));
            drawHoveringText(ItemStack.EMPTY, hoverList, 300, mouseX, mouseY);
        }
    }
}
