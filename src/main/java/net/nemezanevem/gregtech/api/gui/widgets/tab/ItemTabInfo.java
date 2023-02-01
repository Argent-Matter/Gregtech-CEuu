package net.nemezanevem.gregtech.api.gui.widgets.tab;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.nemezanevem.gregtech.api.gui.resources.IGuiTexture;

import java.util.Collections;
import java.util.Optional;

public class ItemTabInfo implements ITabInfo {

    private final String nameLocale;
    private final ItemStack iconStack;

    public ItemTabInfo(String nameLocale, ItemStack iconStack) {
        this.nameLocale = nameLocale;
        this.iconStack = iconStack;
    }

    @Override
    public void renderTab(IGuiTexture tabTexture, int posX, int posY, int xSize, int ySize, boolean isSelected) {
        tabTexture.draw(posX, posY, xSize, ySize);
        RenderSystem.disablePolygonOffset();
        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
        Minecraft.getInstance().getItemRenderer().renderGuiItem(iconStack, posX + xSize / 2 - 8, posY + ySize / 2 - 8);
        Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
        RenderSystem.enablePolygonOffset();
    }

    @Override
    public void renderHoverText(PoseStack poseStack, int posX, int posY, int xSize, int ySize, int guiWidth, int guiHeight, boolean isSelected, int mouseX, int mouseY) {
        if (nameLocale != null) {
            Component localizedText = Component.translatable(nameLocale);
            Minecraft mc = Minecraft.getInstance();
            mc.screen.renderTooltip(poseStack, Collections.singletonList(localizedText), Optional.empty(), mouseX, mouseY, mc.font);
        }
    }
}
