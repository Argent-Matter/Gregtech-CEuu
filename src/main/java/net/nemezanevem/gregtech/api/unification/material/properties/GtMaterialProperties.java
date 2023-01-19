package net.nemezanevem.gregtech.api.unification.material.properties;

import net.minecraftforge.registries.RegistryObject;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.*;

import static net.nemezanevem.gregtech.api.registry.material.properties.MaterialPropertyRegistry.MATERIAL_PROPERTIES;

public class GtMaterialProperties {

    public static final RegistryObject<IMaterialProperty<EmptyProperty>> EMPTY = MATERIAL_PROPERTIES.register("empty", EmptyProperty::new);

    public static final RegistryObject<IMaterialProperty<BlastProperty>> BLAST = MATERIAL_PROPERTIES.register("blast", BlastProperty::new);
    public static final RegistryObject<IMaterialProperty<DustProperty>> DUST = MATERIAL_PROPERTIES.register("dust", DustProperty::new);
    public static final RegistryObject<IMaterialProperty<FluidProperty>> FLUID = MATERIAL_PROPERTIES.register("fluid", FluidProperty::new);
    public static final RegistryObject<IMaterialProperty<GemProperty>> GEM = MATERIAL_PROPERTIES.register("gem", GemProperty::new);
    public static final RegistryObject<IMaterialProperty<IngotProperty>> INGOT = MATERIAL_PROPERTIES.register("ingot", IngotProperty::new);
    public static final RegistryObject<IMaterialProperty<OreProperty>> ORE = MATERIAL_PROPERTIES.register("ore", OreProperty::new);
    public static final RegistryObject<IMaterialProperty<PlasmaProperty>> PLASMA = MATERIAL_PROPERTIES.register("plasma", PlasmaProperty::new);
    public static final RegistryObject<IMaterialProperty<PlasmaProperty>> TOOL = MATERIAL_PROPERTIES.register("tool", PlasmaProperty::new);
    public static final RegistryObject<IMaterialProperty<PolymerProperty>> POLYMER = MATERIAL_PROPERTIES.register("polymer", PolymerProperty::new);
    public static final RegistryObject<IMaterialProperty<RotorProperty>> ROTOR = MATERIAL_PROPERTIES.register("rotor", () -> new RotorProperty(0, 0, 0));
}
