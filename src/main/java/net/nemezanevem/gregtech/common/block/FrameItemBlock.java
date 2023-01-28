package net.nemezanevem.gregtech.common.block;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.tag.TagPrefix;
import net.nemezanevem.gregtech.common.ConfigHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class FrameItemBlock extends BlockItem {

    private final BlockFrame frameBlock;

    public FrameItemBlock(BlockFrame block) {
        super(block, new Properties().tab(GregTech.TAB_GREGTECH));
        this.frameBlock = block;
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    public BlockState getBlockState(ItemStack stack) {
        return frameBlock.getStateFromMeta(getMetadata(stack.getItemDamage()));
    }

    @Nonnull
    @Override
    public Component getName(@Nonnull ItemStack stack) {
        Material material = getBlockState(stack).getValue(frameBlock.variantProperty);
        return TagPrefix.frameGt.getLocalNameForItem(material);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level worldIn, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if (ConfigHolder.misc.debug) {
            tooltip.add("MetaItem Id: frame" + frameBlock.getGtMaterial(stack.getMetadata()).toCamelCaseString());
        }
    }
}
