package net.nemezanevem.gregtech.api.registry.cover;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.cover.CoverDefinition;

import java.util.function.Supplier;

public class CoverRegistry {

    public static final DeferredRegister<CoverDefinition> COVERS = DeferredRegister.create(new ResourceLocation(GregTech.MODID, "cover"), GregTech.MODID);

    public static Supplier<IForgeRegistry<CoverDefinition>> COVERS_BUILTIN = COVERS.makeRegistry(() -> new RegistryBuilder<CoverDefinition>().setDefaultKey(new ResourceLocation(GregTech.MODID, "dummy")));

    public static void init() {}
}
