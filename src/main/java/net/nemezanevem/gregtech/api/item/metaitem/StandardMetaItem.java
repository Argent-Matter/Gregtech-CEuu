package net.nemezanevem.gregtech.api.item.metaitem;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.IForgeRegistry;
import net.nemezanevem.gregtech.api.gui.ModularUI;

public class StandardMetaItem extends MetaItem {

    public StandardMetaItem() {
        super();
    }

    @Override
    protected ExtendedProperties constructMetaValueItem(ResourceLocation id, IForgeRegistry<Item> registry) {
        return new ExtendedProperties(id, registry);
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, Player entityPlayer) {
        return null;
    }
}
