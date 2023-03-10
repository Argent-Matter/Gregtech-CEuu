package net.nemezanevem.gregtech.api.capability;

import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

/**
 * Interface for FluidHandlerItemStacks which handle GT's unique fluid mechanics
 * @see FluidType
 * @see FluidTypes
 * @see MaterialFluid
 */
public interface IThermalFluidHandlerItemStack {

    /**
     *
     * @param stack the {@link FluidStack} to check
     * @return whether the FluidStack can be used to fill this fluid container
     */
    default boolean canFillFluidType(FluidStack stack) {
        if (stack == null || stack.getFluid() == null) return false;

        Fluid fluid = stack.getFluid();
        if (fluid.getFluidType().getTemperature() > getMaxFluidTemperature()) return false;
        // fluids less than 120K are cryogenic
        if (fluid.getFluidType().getTemperature() < 120 && !isCryoProof()) return false;
        if (fluid.getFluidType().getDensity() <= 0 && !isGasProof()) return false;

        if (fluid instanceof MaterialFluid) {
            FluidType fluidType = ((MaterialFluid) fluid).getFluidType();
            if (fluidType == GTFluidTypes.ACID && !isAcidProof()) return false;
            if (fluidType == GTFluidTypes.PLASMA && !isPlasmaProof()) return false;
        }
        return true;
    }

    /**
     * This is always checked, regardless of the contained fluid being a {@link MaterialFluid} or not
     *
     * @return the maximum allowed temperature for a fluid to be stored in this container
     */
    int getMaxFluidTemperature();

    /**
     * This is always checked, regardless of the contained fluid being a {@link MaterialFluid} or not
     *
     * @return true if this fluid container allows gases, otherwise false
     */
    boolean isGasProof();

    /**
     * @see FluidTypes
     *
     * @return true if this fluid container allows acids, otherwise false
     */
    boolean isAcidProof();

    /**
     * @see FluidTypes
     *
     * @return true if this fluid container allows cryogenics, otherwise false
     */
    boolean isCryoProof();

    /**
     * @see FluidTypes
     *
     * @return true if this fluid container allows plasmas, otherwise false
     */
    boolean isPlasmaProof();
}
