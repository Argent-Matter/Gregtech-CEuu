package net.nemezanevem.gregtech.api.pipenet.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.pipenet.tile.IPipeTile;

import javax.annotation.Nonnull;

public class ItemBlockPipe<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType> extends BlockItem {

    protected final BlockPipe<PipeType, NodeDataType, ?> blockPipe;

    public ItemBlockPipe(BlockPipe<PipeType, NodeDataType, ?> block) {
        super(block, new Item.Properties().tab(GregTech.TAB_GREGTECH));
        this.blockPipe = block;
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public InteractionResult place(BlockPlaceContext pContext) {
        InteractionResult superVal = super.place(pContext);
        if (superVal && !world.isClientSide) {
            IPipeTile selfTile = (IPipeTile) world.getTileEntity(pos);
            if (selfTile == null) return superVal;
            if (selfTile.getPipeBlock().canConnect(selfTile, side.getOpposite())) {
                selfTile.setConnection(side.getOpposite(), true, false);
            }
            for (Direction facing : Direction.values()) {
                TileEntity te = world.getTileEntity(pos.offset(facing));
                if (te instanceof IPipeTile) {
                    IPipeTile otherPipe = ((IPipeTile) te);
                    if (otherPipe.isConnected(facing.getOpposite())) {
                        if (otherPipe.getPipeBlock().canPipesConnect(otherPipe, facing.getOpposite(), selfTile)) {
                            selfTile.setConnection(facing, true, true);
                        } else {
                            otherPipe.setConnection(facing.getOpposite(), false, true);
                        }
                    }
                } else if (!ConfigHolder.machines.gt6StylePipesCables && selfTile.getPipeBlock().canPipeConnectToBlock(selfTile, facing, te)) {
                    selfTile.setConnection(facing, true, false);
                }
            }
        }
        return superVal;
    }
}
