package net.nemezanevem.gregtech.api.registry.material.properties;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.unification.material.properties.IMaterialProperty;

import java.util.function.Supplier;

public class MaterialPropertyRegistry {

    public static final DeferredRegister<IMaterialProperty<?>> MATERIAL_PROPERTIES = DeferredRegister.create(new ResourceLocation(GregTech.MODID, "material_property"), GregTech.MODID);

    public static Supplier<IForgeRegistry<IMaterialProperty<?>>> MATERIAL_PROPERTIES_BUILTIN = MATERIAL_PROPERTIES.makeRegistry(() -> new RegistryBuilder<IMaterialProperty<?>>().setDefaultKey(new ResourceLocation(GregTech.MODID, "dust")));

    public static void init() {}
}
