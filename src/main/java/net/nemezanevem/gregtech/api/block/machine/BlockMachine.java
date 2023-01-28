package net.nemezanevem.gregtech.api.block.machine;

import codechicken.lib.raytracer.IndexedVoxelShape;
import codechicken.lib.raytracer.RayTracer;
import codechicken.lib.raytracer.VoxelShapeBlockHitResult;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.block.BlockCustomParticle;
import net.nemezanevem.gregtech.api.cover.CoverBehavior;
import net.nemezanevem.gregtech.api.cover.IFacadeCover;
import net.nemezanevem.gregtech.api.item.toolitem.ToolClass;
import net.nemezanevem.gregtech.api.pipenet.IBlockAppearance;
import net.nemezanevem.gregtech.api.registry.tileentity.MetaTileEntityRegistry;
import net.nemezanevem.gregtech.api.tileentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.tileentity.MetaTileEntityHolder;
import net.nemezanevem.gregtech.api.tileentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.common.block.properties.StringProperty;
import net.nemezanevem.gregtech.integration.IFacadeWrapper;
import org.checkerframework.checker.units.qual.C;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.tools.Tool;
import java.util.*;

import static net.nemezanevem.gregtech.api.util.Util.getMetaTileEntity;

public class BlockMachine extends BlockCustomParticle implements EntityBlock, IFacadeWrapper, IBlockAppearance {

    private static final List<IndexedVoxelShape> EMPTY_COLLISION_BOX = Collections.emptyList();
    //used for rendering purposes of non-opaque machines like chests and tanks
    public static final BooleanProperty OPAQUE = BooleanProperty.create("opaque");

    // Vanilla MC's getHarvestTool() and getHarvestLevel() only pass the state, which is
    // not enough information to get the harvest tool and level from a MetaTileEntity on its own.
    // Using unlisted properties lets us get this information from getActualState(), which
    // provides enough information to get and read the MetaTileEntity data.
    private static final EnumProperty<ToolClass> HARVEST_TOOL = EnumProperty.create("harvest_tool", ToolClass.class);
    private static final IntegerProperty HARVEST_LEVEL = IntegerProperty.create("harvest_level", 0, 10);

    public BlockMachine() {
        super(BlockBehaviour.Properties.of(Material.METAL).strength(6.0f, 6.0f).sound(SoundType.METAL).isSuffocating(((pState, pLevel, pPos) -> pState.getValue(OPAQUE))).isValidSpawn(((pState, pLevel, pPos, pValue) -> false)));
        registerDefaultState(stateDefinition.any().setValue(OPAQUE, true).setValue(HARVEST_LEVEL, 0).setValue(HARVEST_TOOL, ToolClass.WRENCH));
    }

    @Nullable
    @Override
    public ToolClass getHarvestTool(@Nonnull BlockState state) {
        return state.getValue(HARVEST_TOOL);
    }

    @Override
    public int getHarvestLevel(@Nonnull BlockState state) {
        Integer value = state.getValue(HARVEST_LEVEL);
        return value == null ? 1 : value;
    }

