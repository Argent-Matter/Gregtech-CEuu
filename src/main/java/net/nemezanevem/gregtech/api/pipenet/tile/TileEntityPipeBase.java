package net.nemezanevem.gregtech.api.pipenet.tile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import net.nemezanevem.gregtech.api.blockentity.SyncedTileEntityBase;
import net.nemezanevem.gregtech.api.capability.GregtechTileCapabilities;
import net.nemezanevem.gregtech.api.cover.CoverBehavior;
import net.nemezanevem.gregtech.api.cover.ICoverable;
import net.nemezanevem.gregtech.api.pipenet.PipeNet;
import net.nemezanevem.gregtech.api.pipenet.WorldPipeNet;
import net.nemezanevem.gregtech.api.pipenet.block.BlockPipe;
import net.nemezanevem.gregtech.api.pipenet.block.IPipeType;
import net.nemezanevem.gregtech.api.registry.GregTechRegistries;
import net.nemezanevem.gregtech.api.registry.material.MaterialRegistry;
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

    public TileEntityPipeBase(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
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
        return this.getBlockState().getBlock() instanceof EntityBlock block && block.getTicker(this.level, this.getBlockState(), this.getType()) != null;
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
        IPipeTile<PipeType, NodeDataType> newTile = getPipeBlock().createNewTileEntity(true, getBlockPos(), getBlockState());
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
        return (connections & 1 << side.ordinal()) > 0;
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
        return (blockedConnections & (1 << side.ordinal())) > 0;
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
                BlockEntity neighbourTile = level.getBlockEntity(worldPosition.offset(facing.getNormal()));
                if (neighbourTile instanceof IPipeTile) {
                    IPipeTile<?, ?> pipeTile = (IPipeTile<?, ?>) neighbourTile;
                    if (pipeTile.isConnected(facing.getOpposite()) && pipeTile.getPipeType().getThickness() < selfThickness) {
                        connections |= 1 << (facing.ordinal() + 6);
                    }
                }
                if (getCoverableImplementation().getCoverAtSide(facing) != null) {
                    connections |= 1 << (facing.ordinal() + 12);
                }
            }
        }
        return connections;
    }

    private LazyOptional<ICoverable> coverableLazyOptional = LazyOptional.of(() -> coverableImplementation);

    public <T> LazyOptional<T> getCapabilityInternal(Capability<T> capability, @Nullable Direction facing) {
        if (capability == GregtechTileCapabilities.CAPABILITY_COVERABLE) {
            return coverableLazyOptional.cast();
        }
        return super.getCapability(capability, facing);
    }

    @Nullable
    @Override
    public final <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        boolean isCoverable = capability == GregtechTileCapabilities.CAPABILITY_COVERABLE;
        CoverBehavior coverBehavior = facing == null ? null : coverableImplementation.getCoverAtSide(facing);
        LazyOptional<T> defaultValue;
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

    @Nonnull
    @Override
    public void saveAdditional(@Nonnull CompoundTag compound) {
        super.saveAdditional(compound);
        BlockPipe<PipeType, NodeDataType, ?> pipeBlock = getPipeBlock();
        if (pipeBlock != null) {
            //noinspection ConstantConditions
            compound.putString("PipeBlock", ForgeRegistries.BLOCKS.getKey(pipeBlock).toString());
        }
        compound.putInt("PipeType", pipeType.ordinal());
        compound.putInt("Connections", connections);
        compound.putInt("BlockedConnections", blockedConnections);
        if (isPainted()) {
            compound.putInt("InsulationColor", paintingColor);
        }
        compound.putString("FrameMaterial", frameMaterial == null ? "" : frameMaterial.toString());
        this.coverableImplementation.writeToNBT(compound);
    }

    @Override
    public void load(@Nonnull CompoundTag compound) {
        super.load(compound);
        if (compound.contains("PipeBlock", Tag.TAG_STRING)) {
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(compound.getString("PipeBlock")));
            //noinspection unchecked
            this.pipeBlock = block instanceof BlockPipe ? (BlockPipe<PipeType, NodeDataType, ?>) block : null;
        }
        this.pipeType = getPipeTypeClass().getEnumConstants()[compound.getInt("PipeType")];

        if (compound.contains("Connections")) {
            connections = compound.getInt("Connections");
        } else if (compound.contains("BlockedConnectionsMap")) {
            connections = 0;
            CompoundTag blockedConnectionsTag = compound.getCompound("BlockedConnectionsMap");
            for (String attachmentTypeKey : blockedConnectionsTag.getAllKeys()) {
                int blockedConnections = blockedConnectionsTag.getInt(attachmentTypeKey);
                connections |= blockedConnections;
            }
        }
        blockedConnections = compound.getInt("BlockedConnections");

        if (compound.contains("InsulationColor")) {
            this.paintingColor = compound.getInt("InsulationColor");
        }
        String frameMaterialName = compound.getString("FrameMaterial");
        if (!frameMaterialName.isEmpty()) {
            this.frameMaterial = MaterialRegistry.MATERIALS_BUILTIN.get().getValue(new ResourceLocation(frameMaterialName));
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
        buf.writeRegistryId(GregTechRegistries.MATERIAL.get(), frameMaterial);
        this.coverableImplementation.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        readPipeProperties(buf);
        this.connections = buf.readVarInt();
        this.blockedConnections = buf.readVarInt();
        this.paintingColor = buf.readInt();
        this.frameMaterial = buf.readRegistryId();
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
            this.frameMaterial = buf.readRegistryId();
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
        BlockPos pos = getBlockPos();
        if(getLevel().isClientSide) {
            LevelRenderer renderer = Minecraft.getInstance().levelRenderer;
            renderer.setBlocksDirty(
                    pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1,
                    pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        }

    }

    @Override
    public void notifyBlockUpdate() {
        getLevel().updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
        getPipeBlock().updateActiveNodeStatus(getLevel(), getBlockPos(), this);
    }

    @Override
    public void markAsDirty() {
        setChanged();
    }

    @Override
    public boolean isValidTile() {
        return !isRemoved();
    }

    /*@Override
    public boolean shouldRefresh(@Nonnull Level world, @Nonnull BlockPos pos, BlockState oldState, BlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }*/

    public void doExplosion(float explosionPower) {
        getLevel().setBlock(getBlockPos(), Blocks.AIR.defaultBlockState(), 3);
        if (!getLevel().isClientSide) {
            ((ServerLevel) getLevel()).sendParticles(ParticleTypes.LARGE_SMOKE, getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5,
                    10, 0.2, 0.2, 0.2, 0.0);
        }
        getLevel().explode(null, getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5,
                explosionPower, Explosion.BlockInteraction.NONE);
    }
}
