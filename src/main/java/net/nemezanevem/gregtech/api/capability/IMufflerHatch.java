package net.nemezanevem.gregtech.api.capability;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IMufflerHatch {

    void recoverItemsTable(List<ItemStack> recoveryItems);

    /**
     * @return true if front face is free and contains only air blocks in 1x1 area
     */
    boolean isFrontFaceFree();
}
