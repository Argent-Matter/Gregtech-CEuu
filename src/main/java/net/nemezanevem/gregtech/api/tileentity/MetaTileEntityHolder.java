package net.nemezanevem.gregtech.api.tileentity;

import com.google.common.base.Preconditions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.capabilities.Capability;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.block.machine.BlockMachine;
import net.nemezanevem.gregtech.api.gui.IUIHolder;
import net.nemezanevem.gregtech.api.tileentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.client.particle.GTNameTagParticle;
import net.nemezanevem.gregtech.common.network.packets.PacketRecoverMTE;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static net.nemezanevem.gregtech.api.capability.GregtechDataCodes.INITIALIZE_MTE;

public class MetaTileEntityHolder extends TickableTileEntityBase implements IGregTechTileEntity, IUIHolder, Nameable {

    MetaTileEntity metaTileEntity;
    private boolean needToUpdateLightning = false;
    private String customName;
    private GTNameTagParticle nameTagParticle;

    private final int[] timeStatistics = new int[20];
    private int timeStatisticsIndex = 0;
    private int lagWarningCount = 0;
    protected static final DecimalFormat tricorderFormat = new DecimalFormat("#.#########");

    public MetaTileEntity getMetaTileEntity() {
        return metaTileEntity;
    }

    /**
     * Sets this holder's current meta tile entity to copy of given one
     * Note that this method copies given meta tile entity and returns actual instance
     * so it is safe to call it on sample meta tile entities
     * Also can use certain data to preinit the block before data is synced
     */
    @Override
    public MetaTileEntity setMetaTileEntity(MetaTileEntity sampleMetaTileEntity) {
        Preconditions.checkNotNull(sampleMetaTileEntity, "metaTileEntity");
        setRawMetaTileEntity(sampleMetaTileEntity.createMetaTileEntity(this));
        // TODO remove this method call after v2.5.0. This is a deprecated method is set for removal.
        this.metaTileEntity.onPlacement();
        if (hasWorld() && !level().isClientSide) {
            updateBlockOpacity();
            writeCustomData(INITIALIZE_MTE, buffer -> {
                buffer.writeVarInt(GregTechAPI.MTE_REGISTRY.getIdByObjectName(getMetaTileEntity().metaTileEntityId));
                getMetaTileEntity().writeInitialSyncData(buffer);
            });
            //just to update neighbours so cables and other things will work properly
            this.needToUpdateLightning = true;
            world.neighborChanged(getPos(), getBlockType(), getPos());
            markDirty();
        }
        return metaTileEntity;
    }

    protected void setRawMetaTileEntity(MetaTileEntity metaTileEntity) {
        this.metaTileEntity = metaTileEntity;
        this.metaTileEntity.holder = this;
    }

    private void updateBlockOpacity() {
        BlockState currentState = world.getBlockState(getPos());
        boolean isMetaTileEntityOpaque = metaTileEntity.isOpaqueCube();
        if (currentState.getValue(BlockMachine.OPAQUE) != isMetaTileEntityOpaque) {
            world.setBlockState(getPos(), currentState.withProperty(BlockMachine.OPAQUE, isMetaTileEntityOpaque));
        }
    }

    @Override
    public void notifyBlockUpdate() {
        getWorld().notifyNeighborsOfStateChange(pos, getBlockType(), false);
    }

    @Override
    public void readFromNBT(@Nonnull CompoundTag compound) {
        super.readFromNBT(compound);
        customName = compound.getString("CustomName");
        if (compound.hasKey("MetaId", NBT.TAG_STRING)) {
            String metaTileEntityIdRaw = compound.getString("MetaId");
            ResourceLocation metaTileEntityId = new ResourceLocation(metaTileEntityIdRaw);
            MetaTileEntity sampleMetaTileEntity = GregTechAPI.MTE_REGISTRY.getObject(metaTileEntityId);
            CompoundTag metaTileEntityData = compound.getCompoundTag("MetaTileEntity");
            if (sampleMetaTileEntity != null) {
                setRawMetaTileEntity(sampleMetaTileEntity.createMetaTileEntity(this));
                /* Note: NBTs need to be read before onAttached is run, since NBTs may contain important information
                * about the composition of the BlockPattern that onAttached may generate. */
                this.metaTileEntity.readFromNBT(metaTileEntityData);
                // TODO remove this method call after v2.5.0. This is a deprecated method is set for removal.
                this.metaTileEntity.onAttached();
            } else {
                GTLog.logger.error("Failed to load MetaTileEntity with invalid ID " + metaTileEntityIdRaw);
            }
            if (Loader.isModLoaded(GregTech.MODID_APPENG)) {
                readFromNBT_AENetwork(compound);
            }
        }
    }

