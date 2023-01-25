package net.nemezanevem.gregtech.api.block.machine;

import codechicken.lib.raytracer.IndexedVoxelShape;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Material;
import net.nemezanevem.gregtech.api.block.BlockCustomParticle;
import net.nemezanevem.gregtech.api.pipenet.IBlockAppearance;
import net.nemezanevem.gregtech.integration.IFacadeWrapper;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class BlockMachine extends BlockCustomParticle implements EntityBlock, IFacadeWrapper, IBlockAppearance {

    private static final List<IndexedVoxelShape> EMPTY_COLLISION_BOX = Collections.emptyList();
    //used for rendering purposes of non-opaque machines like chests and tanks
    public static final BooleanProperty OPAQUE = BooleanProperty.create("opaque");

    // Vanilla MC's getHarvestTool() and getHarvestLevel() only pass the state, which is
    // not enough information to get the harvest tool and level from a MetaTileEntity on its own.
    // Using unlisted properties lets us get this information from getActualState(), which
    // provides enough information to get and read the MetaTileEntity data.
    private static final Property<String> HARVEST_TOOL = new Property<String>("harvest_tool");
    private static final Property<Integer> HARVEST_LEVEL = new UnlistedIntegerProperty("harvest_level");

    public BlockMachine() {
        super(BlockBehaviour.Properties.of(Material.METAL).strength(6.0f, 6.0f).sound(SoundType.METAL));
        setCreativeTab(GregTechAPI.TAB_GREGTECH);
        setDefaultState(getDefaultState().withProperty(OPAQUE, true));
    }

    @Nullable
    @Override
    public String getHarvestTool(@Nonnull BlockState state) {
        String value = ((IExtendedBlockState) state).getValue(HARVEST_TOOL);
        return value == null ? ToolClasses.WRENCH : value;
    }

    @Override
    public int getHarvestLevel(@Nonnull BlockState state) {
        Integer value = ((IExtendedBlockState) state).getValue(HARVEST_LEVEL);
        return value == null ? 1 : value;
    }

    @Override
    public boolean causesSuffocation(BlockState state) {
        return state.getValue(OPAQUE);
    }

    @Nonnull
    @Override
    public BlockState getActualState(@Nonnull BlockState state, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(worldIn, pos);
        if (metaTileEntity == null) return state;

        return ((IExtendedBlockState) state)
                .withProperty(HARVEST_TOOL, metaTileEntity.getHarvestTool())
                .withProperty(HARVEST_LEVEL, metaTileEntity.getHarvestLevel());
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[]{OPAQUE}, new IUnlistedProperty[]{HARVEST_TOOL, HARVEST_LEVEL});
    }

    @Override
    public float getPlayerRelativeBlockHardness(@Nonnull BlockState state, @Nonnull EntityPlayer player, @Nonnull Level worldIn, @Nonnull BlockPos pos) {
        // make sure our extended block state info is here for callers (since forge does not do it for us in this case)
        state = state.getBlock().getActualState(state, worldIn, pos);
        return super.getPlayerRelativeBlockHardness(state, player, worldIn, pos);
    }

    @Nonnull
    @Override
    public BlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(OPAQUE, meta % 2 == 0);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(OPAQUE) ? 0 : 1;
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull BlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull SpawnPlacementType type) {
        return false;
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

    private List<IndexedCuboid6> getCollisionBox(IBlockAccess blockAccess, BlockPos pos) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(blockAccess, pos);
        if (metaTileEntity == null)
            return EMPTY_COLLISION_BOX;
        ArrayList<IndexedCuboid6> collisionList = new ArrayList<>();
        metaTileEntity.addCollisionBoundingBox(collisionList);
        metaTileEntity.addCoverCollisionBoundingBox(collisionList);
        return collisionList;
    }

    @Override
    public boolean doesSideBlockRendering(@Nonnull BlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull Direction face) {
        return state.isOpaqueCube() && getMetaTileEntity(world, pos) != null;
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull BlockState state, @Nonnull RayTraceResult target, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        if (metaTileEntity == null)
            return ItemStack.EMPTY;
        if (target instanceof CuboidRayTraceResult) {
            return metaTileEntity.getPickItem((CuboidRayTraceResult) target, player);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void addCollisionBoxToList(@Nonnull BlockState state, @Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        for (Cuboid6 axisAlignedBB : getCollisionBox(worldIn, pos)) {
            AxisAlignedBB offsetBox = axisAlignedBB.aabb().offset(pos);
            if (offsetBox.intersects(entityBox)) collidingBoxes.add(offsetBox);
        }
    }

    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(@Nonnull BlockState blockState, @Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
        return RayTracer.rayTraceCuboidsClosest(start, end, pos, getCollisionBox(worldIn, pos));
    }

    @Override
    public boolean rotateBlock(@Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Direction axis) {
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
        return Arrays.stream(Direction.VALUES)
                .filter(metaTileEntity::isValidFrontFacing)
                .toArray(Direction[]::new);
    }

    @Override
    public boolean recolorBlock(@Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Direction side, @Nonnull EnumDyeColor color) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        if (metaTileEntity == null || metaTileEntity.getPaintingColor() == color.colorValue)
            return false;
        metaTileEntity.setPaintingColor(color.colorValue);
        return true;
    }

    @Override
    public void onBlockPlacedBy(Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull EntityLivingBase placer, ItemStack stack) {
        IGregTechTileEntity holder = (IGregTechTileEntity) worldIn.getTileEntity(pos);
        MetaTileEntity sampleMetaTileEntity = GregTechAPI.MTE_REGISTRY.getObjectById(stack.getItemDamage());
        if (holder != null && sampleMetaTileEntity != null) {
            // TODO Fix this
            if (stack.hasDisplayName() && holder instanceof MetaTileEntityHolder) {
                ((MetaTileEntityHolder) holder).setCustomName(stack.getDisplayName());
            }
            MetaTileEntity metaTileEntity = holder.setMetaTileEntity(sampleMetaTileEntity);
            if (stack.hasTagCompound()) {
                //noinspection ConstantConditions
                metaTileEntity.initFromItemStackData(stack.getTagCompound());
            }
            if (metaTileEntity.isValidFrontFacing(Direction.UP)) {
                metaTileEntity.setFrontFacing(Direction.getDirectionFromEntityLiving(pos, placer));
            } else {
                metaTileEntity.setFrontFacing(placer.getHorizontalFacing().getOpposite());
            }
            if (Loader.isModLoaded(GTValues.MODID_APPENG)) {
                if (metaTileEntity.getProxy() != null) {
                    metaTileEntity.getProxy().setOwner((EntityPlayer) placer);
                }
            }

            // Color machines on place if holding spray can in off-hand
            if (placer instanceof EntityPlayer) {
                ItemStack offhand = placer.getHeldItemOffhand();
                for (int i  = 0; i < EnumDyeColor.values().length; i++) {
                    if (offhand.isItemEqual(MetaItems.SPRAY_CAN_DYES[i].getStackForm())) {
                        MetaItems.SPRAY_CAN_DYES[i].getBehaviours().get(0).onItemUse((EntityPlayer) placer, worldIn, pos, EnumHand.OFF_HAND, Direction.UP, 0, 0 , 0);
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
                    Block.spawnAsEntity(worldIn, pos, itemStack);
                }
            }
            metaTileEntity.dropAllCovers();
            metaTileEntity.onRemoval();

            tileEntities.set(metaTileEntity);
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void getDrops(@Nonnull NonNullList<ItemStack> drops, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull BlockState state, int fortune) {
        MetaTileEntity metaTileEntity = tileEntities.get() == null ? getMetaTileEntity(world, pos) : tileEntities.get();
        if (metaTileEntity == null) return;
        if (!metaTileEntity.shouldDropWhenDestroyed())
            return;
        ItemStack itemStack = metaTileEntity.getStackForm();
        NBTTagCompound tagCompound = new NBTTagCompound();
        metaTileEntity.writeItemStackData(tagCompound);
        //only set item tag if it's not empty, so newly created items will stack with dismantled
        if (!tagCompound.isEmpty())
            itemStack.setTagCompound(tagCompound);
        // TODO Clean this up
        if (metaTileEntity.getHolder() instanceof MetaTileEntityHolder) {
            MetaTileEntityHolder holder = (MetaTileEntityHolder) metaTileEntity.getHolder();
            if (holder.hasCustomName()) {
                itemStack.setStackDisplayName(holder.getName());
            }
        }
        drops.add(itemStack);
        metaTileEntity.getDrops(drops, harvesters.get());
    }

    @Override
    public boolean onBlockActivated(@Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull Direction facing, float hitX, float hitY, float hitZ) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(worldIn, pos);
        CuboidRayTraceResult rayTraceResult = (CuboidRayTraceResult) RayTracer.retraceBlock(worldIn, playerIn, pos);
        ItemStack itemStack = playerIn.getHeldItem(hand);
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
    public void onBlockClicked(@Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull EntityPlayer playerIn) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(worldIn, pos);
        if (metaTileEntity == null) return;
        CuboidRayTraceResult rayTraceResult = (CuboidRayTraceResult) RayTracer.retraceBlock(worldIn, playerIn, pos);
        if (rayTraceResult != null) {
            metaTileEntity.onCoverLeftClick(playerIn, rayTraceResult);
        }
    }

    @Override
    public boolean canConnectRedstone(@Nonnull BlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable Direction side) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        return metaTileEntity != null && metaTileEntity.canConnectRedstone(side == null ? null : side.getOpposite());
    }

    @Override
    public boolean shouldCheckWeakPower(@Nonnull BlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull Direction side) {
        // The check in Level::getRedstonePower in the vanilla code base is reversed. Setting this to false will
        // actually cause getWeakPower to be called, rather than prevent it.
        return false;
    }

    @Override
    public int getWeakPower(@Nonnull BlockState blockState, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos pos, @Nonnull Direction side) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(blockAccess, pos);
        return metaTileEntity == null ? 0 : metaTileEntity.getOutputRedstoneSignal(side == null ? null : side.getOpposite());
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
    public void harvestBlock(@Nonnull Level worldIn, @Nonnull EntityPlayer player, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable TileEntity te, @Nonnull ItemStack stack) {
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
    public TileEntity createNewTileEntity(@Nullable Level worldIn, int meta) {
        return new MetaTileEntityHolder();
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    public EnumBlockRenderType getRenderType(@Nonnull BlockState state) {
        return MetaTileEntityRenderer.BLOCK_RENDER_TYPE;
    }

    @Override
    public boolean canRenderInLayer(@Nonnull BlockState state, @Nonnull BlockRenderLayer layer) {
        return true;
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return state.getValue(OPAQUE);
    }

    @Override
    public boolean isFullCube(BlockState state) {
        return state.getValue(OPAQUE);
    }

    @Nonnull
    @Override
    public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull BlockState state, @Nonnull BlockPos pos, @Nonnull Direction face) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(worldIn, pos);
        return metaTileEntity == null ? BlockFaceShape.SOLID : metaTileEntity.getCoverFaceShape(face);
    }

    @Override
    public int getLightValue(@Nonnull BlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        //why mc is so fucking retarded to call this method on fucking NEIGHBOUR BLOCKS!
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        return metaTileEntity == null ? 0 : metaTileEntity.getLightValue();
    }

    @Override
    public int getLightOpacity(@Nonnull BlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        //why mc is so fucking retarded to call this method on fucking NEIGHBOUR BLOCKS!
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        return metaTileEntity == null ? 0 : metaTileEntity.getLightOpacity();
    }

    @Override
    public void getSubBlocks(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
        for (MetaTileEntity metaTileEntity : GregTechAPI.MTE_REGISTRY) {
            metaTileEntity.getSubItems(tab, items);
        }
    }

    @Nonnull
    @Override
    public BlockState getFacade(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable Direction side, @Nonnull BlockPos otherPos) {
        return getFacade(world, pos, side);
    }

    @Nonnull
    @Override
    public BlockState getFacade(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, Direction side) {
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
    public BlockState getVisualState(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull Direction side) {
        return getFacade(world, pos, side);
    }

    @Override
    public boolean supportsVisualConnections() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected Pair<TextureAtlasSprite, Integer> getParticleTexture(Level world, BlockPos blockPos) {
        return MetaTileEntityRenderer.INSTANCE.getParticleTexture(world, blockPos);
    }

    @Override
    public boolean canEntityDestroy(@Nonnull BlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull Entity entity) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        if(metaTileEntity == null) {
            return super.canEntityDestroy(state, world, pos, entity);
        }
        return !((entity instanceof EntityWither || entity instanceof EntityWitherSkull) && metaTileEntity.getWitherProof());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(@Nonnull BlockState stateIn, @Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull Random rand) {
        super.randomDisplayTick(stateIn, worldIn, pos, rand);
        MetaTileEntity metaTileEntity = getMetaTileEntity(worldIn, pos);
        if (metaTileEntity != null) metaTileEntity.randomDisplayTick();
    }
}
