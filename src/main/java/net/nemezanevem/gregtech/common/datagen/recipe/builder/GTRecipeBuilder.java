package net.nemezanevem.gregtech.common.datagen.recipe.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.recipe.GTRecipe;
import net.nemezanevem.gregtech.api.recipe.GTRecipeType;
import net.nemezanevem.gregtech.api.recipe.ingredient.ExtendedIngredient;
import net.nemezanevem.gregtech.api.recipe.ingredient.FluidIngredient;
import net.nemezanevem.gregtech.api.recipe.property.EmptyRecipePropertyStorage;
import net.nemezanevem.gregtech.api.recipe.property.IRecipePropertyStorage;
import net.nemezanevem.gregtech.api.recipe.property.RecipeProperty;
import net.nemezanevem.gregtech.api.recipe.property.RecipePropertyStorage;
import net.nemezanevem.gregtech.api.util.Util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class GTRecipeBuilder<R extends GTRecipeBuilder<R>> implements RecipeBuilder {

    private final NonNullList<ExtendedIngredient> inputs = NonNullList.createWithCapacity(6);
    private final NonNullList<ItemStack> outputs = NonNullList.createWithCapacity(1);
    private final NonNullList<GTRecipe.ChanceEntry> chancedOutputs = NonNullList.create();
    private final NonNullList<FluidIngredient> fluidInputs = NonNullList.create();
    private NonNullList<FluidStack> fluidOutputs = NonNullList.create();
    private int duration, EUt;
    private boolean hidden = false;
    protected int parallel = 0;

    private IRecipePropertyStorage recipePropertyStorage = new RecipePropertyStorage();

    private GTRecipeType<R> type;

    private final Advancement.Builder advancement = Advancement.Builder.advancement();

    public GTRecipeBuilder() {
    }

    public GTRecipeBuilder(GTRecipe recipe, GTRecipeType<R> type) {
        this.type = type;

        this.inputs.addAll(recipe.getInputs());
        this.outputs.addAll(Util.copyStackList(recipe.getOutputs()));
        this.chancedOutputs.addAll(recipe.getChancedOutputs());
        this.fluidInputs.addAll(recipe.getFluidInputs());
        this.fluidOutputs = Util.copyFluidList(recipe.getFluidOutputs());
        this.duration = recipe.getDuration();
        this.EUt = recipe.getEUt();
        this.hidden = recipe.isHidden();
        this.recipePropertyStorage = recipe.getRecipePropertyStorage().copy();
        if (this.recipePropertyStorage != null) {
            this.recipePropertyStorage.freeze(false);
        }
    }

    protected GTRecipeBuilder(GTRecipeBuilder<R> recipeBuilder) {
        this.type = recipeBuilder.type;

        this.inputs.addAll(recipeBuilder.getInputs());
        this.outputs.addAll(Util.copyStackList(recipeBuilder.getOutputs()));
        this.chancedOutputs.addAll(recipeBuilder.chancedOutputs);
        this.fluidInputs.addAll(recipeBuilder.getFluidInputs());
        this.fluidOutputs = Util.copyFluidList(recipeBuilder.getFluidOutputs());
        this.duration = recipeBuilder.duration;
        this.EUt = recipeBuilder.EUt;
        this.hidden = recipeBuilder.hidden;
        this.recipePropertyStorage = recipeBuilder.recipePropertyStorage;
        if (this.recipePropertyStorage != null) {
            this.recipePropertyStorage = this.recipePropertyStorage.copy();
        }
    }

    /**
     * Adds an ingredient that can be any item in the given tag.
     */
    public R input(TagKey<Item> pTag) {
        return this.input(pTag, true);
    }

    /**
     * Adds an ingredient that can be any item in the given tag.
     */
    public R input(TagKey<Item> pTag, boolean isConsumable) {
        return this.input(ExtendedIngredient.of(pTag, isConsumable));
    }

    /**
     * Adds an ingredient of the given item.
     */
    public R inputMany(ItemLike pItem) {
        return this.input(pItem, true);
    }

    /**
     * Adds an ingredient of the given item.
     */
    public R input(ItemLike pItem, boolean isConsumable) {
        return this.input(pItem, 1, isConsumable);
    }

    /**
     * Adds the given ingredient multiple times.
     */
    public R input(ItemLike pItem, int pQuantity) {
        return this.input(pItem, pQuantity, true);
    }

    /**
     * Adds the given ingredient multiple times.
     */
    public R input(ItemLike pItem, int pQuantity, boolean isConsumable) {
        for(int i = 0; i < pQuantity; ++i) {
            this.input(ExtendedIngredient.of(new ItemStack(pItem), isConsumable));
        }

        return (R) this;
    }

    /**
     * Adds the given ingredient multiple times.
     */
    public R inputMany(ItemStack... pItems) {
        return this.inputMany(true, pItems);
    }

    /**
     * Adds the given ingredient multiple times.
     */
    public R inputMany(boolean isConsumable, ItemStack... pItems) {
        for (ItemStack pItem : pItems) {
            this.input(ExtendedIngredient.of(pItem, isConsumable));
        }

        return (R) this;
    }

    /**
     * Adds an ingredient.
     */
    public R input(ExtendedIngredient pIngredient) {
        return this.input(pIngredient, 1);
    }

    /**
     * Adds an ingredient multiple times.
     */
    public R input(ExtendedIngredient pIngredient, int pQuantity) {
        for(int i = 0; i < pQuantity; ++i) {
            this.inputs.add(pIngredient);
        }

        return (R) this;
    }

    /**
     * adds a fluid ingredient.
     */
    public R fluidInput(FluidStack input) {
        return this.fluidInput(FluidIngredient.ofFluid(true, input));
    }

    /**
     * adds a fluid ingredient.
     */
    public R fluidInput(FluidStack... input) {
        return this.fluidInput(FluidIngredient.ofFluid(true, input));
    }

    /**
     * adds a fluid ingredient.
     */
    public R fluidInput(boolean isConsumable, FluidStack... input) {
        return this.fluidInput(FluidIngredient.ofFluid(isConsumable, input));
    }

    /**
     * adds a fluid ingredient.
     */
    public R fluidInput(FluidStack input, boolean isConsumable) {
        return this.fluidInput(FluidIngredient.ofFluid(isConsumable, input));
    }

    /**
     * adds a fluid ingredient.
     */
    public R fluidInput(FluidIngredient input) {
        this.fluidInputs.add(input);
        return (R) this;
    }

    /**
     * adds many fluid ingredients.
     */
    public R fluidInputMany(FluidStack... inputs) {
        return this.fluidInputMany(true, inputs);
    }

    /**
     * adds many fluid ingredients.
     */
    public R fluidInputMany(boolean isConsumable, FluidStack... inputs) {
        for(FluidStack input : inputs) {
            this.fluidInput(FluidIngredient.ofFluid(isConsumable, input));
        }
        return (R) this;
    }

    /**
     * adds many fluid ingredients.
     */
    public R fluidInputMany(FluidIngredient... input) {
        this.fluidInputs.addAll(Arrays.asList(input));
        return (R) this;
    }

    public R fluidInputs(Collection<FluidIngredient> fluidIngredients) {
        this.fluidInputs.addAll(fluidIngredients);
        return (R) this;
    }

    public R output(Item output) {
        return this.output(output, 1);
    }

    public R output(Item output, int count) {
        return this.output(new ItemStack(output, count));
    }

    public R output(ItemStack output) {
        this.outputs.add(output);
        return (R) this;
    }

    public R outputs(ItemStack... output) {
        this.outputs.addAll(Arrays.asList(output));
        return (R) this;
    }

    public R outputs(Collection<ItemStack> outputs) {
        outputs = new ArrayList<>(outputs);
        outputs.removeIf(stack -> stack == null || stack.isEmpty());
        this.outputs.addAll(outputs);
        return (R) this;
    }


    public R chancedOutput(GTRecipe.ChanceEntry output) {
        this.chancedOutputs.add(output);
        return (R) this;
    }

    public R chancedOutput(ItemStack output, int chance, int boostPerTier) {
        this.chancedOutputs.add(new GTRecipe.ChanceEntry(output, chance, boostPerTier));
        return (R) this;
    }

    public R fluidOutput(FluidStack output) {
        this.fluidOutputs.add(output);
        return (R) this;
    }

    public R fluidOutput(FluidStack... output) {
        this.fluidOutputs.addAll(Arrays.asList(output));
        return (R) this;
    }

    public R fluidOutputs(Collection<FluidStack> outputs) {
        outputs = new ArrayList<>(outputs);
        outputs.removeIf(FluidStack::isEmpty);
        this.fluidOutputs.addAll(outputs);
        return (R) this;
    }

    /**
     * Copies the chanced outputs of a Recipe numberOfOperations times, so every chanced output
     * gets an individual roll, instead of an all or nothing situation
     *
     * @param chancedOutputsFrom The original recipe before any parallel multiplication
     * @param numberOfOperations The number of parallel operations that have been performed
     */

    public void chancedOutputsMultiply(GTRecipe chancedOutputsFrom, int numberOfOperations) {
        for (GTRecipe.ChanceEntry entry : chancedOutputsFrom.getChancedOutputs()) {
            int chance = entry.chance();
            int boost = entry.boostPerTier();

            // Add individual chanced outputs per number of parallel operations performed, to mimic regular recipes.
            // This is done instead of simply batching the chanced outputs by the number of parallel operations performed
            IntStream.range(0, numberOfOperations).forEach(value -> {
                this.chancedOutput(entry.itemStack(), chance, boost);
            });
        }
    }

    public R inputIngredients(Collection<ExtendedIngredient> inputs) {
        for (ExtendedIngredient input : inputs) {
            if (input.value instanceof ExtendedIngredient.CountValue countValue && countValue.getCount() < 0) {
                GregTech.LOGGER.error("Count cannot be less than 0. Actual: {}.", countValue.getCount());
                GregTech.LOGGER.error("Stacktrace:", new IllegalArgumentException());
                continue;
            }
            this.inputs.add(input);
        }
        return (R) this;
    }

    public R unlockedBy(@Nonnull String pCriterionName, @Nonnull CriterionTriggerInstance pCriterionTrigger) {
        this.advancement.addCriterion(pCriterionName, pCriterionTrigger);
        return (R) this;
    }

    /**
     * Appends the passed {@link GTRecipe} onto the inputs and outputs, multiplied by the amount specified by multiplier
     * The duration of the multiplied {@link GTRecipe} is also added to the current duration
     *
     * @param recipe           The Recipe to be multiplied
     * @param multiplier       Amount to multiply the recipe by
     * @param multiplyDuration Whether duration should be multiplied instead of EUt
     * @return the builder holding the multiplied recipe
     */

    public R append(GTRecipe recipe, int multiplier, boolean multiplyDuration) {
        for (Map.Entry<RecipeProperty<?>, Object> property : recipe.getPropertyValues()) {
            this.applyProperty(property.getKey(), property.getValue());
        }

        // Create holders for the various parts of the new multiplied Recipe
        List<ExtendedIngredient> newRecipeInputs = new ArrayList<>();
        List<FluidIngredient> newFluidInputs = new ArrayList<>();
        List<ItemStack> outputItems = new ArrayList<>();
        List<FluidStack> outputFluids = new ArrayList<>();

        // Populate the various holders of the multiplied Recipe
        multiplyInputsAndOutputs(newRecipeInputs, newFluidInputs, outputItems, outputFluids, recipe, multiplier);

        // Build the new Recipe with multiplied components
        this.inputIngredients(newRecipeInputs);
        this.fluidInputs(newFluidInputs);

        this.outputs(outputItems);
        chancedOutputsMultiply(recipe, multiplier);

        this.fluidOutputs(outputFluids);

        this.setEUt(multiplyDuration ? recipe.getEUt() : this.EUt + recipe.getEUt() * multiplier);
        this.setBaseDuration(multiplyDuration ? this.duration + recipe.getDuration() * multiplier : recipe.getDuration());
        this.parallel += multiplier;

        return (R) this;
    }

    protected static void multiplyInputsAndOutputs(List<ExtendedIngredient> newRecipeInputs,
                                                   List<FluidIngredient> newFluidInputs,
                                                   List<ItemStack> outputItems,
                                                   List<FluidStack> outputFluids,
                                                   GTRecipe recipe,
                                                   int numberOfOperations) {
        recipe.getInputs().forEach(ri -> {
            if(ri.value instanceof ExtendedIngredient.CountValue value) {
                if (!ri.isConsumable()) {
                    newRecipeInputs.add(ri);
                } else {
                    newRecipeInputs.add(ri.copyWithAmount(value.getCount() * numberOfOperations));
                }
            }

        });

        recipe.getFluidInputs().forEach(fi -> {
            if (!fi.isConsumable()) {
                newFluidInputs.add(fi);
            } else {
                newFluidInputs.add(fi.copyWithAmount(fi.getAmount() * numberOfOperations));
            }
        });

        recipe.getOutputs().forEach(itemStack ->
                outputItems.add(copyItemStackWithCount(itemStack,
                        itemStack.getCount() * numberOfOperations)));

        recipe.getFluidOutputs().forEach(fluidStack ->
                outputFluids.add(copyFluidStackWithAmount(fluidStack,
                        fluidStack.getAmount() * numberOfOperations)));
    }

    protected static ItemStack copyItemStackWithCount(ItemStack itemStack, int count) {
        ItemStack itemCopy = itemStack.copy();
        itemCopy.setCount(count);
        return itemCopy;
    }

    protected static FluidStack copyFluidStackWithAmount(FluidStack fluidStack, int count) {
        FluidStack fluidCopy = fluidStack.copy();
        fluidCopy.setAmount(count);
        return fluidCopy;
    }

    ///////////////////
    //    Getters    //
    ///////////////////

    public List<ExtendedIngredient> getInputs() {
        return inputs;
    }

    public List<ItemStack> getOutputs() {
        return outputs;
    }

    public List<GTRecipe.ChanceEntry> getChancedOutputs() {
        return chancedOutputs;
    }

    public List<FluidIngredient> getFluidInputs() {
        return fluidInputs;
    }

    public List<FluidStack> getFluidOutputs() {
        return fluidOutputs;
    }

    /**
     * Similar to {@link GTRecipe#getAllItemOutputs()}, returns the recipe outputs and all chanced outputs
     *
     * @return A List of ItemStacks composed of the recipe outputs and chanced outputs
     */
    public List<ItemStack> getAllItemOutputs() {
        List<ItemStack> stacks = new ArrayList<>(getOutputs());

        for (int i = 0; i < this.chancedOutputs.size(); i++) {
            GTRecipe.ChanceEntry entry = this.chancedOutputs.get(i);
            stacks.add(entry.itemStack());
        }

        return stacks;
    }

    public int getParallel() {
        return parallel;
    }

    public int getEUt() {
        return EUt;
    }

    /**
     * note: unused, don't use!!!
     * @param pGroupName UNUSED
     * @return this
     */
    @Deprecated
    @Override

    public R group(@Nullable String pGroupName) {
        return (R) this;
    }

    public R setHidden() {
        this.hidden = true;
        return (R) this;
    }

    public R setEUt(int eUt) {
        this.EUt = eUt;
        return (R) this;
    }

    public R setBaseDuration(int duration) {
        this.duration = duration;
        return (R) this;
    }

    public R applyProperty(RecipeProperty<?> property, Object value) {
        this.recipePropertyStorage.store(property, value);
        return (R) this;
    }

    public R setType(GTRecipeType<R> type) {
        this.type = type;
        return (R) this;
    }

    public R copy() {
        return (R) new GTRecipeBuilder<R>(this);
    }


    public Item getResult() {
        return this.outputs.get(0).getItem();
    }

    public void save(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ResourceLocation pRecipeId) {
        this.ensureValid(pRecipeId);
        this.advancement.parent(ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(pRecipeId)).rewards(AdvancementRewards.Builder.recipe(pRecipeId)).requirements(RequirementsStrategy.OR);
        pFinishedRecipeConsumer.accept(
                new Result<>(this.type, pRecipeId,
                        this.inputs, this.outputs, this.chancedOutputs,
                        this.fluidInputs, this.fluidOutputs,
                        this.duration, this.EUt, this.hidden,
                        this.recipePropertyStorage,
                        this.advancement, new ResourceLocation(pRecipeId.getNamespace(), "recipes/" + this.type.toString() + "/" + pRecipeId.getPath())));
    }

    public GTRecipe build(ResourceLocation pRecipeId) {
        return new GTRecipe(this.type, pRecipeId,
                this.inputs, this.outputs, this.chancedOutputs,
                this.fluidInputs, this.fluidOutputs,
                this.duration, this.EUt, this.hidden,
                this.recipePropertyStorage);
    }

    /**
     * Makes sure that this recipe is valid and obtainable.
     */
    private void ensureValid(ResourceLocation pId) {
        if (this.advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + pId);
        }
    }

    public static class Result<R extends GTRecipeBuilder<R>> implements FinishedRecipe {
        public ResourceLocation id;
        private final NonNullList<ExtendedIngredient> inputs;
        private final NonNullList<ItemStack> outputs;
        private final NonNullList<GTRecipe.ChanceEntry> chancedOutputs;
        private final NonNullList<FluidIngredient> fluidInputs;
        private final NonNullList<FluidStack> fluidOutputs;
        private final int duration;
        private final IRecipePropertyStorage recipePropertyStorage;
        private final int EUt;
        private final boolean hidden;
        private final GTRecipeType<R> type;

        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;

        public Result(GTRecipeType<R> type, ResourceLocation id, NonNullList<ExtendedIngredient> inputs, NonNullList<ItemStack> outputs, NonNullList<GTRecipe.ChanceEntry> chancedOutputs,
                      NonNullList<FluidIngredient> fluidInputs, NonNullList<FluidStack> fluidOutputs,
                      int duration, int EUt, boolean hidden,
                      IRecipePropertyStorage recipePropertyStorage,
                      Advancement.Builder advancement, ResourceLocation advancementId) {
            this.recipePropertyStorage = recipePropertyStorage == null ? EmptyRecipePropertyStorage.INSTANCE : recipePropertyStorage;
            this.inputs = inputs;
            this.outputs = outputs;
            this.chancedOutputs = chancedOutputs;
            this.fluidInputs = fluidInputs;
            this.fluidOutputs = fluidOutputs;
            this.duration = duration;
            this.EUt = EUt;
            this.hidden = hidden;
            this.type = type;
            this.id = id;

            this.advancement = advancement;
            this.advancementId = advancementId;
        }

        public void serializeRecipeData(JsonObject json) {
            json.addProperty("type", ForgeRegistries.RECIPE_TYPES.getKey(this.type).toString());

            json.addProperty("EUt", this.EUt);
            json.addProperty("duration", this.duration);
            json.addProperty("hidden", this.hidden);

            //ingredients
            JsonArray itemInputs = new JsonArray();
            for(Ingredient ingredient : this.inputs) {
                itemInputs.add(ingredient.toJson());
            }
            json.add("ingredients", itemInputs);

            //fluid ingredients
            JsonArray fluidInputs = new JsonArray();
            for(FluidIngredient fluidIngredient : this.fluidInputs) {
                fluidInputs.add(fluidIngredient.toJson());
            }
            json.add("fluidIngredients", fluidInputs);

            //non-chanced outputs
            if(outputs.size() > 1) {
                JsonArray itemResults = new JsonArray();

                for (ItemStack result : this.outputs) {
                    JsonObject itemResult = new JsonObject();
                    itemResult.addProperty("item", ForgeRegistries.ITEMS.getKey(result.getItem()).toString());
                    itemResult.addProperty("count", result.getCount());
                    if (result.hasTag()) itemResult.addProperty("nbt", result.getTag().toString());

                    itemResults.add(itemResult);
                }

                json.add("results", itemResults);
            } else {
                ItemStack result = outputs.get(0);
                JsonObject itemResult = new JsonObject();
                itemResult.addProperty("item", ForgeRegistries.ITEMS.getKey(result.getItem()).toString());
                itemResult.addProperty("count", result.getCount());
                if (result.hasTag()) itemResult.addProperty("nbt", result.getTag().toString());

                json.add("result", itemResult);
            }

            //chanced outputs
            if(chancedOutputs.size() > 1) {
                JsonArray results = new JsonArray();

                for (GTRecipe.ChanceEntry result : this.chancedOutputs) {
                    JsonObject resultObject = new JsonObject();
                    result.toJson(resultObject);
                    results.add(resultObject);
                }

                json.add("chancedResults", results);
            } else {
                GTRecipe.ChanceEntry result = chancedOutputs.get(0);
                JsonObject resultObject = new JsonObject();
                result.toJson(resultObject);

                resultObject.add("chancedResult", resultObject);
            }

            //fluid outputs
            if(fluidOutputs.size() > 1) {
                JsonArray results = new JsonArray();

                for (FluidStack result : this.fluidOutputs) {
                    var dataResult = FluidStack.CODEC.encode(result, JsonOps.INSTANCE, JsonOps.INSTANCE.empty());
                    dataResult.result().ifPresent(results::add);
                }

                json.add("chancedResults", results);
            } else {
                var dataResult = FluidStack.CODEC.encode(this.fluidOutputs.get(0), JsonOps.INSTANCE, JsonOps.INSTANCE.empty());

                dataResult.result().ifPresent(val -> json.add("chancedResult", val));
            }

            //recipe properties
            JsonObject props = new JsonObject();

            recipePropertyStorage.toJson(props);
        }

        public RecipeSerializer<?> getType() {
            return RecipeSerializer.SHAPELESS_RECIPE;
        }

        /**
         * Gets the ID for the recipe.
         */
        public ResourceLocation getId() {
            return this.id;
        }

        /**
         * Gets the JSON for the advancement that unlocks this recipe. Null if there is no advancement.
         */
        @Nullable
        public JsonObject serializeAdvancement() {
            return this.advancement.serializeToJson();
        }

        /**
         * Gets the ID for the advancement associated with this recipe. Should not be null if {@link #}
         * is non-null.
         */
        @Nullable
        public ResourceLocation getAdvancementId() {
            return this.advancementId;
        }
    }
}
