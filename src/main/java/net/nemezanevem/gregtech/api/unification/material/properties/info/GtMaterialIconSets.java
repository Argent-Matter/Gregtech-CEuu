package net.nemezanevem.gregtech.api.unification.material.properties.info;

import net.minecraftforge.registries.RegistryObject;

import static net.nemezanevem.gregtech.api.registry.material.info.MaterialIconSetRegistry.MATERIAL_ICON_SETS;

public class GtMaterialIconSets {

    public static final RegistryObject<MaterialIconSet> DULL = MATERIAL_ICON_SETS.register("dull", () -> new MaterialIconSet("dull", null, true));
    public static final RegistryObject<MaterialIconSet> METALLIC = MATERIAL_ICON_SETS.register("metallic", () -> new MaterialIconSet("metallic"));
    public static final RegistryObject<MaterialIconSet> MAGNETIC = MATERIAL_ICON_SETS.register("magnetic", () -> new MaterialIconSet("magnetic", METALLIC.get()));
    public static final RegistryObject<MaterialIconSet> SHINY = MATERIAL_ICON_SETS.register("shiny", () -> new MaterialIconSet("shiny", METALLIC.get()));
    public static final RegistryObject<MaterialIconSet> BRIGHT = MATERIAL_ICON_SETS.register("bright", () -> new MaterialIconSet("bright", SHINY.get()));
    public static final RegistryObject<MaterialIconSet> DIAMOND = MATERIAL_ICON_SETS.register("diamond", () -> new MaterialIconSet("diamond", SHINY.get()));
    public static final RegistryObject<MaterialIconSet> EMERALD = MATERIAL_ICON_SETS.register("emerald", () -> new MaterialIconSet("emerald", DIAMOND.get()));
    public static final RegistryObject<MaterialIconSet> GEM_HORIZONTAL = MATERIAL_ICON_SETS.register("gem_horizontal", () -> new MaterialIconSet("gem_horizontal", EMERALD.get()));
    public static final RegistryObject<MaterialIconSet> GEM_VERTICAL = MATERIAL_ICON_SETS.register("gem_vertical", () -> new MaterialIconSet("gem_vertical", EMERALD.get()));
    public static final RegistryObject<MaterialIconSet> RUBY = MATERIAL_ICON_SETS.register("ruby", () -> new MaterialIconSet("ruby", EMERALD.get()));
    public static final RegistryObject<MaterialIconSet> OPAL = MATERIAL_ICON_SETS.register("opal", () -> new MaterialIconSet("opal", RUBY.get()));
    public static final RegistryObject<MaterialIconSet> GLASS = MATERIAL_ICON_SETS.register("glass", () -> new MaterialIconSet("glass", RUBY.get()));
    public static final RegistryObject<MaterialIconSet> NETHERSTAR = MATERIAL_ICON_SETS.register("netherstar", () -> new MaterialIconSet("netherstar", GLASS.get()));
    public static final RegistryObject<MaterialIconSet> FINE = MATERIAL_ICON_SETS.register("fine", () -> new MaterialIconSet("fine"));
    public static final RegistryObject<MaterialIconSet> SAND = MATERIAL_ICON_SETS.register("sand", () -> new MaterialIconSet("sand", FINE.get()));
    public static final RegistryObject<MaterialIconSet> WOOD = MATERIAL_ICON_SETS.register("wood", () -> new MaterialIconSet("wood", FINE.get()));
    public static final RegistryObject<MaterialIconSet> ROUGH = MATERIAL_ICON_SETS.register("rough", () -> new MaterialIconSet("rough", FINE.get()));
    public static final RegistryObject<MaterialIconSet> FLINT = MATERIAL_ICON_SETS.register("flint", () -> new MaterialIconSet("flint", ROUGH.get()));
    public static final RegistryObject<MaterialIconSet> LIGNITE = MATERIAL_ICON_SETS.register("lignite", () -> new MaterialIconSet("lignite", ROUGH.get()));
    public static final RegistryObject<MaterialIconSet> QUARTZ = MATERIAL_ICON_SETS.register("quartz", () -> new MaterialIconSet("quartz", ROUGH.get()));
    public static final RegistryObject<MaterialIconSet> CERTUS = MATERIAL_ICON_SETS.register("certus", () -> new MaterialIconSet("certus", QUARTZ.get()));
    public static final RegistryObject<MaterialIconSet> LAPIS = MATERIAL_ICON_SETS.register("lapis", () -> new MaterialIconSet("lapis", QUARTZ.get()));
    public static final RegistryObject<MaterialIconSet> FLUID = MATERIAL_ICON_SETS.register("fluid", () -> new MaterialIconSet("fluid"));
    public static final RegistryObject<MaterialIconSet> GAS = MATERIAL_ICON_SETS.register("gas", () -> new MaterialIconSet("gas"));

    public static void init() {}
}
