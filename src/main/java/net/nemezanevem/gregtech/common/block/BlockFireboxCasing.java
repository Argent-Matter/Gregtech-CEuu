package net.nemezanevem.gregtech.common.block;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantActiveBlock;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.common.blocks.BlockFireboxCasing.FireboxCasingType;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

public class BlockFireboxCasing extends VariantActiveBlock<FireboxCasingType> {

    public BlockFireboxCasing() {
        super(Material.IRON);
        setTranslationKey("boiler_casing");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(FireboxCasingType.BRONZE_FIREBOX));
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull BlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull SpawnPlacementType type) {
        return false;
    }

    public enum FireboxCasingType implements IStringSerializable, IStateHarvestLevel {

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
