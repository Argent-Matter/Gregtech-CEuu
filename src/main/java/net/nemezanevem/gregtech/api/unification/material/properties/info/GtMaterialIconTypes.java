package net.nemezanevem.gregtech.api.unification.material.properties.info;

import net.minecraftforge.registries.RegistryObject;

import static net.nemezanevem.gregtech.api.registry.material.info.MaterialIconTypeRegistry.MATERIAL_ICON_TYPES;

public class GtMaterialIconTypes {

    public static final RegistryObject<MaterialIconType> dustTiny = MATERIAL_ICON_TYPES.register("dust_tiny", () -> new MaterialIconType("dust_tiny"));
    public static final RegistryObject<MaterialIconType> dustSmall = MATERIAL_ICON_TYPES.register("dust_small", () -> new MaterialIconType("dust_small"));
    public static final RegistryObject<MaterialIconType> dust = MATERIAL_ICON_TYPES.register("dust",  () -> new MaterialIconType("dust"));
    public static final RegistryObject<MaterialIconType> dustImpure = MATERIAL_ICON_TYPES.register("dust_impure", () -> new MaterialIconType("dust_impure"));
    public static final RegistryObject<MaterialIconType> dustPure = MATERIAL_ICON_TYPES.register("dust_pure", () -> new MaterialIconType("dust_pure"));

    public static final RegistryObject<MaterialIconType> crushed = MATERIAL_ICON_TYPES.register("crushed", () -> new MaterialIconType("crushed"));
    public static final RegistryObject<MaterialIconType> crushedPurified = MATERIAL_ICON_TYPES.register("crushed_purified", () -> new MaterialIconType("crushed_purified"));
    public static final RegistryObject<MaterialIconType> crushedCentrifuged = MATERIAL_ICON_TYPES.register("crushed_centrifuged", () -> new MaterialIconType("crushed_centrifuged"));

    public static final RegistryObject<MaterialIconType> gem = MATERIAL_ICON_TYPES.register("gem", () -> new MaterialIconType("gem"));
    public static final RegistryObject<MaterialIconType> gemChipped = MATERIAL_ICON_TYPES.register("gem_chipped", () -> new MaterialIconType("gem-chipped"));
    public static final RegistryObject<MaterialIconType> gemFlawed = MATERIAL_ICON_TYPES.register("gem_flawed", () -> new MaterialIconType("gem_flawed"));
    public static final RegistryObject<MaterialIconType> gemFlawless = MATERIAL_ICON_TYPES.register("gem_flawless", () -> new MaterialIconType("gem_flawless"));
    public static final RegistryObject<MaterialIconType> gemExquisite = MATERIAL_ICON_TYPES.register("gem_exquisite", () -> new MaterialIconType("gem_exquisite"));

    public static final RegistryObject<MaterialIconType> nugget = MATERIAL_ICON_TYPES.register("nugget", () -> new MaterialIconType("nugget"));

    public static final RegistryObject<MaterialIconType> ingot = MATERIAL_ICON_TYPES.register("ingot", () -> new MaterialIconType("ingot"));
    public static final RegistryObject<MaterialIconType> ingotHot = MATERIAL_ICON_TYPES.register("ingot_hot", () -> new MaterialIconType("ingot_hot"));
    public static final RegistryObject<MaterialIconType> ingotDouble = MATERIAL_ICON_TYPES.register("ingot_double", () -> new MaterialIconType("ingot_double"));
    public static final RegistryObject<MaterialIconType> ingotTriple = MATERIAL_ICON_TYPES.register("ingot_triple", () -> new MaterialIconType("ingot_triple"));
    public static final RegistryObject<MaterialIconType> ingotQuadruple = MATERIAL_ICON_TYPES.register("ingot_quadruple", () -> new MaterialIconType("ingot_quadruple"));
    public static final RegistryObject<MaterialIconType> ingotQuintuple = MATERIAL_ICON_TYPES.register("ingot_quintuple", () -> new MaterialIconType("ingot_quintuple"));

    public static final RegistryObject<MaterialIconType> plate = MATERIAL_ICON_TYPES.register("plate", () -> new MaterialIconType("plate"));
    public static final RegistryObject<MaterialIconType> plateDouble = MATERIAL_ICON_TYPES.register("plate_double", () -> new MaterialIconType("plate_double"));
    public static final RegistryObject<MaterialIconType> plateTriple = MATERIAL_ICON_TYPES.register("plate_triple", () -> new MaterialIconType("plate_triple"));
    public static final RegistryObject<MaterialIconType> plateQuadruple = MATERIAL_ICON_TYPES.register("plate_quadruple", () -> new MaterialIconType("plate_quadruple"));
    public static final RegistryObject<MaterialIconType> plateQuintuple = MATERIAL_ICON_TYPES.register("plate_quintuple", () -> new MaterialIconType("plate_quintuple"));
    public static final RegistryObject<MaterialIconType> plateDense = MATERIAL_ICON_TYPES.register("plate_dense", () -> new MaterialIconType("plate_dense"));

