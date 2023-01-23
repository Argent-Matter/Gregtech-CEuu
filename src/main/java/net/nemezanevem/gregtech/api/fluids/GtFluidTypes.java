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
                    .pathType(BlockPathTypes.WATER)
                    .adjacentPathType(BlockPathTypes.WATER_BORDER)
                    .canDrown(true)
                    .canSwim(true)
                    .density(1)
                    .canExtinguish(true)
            ));


    public static final RegistryObject<FluidType> ACID = FLUID_TYPES.register("acid", () -> new FluidType(
            FluidType.Properties.create()
                    .pathType(BlockPathTypes.DANGER_OTHER)
                    .canDrown(true)
                    .canSwim(true)
                    .density(1)
    ));

    public static final RegistryObject<FluidType> GAS = FLUID_TYPES.register("gas", () -> new FluidType(
            FluidType.Properties.create()
                    .pathType(BlockPathTypes.OPEN)
                    .canDrown(true)
                    .canSwim(false)
                    .density(-1)
                    .canPushEntity(false)
    ));

    public static final RegistryObject<FluidType> PLASMA = FLUID_TYPES.register("plasma", () -> new FluidType(
            FluidType.Properties.create()
                    .pathType(BlockPathTypes.OPEN)
                    .canDrown(true)
                    .canSwim(false)
                    .density(-1)
                    .canPushEntity(false)
    ));

}