    @Nonnull
    @Override
    public CompoundTag writeToNBT(@Nonnull CompoundTag compound) {
        super.writeToNBT(compound);
        compound.setString("CustomName", getName());
        if (metaTileEntity != null) {
            compound.setString("MetaId", metaTileEntity.metaTileEntityId.toString());
            CompoundTag metaTileEntityData = new CompoundTag();
            metaTileEntity.writeToNBT(metaTileEntityData);
            compound.setTag("MetaTileEntity", metaTileEntityData);
            if (Loader.isModLoaded(GregTech.MODID_APPENG)) {
                writeToNBT_AENetwork(compound);
            }
        }
        return compound;
    }

    @Override
    public void invalidate() {
        if (metaTileEntity != null) {
            metaTileEntity.invalidate();
        }
        super.invalidate();
        if (Loader.isModLoaded(GregTech.MODID_APPENG)) {
            invalidateAE();
        }
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable Direction facing) {
        Object metaTileEntityValue = metaTileEntity == null ? null : metaTileEntity.getCoverCapability(capability, facing);
        return metaTileEntityValue != null || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        T metaTileEntityValue = metaTileEntity == null ? null : metaTileEntity.getCoverCapability(capability, facing);
        return metaTileEntityValue != null ? metaTileEntityValue : super.getCapability(capability, facing);
    }

    @Override
    public void update() {
        long tickTime = System.nanoTime();
        if (metaTileEntity != null) {
            metaTileEntity.update();
        } else if (level.isClientSide) { // recover the mte
            GregTech.NETWORK_HANDLER.sendToServer(new PacketRecoverMTE(level.dimension(), worldPosition));
        } else { // remove the block
            if (level.getBlockState(worldPosition).getBlock() instanceof BlockMachine) {
                level.setBlock(worldPosition, Blocks.AIR.defaultBlockState(), 3);
            }
        }

        if (this.needToUpdateLightning) {
            level.getLightEngine().getRawBrightness(worldPosition, 0);
            this.needToUpdateLightning = false;
        }

        if (!level.isClientSide && metaTileEntity != null && getMetaTileEntity().isValid()) {
            tickTime = System.nanoTime() - tickTime;
            if (timeStatistics.length > 0) {
                timeStatistics[timeStatisticsIndex] = (int) tickTime;
                timeStatisticsIndex = (timeStatisticsIndex + 1) % timeStatistics.length;
            }
            if (tickTime > 100_000_000L && getMetaTileEntity().doTickProfileMessage() && lagWarningCount++ < 10)
                GregTech.LOGGER.warn("WARNING: Possible Lag Source at [" + worldPosition.getX() + ", " + worldPosition.getY() + ", " + worldPosition.getZ() + "] in Dimension " + level.dimension() + " with " + tickTime + "ns caused by an instance of " + getMetaTileEntity().getClass());
        }

        //increment only after current tick, so meta tile entities will get first tick as timer == 0
        //and update their settings which depend on getTimer() % N properly
        super.update();
    }

