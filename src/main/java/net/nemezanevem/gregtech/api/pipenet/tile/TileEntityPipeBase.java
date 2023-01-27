package net.nemezanevem.gregtech.api.pipenet.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.nemezanevem.gregtech.api.pipenet.PipeNet;
import net.nemezanevem.gregtech.api.pipenet.WorldPipeNet;
import net.nemezanevem.gregtech.api.pipenet.block.BlockPipe;
import net.nemezanevem.gregtech.api.pipenet.block.IPipeType;
import net.nemezanevem.gregtech.api.registry.material.MaterialRegistry;
import net.nemezanevem.gregtech.api.tileentity.SyncedTileEntityBase;
import net.nemezanevem.gregtech.api.unification.material.Material;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

import static net.nemezanevem.gregtech.api.capability.GregtechDataCodes.*;

public abstract class TileEntityPipeBase<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType> extends SyncedTileEntityBase implements IPipeTile<PipeType, NodeDataType> {

    protected final PipeCoverableImplementation coverableImplementation = new PipeCoverableImplementation(this);
    protected int paintingColor = -1;
    private int connections = 0;
    private int blockedConnections = 0;
    private NodeDataType cachedNodeData;
    private BlockPipe<PipeType, NodeDataType, ?> pipeBlock;
    private PipeType pipeType = getPipeTypeClass().getEnumConstants()[0];
    @Nullable
    private Material frameMaterial;

    public TileEntityPipeBase() {
    }

    public void setPipeData(BlockPipe<PipeType, NodeDataType, ?> pipeBlock, PipeType pipeType) {
        this.pipeBlock = pipeBlock;
        this.pipeType = pipeType;
        if (!getLevel().isClientSide) {
            writeCustomData(UPDATE_PIPE_TYPE, this::writePipeProperties);
        }
    }

    @Override
    public void transferDataFrom(IPipeTile<PipeType, NodeDataType> tileEntity) {
        this.pipeType = tileEntity.getPipeType();
        this.paintingColor = tileEntity.getPaintingColor();
        this.connections = tileEntity.getConnections();
        if (tileEntity instanceof TileEntityPipeBase) {
            this.updates.putAll(((TileEntityPipeBase<?, ?>) tileEntity).updates);
        }
        tileEntity.getCoverableImplementation().transferDataTo(coverableImplementation);
        setFrameMaterial(tileEntity.getFrameMaterial());
    }

    public abstract Class<PipeType> getPipeTypeClass();

    @Nullable
    @Override
    public Material getFrameMaterial() {
        return frameMaterial;
    }

    public void setFrameMaterial(@Nullable Material frameMaterial) {
        this.frameMaterial = frameMaterial;
        if (level != null && level.isClientSide) {
            writeCustomData(UPDATE_FRAME_MATERIAL, buf -> buf.writeRegistryId(MaterialRegistry.MATERIALS_BUILTIN.get(), frameMaterial));
        }
    }

    @Override
    public boolean supportsTicking() {
        return this instanceof ITickable;
    }

    @Override
    public Level getPipeWorld() {
        return getLevel();
    }

    @Override
    public BlockPos getPipePos() {
        return getBlockPos();
    }

    @Override
    public PipeCoverableImplementation getCoverableImplementation() {
        return coverableImplementation;
    }

    @Override
    public boolean canPlaceCoverOnSide(Direction side) {
        return true;
    }

    @Override
    public IPipeTile<PipeType, NodeDataType> setSupportsTicking() {
        if (supportsTicking()) {
            return this;
        }
        //create new tickable tile entity, transfer data, and replace it
        IPipeTile<PipeType, NodeDataType> newTile = getPipeBlock().createNewTileEntity(true);
        newTile.transferDataFrom(this);
        getLevel().setBlockEntity((BlockEntity) newTile);
        return newTile;
    }

    @Override
    public BlockPipe<PipeType, NodeDataType, ?> getPipeBlock() {
        if (pipeBlock == null) {
            Block block = getBlockState().getBlock();
            //noinspection unchecked
            this.pipeBlock = block instanceof BlockPipe ? (BlockPipe<PipeType, NodeDataType, ?>) block : null;
        }
        return pipeBlock;
    }

