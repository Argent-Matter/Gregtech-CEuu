package net.nemezanevem.gregtech.api.unification.material.properties;

import net.minecraftforge.registries.RegistryObject;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.*;

import static net.nemezanevem.gregtech.api.registry.material.properties.MaterialPropertyRegistry.MATERIAL_PROPERTIES;

public class GtMaterialProperties {

    public static final RegistryObject<PropertyKey<EmptyProperty>> EMPTY = MATERIAL_PROPERTIES.register("empty", () -> new PropertyKey<>(EmptyProperty.class));

    public static final RegistryObject<PropertyKey<BlastProperty>> BLAST = MATERIAL_PROPERTIES.register("blast", () -> new PropertyKey<>(BlastProperty.class));
    public static final RegistryObject<PropertyKey<DustProperty>> DUST = MATERIAL_PROPERTIES.register("dust", () -> new PropertyKey<>(DustProperty.class));
    public static final RegistryObject<PropertyKey<FluidProperty>> FLUID = MATERIAL_PROPERTIES.register("fluid", () -> new PropertyKey<>(FluidProperty.class));
    public static final RegistryObject<PropertyKey<GemProperty>> GEM = MATERIAL_PROPERTIES.register("gem", () -> new PropertyKey<>(GemProperty.class));
    public static final RegistryObject<PropertyKey<IngotProperty>> INGOT = MATERIAL_PROPERTIES.register("ingot", () -> new PropertyKey<>(IngotProperty.class));
    public static final RegistryObject<PropertyKey<OreProperty>> ORE = MATERIAL_PROPERTIES.register("ore", () -> new PropertyKey<>(OreProperty.class));
    public static final RegistryObject<PropertyKey<PlasmaProperty>> PLASMA = MATERIAL_PROPERTIES.register("plasma", () -> new PropertyKey<>(PlasmaProperty.class));
    public static final RegistryObject<PropertyKey<ToolProperty>> TOOL = MATERIAL_PROPERTIES.register("tool", () -> new PropertyKey<>(ToolProperty.class));
    public static final RegistryObject<PropertyKey<PolymerProperty>> POLYMER = MATERIAL_PROPERTIES.register("polymer", () -> new PropertyKey<>(PolymerProperty.class));
    public static final RegistryObject<PropertyKey<RotorProperty>> ROTOR = MATERIAL_PROPERTIES.register("rotor", () -> new PropertyKey<>(RotorProperty.class));
    public static final RegistryObject<PropertyKey<WireProperty>> WIRE = MATERIAL_PROPERTIES.register("wire", () -> new PropertyKey<>(WireProperty.class));
    public static final RegistryObject<PropertyKey<FluidPipeProperty>> FLUID_PIPE = MATERIAL_PROPERTIES.register("fluid_pipe", () -> new PropertyKey<>(FluidPipeProperty.class));
    public static final RegistryObject<PropertyKey<ItemPipeProperty>> ITEM_PIPE = MATERIAL_PROPERTIES.register("item_pipe", () -> new PropertyKey<>(ItemPipeProperty.class));
}