    public ArrayList<Component> getDebugInfo(Player player, int logLevel) {
        ArrayList<Component> list = new ArrayList<>();
        if (logLevel > 2) {
            if (isValid()) {
                list.add(Component.translatable("behavior.tricorder.debug_machine",
                        Component.translatable(getMetaTileEntity().metaTileEntityId.toString()).withStyle(ChatFormatting.BLUE),
                        Component.translatable("behavior.tricorder.debug_machine_valid").withStyle(ChatFormatting.GREEN)
                ));
            } else if (metaTileEntity == null) {
                list.add(Component.translatable("behavior.tricorder.debug_machine",
                        Component.translatable("-1").withStyle(ChatFormatting.BLUE),
                        Component.translatable("behavior.tricorder.debug_machine_invalid_null").withStyle(ChatFormatting.RED)
                ));
            } else {
                list.add(Component.translatable("behavior.tricorder.debug_machine",
                        Component.translatable(getMetaTileEntity().metaTileEntityId.toString()).withStyle(ChatFormatting.BLUE),
                        Component.translatable("behavior.tricorder.debug_machine_invalid").withStyle(ChatFormatting.RED)
                ));
            }
        }
        if (logLevel > 1) {
            if (timeStatistics.length > 0) {
                double averageTickTime = 0;
                double worstTickTime = 0;
                for (int tickTime : timeStatistics) {
                    averageTickTime += tickTime;
                    if (tickTime > worstTickTime) {
                        worstTickTime = tickTime;
                    }
                    // Uncomment this line to print out tick-by-tick times.
                    // list.add(Component.translatable("tickTime " + tickTime));
                }
                list.add(Component.translatable("behavior.tricorder.debug_cpu_load",
                        Component.translatable(Util.formatNumbers(averageTickTime / timeStatistics.length)).withStyle(ChatFormatting.YELLOW),
                        Component.translatable(Util.formatNumbers(timeStatistics.length)).withStyle(ChatFormatting.GREEN),
                        Component.translatable(Util.formatNumbers(worstTickTime)).withStyle(ChatFormatting.RED)
                ));
                list.add(Component.translatable("behavior.tricorder.debug_cpu_load_seconds", tricorderFormat.format(worstTickTime / 1000000000)));
            }
            if (lagWarningCount > 0) {
                list.add(Component.translatable("behavior.tricorder.debug_lag_count",
                        Component.translatable(Util.formatNumbers(lagWarningCount)).withStyle(ChatFormatting.RED),
                        Component.translatable(Util.formatNumbers(100_000_000L)).withStyle(ChatFormatting.RED)
                ));
            }
        }
        return list;
    }

    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        buf.writeString(getName());
        if (metaTileEntity != null) {
            buf.writeBoolean(true);
            buf.writeVarInt(GregTechAPI.MTE_REGISTRY.getIdByObjectName(metaTileEntity.metaTileEntityId));
            metaTileEntity.writeInitialSyncData(buf);
        } else buf.writeBoolean(false);
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        setCustomName(buf.readString(Short.MAX_VALUE));
        if (buf.readBoolean()) {
            receiveMTEInitializationData(buf);
        }
    }

    @Override
    public void receiveCustomData(int discriminator, FriendlyByteBuf buffer) {
        if (discriminator == INITIALIZE_MTE) {
            receiveMTEInitializationData(buffer);
        } else if (metaTileEntity != null) {
            metaTileEntity.receiveCustomData(discriminator, buffer);
        }
    }

    /**
     * Sets and initializes the MTE
     *
     * @param buf the buffer to read data from
     */
    private void receiveMTEInitializationData(@Nonnull FriendlyByteBuf buf) {
        int metaTileEntityId = buf.readVarInt();
        setMetaTileEntity(GregTechAPI.MTE_REGISTRY.getObjectById(metaTileEntityId));
        this.metaTileEntity.onPlacement();
        this.metaTileEntity.receiveInitialSyncData(buf);
        scheduleRenderUpdate();
        this.needToUpdateLightning = true;
    }

    @Override
    public boolean isValid() {
        return !super.isInvalid() && metaTileEntity != null;
    }

    @Override
    public boolean isClientSide() {
        return getWorld().isClientSide;
    }

    @Override
    public World world() {
        return getWorld();
    }

    @Override
    public BlockPos pos() {
        return getPos();
    }

    @Override
    public void markAsDirty() {
        markDirty();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (metaTileEntity != null) {
            metaTileEntity.onLoad();
        }
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        if (metaTileEntity != null) {
            metaTileEntity.onUnload();
        }
        if (Loader.isModLoaded(GregTech.MODID_APPENG)) {
            onChunkUnloadAE();
        }
    }

    @Override
    public boolean shouldRefresh(@Nonnull World world, @Nonnull BlockPos pos, BlockState oldState, BlockState newState) {
        return oldState.getBlock() != newState.getBlock(); //MetaTileEntityHolder should never refresh (until block changes)
    }

    @Override
    public void rotate(@Nonnull Rotation rotationIn) {
        if (metaTileEntity != null) {
            metaTileEntity.setFrontFacing(rotationIn.rotate(metaTileEntity.getFrontFacing()));
        }
    }

    @Override
    public void mirror(@Nonnull Mirror mirrorIn) {
        if (metaTileEntity != null) {
            rotate(mirrorIn.toRotation(metaTileEntity.getFrontFacing()));
        }
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        if (metaTileEntity == null) return false;
        for (Direction side : Direction.VALUES) {
            CoverBehavior cover = metaTileEntity.getCoverAtSide(side);
            if (cover instanceof IFastRenderMetaTileEntity && ((IFastRenderMetaTileEntity) cover).shouldRenderInPass(pass)) {
                return true;
            }
        }
        if (metaTileEntity instanceof IFastRenderMetaTileEntity) {
            return ((IFastRenderMetaTileEntity) metaTileEntity).shouldRenderInPass(pass);
        }
        return false;
    }

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (metaTileEntity instanceof IFastRenderMetaTileEntity) {
            return ((IFastRenderMetaTileEntity) metaTileEntity).getRenderBoundingBox();
        }
        return new AxisAlignedBB(getPos());
    }

    @Override
    public boolean canRenderBreaking() {
        return false;
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }

    public boolean hasTESR() {
        if (metaTileEntity == null) return false;
        if (metaTileEntity instanceof IFastRenderMetaTileEntity) {
            return true;
        }
        for (Direction side : Direction.VALUES) {
            CoverBehavior cover = metaTileEntity.getCoverAtSide(side);
            if (cover instanceof IFastRenderMetaTileEntity) {
                return true;
            }
        }
        return false;
    }

    public void setCustomName(String customName) {
        if (!getName().equals(customName)) {
            this.customName = customName;
            if (level.isClientSide) {
                if (hasCustomName()) {
                    if (nameTagParticle == null) {
                        nameTagParticle = new GTNameTagParticle((ClientLevel) level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.5, worldPosition.getZ() + 0.5, getName());
                        nameTagParticle.setOnUpdate(p -> {
                            if (isRemoved() || !Util.isPosChunkLoaded(level, worldPosition)) p.remove();
                        });
                        GTParticleManager.INSTANCE.addEffect(nameTagParticle);
                    } else {
                        nameTagParticle.name = getName();
                    }
                } else {
                    if (nameTagParticle != null) {
                        nameTagParticle.setExpired();
                        nameTagParticle = null;
                    }
                }
            } else {
                markAsDirty();
            }
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return this.customName == null ? "" : this.customName;
    }

    @Override
    public boolean hasCustomName() {
        return this.customName != null && !this.customName.isEmpty();
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return this.hasCustomName() ? new TextComponentString(this.getName()) : metaTileEntity != null ? Component.translatable(metaTileEntity.getMetaFullName()) : new TextComponentString(this.getName());
    }

    @Nullable
    @Override
    @Method(modid = GregTech.MODID_APPENG)
    public IGridNode getGridNode(@Nonnull AEPartLocation part) {
        AENetworkProxy proxy = getProxy();
        return proxy == null ? null : proxy.getNode();
    }

    @Nonnull
    @Override
    @Method(modid = GregTech.MODID_APPENG)
    public AECableType getCableConnectionType(@Nonnull AEPartLocation part) {
        return metaTileEntity == null ? AECableType.NONE : metaTileEntity.getCableConnectionType(part);
    }

    @Override
    @Method(modid = GregTech.MODID_APPENG)
    public void securityBreak() {}

    @Nonnull
    @Override
    @Method(modid = GregTech.MODID_APPENG)
    public IGridNode getActionableNode() {
        AENetworkProxy proxy = getProxy();
        return proxy == null ? null : proxy.getNode();
    }

    @Override
    @Method(modid = GregTech.MODID_APPENG)
    public AENetworkProxy getProxy() {
        return metaTileEntity == null ? null : metaTileEntity.getProxy();
    }

    @Override
    @Method(modid = GregTech.MODID_APPENG)
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Override
    @Method(modid = GregTech.MODID_APPENG)
    public void gridChanged() {
        if (metaTileEntity != null) {
            metaTileEntity.gridChanged();
        }
    }

    @Method(modid = GregTech.MODID_APPENG)
    public void readFromNBT_AENetwork(CompoundTag data) {
        AENetworkProxy proxy = getProxy();
        if (proxy != null) {
            proxy.readFromNBT(data);
        }
    }

    @Method(modid = GregTech.MODID_APPENG)
    public void writeToNBT_AENetwork(CompoundTag data) {
        AENetworkProxy proxy = getProxy();
        if (proxy != null) {
            proxy.writeToNBT(data);
        }
    }

    @Method(modid = GregTech.MODID_APPENG)
    void onChunkUnloadAE() {
        AENetworkProxy proxy = getProxy();
        if (proxy != null) {
            proxy.onChunkUnload();
        }
    }

    @Method(modid = GregTech.MODID_APPENG)
    void invalidateAE() {
        AENetworkProxy proxy = getProxy();
        if (proxy != null) {
            proxy.invalidate();
        }
    }
}
