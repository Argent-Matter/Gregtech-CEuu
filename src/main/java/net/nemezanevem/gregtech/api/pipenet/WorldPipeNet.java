package net.nemezanevem.gregtech.api.pipenet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.nemezanevem.gregtech.api.pipenet.tickable.TickableWorldPipeNet;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.*;

public abstract class WorldPipeNet<NodeDataType, T extends PipeNet<NodeDataType>> extends SavedData {

    private WeakReference<Level> worldRef = new WeakReference<>(null);
    protected List<T> pipeNets = new ArrayList<>();
    protected final Map<ChunkPos, List<T>> pipeNetsByChunk = new HashMap<>();

    public WorldPipeNet() {
        super();
    }

    public Level getWorld() {
        return this.worldRef.get();
    }

    protected void setWorldAndInit(Level world) {
        if (world != this.worldRef.get()) {
            this.worldRef = new WeakReference<>(world);
            onWorldSet();
        }
    }

    public static String getDataID(final String baseID, final Level world) {
        if (world == null || world.isClientSide)
            throw new RuntimeException("WorldPipeNet should only be created on the server!");
        ResourceKey<Level> dimension = world.dimension();
        return dimension == Level.OVERWORLD ? baseID : baseID + '.' + dimension.location();
    }

    protected void onWorldSet() {
        this.pipeNets.forEach(PipeNet::onNodeConnectionsUpdate);
    }

    public void addNode(BlockPos nodePos, NodeDataType nodeData, int mark, int openConnections, boolean isActive) {
        T myPipeNet = null;
        Node<NodeDataType> node = new Node<>(nodeData, openConnections, mark, isActive);
        for (Direction facing : Direction.values()) {
            BlockPos offsetPos = nodePos.offset(facing.getNormal());
            T pipeNet = getNetFromPos(offsetPos);
            Node<NodeDataType> secondNode = pipeNet == null ? null : pipeNet.getAllNodes().get(offsetPos);
            if (pipeNet != null && pipeNet.canAttachNode(nodeData) &&
                    pipeNet.canNodesConnect(secondNode, facing.getOpposite(), node, null)) {
                if (myPipeNet == null) {
                    myPipeNet = pipeNet;
                    myPipeNet.addNode(nodePos, node);
                } else if (myPipeNet != pipeNet) {
                    myPipeNet.uniteNetworks(pipeNet);
                }
            }

        }
        if (myPipeNet == null) {
            myPipeNet = createNetInstance();
            myPipeNet.addNode(nodePos, node);
            addPipeNet(myPipeNet);
            setDirty();
        }
    }

    protected void addPipeNetToChunk(ChunkPos chunkPos, T pipeNet) {
        this.pipeNetsByChunk.computeIfAbsent(chunkPos, any -> new ArrayList<>()).add(pipeNet);
    }

    protected void removePipeNetFromChunk(ChunkPos chunkPos, T pipeNet) {
        List<T> list = this.pipeNetsByChunk.get(chunkPos);
        if (list != null) list.remove(pipeNet);
        if (list.isEmpty()) this.pipeNetsByChunk.remove(chunkPos);
    }

    public void removeNode(BlockPos nodePos) {
        T pipeNet = getNetFromPos(nodePos);
        if (pipeNet != null) {
            pipeNet.removeNode(nodePos);
        }
    }

    public void updateBlockedConnections(BlockPos nodePos, Direction side, boolean isBlocked) {
        T pipeNet = getNetFromPos(nodePos);
        if (pipeNet != null) {
            pipeNet.updateBlockedConnections(nodePos, side, isBlocked);
            pipeNet.onPipeConnectionsUpdate();
        }
    }

    public void updateMark(BlockPos nodePos, int newMark) {
        T pipeNet = getNetFromPos(nodePos);
        if (pipeNet != null) {
            pipeNet.updateMark(nodePos, newMark);
        }
    }

    public T getNetFromPos(BlockPos blockPos) {
        List<T> pipeNetsInChunk = pipeNetsByChunk.getOrDefault(new ChunkPos(blockPos), Collections.emptyList());
        for (T pipeNet : pipeNetsInChunk) {
            if (pipeNet.containsNode(blockPos))
                return pipeNet;
        }
        return null;
    }

    protected void addPipeNet(T pipeNet) {
        addPipeNetSilently(pipeNet);
    }

    protected void addPipeNetSilently(T pipeNet) {
        this.pipeNets.add(pipeNet);
        pipeNet.getContainedChunks().forEach(chunkPos -> addPipeNetToChunk(chunkPos, pipeNet));
        pipeNet.isValid = true;
    }

    protected void removePipeNet(T pipeNet) {
        this.pipeNets.remove(pipeNet);
        pipeNet.getContainedChunks().forEach(chunkPos -> removePipeNetFromChunk(chunkPos, pipeNet));
        pipeNet.isValid = false;
    }

    protected abstract T createNetInstance();

    public void readFromNBT(CompoundTag nbt) {
        this.pipeNets = new ArrayList<>();
        ListTag allEnergyNets = nbt.getList("PipeNets", Tag.TAG_COMPOUND);
        for (int i = 0; i < allEnergyNets.size(); i++) {
            CompoundTag pNetTag = allEnergyNets.getCompound(i);
            T pipeNet = createNetInstance();
            pipeNet.deserializeNBT(pNetTag);
            addPipeNetSilently(pipeNet);
        }
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag compound) {
        ListTag allPipeNets = new ListTag();
        for (T pipeNet : pipeNets) {
            CompoundTag pNetTag = pipeNet.serializeNBT();
            allPipeNets.add(pNetTag);
        }
        compound.put("PipeNets", allPipeNets);
        return compound;
    }
}
