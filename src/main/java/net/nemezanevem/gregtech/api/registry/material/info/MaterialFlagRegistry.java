package net.nemezanevem.gregtech.api.registry.material.info;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.unification.material.properties.info.MaterialFlag;

import java.util.function.Supplier;

public class MaterialFlagRegistry {

    public static final DeferredRegister<MaterialFlag> MATERIAL_FLAGS = DeferredRegister.create(new ResourceLocation(GregTech.MODID, "material_flag"), GregTech.MODID);

    public static Supplier<IForgeRegistry<MaterialFlag>> MATERIAL_FLAGS_BUILTIN = MATERIAL_FLAGS.makeRegistry(() -> new RegistryBuilder<MaterialFlag>().setDefaultKey(new ResourceLocation(GregTech.MODID, "no_unification")));

    public static void init() {}
}
