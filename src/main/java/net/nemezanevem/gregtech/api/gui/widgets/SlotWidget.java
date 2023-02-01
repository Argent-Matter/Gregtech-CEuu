package net.nemezanevem.gregtech.api.gui.widgets;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;
import net.nemezanevem.gregtech.api.gui.INativeWidget;
import net.nemezanevem.gregtech.api.gui.IRenderContext;
import net.nemezanevem.gregtech.api.gui.ISizeProvider;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.gui.impl.ModularUIGui;
import net.nemezanevem.gregtech.api.gui.resources.IGuiTexture;
import net.nemezanevem.gregtech.api.util.Position;
import net.nemezanevem.gregtech.api.util.Size;

import javax.annotation.Nonnull;
import java.util.List;

public class SlotWidget extends Widget implements INativeWidget {

    protected final Slot slotReference;
    protected final boolean canTakeItems;
    protected final boolean canPutItems;
    protected SlotLocationInfo locationInfo = new SlotLocationInfo(false, false);

    protected IGuiTexture[] backgroundTexture;
    protected Runnable changeListener;

    private String tooltipText;
    private Object[] tooltipArgs;

    public SlotWidget(Container inventory, int slotIndex, int xPosition, int yPosition, boolean canTakeItems, boolean canPutItems) {
        super(new Position(xPosition, yPosition), new Size(18, 18));
        this.canTakeItems = canTakeItems;
        this.canPutItems = canPutItems;
        this.slotReference = createSlot(inventory, slotIndex);
    }

    public SlotWidget(IItemHandler itemHandler, int slotIndex, int xPosition, int yPosition, boolean canTakeItems, boolean canPutItems) {
        super(new Position(xPosition, yPosition), new Size(18, 18));
        this.canTakeItems = canTakeItems;
        this.canPutItems = canPutItems;
        this.slotReference = createSlot(itemHandler, slotIndex, true);
    }

    public SlotWidget(IItemHandler itemHandler, int slotIndex, int xPosition, int yPosition, boolean canTakeItems, boolean canPutItems, boolean canShiftClickInto) {
        super(new Position(xPosition, yPosition), new Size(18, 18));
        this.canTakeItems = canTakeItems;
        this.canPutItems = canPutItems;
        this.slotReference = createSlot(itemHandler, slotIndex, canShiftClickInto);
    }

    @Override
    public void setSizes(ISizeProvider sizes) {
        super.setSizes(sizes);
        onPositionUpdate();
    }

    protected Slot createSlot(Container inventory, int index) {
        return new WidgetSlot(inventory, index, 0, 0);
    }

    protected Slot createSlot(IItemHandler itemHandler, int index, boolean canShiftClickInto) {
        return new WidgetSlotItemHandler(itemHandler, index, 0, 0, canShiftClickInto);
    }

    @Override
    public void drawInForeground(PoseStack poseStack, int mouseX, int mouseY) {
        ((ISlotWidget) slotReference).setHover(isMouseOverElement(mouseX, mouseY) && isActive());
        if (tooltipText != null && isMouseOverElement(mouseX, mouseY) && !slotReference.hasItem()) {
            List<Component> hoverList = List.of(Component.translatable(tooltipText, tooltipArgs));
            drawHoveringText(poseStack, ItemStack.EMPTY, hoverList, 300, mouseX, mouseY);
        }
    }

