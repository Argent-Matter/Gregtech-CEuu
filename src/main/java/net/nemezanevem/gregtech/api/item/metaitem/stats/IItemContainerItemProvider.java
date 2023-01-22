package net.nemezanevem.gregtech.api.item.metaitem.stats;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface IItemContainerItemProvider extends IItemComponent {

    ItemStack getContainerItem(ItemStack itemStack);
}
