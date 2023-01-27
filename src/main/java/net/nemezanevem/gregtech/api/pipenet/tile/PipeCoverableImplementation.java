package net.nemezanevem.gregtech.api.pipenet.tile;

import com.google.common.base.Preconditions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.capabilities.Capability;
import net.nemezanevem.gregtech.api.cover.CoverBehavior;
import net.nemezanevem.gregtech.api.cover.CoverDefinition;
import net.nemezanevem.gregtech.api.cover.ICoverable;
import net.nemezanevem.gregtech.api.pipenet.block.BlockPipe;
import net.nemezanevem.gregtech.common.ConfigHolder;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

import static net.nemezanevem.gregtech.api.capability.GregtechDataCodes.*;


public class PipeCoverableImplementation implements ICoverable {

    private final IPipeTile<?, ?> holder;
    private final CoverBehavior[] coverBehaviors = new CoverBehavior[6];
    private final int[] sidedRedstoneInput = new int[6];

    public PipeCoverableImplementation(IPipeTile<?, ?> holder) {
        this.holder = holder;
    }

    public void transferDataTo(PipeCoverableImplementation destImpl) {
        for (Direction coverSide : Direction.values()) {
            CoverBehavior behavior = coverBehaviors[coverSide.ordinal()];
            if (behavior == null) continue;
            CompoundTag tagCompound = new CompoundTag();
            behavior.writeToNBT(tagCompound);
            CoverBehavior newBehavior = behavior.getCoverDefinition().createCoverBehavior(destImpl, coverSide);
            newBehavior.readFromNBT(tagCompound);
            destImpl.coverBehaviors[coverSide.ordinal()] = newBehavior;
        }
    }

    public final boolean placeCoverOnSide(Direction side, ItemStack itemStack, CoverDefinition coverDefinition, Player player) {
        if (side == null || coverDefinition == null) {
            return false;
        }
        CoverBehavior coverBehavior = coverDefinition.createCoverBehavior(this, side);
        if (!canPlaceCoverOnSide(side) || !coverBehavior.canAttach()) {
            return false;
        }
        //if cover requires ticking and we're not tickable, update ourselves and redirect call to new tickable tile entity
        boolean requiresTicking = coverBehavior instanceof ITickable;
        if (requiresTicking && !holder.supportsTicking()) {
            IPipeTile<?, ?> newHolderTile = holder.setSupportsTicking();
            return newHolderTile.getCoverableImplementation().placeCoverOnSide(side, itemStack, coverDefinition, player);
        }
        if (coverBehaviors[side.ordinal()] != null) {
            removeCover(side);
        }
        this.coverBehaviors[side.ordinal()] = coverBehavior;
        coverBehavior.onAttached(itemStack, player);
        writeCustomData(COVER_ATTACHED_PIPE, buffer -> {
            buffer.writeByte(side.ordinal());
            buffer.writeVarInt(CoverDefinition.getNetworkIdForCover(coverDefinition));
            coverBehavior.writeInitialSyncData(buffer);
        });
        if (coverBehavior.shouldAutoConnect()) {
            holder.setConnection(side, true, false);
        }
        holder.notifyBlockUpdate();
        holder.markAsDirty();
        AdvancementTriggers.FIRST_COVER_PLACE.trigger((ServerPlayer) player);
        return true;
    }

    public final boolean removeCover(Direction side) {
        Preconditions.checkNotNull(side, "side");
        CoverBehavior coverBehavior = getCoverAtSide(side);
        if (coverBehavior == null) {
            return false;
        }
        List<ItemStack> drops = coverBehavior.getDrops();
        coverBehavior.onRemoved();
        this.coverBehaviors[side.ordinal()] = null;
        for (ItemStack dropStack : drops) {
            Block.popResource(getWorld(), getPos(), dropStack);
        }
        writeCustomData(COVER_REMOVED_PIPE, buffer -> buffer.writeByte(side.ordinal()));
        if (coverBehavior.shouldAutoConnect()) {
            holder.setConnection(side, false, false);
        }
        holder.notifyBlockUpdate();
        holder.markAsDirty();
        return true;
    }

