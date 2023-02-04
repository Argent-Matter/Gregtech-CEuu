package net.nemezanevem.gregtech.common.block;


import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.nemezanevem.gregtech.api.block.VariantActiveBlock;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class BlockGlassCasing extends VariantActiveBlock<BlockGlassCasing.CasingType> {

    public BlockGlassCasing() {
        super(BlockBehaviour.Properties.of(Material.METAL).sound(SoundType.GLASS).strength(5.0f, 5.0f).isValidSpawn((pState, pLevel, pPos, pValue) -> false));
        registerDefaultState(getState(CasingType.TEMPERED_GLASS));
    }

    @Nonnull
    public RenderType getRenderLayer() {
        return RenderType.cutout();
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    public boolean skipRendering(BlockState pState, BlockState pAdjacentBlockState, Direction pDirection) {
        Block block = pAdjacentBlockState.getBlock();

        return block != this && super.skipRendering(pState, pAdjacentBlockState, pDirection);
    }

    public enum CasingType implements StringRepresentable {

        TEMPERED_GLASS("tempered_glass"),
        FUSION_GLASS("fusion_glass"),
        LAMINATED_GLASS("laminated_glass"),
        CLEANROOM_GLASS("cleanroom_glass");

        private final String name;

        CasingType(String name) {
            this.name = name;
        }

        @Override
        @Nonnull
        public String getSerializedName() {
            return this.name;
        }

    }
}
