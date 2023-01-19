package net.nemezanevem.gregtech.api.recipe;


import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.nemezanevem.gregtech.GregTech;

public class ModRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, GregTech.MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, GregTech.MODID);


    public void registerRecipeTypes(IEventBus bus) {
        RECIPE_TYPES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
        for (Machine machine : MachineRegistry.MACHINES_BUILTIN.get()) {
            RECIPE_TYPES.register(machine.name, () -> new SimpleMachineRecipe(machine));
            RECIPE_SERIALIZERS.register(machine.name, () -> new SimpleMachineRecipeSerializer(machine));
        }
    }


    public void registerRecipeTypes(IEventBus bus) {
        RECIPE_TYPES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
        for (Machine machine : MachineRegistry.MACHINES_BUILTIN.get()) {
            RECIPE_TYPES.register(machine.name, () -> new EBFRecipe(machine));
            RECIPE_SERIALIZERS.register(machine.name, () -> new EBFRecipeSerializer(machine));


        }
    }

    public void registerRecipeTypes(IEventBus bus) {
        RECIPE_TYPES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
        for (Machine machine : MachineRegistry.MACHINES_BUILTIN.get()) {
            RECIPE_TYPES.register(machine.name, () -> new BBFRecipe(machine));
            RECIPE_SERIALIZERS.register(machine.name, () -> new BBFRecipeSerializer(machine));
        }
    }

    public void registerRecipeTypes(IEventBus bus) {
        RECIPE_TYPES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
        for (Machine machine : MachineRegistry.MACHINES_BUILTIN.get()) {
            RECIPE_TYPES.register(machine.name, () -> new FreezingRecipe(machine));
            RECIPE_SERIALIZERS.register(machine.name, () -> new FreezingRecipeSerializer(machine));
        }
    }

    public void registerRecipeTypes(IEventBus bus) {
        RECIPE_TYPES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
        for (Machine machine : MachineRegistry.MACHINES_BUILTIN.get()) {
            RECIPE_TYPES.register(machine.name, () -> new PyroRecipe(machine));
            RECIPE_SERIALIZERS.register(machine.name, () -> new PyroRecipeSerializer(machine));
        }
    }

    public void registerRecipeTypes(IEventBus bus) {
        RECIPE_TYPES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
        for (Machine machine : MachineRegistry.MACHINES_BUILTIN.get()) {
            RECIPE_TYPES.register(machine.name, () -> new LCRRecipe(machine));
            RECIPE_SERIALIZERS.register(machine.name, () -> new LCRRecipeSerializer(machine));
        }
    }

    public void registerRecipeTypes(IEventBus bus) {
        RECIPE_TYPES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
        for (Machine machine : MachineRegistry.MACHINES_BUILTIN.get()) {
            RECIPE_TYPES.register(machine.name, () -> new DistillingRecipe(machine));
            RECIPE_SERIALIZERS.register(machine.name, () -> new DistillingRecipeSerializer(machine));
        }
    }

    public void registerRecipeTypes(IEventBus bus) {
        RECIPE_TYPES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
        for (Machine machine : MachineRegistry.MACHINES_BUILTIN.get()) {
            RECIPE_TYPES.register(machine.name, () -> new Implosion_CompressorRecipe(machine));
            RECIPE_SERIALIZERS.register(machine.name, () -> new Implosion_CompressorRecipeSerializer(machine));
        }
    }

    public void registerRecipeTypes(IEventBus bus) {
        RECIPE_TYPES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
        for (Machine machine : MachineRegistry.MACHINES_BUILTIN.get()) {
            RECIPE_TYPES.register(machine.name, () -> new Oil_Cracking_UnitRecipe(machine));
            RECIPE_SERIALIZERS.register(machine.name, () -> new Oil_Cracking_UnitRecipeSerializer(machine));
        }
    }

    public void registerRecipeTypes(IEventBus bus) {
        RECIPE_TYPES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
        for (Machine machine : MachineRegistry.MACHINES_BUILTIN.get()) {
            RECIPE_TYPES.register(machine.name, () -> new Coke_OvenRecipe(machine));
            RECIPE_SERIALIZERS.register(machine.name, () -> new Coke_OvenRecipeSerializer(machine));
        }
    }
}
