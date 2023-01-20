package net.nemezanevem.gregtech.api.unification.material.properties.properties;

import com.google.common.base.Preconditions;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.nemezanevem.gregtech.api.unification.material.properties.GtMaterialProperties;
import net.nemezanevem.gregtech.api.unification.material.properties.IMaterialProperty;
import net.nemezanevem.gregtech.api.unification.material.properties.MaterialProperties;
import net.nemezanevem.gregtech.common.mixinutil.IMixinFluidType;

import javax.annotation.Nonnull;

public class FluidProperty implements IMaterialProperty<FluidProperty> {

    public static final int BASE_TEMP = 293; // Room Temperature

    /**
     * Internal material fluid field
     */
    private Fluid fluid;

    private final FluidType fluidType;

    private boolean hasBlock;
    private boolean isGas;
    private int fluidTemperature = BASE_TEMP;

    public FluidProperty(@Nonnull FluidType fluidType, boolean hasBlock) {
        this.fluidType = fluidType;
        this.isGas = fluidType.isLighterThanAir();
        this.hasBlock = hasBlock;
    }

    /**
     * Default values of: no Block, not Gas.
     */
    public FluidProperty() {
        this(ForgeMod.WATER_TYPE.get(), false);
    }

    public boolean isGas() {
        return isGas;
    }

    /**
     * internal usage only
     */
    public void setFluid(@Nonnull Fluid materialFluid) {
        Preconditions.checkNotNull(materialFluid);
        this.fluid = materialFluid;
    }

    public Fluid getFluid() {
        return fluid;
    }

    public boolean hasBlock() {
        return hasBlock;
    }

    public void setHasBlock(boolean hasBlock) {
        this.hasBlock = hasBlock;
    }

    public void setIsGas(boolean isGas) {
        this.isGas = isGas;
    }

    @Nonnull
    public FluidStack getFluid(int amount) {
        return new FluidStack(fluid, amount);
    }

    public void setFluidTemperature(int fluidTemperature) {
        setFluidTemperature(fluidTemperature, true);
    }

    public void setFluidTemperature(int fluidTemperature, boolean isKelvin) {
        if (isKelvin) Preconditions.checkArgument(fluidTemperature >= 0, "Invalid temperature");
        else fluidTemperature += 273;
        this.fluidTemperature = fluidTemperature;
        if (fluid != null)
            ((IMixinFluidType) fluidType).setTemperature(fluidTemperature);
    }

    public int getFluidTemperature() {
        return fluidTemperature;
    }

    @Nonnull
    public FluidType getFluidType() {
        return this.fluidType;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (properties.hasProperty(GtMaterialProperties.PLASMA.get())) {
            hasBlock = false;
        }
    }
}