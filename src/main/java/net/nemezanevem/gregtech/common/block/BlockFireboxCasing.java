package net.nemezanevem.gregtech.common.block;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantActiveBlock;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.common.blocks.BlockFireboxCasing.FireboxCasingType;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Material;
import net.nemezanevem.gregtech.api.block.IStateHarvestLevel;
import net.nemezanevem.gregtech.api.block.VariantActiveBlock;

import javax.annotation.Nonnull;

public class BlockFireboxCasing extends VariantActiveBlock<BlockFireboxCasing.FireboxCasingType> {

    public BlockFireboxCasing() {
        super(Properties.of(Material.METAL).strength(5.0f, 10.0f).sound(SoundType.METAL).isValidSpawn((pState, pLevel, pPos, pValue) -> false));
        registerDefaultState(getState(FireboxCasingType.BRONZE_FIREBOX));
    }

    public enum FireboxCasingType implements StringRepresentable, IStateHarvestLevel {

        BRONZE_FIREBOX("bronze_firebox", 1),
        STEEL_FIREBOX("steel_firebox", 2),
        TITANIUM_FIREBOX("titanium_firebox", 2),
        TUNGSTENSTEEL_FIREBOX("tungstensteel_firebox", 3);

        private final String name;
        private final int harvestLevel;

        FireboxCasingType(String name, int harvestLevel) {
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

        @Override
        public String getHarvestTool(BlockState state) {
            return ToolClasses.WRENCH;
        }
    }

}
