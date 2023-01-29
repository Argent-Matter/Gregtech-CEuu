package net.nemezanevem.gregtech.api.fluids.type;

import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import net.nemezanevem.gregtech.common.mixinutil.IMixinFluidType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GTFluidTypeLiquid extends GTFluidType {

    private static final String TOOLTIP_NAME = "gregtech.fluid.state_liquid";

    public GTFluidTypeLiquid(@Nonnull String name, @Nullable String prefix, @Nullable String suffix, @Nonnull String localization) {
        super(name, prefix, suffix, localization);
    }

    @Override
    protected void setFluidProperties(@Nonnull Fluid fluid) {
        FluidType type = fluid.getFluidType();
        ((IMixinFluidType)type).setDensity(1000);
        ((IMixinFluidType)type).setViscosity(1000);
    }

    @Override
    public String getUnlocalizedTooltip() {
        return TOOLTIP_NAME;
    }
}
