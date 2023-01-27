package net.nemezanevem.gregtech.api.recipe;


import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.registry.tileentity.MetaTileEntityRegistry;

import java.util.*;

public class GtRecipeTypes {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, GregTech.MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, GregTech.MODID);

    public static final RegistryObject<RecipeSerializer<GTRecipe>> SIMPLE_SERIALIZER = RECIPE_SERIALIZERS.register("simple_recipe_serializer", GTRecipe.Serializer::new);

    public static final Map<ResourceLocation, GTRecipeType<?>> toRegister = new HashMap<>(20, 0.5f);

    public void registerRecipeTypes(IEventBus bus) {
        RECIPE_TYPES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
        for (var machineId : MetaTileEntityRegistry.META_TILE_ENTITIES_BUILTIN.get().getEntries()) {
            RECIPE_TYPES.register(machineId.getKey().location().getPath(), () -> new GTRecipeType<GTRecipe>() {
                @Override
                public IChanceFunction getChanceFunction() {
                    return machineId.getValue().getHolder().getMetaTileEntity().getRecipeType().getChanceFunction();
                }

                @Override
                public String toString() {
                    return machineId.getKey().location().getPath();
                }
            });
        }
    }
}
