package net.nemezanevem.gregtech.integration.jei.recipe;

import mezz.jei.api.recipe.category.extensions.IExtendableRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.recipe.GTRecipe.ChanceEntry;

public class GTRecipeWrapper extends IExtendableRecipeCategory<> {

    private static final int LINE_HEIGHT = 10;

    private final RecipeType<?> recipeMap;
    private final GTRecipe recipe;

    public GTRecipeWrapper(RecipeType<?> recipeMap, Recipe recipe) {
        this.recipeMap = recipeMap;
        this.recipe = recipe;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    @Override
    public void getIngredients(@Nonnull IIngredients ingredients) {

        // Inputs
        if (!recipe.getInputs().isEmpty()) {
            List<List<ItemStack>> matchingInputs = new ArrayList<>(recipe.getInputs().size());
            for (GTRecipeInput recipeInput : recipe.getInputs()) {
                matchingInputs.add(Arrays.stream(recipeInput.getInputStacks())
                        .map(ItemStack::copy)
                        .collect(Collectors.toList()));
            }
            ingredients.setInputLists(VanillaTypes.ITEM, matchingInputs);
        }

        // Fluid Inputs
        if (!recipe.getFluidInputs().isEmpty()) {
            List<FluidStack> matchingFluidInputs = new ArrayList<>(recipe.getFluidInputs().size());

            for (GTRecipeInput fluidInput : recipe.getFluidInputs()) {
                FluidStack fluidStack = fluidInput.getInputFluidStack();
                Collections.addAll(matchingFluidInputs, fluidStack);
            }
            ingredients.setInputs(VanillaTypes.FLUID, matchingFluidInputs);
        }

        // Outputs
        if (!recipe.getOutputs().isEmpty() || !recipe.getChancedOutputs().isEmpty()) {
            List<ItemStack> recipeOutputs = recipe.getOutputs()
                    .stream().map(ItemStack::copy).collect(Collectors.toList());

            List<ChanceEntry> chancedOutputs = recipe.getChancedOutputs();
            chancedOutputs.sort(Comparator.comparingInt(entry -> entry == null ? 0 : entry.getChance()));
            for (ChanceEntry chancedEntry : chancedOutputs) {
                recipeOutputs.add(chancedEntry.getItemStackRaw());
            }
            ingredients.setOutputs(VanillaTypes.ITEM, recipeOutputs);
        }

        // Fluid Outputs
        if (!recipe.getFluidOutputs().isEmpty()) {
            ingredients.setOutputs(VanillaTypes.FLUID, recipe.getFluidOutputs().stream()
                    .map(FluidStack::copy)
                    .collect(Collectors.toList()));
        }
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

    @Override
    public void initExtras() {
        BooleanSupplier creativePlayerCtPredicate = () -> Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.isCreative() && Loader.isModLoaded(GTValues.MODID_CT);
        final String mod = GroovyScriptCompat.isLoaded() ? "GroovyScript" : "CraftTweaker";
        buttons.add(new JeiButton(166, 2, 10, 10)
                .setTextures(GuiTextures.BUTTON_CLEAR_GRID)
                .setTooltipBuilder(lines -> lines.add("Copies a " + mod + " script, to remove this recipe, to the clipboard"))
                .setClickAction((minecraft, mouseX, mouseY, mouseButton) -> {
                    String recipeLine = GroovyScriptCompat.isLoaded() ?
                            GroovyScriptCompat.getRecipeRemoveLine(recipeMap, recipe) :
                            CTRecipeHelper.getRecipeRemoveLine(recipeMap, recipe);
                    String output = CTRecipeHelper.getFirstOutputString(recipe);
                    if (!output.isEmpty()) {
                        output = "// " + output + "\n";
                    }
                    String copyString = output + recipeLine + "\n";
                    ClipboardUtil.copyToClipboard(copyString);
                    Minecraft.getInstance().player.sendSystemMessage(Component.literal("Copied [\u00A76" + recipeLine + "\u00A7r] to the clipboard"));
                    return true;
                })
                .setActiveSupplier(creativePlayerCtPredicate));
    }

    public ChanceEntry getOutputChance(int slot) {
        if (slot >= recipe.getChancedOutputs().size() || slot < 0) return null;
        return recipe.getChancedOutputs().get(slot);
    }

    public boolean isNotConsumedItem(int slot) {
        if (slot >= recipe.getInputs().size()) return false;
        return recipe.getInputs().get(slot).isNonConsumable();
    }

    public boolean isNotConsumedFluid(int slot) {
        if (slot >= recipe.getFluidInputs().size()) return false;
        return recipe.getFluidInputs().get(slot).isNonConsumable();
    }

    private int getPropertyListHeight() {
        if (recipeMap == RecipeTypes.COKE_OVEN_RECIPES)
            return LINE_HEIGHT - 6; // fun hack TODO Make this easier to position
        return (recipe.getUnhiddenPropertyCount() + 3) * LINE_HEIGHT - 3;
    }
}