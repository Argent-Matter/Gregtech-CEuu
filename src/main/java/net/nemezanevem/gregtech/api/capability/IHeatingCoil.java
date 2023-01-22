package net.nemezanevem.gregtech.api.capability;

import net.nemezanevem.gregtech.api.capability.impl.HeatingCoilRecipeLogic;

/**
 * intended for use in conjunction with {@link HeatingCoilRecipeLogic}
 * use with temperature-based multiblocks
 */
public interface IHeatingCoil {

    /**
     *
     * @return the current temperature of the multiblock in Kelvin
     */
    int getCurrentTemperature();
}
