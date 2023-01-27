package net.nemezanevem.gregtech.api.tileentity.multiblock;

import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.RegistryObject;
import net.nemezanevem.gregtech.api.capability.IEnergyContainer;
import net.nemezanevem.gregtech.api.capability.IMaintenanceHatch;
import net.nemezanevem.gregtech.api.capability.IMufflerHatch;
import net.nemezanevem.gregtech.api.capability.IRotorHolder;

import static net.nemezanevem.gregtech.api.registry.tileentity.multiblock.MultiBlockAbilityRegistry.MULTIBLOCK_ABILITIES;

public class GtMultiblockAbilities {
    
    public static final RegistryObject<MultiblockAbility<IItemHandlerModifiable>> EXPORT_ITEMS = MULTIBLOCK_ABILITIES.register("export_items", MultiblockAbility::new);
    public static final RegistryObject<MultiblockAbility<IItemHandlerModifiable>> IMPORT_ITEMS = MULTIBLOCK_ABILITIES.register("import_items", MultiblockAbility::new);

    public static final RegistryObject<MultiblockAbility<IFluidTank>> EXPORT_FLUIDS = MULTIBLOCK_ABILITIES.register("export_fluids", MultiblockAbility::new);
    public static final RegistryObject<MultiblockAbility<IFluidTank>> IMPORT_FLUIDS = MULTIBLOCK_ABILITIES.register("import_fluids", MultiblockAbility::new);

    public static final RegistryObject<MultiblockAbility<IEnergyContainer>> INPUT_ENERGY = MULTIBLOCK_ABILITIES.register("input_energy", MultiblockAbility::new);
    public static final RegistryObject<MultiblockAbility<IEnergyContainer>> OUTPUT_ENERGY = MULTIBLOCK_ABILITIES.register("output_energy", MultiblockAbility::new);

    public static final RegistryObject<MultiblockAbility<IRotorHolder>> ROTOR_HOLDER = MULTIBLOCK_ABILITIES.register("rotor_holder", MultiblockAbility::new);

    public static final RegistryObject<MultiblockAbility<IFluidTank>> PUMP_FLUID_HATCH = MULTIBLOCK_ABILITIES.register("pump_fluid_hatch", MultiblockAbility::new);

    public static final RegistryObject<MultiblockAbility<IFluidTank>> STEAM = MULTIBLOCK_ABILITIES.register("§steam", MultiblockAbility::new);
    public static final RegistryObject<MultiblockAbility<IItemHandlerModifiable>> STEAM_IMPORT_ITEMS = MULTIBLOCK_ABILITIES.register("steam_import_items", MultiblockAbility::new);
    public static final RegistryObject<MultiblockAbility<IItemHandlerModifiable>> STEAM_EXPORT_ITEMS = MULTIBLOCK_ABILITIES.register("steam_export_items", MultiblockAbility::new);

    public static final RegistryObject<MultiblockAbility<IMaintenanceHatch>> MAINTENANCE_HATCH = MULTIBLOCK_ABILITIES.register("maintenance_hatch", MultiblockAbility::new);
    public static final RegistryObject<MultiblockAbility<IMufflerHatch>> MUFFLER_HATCH = MULTIBLOCK_ABILITIES.register("muffler_hatch", MultiblockAbility::new);

    public static final RegistryObject<MultiblockAbility<IItemHandlerModifiable>> MACHINE_HATCH = MULTIBLOCK_ABILITIES.register("machine_hatch", MultiblockAbility::new);

    public static final RegistryObject<MultiblockAbility<IFluidHandler>> TANK_VALVE = MULTIBLOCK_ABILITIES.register("tank_valve", MultiblockAbility::new);

    public static final RegistryObject<MultiblockAbility<IPassthroughHatch>> PASSTHROUGH_HATCH = MULTIBLOCK_ABILITIES.register("passthrough_hatch", MultiblockAbility::new);
}
