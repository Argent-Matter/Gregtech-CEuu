package net.nemezanevem.gregtech.api.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.nemezanevem.gregtech.api.GTValues;

public abstract class TickableTileEntityBase extends SyncedTileEntityBase implements BlockEntityTicker<TickableTileEntityBase> {

    private long timer = 0L;

    // Create an offset [0,20) to distribute ticks more evenly
    private final int offset = GTValues.RNG.nextInt(20);

    public TickableTileEntityBase(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public boolean isFirstTick() {
        return timer == 0;
    }

    /**
     * Replacement for former getTimer().
     *
     * @return Timer value with a random offset of [0,20].
     */
    public long getOffsetTimer() {
        return timer + offset;
    }

    @Override
    public void update() {
        if (timer == 0) {
            onFirstTick();
        }
        timer++;
    }

    protected void onFirstTick() {
    }



    @Override
    public void tick(Level pLevel, BlockPos pPos, BlockState pState, TickableTileEntityBase pBlockEntity) {
        pBlockEntity.update();
    }

}
