package net.nemezanevem.gregtech.integration.jei.recipe;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.recipe.GTRecipe;
import net.nemezanevem.gregtech.api.recipe.GTRecipe.ChanceEntry;
import net.nemezanevem.gregtech.api.recipe.GTRecipeType;
import net.nemezanevem.gregtech.api.recipe.GtRecipeTypes;
import net.nemezanevem.gregtech.api.recipe.property.PrimitiveProperty;
import net.nemezanevem.gregtech.api.recipe.property.RecipeProperty;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.client.util.TooltipHelper;

import javax.annotation.Nonnull;
import javax.swing.text.html.Option;
import java.util.*;
import java.util.function.Function;
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

    /*public Map<IIngredientType<?>, ? extends ExtendedIngredient> getIngredients() {
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
    }*/


    public void addItemTooltip(IRecipeSlotView recipeSlotView, List<Component> tooltip) {
        if (recipeSlotView.getRole() == RecipeIngredientRole.CATALYST) {
            tooltip.add(Component.literal(TooltipHelper.BLINKING_CYAN + Component.translatable("gregtech.recipe.not_consumed").getString()));
        } else {
            Optional<ChanceEntry> entry = recipe.getChancedOutputs().stream().filter(chanceEntry -> recipeSlotView.getItemStacks().collect(Collectors.toSet()).contains(chanceEntry.itemStack())).findFirst();

            if (entry.isPresent()) {
                var realEntry = entry.get();
                double chance = realEntry.chance() / 100.0;
                double boost = realEntry.boostPerTier() / 100.0;
                tooltip.add(Component.literal(TooltipHelper.BLINKING_CYAN + Component.translatable("gregtech.recipe.chance", chance, boost).getString()));
            }
        }
    }

    public void addFluidTooltip(IRecipeSlotView recipeSlotView, List<Component> tooltip) {
        boolean notConsumed = recipeSlotView.getRole() == RecipeIngredientRole.CATALYST;

        if (notConsumed) {
            tooltip.add(Component.literal(TooltipHelper.BLINKING_CYAN + Component.translatable("gregtech.recipe.not_consumed").getString()));
        }
    }

    public void drawInfo(PoseStack poseStack, @Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, double mouseX, double mouseY) {
        int yPosition = recipeHeight - getPropertyListHeight();
        if (!recipe.hasProperty(PrimitiveProperty.getInstance())) {
            minecraft.font.draw(poseStack, Component.translatable("gregtech.recipe.total", Math.abs((long) recipe.getEUt()) * recipe.getDuration()), 0, yPosition, 0x111111);
            minecraft.font.draw(poseStack, Component.translatable(recipe.getEUt() >= 0 ? "gregtech.recipe.eu" : "gregtech.recipe.eu_inverted", Math.abs(recipe.getEUt()), GTValues.VN[Util.getTierByVoltage(recipe.getEUt())]), 0, yPosition += LINE_HEIGHT, 0x111111);
        } else yPosition -= LINE_HEIGHT * 2;
        minecraft.font.draw(poseStack, Component.translatable("gregtech.recipe.duration", recipe.getDuration() / 20f), 0, yPosition += LINE_HEIGHT, 0x111111);
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