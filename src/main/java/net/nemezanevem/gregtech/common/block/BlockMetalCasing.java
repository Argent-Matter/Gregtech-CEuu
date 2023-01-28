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

public class BlockMetalCasing extends VariantBlock<BlockMetalCasing.MetalCasingType> {

    public BlockMetalCasing() {
        super(BlockBehaviour.Properties.of(Material.METAL).strength(5.0f, 10.0f).sound(SoundType.METAL).isValidSpawn((pState, pLevel, pPos, pValue) -> false));
        registerDefaultState(getState(MetalCasingType.BRONZE_BRICKS));
    }

    public enum MetalCasingType implements StringRepresentable, IStateHarvestLevel {

        BRONZE_BRICKS("bronze_bricks", 1),
        PRIMITIVE_BRICKS("primitive_bricks", 1),
        INVAR_HEATPROOF("invar_heatproof", 1),
        ALUMINIUM_FROSTPROOF("aluminium_frostproof", 1),
        STEEL_SOLID("steel_solid", 2),
        STAINLESS_CLEAN("stainless_clean", 2),
        TITANIUM_STABLE("titanium_stable", 2),
        TUNGSTENSTEEL_ROBUST("tungstensteel_robust", 3),
        COKE_BRICKS("coke_bricks", 1),
        PTFE_INERT_CASING("ptfe_inert", 0),
        HSSE_STURDY("hsse_sturdy", 3);

        private final String name;
        private final int harvestLevel;

        MetalCasingType(String name, int harvestLevel) {
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
