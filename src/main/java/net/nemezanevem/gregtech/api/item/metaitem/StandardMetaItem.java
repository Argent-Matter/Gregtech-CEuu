package net.nemezanevem.gregtech.api.item.metaitem;

import net.minecraft.world.entity.player.Player;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.item.gui.PlayerInventoryHolder;

public class StandardMetaItem extends MetaItem {

    public StandardMetaItem(ExtendedProperties properties) {
        super(properties);
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, Player Player) {
        return null;
    }
}
