package net.nemezanevem.gregtech.api.item.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.UIFactory;
import net.nemezanevem.gregtech.api.item.metaitem.MetaItem;

import java.io.IOException;

/**
 * {@link UIFactory} implementation for {@link MetaItem}s
 */
public class PlayerInventoryUIFactory extends UIFactory<PlayerInventoryHolder> {

    public static final PlayerInventoryUIFactory INSTANCE = new PlayerInventoryUIFactory();

    private PlayerInventoryUIFactory() {
    }

    public void init() {
        GregTechAPI.UI_FACTORY_REGISTRY.register(1, new ResourceLocation(GregTech.MODID, "player_inventory_factory"), this);
    }

    @Override
    protected ModularUI createUITemplate(PlayerInventoryHolder holder, Player entityPlayer) {
        return holder.createUI(entityPlayer);
    }

    @Override
    protected PlayerInventoryHolder readHolderFromSyncData(FriendlyByteBuf syncData) {
        Player entityPlayer = Minecraft.getInstance().player;
        InteractionHand enumHand = InteractionHand.values()[syncData.readByte()];
        ItemStack itemStack;
        itemStack = syncData.readItem();
        return new PlayerInventoryHolder(entityPlayer, enumHand, itemStack);
    }

    @Override
    protected void writeHolderToSyncData(FriendlyByteBuf syncData, PlayerInventoryHolder holder) {
        syncData.writeByte(holder.hand.ordinal());
        syncData.writeItem(holder.getCurrentItem());
    }

}