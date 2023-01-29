package net.nemezanevem.gregtech.common.block;

import gregtech.api.items.toolitem.ToolClasses;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.nemezanevem.gregtech.api.block.VariantActiveBlock;

import javax.annotation.Nonnull;

public class BlockMultiblockCasing extends VariantActiveBlock<BlockMultiblockCasing.MultiblockCasingType> {

    public BlockMultiblockCasing() {
        super(BlockBehaviour.Properties.of(Material.METAL).strength(5.0f, 10.0f).sound(SoundType.METAL).isValidSpawn(((pState, pLevel, pPos, pValue) -> false)));
        setHarvestLevel(ToolClasses.WRENCH, 2);
        registerDefaultState(getState(MultiblockCasingType.ENGINE_INTAKE_CASING));
    }

    public enum MultiblockCasingType implements StringRepresentable {

        ENGINE_INTAKE_CASING("engine_intake"),
        EXTREME_ENGINE_INTAKE_CASING("extreme_engine_intake"),
        GRATE_CASING("grate"),
        ASSEMBLY_CONTROL("assembly_control"),
        ASSEMBLY_LINE_CASING("assembly_line");

        private final String name;

        MultiblockCasingType(String name) {
            this.name = name;
        }

        @Nonnull
        @Override
        public String getSerializedName() {
            return this.name;
        }

    }

}
