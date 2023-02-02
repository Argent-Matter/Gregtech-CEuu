package net.nemezanevem.gregtech.api.pipenet.block.material;

import net.minecraft.world.item.ItemStack;
import net.nemezanevem.gregtech.api.pipenet.PipeNet;
import net.nemezanevem.gregtech.api.pipenet.WorldPipeNet;
import net.nemezanevem.gregtech.api.pipenet.block.BlockPipe;
import net.nemezanevem.gregtech.api.pipenet.block.IPipeType;
import net.nemezanevem.gregtech.api.pipenet.tile.IPipeTile;
import net.nemezanevem.gregtech.api.pipenet.tile.TileEntityPipeBase;
import net.nemezanevem.gregtech.api.registry.material.MaterialRegistry;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.tag.TagPrefix;
import net.nemezanevem.gregtech.common.pipelike.cable.ItemBlockCable;

public abstract class BlockMaterialPipe<PipeType extends Enum<PipeType> & IPipeType<NodeDataType> & IMaterialPipeType<NodeDataType>, NodeDataType, WorldPipeNetType extends WorldPipeNet<NodeDataType, ? extends PipeNet<NodeDataType>>> extends BlockPipe<PipeType, NodeDataType, WorldPipeNetType> {

    protected final PipeType pipeType;

    public BlockMaterialPipe(PipeType pipeType) {
        this.pipeType = pipeType;
    }

    @Override
    public NodeDataType createProperties(IPipeTile<PipeType, NodeDataType> pipeTile) {
        PipeType pipeType = pipeTile.getPipeType();
        Material material = ((IMaterialPipeTile<PipeType, NodeDataType>) pipeTile).getPipeMaterial();
        if (pipeType == null || material == null) {
            return getFallbackType();
        }
        return createProperties(pipeType, material);
    }

    @Override
    public NodeDataType createItemProperties(ItemStack itemStack) {
        Material material = getItemMaterial(itemStack);
        if (pipeType == null || material == null) {
            return getFallbackType();
        }
        return createProperties(pipeType, material);
    }

    public ItemStack getItem(Material material) {
        if (material == null) return ItemStack.EMPTY;
        int materialId = GregTechAPI.MATERIAL_REGISTRY.getIDForObject(material);

        return new ItemStack(this, 1);
    }

    public Material getItemMaterial(ItemStack itemStack) {
        return MaterialRegistry.MATERIALS_BUILTIN.get().getValue(itemStack.getItem() instanceof ItemBlockCable cable ? cable)
        return GregTechAPI.MATERIAL_REGISTRY.getObjectById(itemStack.getMetadata());
    }

    @Override
    public void setTileEntityData(TileEntityPipeBase<PipeType, NodeDataType> pipeTile, ItemStack itemStack) {
        ((TileEntityMaterialPipeBase<PipeType, NodeDataType>) pipeTile).setPipeData(this, pipeType, getItemMaterial(itemStack));
    }

    @Override
    public ItemStack getDropItem(IPipeTile<PipeType, NodeDataType> pipeTile) {
        Material material = ((IMaterialPipeTile<PipeType, NodeDataType>) pipeTile).getPipeMaterial();
        return getItem(material);
    }

    protected abstract NodeDataType createProperties(PipeType pipeType, Material material);

    public TagPrefix getPrefix() {
        return pipeType.getTagPrefix();
    }

    public PipeType getItemPipeType(ItemStack is) {
        return pipeType;
    }
}
