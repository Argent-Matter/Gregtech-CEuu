package net.nemezanevem.gregtech.api.pipenet.block;

import codechicken.lib.raytracer.RayTracer;
import codechicken.lib.raytracer.VoxelShapeBlockHitResult;
import codechicken.lib.vec.Cuboid6;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.nemezanevem.gregtech.api.block.BuiltInRenderBlock;
import net.nemezanevem.gregtech.api.cover.CoverBehavior;
import net.nemezanevem.gregtech.api.cover.ICoverable;
import net.nemezanevem.gregtech.api.cover.IFacadeCover;
import net.nemezanevem.gregtech.api.pipenet.IBlockAppearance;
import net.nemezanevem.gregtech.api.pipenet.PipeNet;
import net.nemezanevem.gregtech.api.pipenet.WorldPipeNet;
import net.nemezanevem.gregtech.api.pipenet.tile.IPipeTile;
import net.nemezanevem.gregtech.api.pipenet.tile.TileEntityPipeBase;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.common.ConfigHolder;
import net.nemezanevem.gregtech.common.block.BlockFrame;
import net.nemezanevem.gregtech.common.block.FrameItemBlock;
import net.nemezanevem.gregtech.integration.IFacadeWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static gregtech.api.metatileentity.MetaTileEntity.FULL_CUBE_COLLISION;

@SuppressWarnings("deprecation")
public abstract class BlockPipe<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType, WorldPipeNetType extends WorldPipeNet<NodeDataType, ? extends PipeNet<NodeDataType>>> extends BuiltInRenderBlock implements EntityBlock, IFacadeWrapper, IBlockAppearance {

    protected final ThreadLocal<IPipeTile<PipeType, NodeDataType>> tileEntities = new ThreadLocal<>();

