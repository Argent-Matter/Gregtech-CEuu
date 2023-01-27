package net.nemezanevem.gregtech.common.block.block;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

public class BlockWireCoil extends VariantActiveBlock<BlockWireCoil.CoilType> {

    public BlockWireCoil() {
        super(BlockBehaviour.Properties.of(Material.METAL).strength(5.0f, 10.0f).sound(SoundType.METAL));
        setDefaultState(getState(CoilType.CUPRONICKEL));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack itemStack, @Nullable World worldIn, List<String> lines, @Nonnull ITooltipFlag tooltipFlag) {
        super.addInformation(itemStack, worldIn, lines, tooltipFlag);

        // noinspection rawtypes, unchecked
        VariantItemBlock itemBlock = (VariantItemBlock<CoilType, BlockWireCoil>) itemStack.getItem();
        BlockState stackState = itemBlock.getBlockState(itemStack);
        CoilType coilType = getState(stackState);

        lines.add(Component.translatable("tile.wire_coil.tooltip_heat", coilType.coilTemperature));

        if (TooltipHelper.isShiftDown()) {
            int coilTier = coilType.ordinal();
            lines.add(Component.translatable("tile.wire_coil.tooltip_smelter"));
            lines.add(Component.translatable("tile.wire_coil.tooltip_parallel_smelter", coilType.level * 32));
            lines.add(Component.translatable("tile.wire_coil.tooltip_energy_smelter", Math.max(1, 16 / coilType.energyDiscount)));
            lines.add(Component.translatable("tile.wire_coil.tooltip_pyro"));
            lines.add(Component.translatable("tile.wire_coil.tooltip_speed_pyro", coilTier == 0 ? 75 : 50 * (coilTier + 1)));
            lines.add(Component.translatable("tile.wire_coil.tooltip_cracking"));
            lines.add(Component.translatable("tile.wire_coil.tooltip_energy_cracking", 100 - 10 * coilTier));
        } else {
            lines.add(Component.translatable("tile.wire_coil.tooltip_extended_info"));
        }
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nonnull SpawnPlacementType type) {
        return false;
    }

    public enum CoilType implements IStringSerializable, IHeatingCoilBlockStats {

        CUPRONICKEL("cupronickel", 1800, 1, 1, Materials.Cupronickel),
        KANTHAL("kanthal", 2700, 2, 1, Materials.Kanthal),
        NICHROME("nichrome", 3600, 2, 2, Materials.Nichrome),
        TUNGSTENSTEEL("tungstensteel", 4500, 4, 2, Materials.TungstenSteel),
        HSS_G("hss_g", 5400, 4, 4, Materials.HSSG),
        NAQUADAH("naquadah", 7200, 8, 4, Materials.Naquadah),
        TRINIUM("trinium", 9001, 8, 8, Materials.Trinium),
        TRITANIUM("tritanium", 10800, 16, 8, Materials.Tritanium);

        private final String name;
        //electric blast furnace properties
        private final int coilTemperature;
        //multi smelter properties
        private final int level;
        private final int energyDiscount;
        private final Material material;

        CoilType(String name, int coilTemperature, int level, int energyDiscount, Material material) {
            this.name = name;
            this.coilTemperature = coilTemperature;
            this.level = level;
            this.energyDiscount = energyDiscount;
            this.material = material;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public int getCoilTemperature() {
            return coilTemperature;
        }

        @Override
        public int getLevel() {
            return level;
        }

        @Override
        public int getEnergyDiscount() {
            return energyDiscount;
        }

        @Override
        public int getTier() {
            return this.ordinal();
        }

        @Nullable
        @Override
        public Material getMaterial() {
            return material;
        }

        @Nonnull
        @Override
        public String toString() {
            return getName();
        }
    }
}
