package net.nemezanevem.gregtech.api.gui.resources;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemStackTexture implements IGuiTexture{
    private final ItemStack[] itemStack;
    private int index = 0;
    private int ticks = 0;

    public ItemStackTexture(ItemStack stack, ItemStack... itemStack) {
        this.itemStack = new ItemStack[itemStack.length + 1];
        this.itemStack[0] = stack;
        System.arraycopy(itemStack, 0, this.itemStack, 1, itemStack.length);
    }

    public ItemStackTexture(Item item, Item... items) {
        this.itemStack = new ItemStack[items.length + 1];
        this.itemStack[0] = new ItemStack(item);
        for(int i = 0; i < items.length; i++) {
            itemStack[i+1] = new ItemStack(items[i]);
        }
    }

    @Override
    public void updateTick() {
        if(itemStack.length > 1 && ++ticks % 20 == 0)
            if(++index == itemStack.length)
                index = 0;
    }

    @Override
    public void draw(PoseStack poseStack, double x, double y, int width, int height) {
        Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
        RenderSystem.disableDepthTest();
        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
        poseStack.pushPose();
        poseStack.scale(width / 16f, height / 16f, 0.0001f);
        poseStack.translate(x * 16 / width, y * 16 / height, 0);
        ItemRenderer itemRender = Minecraft.getInstance().getItemRenderer();
        itemRender.renderAndDecorateItem(itemStack[index], 0, 0);
        RenderSystem.enableDepthTest();
        poseStack.popPose();
        Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
    }
}
