package net.nemezanevem.gregtech.api.fluids.type;

public class GTFluidTypes {

    public static final GTFluidType LIQUID = new GTFluidTypeLiquid("liquid", null, null, "gregtech.fluid.generic");

    public static final GTFluidType ACID = new GTFluidTypeAcid("acid", null, null, "gregtech.fluid.generic");

    public static final GTFluidType GAS = new GTFluidTypeGas("gas", null, null, "gregtech.fluid.generic");

    public static final GTFluidType PLASMA = new GTFluidTypeLiquid("plasma", "plasma", null, "gregtech.fluid.plasma");
}
