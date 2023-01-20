package net.nemezanevem.gregtech.api.registry.material.properties;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.unification.material.properties.IMaterialProperty;
import net.nemezanevem.gregtech.api.unification.material.properties.PropertyKey;

import java.util.function.Supplier;

public class MaterialPropertyRegistry {

    public static final DeferredRegister<PropertyKey<?>> MATERIAL_PROPERTIES = DeferredRegister.create(new ResourceLocation(GregTech.MODID, "material_property"), GregTech.MODID);

    public static Supplier<IForgeRegistry<PropertyKey<?>>> MATERIAL_PROPERTIES_BUILTIN = MATERIAL_PROPERTIES.makeRegistry(() -> new RegistryBuilder<PropertyKey<?>>().setDefaultKey(new ResourceLocation(GregTech.MODID, "dust")));

    public static void init() {}
}