    @Override
    public int getConnections() {
        return connections;
    }

    @Override
    public int getBlockedConnections() {
        return canHaveBlockedFaces() ? blockedConnections : 0;
    }

    @Override
    public int getPaintingColor() {
        return isPainted() ? paintingColor : getDefaultPaintingColor();
    }

    @Override
    public void setPaintingColor(int paintingColor) {
        this.paintingColor = paintingColor;
        if (!getLevel().isClientSide) {
            getPipeBlock().getWorldPipeNet(getLevel()).updateMark(getBlockPos(), getCableMark());
            writeCustomData(UPDATE_INSULATION_COLOR, buffer -> buffer.writeInt(paintingColor));
            setChanged();
        }
    }

    @Override
    public boolean isPainted() {
        return this.paintingColor != -1;
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @Override
    public boolean isConnected(Direction side) {
        return (connections & 1 << side.getIndex()) > 0;
    }

    @Override
    public void setConnection(Direction side, boolean connected, boolean fromNeighbor) {
        // fix desync between two connections. Can happen if a pipe side is blocked, and a new pipe is placed next to it.
        if (!getLevel().isClientSide) {
            if (isConnected(side) == connected) {
                return;
            }
            BlockEntity tile = getLevel().getBlockEntity(getBlockPos().offset(side.getNormal()));
            // block connections if Pipe Types do not match
            if (connected && tile instanceof IPipeTile && ((IPipeTile<?, ?>) tile).getPipeType().getClass() != this.getPipeType().getClass()) {
                return;
            }
            connections = withSideConnection(connections, side, connected);

            updateNetworkConnection(side, connected);
            writeCustomData(UPDATE_CONNECTIONS, buffer -> {
                buffer.writeVarInt(connections);
            });
            setChanged();

            if (!fromNeighbor && tile instanceof IPipeTile) {
                syncPipeConnections(side, (IPipeTile<?, ?>) tile);
            }
        }
    }

    private void syncPipeConnections(Direction side, IPipeTile<?, ?> pipe) {
        Direction oppositeSide = side.getOpposite();
        boolean neighbourOpen = pipe.isConnected(oppositeSide);
        if (isConnected(side) == neighbourOpen) {
            return;
        }
        if (!neighbourOpen || pipe.getCoverableImplementation().getCoverAtSide(oppositeSide) == null) {
            pipe.setConnection(oppositeSide, !neighbourOpen, true);
        }
    }

    private void updateNetworkConnection(Direction side, boolean connected) {
        WorldPipeNet<?, ?> worldPipeNet = getPipeBlock().getWorldPipeNet(getLevel());
        worldPipeNet.updateBlockedConnections(getBlockPos(), side, !connected);
    }

    protected int withSideConnection(int blockedConnections, Direction side, boolean connected) {
        int index = 1 << side.ordinal();
        if (connected) {
            return blockedConnections | index;
        } else {
            return blockedConnections & ~index;
        }
    }

    @Override
    public void setFaceBlocked(Direction side, boolean blocked) {
        if (!level.isClientSide && canHaveBlockedFaces()) {
            blockedConnections = withSideConnection(blockedConnections, side, blocked);
            writeCustomData(UPDATE_BLOCKED_CONNECTIONS, buf -> {
                buf.writeVarInt(blockedConnections);
            });
            setChanged();
            WorldPipeNet<?, ?> worldPipeNet = getPipeBlock().getWorldPipeNet(getLevel());
            PipeNet<?> net = worldPipeNet.getNetFromPos(worldPosition);
            if (net != null) {
                net.onPipeConnectionsUpdate();
            }
        }
    }

    @Override
    public boolean isFaceBlocked(Direction side) {
        return (blockedConnections & (1 << side.getIndex())) > 0;
    }

    @Override
    public PipeType getPipeType() {
        return pipeType;
    }

    @Override
    public NodeDataType getNodeData() {
        if (cachedNodeData == null) {
            this.cachedNodeData = getPipeBlock().createProperties(this);
        }
        return cachedNodeData;
    }

    private int getCableMark() {
        return paintingColor == -1 ? 0 : paintingColor;
    }

    /**
     * This returns open connections purely for rendering
     *
     * @return open connections
     */
    public int getVisualConnections() {
        int connections = getConnections();
        float selfThickness = getPipeType().getThickness();
        for (Direction facing : Direction.values()) {
            if (isConnected(facing)) {
                TileEntity neighbourTile = world.getTileEntity(pos.offset(facing));
                if (neighbourTile instanceof IPipeTile) {
                    IPipeTile<?, ?> pipeTile = (IPipeTile<?, ?>) neighbourTile;
                    if (pipeTile.isConnected(facing.getOpposite()) && pipeTile.getPipeType().getThickness() < selfThickness) {
                        connections |= 1 << (facing.getIndex() + 6);
                    }
                }
                if (getCoverableImplementation().getCoverAtSide(facing) != null) {
                    connections |= 1 << (facing.getIndex() + 12);
                }
            }
        }
        return connections;
    }

    public <T> T getCapabilityInternal(Capability<T> capability, @Nullable Direction facing) {
        if (capability == GregtechTileCapabilities.CAPABILITY_COVERABLE) {
            return GregtechTileCapabilities.CAPABILITY_COVERABLE.cast(getCoverableImplementation());
        }
        return super.getCapability(capability, facing);
    }

    @Nullable
    @Override
    public final <T> T getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        boolean isCoverable = capability == GregtechTileCapabilities.CAPABILITY_COVERABLE;
        CoverBehavior coverBehavior = facing == null ? null : coverableImplementation.getCoverAtSide(facing);
        T defaultValue;
        if (getPipeBlock() == null)
            defaultValue = null;
        else
            defaultValue = getCapabilityInternal(capability, facing);

        if (isCoverable) {
            return defaultValue;
        }
        if (coverBehavior == null && facing != null) {
            return isConnected(facing) ? defaultValue : null;
        }
        if (coverBehavior != null) {
            return coverBehavior.getCapability(capability, defaultValue);
        }
        return defaultValue;
    }

