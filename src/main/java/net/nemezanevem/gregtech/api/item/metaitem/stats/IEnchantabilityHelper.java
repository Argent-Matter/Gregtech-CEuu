package net.nemezanevem.gregtech.api.item.metaitem.stats;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public interface IEnchantabilityHelper extends IItemComponent {

    boolean isEnchantable(ItemStack stack);

    int getItemEnchantability();

    boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment);

}