package net.nemezanevem.gregtech.api.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.nemezanevem.gregtech.common.mixinutil.IMixinBlock;

import java.util.List;

public class BlockUtility {

    private static final Block WRAPPER = new Block(BlockBehaviour.Properties.of(Material.AIR));

    public static void startCaptureDrops() {
        ((IMixinBlock)WRAPPER).captureDrops(true);
    }

    public static List<ItemStack> stopCaptureDrops() {
        return ((IMixinBlock)WRAPPER).captureDrops(false);
    }

}
