package net.nemezanevem.gregtech.common.block;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.nemezanevem.gregtech.api.block.IStateHarvestLevel;
import net.nemezanevem.gregtech.api.block.VariantBlock;
import net.nemezanevem.gregtech.api.item.toolitem.ToolClass;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class BlockTurbineCasing extends VariantBlock<BlockTurbineCasing.TurbineCasingType> {

    public BlockTurbineCasing() {
        super(BlockBehaviour.Properties.of(Material.METAL).sound(SoundType.METAL).strength(5.0f, 10.0f).isValidSpawn(((pState, pLevel, pPos, pValue) -> false)));
        registerDefaultState(getState(TurbineCasingType.BRONZE_GEARBOX));
    }

    public enum TurbineCasingType implements StringRepresentable, IStateHarvestLevel {

        BRONZE_GEARBOX("bronze_gearbox", 1),
        STEEL_GEARBOX("steel_gearbox", 2),
        STAINLESS_STEEL_GEARBOX("stainless_steel_gearbox", 2),
        TITANIUM_GEARBOX("titanium_gearbox", 2),
        TUNGSTENSTEEL_GEARBOX("tungstensteel_gearbox", 3),

        STEEL_TURBINE_CASING("steel_turbine_casing", 2),
        TITANIUM_TURBINE_CASING("titanium_turbine_casing", 2),
        STAINLESS_TURBINE_CASING("stainless_turbine_casing", 2),
        TUNGSTENSTEEL_TURBINE_CASING("tungstensteel_turbine_casing", 3);

        private final String name;
        private final int harvestLevel;

        TurbineCasingType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Override
        @Nonnull
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
