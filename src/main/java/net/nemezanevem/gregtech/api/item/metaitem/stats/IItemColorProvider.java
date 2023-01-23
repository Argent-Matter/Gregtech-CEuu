package net.nemezanevem.gregtech.api.item.metaitem.stats;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface IItemColorProvider extends IItemComponent {

    int getItemStackColor(ItemStack itemStack, int tintIndex);
}
