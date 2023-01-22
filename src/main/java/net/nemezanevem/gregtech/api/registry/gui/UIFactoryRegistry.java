package net.nemezanevem.gregtech.api.registry.gui;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.gui.UIFactory;

import java.util.function.Supplier;

public class UIFactoryRegistry {

    public static final DeferredRegister<UIFactory<?>> UI_FACTORIES = DeferredRegister.create(new ResourceLocation(GregTech.MODID, "ui_factory"), GregTech.MODID);

    public static Supplier<IForgeRegistry<UIFactory<?>>> UI_FACTORIES_BUILTIN = UI_FACTORIES.makeRegistry(() -> new RegistryBuilder<UIFactory<?>>().setDefaultKey(new ResourceLocation(GregTech.MODID, "ass")));

    public static void init() {}
}
