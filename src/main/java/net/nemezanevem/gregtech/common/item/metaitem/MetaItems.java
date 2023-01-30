package net.nemezanevem.gregtech.common.item.metaitem;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.item.armor.ArmorMetaItem;
import net.nemezanevem.gregtech.api.item.materialitem.PrefixItem;
import net.nemezanevem.gregtech.api.item.metaitem.MetaItem;
import net.nemezanevem.gregtech.api.item.metaitem.MetaTagItem;
import net.nemezanevem.gregtech.api.unification.material.MarkerMaterial;
import net.nemezanevem.gregtech.api.unification.material.MarkerMaterials;
import net.nemezanevem.gregtech.api.unification.material.TagUnifier;
import net.nemezanevem.gregtech.api.unification.tag.TagPrefix;
import net.nemezanevem.gregtech.api.util.SizedArrayList;
import net.nemezanevem.gregtech.common.item.behavior.ClipboardBehavior;

import java.util.*;

@SuppressWarnings("unused")
public final class MetaItems {

    public static final DeferredRegister<Item> ITEM_REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, GregTech.MODID);

    private MetaItems() {
    }

    public static final List<MetaItem> META_ITEMS = MetaItem.getMetaItems();

    public static final RegistryObject<MetaItem> CREDIT_COPPER = ITEM_REGISTRY.register("credit_copper", () -> MetaItem.builder("credit.copper").build());
    public static final RegistryObject<MetaItem> CREDIT_CUPRONICKEL = ITEM_REGISTRY.register("credit_cupronickel", () -> MetaItem.builder("credit.cupronickel").build());
    public static final RegistryObject<MetaItem> CREDIT_SILVER = ITEM_REGISTRY.register("credit_silver", () -> MetaItem.builder("credit.silver").build());
    public static final RegistryObject<MetaItem> CREDIT_GOLD = ITEM_REGISTRY.register("credit_gold", () -> MetaItem.builder("credit.gold").build());
    public static final RegistryObject<MetaItem> CREDIT_PLATINUM = ITEM_REGISTRY.register("credit_platinum", () -> MetaItem.builder("credit.platinum").build());
    public static final RegistryObject<MetaItem> CREDIT_OSMIUM = ITEM_REGISTRY.register("credit_osmium", () -> MetaItem.builder("credit.osmium").build());
    public static final RegistryObject<MetaItem> CREDIT_NAQUADAH = ITEM_REGISTRY.register("credit_naquadah", () -> MetaItem.builder("credit.naquadah").build());
    public static final RegistryObject<MetaItem> CREDIT_NEUTRONIUM = ITEM_REGISTRY.register("credit_neutronium", () -> MetaItem.builder("credit.neutronium").build());

    public static final RegistryObject<MetaItem> COIN_GOLD_ANCIENT = ITEM_REGISTRY.register("coin_gold_ancient", () -> MetaItem.builder("coin_gold.ancient").build());
    public static final RegistryObject<MetaItem> COIN_DOGE = ITEM_REGISTRY.register("coin_doge", () -> MetaItem.builder("coin.doge").build());
    public static final RegistryObject<MetaItem> COIN_CHOCOLATE = ITEM_REGISTRY.register("coin_chocolate", () -> MetaItem.builder("coin.chocolate").build());

    public static final RegistryObject<MetaItem> COMPRESSED_CLAY = ITEM_REGISTRY.register("compressed_clay", () -> MetaItem.builder("compressed.clay").build());
    public static final RegistryObject<MetaItem> COMPRESSED_COKE_CLAY = ITEM_REGISTRY.register("compressed_coke_clay", () -> MetaItem.builder("compressed_coke.clay").build());
    public static final RegistryObject<MetaItem> COMPRESSED_FIRECLAY = ITEM_REGISTRY.register("compressed_fireclay", () -> MetaItem.builder("compressed.fireclay").build());
    public static final RegistryObject<MetaItem> FIRECLAY_BRICK = ITEM_REGISTRY.register("fireclay_brick", () -> MetaItem.builder("fireclay.brick").build());
    public static final RegistryObject<MetaItem> COKE_OVEN_BRICK = ITEM_REGISTRY.register("coke_oven_brick", () -> MetaItem.builder("coke_oven.brick").build());

    public static final RegistryObject<MetaItem> WOODEN_FORM_EMPTY = ITEM_REGISTRY.register("wooden_form_empty", () -> MetaItem.builder("wooden_form.empty").build());
    public static final RegistryObject<MetaItem> WOODEN_FORM_BRICK = ITEM_REGISTRY.register("wooden_form_brick", () -> MetaItem.builder("wooden_form.brick").build());

    public static final RegistryObject<MetaItem> SHAPE_EMPTY = ITEM_REGISTRY.register("shape_empty", () -> MetaItem.builder("shape.empty").build());

    public static final List<RegistryObject<MetaItem>>  SHAPE_MOLDS = new SizedArrayList<>(13);
    public static final RegistryObject<MetaItem> SHAPE_MOLD_PLATE = ITEM_REGISTRY.register("shape_mold_plate", () -> MetaItem.builder("shape_mold.plate").build());
    public static final RegistryObject<MetaItem> SHAPE_MOLD_GEAR = ITEM_REGISTRY.register("shape_mold_gear", () -> MetaItem.builder("shape_mold.gear").build());
    public static final RegistryObject<MetaItem> SHAPE_MOLD_CREDIT = ITEM_REGISTRY.register("shape_mold_credit", () -> MetaItem.builder("shape_mold.credit").build());
    public static final RegistryObject<MetaItem> SHAPE_MOLD_BOTTLE = ITEM_REGISTRY.register("shape_mold_bottle", () -> MetaItem.builder("shape_mold.bottle").build());
    public static final RegistryObject<MetaItem> SHAPE_MOLD_INGOT = ITEM_REGISTRY.register("shape_mold_ingot", () -> MetaItem.builder("shape_mold.ingot").build());
    public static final RegistryObject<MetaItem> SHAPE_MOLD_BALL = ITEM_REGISTRY.register("shape_mold_ball", () -> MetaItem.builder("shape_mold.ball").build());
    public static final RegistryObject<MetaItem> SHAPE_MOLD_BLOCK = ITEM_REGISTRY.register("shape_mold_block", () -> MetaItem.builder("shape_mold.block").build());
    public static final RegistryObject<MetaItem> SHAPE_MOLD_NUGGET = ITEM_REGISTRY.register("shape_mold_nugget", () -> MetaItem.builder("shape_mold.nugget").build());
    public static final RegistryObject<MetaItem> SHAPE_MOLD_CYLINDER = ITEM_REGISTRY.register("shape_mold_cylinder", () -> MetaItem.builder("shape_mold.cylinder").build());
    public static final RegistryObject<MetaItem> SHAPE_MOLD_ANVIL = ITEM_REGISTRY.register("shape_mold_anvil", () -> MetaItem.builder("shape_mold.anvil").build());
    public static final RegistryObject<MetaItem> SHAPE_MOLD_NAME = ITEM_REGISTRY.register("shape_mold_name", () -> MetaItem.builder("shape_mold.name").build());
    public static final RegistryObject<MetaItem> SHAPE_MOLD_GEAR_SMALL = ITEM_REGISTRY.register("shape_mold_gear_small", () -> MetaItem.builder("shape_mold_gear.small").build());
    public static final RegistryObject<MetaItem> SHAPE_MOLD_ROTOR = ITEM_REGISTRY.register("shape_mold_rotor", () -> MetaItem.builder("shape_mold.rotor").build());

    public static final List<RegistryObject<MetaItem>>  SHAPE_EXTRUDERS = new SizedArrayList<>(27);
    public static final RegistryObject<MetaItem> SHAPE_EXTRUDER_PLATE = ITEM_REGISTRY.register("shape_extruder_plate", () -> MetaItem.builder("shape_extruder.plate").build());
    public static final RegistryObject<MetaItem> SHAPE_EXTRUDER_ROD = ITEM_REGISTRY.register("shape_extruder_rod", () -> MetaItem.builder("shape_extruder.rod").build());
    public static final RegistryObject<MetaItem> SHAPE_EXTRUDER_BOLT = ITEM_REGISTRY.register("shape_extruder_bolt", () -> MetaItem.builder("shape_extruder.bolt").build());
    public static final RegistryObject<MetaItem> SHAPE_EXTRUDER_RING = ITEM_REGISTRY.register("shape_extruder_ring", () -> MetaItem.builder("shape_extruder.ring").build());
    public static final RegistryObject<MetaItem> SHAPE_EXTRUDER_CELL = ITEM_REGISTRY.register("shape_extruder_cell", () -> MetaItem.builder("shape_extruder.cell").build());
    public static final RegistryObject<MetaItem> SHAPE_EXTRUDER_INGOT = ITEM_REGISTRY.register("shape_extruder_ingot", () -> MetaItem.builder("shape_extruder.ingot").build());
    public static final RegistryObject<MetaItem> SHAPE_EXTRUDER_WIRE = ITEM_REGISTRY.register("shape_extruder_wire", () -> MetaItem.builder("shape_extruder.wire").build());
    public static final RegistryObject<MetaItem> SHAPE_EXTRUDER_PIPE_TINY = ITEM_REGISTRY.register("shape_extruder_pipe_tiny", () -> MetaItem.builder("shape_extruder_pipe.tiny").build());
    public static final RegistryObject<MetaItem> SHAPE_EXTRUDER_PIPE_SMALL = ITEM_REGISTRY.register("shape_extruder_pipe_small", () -> MetaItem.builder("shape_extruder_pipe.small").build());
    public static final RegistryObject<MetaItem> SHAPE_EXTRUDER_PIPE_NORMAL = ITEM_REGISTRY.register("shape_extruder_pipe_normal", () -> MetaItem.builder("shape_extruder_pipe.normal").build());
    public static final RegistryObject<MetaItem> SHAPE_EXTRUDER_PIPE_LARGE = ITEM_REGISTRY.register("shape_extruder_pipe_large", () -> MetaItem.builder("shape_extruder_pipe.large").build());
    public static final RegistryObject<MetaItem> SHAPE_EXTRUDER_PIPE_HUGE = ITEM_REGISTRY.register("shape_extruder_pipe_huge", () -> MetaItem.builder("shape_extruder_pipe.huge").build());
    public static final RegistryObject<MetaItem> SHAPE_EXTRUDER_BLOCK = ITEM_REGISTRY.register("shape_extruder_block", () -> MetaItem.builder("shape_extruder.block").build());
    public static final RegistryObject<MetaItem> SHAPE_EXTRUDER_GEAR = ITEM_REGISTRY.register("shape_extruder_gear", () -> MetaItem.builder("shape_extruder.gear").build());
    public static final RegistryObject<MetaItem> SHAPE_EXTRUDER_BOTTLE = ITEM_REGISTRY.register("shape_extruder_bottle", () -> MetaItem.builder("shape_extruder.bottle").build());
    public static final RegistryObject<MetaItem> SHAPE_EXTRUDER_FOIL = ITEM_REGISTRY.register("shape_extruder_foil", () -> MetaItem.builder("shape_extruder.foil").build());
    public static final RegistryObject<MetaItem> SHAPE_EXTRUDER_GEAR_SMALL = ITEM_REGISTRY.register("shape_extruder_gear_small", () -> MetaItem.builder("shape_extruder_gear.small").build());
    public static final RegistryObject<MetaItem> SHAPE_EXTRUDER_ROD_LONG = ITEM_REGISTRY.register("shape_extruder_rod_long", () -> MetaItem.builder("shape_extruder_rod.long").build());
    public static final RegistryObject<MetaItem> SHAPE_EXTRUDER_ROTOR = ITEM_REGISTRY.register("shape_extruder_rotor", () -> MetaItem.builder("shape_extruder.rotor").build());

    public static final RegistryObject<MetaItem> SPRAY_SOLVENT = ITEM_REGISTRY.register("spray_solvent", () -> MetaItem.builder("spray.solvent").build());
    public static final RegistryObject<MetaItem> SPRAY_EMPTY = ITEM_REGISTRY.register("spray_empty", () -> MetaItem.builder("spray.empty").build());

    public static final RegistryObject<MetaItem> FLUID_CELL = ITEM_REGISTRY.register("fluid_cell", () -> MetaItem.builder("fluid.cell").build());
    public static final RegistryObject<MetaItem> FLUID_CELL_UNIVERSAL = ITEM_REGISTRY.register("fluid_cell_universal", () -> MetaItem.builder("fluid_cell.universal").build());
    public static final RegistryObject<MetaItem> FLUID_CELL_LARGE_STEEL = ITEM_REGISTRY.register("fluid_cell_large_steel", () -> MetaItem.builder("fluid_cell_large.steel").build());
    public static final RegistryObject<MetaItem> FLUID_CELL_LARGE_ALUMINIUM = ITEM_REGISTRY.register("fluid_cell_large_aluminium", () -> MetaItem.builder("fluid_cell_large.aluminium").build());
    public static final RegistryObject<MetaItem> FLUID_CELL_LARGE_STAINLESS_STEEL = ITEM_REGISTRY.register("fluid_cell_large_stainless_steel", () -> MetaItem.builder("fluid_cell_large_stainless.steel").build());
    public static final RegistryObject<MetaItem> FLUID_CELL_LARGE_TITANIUM = ITEM_REGISTRY.register("fluid_cell_large_titanium", () -> MetaItem.builder("fluid_cell_large.titanium").build());
    public static final RegistryObject<MetaItem> FLUID_CELL_LARGE_TUNGSTEN_STEEL = ITEM_REGISTRY.register("fluid_cell_large_tungsten_steel", () -> MetaItem.builder("fluid_cell_large_tungsten.steel").build());
    public static final RegistryObject<MetaItem> FLUID_CELL_GLASS_VIAL = ITEM_REGISTRY.register("fluid_cell_glass_vial", () -> MetaItem.builder("fluid_cell_glass.vial").build());

    public static final RegistryObject<MetaItem> TOOL_MATCHES = ITEM_REGISTRY.register("tool_matches", () -> MetaItem.builder("tool.matches").build());
    public static final RegistryObject<MetaItem> TOOL_MATCHBOX = ITEM_REGISTRY.register("tool_matchbox", () -> MetaItem.builder("tool.matchbox").build());
    public static final RegistryObject<MetaItem> TOOL_LIGHTER_INVAR = ITEM_REGISTRY.register("tool_lighter_invar", () -> MetaItem.builder("tool_lighter.invar").build());
    public static final RegistryObject<MetaItem> TOOL_LIGHTER_PLATINUM = ITEM_REGISTRY.register("tool_lighter_platinum", () -> MetaItem.builder("tool_lighter.platinum").build());

    public static final RegistryObject<MetaItem> CARBON_FIBERS = ITEM_REGISTRY.register("carbon_fibers", () -> MetaItem.builder("carbon.fibers").build());
    public static final RegistryObject<MetaItem> CARBON_MESH = ITEM_REGISTRY.register("carbon_mesh", () -> MetaItem.builder("carbon.mesh").build());
    public static final RegistryObject<MetaItem> CARBON_FIBER_PLATE = ITEM_REGISTRY.register("carbon_fiber_plate", () -> MetaItem.builder("carbon_fiber.plate").build());
    public static final RegistryObject<MetaItem> DUCT_TAPE = ITEM_REGISTRY.register("duct_tape", () -> MetaItem.builder("duct.tape").build());

    public static final RegistryObject<MetaItem> NEUTRON_REFLECTOR = ITEM_REGISTRY.register("neutron_reflector", () -> MetaItem.builder("neutron.reflector").build());

    public static final RegistryObject<MetaItem> BATTERY_HULL_LV = ITEM_REGISTRY.register("battery_hull_lv", () -> MetaItem.builder("battery_hull.lv").build());
    public static final RegistryObject<MetaItem> BATTERY_HULL_MV = ITEM_REGISTRY.register("battery_hull_mv", () -> MetaItem.builder("battery_hull.mv").build());
    public static final RegistryObject<MetaItem> BATTERY_HULL_HV = ITEM_REGISTRY.register("battery_hull_hv", () -> MetaItem.builder("battery_hull.hv").build());
    public static final RegistryObject<MetaItem> BATTERY_HULL_SMALL_VANADIUM = ITEM_REGISTRY.register("battery_hull_small_vanadium", () -> MetaItem.builder("battery_hull_small.vanadium").build());
    public static final RegistryObject<MetaItem> BATTERY_HULL_MEDIUM_VANADIUM = ITEM_REGISTRY.register("battery_hull_medium_vanadium", () -> MetaItem.builder("battery_hull_medium.vanadium").build());
    public static final RegistryObject<MetaItem> BATTERY_HULL_LARGE_VANADIUM = ITEM_REGISTRY.register("battery_hull_large_vanadium", () -> MetaItem.builder("battery_hull_large.vanadium").build());
    public static final RegistryObject<MetaItem> BATTERY_HULL_MEDIUM_NAQUADRIA = ITEM_REGISTRY.register("battery_hull_medium_naquadria", () -> MetaItem.builder("battery_hull_medium.naquadria").build());
    public static final RegistryObject<MetaItem> BATTERY_HULL_LARGE_NAQUADRIA = ITEM_REGISTRY.register("battery_hull_large_naquadria", () -> MetaItem.builder("battery_hull_large.naquadria").build());

    public static final RegistryObject<MetaItem> BATTERY_ULV_TANTALUM = ITEM_REGISTRY.register("battery_ulv_tantalum", () -> MetaItem.builder("battery_ulv.tantalum").build());
    public static final RegistryObject<MetaItem> BATTERY_LV_CADMIUM = ITEM_REGISTRY.register("battery_lv_cadmium", () -> MetaItem.builder("battery_lv.cadmium").build());
    public static final RegistryObject<MetaItem> BATTERY_LV_LITHIUM = ITEM_REGISTRY.register("battery_lv_lithium", () -> MetaItem.builder("battery_lv.lithium").build());
    public static final RegistryObject<MetaItem> BATTERY_LV_SODIUM = ITEM_REGISTRY.register("battery_lv_sodium", () -> MetaItem.builder("battery_lv.sodium").build());
    public static final RegistryObject<MetaItem> BATTERY_MV_CADMIUM = ITEM_REGISTRY.register("battery_mv_cadmium", () -> MetaItem.builder("battery_mv.cadmium").build());
    public static final RegistryObject<MetaItem> BATTERY_MV_LITHIUM = ITEM_REGISTRY.register("battery_mv_lithium", () -> MetaItem.builder("battery_mv.lithium").build());
    public static final RegistryObject<MetaItem> BATTERY_MV_SODIUM = ITEM_REGISTRY.register("battery_mv_sodium", () -> MetaItem.builder("battery_mv.sodium").build());
    public static final RegistryObject<MetaItem> BATTERY_HV_CADMIUM = ITEM_REGISTRY.register("battery_hv_cadmium", () -> MetaItem.builder("battery_hv.cadmium").build());
    public static final RegistryObject<MetaItem> BATTERY_HV_LITHIUM = ITEM_REGISTRY.register("battery_hv_lithium", () -> MetaItem.builder("battery_hv.lithium").build());
    public static final RegistryObject<MetaItem> BATTERY_HV_SODIUM = ITEM_REGISTRY.register("battery_hv_sodium", () -> MetaItem.builder("battery_hv.sodium").build());
    public static final RegistryObject<MetaItem> ENERGIUM_CRYSTAL = ITEM_REGISTRY.register("energium_crystal", () -> MetaItem.builder("energium.crystal").build());
    public static final RegistryObject<MetaItem> LAPOTRON_CRYSTAL = ITEM_REGISTRY.register("lapotron_crystal", () -> MetaItem.builder("lapotron.crystal").build());

    public static final RegistryObject<MetaItem> BATTERY_EV_VANADIUM = ITEM_REGISTRY.register("battery_ev_vanadium", () -> MetaItem.builder("battery_ev.vanadium").build());
    public static final RegistryObject<MetaItem> BATTERY_IV_VANADIUM = ITEM_REGISTRY.register("battery_iv_vanadium", () -> MetaItem.builder("battery_iv.vanadium").build());
    public static final RegistryObject<MetaItem> BATTERY_LUV_VANADIUM = ITEM_REGISTRY.register("battery_luv_vanadium", () -> MetaItem.builder("battery_luv.vanadium").build());
    public static final RegistryObject<MetaItem> BATTERY_ZPM_NAQUADRIA = ITEM_REGISTRY.register("battery_zpm_naquadria", () -> MetaItem.builder("battery_zpm.naquadria").build());
    public static final RegistryObject<MetaItem> BATTERY_UV_NAQUADRIA = ITEM_REGISTRY.register("battery_uv_naquadria", () -> MetaItem.builder("battery_uv.naquadria").build());

    public static final RegistryObject<MetaItem> ENERGY_LAPOTRONIC_ORB = ITEM_REGISTRY.register("energy_lapotronic_orb", () -> MetaItem.builder("energy_lapotronic.orb").build());
    public static final RegistryObject<MetaItem> ENERGY_LAPOTRONIC_ORB_CLUSTER = ITEM_REGISTRY.register("energy_lapotronic_orb_cluster", () -> MetaItem.builder("energy_lapotronic_orb.cluster").build());
    public static final RegistryObject<MetaItem> ZERO_POINT_MODULE = ITEM_REGISTRY.register("zero_point_module", () -> MetaItem.builder("zero_point.module").build());
    public static final RegistryObject<MetaItem> ULTIMATE_BATTERY = ITEM_REGISTRY.register("ultimate_battery", () -> MetaItem.builder("ultimate.battery").build());

    public static final RegistryObject<MetaItem> ELECTRIC_MOTOR_LV = ITEM_REGISTRY.register("electric_motor_lv", () -> MetaItem.builder("electric_motor.lv").build());
    public static final RegistryObject<MetaItem> ELECTRIC_MOTOR_MV = ITEM_REGISTRY.register("electric_motor_mv", () -> MetaItem.builder("electric_motor.mv").build());
    public static final RegistryObject<MetaItem> ELECTRIC_MOTOR_HV = ITEM_REGISTRY.register("electric_motor_hv", () -> MetaItem.builder("electric_motor.hv").build());
    public static final RegistryObject<MetaItem> ELECTRIC_MOTOR_EV = ITEM_REGISTRY.register("electric_motor_ev", () -> MetaItem.builder("electric_motor.ev").build());
    public static final RegistryObject<MetaItem> ELECTRIC_MOTOR_IV = ITEM_REGISTRY.register("electric_motor_iv", () -> MetaItem.builder("electric_motor.iv").build());
    public static final RegistryObject<MetaItem> ELECTRIC_MOTOR_LuV = ITEM_REGISTRY.register("electric_motor_luv", () -> MetaItem.builder("electric_motor.luv").build());
    public static final RegistryObject<MetaItem> ELECTRIC_MOTOR_ZPM = ITEM_REGISTRY.register("electric_motor_zpm", () -> MetaItem.builder("electric_motor.zpm").build());
    public static final RegistryObject<MetaItem> ELECTRIC_MOTOR_UV = ITEM_REGISTRY.register("electric_motor_uv", () -> MetaItem.builder("electric_motor.uv").build());
    public static final RegistryObject<MetaItem> ELECTRIC_MOTOR_UHV = ITEM_REGISTRY.register("electric_motor_uhv", () -> MetaItem.builder("electric_motor.uhv").build());
    public static final RegistryObject<MetaItem> ELECTRIC_MOTOR_UEV = ITEM_REGISTRY.register("electric_motor_uev", () -> MetaItem.builder("electric_motor.uev").build());
    public static final RegistryObject<MetaItem> ELECTRIC_MOTOR_UIV = ITEM_REGISTRY.register("electric_motor_uiv", () -> MetaItem.builder("electric_motor.uiv").build());
    public static final RegistryObject<MetaItem> ELECTRIC_MOTOR_UXV = ITEM_REGISTRY.register("electric_motor_uxv", () -> MetaItem.builder("electric_motor.uxv").build());
    public static final RegistryObject<MetaItem> ELECTRIC_MOTOR_OpV = ITEM_REGISTRY.register("electric_motor_opv", () -> MetaItem.builder("electric_motor.opv").build());

    public static final RegistryObject<MetaItem> ELECTRIC_PUMP_LV = ITEM_REGISTRY.register("electric_pump_lv", () -> MetaItem.builder("electric_pump.lv").build());
    public static final RegistryObject<MetaItem> ELECTRIC_PUMP_MV = ITEM_REGISTRY.register("electric_pump_mv", () -> MetaItem.builder("electric_pump.mv").build());
    public static final RegistryObject<MetaItem> ELECTRIC_PUMP_HV = ITEM_REGISTRY.register("electric_pump_hv", () -> MetaItem.builder("electric_pump.hv").build());
    public static final RegistryObject<MetaItem> ELECTRIC_PUMP_EV = ITEM_REGISTRY.register("electric_pump_ev", () -> MetaItem.builder("electric_pump.ev").build());
    public static final RegistryObject<MetaItem> ELECTRIC_PUMP_IV = ITEM_REGISTRY.register("electric_pump_iv", () -> MetaItem.builder("electric_pump.iv").build());
    public static final RegistryObject<MetaItem> ELECTRIC_PUMP_LuV = ITEM_REGISTRY.register("electric_pump_luv", () -> MetaItem.builder("electric_pump.luv").build());
    public static final RegistryObject<MetaItem> ELECTRIC_PUMP_ZPM = ITEM_REGISTRY.register("electric_pump_zpm", () -> MetaItem.builder("electric_pump.zpm").build());
    public static final RegistryObject<MetaItem> ELECTRIC_PUMP_UV = ITEM_REGISTRY.register("electric_pump_uv", () -> MetaItem.builder("electric_pump.uv").build());
    public static final RegistryObject<MetaItem> ELECTRIC_PUMP_UHV = ITEM_REGISTRY.register("electric_pump_uhv", () -> MetaItem.builder("electric_pump.uhv").build());
    public static final RegistryObject<MetaItem> ELECTRIC_PUMP_UEV = ITEM_REGISTRY.register("electric_pump_uev", () -> MetaItem.builder("electric_pump.uev").build());
    public static final RegistryObject<MetaItem> ELECTRIC_PUMP_UIV = ITEM_REGISTRY.register("electric_pump_uiv", () -> MetaItem.builder("electric_pump.uiv").build());
    public static final RegistryObject<MetaItem> ELECTRIC_PUMP_UXV = ITEM_REGISTRY.register("electric_pump_uxv", () -> MetaItem.builder("electric_pump.uxv").build());
    public static final RegistryObject<MetaItem> ELECTRIC_PUMP_OpV = ITEM_REGISTRY.register("electric_pump_opv", () -> MetaItem.builder("electric_pump.opv").build());

    public static final RegistryObject<MetaItem> FLUID_REGULATOR_LV = ITEM_REGISTRY.register("fluid_regulator_lv", () -> MetaItem.builder("fluid_regulator.lv").build());
    public static final RegistryObject<MetaItem> FLUID_REGULATOR_MV = ITEM_REGISTRY.register("fluid_regulator_mv", () -> MetaItem.builder("fluid_regulator.mv").build());
    public static final RegistryObject<MetaItem> FLUID_REGULATOR_HV = ITEM_REGISTRY.register("fluid_regulator_hv", () -> MetaItem.builder("fluid_regulator.hv").build());
    public static final RegistryObject<MetaItem> FLUID_REGULATOR_EV = ITEM_REGISTRY.register("fluid_regulator_ev", () -> MetaItem.builder("fluid_regulator.ev").build());
    public static final RegistryObject<MetaItem> FLUID_REGULATOR_IV = ITEM_REGISTRY.register("fluid_regulator_iv", () -> MetaItem.builder("fluid_regulator.iv").build());
    public static final RegistryObject<MetaItem> FLUID_REGULATOR_LUV = ITEM_REGISTRY.register("fluid_regulator_luv", () -> MetaItem.builder("fluid_regulator.luv").build());
    public static final RegistryObject<MetaItem> FLUID_REGULATOR_ZPM = ITEM_REGISTRY.register("fluid_regulator_zpm", () -> MetaItem.builder("fluid_regulator.zpm").build());
    public static final RegistryObject<MetaItem> FLUID_REGULATOR_UV = ITEM_REGISTRY.register("fluid_regulator_uv", () -> MetaItem.builder("fluid_regulator.uv").build());

    public static final RegistryObject<MetaItem> FLUID_FILTER = ITEM_REGISTRY.register("fluid_filter", () -> MetaItem.builder("fluid.filter").build());

    public static final RegistryObject<MetaItem> DYNAMITE = ITEM_REGISTRY.register("dynamite", () -> MetaItem.builder("dynamite").addComponents(new DynamiteBehaviour()).build());

    public static final RegistryObject<MetaItem> CONVEYOR_MODULE_LV = ITEM_REGISTRY.register("conveyor_module_lv", () -> MetaItem.builder("conveyor_module.lv").build());
    public static final RegistryObject<MetaItem> CONVEYOR_MODULE_MV = ITEM_REGISTRY.register("conveyor_module_mv", () -> MetaItem.builder("conveyor_module.mv").build());
    public static final RegistryObject<MetaItem> CONVEYOR_MODULE_HV = ITEM_REGISTRY.register("conveyor_module_hv", () -> MetaItem.builder("conveyor_module.hv").build());
    public static final RegistryObject<MetaItem> CONVEYOR_MODULE_EV = ITEM_REGISTRY.register("conveyor_module_ev", () -> MetaItem.builder("conveyor_module.ev").build());
    public static final RegistryObject<MetaItem> CONVEYOR_MODULE_IV = ITEM_REGISTRY.register("conveyor_module_iv", () -> MetaItem.builder("conveyor_module.iv").build());
    public static final RegistryObject<MetaItem> CONVEYOR_MODULE_LuV = ITEM_REGISTRY.register("conveyor_module_luv", () -> MetaItem.builder("conveyor_module.luv").build());
    public static final RegistryObject<MetaItem> CONVEYOR_MODULE_ZPM = ITEM_REGISTRY.register("conveyor_module_zpm", () -> MetaItem.builder("conveyor_module.zpm").build());
    public static final RegistryObject<MetaItem> CONVEYOR_MODULE_UV = ITEM_REGISTRY.register("conveyor_module_uv", () -> MetaItem.builder("conveyor_module.uv").build());
    public static final RegistryObject<MetaItem> CONVEYOR_MODULE_UHV = ITEM_REGISTRY.register("conveyor_module_uhv", () -> MetaItem.builder("conveyor_module.uhv").build());
    public static final RegistryObject<MetaItem> CONVEYOR_MODULE_UEV = ITEM_REGISTRY.register("conveyor_module_uev", () -> MetaItem.builder("conveyor_module.uev").build());
    public static final RegistryObject<MetaItem> CONVEYOR_MODULE_UIV = ITEM_REGISTRY.register("conveyor_module_uiv", () -> MetaItem.builder("conveyor_module.uiv").build());
    public static final RegistryObject<MetaItem> CONVEYOR_MODULE_UXV = ITEM_REGISTRY.register("conveyor_module_uxv", () -> MetaItem.builder("conveyor_module.uxv").build());
    public static final RegistryObject<MetaItem> CONVEYOR_MODULE_OpV = ITEM_REGISTRY.register("conveyor_module_opv", () -> MetaItem.builder("conveyor_module.opv").build());

    public static final RegistryObject<MetaItem> ELECTRIC_PISTON_LV = ITEM_REGISTRY.register("electric_piston_lv", () -> MetaItem.builder("electric_piston.lv").build());
    public static final RegistryObject<MetaItem> ELECTRIC_PISTON_MV = ITEM_REGISTRY.register("electric_piston_mv", () -> MetaItem.builder("electric_piston.mv").build());
    public static final RegistryObject<MetaItem> ELECTRIC_PISTON_HV = ITEM_REGISTRY.register("electric_piston_hv", () -> MetaItem.builder("electric_piston.hv").build());
    public static final RegistryObject<MetaItem> ELECTRIC_PISTON_EV = ITEM_REGISTRY.register("electric_piston_ev", () -> MetaItem.builder("electric_piston.ev").build());
    public static final RegistryObject<MetaItem> ELECTRIC_PISTON_IV = ITEM_REGISTRY.register("electric_piston_iv", () -> MetaItem.builder("electric_piston.iv").build());
    public static final RegistryObject<MetaItem> ELECTRIC_PISTON_LUV = ITEM_REGISTRY.register("electric_piston_luv", () -> MetaItem.builder("electric_piston.luv").build());
    public static final RegistryObject<MetaItem> ELECTRIC_PISTON_ZPM = ITEM_REGISTRY.register("electric_piston_zpm", () -> MetaItem.builder("electric_piston.zpm").build());
    public static final RegistryObject<MetaItem> ELECTRIC_PISTON_UV = ITEM_REGISTRY.register("electric_piston_uv", () -> MetaItem.builder("electric_piston.uv").build());
    public static final RegistryObject<MetaItem> ELECTRIC_PISTON_UHV = ITEM_REGISTRY.register("electric_piston_uhv", () -> MetaItem.builder("electric_piston.uhv").build());
    public static final RegistryObject<MetaItem> ELECTRIC_PISTON_UEV = ITEM_REGISTRY.register("electric_piston_uev", () -> MetaItem.builder("electric_piston.uev").build());
    public static final RegistryObject<MetaItem> ELECTRIC_PISTON_UIV = ITEM_REGISTRY.register("electric_piston_uiv", () -> MetaItem.builder("electric_piston.uiv").build());
    public static final RegistryObject<MetaItem> ELECTRIC_PISTON_UXV = ITEM_REGISTRY.register("electric_piston_uxv", () -> MetaItem.builder("electric_piston.uxv").build());
    public static final RegistryObject<MetaItem> ELECTRIC_PISTON_OpV = ITEM_REGISTRY.register("electric_piston_opv", () -> MetaItem.builder("electric_piston.opv").build());

    public static final RegistryObject<MetaItem> ROBOT_ARM_LV = ITEM_REGISTRY.register("robot_arm_lv", () -> MetaItem.builder("robot_arm.lv").build());
    public static final RegistryObject<MetaItem> ROBOT_ARM_MV = ITEM_REGISTRY.register("robot_arm_mv", () -> MetaItem.builder("robot_arm.mv").build());
    public static final RegistryObject<MetaItem> ROBOT_ARM_HV = ITEM_REGISTRY.register("robot_arm_hv", () -> MetaItem.builder("robot_arm.hv").build());
    public static final RegistryObject<MetaItem> ROBOT_ARM_EV = ITEM_REGISTRY.register("robot_arm_ev", () -> MetaItem.builder("robot_arm.ev").build());
    public static final RegistryObject<MetaItem> ROBOT_ARM_IV = ITEM_REGISTRY.register("robot_arm_iv", () -> MetaItem.builder("robot_arm.iv").build());
    public static final RegistryObject<MetaItem> ROBOT_ARM_LuV = ITEM_REGISTRY.register("robot_arm_luv", () -> MetaItem.builder("robot_arm.luv").build());
    public static final RegistryObject<MetaItem> ROBOT_ARM_ZPM = ITEM_REGISTRY.register("robot_arm_zpm", () -> MetaItem.builder("robot_arm.zpm").build());
    public static final RegistryObject<MetaItem> ROBOT_ARM_UV = ITEM_REGISTRY.register("robot_arm_uv", () -> MetaItem.builder("robot_arm.uv").build());
    public static final RegistryObject<MetaItem> ROBOT_ARM_UHV = ITEM_REGISTRY.register("robot_arm_uhv", () -> MetaItem.builder("robot_arm.uhv").build());
    public static final RegistryObject<MetaItem> ROBOT_ARM_UEV = ITEM_REGISTRY.register("robot_arm_uev", () -> MetaItem.builder("robot_arm.uev").build());
    public static final RegistryObject<MetaItem> ROBOT_ARM_UIV = ITEM_REGISTRY.register("robot_arm_uiv", () -> MetaItem.builder("robot_arm.uiv").build());
    public static final RegistryObject<MetaItem> ROBOT_ARM_UXV = ITEM_REGISTRY.register("robot_arm_uxv", () -> MetaItem.builder("robot_arm.uxv").build());
    public static final RegistryObject<MetaItem> ROBOT_ARM_OpV = ITEM_REGISTRY.register("robot_arm_opv", () -> MetaItem.builder("robot_arm.opv").build());

    public static final RegistryObject<MetaItem> FIELD_GENERATOR_LV = ITEM_REGISTRY.register("field_generator_lv", () -> MetaItem.builder("field_generator.lv").build());
    public static final RegistryObject<MetaItem> FIELD_GENERATOR_MV = ITEM_REGISTRY.register("field_generator_mv", () -> MetaItem.builder("field_generator.mv").build());
    public static final RegistryObject<MetaItem> FIELD_GENERATOR_HV = ITEM_REGISTRY.register("field_generator_hv", () -> MetaItem.builder("field_generator.hv").build());
    public static final RegistryObject<MetaItem> FIELD_GENERATOR_EV = ITEM_REGISTRY.register("field_generator_ev", () -> MetaItem.builder("field_generator.ev").build());
    public static final RegistryObject<MetaItem> FIELD_GENERATOR_IV = ITEM_REGISTRY.register("field_generator_iv", () -> MetaItem.builder("field_generator.iv").build());
    public static final RegistryObject<MetaItem> FIELD_GENERATOR_LuV = ITEM_REGISTRY.register("field_generator_luv", () -> MetaItem.builder("field_generator.luv").build());
    public static final RegistryObject<MetaItem> FIELD_GENERATOR_ZPM = ITEM_REGISTRY.register("field_generator_zpm", () -> MetaItem.builder("field_generator.zpm").build());
    public static final RegistryObject<MetaItem> FIELD_GENERATOR_UV = ITEM_REGISTRY.register("field_generator_uv", () -> MetaItem.builder("field_generator.uv").build());
    public static final RegistryObject<MetaItem> FIELD_GENERATOR_UHV = ITEM_REGISTRY.register("field_generator_uhv", () -> MetaItem.builder("field_generator.uhv").build());
    public static final RegistryObject<MetaItem> FIELD_GENERATOR_UEV = ITEM_REGISTRY.register("field_generator_uev", () -> MetaItem.builder("field_generator.uev").build());
    public static final RegistryObject<MetaItem> FIELD_GENERATOR_UIV = ITEM_REGISTRY.register("field_generator_uiv", () -> MetaItem.builder("field_generator.uiv").build());
    public static final RegistryObject<MetaItem> FIELD_GENERATOR_UXV = ITEM_REGISTRY.register("field_generator_uxv", () -> MetaItem.builder("field_generator.uxv").build());
    public static final RegistryObject<MetaItem> FIELD_GENERATOR_OpV = ITEM_REGISTRY.register("field_generator_opv", () -> MetaItem.builder("field_generator.opv").build());

    public static final RegistryObject<MetaItem> EMITTER_LV = ITEM_REGISTRY.register("emitter_lv", () -> MetaItem.builder("emitter.lv").build());
    public static final RegistryObject<MetaItem> EMITTER_MV = ITEM_REGISTRY.register("emitter_mv", () -> MetaItem.builder("emitter.mv").build());
    public static final RegistryObject<MetaItem> EMITTER_HV = ITEM_REGISTRY.register("emitter_hv", () -> MetaItem.builder("emitter.hv").build());
    public static final RegistryObject<MetaItem> EMITTER_EV = ITEM_REGISTRY.register("emitter_ev", () -> MetaItem.builder("emitter.ev").build());
    public static final RegistryObject<MetaItem> EMITTER_IV = ITEM_REGISTRY.register("emitter_iv", () -> MetaItem.builder("emitter.iv").build());
    public static final RegistryObject<MetaItem> EMITTER_LuV = ITEM_REGISTRY.register("emitter_luv", () -> MetaItem.builder("emitter.luv").build());
    public static final RegistryObject<MetaItem> EMITTER_ZPM = ITEM_REGISTRY.register("emitter_zpm", () -> MetaItem.builder("emitter.zpm").build());
    public static final RegistryObject<MetaItem> EMITTER_UV = ITEM_REGISTRY.register("emitter_uv", () -> MetaItem.builder("emitter.uv").build());
    public static final RegistryObject<MetaItem> EMITTER_UHV = ITEM_REGISTRY.register("emitter_uhv", () -> MetaItem.builder("emitter.uhv").build());
    public static final RegistryObject<MetaItem> EMITTER_UEV = ITEM_REGISTRY.register("emitter_uev", () -> MetaItem.builder("emitter.uev").build());
    public static final RegistryObject<MetaItem> EMITTER_UIV = ITEM_REGISTRY.register("emitter_uiv", () -> MetaItem.builder("emitter.uiv").build());
    public static final RegistryObject<MetaItem> EMITTER_UXV = ITEM_REGISTRY.register("emitter_uxv", () -> MetaItem.builder("emitter.uxv").build());
    public static final RegistryObject<MetaItem> EMITTER_OpV = ITEM_REGISTRY.register("emitter_opv", () -> MetaItem.builder("emitter.opv").build());

    public static final RegistryObject<MetaItem> SENSOR_LV = ITEM_REGISTRY.register("sensor_lv", () -> MetaItem.builder("sensor.lv").build());
    public static final RegistryObject<MetaItem> SENSOR_MV = ITEM_REGISTRY.register("sensor_mv", () -> MetaItem.builder("sensor.mv").build());
    public static final RegistryObject<MetaItem> SENSOR_HV = ITEM_REGISTRY.register("sensor_hv", () -> MetaItem.builder("sensor.hv").build());
    public static final RegistryObject<MetaItem> SENSOR_EV = ITEM_REGISTRY.register("sensor_ev", () -> MetaItem.builder("sensor.ev").build());
    public static final RegistryObject<MetaItem> SENSOR_IV = ITEM_REGISTRY.register("sensor_iv", () -> MetaItem.builder("sensor.iv").build());
    public static final RegistryObject<MetaItem> SENSOR_LuV = ITEM_REGISTRY.register("sensor_luv", () -> MetaItem.builder("sensor.luv").build());
    public static final RegistryObject<MetaItem> SENSOR_ZPM = ITEM_REGISTRY.register("sensor_zpm", () -> MetaItem.builder("sensor.zpm").build());
    public static final RegistryObject<MetaItem> SENSOR_UV = ITEM_REGISTRY.register("sensor_uv", () -> MetaItem.builder("sensor.uv").build());
    public static final RegistryObject<MetaItem> SENSOR_UHV = ITEM_REGISTRY.register("sensor_uhv", () -> MetaItem.builder("sensor.uhv").build());
    public static final RegistryObject<MetaItem> SENSOR_UEV = ITEM_REGISTRY.register("sensor_uev", () -> MetaItem.builder("sensor.uev").build());
    public static final RegistryObject<MetaItem> SENSOR_UIV = ITEM_REGISTRY.register("sensor_uiv", () -> MetaItem.builder("sensor.uiv").build());
    public static final RegistryObject<MetaItem> SENSOR_UXV = ITEM_REGISTRY.register("sensor_uxv", () -> MetaItem.builder("sensor.uxv").build());
    public static final RegistryObject<MetaItem> SENSOR_OpV = ITEM_REGISTRY.register("sensor_opv", () -> MetaItem.builder("sensor.opv").build());

    public static final RegistryObject<MetaItem> TOOL_DATA_STICK = ITEM_REGISTRY.register("tool_data_stick", () -> MetaItem.builder("tool_data.stick").build());
    public static final RegistryObject<MetaItem> TOOL_DATA_ORB = ITEM_REGISTRY.register("tool_data_orb", () -> MetaItem.builder("tool_data.orb").build());

    public static final Map<MarkerMaterial, MetaItem> GLASS_LENSES = new HashMap<>();

    public static final RegistryObject<MetaItem> SILICON_BOULE = ITEM_REGISTRY.register("silicon_boule", () -> MetaItem.builder("silicon.boule").build());
    public static final RegistryObject<MetaItem> GLOWSTONE_BOULE = ITEM_REGISTRY.register("glowstone_boule", () -> MetaItem.builder("glowstone.boule").build());
    public static final RegistryObject<MetaItem> NAQUADAH_BOULE = ITEM_REGISTRY.register("naquadah_boule", () -> MetaItem.builder("naquadah.boule").build());
    public static final RegistryObject<MetaItem> NEUTRONIUM_BOULE = ITEM_REGISTRY.register("neutronium_boule", () -> MetaItem.builder("neutronium.boule").build());
    public static final RegistryObject<MetaItem> SILICON_WAFER = ITEM_REGISTRY.register("silicon_wafer", () -> MetaItem.builder("silicon.wafer").build());
    public static final RegistryObject<MetaItem> GLOWSTONE_WAFER = ITEM_REGISTRY.register("glowstone_wafer", () -> MetaItem.builder("glowstone.wafer").build());
    public static final RegistryObject<MetaItem> NAQUADAH_WAFER = ITEM_REGISTRY.register("naquadah_wafer", () -> MetaItem.builder("naquadah.wafer").build());
    public static final RegistryObject<MetaItem> NEUTRONIUM_WAFER = ITEM_REGISTRY.register("neutronium_wafer", () -> MetaItem.builder("neutronium.wafer").build());

    public static final RegistryObject<MetaItem> HIGHLY_ADVANCED_SOC_WAFER = ITEM_REGISTRY.register("highly_advanced_soc_wafer", () -> MetaItem.builder("highly_advanced_soc.wafer").build());
    public static final RegistryObject<MetaItem> ADVANCED_SYSTEM_ON_CHIP_WAFER = ITEM_REGISTRY.register("advanced_system_on_chip_wafer", () -> MetaItem.builder("advanced_system_on_chip.wafer").build());
    public static final RegistryObject<MetaItem> INTEGRATED_LOGIC_CIRCUIT_WAFER = ITEM_REGISTRY.register("integrated_logic_circuit_wafer", () -> MetaItem.builder("integrated_logic_circuit.wafer").build());
    public static final RegistryObject<MetaItem> CENTRAL_PROCESSING_UNIT_WAFER = ITEM_REGISTRY.register("central_processing_unit_wafer", () -> MetaItem.builder("central_processing_unit.wafer").build());
    public static final RegistryObject<MetaItem> ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER = ITEM_REGISTRY.register("ultra_low_power_integrated_circuit_wafer", () -> MetaItem.builder("ultra_low_power_integrated_circuit.wafer").build());
    public static final RegistryObject<MetaItem> LOW_POWER_INTEGRATED_CIRCUIT_WAFER = ITEM_REGISTRY.register("low_power_integrated_circuit_wafer", () -> MetaItem.builder("low_power_integrated_circuit.wafer").build());
    public static final RegistryObject<MetaItem> POWER_INTEGRATED_CIRCUIT_WAFER = ITEM_REGISTRY.register("power_integrated_circuit_wafer", () -> MetaItem.builder("power_integrated_circuit.wafer").build());
    public static final RegistryObject<MetaItem> HIGH_POWER_INTEGRATED_CIRCUIT_WAFER = ITEM_REGISTRY.register("high_power_integrated_circuit_wafer", () -> MetaItem.builder("high_power_integrated_circuit.wafer").build());
    public static final RegistryObject<MetaItem> ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT_WAFER = ITEM_REGISTRY.register("ultra_high_power_integrated_circuit_wafer", () -> MetaItem.builder("ultra_high_power_integrated_circuit.wafer").build());
    public static final RegistryObject<MetaItem> NAND_MEMORY_CHIP_WAFER = ITEM_REGISTRY.register("nand_memory_chip_wafer", () -> MetaItem.builder("nand_memory_chip.wafer").build());
    public static final RegistryObject<MetaItem> NANO_CENTRAL_PROCESSING_UNIT_WAFER = ITEM_REGISTRY.register("nano_central_processing_unit_wafer", () -> MetaItem.builder("nano_central_processing_unit.wafer").build());
    public static final RegistryObject<MetaItem> NOR_MEMORY_CHIP_WAFER = ITEM_REGISTRY.register("nor_memory_chip_wafer", () -> MetaItem.builder("nor_memory_chip.wafer").build());
    public static final RegistryObject<MetaItem> QUBIT_CENTRAL_PROCESSING_UNIT_WAFER = ITEM_REGISTRY.register("qubit_central_processing_unit_wafer", () -> MetaItem.builder("qubit_central_processing_unit.wafer").build());
    public static final RegistryObject<MetaItem> RANDOM_ACCESS_MEMORY_WAFER = ITEM_REGISTRY.register("random_access_memory_wafer", () -> MetaItem.builder("random_access_memory.wafer").build());
    public static final RegistryObject<MetaItem> SYSTEM_ON_CHIP_WAFER = ITEM_REGISTRY.register("system_on_chip_wafer", () -> MetaItem.builder("system_on_chip.wafer").build());
    public static final RegistryObject<MetaItem> SIMPLE_SYSTEM_ON_CHIP_WAFER = ITEM_REGISTRY.register("simple_system_on_chip_wafer", () -> MetaItem.builder("simple_system_on_chip.wafer").build());

    public static final RegistryObject<MetaItem> ENGRAVED_CRYSTAL_CHIP = ITEM_REGISTRY.register("engraved_crystal_chip", () -> MetaItem.builder("engraved_crystal.chip").build());
    public static final RegistryObject<MetaItem> ENGRAVED_LAPOTRON_CHIP = ITEM_REGISTRY.register("engraved_lapotron_chip", () -> MetaItem.builder("engraved_lapotron.chip").build());

    public static final RegistryObject<MetaItem> HIGHLY_ADVANCED_SOC = ITEM_REGISTRY.register("highly_advanced_soc", () -> MetaItem.builder("highly_advanced.soc").build());
    public static final RegistryObject<MetaItem> ADVANCED_SYSTEM_ON_CHIP = ITEM_REGISTRY.register("advanced_system_on_chip", () -> MetaItem.builder("advanced_system_on.chip").build());
    public static final RegistryObject<MetaItem> INTEGRATED_LOGIC_CIRCUIT = ITEM_REGISTRY.register("integrated_logic_circuit", () -> MetaItem.builder("integrated_logic.circuit").build());
    public static final RegistryObject<MetaItem> CENTRAL_PROCESSING_UNIT = ITEM_REGISTRY.register("central_processing_unit", () -> MetaItem.builder("central_processing.unit").build());
    public static final RegistryObject<MetaItem> ULTRA_LOW_POWER_INTEGRATED_CIRCUIT = ITEM_REGISTRY.register("ultra_low_power_integrated_circuit", () -> MetaItem.builder("ultra_low_power_integrated.circuit").build());
    public static final RegistryObject<MetaItem> LOW_POWER_INTEGRATED_CIRCUIT = ITEM_REGISTRY.register("low_power_integrated_circuit", () -> MetaItem.builder("low_power_integrated.circuit").build());
    public static final RegistryObject<MetaItem> POWER_INTEGRATED_CIRCUIT = ITEM_REGISTRY.register("power_integrated_circuit", () -> MetaItem.builder("power_integrated.circuit").build());
    public static final RegistryObject<MetaItem> HIGH_POWER_INTEGRATED_CIRCUIT = ITEM_REGISTRY.register("high_power_integrated_circuit", () -> MetaItem.builder("high_power_integrated.circuit").build());
    public static final RegistryObject<MetaItem> ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT = ITEM_REGISTRY.register("ultra_high_power_integrated_circuit", () -> MetaItem.builder("ultra_high_power_integrated.circuit").build());
    public static final RegistryObject<MetaItem> NAND_MEMORY_CHIP = ITEM_REGISTRY.register("nand_memory_chip", () -> MetaItem.builder("nand_memory.chip").build());
    public static final RegistryObject<MetaItem> NANO_CENTRAL_PROCESSING_UNIT = ITEM_REGISTRY.register("nano_central_processing_unit", () -> MetaItem.builder("nano_central_processing.unit").build());
    public static final RegistryObject<MetaItem> NOR_MEMORY_CHIP = ITEM_REGISTRY.register("nor_memory_chip", () -> MetaItem.builder("nor_memory.chip").build());
    public static final RegistryObject<MetaItem> QUBIT_CENTRAL_PROCESSING_UNIT = ITEM_REGISTRY.register("qubit_central_processing_unit", () -> MetaItem.builder("qubit_central_processing.unit").build());
    public static final RegistryObject<MetaItem> RANDOM_ACCESS_MEMORY = ITEM_REGISTRY.register("random_access_memory", () -> MetaItem.builder("random_access.memory").build());
    public static final RegistryObject<MetaItem> SYSTEM_ON_CHIP = ITEM_REGISTRY.register("system_on_chip", () -> MetaItem.builder("system_on.chip").build());
    public static final RegistryObject<MetaItem> SIMPLE_SYSTEM_ON_CHIP = ITEM_REGISTRY.register("simple_system_on_chip", () -> MetaItem.builder("simple_system_on.chip").build());

    public static final RegistryObject<MetaItem> RAW_CRYSTAL_CHIP = ITEM_REGISTRY.register("raw_crystal_chip", () -> MetaItem.builder("raw_crystal.chip").build());
    public static final RegistryObject<MetaItem> RAW_CRYSTAL_CHIP_PART = ITEM_REGISTRY.register("raw_crystal_chip_part", () -> MetaItem.builder("raw_crystal_chip.part").build());
    public static final RegistryObject<MetaItem> CRYSTAL_CENTRAL_PROCESSING_UNIT = ITEM_REGISTRY.register("crystal_central_processing_unit", () -> MetaItem.builder("crystal_central_processing.unit").build());
    public static final RegistryObject<MetaItem> CRYSTAL_SYSTEM_ON_CHIP = ITEM_REGISTRY.register("crystal_system_on_chip", () -> MetaItem.builder("crystal_system_on.chip").build());

    public static final RegistryObject<MetaItem> COATED_BOARD = ITEM_REGISTRY.register("coated_board", () -> MetaItem.builder("coated.board").build());
    public static final RegistryObject<MetaItem> PHENOLIC_BOARD = ITEM_REGISTRY.register("phenolic_board", () -> MetaItem.builder("phenolic.board").build());
    public static final RegistryObject<MetaItem> PLASTIC_BOARD = ITEM_REGISTRY.register("plastic_board", () -> MetaItem.builder("plastic.board").build());
    public static final RegistryObject<MetaItem> EPOXY_BOARD = ITEM_REGISTRY.register("epoxy_board", () -> MetaItem.builder("epoxy.board").build());
    public static final RegistryObject<MetaItem> FIBER_BOARD = ITEM_REGISTRY.register("fiber_board", () -> MetaItem.builder("fiber.board").build());
    public static final RegistryObject<MetaItem> MULTILAYER_FIBER_BOARD = ITEM_REGISTRY.register("multilayer_fiber_board", () -> MetaItem.builder("multilayer_fiber.board").build());
    public static final RegistryObject<MetaItem> WETWARE_BOARD = ITEM_REGISTRY.register("wetware_board", () -> MetaItem.builder("wetware.board").build());

    public static final RegistryObject<MetaItem> BASIC_CIRCUIT_BOARD = ITEM_REGISTRY.register("basic_circuit_board", () -> MetaItem.builder("basic_circuit.board").build());
    public static final RegistryObject<MetaItem> GOOD_CIRCUIT_BOARD = ITEM_REGISTRY.register("good_circuit_board", () -> MetaItem.builder("good_circuit.board").build());
    public static final RegistryObject<MetaItem> PLASTIC_CIRCUIT_BOARD = ITEM_REGISTRY.register("plastic_circuit_board", () -> MetaItem.builder("plastic_circuit.board").build());
    public static final RegistryObject<MetaItem> ADVANCED_CIRCUIT_BOARD = ITEM_REGISTRY.register("advanced_circuit_board", () -> MetaItem.builder("advanced_circuit.board").build());
    public static final RegistryObject<MetaItem> EXTREME_CIRCUIT_BOARD = ITEM_REGISTRY.register("extreme_circuit_board", () -> MetaItem.builder("extreme_circuit.board").build());
    public static final RegistryObject<MetaItem> ELITE_CIRCUIT_BOARD = ITEM_REGISTRY.register("elite_circuit_board", () -> MetaItem.builder("elite_circuit.board").build());
    public static final RegistryObject<MetaItem> WETWARE_CIRCUIT_BOARD = ITEM_REGISTRY.register("wetware_circuit_board", () -> MetaItem.builder("wetware_circuit.board").build());

    public static final RegistryObject<MetaItem> VACUUM_TUBE = ITEM_REGISTRY.register("vacuum_tube", () -> MetaItem.builder("vacuum.tube").build());
    public static final RegistryObject<MetaItem> GLASS_TUBE = ITEM_REGISTRY.register("component/glass_tube", () -> MetaItem.builder("component.glass.tube").build());
    public static final RegistryObject<MetaItem> RESISTOR = ITEM_REGISTRY.register("component/resistor", () -> MetaItem.builder("component.resistor").setUnificationData(TagPrefix.component, MarkerMaterials.Component.Resistor.get()).build());
    public static final RegistryObject<MetaItem> DIODE = ITEM_REGISTRY.register("component/diode", () -> MetaItem.builder("component.diode").setUnificationData(TagPrefix.component, MarkerMaterials.Component.Diode.get()).build());
    public static final RegistryObject<MetaItem> CAPACITOR = ITEM_REGISTRY.register("component/capacitor", () -> MetaItem.builder("component.capacitor").setUnificationData(TagPrefix.component, MarkerMaterials.Component.Capacitor.get()).build());
    public static final RegistryObject<MetaItem> TRANSISTOR = ITEM_REGISTRY.register("component/transistor", () -> MetaItem.builder("component.transistor").setUnificationData(TagPrefix.component, MarkerMaterials.Component.Transistor.get()).build());
    public static final RegistryObject<MetaItem> INDUCTOR = ITEM_REGISTRY.register("component/inductor", () -> MetaItem.builder("component.inductor").setUnificationData(TagPrefix.component, MarkerMaterials.Component.Inductor.get()).build());
    public static final RegistryObject<MetaItem> SMD_CAPACITOR = ITEM_REGISTRY.register("smd_capacitor", () -> MetaItem.builder("component.smd.capacitor").build());
    public static final RegistryObject<MetaItem> SMD_DIODE = ITEM_REGISTRY.register("smd_diode", () -> MetaItem.builder("component.smd.diode").setUnificationData(TagPrefix.component, MarkerMaterials.Component.Diode.get()).build());
    public static final RegistryObject<MetaItem> SMD_RESISTOR = ITEM_REGISTRY.register("smd_resistor", () -> MetaItem.builder("component.smd.resistor").setUnificationData(TagPrefix.component, MarkerMaterials.Component.Resistor.get()).build());
    public static final RegistryObject<MetaItem> SMD_TRANSISTOR = ITEM_REGISTRY.register("smd_transistor", () -> MetaItem.builder("component.smd.transistor").setUnificationData(TagPrefix.component, MarkerMaterials.Component.Transistor.get()).build());
    public static final RegistryObject<MetaItem> SMD_INDUCTOR = ITEM_REGISTRY.register("smd_inductor", () -> MetaItem.builder("component.smd.inductor").setUnificationData(TagPrefix.component, MarkerMaterials.Component.Inductor.get()).build());
    public static final RegistryObject<MetaItem> ADVANCED_SMD_CAPACITOR = ITEM_REGISTRY.register("advanced_smd_capacitor", () -> MetaItem.builder("component.advanced_smd.capacitor").build());
    public static final RegistryObject<MetaItem> ADVANCED_SMD_DIODE = ITEM_REGISTRY.register("advanced_smd_diode", () -> MetaItem.builder("component.advanced_smd.diode").build());
    public static final RegistryObject<MetaItem> ADVANCED_SMD_RESISTOR = ITEM_REGISTRY.register("advanced_smd_resistor", () -> MetaItem.builder("component.advanced_smd.resistor").build());
    public static final RegistryObject<MetaItem> ADVANCED_SMD_TRANSISTOR = ITEM_REGISTRY.register("advanced_smd_transistor", () -> MetaItem.builder("component.advanced_smd.transistor").build());
    public static final RegistryObject<MetaItem> ADVANCED_SMD_INDUCTOR = ITEM_REGISTRY.register("advanced_smd_inductor", () -> MetaItem.builder("component.advanced_smd.inductor").build());

    // T1: Electronic
    public static final RegistryObject<MetaItem> ELECTRONIC_CIRCUIT_LV = ITEM_REGISTRY.register("electronic_circuit_lv", () -> MetaItem.builder("electronic_circuit.lv").build());
    public static final RegistryObject<MetaItem> ELECTRONIC_CIRCUIT_MV = ITEM_REGISTRY.register("electronic_circuit_mv", () -> MetaItem.builder("electronic_circuit.mv").build());

    // T2: Integrated
    public static final RegistryObject<MetaItem> INTEGRATED_CIRCUIT_LV = ITEM_REGISTRY.register("integrated_circuit_lv", () -> MetaItem.builder("integrated_circuit.lv").build());
    public static final RegistryObject<MetaItem> INTEGRATED_CIRCUIT_MV = ITEM_REGISTRY.register("integrated_circuit_mv", () -> MetaItem.builder("integrated_circuit.mv").build());
    public static final RegistryObject<MetaItem> INTEGRATED_CIRCUIT_HV = ITEM_REGISTRY.register("integrated_circuit_hv", () -> MetaItem.builder("integrated_circuit.hv").build());

    // ULV/LV easier circuits
    public static final RegistryObject<MetaItem> NAND_CHIP_ULV = ITEM_REGISTRY.register("nand_chip_ulv", () -> MetaItem.builder("nand_chip.ulv").build());
    public static final RegistryObject<MetaItem> MICROPROCESSOR_LV = ITEM_REGISTRY.register("microprocessor_lv", () -> MetaItem.builder("microprocessor.lv").build());

    // T3: Processor
    public static final RegistryObject<MetaItem> PROCESSOR_MV = ITEM_REGISTRY.register("processor_mv", () -> MetaItem.builder("processor.mv").build());
    public static final RegistryObject<MetaItem> PROCESSOR_ASSEMBLY_HV = ITEM_REGISTRY.register("processor_assembly_hv", () -> MetaItem.builder("processor_assembly.hv").build());
    public static final RegistryObject<MetaItem> WORKSTATION_EV = ITEM_REGISTRY.register("workstation_ev", () -> MetaItem.builder("workstation.ev").build());
    public static final RegistryObject<MetaItem> MAINFRAME_IV = ITEM_REGISTRY.register("mainframe_iv", () -> MetaItem.builder("mainframe.iv").build());

    // T4: Nano
    public static final RegistryObject<MetaItem> NANO_PROCESSOR_HV = ITEM_REGISTRY.register("nano_processor_hv", () -> MetaItem.builder("nano_processor.hv").build());
    public static final RegistryObject<MetaItem> NANO_PROCESSOR_ASSEMBLY_EV = ITEM_REGISTRY.register("nano_processor_assembly_ev", () -> MetaItem.builder("nano_processor_assembly.ev").build());
    public static final RegistryObject<MetaItem> NANO_COMPUTER_IV = ITEM_REGISTRY.register("nano_computer_iv", () -> MetaItem.builder("nano_computer.iv").build());
    public static final RegistryObject<MetaItem> NANO_MAINFRAME_LUV = ITEM_REGISTRY.register("nano_mainframe_luv", () -> MetaItem.builder("nano_mainframe.luv").build());

    // T5: Quantum
    public static final RegistryObject<MetaItem> QUANTUM_PROCESSOR_EV = ITEM_REGISTRY.register("quantum_processor_ev", () -> MetaItem.builder("quantum_processor.ev").build());
    public static final RegistryObject<MetaItem> QUANTUM_ASSEMBLY_IV = ITEM_REGISTRY.register("quantum_assembly_iv", () -> MetaItem.builder("quantum_assembly.iv").build());
    public static final RegistryObject<MetaItem> QUANTUM_COMPUTER_LUV = ITEM_REGISTRY.register("quantum_computer_luv", () -> MetaItem.builder("quantum_computer.luv").build());
    public static final RegistryObject<MetaItem> QUANTUM_MAINFRAME_ZPM = ITEM_REGISTRY.register("quantum_mainframe_zpm", () -> MetaItem.builder("quantum_mainframe.zpm").build());

    // T6: Crystal
    public static final RegistryObject<MetaItem> CRYSTAL_PROCESSOR_IV = ITEM_REGISTRY.register("crystal_processor_iv", () -> MetaItem.builder("crystal_processor.iv").build());
    public static final RegistryObject<MetaItem> CRYSTAL_ASSEMBLY_LUV = ITEM_REGISTRY.register("crystal_assembly_luv", () -> MetaItem.builder("crystal_assembly.luv").build());
    public static final RegistryObject<MetaItem> CRYSTAL_COMPUTER_ZPM = ITEM_REGISTRY.register("crystal_computer_zpm", () -> MetaItem.builder("crystal_computer.zpm").build());
    public static final RegistryObject<MetaItem> CRYSTAL_MAINFRAME_UV = ITEM_REGISTRY.register("crystal_mainframe_uv", () -> MetaItem.builder("crystal_mainframe.uv").build());

    // T7: Wetware
    public static final RegistryObject<MetaItem> WETWARE_PROCESSOR_LUV = ITEM_REGISTRY.register("wetware_processor_luv", () -> MetaItem.builder("wetware_processor.luv").build());
    public static final RegistryObject<MetaItem> WETWARE_PROCESSOR_ASSEMBLY_ZPM = ITEM_REGISTRY.register("wetware_processor_assembly_zpm", () -> MetaItem.builder("wetware_processor_assembly.zpm").build());
    public static final RegistryObject<MetaItem> WETWARE_SUPER_COMPUTER_UV = ITEM_REGISTRY.register("wetware_super_computer_uv", () -> MetaItem.builder("wetware_super_computer.uv").build());
    public static final RegistryObject<MetaItem> WETWARE_MAINFRAME_UHV = ITEM_REGISTRY.register("wetware_mainframe_uhv", () -> MetaItem.builder("wetware_mainframe.uhv").build());

    public static final RegistryObject<MetaItem> COMPONENT_GRINDER_DIAMOND = ITEM_REGISTRY.register("component_grinder_diamond", () -> MetaItem.builder("component_grinder.diamond").build());
    public static final RegistryObject<MetaItem> COMPONENT_GRINDER_TUNGSTEN = ITEM_REGISTRY.register("component_grinder_tungsten", () -> MetaItem.builder("component_grinder.tungsten").build());

    public static final RegistryObject<MetaItem> QUANTUM_EYE = ITEM_REGISTRY.register("quantum_eye", () -> MetaItem.builder("quantum.eye").build());
    public static final RegistryObject<MetaItem> QUANTUM_STAR = ITEM_REGISTRY.register("quantum_star", () -> MetaItem.builder("quantum.star").build());
    public static final RegistryObject<MetaItem> GRAVI_STAR = ITEM_REGISTRY.register("gravi_star", () -> MetaItem.builder("gravi.star").build());

    public static final RegistryObject<MetaItem> ITEM_FILTER = ITEM_REGISTRY.register("item_filter", () -> MetaItem.builder("item.filter").build());
    public static final RegistryObject<MetaItem> ORE_DICTIONARY_FILTER = ITEM_REGISTRY.register("ore_dictionary_filter", () -> MetaItem.builder("ore_dictionary.filter").build());
    public static final RegistryObject<MetaItem> SMART_FILTER = ITEM_REGISTRY.register("smart_filter", () -> MetaItem.builder("smart.filter").build());

    public static final RegistryObject<MetaItem> COVER_SHUTTER = ITEM_REGISTRY.register("cover_shutter", () -> MetaItem.builder("cover.shutter").build());
    public static final RegistryObject<MetaItem> COVER_MACHINE_CONTROLLER = ITEM_REGISTRY.register("cover_machine_controller", () -> MetaItem.builder("cover_machine.controller").build());
    public static final RegistryObject<MetaItem> COVER_FACADE = ITEM_REGISTRY.register("cover_facade", () -> MetaItem.builder("cover.facade").build());

    public static final RegistryObject<MetaItem> COVER_ACTIVITY_DETECTOR = ITEM_REGISTRY.register("cover_activity_detector", () -> MetaItem.builder("cover_activity.detector").build());
    public static final RegistryObject<MetaItem> COVER_ACTIVITY_DETECTOR_ADVANCED = ITEM_REGISTRY.register("cover_activity_detector_advanced", () -> MetaItem.builder("cover_activity_detector.advanced").build());
    public static final RegistryObject<MetaItem> COVER_FLUID_DETECTOR = ITEM_REGISTRY.register("cover_fluid_detector", () -> MetaItem.builder("cover_fluid.detector").build());
    public static final RegistryObject<MetaItem> COVER_ITEM_DETECTOR = ITEM_REGISTRY.register("cover_item_detector", () -> MetaItem.builder("cover_item.detector").build());
    public static final RegistryObject<MetaItem> COVER_ENERGY_DETECTOR = ITEM_REGISTRY.register("cover_energy_detector", () -> MetaItem.builder("cover_energy.detector").build());
    public static final RegistryObject<MetaItem> COVER_ENERGY_DETECTOR_ADVANCED = ITEM_REGISTRY.register("cover_energy_detector_advanced", () -> MetaItem.builder("cover_energy_detector.advanced").build());

    public static final RegistryObject<MetaItem> COVER_SCREEN = ITEM_REGISTRY.register("cover_screen", () -> MetaItem.builder("cover.screen").build());
    public static final RegistryObject<MetaItem> COVER_CRAFTING = ITEM_REGISTRY.register("cover_crafting", () -> MetaItem.builder("cover.crafting").build());
    public static final RegistryObject<MetaItem> COVER_INFINITE_WATER = ITEM_REGISTRY.register("cover_infinite_water", () -> MetaItem.builder("cover_infinite.water").build());
    public static final RegistryObject<MetaItem> COVER_ENDER_FLUID_LINK = ITEM_REGISTRY.register("cover_ender_fluid_link", () -> MetaItem.builder("cover_ender_fluid.link").build());
    public static final RegistryObject<MetaItem> COVER_DIGITAL_INTERFACE = ITEM_REGISTRY.register("cover_digital_interface", () -> MetaItem.builder("cover_digital.interface").build());
    public static final RegistryObject<MetaItem> COVER_DIGITAL_INTERFACE_WIRELESS = ITEM_REGISTRY.register("cover_digital_interface_wireless", () -> MetaItem.builder("cover_digital_interface.wireless").build());
    public static final RegistryObject<MetaItem> COVER_FLUID_VOIDING = ITEM_REGISTRY.register("cover_fluid_voiding", () -> MetaItem.builder("cover_fluid.voiding").build());
    public static final RegistryObject<MetaItem> COVER_FLUID_VOIDING_ADVANCED = ITEM_REGISTRY.register("cover_fluid_voiding_advanced", () -> MetaItem.builder("cover_fluid_voiding.advanced").build());
    public static final RegistryObject<MetaItem> COVER_ITEM_VOIDING = ITEM_REGISTRY.register("cover_item_voiding", () -> MetaItem.builder("cover_item.voiding").build());
    public static final RegistryObject<MetaItem> COVER_ITEM_VOIDING_ADVANCED = ITEM_REGISTRY.register("cover_item_voiding_advanced", () -> MetaItem.builder("cover_item_voiding.advanced").build());

    public static final RegistryObject<MetaItem> COVER_SOLAR_PANEL = ITEM_REGISTRY.register("cover_solar_panel", () -> MetaItem.builder("cover_solar.panel").build());
    public static final RegistryObject<MetaItem> COVER_SOLAR_PANEL_ULV = ITEM_REGISTRY.register("cover_solar_panel_ulv", () -> MetaItem.builder("cover_solar_panel.ulv").build());
    public static final RegistryObject<MetaItem> COVER_SOLAR_PANEL_LV = ITEM_REGISTRY.register("cover_solar_panel_lv", () -> MetaItem.builder("cover_solar_panel.lv").build());
    public static final RegistryObject<MetaItem> COVER_SOLAR_PANEL_MV = ITEM_REGISTRY.register("cover_solar_panel_mv", () -> MetaItem.builder("cover_solar_panel.mv").build());
    public static final RegistryObject<MetaItem> COVER_SOLAR_PANEL_HV = ITEM_REGISTRY.register("cover_solar_panel_hv", () -> MetaItem.builder("cover_solar_panel.hv").build());
    public static final RegistryObject<MetaItem> COVER_SOLAR_PANEL_EV = ITEM_REGISTRY.register("cover_solar_panel_ev", () -> MetaItem.builder("cover_solar_panel.ev").build());
    public static final RegistryObject<MetaItem> COVER_SOLAR_PANEL_IV = ITEM_REGISTRY.register("cover_solar_panel_iv", () -> MetaItem.builder("cover_solar_panel.iv").build());
    public static final RegistryObject<MetaItem> COVER_SOLAR_PANEL_LUV = ITEM_REGISTRY.register("cover_solar_panel_luv", () -> MetaItem.builder("cover_solar_panel.luv").build());
    public static final RegistryObject<MetaItem> COVER_SOLAR_PANEL_ZPM = ITEM_REGISTRY.register("cover_solar_panel_zpm", () -> MetaItem.builder("cover_solar_panel.zpm").build());
    public static final RegistryObject<MetaItem> COVER_SOLAR_PANEL_UV = ITEM_REGISTRY.register("cover_solar_panel_uv", () -> MetaItem.builder("cover_solar_panel.uv").build());


    public static final RegistryObject<MetaItem> PLUGIN_TEXT = ITEM_REGISTRY.register("plugin_text", () -> MetaItem.builder("plugin.text").build());
    public static final RegistryObject<MetaItem> PLUGIN_ONLINE_PIC = ITEM_REGISTRY.register("plugin_online_pic", () -> MetaItem.builder("plugin_online.pic").build());
    public static final RegistryObject<MetaItem> PLUGIN_FAKE_GUI = ITEM_REGISTRY.register("plugin_fake_gui", () -> MetaItem.builder("plugin_fake.gui").build());
    public static final RegistryObject<MetaItem> PLUGIN_ADVANCED_MONITOR = ITEM_REGISTRY.register("plugin_advanced_monitor", () -> MetaItem.builder("plugin_advanced.monitor").build());

    public static final RegistryObject<MetaItem> INTEGRATED_CIRCUIT = ITEM_REGISTRY.register("integrated_circuit", () -> MetaItem.builder("integrated.circuit").build());

    public static final RegistryObject<MetaItem> FOAM_SPRAYER = ITEM_REGISTRY.register("foam_sprayer", () -> MetaItem.builder("foam.sprayer").build());

    public static final RegistryObject<MetaItem> GELLED_TOLUENE = ITEM_REGISTRY.register("gelled_toluene", () -> MetaItem.builder("gelled.toluene").build());

    public static final RegistryObject<MetaItem> BOTTLE_PURPLE_DRINK = ITEM_REGISTRY.register("bottle_purple_drink", () -> MetaItem.builder("bottle_purple.drink").build());

    public static final RegistryObject<MetaItem> PLANT_BALL = ITEM_REGISTRY.register("plant_ball", () -> MetaItem.builder("plant.ball").build());
    public static final RegistryObject<MetaItem> STICKY_RESIN = ITEM_REGISTRY.register("sticky_resin", () -> MetaItem.builder("sticky.resin").build());
    public static final RegistryObject<MetaItem> ENERGIUM_DUST = ITEM_REGISTRY.register("energium_dust", () -> MetaItem.builder("energium.dust").build());

    public static final RegistryObject<MetaItem> POWER_UNIT_LV = ITEM_REGISTRY.register("power_unit_lv", () -> MetaItem.builder("power_unit.lv").build());
    public static final RegistryObject<MetaItem> POWER_UNIT_MV = ITEM_REGISTRY.register("power_unit_mv", () -> MetaItem.builder("power_unit.mv").build());
    public static final RegistryObject<MetaItem> POWER_UNIT_HV = ITEM_REGISTRY.register("power_unit_hv", () -> MetaItem.builder("power_unit.hv").build());
    public static final RegistryObject<MetaItem> POWER_UNIT_EV = ITEM_REGISTRY.register("power_unit_ev", () -> MetaItem.builder("power_unit.ev").build());
    public static final RegistryObject<MetaItem> POWER_UNIT_IV = ITEM_REGISTRY.register("power_unit_iv", () -> MetaItem.builder("power_unit.iv").build());

    public static final RegistryObject<MetaItem> NANO_SABER = ITEM_REGISTRY.register("nano_saber", () -> MetaItem.builder("nano.saber").build());
    public static final RegistryObject<MetaItem> PROSPECTOR_LV = ITEM_REGISTRY.register("prospector_lv", () -> MetaItem.builder("prospector.lv").build());
    public static final RegistryObject<MetaItem> PROSPECTOR_HV = ITEM_REGISTRY.register("prospector_hv", () -> MetaItem.builder("prospector.hv").build());
    public static final RegistryObject<MetaItem> PROSPECTOR_LUV = ITEM_REGISTRY.register("prospector_luv", () -> MetaItem.builder("prospector.luv").build());

    public static final RegistryObject<MetaItem> TRICORDER_SCANNER = ITEM_REGISTRY.register("tricorder_scanner", () -> MetaItem.builder("tricorder.scanner").build());
    public static final RegistryObject<MetaItem> DEBUG_SCANNER = ITEM_REGISTRY.register("debug_scanner", () -> MetaItem.builder("debug.scanner").build());

    public static final RegistryObject<MetaItem> ITEM_MAGNET_LV = ITEM_REGISTRY.register("item_magnet_lv", () -> MetaItem.builder("item_magnet.lv").build());
    public static final RegistryObject<MetaItem> ITEM_MAGNET_HV = ITEM_REGISTRY.register("item_magnet_hv", () -> MetaItem.builder("item_magnet.hv").build());

    public static final RegistryObject<MetaItem> WIRELESS = ITEM_REGISTRY.register("wireless", () -> MetaItem.builder("wireless").build());
    public static final RegistryObject<MetaItem> CAMERA = ITEM_REGISTRY.register("camera", () -> MetaItem.builder("camera").build());
    public static final RegistryObject<MetaItem> TERMINAL = ITEM_REGISTRY.register("terminal", () -> MetaItem.builder("terminal").addComponents(new HardwareProvider(), new TerminalBehaviour()).setMaxStackSize(1));

    public static final List<RegistryObject<MetaItem>>  DYE_ONLY_ITEMS = new SizedArrayList<>(DyeColor.values().length);
    public static final List<RegistryObject<MetaItem>> SPRAY_CAN_DYES = new SizedArrayList<>(DyeColor.values().length);

    public static final RegistryObject<MetaItem> TURBINE_ROTOR = ITEM_REGISTRY.register("turbine_rotor", () -> MetaItem.builder("turbine.rotor").build());

    public static final RegistryObject<MetaItem> ENERGY_MODULE = ITEM_REGISTRY.register("energy_module", () -> MetaItem.builder("energy.module").build());
    public static final RegistryObject<MetaItem> ENERGY_CLUSTER = ITEM_REGISTRY.register("energy_cluster", () -> MetaItem.builder("energy.cluster").build());
    public static final RegistryObject<MetaItem> NEURO_PROCESSOR = ITEM_REGISTRY.register("neuro_processor", () -> MetaItem.builder("neuro.processor").build());
    public static final RegistryObject<MetaItem> STEM_CELLS = ITEM_REGISTRY.register("stem_cells", () -> MetaItem.builder("stem.cells").build());
    public static final RegistryObject<MetaItem> PETRI_DISH = ITEM_REGISTRY.register("petri_dish", () -> MetaItem.builder("petri.dish").build());

    public static final RegistryObject<MetaItem> BIO_CHAFF = ITEM_REGISTRY.register("bio_chaff", () -> MetaItem.builder("bio.chaff").build());

    public static final RegistryObject<MetaItem> VOLTAGE_COIL_ULV = ITEM_REGISTRY.register("voltage_coil_ulv", () -> MetaItem.builder("voltage_coil.ulv").build());
    public static final RegistryObject<MetaItem> VOLTAGE_COIL_LV = ITEM_REGISTRY.register("voltage_coil_lv", () -> MetaItem.builder("voltage_coil.lv").build());
    public static final RegistryObject<MetaItem> VOLTAGE_COIL_MV = ITEM_REGISTRY.register("voltage_coil_mv", () -> MetaItem.builder("voltage_coil.mv").build());
    public static final RegistryObject<MetaItem> VOLTAGE_COIL_HV = ITEM_REGISTRY.register("voltage_coil_hv", () -> MetaItem.builder("voltage_coil.hv").build());
    public static final RegistryObject<MetaItem> VOLTAGE_COIL_EV = ITEM_REGISTRY.register("voltage_coil_ev", () -> MetaItem.builder("voltage_coil.ev").build());
    public static final RegistryObject<MetaItem> VOLTAGE_COIL_IV = ITEM_REGISTRY.register("voltage_coil_iv", () -> MetaItem.builder("voltage_coil.iv").build());
    public static final RegistryObject<MetaItem> VOLTAGE_COIL_LuV = ITEM_REGISTRY.register("voltage_coil_luv", () -> MetaItem.builder("voltage_coil.luv").build());
    public static final RegistryObject<MetaItem> VOLTAGE_COIL_ZPM = ITEM_REGISTRY.register("voltage_coil_zpm", () -> MetaItem.builder("voltage_coil.zpm").build());
    public static final RegistryObject<MetaItem> VOLTAGE_COIL_UV = ITEM_REGISTRY.register("voltage_coil_uv", () -> MetaItem.builder("voltage_coil.uv").build());

    public static final RegistryObject<MetaItem> CLIPBOARD = ITEM_REGISTRY.register("clipboard", () -> MetaItem.builder("clipboard").addComponents(new ClipboardBehavior()).stacksTo(1).build());

    public static final RegistryObject<ArmorMetaItem> NIGHTVISION_GOGGLES = ITEM_REGISTRY.register("nightvision_goggles", () -> ArmorMetaItem.builder("nightvision_goggles").build());

    public static final RegistryObject<ArmorMetaItem> NANO_CHESTPLATE = ITEM_REGISTRY.register("nano_chestplate", () -> ArmorMetaItem.builder("nano_chestplate").build());
    public static final RegistryObject<ArmorMetaItem> NANO_LEGGINGS = ITEM_REGISTRY.register("nano_leggings", () -> ArmorMetaItem.builder("nano_leggings").build());
    public static final RegistryObject<ArmorMetaItem> NANO_BOOTS = ITEM_REGISTRY.register("nano_boots", () -> ArmorMetaItem.builder("nano_boots").build());
    public static final RegistryObject<ArmorMetaItem> NANO_HELMET = ITEM_REGISTRY.register("nano_helmet", () -> ArmorMetaItem.builder("nano_helmet").build());

    public static final RegistryObject<ArmorMetaItem> QUANTUM_CHESTPLATE = ITEM_REGISTRY.register("quantum_chestplate", () -> ArmorMetaItem.builder("quantum_chestplate").build());
    public static final RegistryObject<ArmorMetaItem> QUANTUM_LEGGINGS = ITEM_REGISTRY.register("quantum_leggings", () -> ArmorMetaItem.builder("quantum_leggings").build());
    public static final RegistryObject<ArmorMetaItem> QUANTUM_BOOTS = ITEM_REGISTRY.register("quantum_boots", () -> ArmorMetaItem.builder("quantum_boots").build());
    public static final RegistryObject<ArmorMetaItem> QUANTUM_HELMET = ITEM_REGISTRY.register("quantum_helmet", () -> ArmorMetaItem.builder("quantum_helmet").build());

    public static final RegistryObject<ArmorMetaItem> SEMIFLUID_JETPACK = ITEM_REGISTRY.register("semifluid_jetpack", () -> ArmorMetaItem.builder("semifluid_jetpack").build());
    public static final RegistryObject<ArmorMetaItem> ELECTRIC_JETPACK = ITEM_REGISTRY.register("electric_jetpack", () -> ArmorMetaItem.builder("electric_jetpack").build());

    public static final RegistryObject<ArmorMetaItem> ELECTRIC_JETPACK_ADVANCED = ITEM_REGISTRY.register("electric_jetpack_advanced", () -> ArmorMetaItem.builder("electric_jetpack_advanced").build());
    public static final RegistryObject<ArmorMetaItem> NANO_CHESTPLATE_ADVANCED = ITEM_REGISTRY.register("nano_chestplate_advanced", () -> ArmorMetaItem.builder("nano_chestplate_advanced").build());
    public static final RegistryObject<ArmorMetaItem> QUANTUM_CHESTPLATE_ADVANCED = ITEM_REGISTRY.register("quantum_chestplate_advanced", () -> ArmorMetaItem.builder("quantum_chestplate_advanced").build());

    public static final RegistryObject<MetaItem> POWER_THRUSTER = ITEM_REGISTRY.register("power_thruster", () -> MetaItem.builder("power_thruster").build());
    public static final RegistryObject<MetaItem> POWER_THRUSTER_ADVANCED = ITEM_REGISTRY.register("power_thruster_advanced", () -> MetaItem.builder("power_thruster.advanced").build());
    public static final RegistryObject<MetaItem> GRAVITATION_ENGINE = ITEM_REGISTRY.register("gravitation_engine", () -> MetaItem.builder("gravitation_engine").build());

    public static final RegistryObject<MetaItem> SUS_RECORD = ITEM_REGISTRY.register("sus_record", () -> MetaItem.builder("sus_record").build());
    public static final RegistryObject<MetaItem> NAN_CERTIFICATE = ITEM_REGISTRY.register("nan_certificate", () -> MetaItem.builder("nan_certificate").build());

    public static final RegistryObject<MetaItem> FERTILIZER = ITEM_REGISTRY.register("fertilizer", () -> MetaItem.builder("fertilizer").addComponents(new FertilizerBehaviour()).build());
    public static final RegistryObject<MetaItem> BLACKLIGHT = ITEM_REGISTRY.register("blacklight", () -> MetaItem.builder("blacklight").build());


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

    public static void registerColors(RegisterColorHandlersEvent.Item event) {
        for (MetaItem item : META_ITEMS) {
            item.registerItemColor(event);
        }
    }

    @SubscribeEvent
    public static void registerBakedModels(ModelEvent.BakingCompleted event) {
        GregTech.LOGGER.info("Registering special item models");
        registerSpecialItemModel(event, COVER_FACADE, new FacadeRenderer());
    }

    private static void registerSpecialItemModel(ModelEvent.BakingCompleted event, MetaItem metaValueItem, BakedModel bakedModel) {
        ResourceLocation modelPath = MetaItem.createItemModelPath(metaValueItem, "");
        event.getModels().put(modelPath, bakedModel);
    }

    @SuppressWarnings("unused")
    public static void addTagPrefix(TagPrefix... prefixes) {
        TagPrefixes.addAll(Arrays.asList(prefixes));
    }
}