    @Override
    public void drawInBackground(PoseStack poseStack, int mouseY, int mouseX, float partialTicks, IRenderContext context) {
        Position pos = getPosition();
        Size size = getSize();
        if (backgroundTexture != null) {
            for (IGuiTexture backgroundTexture : this.backgroundTexture) {
                backgroundTexture.draw(poseStack, pos.x, pos.y, size.width, size.height);
            }
        }
        ItemStack itemStack = slotReference.getItem();
        ModularUIGui modularUIGui = gui == null ? null : gui.getModularUIGui();
        if (itemStack.isEmpty() && modularUIGui!= null && modularUIGui.getIsQuickCrafting() && modularUIGui.getQuickCraftSlots().contains(slotReference)) { // draw split
            int splitSize = modularUIGui.getQuickCraftSlots().size();
            itemStack = gui.player.inventoryMenu.getCarried();
            if (!itemStack.isEmpty() && splitSize > 1 && slotReference.container.canPlaceItem(slotReference.index, itemStack)) {
                itemStack = itemStack.copy();
                gui.player.containerMenu.getQuickCraftSlotCount(modularUIGui.getQuickCraftSlots(), modularUIGui.dragSplittingLimit, itemStack, slotReference.getItem().isEmpty() ? 0 : slotReference.getItem().getCount());
                int k = Math.min(itemStack.getMaxStackSize(), slotReference.getMaxStackSize(itemStack));
                if (itemStack.getCount() > k) {
                    itemStack.setCount(k);
                }
            }
        }
        if (!itemStack.isEmpty()) {
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.disablePolygonOffset();
            Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
            Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
            poseStack.pushPose();
            ItemRenderer itemRender = Minecraft.getInstance().getItemRenderer();
            itemRender.renderAndDecorateItem(itemStack, pos.x + 1, pos.y + 1);
            itemRender.renderGuiItemDecorations(Minecraft.getInstance().font, itemStack, pos.x + 1, pos.y + 1, null);
            poseStack.popPose();
            Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
        }
        if (isActive()) {
            if (slotReference instanceof ISlotWidget) {
                if (isMouseOverElement(mouseX, mouseY)) {
                    RenderSystem.disableDepthTest();
                    RenderSystem.colorMask(true, true, true, false);
                    drawSolidRect(poseStack, getPosition().x + 1, getPosition().y + 1, 16, 16, -2130706433);
                    RenderSystem.colorMask(true, true, true, true);
                    RenderSystem.enableDepthTest();
                    RenderSystem.enableBlend();
                }
            }
        } else {
            RenderSystem.colorMask(true, true, true, false);
            drawSolidRect(poseStack, getPosition().x + 1, getPosition().y + 1, 16, 16, 0xbf000000);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.enableBlend();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY) && gui != null) {
            ModularUIGui modularUIGui = gui.getModularUIGui();
            boolean last = modularUIGui.getIsQuickCrafting();
            gui.getModularUIGui().superMouseClicked(mouseX, mouseY, button);
            if (last != modularUIGui.getIsQuickCrafting()) {
                modularUIGui.dragSplittingButton = button;
                if (button == 0) {
                    modularUIGui.dragSplittingLimit = 0;
                }
                else if (button == 1) {
                    modularUIGui.dragSplittingLimit = 1;
                }
                else if (Minecraft.getInstance().options.keyPickItem.isActiveAndMatches(InputConstants.getKey(button - 100, -1))) {
                    modularUIGui.dragSplittingLimit = 2;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY) && gui != null) {
            gui.getModularUIGui().superMouseReleased(mouseX, mouseY, button);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isMouseOverElement(mouseX, mouseY) && gui != null) {
            gui.getModularUIGui().superMouseDragged(mouseX, mouseY, button, dragX, dragY);
            return true;
        }
        return false;
    }

    @Override
    protected void onPositionUpdate() {
        if (slotReference != null && sizes != null) {
            Position position = getPosition();
            this.slotReference.x = position.x + 1 - sizes.getGuiLeft();
            this.slotReference.y = position.y + 1 - sizes.getGuiTop();
        }
    }

    public SlotWidget setChangeListener(Runnable changeListener) {
        this.changeListener = changeListener;
        return this;
    }