    public BlockPipe() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("pipe");
        setCreativeTab(GregTechAPI.TAB_GREGTECH);
        setSoundType(SoundType.METAL);
        setHardness(2.0f);
        setResistance(3.0f);
        setLightOpacity(0);
        disableStats();
    }

    public static Cuboid6 getSideBox(Direction side, float thickness) {
        float min = (1.0f - thickness) / 2.0f, max = min + thickness;
        float faceMin = 0f, faceMax = 1f;

        if (side == null)
            return new Cuboid6(min, min, min, max, max, max);
        Cuboid6 cuboid;
        switch (side) {
            case WEST:
                cuboid = new Cuboid6(faceMin, min, min, min, max, max);
                break;
            case EAST:
                cuboid = new Cuboid6(max, min, min, faceMax, max, max);
                break;
            case NORTH:
                cuboid = new Cuboid6(min, min, faceMin, max, max, min);
                break;
            case SOUTH:
                cuboid = new Cuboid6(min, min, max, max, max, faceMax);
                break;
            case UP:
                cuboid = new Cuboid6(min, max, min, max, faceMax, max);
                break;
            case DOWN:
                cuboid = new Cuboid6(min, faceMin, min, max, min, max);
                break;
            default:
                cuboid = new Cuboid6(min, min, min, max, max, max);
        }
        return cuboid;
    }

    /**
     * @return the pipe cuboid for that side but with a offset one the facing with the cover to prevent z fighting.
     */
    public static Cuboid6 getCoverSideBox(Direction side, float thickness) {
        Cuboid6 cuboid = getSideBox(side, thickness);
        if (side != null)
            cuboid.setSide(side, side.getAxisDirection() == Direction.AxisDirection.NEGATIVE ? 0.001 : 0.999);
        return cuboid;
    }

    public abstract Class<PipeType> getPipeTypeClass();

    public abstract WorldPipeNetType getWorldPipeNet(Level world);

    public abstract TileEntityPipeBase<PipeType, NodeDataType> createNewTileEntity(boolean supportsTicking);

    public abstract NodeDataType createProperties(IPipeTile<PipeType, NodeDataType> pipeTile);

    public abstract NodeDataType createItemProperties(ItemStack itemStack);

    public abstract ItemStack getDropItem(IPipeTile<PipeType, NodeDataType> pipeTile);

    protected abstract NodeDataType getFallbackType();

    // TODO this has no reason to need an ItemStack parameter
    public abstract PipeType getItemPipeType(ItemStack itemStack);

    public abstract void setTileEntityData(TileEntityPipeBase<PipeType, NodeDataType> pipeTile, ItemStack itemStack);


    @Override
    public void breakBlock(@Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(worldIn, pos);
        if (pipeTile != null) {
            pipeTile.getCoverableImplementation().dropAllCovers();
            tileEntities.set(pipeTile);
        }
        super.breakBlock(worldIn, pos, state);
        getWorldPipeNet(worldIn).removeNode(pos);
    }

    @Override
    public void onBlockAdded(Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        worldIn.scheduleUpdate(pos, this, 1);
    }

    @Override
    public void updateTick(@Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Random rand) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(worldIn, pos);
        if (pipeTile != null) {
            int activeConnections = pipeTile.getConnections();
            boolean isActiveNode = activeConnections != 0;
            getWorldPipeNet(worldIn).addNode(pos, createProperties(pipeTile), 0, activeConnections, isActiveNode);
            onActiveModeChange(worldIn, pos, isActiveNode, true);
        }
    }

    @Override
    public void onBlockPlacedBy(@Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull LivingEntity placer, @Nonnull ItemStack stack) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(worldIn, pos);
        if (pipeTile != null) {
            setTileEntityData((TileEntityPipeBase<PipeType, NodeDataType>) pipeTile, stack);

            // Color pipes/cables on place if holding spray can in off-hand
            if (placer instanceof Player) {
                ItemStack offhand = placer.getOffhandItem();
                for (int i = 0; i < DyeColor.values().length; i++) {
                    if (ItemStack.matches(offhand, MetaItems.SPRAY_CAN_DYES[i].getStackForm())) {
                        MetaItems.SPRAY_CAN_DYES[i].getBehaviours().get(0).onItemUse((Player) placer, worldIn, pos, InteractionHand.OFF_HAND, Direction.UP, 0, 0 , 0);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void neighborChanged(@Nonnull BlockState state, @Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos) {
        if (worldIn.isClientSide) return;
        if (!ConfigHolder.machines.gt6StylePipesCables) {
            IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(worldIn, pos);
            if (pipeTile != null) {
                Direction facing = null;
                for (Direction facing1 : Direction.values()) {
                    if (GTUtility.arePosEqual(fromPos, pos.offset(facing1))) {
                        facing = facing1;
                        break;
                    }
                }
                if (facing == null) {
                    //not our neighbor
                    return;
                }
                boolean open = pipeTile.isConnected(facing);
                boolean canConnect = pipeTile.getCoverableImplementation().getCoverAtSide(facing) != null || canConnect(pipeTile, facing);
                if (!open && canConnect && state.getBlock() != blockIn)
                    pipeTile.setConnection(facing, true, false);
                if (open && !canConnect)
                    pipeTile.setConnection(facing, false, false);
                updateActiveNodeStatus(worldIn, pos, pipeTile);
                pipeTile.getCoverableImplementation().updateInputRedstoneSignals();
            }
        }

    }

    @Override
    public void observedNeighborChange(@Nonnull BlockState observerState, @Nonnull Level world, @Nonnull BlockPos observerPos, @Nonnull Block changedBlock, @Nonnull BlockPos changedBlockPos) {
        PipeNet<NodeDataType> net = getWorldPipeNet(world).getNetFromPos(observerPos);
        if (net != null) {
            net.onNeighbourUpdate(changedBlockPos);
        }
    }

    @Override
    public boolean canConnectRedstone(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nullable Direction side) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(world, pos);
        return pipeTile != null && pipeTile.getCoverableImplementation().canConnectRedstone(side);
    }

    @Override
    public boolean shouldCheckWeakPower(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nonnull Direction side) {
        // The check in World::getRedstonePower in the vanilla code base is reversed. Setting this to false will
        // actually cause getWeakPower to be called, rather than prevent it.
        return false;
    }

    @Override
    public int getWeakPower(@Nonnull BlockState blockState, @Nonnull BlockGetter blockAccess, @Nonnull BlockPos pos, @Nonnull Direction side) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(blockAccess, pos);
        return pipeTile == null ? 0 : pipeTile.getCoverableImplementation().getOutputRedstoneSignal(side.getOpposite());
    }

    public void updateActiveNodeStatus(Level worldIn, BlockPos pos, IPipeTile<PipeType, NodeDataType> pipeTile) {
        PipeNet<NodeDataType> pipeNet = getWorldPipeNet(worldIn).getNetFromPos(pos);
        if (pipeNet != null && pipeTile != null) {
            int activeConnections = pipeTile.getConnections(); //remove blocked connections
            boolean isActiveNodeNow = activeConnections != 0;
            boolean modeChanged = pipeNet.markNodeAsActive(pos, isActiveNodeNow);
            if (modeChanged) {
                onActiveModeChange(worldIn, pos, isActiveNodeNow, false);
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity createNewTileEntity(@Nonnull Level worldIn, int meta) {
        return createNewTileEntity(false);
    }

    /**
     * Can be used to update tile entity to tickable when node becomes active
     * usable for fluid pipes, as example
     */
    protected void onActiveModeChange(Level world, BlockPos pos, boolean isActiveNow, boolean isInitialChange) {
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull BlockState state, @Nonnull HitResult target, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Player player) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(world, pos);
        if (pipeTile == null) {
            return ItemStack.EMPTY;
        }
        if (target instanceof VoxelShapeBlockHitResult) {
            VoxelShapeBlockHitResult result = (VoxelShapeBlockHitResult) target;
            if (result.shape.getData() instanceof ICoverable.CoverSideData data) {
                Direction coverSide = data.side;
                CoverBehavior coverBehavior = pipeTile.getCoverableImplementation().getCoverAtSide(coverSide);
                return coverBehavior == null ? ItemStack.EMPTY : coverBehavior.getPickItem();
            }
        }
        return getDropItem(pipeTile);
    }

    @Override
    public boolean onBlockActivated(@Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Player playerIn, @Nonnull InteractionHand hand, @Nonnull Direction facing, float hitX, float hitY, float hitZ) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(worldIn, pos);
        VoxelShapeBlockHitResult rayTraceResult = (VoxelShapeBlockHitResult) RayTracer.retraceBlock(worldIn, playerIn, pos);
        if (rayTraceResult == null || pipeTile == null) {
            return false;
        }
        return onPipeActivated(worldIn, state, pos, playerIn, hand, facing, rayTraceResult, pipeTile);
    }

    public boolean onPipeActivated(Level world, BlockState state, BlockPos pos, Player entityPlayer, InteractionHand hand, Direction side, VoxelShapeBlockHitResult hit, IPipeTile<PipeType, NodeDataType> pipeTile) {
        ItemStack itemStack = entityPlayer.getItemInHand(hand);

        if (pipeTile.getFrameMaterial() == null && pipeTile instanceof TileEntityPipeBase && itemStack.getItem() instanceof FrameItemBlock && pipeTile.getPipeType().getThickness() < 1) {
            BlockFrame frameBlock = (BlockFrame) ((FrameItemBlock) itemStack.getItem()).getBlock();
            Material material = frameBlock.getGtMaterial(itemStack.getMetadata());
            ((TileEntityPipeBase<PipeType, NodeDataType>) pipeTile).setFrameMaterial(material);
            SoundType type = frameBlock.getSoundType(itemStack);
            world.playSound(entityPlayer, pos, type.getPlaceSound(), SoundCategory.BLOCKS, (type.getVolume() + 1.0F) / 2.0F, type.getPitch() * 0.8F);
            if (!entityPlayer.capabilities.isCreativeMode) {
                itemStack.shrink(1);
            }
            return true;
        }

        if (itemStack.getItem() instanceof ItemBlockPipe) {
            BlockState blockStateAtSide = world.getBlockState(pos.offset(side));
            if (blockStateAtSide.getBlock() instanceof BlockFrame) {
                ItemBlockPipe<?, ?> itemBlockPipe = (ItemBlockPipe<?, ?>) itemStack.getItem();
                if (itemBlockPipe.blockPipe.getItemPipeType(itemStack) == getItemPipeType(itemStack)) {
                    BlockFrame frameBlock = (BlockFrame) blockStateAtSide.getBlock();
                    boolean wasPlaced = frameBlock.replaceWithFramedPipe(world, pos.offset(side), blockStateAtSide, entityPlayer, itemStack, side);
                    if (wasPlaced) {
                        pipeTile.setConnection(side, true, false);
                    }
                    return wasPlaced;
                }
            }
        }

        Direction coverSide = ICoverable.traceCoverSide(hit);
        if (coverSide == null) {
            return activateFrame(world, state, pos, entityPlayer, hand, hit, pipeTile);
        }

        if (!(hit.cuboid6.data instanceof CoverSideData)) {
            switch (onPipeToolUsed(world, pos, itemStack, coverSide, pipeTile, entityPlayer)) {
                case SUCCESS:
                    return true;
                case FAIL:
                    return false;
            }
        }
        CoverBehavior coverBehavior = pipeTile.getCoverableImplementation().getCoverAtSide(coverSide);
        if (coverBehavior == null) {
            return activateFrame(world, state, pos, entityPlayer, hand, hit, pipeTile);
        }

        if (itemStack.getItem().getToolClasses(itemStack).contains(ToolClasses.SCREWDRIVER)) {
            if (coverBehavior.onScrewdriverClick(entityPlayer, hand, hit) == InteractionResult.SUCCESS) {
                ToolHelper.damageItem(itemStack, entityPlayer);
                if (itemStack.getItem() instanceof IGTTool) {
                    ((IGTTool) itemStack.getItem()).playSound(entityPlayer);
                }
                return true;
            }
        }

        InteractionResult result = coverBehavior.onRightClick(entityPlayer, hand, hit);
        if (result == InteractionResult.PASS) {
            if (activateFrame(world, state, pos, entityPlayer, hand, hit, pipeTile)) {
                return true;
            }
            return entityPlayer.isSneaking() && entityPlayer.getHeldItemMainhand().isEmpty() && coverBehavior.onScrewdriverClick(entityPlayer, hand, hit) != InteractionResult.PASS;
        }
        return true;
    }

    private boolean activateFrame(World world, BlockState state, BlockPos pos, Player entityPlayer, InteractionHand hand, VoxelShapeBlockHitResult hit, IPipeTile<PipeType, NodeDataType> pipeTile) {
        if (pipeTile.getFrameMaterial() != null && !(entityPlayer.getHeldItem(hand).getItem() instanceof ItemBlockPipe)) {
            BlockFrame blockFrame = MetaBlocks.FRAMES.get(pipeTile.getFrameMaterial());
            return blockFrame.onBlockActivated(world, pos, state, entityPlayer, hand, hit.sideHit, (float) hit.hitVec.x, (float) hit.hitVec.y, (float) hit.hitVec.z);
        }
        return false;
    }

    /**
     * @return 1 if successfully used tool, 0 if failed to use tool,
     * -1 if ItemStack failed the capability check (no action done, continue checks).
     */
    public InteractionResult onPipeToolUsed(World world, BlockPos pos, ItemStack stack, Direction coverSide, IPipeTile<PipeType, NodeDataType> pipeTile, Player entityPlayer) {
        if (isPipeTool(stack)) {
            if (!entityPlayer.world.isClientSide) {
                if (entityPlayer.isSneaking() && pipeTile.canHaveBlockedFaces()) {
                    boolean isBlocked = pipeTile.isFaceBlocked(coverSide);
                    pipeTile.setFaceBlocked(coverSide, !isBlocked);
                    if (stack.getItem() instanceof IGTTool) {
                        ((IGTTool) stack.getItem()).playSound(entityPlayer);
                    }
                } else {
                    boolean isOpen = pipeTile.isConnected(coverSide);
                    pipeTile.setConnection(coverSide, !isOpen, false);
                    if (stack.getItem() instanceof IGTTool && isOpen != pipeTile.isConnected(coverSide)) {
                        ((IGTTool) stack.getItem()).playSound(entityPlayer);
                    }
                }
                ToolHelper.damageItem(stack, entityPlayer);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    protected boolean isPipeTool(@Nonnull ItemStack stack) {
        return ToolHelper.isTool(stack, ToolClasses.WRENCH);
    }

    @Override
    public void onBlockClicked(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Player playerIn) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(worldIn, pos);
        VoxelShapeBlockHitResult rayTraceResult = (VoxelShapeBlockHitResult) RayTracer.retraceBlock(worldIn, playerIn, pos);
        if (pipeTile == null || rayTraceResult == null) {
            return;
        }
        Direction coverSide = ICoverable.traceCoverSide(rayTraceResult);
        CoverBehavior coverBehavior = coverSide == null ? null : pipeTile.getCoverableImplementation().getCoverAtSide(coverSide);

        if (coverBehavior != null) {
            coverBehavior.onLeftClick(playerIn, rayTraceResult);
        }
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, BlockState state, Entity entityIn) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(worldIn, pos);
        if (pipeTile != null && pipeTile.getFrameMaterial() != null) {
            // make pipe with frame climbable
            BlockFrame blockFrame = MetaBlocks.FRAMES.get(pipeTile.getFrameMaterial());
            blockFrame.onEntityCollision(worldIn, pos, state, entityIn);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void harvestBlock(@Nonnull World worldIn, @Nonnull Player player, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable TileEntity te, @Nonnull ItemStack stack) {
        tileEntities.set(te == null ? tileEntities.get() : (IPipeTile<PipeType, NodeDataType>) te);
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        tileEntities.set(null);
    }

    @Override
    public void getDrops(@Nonnull NonNullList<ItemStack> drops, @Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nonnull BlockState state, int fortune) {
        IPipeTile<PipeType, NodeDataType> pipeTile = tileEntities.get() == null ? getPipeTileEntity(world, pos) : tileEntities.get();
        if (pipeTile == null) return;
        if (pipeTile.getFrameMaterial() != null) {
            BlockFrame blockFrame = MetaBlocks.FRAMES.get(pipeTile.getFrameMaterial());
            drops.add(blockFrame.getItem(pipeTile.getFrameMaterial()));
        }
        drops.add(getDropItem(pipeTile));
    }

    @Override
    public void addCollisionBoxToList(@Nonnull BlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        // This iterator causes some heap memory overhead
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(worldIn, pos);
        if (pipeTile != null && pipeTile.getFrameMaterial() != null) {
            AxisAlignedBB box = BlockFrame.COLLISION_BOX.offset(pos);
            if (box.intersects(entityBox)) {
                collidingBoxes.add(box);
            }
            return;
        }
        for (Cuboid6 axisAlignedBB : getCollisionBox(worldIn, pos, entityIn)) {
            AxisAlignedBB offsetBox = axisAlignedBB.aabb().offset(pos);
            if (offsetBox.intersects(entityBox)) collidingBoxes.add(offsetBox);
        }
    }

    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(@Nonnull BlockState blockState, World worldIn, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
        if (worldIn.isClientSide) {
            return getClientCollisionRayTrace(worldIn, pos, start, end);
        }
        return RayTracer.rayTraceCuboidsClosest(start, end, pos, FULL_CUBE_COLLISION);
    }

    @SideOnly(Side.CLIENT)
    public RayTraceResult getClientCollisionRayTrace(World worldIn, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
        return RayTracer.rayTraceCuboidsClosest(start, end, pos, getCollisionBox(worldIn, pos, Minecraft.getMinecraft().player));
    }

    @Nonnull
    @Override
    public BlockFaceShape getBlockFaceShape(@Nonnull BlockGetter worldIn, @Nonnull BlockState state, @Nonnull BlockPos pos, @Nonnull Direction face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean recolorBlock(World world, @Nonnull BlockPos pos, @Nonnull Direction side, @Nonnull EnumDyeColor color) {
        IPipeTile<PipeType, NodeDataType> tileEntityPipe = (IPipeTile<PipeType, NodeDataType>) world.getTileEntity(pos);
        if (tileEntityPipe != null && tileEntityPipe.getPipeType() != null &&
                tileEntityPipe.getPipeType().isPaintable() &&
                tileEntityPipe.getPaintingColor() != color.colorValue) {
            tileEntityPipe.setPaintingColor(color.colorValue);
            return true;
        }
        return false;
    }

    protected boolean isThisPipeBlock(Block block) {
        return block != null && block.getClass().isAssignableFrom(getClass());
    }

    /**
     * Just returns proper pipe tile entity
     */
    public IPipeTile<PipeType, NodeDataType> getPipeTileEntity(BlockGetter world, BlockPos selfPos) {
        TileEntity tileEntityAtPos = world.getTileEntity(selfPos);
        return getPipeTileEntity(tileEntityAtPos);
    }

    public IPipeTile<PipeType, NodeDataType> getPipeTileEntity(TileEntity tileEntityAtPos) {
        if (tileEntityAtPos instanceof IPipeTile && isThisPipeBlock(((IPipeTile) tileEntityAtPos).getPipeBlock())) {
            return (IPipeTile<PipeType, NodeDataType>) tileEntityAtPos;
        }
        return null;
    }

    public boolean canConnect(IPipeTile<PipeType, NodeDataType> selfTile, Direction facing) {
        if (selfTile.getPipeWorld().getBlockState(selfTile.getPipePos().offset(facing)).getBlock() == Blocks.AIR)
            return false;
        CoverBehavior cover = selfTile.getCoverableImplementation().getCoverAtSide(facing);
        if (cover != null && !cover.canPipePassThrough()) {
            return false;
        }
        TileEntity other = selfTile.getPipeWorld().getTileEntity(selfTile.getPipePos().offset(facing));
        if (other instanceof IPipeTile) {
            cover = ((IPipeTile<?, ?>) other).getCoverableImplementation().getCoverAtSide(facing.getOpposite());
            if (cover != null && !cover.canPipePassThrough())
                return false;
            return canPipesConnect(selfTile, facing, (IPipeTile<PipeType, NodeDataType>) other);
        }
        return canPipeConnectToBlock(selfTile, facing, other);
    }

    public abstract boolean canPipesConnect(IPipeTile<PipeType, NodeDataType> selfTile, Direction side, IPipeTile<PipeType, NodeDataType> sideTile);

    public abstract boolean canPipeConnectToBlock(IPipeTile<PipeType, NodeDataType> selfTile, Direction side, @Nullable TileEntity tile);

    private List<IndexedCuboid6> getCollisionBox(BlockGetter world, BlockPos pos, @Nullable Entity entityIn) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(world, pos);
        if (pipeTile == null) {
            return Collections.emptyList();
        }
        if (pipeTile.getFrameMaterial() != null) {
            return Collections.singletonList(FULL_CUBE_COLLISION);
        }
        PipeType pipeType = pipeTile.getPipeType();
        if (pipeType == null) {
            return Collections.emptyList();
        }
        int actualConnections = getPipeTileEntity(world, pos).getVisualConnections();
        float thickness = pipeType.getThickness();
        ArrayList<IndexedCuboid6> result = new ArrayList<>();
        ICoverable coverable = pipeTile.getCoverableImplementation();

        // Check if the machine grid is being rendered
        if (hasPipeCollisionChangingItem(world, pos, entityIn)) {
            result.add(FULL_CUBE_COLLISION);
        }

        // Always add normal collision so player doesn't "fall through" the cable/pipe when
        // a tool is put in hand, and will still be standing where they were before.
        result.add(new IndexedCuboid6(new PrimaryBoxData(true), getSideBox(null, thickness)));
        for (Direction side : Direction.VALUES) {
            if ((actualConnections & 1 << side.getIndex()) > 0) {
                result.add(new IndexedCuboid6(new PipeConnectionData(side), getSideBox(side, thickness)));
            }
        }
        coverable.addCoverCollisionBoundingBox(result);
        return result;
    }

    public boolean hasPipeCollisionChangingItem(BlockGetter world, BlockPos pos, Entity entity) {
        if (entity instanceof Player) {
            return hasPipeCollisionChangingItem(world, pos, ((Player) entity).getHeldItem(InteractionHand.MAIN_HAND)) ||
                    hasPipeCollisionChangingItem(world, pos, ((Player) entity).getHeldItem(InteractionHand.OFF_HAND)) ||
                    entity.isSneaking() && isHoldingPipe((Player) entity);
        }
        return false;
    }

    public abstract boolean isHoldingPipe(Player player);

    public boolean hasPipeCollisionChangingItem(BlockGetter world, BlockPos pos, ItemStack stack) {
        return isPipeTool(stack) || ToolHelper.isTool(stack, ToolClasses.SCREWDRIVER) ||
                GTUtility.isCoverBehaviorItem(stack, () -> hasCover(getPipeTileEntity(world, pos)),
                coverDef -> ICoverable.canPlaceCover(coverDef, getPipeTileEntity(world, pos).getCoverableImplementation()));
    }

    protected boolean hasCover(IPipeTile<PipeType, NodeDataType> pipeTile) {
        if (pipeTile == null)
            return false;
        return pipeTile.getCoverableImplementation().hasAnyCover();
    }

    @Override
    public boolean canRenderInLayer(@Nonnull BlockState state, @Nonnull RenderType layer) {
        return true;
    }

    @Nonnull
    @Override
    public BlockState getFacade(@Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nullable Direction side, @Nonnull BlockPos otherPos) {
        return getFacade(world, pos, side);
    }

    @Nonnull
    @Override
    public BlockState getFacade(@Nonnull BlockGetter world, @Nonnull BlockPos pos, Direction side) {
        IPipeTile<?, ?> pipeTileEntity = getPipeTileEntity(world, pos);
        if (pipeTileEntity != null && side != null) {
            CoverBehavior coverBehavior = pipeTileEntity.getCoverableImplementation().getCoverAtSide(side);
            if (coverBehavior instanceof IFacadeCover) {
                return ((IFacadeCover) coverBehavior).getVisualState();
            }
        }
        return world.getBlockState(pos);
    }

    @Nonnull
    @Override
    public BlockState getVisualState(@Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nonnull Direction side) {
        return getFacade(world, pos, side);
    }

    @Override
    public boolean supportsVisualConnections() {
        return true;
    }

    public static class PipeConnectionData {
        public final Direction side;

        public PipeConnectionData(Direction side) {
            this.side = side;
        }
    }

}
