package net.nemezanevem.gregtech.api.registry.material;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.properties.IMaterialProperty;

import java.util.function.Supplier;

public class MaterialRegistry {

    public static final DeferredRegister<Material> MATERIALS = DeferredRegister.create(new ResourceLocation(GregTech.MODID, "material"), GregTech.MODID);

    public static Supplier<IForgeRegistry<Material>> MATERIALS_BUILTIN = MATERIALS.makeRegistry(() -> new RegistryBuilder<Material>().setDefaultKey(new ResourceLocation(GregTech.MODID, "empty")));

    public static void init() {}
}
