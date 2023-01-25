package net.nemezanevem.gregtech.common.block;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import gregtech.api.items.toolitem.ToolClasses;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

public class BlockBoilerCasing extends VariantBlock<BlockBoilerCasing.BoilerCasingType> {

    public BlockBoilerCasing() {
        super(Material.IRON);
        setTranslationKey("boiler_casing");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(BoilerCasingType.BRONZE_PIPE));
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull BlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull SpawnPlacementType type) {
        return false;
    }

    public enum BoilerCasingType implements IStringSerializable, IStateHarvestLevel {

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
        public String getName() {
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
