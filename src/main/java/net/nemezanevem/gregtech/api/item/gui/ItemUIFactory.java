package net.nemezanevem.gregtech.api.item.gui;


import net.minecraft.world.entity.player.Player;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.item.stats.IItemComponent;

public interface ItemUIFactory extends IItemComponent {

    /**
     * Creates new UI basing on given holder. Holder contains information
     * about item stack and hand, and also player
     */
    ModularUI createUI(PlayerInventoryHolder holder, Player entityPlayer);

}
