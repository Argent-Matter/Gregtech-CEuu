package net.nemezanevem.gregtech.api.item.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.UIFactory;
import net.nemezanevem.gregtech.api.item.metaitem.MetaItem;

/**
 * {@link UIFactory} implementation for {@link MetaItem}s
 */
public class PlayerInventoryUIFactory extends UIFactory<PlayerInventoryHolder> {

    @Override
    protected ModularUI createUITemplate(PlayerInventoryHolder holder, Player Player) {
        return holder.createUI(Player);
    }

    @Override
    protected PlayerInventoryHolder readHolderFromSyncData(FriendlyByteBuf syncData) {
        Player Player = Minecraft.getInstance().player;
        InteractionHand InteractionHand = InteractionHand.values()[syncData.readByte()];
        ItemStack itemStack;
        itemStack = syncData.readItem();
        return new PlayerInventoryHolder(Player, InteractionHand, itemStack);
    }

    @Override
    protected void writeHolderToSyncData(FriendlyByteBuf syncData, PlayerInventoryHolder holder) {
        syncData.writeByte(holder.hand.ordinal());
        syncData.writeItem(holder.getCurrentItem());
    }

}