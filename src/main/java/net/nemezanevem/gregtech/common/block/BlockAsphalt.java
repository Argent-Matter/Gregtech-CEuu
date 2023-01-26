package net.nemezanevem.gregtech.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.nemezanevem.gregtech.api.block.IStateHarvestLevel;
import net.nemezanevem.gregtech.api.block.VariantBlock;

import javax.annotation.Nonnull;

public class BlockAsphalt extends VariantBlock<BlockAsphalt.BlockType> {

    public BlockAsphalt() {
        super(Properties.of(Material.METAL).sound(SoundType.METAL).strength(5.0f, 10.0f).isValidSpawn((pState, pLevel, pPos, pValue) -> false));
        this.registerDefaultState(getState(BlockType.ASPHALT));
    }

    @Override
    public void stepOn(@Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState pState, Entity entityIn) {
        var movement = entityIn.getDeltaMovement();
        if ((movement.x != 0 || movement.z != 0) && !entityIn.isInWater() && !entityIn.isCrouching()) {
            entityIn.setDeltaMovement(movement.x * 1.3, movement.y, movement.z * 1.3);
        }
    }

    public enum BlockType implements StringRepresentable, IStateHarvestLevel {

        ASPHALT("asphalt", 1);

        private final String name;
        private final int harvestLevel;

        BlockType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Nonnull
        @Override
        public String getSerializedName() {
            return this.name;
        }

        @Override
        public int getHarvestLevel(BlockState state) {
            return harvestLevel;
        }
    }
}
