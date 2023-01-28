package net.nemezanevem.gregtech.api.pipenet.block;

import codechicken.lib.raytracer.IndexedVoxelShape;
import codechicken.lib.raytracer.RayTracer;
import codechicken.lib.raytracer.VoxelShapeBlockHitResult;
import codechicken.lib.vec.Cuboid6;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.nemezanevem.gregtech.api.block.BuiltInRenderBlock;
import net.nemezanevem.gregtech.api.cover.CoverBehavior;
import net.nemezanevem.gregtech.api.cover.ICoverable;
import net.nemezanevem.gregtech.api.cover.IFacadeCover;
import net.nemezanevem.gregtech.api.item.toolitem.ToolClass;
import net.nemezanevem.gregtech.api.pipenet.IBlockAppearance;
import net.nemezanevem.gregtech.api.pipenet.PipeNet;
import net.nemezanevem.gregtech.api.pipenet.WorldPipeNet;
import net.nemezanevem.gregtech.api.pipenet.tile.IPipeTile;
import net.nemezanevem.gregtech.api.pipenet.tile.TileEntityPipeBase;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.common.ConfigHolder;
import net.nemezanevem.gregtech.common.block.BlockFrame;
import net.nemezanevem.gregtech.common.block.FrameItemBlock;
import net.nemezanevem.gregtech.common.block.MetaBlocks;
import net.nemezanevem.gregtech.integration.IFacadeWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.nemezanevem.gregtech.api.tileentity.MetaTileEntity.FULL_CUBE_COLLISION;

@ParametersAreNonnullByDefault
@SuppressWarnings("deprecation")
public abstract class BlockPipe<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType, WorldPipeNetType extends WorldPipeNet<NodeDataType, ? extends PipeNet<NodeDataType>>> extends BuiltInRenderBlock implements EntityBlock, IFacadeWrapper, IBlockAppearance {

    protected final ThreadLocal<IPipeTile<PipeType, NodeDataType>> tileEntities = new ThreadLocal<>();

    public BlockPipe() {
        super(BlockBehaviour.Properties.of(net.minecraft.world.level.material.Material.METAL).sound(SoundType.METAL).strength(2.0f, 3.0f).dynamicShape().isViewBlocking((state, level, pos) -> false));
    }

    public static VoxelShape getSideBox(Direction side, float thickness) {
        float min = (1.0f - thickness) / 2.0f, max = min + thickness;
        float faceMin = 0f, faceMax = 1f;

        if (side == null)
            return Shapes.box(min, min, min, max, max, max);
        VoxelShape cuboid;
        switch (side) {
            case WEST:
                cuboid = Shapes.box(faceMin, min, min, min, max, max);
                break;
            case EAST:
                cuboid = Shapes.box(max, min, min, faceMax, max, max);
                break;
            case NORTH:
                cuboid = Shapes.box(min, min, faceMin, max, max, min);
                break;
            case SOUTH:
                cuboid = Shapes.box(min, min, max, max, max, faceMax);
                break;
            case UP:
                cuboid = Shapes.box(min, max, min, max, faceMax, max);
                break;
            case DOWN:
                cuboid = Shapes.box(min, faceMin, min, max, min, max);
                break;
            default:
                cuboid = Shapes.box(min, min, min, max, max, max);
        }
        return cuboid;
    }

    /**
     * @return the pipe cuboid for that side but with a offset one the facing with the cover to prevent z fighting.
     */
    public static VoxelShape getCoverSideBox(Direction side, float thickness) {
        VoxelShape cuboid = getSideBox(side, thickness);
        cuboid.forAllBoxes(((pMinX, pMinY, pMinZ, pMaxX, pMaxY, pMaxZ) -> {
            pMinX -= 0.001;
            pMinY -= 0.001;
            pMinZ -= 0.001;
            pMaxX += 0.001;
            pMaxY += 0.001;
            pMaxZ += 0.001;
        }))
        return cuboid;
    }

    public abstract Class<PipeType> getPipeTypeClass();

    public abstract WorldPipeNetType getWorldPipeNet(LevelReader world);

    public abstract TileEntityPipeBase<PipeType, NodeDataType> createNewTileEntity(boolean supportsTicking);

    public abstract NodeDataType createProperties(IPipeTile<PipeType, NodeDataType> pipeTile);

