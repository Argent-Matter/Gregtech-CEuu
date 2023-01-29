package net.nemezanevem.gregtech.common.pipelike.itempipe;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.nemezanevem.gregtech.api.pipenet.block.material.BlockMaterialPipe;
import net.nemezanevem.gregtech.api.pipenet.block.material.ItemBlockMaterialPipe;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.ItemPipeProperty;
import net.nemezanevem.gregtech.common.ConfigHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockItemPipe extends ItemBlockMaterialPipe<ItemPipeType, ItemPipeProperty> {

    public ItemBlockItemPipe(BlockItemPipe block) {
        super(block);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level worldIn, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn) {
        ItemPipeProperty pipeProperties = blockPipe.createItemProperties(stack);
        if (pipeProperties.getTransferRate() % 1 != 0) {
            tooltip.add(Component.translatable("gregtech.universal.tooltip.item_transfer_rate", (int) ((pipeProperties.getTransferRate() * 64) + 0.5)));
        } else {
            tooltip.add(Component.translatable("gregtech.universal.tooltip.item_transfer_rate_stacks", (int) pipeProperties.getTransferRate()));
        }
        tooltip.add(Component.translatable("gregtech.item_pipe.priority", pipeProperties.getPriority()));

        if (ConfigHolder.misc.debug) {
            tooltip.add(Component.nullToEmpty("MetaItem Id: " + ((BlockMaterialPipe<?, ?, ?>) blockPipe).getPrefix().name + ((BlockMaterialPipe<?, ?, ?>) blockPipe).getItemMaterial(stack).toLowerUnderscoreString()));
        }
    }
}
