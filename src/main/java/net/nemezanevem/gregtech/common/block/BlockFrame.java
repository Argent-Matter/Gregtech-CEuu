package net.nemezanevem.gregtech.common.block;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.block.DelayedStateBlock;
import net.nemezanevem.gregtech.api.item.toolitem.ToolClass;
import net.nemezanevem.gregtech.api.pipenet.block.BlockPipe;
import net.nemezanevem.gregtech.api.pipenet.block.ItemBlockPipe;
import net.nemezanevem.gregtech.api.pipenet.tile.IPipeTile;
import net.nemezanevem.gregtech.api.pipenet.tile.TileEntityPipeBase;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.properties.info.GtMaterialIconTypes;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.client.model.IModelSupplier;
import net.nemezanevem.gregtech.common.block.properties.PropertyMaterial;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class BlockFrame extends DelayedStateBlock implements IModelSupplier {

    public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(new ResourceLocation(GregTech.MODID, "frame_block"), "normal");
    public static final VoxelShape COLLISION_BOX = Shapes.box(0.05, 0.0, 0.05, 0.95, 1.0, 0.95);

    public final PropertyMaterial variantProperty;

    // todo wood?
    public BlockFrame(Material[] materials) {
        super(BlockBehaviour.Properties.of(net.minecraft.world.level.material.Material.METAL).strength(3.0f, 6.0f).noOcclusion());
        this.variantProperty = PropertyMaterial.create("variant", materials);
        initBlockState();
    }

    @Override
    public String getHarvestTool(BlockState state) {
        Material material = state.getValue(variantProperty);
        if (ModHandler.isMaterialWood(material)) {
            return ToolClass.AXE;
        }
        return ToolClass.WRENCH;
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        Material material = state.getValue(variantProperty);
        if (ModHandler.isMaterialWood(material)) {
            return SoundType.WOOD;
        }
        return SoundType.METAL;
    }

    public SoundType getSoundType(ItemStack stack) {
        Material material = getGtMaterial(Util.getId(stack.getItem()).getPath());
        if (ModHandler.isMaterialWood(material)) {
            return SoundType.WOOD;
        }
        return SoundType.METAL;
    }

    @Override
    public int getHarvestLevel(BlockState state) {
        return 1;
    }

    @Override
    @SuppressWarnings("deprecation")
    public net.minecraft.block.material.Material getMaterial(BlockState state) {
        Material material = state.getValue(variantProperty);
        if (ModHandler.isMaterialWood(material)) {
            return net.minecraft.block.material.Material.WOOD;
        }
        return super.getMaterial(state);
    }

    public ItemStack getItem(BlockState blockState) {
        return new ItemStack(blockState.getBlock());
    }

    public ItemStack getItem(Material material) {
        return getItem(stateDefinition.any().setValue(variantProperty, material));
    }

    public BlockState getBlock(Material material) {
        return defaultBlockState().setValue(variantProperty, material);
    }

    public Material getGtMaterial(String id) {
        return variantProperty.getValue(id).get();
    }

    public InteractionResult replaceWithFramedPipe(Level worldIn, BlockPos pos, BlockState state, Player playerIn, ItemStack stackInHand, Direction facing) {
        BlockPipe<?, ?, ?> blockPipe = (BlockPipe<?, ?, ?>) ((ItemBlockPipe<?, ?>) stackInHand.getItem()).getBlock();
        if (blockPipe.getItemPipeType(stackInHand).getThickness() < 1) {
            BlockItem itemBlock = (BlockItem) stackInHand.getItem();
            BlockState pipeState = blockPipe.defaultBlockState();
            // these 0 values are not actually used by forge
            itemBlock.place(new BlockPlaceContext(playerIn, playerIn.getUsedItemHand(), stackInHand, new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, true)));
            IPipeTile<?, ?> pipeTile = blockPipe.getPipeTileEntity(worldIn, pos);
            if (pipeTile instanceof TileEntityPipeBase) {
                ((TileEntityPipeBase<?, ?>) pipeTile).setFrameMaterial(getGtMaterial(getMetaFromState(state)));
            } else {
                GregTech.LOGGER.error("Pipe was not placed!");
                return InteractionResult.PASS;
            }
            SoundType type = blockPipe.getSoundType(state, worldIn, pos, playerIn);
            worldIn.playSound(playerIn, pos, type.getPlaceSound(), SoundSource.BLOCKS, (type.getVolume() + 1.0F) / 2.0F, type.getPitch() * 0.8F);
            if (!playerIn.isCreative()) {
                stackInHand.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stackInHand = player.getItemInHand(hand);
        if (stackInHand.isEmpty()) {
            return null;
        }

        // replace frame with pipe and set the frame material to this frame
        if (stackInHand.getItem() instanceof ItemBlockPipe) {
            return replaceWithFramedPipe(level, pos, state, player, stackInHand, hit.getDirection());
        }

        if (!(stackInHand.getItem() instanceof FrameItemBlock)) {
            return InteractionResult.PASS;
        }
        BlockPos.MutableBlockPos blockPos = pos.mutable();
        blockPos.set(pos);
        for (int i = 0; i < 32; i++) {
            if (level.getBlockState(blockPos).getBlock() instanceof BlockFrame) {
                blockPos.move(Direction.UP);
                continue;
            }
            BlockEntity te = level.getBlockEntity(blockPos);
            if (te instanceof IPipeTile && ((IPipeTile<?, ?>) te).getFrameMaterial() != null) {
                blockPos.move(Direction.UP);
                continue;
            }
            if (canSurvive(state, level, pos)) {
                level.setBlock(blockPos, ((FrameItemBlock) stackInHand.getItem()).getBlockState(stackInHand), 3);
                SoundType type = getSoundType(stackInHand);
                level.playSound(null, pos, type.getPlaceSound(), SoundSource.BLOCKS, (type.getVolume() + 1.0F) / 2.0F, type.getPitch() * 0.8F);
                if (!player.isCreative()) {
                    stackInHand.shrink(1);
                }
                return InteractionResult.SUCCESS;
            } else if (te instanceof TileEntityPipeBase && ((TileEntityPipeBase<?, ?>) te).getFrameMaterial() == null) {
                Material material = ((BlockFrame) ((FrameItemBlock) stackInHand.getItem()).getBlock()).getGtMaterial(stackInHand.getMetadata());
                ((TileEntityPipeBase<?, ?>) te).setFrameMaterial(material);
                SoundType type = getSoundType(stackInHand);
                level.playSound(null, pos, type.getPlaceSound(), SoundSource.BLOCKS, (type.getVolume() + 1.0F) / 2.0F, type.getPitch() * 0.8F);
                if (!player.isCreative()) {
                    stackInHand.shrink(1);
                }
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.PASS;
            }
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return super.getStateForPlacement(pContext);
    }

    @Override
    public void onEntityCollision(Level worldIn, BlockPos pos, BlockState state, Entity entityIn) {
        Vec3 movement = entityIn.getDeltaMovement();
        movement = new Vec3(
                Mth.clamp(movement.x, -0.15, 0.15),
                movement.y,
                Mth.clamp(movement.z, -0.15, 0.15));
        entityIn.fallDistance = 0.0F;
        if (entityIn.getDeltaMovement().y < -0.15D) {
            movement = new Vec3(movement.x, movement.y - 0.15D, movement.z);
        }
        if (entityIn.isCrouching() && entityIn.getDeltaMovement().y < 0.0D) {
            movement = new Vec3(movement.x, 0, movement.z);
        }
        if (entityIn.horizontalCollision) {
            movement = new Vec3(movement.x, 0.3D, movement.z);
        }
        entityIn.setDeltaMovement(movement);
    }

    @Override
    @SuppressWarnings("deprecation")
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return COLLISION_BOX;
    }

    public RenderType getRenderType() {
        return RenderType.cutoutMipped();
    }

    @Override
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        for (BlockState state : this.stateDefinition.getPossibleStates()) {
            Material material = state.getValue(variantProperty);
            event.addSprite(GtMaterialIconTypes.frame.get().getBlockTexturePath(material.getMaterialIconSet()));
        }
    }

    @Override
    public void onModelRegister(ModelEvent.RegisterAdditional event) {
        Minecraft.getInstance().getModelManager().
        ModelLoader.setCustomStateMapper(this, new SimpleStateMapper(MODEL_LOCATION));
        for (BlockState state : this.getBlockState().getValidStates()) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), this.getMetaFromState(state), MODEL_LOCATION);
        }
    }
}