    @Override
    public final boolean hasCapability(@Nonnull Capability<?> capability, @Nullable Direction facing) {
        return getCapability(capability, facing) != null;
    }

    @Nonnull
    @Override
    public CompoundTag writeToNBT(@Nonnull CompoundTag compound) {
        super.writeToNBT(compound);
        BlockPipe<PipeType, NodeDataType, ?> pipeBlock = getPipeBlock();
        if (pipeBlock != null) {
            //noinspection ConstantConditions
            compound.setString("PipeBlock", pipeBlock.getRegistryName().toString());
        }
        compound.setInteger("PipeType", pipeType.ordinal());
        compound.setInteger("Connections", connections);
        compound.setInteger("BlockedConnections", blockedConnections);
        if (isPainted()) {
            compound.setInteger("InsulationColor", paintingColor);
        }
        compound.setString("FrameMaterial", frameMaterial == null ? "" : frameMaterial.toString());
        this.coverableImplementation.writeToNBT(compound);
        return compound;
    }

    @Override
    public void readFromNBT(@Nonnull CompoundTag compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("PipeBlock", NBT.TAG_STRING)) {
            Block block = Block.REGISTRY.getObject(new ResourceLocation(compound.getString("PipeBlock")));
            //noinspection unchecked
            this.pipeBlock = block instanceof BlockPipe ? (BlockPipe<PipeType, NodeDataType, ?>) block : null;
        }
        this.pipeType = getPipeTypeClass().getEnumConstants()[compound.getInteger("PipeType")];

        if (compound.hasKey("Connections")) {
            connections = compound.getInteger("Connections");
        } else if (compound.hasKey("BlockedConnectionsMap")) {
            connections = 0;
            CompoundTag blockedConnectionsTag = compound.getCompoundTag("BlockedConnectionsMap");
            for (String attachmentTypeKey : blockedConnectionsTag.getKeySet()) {
                int blockedConnections = blockedConnectionsTag.getInteger(attachmentTypeKey);
                connections |= blockedConnections;
            }
        }
        blockedConnections = compound.getInteger("BlockedConnections");

