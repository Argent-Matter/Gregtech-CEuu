package net.nemezanevem.gregtech.api.gui.impl;


import codechicken.lib.math.Mth;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.nemezanevem.gregtech.api.gui.IRenderContext;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.gui.widgets.SlotWidget;
import net.nemezanevem.gregtech.common.network.packets.PacketUIWidgetUpdate;

import java.io.IOException;
import java.util.Set;

import static net.nemezanevem.gregtech.api.gui.Widget.drawGradientRect;

public class ModularUIGui extends AbstractContainerScreen<ModularUIContainer> implements IRenderContext {

    private final ModularUI modularUI;

    public static final float FRAMES_PER_TICK = 1/3f;

    private float lastUpdate;

    public int dragSplittingLimit;
    public int dragSplittingButton;

    public ModularUI getModularUI() {
        return modularUI;
    }

    public ModularUIGui(int windowId, ModularUI modularUI, Inventory playerInv) {
        super(new ModularUIContainer(windowId, modularUI), playerInv, Component.translatable("gui"));
        this.modularUI = modularUI;
        modularUI.setModularUIGui(this);
    }

    @Override
    public void init() {
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);
        this.imageWidth = modularUI.getWidth();
        this.imageHeight = modularUI.getHeight();
        super.init();
        this.modularUI.updateScreenSize(width, height);
    }

    @Override
    public void onClose() {
        super.onClose();
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        modularUI.guiWidgets.values().forEach(Widget::containerTick);
    }

    public void handleWidgetUpdate(PacketUIWidgetUpdate packet) {
        if (packet.windowId == menu.containerId) {
            Widget widget = modularUI.guiWidgets.get(packet.widgetId);
            int updateId = packet.updateData.readVarInt();
            if (widget != null) {
                widget.readUpdateInfo(updateId, packet.updateData);
            }
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        lastUpdate += partialTicks;
        while (lastUpdate >= 0) {
            lastUpdate -= FRAMES_PER_TICK;
            modularUI.guiWidgets.values().forEach(Widget::updateScreenOnFrame);
        }
        this.hoveredSlot = null;
        renderBackground(poseStack);

        RenderSystem.disablePolygonOffset();
        Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
        RenderSystem.disableDepthTest();

        this.renderBg(poseStack, partialTicks, mouseX, mouseY);

        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
        poseStack.pushPose();
        poseStack.translate(leftPos, topPos, 0.0F);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enablePolygonOffset();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        for (int i = 0; i < this.menu.slots.size(); ++i) {
            Slot slot = this.menu.slots.get(i);
            if (slot instanceof SlotWidget.ISlotWidget) {
                if (((SlotWidget.ISlotWidget) slot).isHover()) {
                    setHoveredSlot(slot);
                }
            } else if (this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY) && slot.isActive()) {
                renderSlotOverlay(slot);
                setHoveredSlot(slot);
            }
        }

        Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
        poseStack.popPose();

        renderLabels(poseStack, mouseX, mouseY);

        poseStack.pushPose();
        poseStack.translate(leftPos, topPos, 0.0F);
            Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();

        MinecraftForge.EVENT_BUS.post(new ContainerScreenEvent.Render.Foreground(this, poseStack, mouseX, mouseY));

        RenderSystem.enableDepthTest();
        renderItemStackOnMouse(mouseX, mouseY);
        renderReturningItemStack();

        poseStack.popPose();
        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();

        renderTooltip(poseStack, mouseX, mouseY);
    }


    public void setHoveredSlot(Slot hoveredSlot) {
        this.hoveredSlot = hoveredSlot;
    }

    @Deprecated
    public void renderSlotOverlay(Slot slot) {
        RenderSystem.disableDepthTest();
        int slotX = slot.x;
        int slotY = slot.y;
        RenderSystem.colorMask(true, true, true, false);
        drawGradientRect(slotX, slotY, slotX + 16, slotY + 16, -2130706433, -2130706433);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
    }

    public ItemStack getDraggedStack() {
        return this.draggingItem;
    }

    private void renderItemStackOnMouse(int mouseX, int mouseY) {
        InventoryMenu inventory = this.minecraft.player.inventoryMenu;
        ItemStack itemStack = this.draggingItem.isEmpty() ? inventory.getCarried() : this.draggingItem;

        if (!itemStack.isEmpty()) {
            int dragOffset = this.draggingItem.isEmpty() ? 8 : 16;
            if (!this.draggingItem.isEmpty() && this.isQuickCrafting) {
                itemStack = itemStack.copy();
                itemStack.setCount(Mth.ceil((float) itemStack.getCount() / 2.0F));

            } else if (this.isQuickCrafting && this.quickCraftSlots.size() > 1) {
                itemStack = itemStack.copy();
                itemStack.setCount(this.quickCraftingRemainder);
            }
            this.itemRenderer.renderGuiItem(itemStack, mouseX - leftPos - 8, mouseY - topPos - dragOffset);
        }
    }

    private void renderReturningItemStack() {
        if (!this.draggingItem.isEmpty()) {
            float partialTicks = (Minecraft.getInstance().getFrameTime() - this.snapbackTime) / 100.0F;
            if (partialTicks >= 1.0F) {
                partialTicks = 1.0F;
                this.snapbackItem = ItemStack.EMPTY;
            }
            int deltaX = this.snapbackEnd.x - this.snapbackStartX;
            int deltaY = this.snapbackEnd.x - this.snapbackStartY;
            int currentX = this.snapbackStartX + (int) ((float) deltaX * partialTicks);
            int currentY = this.snapbackStartY + (int) ((float) deltaY * partialTicks);
            //noinspection ConstantConditions
            this.itemRenderer.renderGuiItem(this.snapbackItem, currentX, currentY);
        }
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        modularUI.guiWidgets.values().forEach(widget -> {
            if (!widget.isVisible()) return;
            poseStack.pushPose();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            widget.drawInForeground(poseStack, mouseX, mouseY);
            poseStack.popPose();
        });
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int pMouseX, int pMouseY) {
        poseStack.pushPose();
        RenderSystem.setShaderColor(modularUI.getRColorForOverlay(), modularUI.getGColorForOverlay(), modularUI.getBColorForOverlay(), 1.0F);
        RenderSystem.enableBlend();
        poseStack.popPose();
        modularUI.backgroundPath.draw(leftPos, topPos, imageWidth, imageHeight);
        modularUI.guiWidgets.values().forEach(widget -> {
            if (!widget.isVisible()) return;
            poseStack.pushPose();
            RenderSystem.enableBlend();
            widget.drawInBackground(poseStack, pMouseX, pMouseY, partialTicks,this);
            RenderSystem.setShaderColor(modularUI.getRColorForOverlay(), modularUI.getGColorForOverlay(), modularUI.getBColorForOverlay(), 1.0F);
            poseStack.popPose();
        });
    }

    protected void mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        for (int i = modularUI.guiWidgets.size() - 1; i >= 0; i--) {
            Widget widget = modularUI.guiWidgets.get(i);
            if(widget.isVisible() && widget.isActive() && widget.mouseWheelMove(mouseX, mouseY, wheelDelta)) {
                return;
            }
        }
    }

    public Set<Slot> getQuickCraftSlots() {
        return this.quickCraftSlots;
    }

    public boolean getIsQuickCrafting() {
        return this.isQuickCrafting;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        for (int i = modularUI.guiWidgets.size() - 1; i >= 0; i--) {
            Widget widget = modularUI.guiWidgets.get(i);
            if(widget.isVisible() && widget.isActive() && widget.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        return false;
    }

    public void superMouseClicked(double mouseX, double mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int pButton, double dragX, double dragY) {
        for (int i = modularUI.guiWidgets.size() - 1; i >= 0; i--) {
            Widget widget = modularUI.guiWidgets.get(i);
            if(widget.isVisible() && widget.isActive()) {
                return widget.mouseDragged(mouseX, mouseY, pButton, dragX, dragY);
            }
        }
        return false;
    }

    public void superMouseDragged(double mouseX, double mouseY, int clickedMouseButton, double pDragX, double pDragY) {
        super.mouseDragged(mouseX, mouseY, clickedMouseButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        for (int i = modularUI.guiWidgets.size() - 1; i >= 0; i--) {
            Widget widget = modularUI.guiWidgets.get(i);
            if(widget.isVisible() && widget.isActive() && widget.mouseReleased(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    public void superMouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        for (int i = modularUI.guiWidgets.size() - 1; i >= 0; i--) {
            Widget widget = modularUI.guiWidgets.get(i);
            if(widget.isVisible() && widget.isActive() && widget.keyTyped(pCodePoint, pModifiers)) {
                return true;
            }
        }
        return super.charTyped(pCodePoint, pModifiers);
    }
}