package net.nemezanevem.gregtech.api.item.metaitem;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.IForgeRegistry;
import net.nemezanevem.gregtech.api.gui.ModularUI;

public class StandardMetaItem extends MetaItem {

    public StandardMetaItem(ExtendedProperties properties) {
        super(properties);
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, Player Player) {
        return null;
    }
}