    public SlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition) {
        this(itemHandler, slotIndex, xPosition, yPosition, true, true);
    }

    public SlotWidget(Container inventory, int slotIndex, int xPosition, int yPosition) {
        this(inventory, slotIndex, xPosition, yPosition, true, true);
    }

    /**
     * Sets array of background textures used by slot
     * they are drawn on top of each other
     */
    public SlotWidget setBackgroundTexture(IGuiTexture... backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
        return this;
    }

    public SlotWidget setLocationInfo(boolean isPlayerInventory, boolean isHotbarSlot) {
        this.locationInfo = new SlotLocationInfo(isPlayerInventory, isHotbarSlot);
        return this;
    }

    public SlotWidget setTooltipText(String tooltipText, Object... args) {
        Preconditions.checkNotNull(tooltipText, "tooltipText");
        this.tooltipText = tooltipText;
        this.tooltipArgs = args;
        return this;
    }

    @Override
    public SlotLocationInfo getSlotLocationInfo() {
        return locationInfo;
    }

    public boolean canPutStack(ItemStack stack) {
        return isEnabled() && canPutItems;
    }

    public boolean canTakeStack(Player player) {
        return isEnabled() && canTakeItems;
    }

    public boolean isEnabled() {
        return this.isActive() && isVisible();
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack) {
        return isEnabled();
    }

    @Override
    public ItemStack slotClick(int dragType, ClickType clickTypeIn, Player player) {
        return null;
    }

    public void onSlotChanged() {
        gui.holder.markAsDirty();
    }

    @Override
    public final Slot getHandle() {
        return slotReference;
    }

    public interface ISlotWidget {
        void setHover(boolean isHover);
        boolean isHover();
    }

    protected class WidgetSlot extends Slot implements ISlotWidget {
        boolean isHover;

        public WidgetSlot(Container inventory, int index, int xPosition, int yPosition) {
            super(inventory, index, xPosition, yPosition);
        }

        @Override
        public void setHover(boolean isHover) {
            this.isHover = isHover;
        }

        @Override
        public boolean isHover() {
            return isHover;
        }

        @Override
        public boolean mayPlace(@Nonnull ItemStack stack) {
            return SlotWidget.this.canPutStack(stack) && super.mayPlace(stack);
        }

        @Override
        public boolean mayPickup(@Nonnull Player playerIn) {
            return SlotWidget.this.canTakeStack(playerIn) && super.mayPickup(playerIn);
        }

        @Override
        public void set(@Nonnull ItemStack stack) {
            super.set(stack);
            if (changeListener != null) {
                changeListener.run();
            }
        }

        @Override
        public final void onTake(@Nonnull Player thePlayer, @Nonnull ItemStack stack) {
            super.onTake(thePlayer, stack);
            onItemTake(thePlayer, stack, false);
        }

        @Override
        public void setChanged() {
            SlotWidget.this.onSlotChanged();
        }

        @Override
        public boolean isActive() {
            return SlotWidget.this.isEnabled();
        }
    }

    public class WidgetSlotItemHandler extends SlotItemHandler implements ISlotWidget {
        boolean isHover;
        final boolean canShiftClickInto;

        public WidgetSlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition, boolean canShiftClickInto) {
            super(itemHandler, index, xPosition, yPosition);
            this.canShiftClickInto = canShiftClickInto;
        }

        @Override
        public void setHover(boolean isHover) {
            this.isHover = isHover;
        }

        @Override
        public boolean isHover() {
            return isHover;
        }

        @Override
        public boolean mayPlace(@Nonnull ItemStack stack) {
            return SlotWidget.this.canPutStack(stack) && super.mayPlace(stack);
        }

        @Override
        public boolean mayPickup(Player playerIn) {
            return SlotWidget.this.canTakeStack(playerIn) && super.mayPickup(playerIn);
        }

        @Override
        public void set(@Nonnull ItemStack stack) {
            super.set(stack);
            if (changeListener != null) {
                changeListener.run();
            }
        }

        @Override
        public void onTake(@Nonnull Player thePlayer, @Nonnull ItemStack stack) {
            super.onTake(thePlayer, stack);
            onItemTake(thePlayer, stack, false);
        }

        @Override
        public void setChanged() {
            SlotWidget.this.onSlotChanged();
        }

        @Override
        public boolean isActive() {
            return SlotWidget.this.isEnabled();
        }

        public boolean canInsert() {
            return canShiftClickInto;
        }
    }
}
