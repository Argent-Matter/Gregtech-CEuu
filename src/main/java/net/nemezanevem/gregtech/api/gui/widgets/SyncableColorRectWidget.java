package net.nemezanevem.gregtech.api.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.FriendlyByteBuf;
import net.nemezanevem.gregtech.api.gui.IRenderContext;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.util.Position;
import net.nemezanevem.gregtech.api.util.Size;

import java.util.function.Supplier;

public class SyncableColorRectWidget extends Widget {

    protected Supplier<Integer> colorSupplier;
    protected int borderWidth;
    protected int borderColor;
    protected boolean drawCheckerboard;
    protected int checkerboardGridRows;
    protected int checkerboardGridColumns;
    private int color;

    public SyncableColorRectWidget(int x, int y, int width, int height, Supplier<Integer> colorSupplier) {
        super(x, y, width, height);
        this.colorSupplier = colorSupplier;
        this.borderWidth = 0;
        drawCheckerboard = false;
        checkerboardGridRows = 1;
        checkerboardGridColumns = 1;
        borderColor = 0xFF000000;
    }

    public SyncableColorRectWidget setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
        return this;
    }

    public SyncableColorRectWidget setBorderColor(int borderColor) {
        this.borderColor = borderColor;
        return this;
    }

    /**
     * Make sure the number of rows and columns evenly divides into the size of the non-border area
     */
    public SyncableColorRectWidget drawCheckerboard(int checkerboardGridColumns, int checkerboardGridRows) {
        this.drawCheckerboard = true;
        this.checkerboardGridColumns = checkerboardGridColumns;
        this.checkerboardGridRows = checkerboardGridRows;
        return this;
    }

    @Override
    public void drawInBackground(PoseStack poseStack, int mouseY, int mouseX, float partialTicks, IRenderContext context) {
        super.drawInBackground(poseStack, mouseY, mouseX, partialTicks, context);
        Position position = getPosition();
        Size size = getSize();
        drawSolidRect(poseStack, position.x, position.y, size.width, size.height, borderColor);
        if (drawCheckerboard) {
            int white = 0xFFFFFFFF;
            int grey = 0xFFBFBFBF;
            int columnWidth = (size.width - 2*borderWidth) / checkerboardGridColumns;
            int rowHeight = (size.height - 2*borderWidth) / checkerboardGridRows;
            boolean whiteGrey = false;
            for (int i = 0; i < checkerboardGridRows; i++) {
                for (int j = 0; j < checkerboardGridColumns; j++) {
                    drawSolidRect(poseStack,position.x + borderWidth + i * columnWidth, position.y + borderWidth + j * rowHeight, columnWidth, rowHeight, whiteGrey ? white : grey);
                    whiteGrey = !whiteGrey;
                }
                whiteGrey = !whiteGrey;
            }
        }
        drawSolidRect(poseStack,position.x + borderWidth, position.y + borderWidth, size.width - 2*borderWidth, size.height - 2*borderWidth, color);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (colorSupplier != null && colorSupplier.get() != color) {
            this.color = colorSupplier.get();
            writeUpdateInfo(1, buffer -> buffer.writeInt(color));
        }
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 1) {
            this.color = buffer.readInt();
        }
    }
}
