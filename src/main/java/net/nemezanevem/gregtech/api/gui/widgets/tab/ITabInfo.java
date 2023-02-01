package net.nemezanevem.gregtech.api.gui.widgets.tab;


import com.mojang.blaze3d.vertex.PoseStack;
import net.nemezanevem.gregtech.api.gui.resources.IGuiTexture;

public interface ITabInfo {

    void renderTab(IGuiTexture tabTexture, int posX, int posY, int xSize, int ySize, boolean isSelected);

    void renderHoverText(PoseStack poseStack, int posX, int posY, int xSize, int ySize, int guiWidth, int guiHeight, boolean isSelected, int mouseX, int mouseY);

}