    public final void dropAllCovers() {
        for (Direction coverSide : Direction.VALUES) {
            CoverBehavior coverBehavior = coverBehaviors[coverSide.getIndex()];
            if (coverBehavior == null) continue;
            List<ItemStack> drops = coverBehavior.getDrops();
            coverBehavior.onRemoved();
            for (ItemStack dropStack : drops) {
                Block.spawnAsEntity(getWorld(), getPos(), dropStack);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public ItemStack getStackForm() {
        BlockPipe pipeBlock = holder.getPipeBlock();
        return pipeBlock.getDropItem(holder);
    }

    public void onLoad() {
        for (Direction side : Direction.values()) {
            this.sidedRedstoneInput[side.ordinal()] = GTUtility.getRedstonePower(getWorld(), getPos(), side);
        }
    }

    @Override
    public final int getInputRedstoneSignal(Direction side, boolean ignoreCover) {
        if (!ignoreCover && getCoverAtSide(side) != null) {
            return 0; //covers block input redstone signal for machine
        }
        return sidedRedstoneInput[side.ordinal()];
    }

    public void updateInputRedstoneSignals() {
        for (Direction side : Direction.values()) {
            int redstoneValue = GTUtility.getRedstonePower(getWorld(), getPos(), side);
            int currentValue = sidedRedstoneInput[side.ordinal()];
            if (redstoneValue != currentValue) {
                this.sidedRedstoneInput[side.ordinal()] = redstoneValue;
                CoverBehavior coverBehavior = getCoverAtSide(side);
                if (coverBehavior != null) {
                    coverBehavior.onRedstoneInputSignalChange(redstoneValue);
                }
            }
        }
    }

    @Override
    public void notifyBlockUpdate() {
        holder.notifyBlockUpdate();
    }

    @Override
    public void scheduleRenderUpdate() {
        BlockPos pos = getPos();
        Minecraft.getInstance().levelRenderer.setBlocksDirty(
                pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1,
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }

    @Override
    public double getCoverPlateThickness() {
        float thickness = holder.getPipeType().getThickness();
        // no cover plate for pipes >= 1 block thick
        if (thickness >= 1) return 0;

        // If the available space for the cover is less than the regular cover plate thickness, use that

        // need to divide by 2 because thickness is centered on the block, so the space is half on each side of the pipe
        return Math.min(1.0 / 16.0, (1.0 - thickness) / 2);
    }

    @Override
    public int getPaintingColorForRendering() {
        return ConfigHolder.ClientConfig.defaultPaintingColor;
    }

    @Override
    public boolean shouldRenderBackSide() {
        return false;
    }

    @Override
    public CoverBehavior getCoverAtSide(Direction side) {
        return side == null ? null : coverBehaviors[side.ordinal()];
    }

    @Override
    public boolean canPlaceCoverOnSide(Direction side) {
        return holder.canPlaceCoverOnSide(side);
    }

    public boolean canConnectRedstone(@Nullable Direction side) {
        //so far null side means either upwards or downwards redstone wire connection
        //so check both top cover and bottom cover
        if (side == null) {
            return canConnectRedstone(Direction.UP) ||
                    canConnectRedstone(Direction.DOWN);
        }
        CoverBehavior behavior = getCoverAtSide(side);
        return behavior != null && behavior.canConnectRedstone();
    }

    public int getOutputRedstoneSignal(@Nullable Direction side) {
        if (side == null) {
            return getHighestOutputRedstoneSignal();
        }
        CoverBehavior behavior = getCoverAtSide(side);
        return behavior == null ? 0 : behavior.getRedstoneSignalOutput();
    }

    public int getHighestOutputRedstoneSignal() {
        int highestSignal = 0;
        for (Direction side : Direction.values()) {
            CoverBehavior behavior = getCoverAtSide(side);
            highestSignal = Math.max(highestSignal, behavior.getRedstoneSignalOutput());
        }
        return highestSignal;
    }

    public void update() {
        if (!getWorld().isClientSide) {
            for (CoverBehavior coverBehavior : coverBehaviors) {
                if (coverBehavior instanceof ITickable) {
                    ((ITickable) coverBehavior).update();
                }
            }
        }
    }

    @Override
    public void writeCoverData(CoverBehavior behavior, int id, Consumer<FriendlyByteBuf> writer) {
        writeCustomData(UPDATE_COVER_DATA_PIPE, buffer -> {
            buffer.writeByte(behavior.attachedSide.ordinal());
            buffer.writeVarInt(id);
            writer.accept(buffer);
        });
    }

    public void writeInitialSyncData(FriendlyByteBuf buf) {
        for (Direction coverSide : Direction.values()) {
            CoverBehavior coverBehavior = getCoverAtSide(coverSide);
            if (coverBehavior != null) {
                int coverId = CoverDefinition.getNetworkIdForCover(coverBehavior.getCoverDefinition());
                buf.writeVarInt(coverId);
                coverBehavior.writeInitialSyncData(buf);
            } else {
                // -1 means no cover attached
                buf.writeVarInt(-1);
            }
        }
    }

    public void readInitialSyncData(FriendlyByteBuf buf) {
        for (Direction coverSide : Direction.values()) {
            int coverId = buf.readVarInt();
            if (coverId != -1) {
                CoverDefinition coverDefinition = CoverDefinition.getCoverByNetworkId(coverId);
                CoverBehavior coverBehavior = coverDefinition.createCoverBehavior(this, coverSide);
                coverBehavior.readInitialSyncData(buf);
                this.coverBehaviors[coverSide.ordinal()] = coverBehavior;
            }
        }
    }

    public void writeCustomData(int dataId, Consumer<FriendlyByteBuf> writer) {
        holder.writeCoverCustomData(dataId, writer);
    }

    public void readCustomData(int dataId, FriendlyByteBuf buf) {
        if (dataId == COVER_ATTACHED_PIPE) {
            //cover placement event
            Direction placementSide = Direction.values()[buf.readByte()];
            int coverId = buf.readVarInt();
            CoverDefinition coverDefinition = CoverDefinition.getCoverByNetworkId(coverId);
            CoverBehavior coverBehavior = coverDefinition.createCoverBehavior(this, placementSide);
            this.coverBehaviors[placementSide.ordinal()] = coverBehavior;
            coverBehavior.readInitialSyncData(buf);
            holder.scheduleChunkForRenderUpdate();
        } else if (dataId == COVER_REMOVED_PIPE) {
            //cover removed event
            Direction placementSide = Direction.values()[buf.readByte()];
            this.coverBehaviors[placementSide.ordinal()] = null;
            holder.scheduleChunkForRenderUpdate();
        } else if (dataId == UPDATE_COVER_DATA_PIPE) {
            //cover custom data received
            Direction coverSide = Direction.values()[buf.readByte()];
            CoverBehavior coverBehavior = getCoverAtSide(coverSide);
            int internalId = buf.readVarInt();
            if (coverBehavior != null) {
                coverBehavior.readUpdateData(internalId, buf);
            }
        }
    }

    public void writeToNBT(CompoundTag data) {
        ListTag coversList = new ListTag();
        for (Direction coverSide : Direction.values()) {
            CoverBehavior coverBehavior = coverBehaviors[coverSide.ordinal()];
            if (coverBehavior != null) {
                CompoundTag tagCompound = new CompoundTag();
                ResourceLocation coverId = coverBehavior.getCoverDefinition().getCoverId();
                tagCompound.putString("CoverId", coverId.toString());
                tagCompound.putByte("Side", (byte) coverSide.ordinal());
                coverBehavior.writeToNBT(tagCompound);
                coversList.add(tagCompound);
            }
        }
        data.setTag("Covers", coversList);
    }

    public void readFromNBT(CompoundTag data) {
        ListTag coversList = data.getList("Covers", Tag.TAG_COMPOUND);
        for (int index = 0; index < coversList.size(); index++) {
            CompoundTag tagCompound = coversList.getCompound(index);
            if (tagCompound.contains("CoverId", Tag.TAG_STRING)) {
                Direction coverSide = Direction.values()[tagCompound.getByte("Side")];
                ResourceLocation coverId = new ResourceLocation(tagCompound.getString("CoverId"));
                CoverDefinition coverDefinition = CoverDefinition.getCoverById(coverId);
                CoverBehavior coverBehavior = coverDefinition.createCoverBehavior(this, coverSide);
                coverBehavior.readFromNBT(tagCompound);
                this.coverBehaviors[coverSide.ordinal()] = coverBehavior;
            }
        }
    }

    @Override
    public Level getWorld() {
        return holder.getPipeWorld();
    }

    @Override
    public BlockPos getPos() {
        return holder.getPipePos();
    }

    @Override
    public long getOffsetTimer() {
        return holder.getTickTimer();
    }

    @Override
    public void markDirty() {
        holder.markAsDirty();
    }

    @Override
    public boolean isValid() {
        return holder.isValidTile();
    }

    @Override
    public <T> T getCapability(Capability<T> capability, Direction side) {
        return holder.getCapabilityInternal(capability, side);
    }
}
