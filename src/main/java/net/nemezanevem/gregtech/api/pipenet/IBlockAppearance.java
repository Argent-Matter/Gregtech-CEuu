package net.nemezanevem.gregtech.api.pipenet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

/**
 * Implement this interface on blocks that can mimic the appearance of other blocks. Note that this is meant to be available server-side, so ensure the code is
 * server-safe and doesn't use client-side code.
 * <p>
 * Mostly based on and (copied from) CoFHCore with minor tweaks
 * https://github.com/CoFH/CoFHCore/
 */
public interface IBlockAppearance {

    /**
     * This function returns the state of the block that is being shown on a given side.
     *
     * @param world Reference to the world.
     * @param pos   The Position of the block.
     * @param side  The side of the block.
     */
    @Nonnull
    BlockState getVisualState(@Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nonnull Direction side);

    /**
     * This function returns whether the block's renderer will visually connect to other blocks implementing IBlockAppearance.
     */
    boolean supportsVisualConnections();
}