    public static final RegistryObject<MaterialIconType> rod = MATERIAL_ICON_TYPES.register("stick", () -> new MaterialIconType("stick"));
    public static final RegistryObject<MaterialIconType> lens = MATERIAL_ICON_TYPES.register("lens", () -> new MaterialIconType("lens"));
    public static final RegistryObject<MaterialIconType> round = MATERIAL_ICON_TYPES.register("round", () -> new MaterialIconType("round"));
    public static final RegistryObject<MaterialIconType> bolt = MATERIAL_ICON_TYPES.register("bolt", () -> new MaterialIconType("bolt"));
    public static final RegistryObject<MaterialIconType> screw = MATERIAL_ICON_TYPES.register("screw", () -> new MaterialIconType("screw"));
    public static final RegistryObject<MaterialIconType> ring = MATERIAL_ICON_TYPES.register("ring", () -> new MaterialIconType("ring"));
    public static final RegistryObject<MaterialIconType> wireFine = MATERIAL_ICON_TYPES.register("wire_fine", () -> new MaterialIconType("wire_fine"));
    public static final RegistryObject<MaterialIconType> gearSmall = MATERIAL_ICON_TYPES.register("gear_small", () -> new MaterialIconType("gear_small"));
    public static final RegistryObject<MaterialIconType> rotor = MATERIAL_ICON_TYPES.register("rotor", () -> new MaterialIconType("rotor"));
    public static final RegistryObject<MaterialIconType> rodLong = MATERIAL_ICON_TYPES.register("stick_long", () -> new MaterialIconType("stick_long"));
    public static final RegistryObject<MaterialIconType> springSmall = MATERIAL_ICON_TYPES.register("spring_small", () -> new MaterialIconType("spring_small"));
    public static final RegistryObject<MaterialIconType> spring = MATERIAL_ICON_TYPES.register("spring", () -> new MaterialIconType("spring"));
    public static final RegistryObject<MaterialIconType> gear = MATERIAL_ICON_TYPES.register("gear", () -> new MaterialIconType("gear"));
    public static final RegistryObject<MaterialIconType> foil = MATERIAL_ICON_TYPES.register("foil", () -> new MaterialIconType("foil"));

    public static final RegistryObject<MaterialIconType> toolHeadSword = MATERIAL_ICON_TYPES.register("tool_head_sword", () -> new MaterialIconType("tool_head_sword"));
    public static final RegistryObject<MaterialIconType> toolHeadPickaxe = MATERIAL_ICON_TYPES.register("tool_head_pickaxe", () -> new MaterialIconType("tool_head_pickaxe"));
    public static final RegistryObject<MaterialIconType> toolHeadShovel = MATERIAL_ICON_TYPES.register("tool_head_shovel", () -> new MaterialIconType("tool_head_shovel"));
    public static final RegistryObject<MaterialIconType> toolHeadAxe = MATERIAL_ICON_TYPES.register("tool_head_axe", () -> new MaterialIconType("tool_head_axe"));
    public static final RegistryObject<MaterialIconType> toolHeadHoe = MATERIAL_ICON_TYPES.register("tool_head_hoe", () -> new MaterialIconType("tool_head_hoe"));
    public static final RegistryObject<MaterialIconType> toolHeadHammer = MATERIAL_ICON_TYPES.register("tool_head_hammer", () -> new MaterialIconType("tool_head_hammer"));
    public static final RegistryObject<MaterialIconType> toolHeadFile = MATERIAL_ICON_TYPES.register("tool_head_file", () -> new MaterialIconType("tool_head_file"));
    public static final RegistryObject<MaterialIconType> toolHeadSaw = MATERIAL_ICON_TYPES.register("tool_head_saw", () -> new MaterialIconType("tool_head_saw"));
    public static final RegistryObject<MaterialIconType> toolHeadBuzzSaw = MATERIAL_ICON_TYPES.register("tool_head_buzzsaw", () -> new MaterialIconType("tool_head_buzzsaw"));
    public static final RegistryObject<MaterialIconType> toolHeadDrill = MATERIAL_ICON_TYPES.register("tool_head_drill", () -> new MaterialIconType("tool_head_drill"));
    public static final RegistryObject<MaterialIconType> toolHeadChainsaw = MATERIAL_ICON_TYPES.register("tool_head_chainsaw", () -> new MaterialIconType("tool_head_chainsaw"));
    public static final RegistryObject<MaterialIconType> toolHeadScythe = MATERIAL_ICON_TYPES.register("tool_head_scythe", () -> new MaterialIconType("tool_head_scythe"));
    public static final RegistryObject<MaterialIconType> toolHeadScrewdriver = MATERIAL_ICON_TYPES.register("tool_head_screwdriver", () -> new MaterialIconType("tool_head_screwdriver"));
    public static final RegistryObject<MaterialIconType> toolHeadWrench = MATERIAL_ICON_TYPES.register("tool_head_wrench", () -> new MaterialIconType("tool_head_wrench"));

    public static final RegistryObject<MaterialIconType> turbineBlade = MATERIAL_ICON_TYPES.register("turbine_blade", () -> new MaterialIconType("turbine_blade"));

    // BLOCK TEXTURES
    public static final RegistryObject<MaterialIconType> block = MATERIAL_ICON_TYPES.register("block", () -> new MaterialIconType("block"));
    public static final RegistryObject<MaterialIconType> fluid = MATERIAL_ICON_TYPES.register("fluid", () -> new MaterialIconType("fluid"));
    public static final RegistryObject<MaterialIconType> ore = MATERIAL_ICON_TYPES.register("ore", () -> new MaterialIconType("ore"));
    public static final RegistryObject<MaterialIconType> oreSmall = MATERIAL_ICON_TYPES.register("ore_small", () -> new MaterialIconType("ore_small"));
    public static final RegistryObject<MaterialIconType> frame = MATERIAL_ICON_TYPES.register("frame", () -> new MaterialIconType("frame"));

    // USED FOR GREGIFICATION ADDON
    public static final RegistryObject<MaterialIconType> seed = MATERIAL_ICON_TYPES.register("seed", () -> new MaterialIconType("seed"));
    public static final RegistryObject<MaterialIconType> crop = MATERIAL_ICON_TYPES.register("crop", () -> new MaterialIconType("crop"));
    public static final RegistryObject<MaterialIconType> essence = MATERIAL_ICON_TYPES.register("essence", () -> new MaterialIconType("essence"));

    public static void init() {}
}
