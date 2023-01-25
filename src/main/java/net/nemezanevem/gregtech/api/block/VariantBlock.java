package net.nemezanevem.gregtech.api.block;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class VariantBlock<T extends Enum<T> & StringRepresentable> extends Block {

    protected EnumProperty<T> VARIANT;
    protected T[] VALUES;

    public VariantBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any().setValue(VARIANT, VALUES[0]));
    }

    public BlockState getState(T variant) {
        return defaultBlockState().setValue(VARIANT, variant);
    }

    public T getState(BlockState blockState) {
        return blockState.getValue(VARIANT);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable BlockGetter player, List<Component> tooltip, @Nonnull TooltipFlag advanced) {
        //tier less tooltip like: tile.turbine_casing.tooltip
        Component unlocalizedVariantTooltip = Component.translatable(getDescriptionId() + ".tooltip");
        if (ComponentUtils.isTranslationResolvable(unlocalizedVariantTooltip))
            tooltip.add(unlocalizedVariantTooltip);
        //item specific tooltip: tile.turbine_casing.bronze_gearbox.tooltip
        Component unlocalizedTooltip = Component.translatable(stack.getDescriptionId() + ".tooltip");
        if (ComponentUtils.isTranslationResolvable(unlocalizedTooltip)) tooltip.add(unlocalizedTooltip);
    }
}
