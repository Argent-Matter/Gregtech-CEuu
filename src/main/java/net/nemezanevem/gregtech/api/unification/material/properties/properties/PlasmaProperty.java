package net.nemezanevem.gregtech.api.unification.material.properties.properties;

import com.google.common.base.Preconditions;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.nemezanevem.gregtech.api.unification.material.properties.IMaterialProperty;
import net.nemezanevem.gregtech.api.unification.material.properties.MaterialProperties;

import javax.annotation.Nonnull;

public class PlasmaProperty implements IMaterialProperty<PlasmaProperty> {

    /**
     * Internal material plasma fluid field
     */
    private Fluid plasma;

    @Override
    public void verifyProperty(MaterialProperties properties) {
    }

    /**
     * internal usage only
     */
    public void setPlasma(@Nonnull Fluid plasma) {
        Preconditions.checkNotNull(plasma);
        this.plasma = plasma;
    }

    public Fluid getPlasma() {
        return plasma;
    }

    @Nonnull
    public FluidStack getPlasma(int amount) {
        return new FluidStack(plasma, amount);
    }
}