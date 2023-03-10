package net.nemezanevem.gregtech.common.pipelike.itempipe.net;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.nemezanevem.gregtech.api.pipenet.Node;
import net.nemezanevem.gregtech.api.pipenet.PipeNet;
import net.nemezanevem.gregtech.api.pipenet.WorldPipeNet;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.ItemPipeProperty;
import net.nemezanevem.gregtech.api.util.FacingPos;

import java.util.*;
import java.util.function.Predicate;

public class ItemPipeNet extends PipeNet<ItemPipeProperty> {

    private final Map<BlockPos, List<Inventory>> NET_DATA = new HashMap<>();

    public ItemPipeNet(WorldPipeNet<ItemPipeProperty, ? extends PipeNet<ItemPipeProperty>> world) {
        super(world);
    }

    public List<Inventory> getNetData(BlockPos pipePos, Direction facing) {
        List<Inventory> data = NET_DATA.get(pipePos);
        if (data == null) {
            data = ItemNetWalker.createNetData(getWorldData(), pipePos, facing);
            if (data == null) {
                // walker failed, don't cache so it tries again on next insertion
                return Collections.emptyList();
            }
            data.sort(Comparator.comparingInt(inv -> inv.properties.getPriority()));
            NET_DATA.put(pipePos, data);
        }
        return data;
    }

    @Override
    public void onNeighbourUpdate(BlockPos fromPos) {
        NET_DATA.clear();
    }

    @Override
    public void onPipeConnectionsUpdate() {
        NET_DATA.clear();
    }

    @Override
    protected void transferNodeData(Map<BlockPos, Node<ItemPipeProperty>> transferredNodes, PipeNet<ItemPipeProperty> parentNet) {
        super.transferNodeData(transferredNodes, parentNet);
        NET_DATA.clear();
        ((ItemPipeNet) parentNet).NET_DATA.clear();
    }

    @Override
    protected void writeNodeData(ItemPipeProperty nodeData, CompoundTag tagCompound) {
        tagCompound.putInt("Resistance", nodeData.getPriority());
        tagCompound.putFloat("Rate", nodeData.getTransferRate());
    }

    @Override
    protected ItemPipeProperty readNodeData(CompoundTag tagCompound) {
        return new ItemPipeProperty(tagCompound.getInt("Range"), tagCompound.getFloat("Rate"));
    }

    public static class Inventory {
        private final BlockPos pipePos;
        private final Direction faceToHandler;
        private final int distance;
        private final ItemPipeProperty properties;
        private final List<Predicate<ItemStack>> filters;

        public Inventory(BlockPos pipePos, Direction facing, int distance, ItemPipeProperty properties, List<Predicate<ItemStack>> filters) {
            this.pipePos = pipePos;
            this.faceToHandler = facing;
            this.distance = distance;
            this.properties = properties;
            this.filters = filters;
        }

        public BlockPos getPipePos() {
            return pipePos;
        }

        public Direction getFaceToHandler() {
            return faceToHandler;
        }

        public int getDistance() {
            return distance;
        }

        public ItemPipeProperty getProperties() {
            return properties;
        }

        public List<Predicate<ItemStack>> getFilters() {
            return filters;
        }

        public boolean matchesFilters(ItemStack stack) {
            for (Predicate<ItemStack> filter : filters) {
                if (!filter.test(stack)) {
                    return false;
                }
            }
            return true;
        }

        public BlockPos getHandlerPos() {
            return pipePos.offset(faceToHandler.getNormal());
        }

        public IItemHandler getHandler(Level world) {
            BlockEntity tile = world.getBlockEntity(getHandlerPos());
            if (tile != null)
                return tile.getCapability(ForgeCapabilities.ITEM_HANDLER, faceToHandler.getOpposite()).isPresent() ? tile.getCapability(ForgeCapabilities.ITEM_HANDLER, faceToHandler.getOpposite()).resolve().get() : null;
            return null;
        }

        public FacingPos toFacingPos() {
            return new FacingPos(pipePos, faceToHandler);
        }
    }
}
