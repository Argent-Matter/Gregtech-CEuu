package net.nemezanevem.gregtech.common.pipelike.cable.net;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.nemezanevem.gregtech.api.capability.GregtechCapabilities;
import net.nemezanevem.gregtech.api.capability.IEnergyContainer;
import net.nemezanevem.gregtech.api.pipenet.PipeNetWalker;
import net.nemezanevem.gregtech.api.pipenet.tile.IPipeTile;
import net.nemezanevem.gregtech.common.pipelike.cable.tile.TileEntityCable;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EnergyNetWalker extends PipeNetWalker {

    public static List<RoutePath> createNetData(Level world, BlockPos sourcePipe) {
        EnergyNetWalker walker = new EnergyNetWalker(world, sourcePipe, 1, new ArrayList<>());
        walker.traversePipeNet();
        return walker.isFailed() ? null : walker.routes;
    }

    private final List<RoutePath> routes;
    private TileEntityCable[] pipes = {};
    private int loss;

    protected EnergyNetWalker(Level world, BlockPos sourcePipe, int walkedBlocks, List<RoutePath> routes) {
        super(world, sourcePipe, walkedBlocks);
        this.routes = routes;
    }

    @Override
    protected PipeNetWalker createSubWalker(Level world, Direction facingToNextPos, BlockPos nextPos, int walkedBlocks) {
        EnergyNetWalker walker = new EnergyNetWalker(world, nextPos, walkedBlocks, routes);
        walker.loss = loss;
        walker.pipes = pipes;
        return walker;
    }

    @Override
    protected void checkPipe(IPipeTile<?, ?> pipeTile, BlockPos pos) {
        pipes = ArrayUtils.add(pipes, (TileEntityCable) pipeTile);
        loss += ((TileEntityCable) pipeTile).getNodeData().getLossPerBlock();
    }

    @Override
    protected void checkNeighbour(IPipeTile<?, ?> pipeTile, BlockPos pipePos, Direction faceToNeighbour, @Nullable BlockEntity neighbourTile) {
        if (neighbourTile != null) {
            LazyOptional<IEnergyContainer> container = neighbourTile.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, faceToNeighbour.getOpposite());
            if (container.isPresent()) {
                routes.add(new RoutePath(new BlockPos(pipePos), faceToNeighbour, pipes, getWalkedBlocks(), loss));
            }
        }
    }

    @Override
    protected boolean isValidPipe(IPipeTile<?, ?> currentPipe, IPipeTile<?, ?> neighbourPipe, BlockPos pipePos, Direction faceToNeighbour) {
        return neighbourPipe instanceof TileEntityCable;
    }
}
