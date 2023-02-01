package net.nemezanevem.gregtech.integration.jei.recipe;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.recipe.GTRecipe;
import net.nemezanevem.gregtech.api.recipe.GTRecipe.ChanceEntry;
import net.nemezanevem.gregtech.api.recipe.GTRecipeType;
import net.nemezanevem.gregtech.api.recipe.GtRecipeTypes;
import net.nemezanevem.gregtech.api.recipe.ingredient.ExtendedIngredient;
import net.nemezanevem.gregtech.api.recipe.ingredient.FluidIngredient;
import net.nemezanevem.gregtech.api.recipe.property.PrimitiveProperty;
import net.nemezanevem.gregtech.api.recipe.property.RecipeProperty;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.client.util.TooltipHelper;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class GTRecipeWrapper {

    private static final int LINE_HEIGHT = 10;

    private final GTRecipeType<?> recipeMap;
    private final GTRecipe recipe;

    public GTRecipeWrapper(GTRecipeType<?> recipeMap, GTRecipe recipe) {
        this.recipeMap = recipeMap;
        this.recipe = recipe;
    }

    public GTRecipe getRecipe() {
        return recipe;
    }

    public Map<IIngredientType<?>, ? extends ExtendedIngredient> getIngredients() {
        Map<IIngredientType<?>, List<? extends ExtendedIngredient>> ingredients = new Object2ObjectOpenHashMap<>();


        // Inputs
        if (!recipe.getInputs().isEmpty()) {
            List<List<ItemStack>> matchingInputs = new ArrayList<>(recipe.getInputs().size());
            for (ExtendedIngredient recipeInput : recipe.getInputs()) {
                matchingInputs.add(Arrays.stream(recipeInput.getItems())
                        .map(ItemStack::copy)
                        .collect(Collectors.toList()));
            }
            ingredients.put(VanillaTypes.ITEM_STACK, recipe.getInputs());
        }

        // Fluid Inputs
        if (!recipe.getFluidInputs().isEmpty()) {
            List<FluidStack> matchingFluidInputs = new ArrayList<>(recipe.getFluidInputs().size());

            for (FluidIngredient fluidInput : recipe.getFluidInputs()) {
                Collections.addAll(matchingFluidInputs, fluidInput.getFluids());
            }
            ingredients.put(ForgeTypes.FLUID_STACK, recipe.getFluidInputs());
        }

        // Outputs
        if (!recipe.getOutputs().isEmpty() || !recipe.getChancedOutputs().isEmpty()) {
            List<ItemStack> recipeOutputs = recipe.getOutputs()
                    .stream().map(ItemStack::copy).collect(Collectors.toList());

            List<ChanceEntry> chancedOutputs = recipe.getChancedOutputs();
            chancedOutputs.sort(Comparator.comparingInt(entry -> entry == null ? 0 : entry.chance()));
            for (ChanceEntry chancedEntry : chancedOutputs) {
                recipeOutputs.add(chancedEntry.getItemStackRaw());
            }
            ingredients.setOutputs(VanillaTypes.ITEM_STACK, recipeOutputs);
        }

        // Fluid Outputs
        if (!recipe.getFluidOutputs().isEmpty()) {
            ingredients.setOutputs(VanillaTypes.FLUID, recipe.getFluidOutputs().stream()
                    .map(FluidStack::copy)
                    .collect(Collectors.toList()));
        }

        return ingredients;
    }

    public void addItemTooltip(int slotIndex, boolean input, Object ingredient, List<Component> tooltip) {
        boolean notConsumed = input && isNotConsumedItem(slotIndex);

        ChanceEntry entry = null;
        int outputIndex = slotIndex - recipeMap.getMaxInputs();
        if (!input && !recipe.getChancedOutputs().isEmpty() && outputIndex >= recipe.getOutputs().size()) {
            entry = recipe.getChancedOutputs().get(outputIndex - recipe.getOutputs().size());
        }

        if (entry != null) {
            double chance = entry.getChance() / 100.0;
            double boost = entry.getBoostPerTier() / 100.0;
            tooltip.add(TooltipHelper.BLINKING_CYAN + Component.translatable("gregtech.recipe.chance", chance, boost));
        } else if (notConsumed) {
            tooltip.add(TooltipHelper.BLINKING_CYAN + Component.translatable("gregtech.recipe.not_consumed"));
        }
    }

    public void addFluidTooltip(int slotIndex, boolean input, Object ingredient, List<Component> tooltip) {
        boolean notConsumed = input && isNotConsumedFluid(slotIndex);

        if (notConsumed) {
            tooltip.add(TooltipHelper.BLINKING_CYAN + Component.translatable("gregtech.recipe.not_consumed"));
        }
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        super.drawInfo(minecraft, recipeWidth, recipeHeight, mouseX, mouseY);
        int yPosition = recipeHeight - getPropertyListHeight();
        if (!recipe.hasProperty(PrimitiveProperty.getInstance())) {
            minecraft.fontRenderer.drawString(Component.translatable("gregtech.recipe.total", Math.abs((long) recipe.getEUt()) * recipe.getDuration()), 0, yPosition, 0x111111);
            minecraft.fontRenderer.drawString(Component.translatable(recipe.getEUt() >= 0 ? "gregtech.recipe.eu" : "gregtech.recipe.eu_inverted", Math.abs(recipe.getEUt()), GTValues.VN[Util.getTierByVoltage(recipe.getEUt())]), 0, yPosition += LINE_HEIGHT, 0x111111);
        } else yPosition -= LINE_HEIGHT * 2;
        minecraft.fontRenderer.drawString(Component.translatable("gregtech.recipe.duration", recipe.getDuration() / 20f), 0, yPosition += LINE_HEIGHT, 0x111111);
        for (Map.Entry<RecipeProperty<?>, Object> propertyEntry : recipe.getPropertyValues()) {
            if (!propertyEntry.getKey().isHidden()) {
                propertyEntry.getKey().drawInfo(minecraft, 0, yPosition += LINE_HEIGHT, 0x111111, propertyEntry.getValue());
            }
        }
    }

    public ChanceEntry getOutputChance(int slot) {
        if (slot >= recipe.getChancedOutputs().size() || slot < 0) return null;
        return recipe.getChancedOutputs().get(slot);
    }

    public boolean isNotConsumedItem(int slot) {
        if (slot >= recipe.getInputs().size()) return false;
        return !recipe.getInputs().get(slot).isConsumable();
    }

    public boolean isNotConsumedFluid(int slot) {
        if (slot >= recipe.getFluidInputs().size()) return false;
        return !recipe.getFluidInputs().get(slot).isConsumable();
    }

    private int getPropertyListHeight() {
        if (recipeMap == GtRecipeTypes.COKE_OVEN_RECIPES)
            return LINE_HEIGHT - 6; // fun hack TODO Make this easier to position
        return (recipe.getUnhiddenPropertyCount() + 3) * LINE_HEIGHT - 3;
    }
}