    @Override
    public float getBlockHardness(@Nonnull BlockState blockState, @Nonnull Level worldIn, @Nonnull BlockPos pos) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(worldIn, pos);
        return metaTileEntity == null ? 1.0f : metaTileEntity.getBlockHardness();
    }

    @Override
    public float getExplosionResistance(@Nonnull Level world, @Nonnull BlockPos pos, @Nullable Entity exploder, @Nonnull Explosion explosion) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        return metaTileEntity == null ? 1.0f : metaTileEntity.getBlockResistance();
    }

    private List<IndexedVoxelShape> getCollisionBox(BlockGetter blockAccess, BlockPos pos) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(blockAccess, pos);
        if (metaTileEntity == null)
            return EMPTY_COLLISION_BOX;
        ArrayList<IndexedVoxelShape> collisionList = new ArrayList<>();
        metaTileEntity.addCollisionBoundingBox(collisionList);
        metaTileEntity.addCoverCollisionBoundingBox(collisionList);
        return collisionList;
    }

    @Override
    public boolean doesSideBlockRendering(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nonnull Direction face) {
        return state.getValue(OPAQUE) && getMetaTileEntity(world, pos) != null;
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull BlockState state, @Nonnull HitResult target, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Player player) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        if (metaTileEntity == null)
            return ItemStack.EMPTY;
        if (target instanceof VoxelShapeBlockHitResult) {
            return metaTileEntity.getPickItem((VoxelShapeBlockHitResult) target, player);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean rotate(@Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Direction axis) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        if (metaTileEntity == null) return false;
        if (metaTileEntity.hasFrontFacing() && metaTileEntity.isValidFrontFacing(axis)) {
            metaTileEntity.setFrontFacing(axis);
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public Direction[] getValidRotations(@Nonnull Level world, @Nonnull BlockPos pos) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        if (metaTileEntity == null || !metaTileEntity.hasFrontFacing()) return null;
        return Arrays.stream(Direction.values())
                .filter(metaTileEntity::isValidFrontFacing)
                .toArray(Direction[]::new);
    }

    @Override
    public boolean recolorBlock(@Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Direction side, @Nonnull DyeColor color) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        if (metaTileEntity == null || metaTileEntity.getPaintingColor() == color.getTextColor())
            return false;
        metaTileEntity.setPaintingColor(color.getFireworkColor());
        return true;
    }

    @Override
    public void setPlacedBy(Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        IGregTechTileEntity holder = (IGregTechTileEntity) worldIn.getBlockEntity(pos);
        MetaTileEntity sampleMetaTileEntity = MetaTileEntityRegistry.META_TILE_ENTITIES_BUILTIN.get().getValue(Util.getId(stack.getItem()));
        if (holder != null && sampleMetaTileEntity != null) {
            // TODO Fix this
            if (stack.hasCustomHoverName() && holder instanceof MetaTileEntityHolder) {
                ((MetaTileEntityHolder) holder).setCustomName(stack.getDisplayName());
            }
            MetaTileEntity metaTileEntity = holder.setMetaTileEntity(sampleMetaTileEntity);
            if (stack.hasTag()) {
                //noinspection ConstantConditions
                metaTileEntity.initFromItemStackData(stack.getTag());
            }
            if (metaTileEntity.isValidFrontFacing(Direction.UP)) {
                Vec3 lookAngle = placer.getLookAngle();
                metaTileEntity.setFrontFacing(Direction.getNearest(lookAngle.x, lookAngle.y, lookAngle.z));
            } else {
                metaTileEntity.setFrontFacing(placer.getDirection().getOpposite());
            }
            if (ModList.get().isLoaded(GTValues.MODID_APPENG)) {
                if (metaTileEntity.getProxy() != null) {
                    metaTileEntity.getProxy().setOwner((Player) placer);
                }
            }

            // Color machines on place if holding spray can in off-hand
            if (placer instanceof Player) {
                ItemStack offhand = placer.getOffhandItem();
                for (int i  = 0; i < DyeColor.values().length; i++) {
                    if (ItemStack.matches(offhand, MetaItems.SPRAY_CAN_DYES[i].getStackForm())) {
                        MetaItems.SPRAY_CAN_DYES[i].getBehaviours().get(0).onItemUse((Player) placer, worldIn, pos, InteractionHand.OFF_HAND, Direction.UP, 0, 0 , 0);
                        break;
                    }
                }
            }

            metaTileEntity.onPlacement();
        }
    }

    @Override
    public void breakBlock(@Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(worldIn, pos);
        if (metaTileEntity != null) {
            if (!metaTileEntity.keepsInventory()) {
                NonNullList<ItemStack> inventoryContents = NonNullList.create();
                metaTileEntity.clearMachineInventory(inventoryContents);
                for (ItemStack itemStack : inventoryContents) {
                    Block.popResource(worldIn, pos, itemStack);
                }
            }
            metaTileEntity.dropAllCovers();
            metaTileEntity.onRemoval();

            tileEntities.set(metaTileEntity);
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void getDrops(@Nonnull NonNullList<ItemStack> drops, @Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nonnull BlockState state, int fortune) {
        MetaTileEntity metaTileEntity = tileEntities.get() == null ? getMetaTileEntity(world, pos) : tileEntities.get();
        if (metaTileEntity == null) return;
        if (!metaTileEntity.shouldDropWhenDestroyed())
            return;
        ItemStack itemStack = metaTileEntity.getStackForm();
        CompoundTag tagCompound = new CompoundTag();
        metaTileEntity.writeItemStackData(tagCompound);
        //only set item tag if it's not empty, so newly created items will stack with dismantled
        if (!tagCompound.isEmpty())
            itemStack.setTag(tagCompound);
        // TODO Clean this up
        if (metaTileEntity.getHolder() instanceof MetaTileEntityHolder) {
            MetaTileEntityHolder holder = (MetaTileEntityHolder) metaTileEntity.getHolder();
            if (holder.hasCustomName()) {
                itemStack.setHoverName(holder.getName());
            }
        }
        drops.add(itemStack);
        metaTileEntity.getDrops(drops, harvesters.get());
    }

    @Override
    public boolean onBlockActivated(@Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Player playerIn, @Nonnull InteractionHand hand, @Nonnull Direction facing, float hitX, float hitY, float hitZ) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(worldIn, pos);
        VoxelShapeBlockHitResult rayTraceResult = (VoxelShapeBlockHitResult) RayTracer.retraceBlock(worldIn, playerIn, pos);
        ItemStack itemStack = playerIn.getItemInHand(hand);
        if (metaTileEntity == null || rayTraceResult == null) {
            return false;
        }

        // try to click with a tool first
        Set<String> toolClasses = itemStack.getItem().getToolClasses(itemStack);
        if (!toolClasses.isEmpty() && metaTileEntity.onToolClick(playerIn, toolClasses, hand, rayTraceResult)) {
            ToolHelper.damageItem(itemStack, playerIn);
            if (itemStack.getItem() instanceof IGTTool) {
                ((IGTTool) itemStack.getItem()).playSound(playerIn);
            }
            return true;
        }

        // then try to click with normal right hand
        return metaTileEntity.onRightClick(playerIn, hand, facing, rayTraceResult);
    }

    @Override
    public void onBlockClicked(@Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull Player playerIn) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(worldIn, pos);
        if (metaTileEntity == null) return;
        VoxelShapeBlockHitResult rayTraceResult = (VoxelShapeBlockHitResult) RayTracer.retraceBlock(worldIn, playerIn, pos);
        if (rayTraceResult != null) {
            metaTileEntity.onCoverLeftClick(playerIn, rayTraceResult);
        }
    }

    @Override
    public boolean canConnectRedstone(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nullable Direction side) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        return metaTileEntity != null && metaTileEntity.canConnectRedstone(side == null ? null : side.getOpposite());
    }

    @Override
    public boolean shouldCheckWeakPower(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nonnull Direction side) {
        // The check in Level::getRedstonePower in the vanilla code base is reversed. Setting this to false will
        // actually cause getWeakPower to be called, rather than prevent it.
        return false;
    }

    @Override
    public int getWeakPower(@Nonnull BlockState blockState, @Nonnull BlockGetter blockAccess, @Nonnull BlockPos pos, @Nonnull Direction side) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(blockAccess, pos);
        return metaTileEntity == null ? 0 : metaTileEntity.getOutputRedstoneSignal(side.getOpposite());
    }

    @Override
    public void neighborChanged(@Nonnull BlockState state, @Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(worldIn, pos);
        if (metaTileEntity != null) {
            metaTileEntity.updateInputRedstoneSignals();
            metaTileEntity.onNeighborChanged();
        }
    }

    @Override
    public int getComparatorInputOverride(@Nonnull BlockState blockState, @Nonnull Level worldIn, @Nonnull BlockPos pos) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(worldIn, pos);
        return metaTileEntity == null ? 0 : metaTileEntity.getComparatorValue();
    }

    protected final ThreadLocal<MetaTileEntity> tileEntities = new ThreadLocal<>();

    @Override
    public void harvestBlock(@Nonnull Level worldIn, @Nonnull Player player, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable BlockEntity te, @Nonnull ItemStack stack) {
        tileEntities.set(te == null ? tileEntities.get() : ((IGregTechTileEntity) te).getMetaTileEntity());
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        tileEntities.set(null);
    }

    @Override
    public boolean hasComparatorInputOverride(@Nonnull BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState blockState) {
        return new MetaTileEntityHolder(TileEntities.MACHINE, pos, blockState);
    }

    @Nonnull
    @Override
    public RenderType getRenderType(@Nonnull BlockState state) {
        return MetaTileEntityRenderer.BLOCK_RENDER_TYPE;
    }

    @Nonnull
    @Override
    public BlockFaceShape getBlockFaceShape(@Nonnull BlockGetter worldIn, @Nonnull BlockState state, @Nonnull BlockPos pos, @Nonnull Direction face) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(worldIn, pos);
        return metaTileEntity == null ? BlockFaceShape.SOLID : metaTileEntity.getCoverFaceShape(face);
    }

    @Override
    public int getLightValue(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos) {
        //why mc is so fucking retarded to call this method on fucking NEIGHBOUR BLOCKS!
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        return metaTileEntity == null ? 0 : metaTileEntity.getLightValue();
    }

    @Override
    public int getLightOpacity(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos) {
        //why mc is so fucking retarded to call this method on fucking NEIGHBOUR BLOCKS!
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        return metaTileEntity == null ? 0 : metaTileEntity.getLightOpacity();
    }

    @Override
    public void fillItemCategory(CreativeModeTab pTab, NonNullList<ItemStack> pItems) {
        for (MetaTileEntity metaTileEntity : MetaTileEntityRegistry.META_TILE_ENTITIES_BUILTIN.get().getValues()) {
            metaTileEntity.getSubItems(pTab, pItems);
        }
    }

    @Nonnull
    @Override
    public BlockState getFacade(@Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nullable Direction side, @Nonnull BlockPos otherPos) {
        return getFacade(world, pos, side);
    }

    @Nonnull
    @Override
    public BlockState getFacade(@Nonnull BlockGetter world, @Nonnull BlockPos pos, Direction side) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        if (metaTileEntity != null && side != null) {
            CoverBehavior coverBehavior = metaTileEntity.getCoverAtSide(side);
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

    @Override
    protected Pair<TextureAtlasSprite, Integer> getParticleTexture(Level world, BlockPos blockPos) {
        return MetaTileEntityRenderer.INSTANCE.getParticleTexture(world, blockPos);
    }

    @Override
    public boolean canEntityDestroy(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nonnull Entity entity) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        if(metaTileEntity == null) {
            return super.canEntityDestroy(state, world, pos, entity);
        }
        return !((entity instanceof WitherBoss || entity instanceof WitherSkull) && metaTileEntity.getWitherProof());
    }

    @Override
    public void randomDisplayTick(@Nonnull BlockState stateIn, @Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull RandomSource rand) {
        super.randomDisplayTick(stateIn, worldIn, pos, rand);
        MetaTileEntity metaTileEntity = getMetaTileEntity(worldIn, pos);
        if (metaTileEntity != null) metaTileEntity.randomDisplayTick();
    }
}
