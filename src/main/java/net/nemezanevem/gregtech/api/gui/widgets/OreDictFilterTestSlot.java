package net.nemezanevem.gregtech.api.gui.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.nemezanevem.gregtech.api.gui.IRenderContext;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.util.Position;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static net.nemezanevem.gregtech.api.gui.Widget.getItemToolTip;

/**
 * @author brachy84
 */
public class OreDictFilterTestSlot extends Widget implements IGhostIngredientTarget {

    private ItemStack testStack = ItemStack.EMPTY;
    private Consumer<ItemStack> listener;

    public OreDictFilterTestSlot(int xPosition, int yPosition) {
        super(xPosition, yPosition, 18, 18);
    }

    public OreDictFilterTestSlot setListener(Consumer<ItemStack> listener) {
        this.listener = listener;
        return this;
    }

    public OreDictFilterTestSlot setTestStack(ItemStack testStack) {
        if (testStack != null) {
            this.testStack = testStack;
        }
        return this;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            // this is only called on client, so this is fine
            Player player = Minecraft.getInstance().player;
            ItemStack cursorStack = player.getInventory().getSelected();
            putItem(cursorStack);
            return true;
        }
        return false;
    }

    private void putItem(ItemStack stack) {
        if ((stack.isEmpty() ^ testStack.isEmpty()) || !ItemStack.matches(stack, testStack) || !ItemStack.isSameItemSameTags(testStack, stack)) {
            testStack = stack.copy();
            testStack.setCount(1);
            if (listener != null)
                listener.accept(testStack);
        }
    }

    @Override
    public void drawInBackground(PoseStack poseStack, int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position pos = getPosition();
        GuiTextures.SLOT.draw(pos.x, pos.y, 18, 18);

        if (!testStack.isEmpty()) {
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderHelper.disableStandardItemLighting();
            RenderHelper.enableStandardItemLighting();
            RenderHelper.enableGUIStandardItemLighting();
            poseStack.pushPose();
            ItemRenderer itemRender = Minecraft.getInstance().getItemRenderer();
            itemRender.renderItemAndEffectIntoGUI(testStack, pos.x + 1, pos.y + 1);
            itemRender.renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRenderer, testStack, pos.x + 1, pos.y + 1, null);
            poseStack.popPose();
            RenderHelper.disableStandardItemLighting();
        }
        if (isActive() && isMouseOverElement(mouseX, mouseY)) {
            RenderSystem.disableDepthTest();
            RenderSystem.colorMask(true, true, true, false);
            drawSolidRect(getPosition().x + 1, getPosition().y + 1, 16, 16, -2130706433);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        if (isActive() && isMouseOverElement(mouseX, mouseY)) {
            if (!testStack.isEmpty()) {
                GuiUtils.preItemToolTip(testStack);
                this.drawHoveringText(testStack, getItemToolTip(testStack), 300, mouseX, mouseY);
                GuiUtils.postItemToolTip();
            } else {
                drawHoveringText(ItemStack.EMPTY, List.of(Component.translatable("cover.ore_dictionary_filter.test_slot.info")), 300, mouseX, mouseY);
            }
        }
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object ingredient) {
        if (!(ingredient instanceof ItemStack)) {
            return Collections.emptyList();
        }
        Rectangle rectangle = toRectangleBox();
        return Lists.newArrayList(new IGhostIngredientHandler.Target<Object>() {
            @Nonnull
            @Override
            public Rectangle getArea() {
                return rectangle;
            }

            @Override
            public void accept(@Nonnull Object ingredient) {
                if (ingredient instanceof ItemStack) {
                    putItem((ItemStack) ingredient);
                }
            }
        });
    }
}
