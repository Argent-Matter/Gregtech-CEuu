package net.nemezanevem.gregtech.api.fluids.type;

import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class GTFluidTypeAcid extends GTFluidTypeLiquid {

    private static final String TOOLTIP_NAME = "gregtech.fluid.state_liquid";

    public GTFluidTypeAcid(@Nonnull String name, @Nullable String prefix, @Nullable String suffix, @Nonnull String localization) {
        super(name, prefix, suffix, localization);
    }

    @Override
    public String getUnlocalizedTooltip() {
        return TOOLTIP_NAME;
    }

    @Override
    public List<Component> getAdditionalTooltips() {
        List<Component> tooltips = super.getAdditionalTooltips();
        tooltips.add(Component.translatable("gregtech.fluid.type_acid.tooltip"));
        return tooltips;
    }
}
