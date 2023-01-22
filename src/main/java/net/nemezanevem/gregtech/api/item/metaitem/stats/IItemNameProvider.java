package net.nemezanevem.gregtech.api.item.metaitem.stats;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface IItemNameProvider extends IItemComponent {

    Component getItemStackDisplayName(ItemStack itemStack, String unlocalizedName);

}
