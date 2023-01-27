package net.nemezanevem.gregtech.api.recipe;


import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.nemezanevem.gregtech.GregTech;

public class GtRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, GregTech.MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, GregTech.MODID);

    public static final RegistryObject<RecipeSerializer<GTRecipe>> SIMPLE_SERIALIZER = RECIPE_SERIALIZERS.register("simple_serializer", GTRecipe.Serializer::new);

    public void registerRecipeTypes(IEventBus bus) {
        RECIPE_TYPES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
        for (var machineId : MachineRegistry.MACHINES_BUILTIN.get().getKeys()) {
            RECIPE_TYPES.register(machineId.path, () -> new RecipeType<GTRecipe>() {
                @Override
                public String toString() {
                    return machineId.path;
                }
            });
        }
    }
}
