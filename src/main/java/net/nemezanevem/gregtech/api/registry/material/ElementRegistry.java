package net.nemezanevem.gregtech.api.registry.material;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.unification.material.Element;
import net.nemezanevem.gregtech.api.unification.material.Material;

import java.util.function.Supplier;

public class ElementRegistry {

    public static final DeferredRegister<Element> ELEMENTS = DeferredRegister.create(new ResourceLocation(GregTech.MODID, "element"), GregTech.MODID);

    public static Supplier<IForgeRegistry<Element>> ELEMENTS_BUILTIN = ELEMENTS.makeRegistry(() -> new RegistryBuilder<Element>().setDefaultKey(new ResourceLocation(GregTech.MODID, "netronium")));

    public static void init() {}
}
