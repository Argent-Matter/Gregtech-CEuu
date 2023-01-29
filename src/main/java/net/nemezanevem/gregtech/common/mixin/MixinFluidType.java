package net.nemezanevem.gregtech.common.mixin;

import net.minecraftforge.fluids.FluidType;
import net.nemezanevem.gregtech.common.mixinutil.IMixinFluidType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FluidType.class)
public class MixinFluidType implements IMixinFluidType {
    private boolean isAcid;

    @Mutable
    @Final
    @Shadow
    private int temperature;

    @Mutable
    @Final
    @Shadow
    private int density;

    @Mutable
    @Final
    @Shadow
    private int viscosity;

    @Mutable
    @Final
    @Shadow
    private int lightLevel;

    @Override
    public boolean isAcid() {
        return isAcid;
    }

    @Override
    public void setAcid(boolean isAcid) {
        this.isAcid = isAcid;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public void setDensity(int density) {
        this.density = density;
    }

    public void setViscosity(int viscosity) {
        this.viscosity = viscosity;
    }

    public void setLightLevel(int lightLevel) {
        this.lightLevel = lightLevel;
    }
}
