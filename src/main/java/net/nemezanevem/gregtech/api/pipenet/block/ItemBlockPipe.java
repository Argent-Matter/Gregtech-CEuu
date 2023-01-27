package net.nemezanevem.gregtech.api.pipenet.block;

import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.common.ConfigHolder;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.player.Player;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ItemBlockPipe<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType> extends ItemBlock {

    protected final BlockPipe<PipeType, NodeDataType, ?> blockPipe;

    public ItemBlockPipe(BlockPipe<PipeType, NodeDataType, ?> block) {
        super(block);
        this.blockPipe = block;
        setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull Player player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Direction side, float hitX, float hitY, float hitZ, @Nonnull BlockState newState) {
        boolean superVal = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
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
