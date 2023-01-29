package net.nemezanevem.gregtech.common.block;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.TagPrefix;
import gregtech.common.ConfigHolder;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.World;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.tag.TagPrefix;
import net.nemezanevem.gregtech.common.ConfigHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class CompressedItemBlock extends BlockItem {

    public final BlockCompressed compressedBlock;

    public CompressedItemBlock(BlockCompressed compressedBlock) {
        super(compressedBlock);
        this.compressedBlock = compressedBlock;
        setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    public BlockState getBlockState(ItemStack stack) {
        return compressedBlock.getStateFromMeta(getMetadata(stack.getItemDamage()));
    }

    @Nonnull
    @Override
    public Component getName(@Nonnull ItemStack stack) {
        Material material = getBlockState(stack).getValue(compressedBlock.variantProperty);
        return TagPrefix.block.getLocalNameForItem(material);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level worldIn, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if (ConfigHolder.misc.debug) {
            tooltip.add("MetaItem Id: block" + compressedBlock.getGtMaterial(stack.getMetadata()).toCamelCaseString());
        }
    }
}
