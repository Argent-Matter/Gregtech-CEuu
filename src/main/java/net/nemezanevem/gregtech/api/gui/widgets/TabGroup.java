package net.nemezanevem.gregtech.api.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Tuple;
import net.nemezanevem.gregtech.api.gui.IRenderContext;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.gui.widgets.tab.HorizontalTabListRenderer;
import net.nemezanevem.gregtech.api.gui.widgets.tab.HorizontalTabListRenderer.*;
import net.nemezanevem.gregtech.api.gui.widgets.tab.VerticalTabListRenderer;
import net.nemezanevem.gregtech.api.gui.widgets.tab.VerticalTabListRenderer.*;
import net.nemezanevem.gregtech.api.gui.widgets.tab.ITabInfo;
import net.nemezanevem.gregtech.api.gui.widgets.tab.TabListRenderer;
import net.nemezanevem.gregtech.api.util.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class TabGroup<T extends AbstractWidgetGroup> extends AbstractWidgetGroup {

    private final List<ITabInfo> tabInfos = new ArrayList<>();
    private final List<T> tabWidgets = new ArrayList<>();
    protected int selectedTabIndex = 0;
    private final TabListRenderer tabListRenderer;
    private BiConsumer<Integer, Integer> onTabChanged;

    public TabGroup(int x, int y, TabListRenderer tabListRenderer) {
        super(new Position(x, y));
        this.tabListRenderer = tabListRenderer;
    }

    public TabGroup(TabLocation tabLocation, Position position) {
        super(position);
        this.tabListRenderer = tabLocation.supplier.get();
    }

    public void addTab(ITabInfo tabInfo, T tabWidget) {
        this.tabInfos.add(tabInfo);
        int tabIndex = tabInfos.size() - 1;
        this.tabWidgets.add(tabWidget);
        tabWidget.setVisible(tabIndex == selectedTabIndex);
        tabWidget.setActive(tabIndex == selectedTabIndex);
        addWidget(tabWidget);
    }

    public ITabInfo getTabInfo(int i) {
        if (i < tabInfos.size()) {
            return this.tabInfos.get(i);
        }
        return null;
    }

    public T getTabWidget(int i) {
        if (i < tabWidgets.size()) {
            return this.tabWidgets.get(i);
        }
        return null;
    }

    public void removeTab(int index) {
        this.tabInfos.remove(index);
        T tab = this.tabWidgets.remove(index);
        this.removeWidget(tab);
        if (selectedTabIndex >= index && selectedTabIndex > 0) {
            selectedTabIndex--;
        }
        for (int i = 0; i < this.tabWidgets.size(); i++) {
            tabWidgets.get(i).setActive(i == selectedTabIndex);
            tabWidgets.get(i).setVisible(i == selectedTabIndex);
        }
    }

    public TabGroup<T> setOnTabChanged(BiConsumer<Integer, Integer> onTabChanged) {
        this.onTabChanged = onTabChanged;
        return this;
    }

    public T getCurrentTag() {
        return tabWidgets.get(selectedTabIndex);
    }

    public List<T> getAllTag() {
        return tabWidgets;
    }

    @Override
    public List<Widget> getContainedWidgets(boolean includeHidden) {
        ArrayList<Widget> containedWidgets = new ArrayList<>(widgets.size());

        if (includeHidden) {
            for (Widget widget : tabWidgets) {
                containedWidgets.add(widget);

                if (widget instanceof AbstractWidgetGroup)
                    containedWidgets.addAll(((AbstractWidgetGroup) widget).getContainedWidgets(true));
            }
        } else {
            T widgetGroup = tabWidgets.get(selectedTabIndex);
            containedWidgets.add(widgetGroup);
            containedWidgets.addAll(widgetGroup.getContainedWidgets(false));
        }

        return containedWidgets;
    }

    @Override
    public void drawInBackground(PoseStack poseStack, int mouseY, int mouseX, float partialTicks, IRenderContext context) {
        super.drawInBackground(poseStack, mouseY, mouseX, partialTicks, context);
        this.tabListRenderer.renderTabs(gui, getPosition(), tabInfos, sizes.getWidth(), sizes.getHeight(), selectedTabIndex);
    }

    @Override
    public void drawInForeground(PoseStack poseStack, int mouseX, int mouseY) {
        super.drawInForeground(poseStack, mouseX, mouseY);
        Tuple<ITabInfo, int[]> tabOnMouse = getTabOnMouse(mouseX, mouseY);
        if (tabOnMouse != null) {
            int[] tabSizes = tabOnMouse.getB();
            ITabInfo tabInfo = tabOnMouse.getA();
            boolean isSelected = tabInfos.get(selectedTabIndex) == tabInfo;
            tabInfo.renderHoverText(poseStack, tabSizes[0], tabSizes[1], tabSizes[2], tabSizes[3], sizes.getWidth(), sizes.getHeight(), isSelected, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean flag = super.mouseClicked(mouseX, mouseY, button);
        Tuple<ITabInfo, int[]> tabOnMouse = getTabOnMouse(mouseX, mouseY);
        if (tabOnMouse != null) {
            ITabInfo tabInfo = tabOnMouse.getA();
            int tabIndex = tabInfos.indexOf(tabInfo);
            if (selectedTabIndex != tabIndex) {
                setSelectedTab(tabIndex);
                playButtonClickSound();
                writeClientAction(2, buf -> buf.writeVarInt(tabIndex));
                return true;
            }
        }
        return flag;
    }

    public void setSelectedTab(int tabIndex) {
        int old = selectedTabIndex;
        this.tabWidgets.get(selectedTabIndex).setVisible(false);
        this.tabWidgets.get(selectedTabIndex).setActive(false);
        this.tabWidgets.get(tabIndex).setVisible(true);
        this.tabWidgets.get(tabIndex).setActive(true);
        this.selectedTabIndex = tabIndex;
        if (this.onTabChanged != null) {
            onTabChanged.accept(old, tabIndex);
        }
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        super.handleClientAction(id, buffer);
        if (id == 2) {
            int tabIndex = buffer.readVarInt();
            if (selectedTabIndex != tabIndex) {
                setSelectedTab(tabIndex);
            }
        }
    }

    private Tuple<ITabInfo, int[]> getTabOnMouse(double mouseX, double mouseY) {
        for (int tabIndex = 0; tabIndex < tabInfos.size(); tabIndex++) {
            ITabInfo tabInfo = tabInfos.get(tabIndex);
            int[] tabSizes = tabListRenderer.getTabPos(tabIndex, sizes.getWidth(), sizes.getHeight());
            tabSizes[0] += getPosition().x;
            tabSizes[1] += getPosition().y;
            if (isMouseOverTab(mouseX, mouseY, tabSizes)) {
                return new Tuple<>(tabInfo, tabSizes);
            }
        }
        return null;
    }

    private static boolean isMouseOverTab(double mouseX, double mouseY, int[] tabSizes) {
        int minX = tabSizes[0];
        int minY = tabSizes[1];
        int maxX = tabSizes[0] + tabSizes[2];
        int maxY = tabSizes[1] + tabSizes[3];
        return mouseX >= minX && mouseY >= minY && mouseX < maxX && mouseY < maxY;
    }

    public boolean isWidgetVisible(Widget widget) {
        return tabWidgets.get(selectedTabIndex) == widget;
    }

    public enum TabLocation {

        HORIZONTAL_TOP_LEFT(() -> new HorizontalTabListRenderer(HorizontalStartCorner.LEFT, VerticalLocation.TOP)),
        HORIZONTAL_TOP_RIGHT(() -> new HorizontalTabListRenderer(HorizontalStartCorner.RIGHT, VerticalLocation.TOP)),
        HORIZONTAL_BOTTOM_LEFT(() -> new HorizontalTabListRenderer(HorizontalStartCorner.LEFT, VerticalLocation.BOTTOM)),
        HORIZONTAL_BOTTOM_RIGHT(() -> new HorizontalTabListRenderer(HorizontalStartCorner.RIGHT, VerticalLocation.BOTTOM)),
        VERTICAL_LEFT_TOP(() -> new VerticalTabListRenderer(VerticalStartCorner.TOP, HorizontalLocation.LEFT)),
        VERTICAL_LEFT_BOTTOM(() -> new VerticalTabListRenderer(VerticalStartCorner.BOTTOM, HorizontalLocation.LEFT)),
        VERTICAL_RIGHT_TOP(() -> new VerticalTabListRenderer(VerticalStartCorner.TOP, HorizontalLocation.RIGHT)),
        VERTICAL_RIGHT_BOTTOM(() -> new VerticalTabListRenderer(VerticalStartCorner.BOTTOM, HorizontalLocation.RIGHT));

        private final Supplier<TabListRenderer> supplier;

        TabLocation(Supplier<TabListRenderer> supplier) {
            this.supplier = supplier;
        }
    }

}
