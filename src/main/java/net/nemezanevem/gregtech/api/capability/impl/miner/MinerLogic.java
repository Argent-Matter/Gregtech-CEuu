package net.nemezanevem.gregtech.api.capability.impl.miner;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.capability.GregtechDataCodes;
import net.nemezanevem.gregtech.api.capability.IMiner;
import net.nemezanevem.gregtech.api.recipe.GTRecipe;
import net.nemezanevem.gregtech.api.recipe.GTRecipeType;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.unification.material.TagUnifier;
import net.nemezanevem.gregtech.api.unification.tag.TagPrefix;
import net.nemezanevem.gregtech.api.util.GTTransferUtils;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.client.renderer.ICubeRenderer;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import net.nemezanevem.gregtech.common.ConfigHolder;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MinerLogic {

    private static final short MAX_SPEED = Short.MAX_VALUE;
    private static final byte POWER = 5;
    private static final byte TICK_TOLERANCE = 20;
    private static final double DIVIDEND = MAX_SPEED * Math.pow(TICK_TOLERANCE, POWER);

    protected final MetaTileEntity metaTileEntity;
    protected final IMiner miner;

    private final int fortune;
    private final int speed;
    private final int maximumRadius;

    private final ICubeRenderer PIPE_TEXTURE;

    private final LinkedList<BlockPos> blocksToMine = new LinkedList<>();

    private final AtomicInteger x = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger y = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger z = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger startX = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger startZ = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger startY = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger pipeY = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger mineX = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger mineZ = new AtomicInteger(Integer.MAX_VALUE);
    private final AtomicInteger mineY = new AtomicInteger(Integer.MAX_VALUE);

    private int pipeLength = 0;
    private int currentRadius;
    private boolean isDone;
    private boolean isActive = false;
    private boolean isWorkingEnabled = true;
    protected boolean wasActiveAndNeedsUpdate;

    private final BlockState oreReplacementBlock = findMiningReplacementBlock();

    /**
     * Creates the general logic for all in-world ore block miners
     *
     * @param metaTileEntity the {@link MetaTileEntity} this logic belongs to
     * @param fortune the fortune amount to apply when mining ores
     * @param speed the speed in ticks per block mined
     * @param maximumRadius the maximum radius (square shaped) the miner can mine in
     */
    public MinerLogic(@Nonnull MetaTileEntity metaTileEntity, int fortune, int speed, int maximumRadius, ICubeRenderer pipeTexture) {
        this.metaTileEntity = metaTileEntity;
        this.miner = (IMiner) metaTileEntity;
        this.fortune = fortune;
        this.speed = speed;
        this.currentRadius = maximumRadius;
        this.maximumRadius = maximumRadius;
        this.isDone = false;
        this.PIPE_TEXTURE = pipeTexture;
    }

    private BlockState findMiningReplacementBlock() {

        String blockDescription = ConfigHolder.machines.replaceMinedBlocksWith;
        Block replacementBlock;

        if(blockDescription.matches(".*\\[.*\\]")) {
            DataResult<Pair<BlockState, JsonElement>> dataResult = BlockState.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(blockDescription));
            if(dataResult.result().isPresent()) {
                return dataResult.result().get().getFirst();
            }
        }

        replacementBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockDescription));
        if(replacementBlock == null) {
            GregTech.LOGGER.error("Miner Config Replacement block was null, replacing with Cobblestone");
            return Blocks.COBBLESTONE.defaultBlockState();
        }

        return replacementBlock.defaultBlockState();
    }

    /**
     * Performs the actual mining in world
     * Call this method every tick in update
     */
    public void performMining() {
        // Needs to be server side
        if (metaTileEntity.getWorld().isClientSide)
            return;

        // Inactive miners do nothing
        if (!this.isWorkingEnabled)
            return;

        // check if mining is possible
        if (!checkCanMine())
            return;

        // if the inventory is not full, drain energy etc. from the miner
        // the storages have already been checked earlier
        if (!miner.isInventoryFull()) {
            // always drain storages when working, even if blocksToMine ends up being empty
            drainStorages(false);

            // since energy is being consumed the miner is now active
            if (!this.isActive)
                setActive(true);
        } else {
            // the miner cannot drain, therefore it is inactive
            if (this.isActive)
                setActive(false);
        }

        // drill a hole beneath the miner and extend the pipe downwards by one
        ServerLevel world = (ServerLevel) metaTileEntity.getWorld();
        if (mineY.get() < pipeY.get()) {
            world.destroyBlock(new BlockPos(metaTileEntity.getPos().getX(), pipeY.get(), metaTileEntity.getPos().getZ()), false);
            pipeY.decrementAndGet();
            incrementPipeLength();
        }

        // check if the miner needs new blocks to mine and get them if needed
        checkBlocksToMine();

        // if there are blocks to mine and the correct amount of time has passed, do the mining
        if (metaTileEntity.getOffsetTimer() % this.speed == 0 && !blocksToMine.isEmpty()) {
            NonNullList<ItemStack> blockDrops = NonNullList.create();
            BlockState blockState = metaTileEntity.getWorld().getBlockState(blocksToMine.getFirst());

            // check to make sure the ore is still there,
            while(!Util.isOre(new ItemStack(blockState.getBlock()))) {
                blocksToMine.removeFirst();
                if (blocksToMine.isEmpty()) break;
                blockState = metaTileEntity.getWorld().getBlockState(blocksToMine.getFirst());
            }
            // When we are here we have an ore to mine! I'm glad we aren't threaded
            if (!blocksToMine.isEmpty() & Util.isOre(new ItemStack(blockState.getBlock()))) {
                // get the small ore drops, if a small ore
                getSmallOreBlockDrops(blockDrops, world, blocksToMine.getFirst(), blockState);
                // get the block's drops.
                getRegularBlockDrops(blockDrops, world, blocksToMine.getFirst(), blockState);
                // try to insert them
                mineAndInsertItems(blockDrops, world);
            }

        }

        if (blocksToMine.isEmpty()) {
            // there were no blocks to mine, so the current position is the previous position
            x.set(mineX.get());
            y.set(mineY.get());
            z.set(mineZ.get());

            // attempt to get more blocks to mine, if there are none, the miner is done mining
            blocksToMine.addAll(getBlocksToMine());
            if (blocksToMine.isEmpty()) {
                this.isDone = true;
                this.wasActiveAndNeedsUpdate = true;
                this.setActive(false);
            }
        }
    }

    /**
     *
     * @return true if the miner is able to mine, else false
     */
    protected boolean checkCanMine() {
        // if the miner is finished, the target coordinates are invalid, or it cannot drain storages, stop
        if (checkShouldStop()) {
            // if the miner is not finished and has invalid coordinates, get new and valid starting coordinates
            if (!isDone && checkCoordinatesInvalid(x, y, z))
                initPos(metaTileEntity.getPos(), currentRadius);

            // don't do anything else this time
            return false;
        }
        return true;
    }

    protected boolean checkShouldStop() {
        return isDone || checkCoordinatesInvalid(x, y, z) || !drainStorages(true);
    }

    /**
     * Called after each block is mined, used to perform additional actions afterwards
     */
    protected void onMineOperation() {

    }

    /**
     * called in order to drain anything the miner needs to drain in order to run
     * only drains energy by default
     */
    protected boolean drainStorages(boolean simulate) {
        return miner.drainEnergy(simulate);
    }

    /**
     * called to handle mining small ores
     * @param blockDrops the List of items to fill after the operation
     * @param world the {@link ServerLevel} the miner is in
     * @param blockToMine the {@link BlockPos} of the block being mined
     * @param blockState the {@link BlockState} of the block being mined
     */
    protected void getSmallOreBlockDrops(NonNullList<ItemStack> blockDrops, ServerLevel world, BlockPos blockToMine, BlockState blockState) {
        /*small ores
            if orePrefix of block in blockPos is small
                applyTieredHammerNoRandomDrops...
            else
                current code...
        */
    }

    /**
     * called to handle mining regular ores and blocks
     * @param blockDrops the List of items to fill after the operation
     * @param world the {@link ServerLevel} the miner is in
     * @param blockToMine the {@link BlockPos} of the block being mined
     * @param blockState the {@link BlockState} of the block being mined
     */
    protected void getRegularBlockDrops(NonNullList<ItemStack> blockDrops, ServerLevel world, BlockPos blockToMine, @Nonnull BlockState blockState) {
        blockDrops.addAll(Block.getDrops(blockState, world, blockToMine, world.getBlockEntity(blockToMine))); // regular ores do not get fortune applied
    }

    /**
     * called in order to insert the mined items into the inventory and actually remove the block in world
     * marks the inventory as full if the items cannot fit, and not full if it previously was full and items could fit
     *
     * @param blockDrops the List of items to insert
     * @param world the {@link ServerLevel} the miner is in
     */
    private void mineAndInsertItems(NonNullList<ItemStack> blockDrops, ServerLevel world) {
        // If the block's drops can fit in the inventory, move the previously mined position to the block
        // replace the ore block with cobblestone instead of breaking it to prevent mob spawning
        // remove the ore block's position from the mining queue
        if (GTTransferUtils.addItemsToItemHandler(metaTileEntity.getExportItems(), true, blockDrops)) {
            GTTransferUtils.addItemsToItemHandler(metaTileEntity.getExportItems(), false, blockDrops);
            world.setBlock(blocksToMine.getFirst(), oreReplacementBlock, 3);
            mineX.set(blocksToMine.getFirst().getX());
            mineZ.set(blocksToMine.getFirst().getZ());
            mineY.set(blocksToMine.getFirst().getY());
            blocksToMine.removeFirst();
            onMineOperation();

            // if the inventory was previously considered full, mark it as not since an item was able to fit
            if (miner.isInventoryFull())
                miner.setInventoryFull(false);
        } else {
            // the ore block was not able to fit, so the inventory is considered full
            miner.setInventoryFull(true);
        }
    }

    /**
     * This method designates the starting position for mining blocks
     *
     * @param pos the {@link BlockPos} of the miner itself
     * @param currentRadius the currently set mining radius
     */
    public void initPos(@Nonnull BlockPos pos, int currentRadius) {
        x.set(pos.getX() - currentRadius);
        z.set(pos.getZ() - currentRadius);
        y.set(pos.getY() - 1);
        startX.set(pos.getX() - currentRadius);
        startZ.set(pos.getZ() - currentRadius);
        startY.set(pos.getY());
        pipeY.set(pos.getY() - 1);
        mineX.set(pos.getX() - currentRadius);
        mineZ.set(pos.getZ() - currentRadius);
        mineY.set(pos.getY() - 1);
    }

    /**
     * Checks if the current coordinates are invalid
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return {@code true} if the coordinates are invalid, else false
     */
    private static boolean checkCoordinatesInvalid(@Nonnull AtomicInteger x, @Nonnull AtomicInteger y, @Nonnull AtomicInteger z) {
        return x.get() == Integer.MAX_VALUE && y.get() == Integer.MAX_VALUE && z.get() == Integer.MAX_VALUE;
    }

    /**
     * Checks whether there are any more blocks to mine, if there are currently none queued
     */
    public void checkBlocksToMine() {
        if (blocksToMine.isEmpty())
            blocksToMine.addAll(getBlocksToMine());
    }

    /**
     * Recalculates the mining area and refills the block list
     */
    public void resetArea() {
        initPos(metaTileEntity.getPos(), currentRadius);
        blocksToMine.clear();
        checkBlocksToMine();
    }

    /**
     * Gets the blocks to mine
     * @return a {@link LinkedList} of {@link BlockPos} for each ore to mine
     */
    private LinkedList<BlockPos> getBlocksToMine() {
        LinkedList<BlockPos> blocks = new LinkedList<>();

        // determine how many blocks to retrieve this time
        double quotient = getQuotient(Util.getMeanTickTime(metaTileEntity.getWorld()));
        int calcAmount = quotient < 1 ? 1 : (int) (Math.min(quotient, Short.MAX_VALUE));
        int calculated = 0;

        // keep getting blocks until the target amount is reached
        while (calculated < calcAmount) {
            // moving down the y-axis
            if (y.get() > 0) {
                // moving across the z-axis
                if (z.get() <= startZ.get() + currentRadius * 2) {
                    // check every block along the x-axis
                    if (x.get() <= startX.get() + currentRadius * 2) {
                        BlockPos blockPos = new BlockPos(x.get(), y.get(), z.get());
                        BlockState state = metaTileEntity.getWorld().getBlockState(blockPos);
                        if (state.getBlock().defaultDestroyTime() >= 0 && metaTileEntity.getWorld().getBlockEntity(blockPos) == null && Util.isOre(new ItemStack(state.getBlock()))) {
                            blocks.addLast(blockPos);
                        }
                        // move to the next x position
                        x.incrementAndGet();
                    } else {
                        // reset x and move to the next z layer
                        x.set(startX.get());
                        z.incrementAndGet();
                    }
                } else {
                    // reset z and move to the next y layer
                    z.set(startZ.get());
                    y.decrementAndGet();
                }
            } else
                return blocks;

            // only count iterations where blocks were found
            if (!blocks.isEmpty())
                calculated++;
        }
        return blocks;
    }

    /**
     * gets the quotient for determining the amount of blocks to mine
     * @param base is a value used for calculation, intended to be the mean tick time of the world the miner is in
     * @return the quotient
     */
    private static double getQuotient(double base) {
        return DIVIDEND / Math.pow(base, POWER);
    }

    /**
     * Applies a fortune hammer to block drops based on a tier value, intended for small ores
     * @param blockState the block being mined
     * @param drops where the drops are stored to
     * @param fortuneLevel the level of fortune used
     * @param map the recipemap from which to get the drops
     * @param tier the tier at which the operation is performed, used for calculating the chanced output boost
     */
    protected static void applyTieredHammerNoRandomDrops(@Nonnull BlockState blockState, List<ItemStack> drops, int fortuneLevel, @Nonnull GTRecipeType<?> map, int tier) {
        ItemStack itemStack = new ItemStack(blockState.getBlock());
        GTRecipe recipe = map.findRecipe(Long.MAX_VALUE, Collections.singletonList(itemStack), Collections.emptyList(), 0);
        if (recipe != null && !recipe.getOutputs().isEmpty()) {
            drops.clear();
            for (ItemStack outputStack : recipe.getResultItemOutputs(Util.getTierByVoltage(recipe.getEUt()), tier, map)) {
                outputStack = outputStack.copy();
                if (TagUnifier.getPrefix(outputStack.getItem()) == TagPrefix.crushed) {
                    if (fortuneLevel > 0) {
                        outputStack.grow(outputStack.getCount() * fortuneLevel);
                    }
                }
                drops.add(outputStack);
            }
        }
    }

    /**
     * Increments the pipe rendering length by one, signaling that the miner's y level has moved down by one
     */
    private void incrementPipeLength() {
        this.pipeLength++;
        this.metaTileEntity.writeCustomData(GregtechDataCodes.PUMP_HEAD_LEVEL, b -> b.writeInt(pipeLength));
        this.metaTileEntity.markDirty();
    }

    /**
     * renders the pipe beneath the miner
     */
    public void renderPipe(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Textures.PIPE_IN_OVERLAY.renderSided(Direction.DOWN, renderState, translation, pipeline);
        for (int i = 0; i < this.pipeLength; i++) {
            translation.translate(0.0, -1.0, 0.0);
            PIPE_TEXTURE.render(renderState, translation, pipeline, IMiner.PIPE_CUBOID);
        }
    }

    /**
     * writes all needed values to NBT
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#writeToNBT(CompoundTag)} method
     */
    public CompoundTag writeToNBT(@Nonnull CompoundTag data) {
        data.putInt("xPos", this.x.get());
        data.putInt("yPos", this.y.get());
        data.putInt("zPos", this.z.get());
        data.putInt("mxPos", this.mineX.get());
        data.putInt("myPos", this.mineY.get());
        data.putInt("mzPos", this.mineZ.get());
        data.putInt("sxPos", this.startX.get());
        data.putInt("syPos", this.startY.get());
        data.putInt("szPos", this.startZ.get());
        data.putInt("tempY", this.pipeY.get());
        data.putBoolean("isActive", this.isActive);
        data.putBoolean("isWorkingEnabled", this.isWorkingEnabled);
        data.putBoolean("wasActiveAndNeedsUpdate", this.wasActiveAndNeedsUpdate);
        data.putInt("pipeLength", this.pipeLength);
        data.putInt("currentRadius", this.currentRadius);
        data.putBoolean("isDone", this.isDone);
        return data;
    }

    /**
     * reads all needed values from NBT
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#readFromNBT(CompoundTag)} method
     */
    public void readFromNBT(@Nonnull CompoundTag data) {
        x.set(data.getInt("xPos"));
        y.set(data.getInt("yPos"));
        z.set(data.getInt("zPos"));
        mineX.set(data.getInt("mxPos"));
        mineY.set(data.getInt("myPos"));
        mineZ.set(data.getInt("mzPos"));
        startX.set(data.getInt("sxPos"));
        startY.set(data.getInt("syPos"));
        startZ.set(data.getInt("szPos"));
        pipeY.set(data.getInt("tempY"));
        setActive(data.getBoolean("isActive"));
        setWorkingEnabled(data.getBoolean("isWorkingEnabled"));
        setWasActiveAndNeedsUpdate(data.getBoolean("wasActiveAndNeedsUpdate"));
        pipeLength = data.getInt("pipeLength");
        this.currentRadius = data.getInt("currentRadius");
        this.isDone = data.getBoolean("isDone");
    }

    /**
     * writes all needed values to InitialSyncData
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#writeInitialSyncData(FriendlyByteBuf)} method
     */
    public void writeInitialSyncData(@Nonnull FriendlyByteBuf buf) {
        buf.writeInt(pipeLength);
        buf.writeBoolean(this.isActive);
        buf.writeBoolean(this.isWorkingEnabled);
        buf.writeBoolean(this.wasActiveAndNeedsUpdate);
    }

    /**
     * reads all needed values from InitialSyncData
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#receiveInitialSyncData(FriendlyByteBuf)} method
     */
    public void receiveInitialSyncData(@Nonnull FriendlyByteBuf buf) {
        this.pipeLength = buf.readInt();
        setActive(buf.readBoolean());
        setWorkingEnabled(buf.readBoolean());
        setWasActiveAndNeedsUpdate(buf.readBoolean());
    }

    /**
     * reads all needed values from CustomData
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#receiveCustomData(int, FriendlyByteBuf)} method
     */
    public void receiveCustomData(int dataId, FriendlyByteBuf buf) {
        if (dataId == GregtechDataCodes.PUMP_HEAD_LEVEL) {
            this.pipeLength = buf.readInt();
            metaTileEntity.scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            this.isActive = buf.readBoolean();
            metaTileEntity.scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.isWorkingEnabled = buf.readBoolean();
            metaTileEntity.scheduleRenderUpdate();
        }
    }

    /**
     *
     * @return the current x value
     */
    public AtomicInteger getX() {
        return x;
    }

    /**
     *
     * @return the current y value
     */
    public AtomicInteger getY() {
        return y;
    }

    /**
     *
     * @return the current z value
     */
    public AtomicInteger getZ() {
        return z;
    }

    /**
     *
     * @return the previously mined x value
     */
    public AtomicInteger getMineX() {
        return mineX;
    }

    /**
     *
     * @return the previously mined y value
     */
    public AtomicInteger getMineY() {
        return mineY;
    }

    /**
     *
     * @return the previously mined z value
     */
    public AtomicInteger getMineZ() {
        return mineZ;
    }

    /**
     *
     * @return the starting x value
     */
    public AtomicInteger getStartX() {
        return startX;
    }

    /**
     *
     * @return the starting y value
     */
    public AtomicInteger getStartY() {
        return startY;
    }

    /**
     *
     * @return the starting z value
     */
    public AtomicInteger getStartZ() {
        return startZ;
    }

    /**
     *
     * @return the pipe y value
     */
    public AtomicInteger getPipeY() {
        return pipeY;
    }

    /**
     *
     * @return the miner's maximum radius
     */
    public int getMaximumRadius() {
        return this.maximumRadius;
    }

    /**
     *
     * @return the miner's current radius
     */
    public int getCurrentRadius() {
        return this.currentRadius;
    }

    /**
     *
     * @param currentRadius the radius to set the miner to use
     */
    public void setCurrentRadius(int currentRadius) {
        this.currentRadius = currentRadius;
    }

    /**
     *
     * @return true if the miner is finished working
     */
    public boolean isDone() {
        return this.isDone;
    }

    /**
     *
     * @return true if the miner is active
     */
    public boolean isActive() {
        return this.isActive;
    }

    /**
     *
     * @param isActive the new state of the miner's activity: true to change to active, else false
     */
    public void setActive(boolean isActive) {
        if (this.isActive != isActive) {
            this.isActive = isActive;
            this.metaTileEntity.markDirty();
            if (metaTileEntity.getWorld() != null && !metaTileEntity.getWorld().isClientSide) {
                this.metaTileEntity.writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(isActive));
            }
        }
    }

    /**
     *
     * @param isWorkingEnabled the new state of the miner's ability to work: true to change to enabled, else false
     */
    public void setWorkingEnabled(boolean isWorkingEnabled) {
        if (this.isWorkingEnabled != isWorkingEnabled) {
            this.isWorkingEnabled = isWorkingEnabled;
            metaTileEntity.markDirty();
            if (metaTileEntity.getWorld() != null && !metaTileEntity.getWorld().isClientSide) {
                this.metaTileEntity.writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(isWorkingEnabled));
            }
        }
    }

    /**
     *
     * @return whether working is enabled for the logic
     */
    public boolean isWorkingEnabled() {
        return isWorkingEnabled;
    }

    /**
     *
     * @return whether the miner is currently working
     */
    public boolean isWorking() {
        return isActive && isWorkingEnabled;
    }

    /**
     *
     * @return whether the miner was active and needs an update
     */
    public boolean wasActiveAndNeedsUpdate() {
        return this.wasActiveAndNeedsUpdate;
    }

    /**
     * set whether the miner was active and needs an update
     *
     * @param wasActiveAndNeedsUpdate the state to set
     */
    public void setWasActiveAndNeedsUpdate(boolean wasActiveAndNeedsUpdate) {
        this.wasActiveAndNeedsUpdate = wasActiveAndNeedsUpdate;
    }

    /**
     *
     * @return the miner's fortune level
     */
    public int getFortune() {
        return this.fortune;
    }

    /**
     *
     * @return the miner's speed in ticks
     */
    public int getSpeed() {
        return this.speed;
    }
}
