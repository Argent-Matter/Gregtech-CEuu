package net.nemezanevem.gregtech.api.gui.widgets;

import net.minecraft.network.FriendlyByteBuf;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.util.Position;
import net.nemezanevem.gregtech.api.util.Size;

import java.util.function.BooleanSupplier;

public class ServerWidgetGroup extends AbstractWidgetGroup {

    private final BooleanSupplier isVisibleGetter;
    private Boolean lastIsVisible;

    public ServerWidgetGroup(BooleanSupplier isVisibleGetter, Position position, Size size) {
        super(position, size);
        this.isVisibleGetter = isVisibleGetter;
    }

    public ServerWidgetGroup(BooleanSupplier isVisibleGetter, Position position) {
        super(position);
        this.isVisibleGetter = isVisibleGetter;
    }

    public ServerWidgetGroup(BooleanSupplier isVisibleGetter) {
        this(isVisibleGetter, Position.ORIGIN);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (lastIsVisible == null || isVisibleGetter.getAsBoolean() != lastIsVisible) {
            this.lastIsVisible = isVisibleGetter.getAsBoolean();
            writeUpdateInfo(2, buffer -> buffer.writeBoolean(lastIsVisible));
        }
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 2) {
            setVisible(buffer.readBoolean());
        }
    }

    @Override
    public void addWidget(Widget widget) {
        super.addWidget(widget);
    }
}
