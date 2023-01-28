package net.nemezanevem.gregtech.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.nemezanevem.gregtech.api.block.IStateHarvestLevel;
import net.nemezanevem.gregtech.api.block.VariantBlock;
import net.nemezanevem.gregtech.api.item.toolitem.ToolClass;

import javax.annotation.Nonnull;

public class BlockBoilerCasing extends VariantBlock<BlockBoilerCasing.BoilerCasingType> {

    public BlockBoilerCasing() {
        super(BlockBehaviour.Properties.of(Material.METAL).strength(5.0f, 10.0f).sound(SoundType.METAL));
        registerDefaultState(getState(BoilerCasingType.BRONZE_PIPE));
    }

    public enum BoilerCasingType implements StringRepresentable, IStateHarvestLevel {

        BRONZE_PIPE("bronze_pipe", 2),
        STEEL_PIPE("steel_pipe", 3),
        TITANIUM_PIPE("titanium_pipe", 3),
        TUNGSTENSTEEL_PIPE("tungstensteel_pipe", 4),
        POLYTETRAFLUOROETHYLENE_PIPE("polytetrafluoroethylene_pipe", 1);

        private final String name;
        private final int harvestLevel;

        BoilerCasingType(String name, int harvestLevel) {
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
        public ToolClass getHarvestTool(BlockState state) {
            return ToolClass.WRENCH;
        }
    }

}