    public abstract NodeDataType createItemProperties(ItemStack itemStack);

    public abstract ItemStack getDropItem(IPipeTile<PipeType, NodeDataType> pipeTile);

    protected abstract NodeDataType getFallbackType();

    // TODO this has no reason to need an ItemStack parameter
    public abstract PipeType getItemPipeType(ItemStack itemStack);

    public abstract void setTileEntityData(TileEntityPipeBase<PipeType, NodeDataType> pipeTile, ItemStack itemStack);


    @Override
    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(pLevel, pPos);
        if (pipeTile != null) {
            pipeTile.getCoverableImplementation().dropAllCovers();
            tileEntities.set(pipeTile);
        }
        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
        getWorldPipeNet(pLevel).removeNode(pPos);
    }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(pLevel, pPos);
        if (pipeTile != null) {
            int activeConnections = pipeTile.getConnections();
            boolean isActiveNode = activeConnections != 0;
            getWorldPipeNet(pLevel).addNode(pPos, createProperties(pipeTile), 0, activeConnections, isActiveNode);
            onActiveModeChange(pLevel, pPos, isActiveNode, true);
        }
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
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
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        if (level.isClientSide()) return;
        if (!ConfigHolder.machines.gt6StylePipesCables) {
            IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(level, pos);
            if (pipeTile != null) {
                Direction facing = null;
                for (Direction direction : Direction.values()) {
                    if (Util.arePosEqual(pos, pos.offset(direction.getNormal()))) {
                        facing = direction;
                        break;
                    }
                }
                if (facing == null) {
                    //not our neighbor
                    return;
                }
                boolean open = pipeTile.isConnected(facing);
                boolean canConnect = pipeTile.getCoverableImplementation().getCoverAtSide(facing) != null || canConnect(pipeTile, facing);
                if (!open && canConnect && state.getBlock() != pipeTile.getPipeBlock())
                    pipeTile.setConnection(facing, true, false);
                if (open && !canConnect)
                    pipeTile.setConnection(facing, false, false);
                updateActiveNodeStatus(level, pos, pipeTile);
                pipeTile.getCoverableImplementation().updateInputRedstoneSignals();
            }
        }

    }

    @Nonnull
    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        PipeNet<NodeDataType> net = getWorldPipeNet(pLevel).getNetFromPos(pCurrentPos);
        if (net != null) {
            net.onNeighbourUpdate(pFacingPos);
        }
        return pState;
    }

    @Override
    public boolean canConnectRedstone(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nullable Direction side) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(world, pos);
        return pipeTile != null && pipeTile.getCoverableImplementation().canConnectRedstone(side);
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, LevelReader level, BlockPos pos, Direction side) {
        // The check in World::getRedstonePower in the vanilla code base is reversed. Setting this to false will
        // actually cause getWeakPower to be called, rather than prevent it.
        return false;
    }

    @Override
    public int getSignal(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(pLevel, pPos);
        return pipeTile == null ? 0 : pipeTile.getCoverableImplementation().getOutputRedstoneSignal(pDirection.getOpposite());
    }

    public void updateActiveNodeStatus(LevelReader worldIn, BlockPos pos, IPipeTile<PipeType, NodeDataType> pipeTile) {
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
    protected void onActiveModeChange(LevelReader world, BlockPos pos, boolean isActiveNow, boolean isInitialChange) {
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
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(pLevel, pPos);
        VoxelShapeBlockHitResult rayTraceResult = (VoxelShapeBlockHitResult) RayTracer.retraceBlock(pLevel, pPlayer, pPos);
        if (rayTraceResult == null || pipeTile == null) {
            return InteractionResult.PASS;
        }
        return onPipeActivated(pLevel, pState, pPos, pPlayer, pHand, pHit.getDirection(), rayTraceResult, pipeTile);
    }

    public InteractionResult onPipeActivated(Level world, BlockState state, BlockPos pos, Player entityPlayer, InteractionHand hand, Direction side, VoxelShapeBlockHitResult hit, IPipeTile<PipeType, NodeDataType> pipeTile) {
        ItemStack itemStack = entityPlayer.getItemInHand(hand);

        if (pipeTile.getFrameMaterial() == null && pipeTile instanceof TileEntityPipeBase && itemStack.getItem() instanceof FrameItemBlock && pipeTile.getPipeType().getThickness() < 1) {
            BlockFrame frameBlock = (BlockFrame) ((FrameItemBlock) itemStack.getItem()).getBlock();
            Material material = frameBlock.getGtMaterial(itemStack.getMetadata());
            ((TileEntityPipeBase<PipeType, NodeDataType>) pipeTile).setFrameMaterial(material);
            SoundType type = frameBlock.getSoundType(itemStack);
            world.playSound(entityPlayer, pos, type.getPlaceSound(), SoundSource.BLOCKS, (type.getVolume() + 1.0F) / 2.0F, type.getPitch() * 0.8F);
            if (!entityPlayer.isCreative()) {
                itemStack.shrink(1);
            }
            return InteractionResult.PASS;
        }

        if (itemStack.getItem() instanceof ItemBlockPipe) {
            BlockState blockStateAtSide = world.getBlockState(pos.offset(side.getNormal()));
            if (blockStateAtSide.getBlock() instanceof BlockFrame) {
                ItemBlockPipe<?, ?> itemBlockPipe = (ItemBlockPipe<?, ?>) itemStack.getItem();
                if (itemBlockPipe.blockPipe.getItemPipeType(itemStack) == getItemPipeType(itemStack)) {
                    BlockFrame frameBlock = (BlockFrame) blockStateAtSide.getBlock();
                    boolean wasPlaced = frameBlock.replaceWithFramedPipe(world, pos.offset(side.getNormal()), blockStateAtSide, entityPlayer, itemStack, side);
                    if (wasPlaced) {
                        pipeTile.setConnection(side, true, false);
                    }
                    return wasPlaced ? InteractionResult.SUCCESS : InteractionResult.PASS;
                }
            }
        }

        Direction coverSide = ICoverable.traceCoverSide(hit);
        if (coverSide == null) {
            return activateFrame(world, state, pos, entityPlayer, hand, hit, pipeTile);
        }

        if (!(hit.shape.getData() instanceof ICoverable.CoverSideData)) {
            return onPipeToolUsed(world, pos, itemStack, coverSide, pipeTile, entityPlayer);
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
                return InteractionResult.SUCCESS;
            }
            return entityPlayer.isCrouching() && entityPlayer.getMainHandItem().isEmpty() && coverBehavior.onScrewdriverClick(entityPlayer, hand, hit) != InteractionResult.PASS ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult activateFrame(Level world, BlockState state, BlockPos pos, Player entityPlayer, InteractionHand hand, VoxelShapeBlockHitResult hit, IPipeTile<PipeType, NodeDataType> pipeTile) {
        if (pipeTile.getFrameMaterial() != null && !(entityPlayer.getItemInHand(hand).getItem() instanceof ItemBlockPipe)) {
            BlockFrame blockFrame = MetaBlocks.FRAMES.get(pipeTile.getFrameMaterial());
            return blockFrame.onBlockActivated(world, pos, state, entityPlayer, hand, hit.getDirection(), (float) hit.getLocation().x, (float) hit.getLocation().y, (float) hit.getLocation().z);
        }
        return false;
    }

    /**
     * @return 1 if successfully used tool, 0 if failed to use tool,
     * -1 if ItemStack failed the capability check (no action done, continue checks).
     */
    public InteractionResult onPipeToolUsed(Level world, BlockPos pos, ItemStack stack, Direction coverSide, IPipeTile<PipeType, NodeDataType> pipeTile, Player entityPlayer) {
        if (isPipeTool(stack)) {
            if (!entityPlayer.level.isClientSide) {
                if (entityPlayer.isCrouching() && pipeTile.canHaveBlockedFaces()) {
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
        return ToolHelper.isTool(stack, ToolClass.WRENCH);
    }

    @Override
    public void onBlockClicked(@Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull Player playerIn) {
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
    public void onEntityCollision(Level worldIn, BlockPos pos, BlockState state, Entity entityIn) {
        IPipeTile<PipeType, NodeDataType> pipeTile = getPipeTileEntity(worldIn, pos);
        if (pipeTile != null && pipeTile.getFrameMaterial() != null) {
            // make pipe with frame climbable
            BlockFrame blockFrame = MetaBlocks.FRAMES.get(pipeTile.getFrameMaterial());
            blockFrame.onEntityCollision(worldIn, pos, state, entityIn);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void playerDestroy(@Nonnull Level worldIn, @Nonnull Player player, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable BlockEntity te, @Nonnull ItemStack stack) {
        tileEntities.set(te == null ? tileEntities.get() : (IPipeTile<PipeType, NodeDataType>) te);
        super.playerDestroy(worldIn, player, pos, state, te, stack);
        tileEntities.set(null);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, ServerLevel world, BlockPos pos, @Nullable BlockEntity pBlockEntity) {
        List<ItemStack> drops = new ArrayList<>();
        IPipeTile<PipeType, NodeDataType> pipeTile = tileEntities.get() == null ? getPipeTileEntity(world, pos) : tileEntities.get();
        if (pipeTile == null) return List.of();
        if (pipeTile.getFrameMaterial() != null) {
            BlockFrame blockFrame = MetaBlocks.FRAMES.get(pipeTile.getFrameMaterial());
            drops.add(blockFrame.getItem(pipeTile.getFrameMaterial()));
        }
        drops.add(getDropItem(pipeTile));
        return drops;
    }

    @Override
    public void addCollisionBoxToList(@Nonnull BlockState state, @Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
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
    public RayTraceResult collisionRayTrace(@Nonnull BlockState blockState, Level worldIn, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
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
        IPipeTile<PipeType, NodeDataType> tileEntityPipe = (IPipeTile<PipeType, NodeDataType>) world.getBlockEntity(pos);
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
        BlockEntity tileEntityAtPos = world.getBlockEntity(selfPos);
        return getPipeTileEntity(tileEntityAtPos);
    }

    public IPipeTile<PipeType, NodeDataType> getPipeTileEntity(BlockEntity tileEntityAtPos) {
        if (tileEntityAtPos instanceof IPipeTile && isThisPipeBlock(((IPipeTile) tileEntityAtPos).getPipeBlock())) {
            return (IPipeTile<PipeType, NodeDataType>) tileEntityAtPos;
        }
        return null;
    }

    public boolean canConnect(IPipeTile<PipeType, NodeDataType> selfTile, Direction facing) {
        if (selfTile.getPipeWorld().getBlockState(selfTile.getPipePos().offset(facing.getNormal())).getBlock() == Blocks.AIR)
            return false;
        CoverBehavior cover = selfTile.getCoverableImplementation().getCoverAtSide(facing);
        if (cover != null && !cover.canPipePassThrough()) {
            return false;
        }
        BlockEntity other = selfTile.getPipeWorld().getBlockEntity(selfTile.getPipePos().offset(facing.getNormal()));
        if (other instanceof IPipeTile) {
            cover = ((IPipeTile<?, ?>) other).getCoverableImplementation().getCoverAtSide(facing.getOpposite());
            if (cover != null && !cover.canPipePassThrough())
                return false;
            return canPipesConnect(selfTile, facing, (IPipeTile<PipeType, NodeDataType>) other);
        }
        return canPipeConnectToBlock(selfTile, facing, other);
    }

    public abstract boolean canPipesConnect(IPipeTile<PipeType, NodeDataType> selfTile, Direction side, IPipeTile<PipeType, NodeDataType> sideTile);

    public abstract boolean canPipeConnectToBlock(IPipeTile<PipeType, NodeDataType> selfTile, Direction side, @Nullable BlockEntity tile);

    private List<IndexedVoxelShape> getCollisionBox(BlockGetter world, BlockPos pos, @Nullable Entity entityIn) {
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
        ArrayList<IndexedVoxelShape> result = new ArrayList<>();
        ICoverable coverable = pipeTile.getCoverableImplementation();

        // Check if the machine grid is being rendered
        if (hasPipeCollisionChangingItem(world, pos, entityIn)) {
            result.add(FULL_CUBE_COLLISION);
        }

        // Always add normal collision so player doesn't "fall through" the cable/pipe when
        // a tool is put in hand, and will still be standing where they were before.
        result.add(new IndexedVoxelShape(new ICoverable.PrimaryBoxData(true), getSideBox(null, thickness)));
        for (Direction side : Direction.values()) {
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
                Util.isCoverBehaviorItem(stack, () -> hasCover(getPipeTileEntity(world, pos)),
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
