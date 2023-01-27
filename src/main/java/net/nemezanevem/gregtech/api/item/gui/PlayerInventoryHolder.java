package net.nemezanevem.gregtech.api.item.gui;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.nemezanevem.gregtech.api.gui.GtGuiFactories;
import net.nemezanevem.gregtech.api.gui.IUIHolder;
import net.nemezanevem.gregtech.api.gui.ModularUI;

import java.util.function.BooleanSupplier;

public class PlayerInventoryHolder implements IUIHolder {

    public static void openHandItemUI(Player player, InteractionHand hand) {
        PlayerInventoryHolder holder = new PlayerInventoryHolder(player, hand);
        holder.openUI();
    }

    public final Player player;

    final InteractionHand hand;

    ItemStack sampleItem;
    BooleanSupplier validityCheck;

    public PlayerInventoryHolder(Player player, InteractionHand hand, ItemStack sampleItem) {
        this.player = player;
        this.hand = hand;
        this.sampleItem = sampleItem;
        this.validityCheck = () -> ItemStack.matches(sampleItem, player.getItemInHand(hand));
    }

    public PlayerInventoryHolder(Player Player, InteractionHand hand) {
        this.player = Player;
        this.hand = hand;
        this.sampleItem = player.getItemInHand(hand);
        this.validityCheck = () -> ItemStack.matches(sampleItem, player.getItemInHand(hand));
    }

    public PlayerInventoryHolder setCustomValidityCheck(BooleanSupplier validityCheck) {
        this.validityCheck = validityCheck;
        return this;
    }

    ModularUI createUI(Player Player) {
        ItemUIFactory uiFactory = (ItemUIFactory) sampleItem.getItem();
        return uiFactory.createUI(this, Player);
    }

    public void openUI() {
        GtGuiFactories.PLAYER_INV.get().openUI(this, (ServerPlayer) player);
    }

    @Override
    public boolean isValid() {
        return validityCheck.getAsBoolean();
    }

    @Override
    public boolean isClientSide() {
        return player.getLevel().isClientSide;
    }

    public ItemStack getCurrentItem() {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!ItemStack.matches(sampleItem, itemStack))
            return null;
        return itemStack;
    }

    /**
     * Will replace current item in hand with the given one
     * will also update sample item to this item
     */
    public void setCurrentItem(ItemStack item) {
        this.sampleItem = item;
        player.setItemInHand(hand, item);
    }

    @Override
    public void markAsDirty() {
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
    }
}
