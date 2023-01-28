package net.nemezanevem.gregtech.api.tileentity;

import com.google.common.base.Preconditions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.ModList;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.block.machine.BlockMachine;
import net.nemezanevem.gregtech.api.cover.CoverBehavior;
import net.nemezanevem.gregtech.api.gui.IUIHolder;
import net.nemezanevem.gregtech.api.registry.tileentity.MetaTileEntityRegistry;
import net.nemezanevem.gregtech.api.tileentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.client.particle.GTNameTagParticle;
import net.nemezanevem.gregtech.client.particle.GTParticleManager;
import net.nemezanevem.gregtech.common.network.packets.PacketRecoverMTE;
import org.checkerframework.checker.units.qual.C;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static net.nemezanevem.gregtech.api.capability.GregtechDataCodes.INITIALIZE_MTE;

public class MetaTileEntityHolder extends TickableTileEntityBase implements IGregTechTileEntity, IUIHolder, Nameable {

    MetaTileEntity metaTileEntity;
    private boolean needToUpdateLightning = false;
    private Component customName;
    private GTNameTagParticle nameTagParticle;

    private final int[] timeStatistics = new int[20];
    private int timeStatisticsIndex = 0;
    private int lagWarningCount = 0;
    protected static final DecimalFormat tricorderFormat = new DecimalFormat("#.#########");

    public MetaTileEntityHolder(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

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
        if (hasLevel() && !level.isClientSide) {
            updateBlockOpacity();
            writeCustomData(INITIALIZE_MTE, buffer -> {
                buffer.writeRegistryId(MetaTileEntityRegistry.META_TILE_ENTITIES_BUILTIN.get(), getMetaTileEntity());
                getMetaTileEntity().writeInitialSyncData(buffer);
            });
            //just to update neighbours so cables and other things will work properly
            this.needToUpdateLightning = true;
            level.neighborChanged(getBlockPos(), getBlockState().getBlock(), getBlockPos());
            setChanged();
        }
        return metaTileEntity;
    }

    protected void setRawMetaTileEntity(MetaTileEntity metaTileEntity) {
        this.metaTileEntity = metaTileEntity;
        this.metaTileEntity.holder = this;
    }

    private void updateBlockOpacity() {
        BlockState currentState = level.getBlockState(getBlockPos());
        boolean isMetaTileEntityOpaque = metaTileEntity.isOpaqueCube();
        if (currentState.getValue(BlockMachine.OPAQUE) != isMetaTileEntityOpaque) {
            level.setBlock(getBlockPos(), currentState.setValue(BlockMachine.OPAQUE, isMetaTileEntityOpaque), 3);
        }
    }

    @Override
    public void notifyBlockUpdate() {
        getLevel().neighborChanged(getBlockState(), worldPosition, getBlockState().getBlock(), worldPosition, false);
    }

    @Override
    public void load(@Nonnull CompoundTag compound) {
        super.load(compound);
        customName = Component.Serializer.fromJson(compound.getString("CustomName"));
        if (compound.contains("MetaId", Tag.TAG_STRING)) {
            String metaTileEntityIdRaw = compound.getString("MetaId");
            ResourceLocation metaTileEntityId = new ResourceLocation(metaTileEntityIdRaw);
            MetaTileEntity sampleMetaTileEntity = MetaTileEntityRegistry.META_TILE_ENTITIES_BUILTIN.get().getValue(metaTileEntityId);
            CompoundTag metaTileEntityData = compound.getCompound("MetaTileEntity");
            if (sampleMetaTileEntity != null) {
                setRawMetaTileEntity(sampleMetaTileEntity.createMetaTileEntity(this));
                /* Note: NBTs need to be read before onAttached is run, since NBTs may contain important information
                * about the composition of the BlockPattern that onAttached may generate. */
                this.metaTileEntity.readFromNBT(metaTileEntityData);
                this.metaTileEntity.onPlacement();
            } else {
                GregTech.LOGGER.error("Failed to load MetaTileEntity with invalid ID " + metaTileEntityIdRaw);
            }
        }
    }

    @Nonnull
    @Override
    public void saveAdditional(@Nonnull CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putString("CustomName", Component.Serializer.toJson(this.getName()));
        if (metaTileEntity != null) {
            compound.putString("MetaId", metaTileEntity.metaTileEntityId.toString());
            CompoundTag metaTileEntityData = new CompoundTag();
            metaTileEntity.writeToNBT(metaTileEntityData);
            compound.put("MetaTileEntity", metaTileEntityData);
        }
    }

