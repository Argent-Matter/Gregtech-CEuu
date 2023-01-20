package net.nemezanevem.gregtech.api.fluids;

import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.nemezanevem.gregtech.GregTech;

public class GtFluidTypes {

    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.FLUID_TYPES.get(), GregTech.MODID);

    public static final RegistryObject<FluidType> LIQUID = FLUID_TYPES.register("liquid", () -> new FluidType(
            FluidType.Properties.create()
                    .pathType(BlockPathTypes.BREACH)
                    .canDrown(true)
                    .canSwim(true)
                    .density(1)
                    .canExtinguish(true)
            ));


    public static final FluidType ACID = new FluidTypeAcid("acid", null, null, "gregtech.fluid.generic");

    public static final FluidType GAS = new FluidTypeGas("gas", null, null, "gregtech.fluid.generic");

    public static final FluidType PLASMA = new FluidTypePlasma("plasma", "plasma", null, "gregtech.fluid.plasma");
}

