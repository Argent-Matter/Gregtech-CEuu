package net.nemezanevem.gregtech.common.block;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.block.DelayedStateBlock;
import gregtech.client.model.IModelSupplier;
import gregtech.client.model.SimpleStateMapper;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.ItemBlockPipe;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.util.GTLog;
import gregtech.api.util.Util;
import gregtech.client.model.IModelSupplier;
import gregtech.client.model.SimpleStateMapper;
import gregtech.common.blocks.properties.PropertyMaterial;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.player.Player;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockGetter;
import net.minecraft.world.World;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.block.DelayedStateBlock;
import net.nemezanevem.gregtech.api.pipenet.block.BlockPipe;
import net.nemezanevem.gregtech.api.pipenet.block.ItemBlockPipe;
import net.nemezanevem.gregtech.api.pipenet.tile.IPipeTile;
import net.nemezanevem.gregtech.api.pipenet.tile.TileEntityPipeBase;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.client.model.IModelSupplier;
import net.nemezanevem.gregtech.common.block.properties.PropertyMaterial;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class BlockFrame extends DelayedStateBlock implements IModelSupplier {

    public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(new ResourceLocation(GregTech.MODID, "frame_block"), "normal");
    public static final AABB COLLISION_BOX = new AABB(0.05, 0.0, 0.05, 0.95, 1.0, 0.95);

    public final PropertyMaterial variantProperty;

    // todo wood?
    public BlockFrame(Material[] materials) {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("frame");
        setHardness(3.0f);
        setResistance(6.0f);
        setCreativeTab(GregTechAPI.TAB_GREGTECH_MATERIALS);
        this.variantProperty = PropertyMaterial.create("variant", materials);
        initBlockState();
    }

    @Override
    public int damageDropped(@Nonnull BlockState state) {
        return getMetaFromState(state);
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public BlockState getStateFromMeta(int meta) {
        if (meta >= variantProperty.getPossibleValues().size()) {
            meta = 0;
        }
        return defaultBlockState().setValue(variantProperty, variantProperty.getPossibleValues().get(meta));
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return variantProperty.getPossibleValues().indexOf(state.getValue(variantProperty));
    }

    @Override
    public String getHarvestTool(BlockState state) {
        Material material = state.getValue(variantProperty);
        if (ModHandler.isMaterialWood(material)) {
            return ToolClasses.AXE;
        }
        return ToolClasses.WRENCH;
    }

    @Nonnull
    @Override
    public SoundType getSoundType(BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nullable Entity entity) {
        Material material = state.getValue(variantProperty);
        if (ModHandler.isMaterialWood(material)) {
            return SoundType.WOOD;
        }
        return SoundType.METAL;
    }

    public SoundType getSoundType(ItemStack stack) {
        Material material = getGtMaterial(stack.getMetadata());
        if (ModHandler.isMaterialWood(material)) {
            return SoundType.WOOD;
        }
        return SoundType.METAL;
    }

    @Override
    public int getHarvestLevel(@Nonnull BlockState state) {
        return 1;
    }

    @Override
    @Nonnull
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
        return getItem(defaultBlockState().withProperty(variantProperty, material));
    }

    public BlockState getBlock(Material material) {
        return defaultBlockState().setValue(variantProperty, material);
    }

    public Material getGtMaterial(int meta) {
        return variantProperty.getPossibleValues().get(meta);
    }

    public boolean replaceWithFramedPipe(Level worldIn, BlockPos pos, BlockState state, Player playerIn, ItemStack stackInHand, Direction facing) {
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
                return false;
            }
            SoundType type = blockPipe.getSoundType(state, worldIn, pos, playerIn);
            worldIn.playSound(playerIn, pos, type.getPlaceSound(), SoundCategory.BLOCKS, (type.getVolume() + 1.0F) / 2.0F, type.getPitch() * 0.8F);
            if (!playerIn.capabilities.isCreativeMode) {
                stackInHand.shrink(1);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, Player playerIn, @Nonnull InteractionHand hand, @Nonnull Direction facing, float hitX, float hitY, float hitZ) {
        ItemStack stackInHand = playerIn.getHeldItem(hand);
        if (stackInHand.isEmpty()) {
            return false;
        }
        // replace frame with pipe and set the frame material to this frame
        if (stackInHand.getItem() instanceof ItemBlockPipe) {
            return replaceWithFramedPipe(worldIn, pos, state, playerIn, stackInHand, facing);
        }

        if (!(stackInHand.getItem() instanceof FrameItemBlock)) {
            return false;
        }
        BlockPos.PooledMutableBlockPos blockPos = BlockPos.PooledMutableBlockPos.retain();
        blockPos.setPos(pos);
        for (int i = 0; i < 32; i++) {
            if (worldIn.getBlockState(blockPos).getBlock() instanceof BlockFrame) {
                blockPos.move(Direction.UP);
                continue;
            }
            TileEntity te = worldIn.getTileEntity(blockPos);
            if (te instanceof IPipeTile && ((IPipeTile<?, ?>) te).getFrameMaterial() != null) {
                blockPos.move(Direction.UP);
                continue;
            }
            if (canPlaceBlockAt(worldIn, blockPos)) {
                worldIn.setBlockState(blockPos, ((FrameItemBlock) stackInHand.getItem()).getBlockState(stackInHand));
                SoundType type = getSoundType(stackInHand);
                worldIn.playSound(null, pos, type.getPlaceSound(), SoundCategory.BLOCKS, (type.getVolume() + 1.0F) / 2.0F, type.getPitch() * 0.8F);
                if (!playerIn.capabilities.isCreativeMode) {
                    stackInHand.shrink(1);
                }
                blockPos.release();
                return true;
            } else if (te instanceof TileEntityPipeBase && ((TileEntityPipeBase<?, ?>) te).getFrameMaterial() == null) {
                Material material = ((BlockFrame) ((FrameItemBlock) stackInHand.getItem()).getBlock()).getGtMaterial(stackInHand.getMetadata());
                ((TileEntityPipeBase<?, ?>) te).setFrameMaterial(material);
                SoundType type = getSoundType(stackInHand);
                worldIn.playSound(null, pos, type.getPlaceSound(), SoundCategory.BLOCKS, (type.getVolume() + 1.0F) / 2.0F, type.getPitch() * 0.8F);
                if (!playerIn.capabilities.isCreativeMode) {
                    stackInHand.shrink(1);
                }
                blockPos.release();
                return true;
            } else {
                blockPos.release();
                return false;
            }
        }
        blockPos.release();
        return false;
    }

    @Override
    public void onEntityCollision(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, Entity entityIn) {
        entityIn.motionX = MathHelper.clamp(entityIn.motionX, -0.15, 0.15);
        entityIn.motionZ = MathHelper.clamp(entityIn.motionZ, -0.15, 0.15);
        entityIn.fallDistance = 0.0F;
        if (entityIn.motionY < -0.15D) {
            entityIn.motionY = -0.15D;
        }
        if (entityIn.isSneaking() && entityIn.motionY < 0.0D) {
            entityIn.motionY = 0.0D;
        }
        if (entityIn.collidedHorizontally) {
            entityIn.motionY = 0.3;
        }
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public EnumPushReaction getPushReaction(@Nonnull BlockState state) {
        return EnumPushReaction.DESTROY;
    }

    @Override
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getCollisionBoundingBox(@Nonnull BlockState blockState, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos) {
        return COLLISION_BOX;
    }

    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(@Nonnull BlockState state) {
        return false;
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public BlockFaceShape getBlockFaceShape(@Nonnull BlockGetter worldIn, @Nonnull BlockState state, @Nonnull BlockPos pos, @Nonnull Direction face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        for (BlockState state : this.getBlockState().getValidStates()) {
            Material material = state.getValue(variantProperty);
            event.getMap().registerSprite(MaterialIconType.frameGt.getBlockTexturePath(material.getMaterialIconSet()));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onModelRegister() {
        ModelLoader.setCustomStateMapper(this, new SimpleStateMapper(MODEL_LOCATION));
        for (BlockState state : this.getBlockState().getValidStates()) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), this.getMetaFromState(state), MODEL_LOCATION);
        }
    }
}
