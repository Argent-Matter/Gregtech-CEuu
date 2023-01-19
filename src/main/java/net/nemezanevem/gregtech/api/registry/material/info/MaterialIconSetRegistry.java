package net.nemezanevem.gregtech.api.registry.material.info;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.unification.material.properties.info.MaterialIconSet;

import java.util.function.Supplier;

public class MaterialIconSetRegistry {

    public static final DeferredRegister<MaterialIconSet> MATERIAL_ICONS = DeferredRegister.create(new ResourceLocation(GregTech.MODID, "material_icon"), GregTech.MODID);

    public static Supplier<IForgeRegistry<MaterialIconSet>> MATERIAL_ICONS_BUILTIN = MATERIAL_ICONS.makeRegistry(() -> new RegistryBuilder<MaterialIconSet>().setDefaultKey(new ResourceLocation(GregTech.MODID, "dull")));

    public static void init() {}
}
