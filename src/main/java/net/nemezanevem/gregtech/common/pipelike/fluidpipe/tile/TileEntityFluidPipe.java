package net.nemezanevem.gregtech.common.pipelike.fluidpipe.tile;

import net.nemezanevem.gregtech.api.pipenet.block.material.TileEntityMaterialPipeBase;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.FluidPipeProperty;
import net.nemezanevem.gregtech.common.pipelike.fluidpipe.FluidPipeType;
import net.nemezanevem.gregtech.common.pipelike.fluidpipe.net.FluidPipeNet;
import net.nemezanevem.gregtech.common.pipelike.fluidpipe.net.WorldFluidPipeNet;

import java.lang.ref.WeakReference;

public class TileEntityFluidPipe extends TileEntityMaterialPipeBase<FluidPipeType, FluidPipeProperty> {

    public static final int FREQUENCY = 5;
    private WeakReference<FluidPipeNet> currentPipeNet = new WeakReference<>(null);

    @Override
    public Class<FluidPipeType> getPipeTypeClass() {
        return FluidPipeType.class;
    }

    @Override
    public boolean supportsTicking() {
        return false;
    }

    public int getCapacityPerTank() {
        return getNodeData().getThroughput() * 20;
    }

    public FluidPipeNet getFluidPipeNet() {
        if (level == null || level.isClientSide)
            return null;
        FluidPipeNet currentPipeNet = this.currentPipeNet.get();
        if (currentPipeNet != null && currentPipeNet.isValid() &&
                currentPipeNet.containsNode(getPipePos()))
            return currentPipeNet; //if current net is valid and does contain position, return it
        WorldFluidPipeNet worldFluidPipeNet = (WorldFluidPipeNet) getPipeBlock().getWorldPipeNet(getPipeWorld());
        currentPipeNet = worldFluidPipeNet.getNetFromPos(getPipePos());
        if (currentPipeNet != null) {
            this.currentPipeNet = new WeakReference<>(currentPipeNet);
        }
        return currentPipeNet;
    }

    public static void setNeighboursToFire(Level world, BlockPos selfPos) {
        for (Direction side : Direction.VALUES) {
            if (!GTValues.RNG.nextBoolean()) continue;
            BlockPos blockPos = selfPos.offset(side);
            BlockState blockState = world.getBlockState(blockPos);
            if (blockState.getBlock().isAir(blockState, world, blockPos) ||
                    blockState.getBlock().isFlammable(world, blockPos, side.getOpposite())) {
                world.setBlockState(blockPos, Blocks.FIRE.getDefaultState());
            }
        }
    }

    public static void spawnParticles(Level worldIn, BlockPos pos, Direction direction, EnumParticleTypes particleType, int particleCount) {
        if (worldIn instanceof WorldServer) {
            ((WorldServer) worldIn).spawnParticle(particleType,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    particleCount,
                    direction.getXOffset() * 0.2 + GTValues.RNG.nextDouble() * 0.1,
                    direction.getYOffset() * 0.2 + GTValues.RNG.nextDouble() * 0.1,
                    direction.getZOffset() * 0.2 + GTValues.RNG.nextDouble() * 0.1,
                    0.1);
        }
    }
}
