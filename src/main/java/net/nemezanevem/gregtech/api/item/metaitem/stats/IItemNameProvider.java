package net.nemezanevem.gregtech.api.item.metaitem.stats;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface IItemNameProvider extends IItemComponent {

    String getItemStackDisplayName(ItemStack itemStack, String unlocalizedName);

}
