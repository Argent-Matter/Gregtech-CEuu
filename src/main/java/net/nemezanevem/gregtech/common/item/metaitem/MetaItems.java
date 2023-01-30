package net.nemezanevem.gregtech.common.item.metaitem;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.item.armor.ArmorMetaItem;
import net.nemezanevem.gregtech.api.item.materialitem.PrefixItem;
import net.nemezanevem.gregtech.api.item.metaitem.MetaItem;
import net.nemezanevem.gregtech.api.item.metaitem.MetaTagItem;
import net.nemezanevem.gregtech.api.unification.material.MarkerMaterial;
import net.nemezanevem.gregtech.api.unification.material.TagUnifier;
import net.nemezanevem.gregtech.api.unification.tag.TagPrefix;

import java.util.*;

public final class MetaItems {

    public static final DeferredRegister<Item> ITEM_REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, GregTech.MODID);

    private MetaItems() {
    }

    public static final List<MetaItem> META_ITEMS = MetaItem.getMetaItems();

    public static MetaItem CREDIT_COPPER;
    public static MetaItem CREDIT_CUPRONICKEL;
    public static MetaItem CREDIT_SILVER;
    public static MetaItem CREDIT_GOLD;
    public static MetaItem CREDIT_PLATINUM;
    public static MetaItem CREDIT_OSMIUM;
    public static MetaItem CREDIT_NAQUADAH;
    public static MetaItem CREDIT_NEUTRONIUM;

    public static MetaItem COIN_GOLD_ANCIENT;
    public static MetaItem COIN_DOGE;
    public static MetaItem COIN_CHOCOLATE;

    public static MetaItem COMPRESSED_CLAY;
    public static MetaItem COMPRESSED_COKE_CLAY;
    public static MetaItem COMPRESSED_FIRECLAY;
    public static MetaItem FIRECLAY_BRICK;
    public static MetaItem COKE_OVEN_BRICK;

    public static MetaItem WOODEN_FORM_EMPTY;
    public static MetaItem WOODEN_FORM_BRICK;

    public static MetaItem SHAPE_EMPTY;

    public static final MetaItem[] SHAPE_MOLDS = new MetaItem[13];
    public static MetaItem SHAPE_MOLD_PLATE;
    public static MetaItem SHAPE_MOLD_GEAR;
    public static MetaItem SHAPE_MOLD_CREDIT;
    public static MetaItem SHAPE_MOLD_BOTTLE;
    public static MetaItem SHAPE_MOLD_INGOT;
    public static MetaItem SHAPE_MOLD_BALL;
    public static MetaItem SHAPE_MOLD_BLOCK;
    public static MetaItem SHAPE_MOLD_NUGGET;
    public static MetaItem SHAPE_MOLD_CYLINDER;
    public static MetaItem SHAPE_MOLD_ANVIL;
    public static MetaItem SHAPE_MOLD_NAME;
    public static MetaItem SHAPE_MOLD_GEAR_SMALL;
    public static MetaItem SHAPE_MOLD_ROTOR;

    public static final MetaItem[] SHAPE_EXTRUDERS = new MetaItem[27];
    public static MetaItem SHAPE_EXTRUDER_PLATE;
    public static MetaItem SHAPE_EXTRUDER_ROD;
    public static MetaItem SHAPE_EXTRUDER_BOLT;
    public static MetaItem SHAPE_EXTRUDER_RING;
    public static MetaItem SHAPE_EXTRUDER_CELL;
    public static MetaItem SHAPE_EXTRUDER_INGOT;
    public static MetaItem SHAPE_EXTRUDER_WIRE;
    public static MetaItem SHAPE_EXTRUDER_PIPE_TINY;
    public static MetaItem SHAPE_EXTRUDER_PIPE_SMALL;
    public static MetaItem SHAPE_EXTRUDER_PIPE_NORMAL;
    public static MetaItem SHAPE_EXTRUDER_PIPE_LARGE;
    public static MetaItem SHAPE_EXTRUDER_PIPE_HUGE;
    public static MetaItem SHAPE_EXTRUDER_BLOCK;
    public static MetaItem SHAPE_EXTRUDER_GEAR;
    public static MetaItem SHAPE_EXTRUDER_BOTTLE;
    public static MetaItem SHAPE_EXTRUDER_FOIL;
    public static MetaItem SHAPE_EXTRUDER_GEAR_SMALL;
    public static MetaItem SHAPE_EXTRUDER_ROD_LONG;
    public static MetaItem SHAPE_EXTRUDER_ROTOR;

    public static MetaItem SPRAY_SOLVENT;
    public static MetaItem SPRAY_EMPTY;

    public static MetaItem FLUID_CELL;
    public static MetaItem FLUID_CELL_UNIVERSAL;
    public static MetaItem FLUID_CELL_LARGE_STEEL;
    public static MetaItem FLUID_CELL_LARGE_ALUMINIUM;
    public static MetaItem FLUID_CELL_LARGE_STAINLESS_STEEL;
    public static MetaItem FLUID_CELL_LARGE_TITANIUM;
    public static MetaItem FLUID_CELL_LARGE_TUNGSTEN_STEEL;
    public static MetaItem FLUID_CELL_GLASS_VIAL;

    public static MetaItem TOOL_MATCHES;
    public static MetaItem TOOL_MATCHBOX;
    public static MetaItem TOOL_LIGHTER_INVAR;
    public static MetaItem TOOL_LIGHTER_PLATINUM;

    public static MetaItem CARBON_FIBERS;
    public static MetaItem CARBON_MESH;
    public static MetaItem CARBON_FIBER_PLATE;
    public static MetaItem DUCT_TAPE;

    public static MetaItem NEUTRON_REFLECTOR;

    public static MetaItem BATTERY_HULL_LV;
    public static MetaItem BATTERY_HULL_MV;
    public static MetaItem BATTERY_HULL_HV;
    public static MetaItem BATTERY_HULL_SMALL_VANADIUM;
    public static MetaItem BATTERY_HULL_MEDIUM_VANADIUM;
    public static MetaItem BATTERY_HULL_LARGE_VANADIUM;
    public static MetaItem BATTERY_HULL_MEDIUM_NAQUADRIA;
    public static MetaItem BATTERY_HULL_LARGE_NAQUADRIA;

    public static MetaItem BATTERY_ULV_TANTALUM;
    public static MetaItem BATTERY_LV_CADMIUM;
    public static MetaItem BATTERY_LV_LITHIUM;
    public static MetaItem BATTERY_LV_SODIUM;
    public static MetaItem BATTERY_MV_CADMIUM;
    public static MetaItem BATTERY_MV_LITHIUM;
    public static MetaItem BATTERY_MV_SODIUM;
    public static MetaItem BATTERY_HV_CADMIUM;
    public static MetaItem BATTERY_HV_LITHIUM;
    public static MetaItem BATTERY_HV_SODIUM;
    public static MetaItem ENERGIUM_CRYSTAL;
    public static MetaItem LAPOTRON_CRYSTAL;

    public static MetaItem BATTERY_EV_VANADIUM;
    public static MetaItem BATTERY_IV_VANADIUM;
    public static MetaItem BATTERY_LUV_VANADIUM;
    public static MetaItem BATTERY_ZPM_NAQUADRIA;
    public static MetaItem BATTERY_UV_NAQUADRIA;

    public static MetaItem ENERGY_LAPOTRONIC_ORB;
    public static MetaItem ENERGY_LAPOTRONIC_ORB_CLUSTER;
    public static MetaItem ZERO_POINT_MODULE;
    public static MetaItem ULTIMATE_BATTERY;

    public static MetaItem ELECTRIC_MOTOR_LV;
    public static MetaItem ELECTRIC_MOTOR_MV;
    public static MetaItem ELECTRIC_MOTOR_HV;
    public static MetaItem ELECTRIC_MOTOR_EV;
    public static MetaItem ELECTRIC_MOTOR_IV;
    public static MetaItem ELECTRIC_MOTOR_LuV;
    public static MetaItem ELECTRIC_MOTOR_ZPM;
    public static MetaItem ELECTRIC_MOTOR_UV;
    public static MetaItem ELECTRIC_MOTOR_UHV;
    public static MetaItem ELECTRIC_MOTOR_UEV;
    public static MetaItem ELECTRIC_MOTOR_UIV;
    public static MetaItem ELECTRIC_MOTOR_UXV;
    public static MetaItem ELECTRIC_MOTOR_OpV;

    public static MetaItem ELECTRIC_PUMP_LV;
    public static MetaItem ELECTRIC_PUMP_MV;
    public static MetaItem ELECTRIC_PUMP_HV;
    public static MetaItem ELECTRIC_PUMP_EV;
    public static MetaItem ELECTRIC_PUMP_IV;
    public static MetaItem ELECTRIC_PUMP_LuV;
    public static MetaItem ELECTRIC_PUMP_ZPM;
    public static MetaItem ELECTRIC_PUMP_UV;
    public static MetaItem ELECTRIC_PUMP_UHV;
    public static MetaItem ELECTRIC_PUMP_UEV;
    public static MetaItem ELECTRIC_PUMP_UIV;
    public static MetaItem ELECTRIC_PUMP_UXV;
    public static MetaItem ELECTRIC_PUMP_OpV;

    public static MetaItem FLUID_REGULATOR_LV;
    public static MetaItem FLUID_REGULATOR_MV;
    public static MetaItem FLUID_REGULATOR_HV;
    public static MetaItem FLUID_REGULATOR_EV;
    public static MetaItem FLUID_REGULATOR_IV;
    public static MetaItem FLUID_REGULATOR_LUV;
    public static MetaItem FLUID_REGULATOR_ZPM;
    public static MetaItem FLUID_REGULATOR_UV;

    public static MetaItem FLUID_FILTER;

    public static MetaItem DYNAMITE;

    public static MetaItem CONVEYOR_MODULE_LV;
    public static MetaItem CONVEYOR_MODULE_MV;
    public static MetaItem CONVEYOR_MODULE_HV;
    public static MetaItem CONVEYOR_MODULE_EV;
    public static MetaItem CONVEYOR_MODULE_IV;
    public static MetaItem CONVEYOR_MODULE_LuV;
    public static MetaItem CONVEYOR_MODULE_ZPM;
    public static MetaItem CONVEYOR_MODULE_UV;
    public static MetaItem CONVEYOR_MODULE_UHV;
    public static MetaItem CONVEYOR_MODULE_UEV;
    public static MetaItem CONVEYOR_MODULE_UIV;
    public static MetaItem CONVEYOR_MODULE_UXV;
    public static MetaItem CONVEYOR_MODULE_OpV;

    public static MetaItem ELECTRIC_PISTON_LV;
    public static MetaItem ELECTRIC_PISTON_MV;
    public static MetaItem ELECTRIC_PISTON_HV;
    public static MetaItem ELECTRIC_PISTON_EV;
    public static MetaItem ELECTRIC_PISTON_IV;
    public static MetaItem ELECTRIC_PISTON_LUV;
    public static MetaItem ELECTRIC_PISTON_ZPM;
    public static MetaItem ELECTRIC_PISTON_UV;
    public static MetaItem ELECTRIC_PISTON_UHV;
    public static MetaItem ELECTRIC_PISTON_UEV;
    public static MetaItem ELECTRIC_PISTON_UIV;
    public static MetaItem ELECTRIC_PISTON_UXV;
    public static MetaItem ELECTRIC_PISTON_OpV;

    public static MetaItem ROBOT_ARM_LV;
    public static MetaItem ROBOT_ARM_MV;
    public static MetaItem ROBOT_ARM_HV;
    public static MetaItem ROBOT_ARM_EV;
    public static MetaItem ROBOT_ARM_IV;
    public static MetaItem ROBOT_ARM_LuV;
    public static MetaItem ROBOT_ARM_ZPM;
    public static MetaItem ROBOT_ARM_UV;
    public static MetaItem ROBOT_ARM_UHV;
    public static MetaItem ROBOT_ARM_UEV;
    public static MetaItem ROBOT_ARM_UIV;
    public static MetaItem ROBOT_ARM_UXV;
    public static MetaItem ROBOT_ARM_OpV;

    public static MetaItem FIELD_GENERATOR_LV;
    public static MetaItem FIELD_GENERATOR_MV;
    public static MetaItem FIELD_GENERATOR_HV;
    public static MetaItem FIELD_GENERATOR_EV;
    public static MetaItem FIELD_GENERATOR_IV;
    public static MetaItem FIELD_GENERATOR_LuV;
    public static MetaItem FIELD_GENERATOR_ZPM;
    public static MetaItem FIELD_GENERATOR_UV;
    public static MetaItem FIELD_GENERATOR_UHV;
    public static MetaItem FIELD_GENERATOR_UEV;
    public static MetaItem FIELD_GENERATOR_UIV;
    public static MetaItem FIELD_GENERATOR_UXV;
    public static MetaItem FIELD_GENERATOR_OpV;

    public static MetaItem EMITTER_LV;
    public static MetaItem EMITTER_MV;
    public static MetaItem EMITTER_HV;
    public static MetaItem EMITTER_EV;
    public static MetaItem EMITTER_IV;
    public static MetaItem EMITTER_LuV;
    public static MetaItem EMITTER_ZPM;
    public static MetaItem EMITTER_UV;
    public static MetaItem EMITTER_UHV;
    public static MetaItem EMITTER_UEV;
    public static MetaItem EMITTER_UIV;
    public static MetaItem EMITTER_UXV;
    public static MetaItem EMITTER_OpV;

    public static MetaItem SENSOR_LV;
    public static MetaItem SENSOR_MV;
    public static MetaItem SENSOR_HV;
    public static MetaItem SENSOR_EV;
    public static MetaItem SENSOR_IV;
    public static MetaItem SENSOR_LuV;
    public static MetaItem SENSOR_ZPM;
    public static MetaItem SENSOR_UV;
    public static MetaItem SENSOR_UHV;
    public static MetaItem SENSOR_UEV;
    public static MetaItem SENSOR_UIV;
    public static MetaItem SENSOR_UXV;
    public static MetaItem SENSOR_OpV;

    public static MetaItem TOOL_DATA_STICK;
    public static MetaItem TOOL_DATA_ORB;

    public static final Map<MarkerMaterial, MetaItem> GLASS_LENSES = new HashMap<>();

    public static MetaItem SILICON_BOULE;
    public static MetaItem GLOWSTONE_BOULE;
    public static MetaItem NAQUADAH_BOULE;
    public static MetaItem NEUTRONIUM_BOULE;
    public static MetaItem SILICON_WAFER;
    public static MetaItem GLOWSTONE_WAFER;
    public static MetaItem NAQUADAH_WAFER;
    public static MetaItem NEUTRONIUM_WAFER;

    public static MetaItem HIGHLY_ADVANCED_SOC_WAFER;
    public static MetaItem ADVANCED_SYSTEM_ON_CHIP_WAFER;
    public static MetaItem INTEGRATED_LOGIC_CIRCUIT_WAFER;
    public static MetaItem CENTRAL_PROCESSING_UNIT_WAFER;
    public static MetaItem ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER;
    public static MetaItem LOW_POWER_INTEGRATED_CIRCUIT_WAFER;
    public static MetaItem POWER_INTEGRATED_CIRCUIT_WAFER;
    public static MetaItem HIGH_POWER_INTEGRATED_CIRCUIT_WAFER;
    public static MetaItem ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT_WAFER;
    public static MetaItem NAND_MEMORY_CHIP_WAFER;
    public static MetaItem NANO_CENTRAL_PROCESSING_UNIT_WAFER;
    public static MetaItem NOR_MEMORY_CHIP_WAFER;
    public static MetaItem QUBIT_CENTRAL_PROCESSING_UNIT_WAFER;
    public static MetaItem RANDOM_ACCESS_MEMORY_WAFER;
    public static MetaItem SYSTEM_ON_CHIP_WAFER;
    public static MetaItem SIMPLE_SYSTEM_ON_CHIP_WAFER;

    public static MetaItem ENGRAVED_CRYSTAL_CHIP;
    public static MetaItem ENGRAVED_LAPOTRON_CHIP;

    public static MetaItem HIGHLY_ADVANCED_SOC;
    public static MetaItem ADVANCED_SYSTEM_ON_CHIP;
    public static MetaItem INTEGRATED_LOGIC_CIRCUIT;
    public static MetaItem CENTRAL_PROCESSING_UNIT;
    public static MetaItem ULTRA_LOW_POWER_INTEGRATED_CIRCUIT;
    public static MetaItem LOW_POWER_INTEGRATED_CIRCUIT;
    public static MetaItem POWER_INTEGRATED_CIRCUIT;
    public static MetaItem HIGH_POWER_INTEGRATED_CIRCUIT;
    public static MetaItem ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT;
    public static MetaItem NAND_MEMORY_CHIP;
    public static MetaItem NANO_CENTRAL_PROCESSING_UNIT;
    public static MetaItem NOR_MEMORY_CHIP;
    public static MetaItem QUBIT_CENTRAL_PROCESSING_UNIT;
    public static MetaItem RANDOM_ACCESS_MEMORY;
    public static MetaItem SYSTEM_ON_CHIP;
    public static MetaItem SIMPLE_SYSTEM_ON_CHIP;

    public static MetaItem RAW_CRYSTAL_CHIP;
    public static MetaItem RAW_CRYSTAL_CHIP_PART;
    public static MetaItem CRYSTAL_CENTRAL_PROCESSING_UNIT;
    public static MetaItem CRYSTAL_SYSTEM_ON_CHIP;

    public static MetaItem COATED_BOARD;
    public static MetaItem PHENOLIC_BOARD;
    public static MetaItem PLASTIC_BOARD;
    public static MetaItem EPOXY_BOARD;
    public static MetaItem FIBER_BOARD;
    public static MetaItem MULTILAYER_FIBER_BOARD;
    public static MetaItem WETWARE_BOARD;

    public static MetaItem BASIC_CIRCUIT_BOARD;
    public static MetaItem GOOD_CIRCUIT_BOARD;
    public static MetaItem PLASTIC_CIRCUIT_BOARD;
    public static MetaItem ADVANCED_CIRCUIT_BOARD;
    public static MetaItem EXTREME_CIRCUIT_BOARD;
    public static MetaItem ELITE_CIRCUIT_BOARD;
    public static MetaItem WETWARE_CIRCUIT_BOARD;

    public static MetaItem VACUUM_TUBE;
    public static MetaItem GLASS_TUBE;
    public static MetaItem RESISTOR;
    public static MetaItem DIODE;
    public static MetaItem CAPACITOR;
    public static MetaItem TRANSISTOR;
    public static MetaItem INDUCTOR;
    public static MetaItem SMD_CAPACITOR;
    public static MetaItem SMD_DIODE;
    public static MetaItem SMD_RESISTOR;
    public static MetaItem SMD_TRANSISTOR;
    public static MetaItem SMD_INDUCTOR;
    public static MetaItem ADVANCED_SMD_CAPACITOR;
    public static MetaItem ADVANCED_SMD_DIODE;
    public static MetaItem ADVANCED_SMD_RESISTOR;
    public static MetaItem ADVANCED_SMD_TRANSISTOR;
    public static MetaItem ADVANCED_SMD_INDUCTOR;

    // T1: Electronic
    public static MetaItem ELECTRONIC_CIRCUIT_LV;
    public static MetaItem ELECTRONIC_CIRCUIT_MV;

    // T2: Integrated
    public static MetaItem INTEGRATED_CIRCUIT_LV;
    public static MetaItem INTEGRATED_CIRCUIT_MV;
    public static MetaItem INTEGRATED_CIRCUIT_HV;

    // ULV/LV easier circuits
    public static MetaItem NAND_CHIP_ULV;
    public static MetaItem MICROPROCESSOR_LV;

    // T3: Processor
    public static MetaItem PROCESSOR_MV;
    public static MetaItem PROCESSOR_ASSEMBLY_HV;
    public static MetaItem WORKSTATION_EV;
    public static MetaItem MAINFRAME_IV;

    // T4: Nano
    public static MetaItem NANO_PROCESSOR_HV;
    public static MetaItem NANO_PROCESSOR_ASSEMBLY_EV;
    public static MetaItem NANO_COMPUTER_IV;
    public static MetaItem NANO_MAINFRAME_LUV;

    // T5: Quantum
    public static MetaItem QUANTUM_PROCESSOR_EV;
    public static MetaItem QUANTUM_ASSEMBLY_IV;
    public static MetaItem QUANTUM_COMPUTER_LUV;
    public static MetaItem QUANTUM_MAINFRAME_ZPM;

    // T6: Crystal
    public static MetaItem CRYSTAL_PROCESSOR_IV;
    public static MetaItem CRYSTAL_ASSEMBLY_LUV;
    public static MetaItem CRYSTAL_COMPUTER_ZPM;
    public static MetaItem CRYSTAL_MAINFRAME_UV;

    // T7: Wetware
    public static MetaItem WETWARE_PROCESSOR_LUV;
    public static MetaItem WETWARE_PROCESSOR_ASSEMBLY_ZPM;
    public static MetaItem WETWARE_SUPER_COMPUTER_UV;
    public static MetaItem WETWARE_MAINFRAME_UHV;

    public static MetaItem COMPONENT_GRINDER_DIAMOND;
    public static MetaItem COMPONENT_GRINDER_TUNGSTEN;

    public static MetaItem QUANTUM_EYE;
    public static MetaItem QUANTUM_STAR;
    public static MetaItem GRAVI_STAR;

    public static MetaItem ITEM_FILTER;
    public static MetaItem ORE_DICTIONARY_FILTER;
    public static MetaItem SMART_FILTER;

    public static MetaItem COVER_SHUTTER;
    public static MetaItem COVER_MACHINE_CONTROLLER;
    public static MetaItem COVER_FACADE;

    public static MetaItem COVER_ACTIVITY_DETECTOR;
    public static MetaItem COVER_ACTIVITY_DETECTOR_ADVANCED;
    public static MetaItem COVER_FLUID_DETECTOR;
    public static MetaItem COVER_ITEM_DETECTOR;
    public static MetaItem COVER_ENERGY_DETECTOR;
    public static MetaItem COVER_ENERGY_DETECTOR_ADVANCED;

    public static MetaItem COVER_SCREEN;
    public static MetaItem COVER_CRAFTING;
    public static MetaItem COVER_INFINITE_WATER;
    public static MetaItem COVER_ENDER_FLUID_LINK;
    public static MetaItem COVER_DIGITAL_INTERFACE;
    public static MetaItem COVER_DIGITAL_INTERFACE_WIRELESS;
    public static MetaItem COVER_FLUID_VOIDING;
    public static MetaItem COVER_FLUID_VOIDING_ADVANCED;
    public static MetaItem COVER_ITEM_VOIDING;
    public static MetaItem COVER_ITEM_VOIDING_ADVANCED;

    public static MetaItem COVER_SOLAR_PANEL;
    public static MetaItem COVER_SOLAR_PANEL_ULV;
    public static MetaItem COVER_SOLAR_PANEL_LV;
    public static MetaItem COVER_SOLAR_PANEL_MV;
    public static MetaItem COVER_SOLAR_PANEL_HV;
    public static MetaItem COVER_SOLAR_PANEL_EV;
    public static MetaItem COVER_SOLAR_PANEL_IV;
    public static MetaItem COVER_SOLAR_PANEL_LUV;
    public static MetaItem COVER_SOLAR_PANEL_ZPM;
    public static MetaItem COVER_SOLAR_PANEL_UV;


    public static MetaItem PLUGIN_TEXT;
    public static MetaItem PLUGIN_ONLINE_PIC;
    public static MetaItem PLUGIN_FAKE_GUI;
    public static MetaItem PLUGIN_ADVANCED_MONITOR;

    public static MetaItem INTEGRATED_CIRCUIT;

    public static MetaItem FOAM_SPRAYER;

    public static MetaItem GELLED_TOLUENE;

    public static MetaItem BOTTLE_PURPLE_DRINK;

    public static MetaItem PLANT_BALL;
    public static MetaItem STICKY_RESIN;
    public static MetaItem ENERGIUM_DUST;

    public static MetaItem POWER_UNIT_LV;
    public static MetaItem POWER_UNIT_MV;
    public static MetaItem POWER_UNIT_HV;
    public static MetaItem POWER_UNIT_EV;
    public static MetaItem POWER_UNIT_IV;

    public static MetaItem NANO_SABER;
    public static MetaItem PROSPECTOR_LV;
    public static MetaItem PROSPECTOR_HV;
    public static MetaItem PROSPECTOR_LUV;

    public static MetaItem TRICORDER_SCANNER;
    public static MetaItem DEBUG_SCANNER;

    public static MetaItem ITEM_MAGNET_LV;
    public static MetaItem ITEM_MAGNET_HV;

    public static MetaItem WIRELESS;
    public static MetaItem CAMERA;
    public static MetaItem TERMINAL;

    public static final MetaItem[] DYE_ONLY_ITEMS = new MetaItem[DyeColor.values().length];
    public static final MetaItem[] SPRAY_CAN_DYES = new MetaItem[DyeColor.values().length];

    public static MetaItem TURBINE_ROTOR;

    public static MetaItem ENERGY_MODULE;
    public static MetaItem ENERGY_CLUSTER;
    public static MetaItem NEURO_PROCESSOR;
    public static MetaItem STEM_CELLS;
    public static MetaItem PETRI_DISH;

    public static MetaItem BIO_CHAFF;

    public static MetaItem VOLTAGE_COIL_ULV;
    public static MetaItem VOLTAGE_COIL_LV;
    public static MetaItem VOLTAGE_COIL_MV;
    public static MetaItem VOLTAGE_COIL_HV;
    public static MetaItem VOLTAGE_COIL_EV;
    public static MetaItem VOLTAGE_COIL_IV;
    public static MetaItem VOLTAGE_COIL_LuV;
    public static MetaItem VOLTAGE_COIL_ZPM;
    public static MetaItem VOLTAGE_COIL_UV;

    public static MetaItem CLIPBOARD;

    public static ArmorMetaItem NIGHTVISION_GOGGLES;

    public static ArmorMetaItem NANO_CHESTPLATE;
    public static ArmorMetaItem NANO_LEGGINGS;
    public static ArmorMetaItem NANO_BOOTS;
    public static ArmorMetaItem NANO_HELMET;

    public static ArmorMetaItem QUANTUM_CHESTPLATE;
    public static ArmorMetaItem QUANTUM_LEGGINGS;
    public static ArmorMetaItem QUANTUM_BOOTS;
    public static ArmorMetaItem QUANTUM_HELMET;

    public static ArmorMetaItem SEMIFLUID_JETPACK;
    public static ArmorMetaItem ELECTRIC_JETPACK;

    public static ArmorMetaItem ELECTRIC_JETPACK_ADVANCED;
    public static ArmorMetaItem NANO_CHESTPLATE_ADVANCED;
    public static ArmorMetaItem QUANTUM_CHESTPLATE_ADVANCED;

    public static MetaItem POWER_THRUSTER;
    public static MetaItem POWER_THRUSTER_ADVANCED;
    public static MetaItem GRAVITATION_ENGINE;

    public static MetaItem SUS_RECORD;
    public static MetaItem NAN_CERTIFICATE;

    public static MetaItem FERTILIZER;
    public static MetaItem BLACKLIGHT;


    public static MetaTagItem CT_OREDICT_ITEM;

    private static final List<TagPrefix> TagPrefixes = new ArrayList<TagPrefix>() {{
        add(TagPrefix.dust);
        add(TagPrefix.dustSmall);
        add(TagPrefix.dustTiny);
        add(TagPrefix.dustImpure);
        add(TagPrefix.dustPure);
        add(TagPrefix.crushed);
        add(TagPrefix.crushedPurified);
        add(TagPrefix.crushedCentrifuged);
        add(TagPrefix.gem);
        add(TagPrefix.gemChipped);
        add(TagPrefix.gemFlawed);
        add(TagPrefix.gemFlawless);
        add(TagPrefix.gemExquisite);
        add(TagPrefix.ingot);
        add(TagPrefix.ingotHot);
        add(TagPrefix.plate);
        add(TagPrefix.plateDouble);
        add(TagPrefix.plateDense);
        add(TagPrefix.foil);
        add(TagPrefix.rod);
        add(TagPrefix.rodLong);
        add(TagPrefix.bolt);
        add(TagPrefix.screw);
        add(TagPrefix.ring);
        add(TagPrefix.nugget);
        add(TagPrefix.round);
        add(TagPrefix.spring);
        add(TagPrefix.springSmall);
        add(TagPrefix.gear);
        add(TagPrefix.gearSmall);
        add(TagPrefix.wireFine);
        add(TagPrefix.rotor);
        add(TagPrefix.lens);
        add(TagPrefix.turbineBlade);
        add(TagPrefix.toolHeadDrill);
        add(TagPrefix.toolHeadChainsaw);
        add(TagPrefix.toolHeadWrench);
        add(TagPrefix.toolHeadBuzzSaw);
        add(TagPrefix.toolHeadScrewdriver);
    }};

    public static void init() {
        PrefixItem.registerItems(ITEM_REGISTRY);
    }

    public static void registerOreDict() {
        for (MetaItem item : META_ITEMS) {
            if (item instanceof PrefixItem) {
                ((PrefixItem) item).registerTag();
            }
        }
        for (Map.Entry<MarkerMaterial, MetaItem> entry : GLASS_LENSES.entrySet()) {
            // Register "craftingLensWhite" for example
            TagUnifier.registerTag(entry.getValue(), TagPrefix.craftingLens, entry.getKey());
            // Register "craftingLensGlass", intended only for recipes to dye lenses and not in the Engraver
            TagUnifier.registerTag(entry.getValue(), String.format("%s/%s", TagPrefix.craftingLens.name(), "glass"));
        }
    }


    public static void registerModels() {
        MinecraftForge.EVENT_BUS.register(MetaItems.class);
        MetaItem.registerModels();
        for (MetaItem item : META_ITEMS) {
            item.registerTextureMesh();
        }
    }

    public static void registerColors() {
        for (MetaItem item : META_ITEMS) {
            item.registerColor();
        }
    }

    @SubscribeEvent
    public static void registerBakedModels(ModelEvent.BakingCompleted event) {
        GregTech.LOGGER.info("Registering special item models");
        registerSpecialItemModel(event, COVER_FACADE, new FacadeRenderer());
    }

    private static void registerSpecialItemModel(ModelEvent.BakingCompleted event, MetaItem metaValueItem, BakedModel bakedModel) {
        //god these casts when intellij says you're fine but compiler complains about shit boundaries
        //noinspection RedundantCast
        ResourceLocation modelPath = MetaItem.createItemModelPath(metaValueItem, "");
        event.getModels().put(modelPath, bakedModel);
    }

    @SuppressWarnings("unused")
    public static void addTagPrefix(TagPrefix... prefixes) {
        TagPrefixes.addAll(Arrays.asList(prefixes));
    }
}
