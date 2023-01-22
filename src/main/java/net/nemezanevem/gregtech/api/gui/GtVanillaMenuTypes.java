package net.nemezanevem.gregtech.api.gui;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.gui.impl.ModularUIContainer;

public class GtVanillaMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, GregTech.MODID);

    public static final RegistryObject<MenuType<ModularUIContainer>> GT_MENU = MENU_TYPES.register("modular", () -> new MenuType<>());
}
