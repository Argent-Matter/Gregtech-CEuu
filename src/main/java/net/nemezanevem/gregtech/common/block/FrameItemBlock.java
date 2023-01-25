package net.nemezanevem.gregtech.common.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.nemezanevem.gregtech.GregTech;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class FrameItemBlock extends BlockItem {

    private final BlockFrame frameBlock;

    public FrameItemBlock(BlockFrame block) {
        super(block, new Properties().tab(GregTech.TAB_GREGTECH));
        this.frameBlock = block;
        setHasSubtypes(true);
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
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        Material material = getBlockState(stack).getValue(frameBlock.variantProperty);
        return OrePrefix.frameGt.getLocalNameForItem(material);
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if (ConfigHolder.misc.debug) {
            tooltip.add("MetaItem Id: frame" + frameBlock.getGtMaterial(stack.getMetadata()).toCamelCaseString());
        }
    }
}
