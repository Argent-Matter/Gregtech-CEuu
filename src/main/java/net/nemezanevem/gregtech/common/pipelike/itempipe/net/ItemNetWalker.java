package net.nemezanevem.gregtech.common.pipelike.itempipe.net;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.nemezanevem.gregtech.api.cover.CoverBehavior;
import net.nemezanevem.gregtech.api.pipenet.PipeNetWalker;
import net.nemezanevem.gregtech.api.pipenet.tile.IPipeTile;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.ItemPipeProperty;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.common.pipelike.itempipe.tile.TileEntityItemPipe;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Predicate;

public class ItemNetWalker extends PipeNetWalker {

    public static List<ItemPipeNet.Inventory> createNetData(Level world, BlockPos sourcePipe, Direction faceToSourceHandler) {
        ItemNetWalker walker = new ItemNetWalker(world, sourcePipe, 1, new ArrayList<>(), null);
        walker.sourcePipe = sourcePipe;
        walker.facingToHandler = faceToSourceHandler;
        walker.traversePipeNet();
        return walker.isFailed() ? null : walker.inventories;
    }

    private ItemPipeProperty minProperties;
    private final List<ItemPipeNet.Inventory> inventories;
    private final List<Predicate<ItemStack>> filters = new ArrayList<>();
    private final EnumMap<Direction, List<Predicate<ItemStack>>> nextFilters = new EnumMap<>(Direction.class);
    private BlockPos sourcePipe;
    private Direction facingToHandler;

    protected ItemNetWalker(Level world, BlockPos sourcePipe, int distance, List<ItemPipeNet.Inventory> inventories, ItemPipeProperty properties) {
        super(world, sourcePipe, distance);
        this.inventories = inventories;
        this.minProperties = properties;
    }

    @Override
    protected PipeNetWalker createSubWalker(Level world, Direction facingToNextPos, BlockPos nextPos, int walkedBlocks) {
        ItemNetWalker walker = new ItemNetWalker(world, nextPos, walkedBlocks, inventories, minProperties);
        walker.facingToHandler = facingToHandler;
        walker.sourcePipe = sourcePipe;
        walker.filters.addAll(filters);
        List<Predicate<ItemStack>> moreFilters = nextFilters.get(facingToNextPos);
        if (moreFilters != null && !moreFilters.isEmpty()) {
            walker.filters.addAll(moreFilters);
        }
        return walker;
    }

    @Override
    protected void checkPipe(IPipeTile<?, ?> pipeTile, BlockPos pos) {
        for (List<Predicate<ItemStack>> filters : nextFilters.values()) {
            if (!filters.isEmpty()) {
                this.filters.addAll(filters);
            }
        }
        nextFilters.clear();
        ItemPipeProperty pipeProperties = ((TileEntityItemPipe) pipeTile).getNodeData();
        if (minProperties == null) {
            minProperties = pipeProperties;
        } else {
            minProperties = new ItemPipeProperty(minProperties.getPriority() + pipeProperties.getPriority(), Math.min(minProperties.getTransferRate(), pipeProperties.getTransferRate()));
        }
    }

    @Override
    protected void checkNeighbour(IPipeTile<?, ?> pipeTile, BlockPos pipePos, Direction faceToNeighbour, @Nullable BlockEntity neighbourTile) {
        if (neighbourTile == null || (Util.arePosEqual(pipePos, sourcePipe) && faceToNeighbour == facingToHandler)) {
            return;
        }
        LazyOptional<IItemHandler> handler = neighbourTile.getCapability(ForgeCapabilities.ITEM_HANDLER, faceToNeighbour.getOpposite());
        if (handler.isPresent()) {
            List<Predicate<ItemStack>> filters = new ArrayList<>(this.filters);
            List<Predicate<ItemStack>> moreFilters = nextFilters.get(faceToNeighbour);
            if (moreFilters != null && !moreFilters.isEmpty()) {
                filters.addAll(moreFilters);
            }
            inventories.add(new ItemPipeNet.Inventory(new BlockPos(pipePos), faceToNeighbour, getWalkedBlocks(), minProperties, filters));
        }
    }

    @Override
    protected boolean isValidPipe(IPipeTile<?, ?> currentPipe, IPipeTile<?, ?> neighbourPipe, BlockPos pipePos, Direction faceToNeighbour) {
        if (!(neighbourPipe instanceof TileEntityItemPipe)) {
            return false;
        }
        CoverBehavior thisCover = currentPipe.getCoverableImplementation().getCoverAtSide(faceToNeighbour);
        CoverBehavior neighbourCover = neighbourPipe.getCoverableImplementation().getCoverAtSide(faceToNeighbour.getOpposite());
        List<Predicate<ItemStack>> filters = new ArrayList<>();
        if (thisCover instanceof CoverShutter) {
            filters.add(stack -> !thisCover.isValid() || !((CoverShutter) thisCover).isWorkingEnabled());
        } else if (thisCover instanceof CoverItemFilter && ((CoverItemFilter) thisCover).getFilterMode() != ItemFilterMode.FILTER_INSERT) {
            filters.add(((CoverItemFilter) thisCover)::testItemStack);
        }
        if (neighbourCover instanceof CoverShutter) {
            filters.add(stack -> !neighbourCover.isValid() || !((CoverShutter) neighbourCover).isWorkingEnabled());
        } else if (neighbourCover instanceof CoverItemFilter && ((CoverItemFilter) neighbourCover).getFilterMode() != ItemFilterMode.FILTER_EXTRACT) {
            filters.add(((CoverItemFilter) neighbourCover)::testItemStack);
        }
        if (!filters.isEmpty()) {
            nextFilters.put(faceToNeighbour, filters);
        }
        return true;
    }
}
