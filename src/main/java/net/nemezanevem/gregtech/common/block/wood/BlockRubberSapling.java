package net.nemezanevem.gregtech.common.block.wood;

import gregtech.api.GregTechAPI;
import gregtech.common.worldgen.WorldGenRubberTree;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockGetter;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;

import javax.annotation.Nonnull;
import java.util.Random;

import static net.minecraft.block.BlockSapling.STAGE;

public class BlockRubberSapling extends BlockBush implements IGrowable {

    protected static final AxisAlignedBB SAPLING_AABB = new AxisAlignedBB(0.1, 0.0D, 0.1, 0.9, 0.8, 0.9);

    public BlockRubberSapling() {
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(STAGE, 0));
        setTranslationKey("rubber_sapling");
        setCreativeTab(GregTechAPI.TAB_GREGTECH);
        setHardness(0.0F);
        setSoundType(SoundType.PLANT);
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, STAGE);
    }

    @Override
    public void updateTick(World worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Random rand) {
        if (!worldIn.isClientSide) {
            super.updateTick(worldIn, pos, state, rand);
            if (!worldIn.isAreaLoaded(pos, 1))
                return;
            if (worldIn.getLightFromNeighbors(pos.up()) >= 9 && rand.nextInt(30) == 0) {
                this.grow(worldIn, rand, pos, state);
            }
        }
    }

    @Override
    @Nonnull
    @SuppressWarnings("deprecation")
    public BlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(STAGE, (meta & 8) >> 3);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        int i = 0;
        i |= state.getValue(STAGE) << 3;
        return i;
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getBoundingBox(@Nonnull BlockState state, @Nonnull BlockGetter source, @Nonnull BlockPos pos) {
        return SAPLING_AABB;
    }

    @Override
    public boolean canGrow(@Nonnull World world, @Nonnull BlockPos blockPos, @Nonnull BlockState BlockState, boolean b) {
        return true;
    }

    @Override
    public boolean canUseBonemeal(@Nonnull World world, @Nonnull Random random, @Nonnull BlockPos blockPos, @Nonnull BlockState BlockState) {
        return true;
    }

    @Override
    public boolean canBeReplacedByLeaves(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos) {
        return true;
    }

    @Override
    public void grow(@Nonnull World worldIn, @Nonnull Random rand, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        WorldGenRubberTree.TREE_GROW_INSTANCE.grow(worldIn, pos, rand);
    }

    @Override
    @Nonnull
    public EnumPlantType getPlantType(@Nonnull BlockGetter world, @Nonnull BlockPos pos) {
        return EnumPlantType.Plains;
    }
}
