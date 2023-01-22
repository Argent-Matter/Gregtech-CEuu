package net.nemezanevem.gregtech.common.tab;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.util.Util;

public class TabGregtechItem extends CreativeModeTab {
    public TabGregtechItem(String label) {
        super(label);
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(ForgeRegistries.ITEMS.getValue(Util.gtResource("ingot_aluminium")), 1);
    }
}
