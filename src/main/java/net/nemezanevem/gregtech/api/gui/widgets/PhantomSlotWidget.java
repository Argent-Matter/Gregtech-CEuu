package net.nemezanevem.gregtech.api.gui.widgets;

import com.google.common.collect.Lists;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler.Target;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.nemezanevem.gregtech.api.gui.ingredient.IGhostIngredientTarget;
import net.nemezanevem.gregtech.api.util.SlotUtil;
import net.nemezanevem.gregtech.client.util.MouseButtonHelper;
import net.nemezanevem.gregtech.client.util.TooltipHelper;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class PhantomSlotWidget extends SlotWidget implements IGhostIngredientTarget {

    private boolean clearSlotOnRightClick;

    public PhantomSlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition) {
        super(itemHandler, slotIndex, xPosition, yPosition, false, false);
    }

    public PhantomSlotWidget setClearSlotOnRightClick(boolean clearSlotOnRightClick) {
        this.clearSlotOnRightClick = clearSlotOnRightClick;
        return this;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY) && gui != null) {
            if (button == 1 && clearSlotOnRightClick && !slotReference.getItem().isEmpty()) {
                slotReference.set(ItemStack.EMPTY);
                writeClientAction(2, buf -> {
                });
            } else {
                gui.getModularUIGui().superMouseClicked(mouseX, mouseY, button);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(int mouseX, int mouseY, int button, double dragX, double dragY) {
        if (isMouseOverElement(mouseX, mouseY) && gui != null) {
            ItemStack is = gui.player.containerMenu.getCarried().copy();
            is.setCount(1);
            slotReference.set(is);
            writeClientAction(1, buffer -> {
                buffer.writeItem(slotReference.getItem());
                int mouseButton = MouseButtonHelper.button;
                boolean shiftDown = Screen.hasShiftDown();
                buffer.writeVarInt(mouseButton);
                buffer.writeBoolean(shiftDown);
            });
            return true;
        }
        return false;
    }

    @Override
    public ItemStack slotClick(int dragType, ClickType clickTypeIn, Player player) {
        ItemStack stackHeld = player.containerMenu.getCarried();
        return SlotUtil.slotClickPhantom(slotReference, dragType, clickTypeIn, stackHeld);
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack onItemTake(Player player, ItemStack stack, boolean simulate) {
        return super.onItemTake(player, stack, simulate);
    }

    @Override
    public List<Target<?>> getPhantomTargets(Object ingredient) {
        if (!(ingredient instanceof ItemStack)) {
            return Collections.emptyList();
        }
        Rect2i rectangle = toRectangleBox();
        return Lists.newArrayList(new Target<>() {
            @Nonnull
            @Override
            public Rect2i getArea() {
                return rectangle;
            }

            @Override
            public void accept(@Nonnull Object ingredient) {
                if (ingredient instanceof ItemStack) {
                    int mouseButton = MouseButtonHelper.button;
                    boolean shiftDown = Screen.hasShiftDown();
                    ClickType clickType = shiftDown ? ClickType.QUICK_MOVE : ClickType.PICKUP;
                    SlotUtil.slotClickPhantom(slotReference, mouseButton, clickType, (ItemStack) ingredient);
                    writeClientAction(1, buffer -> {
                        buffer.writeItem((ItemStack) ingredient);
                        buffer.writeVarInt(mouseButton);
                        buffer.writeBoolean(shiftDown);
                    });
                }
            }
        });
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        if (id == 1) {
            ItemStack stackHeld;
            stackHeld = buffer.readItem();
            int mouseButton = buffer.readVarInt();
            boolean shiftKeyDown = buffer.readBoolean();
            ClickType clickType = shiftKeyDown ? ClickType.QUICK_MOVE : ClickType.PICKUP;
            SlotUtil.slotClickPhantom(slotReference, mouseButton, clickType, stackHeld);
        } else if (id == 2) {
            slotReference.set(ItemStack.EMPTY);
        }
    }
}
