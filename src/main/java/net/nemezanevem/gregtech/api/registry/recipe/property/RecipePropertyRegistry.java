package net.nemezanevem.gregtech.api.registry.recipe.property;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.recipe.property.RecipeProperty;
import net.nemezanevem.gregtech.api.tileentity.MetaTileEntity;

import java.util.function.Supplier;

public class RecipePropertyRegistry {
    public static final DeferredRegister<RecipeProperty<?>> RECIPE_PROPERTIES = DeferredRegister.create(new ResourceLocation(GregTech.MODID, "recipe_property"), GregTech.MODID);

    public static Supplier<IForgeRegistry<RecipeProperty<?>>> RECIPE_PROPERTIES_BUILTIN = RECIPE_PROPERTIES.makeRegistry(() -> new RegistryBuilder<RecipeProperty<?>>().setDefaultKey(new ResourceLocation(GregTech.MODID, "null")));

    public static void init() {}
}
