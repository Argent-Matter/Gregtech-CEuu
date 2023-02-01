package net.nemezanevem.gregtech.api.gui.widgets.tab;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.nemezanevem.gregtech.api.gui.resources.IGuiTexture;

import java.util.Collections;
import java.util.Optional;

public class IGuiTextureTabInfo implements ITabInfo {
    public final IGuiTexture texture;
    public final String nameLocale;

    public IGuiTextureTabInfo(IGuiTexture texture, String nameLocale) {
        this.texture = texture;
        this.nameLocale = nameLocale;
    }

    @Override
    public void renderTab(IGuiTexture tabTexture, int posX, int posY, int xSize, int ySize, boolean isSelected) {
        tabTexture.draw(posX, posY, xSize, ySize);
        texture.draw(posX, posY, xSize, ySize);
    }

    @Override
    public void renderHoverText(PoseStack poseStack, int posX, int posY, int xSize, int ySize, int guiWidth, int guiHeight, boolean isSelected, int mouseX, int mouseY) {
        Component localizedText = Component.translatable(nameLocale);
        Minecraft mc = Minecraft.getInstance();
        mc.screen.renderTooltip(poseStack, Collections.singletonList(localizedText), Optional.empty(), mouseX, mouseY, mc.font);
    }
}