        if (compound.hasKey("InsulationColor")) {
            this.paintingColor = compound.getInteger("InsulationColor");
        }
        String frameMaterialName = compound.getString("FrameMaterial");
        if (!frameMaterialName.isEmpty()) {
            this.frameMaterial = GregTechAPI.MATERIAL_REGISTRY.getObject(frameMaterialName);
        }
        this.coverableImplementation.readFromNBT(compound);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.coverableImplementation.onLoad();
    }

    protected void writePipeProperties(FriendlyByteBuf buf) {
        buf.writeVarInt(pipeType.ordinal());
    }

    protected void readPipeProperties(FriendlyByteBuf buf) {
        this.pipeType = getPipeTypeClass().getEnumConstants()[buf.readVarInt()];
    }

    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        writePipeProperties(buf);
        buf.writeVarInt(connections);
        buf.writeVarInt(blockedConnections);
        buf.writeInt(paintingColor);
        buf.writeVarInt(frameMaterial == null ? -1 : GregTechAPI.MATERIAL_REGISTRY.getIDForObject(frameMaterial));
        this.coverableImplementation.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        readPipeProperties(buf);
        this.connections = buf.readVarInt();
        this.blockedConnections = buf.readVarInt();
        this.paintingColor = buf.readInt();
        int frameMaterialId = buf.readVarInt();
        this.frameMaterial = frameMaterialId < 0 ? null : GregTechAPI.MATERIAL_REGISTRY.getObjectById(frameMaterialId);
        this.coverableImplementation.readInitialSyncData(buf);
    }

    @Override
    public void receiveCustomData(int discriminator, FriendlyByteBuf buf) {
        if (discriminator == UPDATE_INSULATION_COLOR) {
            this.paintingColor = buf.readInt();
            scheduleChunkForRenderUpdate();
        } else if (discriminator == UPDATE_CONNECTIONS) {
            this.connections = buf.readVarInt();
            scheduleChunkForRenderUpdate();
        } else if (discriminator == SYNC_COVER_IMPLEMENTATION) {
            this.coverableImplementation.readCustomData(buf.readVarInt(), buf);
        } else if (discriminator == UPDATE_PIPE_TYPE) {
            readPipeProperties(buf);
            scheduleChunkForRenderUpdate();
        } else if (discriminator == UPDATE_BLOCKED_CONNECTIONS) {
            this.blockedConnections = buf.readVarInt();
            scheduleChunkForRenderUpdate();
        } else if (discriminator == UPDATE_FRAME_MATERIAL) {
            int frameMaterialId = buf.readVarInt();
            this.frameMaterial = frameMaterialId < 0 ? null : GregTechAPI.MATERIAL_REGISTRY.getObjectById(frameMaterialId);
            scheduleChunkForRenderUpdate();
        }
    }

    @Override
    public void writeCoverCustomData(int id, Consumer<FriendlyByteBuf> writer) {
        writeCustomData(SYNC_COVER_IMPLEMENTATION, buffer -> {
            buffer.writeVarInt(id);
            writer.accept(buffer);
        });
    }

    @Override
    public void scheduleChunkForRenderUpdate() {
        BlockPos pos = getPos();
        getWorld().markBlockRangeForRenderUpdate(
                pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1,
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }

    @Override
    public void notifyBlockUpdate() {
        getWorld().notifyNeighborsOfStateChange(getPos(), getBlockType(), true);
        getPipeBlock().updateActiveNodeStatus(getWorld(), getPos(), this);
    }

    @Override
    public void markAsDirty() {
        setChanged();
    }

    @Override
    public boolean isValidTile() {
        return !isInvalid();
    }

    @Override
    public boolean shouldRefresh(@Nonnull World world, @Nonnull BlockPos pos, BlockState oldState, BlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }

    public void doExplosion(float explosionPower) {
        getWorld().setBlockToAir(getPos());
        if (!getWorld().isClientSide) {
            ((WorldServer) getWorld()).spawnParticle(EnumParticleTypes.SMOKE_LARGE, getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5,
                    10, 0.2, 0.2, 0.2, 0.0);
        }
        getWorld().createExplosion(null, getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5,
                explosionPower, false);
    }
}
