package net.nemezanevem.gregtech.api.registry.material.info;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.unification.material.properties.info.MaterialIconSet;
import net.nemezanevem.gregtech.api.unification.material.properties.info.MaterialIconType;

import java.util.function.Supplier;

public class MaterialIconTypeRegistry {

    public static final DeferredRegister<MaterialIconType> MATERIAL_ICON_TYPES = DeferredRegister.create(new ResourceLocation(GregTech.MODID, "material_icon_type"), GregTech.MODID);

    public static Supplier<IForgeRegistry<MaterialIconType>> MATERIAL_ICON_TYPES_BUILTIN = MATERIAL_ICON_TYPES.makeRegistry(() -> new RegistryBuilder<MaterialIconType>().setDefaultKey(new ResourceLocation(GregTech.MODID, "dust")));

    public static void init() {}
}
