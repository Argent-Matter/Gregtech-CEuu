package net.nemezanevem.gregtech.api.item.metaitem.stats;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

@FunctionalInterface
public interface IItemCapabilityProvider extends IItemComponent {

    ICapabilityProvider createProvider(ItemStack itemStack);

}
