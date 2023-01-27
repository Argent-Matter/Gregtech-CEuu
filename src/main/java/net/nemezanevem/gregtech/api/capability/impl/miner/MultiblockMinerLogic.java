package net.nemezanevem.gregtech.api.capability.impl.miner;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.RecipeType;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.block.state.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nonnull;

public class MultiblockMinerLogic extends MinerLogic {

    private static final int CHUNK_LENGTH = 16;

    private final RecipeType<?> blockDropRecipeType;

    private int voltageTier;
    private int overclockAmount = 0;

    private boolean isChunkMode;
    private boolean isSilkTouchMode;

    /**
     * Creates the logic for multiblock ore block miners
     *
     * @param metaTileEntity the {@link MetaTileEntity} this logic belongs to
     * @param fortune        the fortune amount to apply when mining ores
     * @param speed          the speed in ticks per block mined
     * @param maximumRadius  the maximum radius (square shaped) the miner can mine in
     */
    public MultiblockMinerLogic(MetaTileEntity metaTileEntity, int fortune, int speed, int maximumRadius, ICubeRenderer pipeTexture, RecipeType<?> blockDropRecipeType) {
        super(metaTileEntity, fortune, speed, maximumRadius, pipeTexture);
        this.blockDropRecipeType = blockDropRecipeType;
    }

    @Override
    protected boolean drainStorages(boolean simulate) {
        return super.drainStorages(simulate) && miner.drainFluid(simulate);
    }

    @Override
    protected void getSmallOreBlockDrops(NonNullList<ItemStack> blockDrops, WorldServer world, BlockPos blockToMine, BlockState blockState) {
        // Small ores: use (fortune bonus + overclockAmount) value here for fortune, since every overclock increases the yield for small ores
        super.getSmallOreBlockDrops(blockDrops, world, blockToMine, blockState);
    }

    @Override
    protected void getRegularBlockDrops(NonNullList<ItemStack> blockDrops, WorldServer world, BlockPos blockToMine, @Nonnull BlockState blockState) {
        if (!isSilkTouchMode) // 3X the ore compared to the single blocks
            applyTieredHammerNoRandomDrops(blockState, blockDrops, 3, this.blockDropRecipeType, this.voltageTier);
        else
            super.getRegularBlockDrops(blockDrops, world, blockToMine, blockState);
    }

    @Override
    public void initPos(@Nonnull BlockPos pos, int currentRadius) {
        if (!isChunkMode) {
            super.initPos(pos, currentRadius);
        } else {
            WorldServer world = (WorldServer) this.metaTileEntity.getWorld();
            Chunk origin = world.getChunk(this.metaTileEntity.getPos());
            ChunkPos startPos = (world.getChunk(origin.x - currentRadius / CHUNK_LENGTH, origin.z - currentRadius / CHUNK_LENGTH)).getPos();
            getX().set(startPos.getXStart());
            getY().set(this.metaTileEntity.getPos().getY() - 1);
            getZ().set(startPos.getZStart());
            getStartX().set(startPos.getXStart());
            getStartY().set(this.metaTileEntity.getPos().getY());
            getStartZ().set(startPos.getZStart());
            getMineX().set(startPos.getXStart());
            getMineY().set(this.metaTileEntity.getPos().getY() - 1);
            getMineZ().set(startPos.getZStart());
            getPipeY().set(this.metaTileEntity.getPos().getY() - 1);
        }
    }

    public void setVoltageTier(int tier) {
        this.voltageTier = tier;
    }

    public void setOverclockAmount(int amount) {
        this.overclockAmount = amount;
    }

    public int getOverclockAmount() {
        return this.overclockAmount;
    }

    public boolean isChunkMode() {
        return this.isChunkMode;
    }

    public void setChunkMode(boolean isChunkMode) {
        if (!isWorkingEnabled()) {
            getX().set(Integer.MAX_VALUE);
            getY().set(Integer.MAX_VALUE);
            getZ().set(Integer.MAX_VALUE);
            this.isChunkMode = isChunkMode;
        }
    }

    public boolean isSilkTouchMode() {
        return this.isSilkTouchMode;
    }

    public void setSilkTouchMode(boolean isSilkTouchMode) {
        if (!isWorkingEnabled())
            this.isSilkTouchMode = isSilkTouchMode;
    }

    @Override
    public CompoundTag writeToNBT(@Nonnull CompoundTag data) {
        data.setBoolean("isChunkMode", isChunkMode);
        data.setBoolean("isSilkTouchMode", isSilkTouchMode);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(@Nonnull CompoundTag data) {
        this.isChunkMode = data.getBoolean("isChunkMode");
        this.isSilkTouchMode = data.getBoolean("isSilkTouchMode");
        super.readFromNBT(data);
    }

    @Override
    public void writeInitialSyncData(@Nonnull FriendlyByteBuf buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.isChunkMode);
        buf.writeBoolean(this.isSilkTouchMode);
    }

    @Override
    public void receiveInitialSyncData(@Nonnull FriendlyByteBuf buf) {
        super.receiveInitialSyncData(buf);
        this.isChunkMode = buf.readBoolean();
        this.isSilkTouchMode = buf.readBoolean();
    }
}
