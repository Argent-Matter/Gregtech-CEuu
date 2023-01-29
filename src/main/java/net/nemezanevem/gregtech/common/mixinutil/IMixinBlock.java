package net.nemezanevem.gregtech.common.mixinutil;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IMixinBlock {
    List<ItemStack> captureDrops(boolean start);
}