    @Override
    public void setRemoved() {
        if (metaTileEntity != null) {
            metaTileEntity.invalidate();
        }

        super.setRemoved();
    }

    @Nullable
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        LazyOptional<T> metaTileEntityValue = metaTileEntity == null ? null : metaTileEntity.getCoverCapability(capability, facing);
        return metaTileEntityValue != null ? metaTileEntityValue : super.getCapability(capability, facing);
    }

    @Override
    public void tick(Level pLevel, BlockPos pPos, BlockState pState, TickableTileEntityBase pBlockEntity) {
        super.tick(pLevel, pPos, pState, pBlockEntity);
        if (!pLevel.isClientSide) {
            for (CoverBehavior coverBehavior : metaTileEntity.coverBehaviors) {
                if (coverBehavior instanceof BlockEntityTicker ticker) {
                    ticker.tick(pLevel, pPos, pState, pBlockEntity);
                }
            }
        }
    }

    @Override
    public void update() {
        long tickTime = System.nanoTime();
        if (metaTileEntity != null) {
            metaTileEntity.tick();
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
        buf.writeUtf(Component.Serializer.toJson(getName()));
        if (metaTileEntity != null) {
            buf.writeBoolean(true);
            buf.writeRegistryId(MetaTileEntityRegistry.META_TILE_ENTITIES_BUILTIN.get(), metaTileEntity);
            metaTileEntity.writeInitialSyncData(buf);
        } else buf.writeBoolean(false);
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        setCustomName(Component.Serializer.fromJson(buf.readUtf()));
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
        setMetaTileEntity(buf.readRegistryId());
        this.metaTileEntity.onPlacement();
        this.metaTileEntity.receiveInitialSyncData(buf);
        scheduleRenderUpdate();
        this.needToUpdateLightning = true;
    }

    @Override
    public boolean isValid() {
        return !super.isRemoved() && metaTileEntity != null;
    }

    @Override
    public boolean isClientSide() {
        return getLevel().isClientSide;
    }

    @Override
    public Level world() {
        return getLevel();
    }

    @Override
    public BlockPos pos() {
        return getBlockPos();
    }

    @Override
    public void markAsDirty() {
        setChanged();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (metaTileEntity != null) {
            metaTileEntity.onLoad();
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (metaTileEntity != null) {
            metaTileEntity.onUnload();
        }
    }

    @Override
    public boolean shouldRefresh(@Nonnull Level world, @Nonnull BlockPos pos, BlockState oldState, BlockState newState) {
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
            rotate(mirrorIn.getRotation(metaTileEntity.getFrontFacing()));
        }
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        if (metaTileEntity == null) return false;
        for (Direction side : Direction.values()) {
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
    public AABB getRenderBoundingBox() {
        if (metaTileEntity instanceof IFastRenderMetaTileEntity) {
            return ((IFastRenderMetaTileEntity) metaTileEntity).getRenderBoundingBox();
        }
        return new AABB(getBlockPos());
    }

    public void setCustomName(Component customName) {
        Component name = getName();
        if (!name.getString().equals(customName.getString())) {
            this.customName = customName;
            if (level.isClientSide) {
                if (hasCustomName()) {
                    if (nameTagParticle == null) {
                        nameTagParticle = new GTNameTagParticle((ClientLevel) level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.5, worldPosition.getZ() + 0.5, name.getString());
                        nameTagParticle.setOnUpdate(p -> {
                            if (isRemoved() || !Util.isPosChunkLoaded(level, worldPosition)) p.remove();
                        });
                        GTParticleManager.INSTANCE.addEffect(nameTagParticle);
                    } else {
                        nameTagParticle.name = name.getString();
                    }
                } else {
                    if (nameTagParticle != null) {
                        nameTagParticle.remove();
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
    public Component getName() {
        return this.customName == null ? Component.empty() : this.customName;
    }

    @Override
    public boolean hasCustomName() {
        return this.customName != null;
    }

    @Nonnull
    @Override
    public Component getDisplayName() {
        return this.hasCustomName() ? this.getName() : metaTileEntity != null ? Component.translatable(metaTileEntity.getMetaFullName()) : this.getName();
    }
}
