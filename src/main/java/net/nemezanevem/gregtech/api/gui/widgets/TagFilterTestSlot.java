package net.nemezanevem.gregtech.api.gui.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.gui.IRenderContext;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.gui.ingredient.IGhostIngredientTarget;
import net.nemezanevem.gregtech.api.util.Position;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author brachy84
 */
public class TagFilterTestSlot extends Widget implements IGhostIngredientTarget {

    private List<MutableComponent> testStack = ItemStack.EMPTY;
    private Consumer<List<MutableComponent>> listener;

    public TagFilterTestSlot(int xPosition, int yPosition) {
        super(xPosition, yPosition, 18, 18);
    }

    public TagFilterTestSlot setListener(Consumer<List<MutableComponent>> listener) {
        this.listener = listener;
        return this;
    }

    public TagFilterTestSlot setTestStack(List<MutableComponent> testStack) {
        if (testStack != null) {
            this.testStack = testStack;
        }
        return this;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
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
    public void drawInBackground(PoseStack poseStack, int mouseY, int mouseX, float partialTicks, IRenderContext context) {
        Position pos = getPosition();
        GuiTextures.SLOT.draw(poseStack, pos.x, pos.y, 18, 18);

        if (!testStack.isEmpty()) {
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
            Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
                Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
            poseStack.pushPose();
            ItemRenderer itemRender = Minecraft.getInstance().getItemRenderer();
            itemRender.renderAndDecorateItem(testStack, pos.x + 1, pos.y + 1);
            itemRender.renderGuiItemDecorations(Minecraft.getInstance().font, testStack, pos.x + 1, pos.y + 1, null);
            poseStack.popPose();
            Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
        }
        if (isActive() && isMouseOverElement(mouseX, mouseY)) {
            RenderSystem.disableDepthTest();
            RenderSystem.colorMask(true, true, true, false);
            drawSolidRect(poseStack, getPosition().x + 1, getPosition().y + 1, 16, 16, -2130706433);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
        }
    }

    @Override
    public void drawInForeground(PoseStack poseStack, int mouseX, int mouseY) {
        if (isActive() && isMouseOverElement(mouseX, mouseY)) {
            if (!testStack.isEmpty()) {
                this.drawHoveringText(poseStack, testStack, getItemToolTip(testStack), 300, mouseX, mouseY);
            } else {
                drawHoveringText(poseStack, ItemStack.EMPTY, List.of(Component.translatable("cover.ore_dictionary_filter.test_slot.info")), 300, mouseX, mouseY);
            }
        }
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object ingredient) {
        if (!(ingredient instanceof ItemStack)) {
            return Collections.emptyList();
        }
        Rect2i rectangle = toRectangleBox();
        return Lists.newArrayList(new IGhostIngredientHandler.Target<>() {
            @Nonnull
            @Override
            public Rect2i getArea() {
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
