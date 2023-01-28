package net.nemezanevem.gregtech.api.pipenet.block.material;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.nemezanevem.gregtech.api.pipenet.block.ItemBlockPipe;
import net.nemezanevem.gregtech.api.unification.material.Material;

import javax.annotation.Nonnull;

public class ItemBlockMaterialPipe<PipeType extends Enum<PipeType> & IMaterialPipeType<NodeDataType>, NodeDataType> extends ItemBlockPipe<PipeType, NodeDataType> {

    public ItemBlockMaterialPipe(BlockMaterialPipe<PipeType, NodeDataType, ?> block) {
        super(block);
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    @Override
    public Component getName(@Nonnull ItemStack stack) {
        PipeType pipeType = blockPipe.getItemPipeType(stack);
        Material material = ((BlockMaterialPipe<PipeType, NodeDataType, ?>) blockPipe).getItemMaterial(stack);
        return material == null ? Component.literal(" ") : pipeType.getTagPrefix().getLocalNameForItem(material);
    }
}
