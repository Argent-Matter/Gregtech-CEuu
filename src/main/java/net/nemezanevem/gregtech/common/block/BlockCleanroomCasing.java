package net.nemezanevem.gregtech.common.block;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.nemezanevem.gregtech.api.block.IStateHarvestLevel;
import net.nemezanevem.gregtech.api.block.VariantBlock;
import net.nemezanevem.gregtech.api.item.toolitem.ToolClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class BlockCleanroomCasing extends VariantBlock<BlockCleanroomCasing.CasingType> implements IStateHarvestLevel {

    public BlockCleanroomCasing() {
        super(Properties.of(Material.METAL).sound(SoundType.METAL).strength(2.0f, 8.0f));
        registerDefaultState(getState(CasingType.PLASCRETE));
    }

    @Override
    public int getHarvestLevel(BlockState state) {
        return state == getState(CasingType.PLASCRETE) ? 2 : 1;
    }

    @Nullable
    @Override
    public ToolClass getHarvestTool(BlockState state) {
        return state == getState(CasingType.PLASCRETE) ? ToolClass.PICKAXE : ToolClass.WRENCH;
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable BlockGetter level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag advanced) {
        super.appendHoverText(stack, level, tooltip, advanced);
        if (stack.is(getItemVariant(CasingType.FILTER_CASING).getItem())) tooltip.add(Component.translatable("tile.cleanroom_casing.filter.tooltip"));
        if (stack.is(getItemVariant(CasingType.FILTER_CASING_STERILE).getItem())) tooltip.add(Component.translatable("tile.cleanroom_casing.filter_sterile.tooltip"));
    }

    public enum CasingType implements StringRepresentable {

        PLASCRETE("plascrete"),
        FILTER_CASING("filter_casing"),
        FILTER_CASING_STERILE("filter_casing_sterile");

        private final String name;

        CasingType(String name) {
            this.name = name;
        }

        @Nonnull
        @Override
        public String getSerializedName() {
            return this.name;
        }

        @Nonnull
        @Override
        public String toString() {
            return getSerializedName();
        }
    }
}


