package net.nemezanevem.gregtech.api.util;

import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BlockInfo represents immutable information for block in world
 * This includes block state and tile entity, and needed for complete representation
 * of some complex blocks like machines, when rendering or manipulating them without world instance
 */
public class BlockInfo {

    public static final BlockInfo EMPTY = new BlockInfo(Blocks.AIR);

    private final BlockState blockState;
    private final BlockEntity tileEntity;
    private final Object info;

    public BlockInfo(Block block) {
        this(block.defaultBlockState());
    }

    public BlockInfo(BlockState blockState) {
        this(blockState, null);
    }

    public BlockInfo(BlockState blockState, BlockEntity tileEntity) {
        this(blockState, tileEntity, null);
    }

    public BlockInfo(BlockState blockState, BlockEntity tileEntity, Object info) {
        this.blockState = blockState;
        this.tileEntity = tileEntity;
        this.info = info;
        Preconditions.checkArgument(tileEntity == null || (blockState.hasBlockEntity()),
                "Cannot create block info with tile entity for block not having it");
    }

    public BlockState getBlockState() {
        return blockState;
    }

    public BlockEntity getBlockEntity() {
        return tileEntity;
    }

    public Object getInfo() {
        return info;
    }

    public void apply(Level world, BlockPos pos) {
        world.setBlock(pos, blockState, 3);
        if (tileEntity != null) {
            world.setBlockEntity(tileEntity);
        }
    }
}