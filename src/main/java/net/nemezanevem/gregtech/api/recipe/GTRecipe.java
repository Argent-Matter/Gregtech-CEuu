package net.nemezanevem.gregtech.api.recipe;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ForgeRegistries;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.capability.IMultipleTankHandler;
import net.nemezanevem.gregtech.api.recipe.ingredient.ExtendedIngredient;
import net.nemezanevem.gregtech.api.recipe.ingredient.FluidIngredient;
import net.nemezanevem.gregtech.api.recipe.ingredient.GTRecipeInput;
import net.nemezanevem.gregtech.api.recipe.property.EmptyRecipePropertyStorage;
import net.nemezanevem.gregtech.api.recipe.property.IRecipePropertyStorage;
import net.nemezanevem.gregtech.api.recipe.property.RecipeProperty;
import net.nemezanevem.gregtech.api.registry.tileentity.MetaTileEntityRegistry;
import net.nemezanevem.gregtech.api.tileentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.util.ItemStackHashStrategy;
import net.nemezanevem.gregtech.api.util.Util;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.checkerframework.checker.units.qual.C;

import javax.annotation.Nonnull;
import java.util.*;

public class GTRecipe<R extends GTRecipe<R>> implements Recipe<CraftingContainer> {
    private static final NonNullList<ItemStack> EMPTY = NonNullList.create();
    public static int getMaxChancedValue() {
        return 10000;
    }
    public static String formatChanceValue(int outputChance) {
        return String.format("%.2f", outputChance / (getMaxChancedValue() * 1.0) * 100);
    }

    public ResourceLocation id;

    private final List<ExtendedIngredient> inputs;
    private final NonNullList<ItemStack> outputs;

    /**
     * A chance of 10000 equals 100%
     */
    private final List<ChanceEntry> chancedOutputs;
    private final List<FluidIngredient> fluidInputs;
    private final List<FluidStack> fluidOutputs;

    private final int duration;
    private final IRecipePropertyStorage recipePropertyStorage;


    /**
     * if > 0 means EU/t consumed, if < 0 - produced
     */
    private final int EUt;

    /**
     * If this Recipe is hidden from JEI
     */
    private final boolean hidden;


    private final RecipeType<?> type;

    public GTRecipe(RecipeType<?> type, ResourceLocation id, List<ExtendedIngredient> inputs, List<ItemStack> outputs, List<ChanceEntry> chancedOutputs,
                    List<FluidIngredient> fluidInputs, List<FluidStack> fluidOutputs,
                    int duration, int EUt, boolean hidden,
                    IRecipePropertyStorage recipePropertyStorage) {
        this.recipePropertyStorage =
                recipePropertyStorage == null ? EmptyRecipePropertyStorage.INSTANCE : recipePropertyStorage;
        if (inputs.isEmpty()) {
            this.inputs = Collections.emptyList();
        } else {
            this.inputs = NonNullList.create();
            this.inputs.addAll(inputs);
        }
        if (outputs.isEmpty()) {
            this.outputs = EMPTY;
        } else {
            this.outputs = NonNullList.create();
            this.outputs.addAll(outputs);
        }
        this.chancedOutputs = chancedOutputs.isEmpty() ? Collections.emptyList() : new ArrayList<>(chancedOutputs);
        this.fluidInputs = fluidInputs.isEmpty() ? Collections.emptyList() : ImmutableList.copyOf(fluidInputs);
        this.fluidOutputs = fluidOutputs.isEmpty() ? Collections.emptyList() : ImmutableList.copyOf(fluidOutputs);
        this.duration = duration;
        this.EUt = EUt;
        this.hidden = hidden;
        this.type = type;
    }


    public GTRecipe<R> copy() {
        return new GTRecipe<R>(this.type, this.id, this.inputs, this.outputs, this.chancedOutputs, this.fluidInputs,
                this.fluidOutputs, this.duration, this.EUt, this.hidden, this.recipePropertyStorage);
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return GtRecipes.SIMPLE_SERIALIZER.get();
    }

    /**
     * Trims the recipe outputs, chanced outputs, and fluid outputs based on the performing MetaTileEntity's trim limit.
     *
     * @param currentRecipe  The recipe to perform the output trimming upon
     * @param recipeMap      The RecipeMap that the recipe is from
     * @param itemTrimLimit  The Limit to which item outputs should be trimmed to, -1 for no trimming
     * @param fluidTrimLimit The Limit to which fluid outputs should be trimmed to, -1 for no trimming
     * @return A new Recipe whose outputs have been trimmed.
     */
    public GTRecipe<R> trimRecipeOutputs(GTRecipe<R> currentRecipe, RecipeType<?> recipeMap, int itemTrimLimit, int fluidTrimLimit) {

        // Fast return early if no trimming desired
        if (itemTrimLimit == -1 && fluidTrimLimit == -1) {
            return currentRecipe;
        }
        // Chanced outputs are removed in this if they cannot fit the limit
        Pair<List<ItemStack>, List<GTRecipe.ChanceEntry>> recipeOutputs = currentRecipe.getItemAndChanceOutputs(itemTrimLimit);

        List<FluidStack> recipeFluidOutputs = currentRecipe.getAllFluidOutputs(fluidTrimLimit);



        return new GTRecipe<>(currentRecipe.type, currentRecipe.id, currentRecipe.inputs, recipeOutputs.getFirst(), recipeOutputs.getSecond(), currentRecipe.fluidInputs, recipeFluidOutputs, currentRecipe.duration, currentRecipe.EUt, currentRecipe.hidden, currentRecipe.recipePropertyStorage);
    }

    public final boolean matches(boolean consumeIfSuccessful, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs) {
        return matches(consumeIfSuccessful, Util.itemHandlerToList(inputs), Util.fluidHandlerToList(fluidInputs));
    }

    /**
     * This methods aim to verify if the current recipe matches the given inputs according to matchingMode mode.
     *
     * @param consumeIfSuccessful if true will consume the inputs of the recipe.
     * @param inputs              Items input or Collections.emptyList() if none.
     * @param fluidInputs         Fluids input or Collections.emptyList() if none.
     * @return true if the recipe matches the given inputs false otherwise.
     */
    public boolean matches(boolean consumeIfSuccessful, List<ItemStack> inputs, List<FluidStack> fluidInputs) {
        Pair<Boolean, int[]> fluids = null;
        Pair<Boolean, int[]> items = null;

        if (fluidInputs.size() > 0) {
            fluids = matchesFluid(fluidInputs);
            if (!fluids.getFirst()) {
                return false;
            }
        }

        if (inputs.size() > 0) {
            items = matchesItems(inputs);
            if (!items.getFirst()) {
                return false;
            }
        }

        if (consumeIfSuccessful) {
            if (fluids != null) {
                int[] fluidAmountInTank = fluids.getSecond();

                for (int i = 0; i < fluidAmountInTank.length; i++) {
                    FluidStack fluidStack = fluidInputs.get(i);
                    int fluidAmount = fluidAmountInTank[i];
                    if (fluidStack == null || fluidStack.getAmount() == fluidAmount)
                        continue;
                    fluidStack.setAmount(fluidAmount);
                    if (fluidStack.getAmount() == 0)
                        fluidInputs.set(i, null);
                }
            }
            if(items != null) {
                int[] itemAmountInSlot = items.getSecond();
                for (int i = 0; i < itemAmountInSlot.length; i++) {
                    ItemStack itemInSlot = inputs.get(i);
                    int itemAmount = itemAmountInSlot[i];
                    if (itemInSlot.isEmpty() || itemInSlot.getCount() == itemAmount)
                        continue;
                    itemInSlot.setCount(itemAmountInSlot[i]);
                }
            }
        }

        return true;
    }

    private Pair<Boolean, int[]> matchesItems(List<ItemStack> inputs) {
        int[] itemAmountInSlot = new int[inputs.size()];
        int indexed = 0;

        List<ExtendedIngredient> gtRecipeInputs = this.inputs;
        for (int i = 0; i < gtRecipeInputs.size(); i++) {
            ExtendedIngredient ingredient = gtRecipeInputs.get(i);
            int ingredientAmount = ingredient.getItems()[0].getCount();
            for (int j = 0; j < inputs.size(); j++) {
                ItemStack inputStack = inputs.get(j);

                if (j == indexed) {
                    itemAmountInSlot[j] = inputStack.isEmpty() ? 0 : inputStack.getCount();
                    indexed++;
                }

                if (inputStack.isEmpty() || !ingredient.test(inputStack))
                    continue;
                int itemAmountToConsume = Math.min(itemAmountInSlot[j], ingredientAmount);
                ingredientAmount -= itemAmountToConsume;
                if (ingredient.isConsumable()) itemAmountInSlot[j] -= itemAmountToConsume;
                if (ingredientAmount == 0) break;
            }
            if (ingredientAmount > 0)
                return Pair.of(false, itemAmountInSlot);
        }
        int[] retItemAmountInSlot = new int[indexed];
        System.arraycopy(itemAmountInSlot, 0, retItemAmountInSlot, 0, indexed);

        return Pair.of(true, retItemAmountInSlot);
    }

    private Pair<Boolean, int[]> matchesFluid(List<FluidStack> fluidInputs) {
        int[] fluidAmountInTank = new int[fluidInputs.size()];
        int indexed = 0;

        List<FluidIngredient> gtRecipeInputs = this.fluidInputs;
        for (int i = 0; i < gtRecipeInputs.size(); i++) {
            FluidIngredient fluid = gtRecipeInputs.get(i);
            int fluidAmount = fluid.getAmount();
            for (int j = 0; j < fluidInputs.size(); j++) {
                FluidStack tankFluid = fluidInputs.get(j);
                if (j == indexed) {
                    indexed++;
                    fluidAmountInTank[j] = tankFluid == null ? 0 : tankFluid.getAmount();
                }

                if (tankFluid == null || fluid.test(tankFluid))
                    continue;
                int fluidAmountToConsume = Math.min(fluidAmountInTank[j], fluidAmount);
                fluidAmount -= fluidAmountToConsume;
                if (!fluid.isConsumable()) fluidAmountInTank[j] -= fluidAmountToConsume;
                if (fluidAmount == 0) break;
            }
            if (fluidAmount > 0)
                return Pair.of(false, fluidAmountInTank);
        }
        int[] returnFluidAmountInTank = new int[indexed];
        System.arraycopy(fluidAmountInTank, 0, returnFluidAmountInTank, 0, indexed);

        return Pair.of(true, returnFluidAmountInTank);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GTRecipe<R> recipe = (GTRecipe) o;
        return hasSameInputs(recipe) && hasSameFluidInputs(recipe);
    }

    private int makeHashCode() {
        int hash = 31 * hashInputs();
        hash = 31 * hash + hashFluidList(this.fluidInputs);
        return hash;
    }

    private int hashInputs() {
        int hash = 0;
        for (ExtendedIngredient recipeIngredient : this.inputs) {
            if (!recipeIngredient.isTag()) {
                for (ItemStack is : recipeIngredient.getItems()) {
                    hash = 31 * hash + ItemStackHashStrategy.comparingAll().hashCode(is);
                }
            } else {
                hash = 31 * hash + recipeIngredient.getTag().hashCode();
            }
        }
        return hash;
    }

    private boolean hasSameInputs(GTRecipe<R> otherRecipe) {
        List<ItemStack> otherStackList = new ObjectArrayList<>(otherRecipe.inputs.size());
        for (ExtendedIngredient otherInputs : otherRecipe.inputs) {
            otherStackList.addAll(Arrays.asList(otherInputs.getItems()));
        }
        if (!this.matchesItems(otherStackList).getFirst()) {
            return false;
        }

        List<ItemStack> thisStackList = new ObjectArrayList<>(this.inputs.size());
        for (ExtendedIngredient thisInputs : this.inputs) {
            thisStackList.addAll(Arrays.asList(thisInputs.getItems()));
        }
        return otherRecipe.matchesItems(thisStackList).getFirst();
    }

    public int hashFluidList(List<FluidIngredient> fluids) {
        int hash = 0;
        for (FluidIngredient fluidInput : fluids) {
            hash = 31 * hash + fluidInput.hashCode();
        }
        return hash;
    }

    private boolean hasSameFluidInputs(GTRecipe<R> otherRecipe) {
        List<FluidStack> otherFluidList = new ObjectArrayList<>(otherRecipe.fluidInputs.size());
        for (FluidIngredient otherInputs : otherRecipe.fluidInputs) {
            FluidStack[] fluidStack = otherInputs.getFluids();
            otherFluidList.add(fluidStack[0]);
        }
        if (!this.matchesFluid(otherFluidList).getFirst()) {
            return false;
        }

        List<FluidStack> thisFluidsList = new ObjectArrayList<>(this.fluidInputs.size());
        for (FluidIngredient thisFluidInputs : this.fluidInputs) {
            FluidStack fluidStack = thisFluidInputs.getFluids()[0];
            thisFluidsList.add(fluidStack);
        }
        return otherRecipe.matchesFluid(thisFluidsList).getFirst();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("inputs", inputs)
                .append("outputs", outputs)
                .append("chancedOutputs", chancedOutputs)
                .append("fluidInputs", fluidInputs)
                .append("fluidOutputs", fluidOutputs)
                .append("duration", duration)
                .append("EUt", EUt)
                .append("hidden", hidden)
                .toString();
    }

    ///////////////////
    //    Getters    //
    ///////////////////

    public List<ExtendedIngredient> getInputs() {
        return inputs;
    }

    public NonNullList<ItemStack> getOutputs() {
        return outputs;
    }

    // All Recipes this method is called for should be already trimmed, if required

    /**
     * Returns all outputs from the recipe.
     * This is where Chanced Outputs for the recipe are calculated.
     * The Recipe should be trimmed by calling {@link GTRecipe <R>#getItemAndChanceOutputs(int)} before calling this method,
     * if trimming is required.
     *
     * @param recipeTier  The Voltage Tier of the Recipe, used for chanced output calculation
     * @param machineTier The Voltage Tier of the Machine, used for chanced output calculation
     * @param recipeType   The RecipeMap that the recipe is being performed upon, used for chanced output calculation
     * @return A list of all resulting ItemStacks from the recipe, after chance has been applied to any chanced outputs
     */
    public List<ItemStack> getResultItemOutputs(int recipeTier, int machineTier, GTRecipeType<?> recipeType) {
        ArrayList<ItemStack> outputs = new ArrayList<>(Util.copyStackList(getOutputs()));
        List<ChanceEntry> chancedOutputsList = getChancedOutputs();
        List<ItemStack> resultChanced = new ArrayList<>();
        for (ChanceEntry chancedOutput : chancedOutputsList) {
            int outputChance = recipeType.getChanceFunction().chanceFor(
                    chancedOutput.getChance(), chancedOutput.getBoostPerTier(),
                    recipeTier, machineTier);
            if (GTValues.RNG.nextInt(GTRecipe.getMaxChancedValue()) <= outputChance) {
                ItemStack stackToAdd = chancedOutput.getItemStack();
                Util.addStackToItemStackList(stackToAdd, resultChanced);
            }
        }

        outputs.addAll(resultChanced);

        return outputs;
    }

    /**
     * Returns the maximum possible recipe outputs from a recipe, divided into regular and chanced outputs
     * Takes into account any specific output limiters, ie macerator slots, to trim down the output list
     * Trims from chanced outputs first, then regular outputs
     *
     * @param outputLimit The limit on the number of outputs, -1 for disabled.
     * @return A Pair of recipe outputs and chanced outputs, limited by some factor
     */
    public Pair<List<ItemStack>, List<ChanceEntry>> getItemAndChanceOutputs(int outputLimit) {
        List<ItemStack> outputs = new ArrayList<>();


        // Create an entry for the chanced outputs, and initially populate it
        List<ChanceEntry> chancedOutputs = new ArrayList<>(getChancedOutputs());


        // No limiting
        if (outputLimit == -1) {
            outputs.addAll(Util.copyStackList(getOutputs()));
        }
        // If just the regular outputs would satisfy the outputLimit
        else if (getOutputs().size() >= outputLimit) {
            outputs.addAll(Util.copyStackList(getOutputs()).subList(0, Math.min(outputLimit, getOutputs().size())));
            // clear the chanced outputs, as we are only getting regular outputs
            chancedOutputs.clear();
        }
        // If the regular outputs and chanced outputs are required to satisfy the outputLimit
        else if (!getOutputs().isEmpty() && (getOutputs().size() + chancedOutputs.size()) >= outputLimit) {
            outputs.addAll(Util.copyStackList(getOutputs()));

            // Calculate the number of chanced outputs after adding all the regular outputs
            int numChanced = outputLimit - getOutputs().size();

            chancedOutputs = chancedOutputs.subList(0, Math.min(numChanced, chancedOutputs.size()));
        }
        // There are only chanced outputs to satisfy the outputLimit
        else if (getOutputs().isEmpty()) {
            chancedOutputs = chancedOutputs.subList(0, Math.min(outputLimit, chancedOutputs.size()));
        }
        // The number of outputs + chanced outputs is lower than the trim number, so just add everything
        else {
            outputs.addAll(Util.copyStackList(getOutputs()));
            // Chanced outputs are taken care of in the original copy
        }

        return Pair.of(outputs, chancedOutputs);
    }

    /**
     * Returns a list of every possible ItemStack output from a recipe, including all possible chanced outputs.
     *
     * @return A List of ItemStack outputs from the recipe, including all chanced outputs
     */
    public List<ItemStack> getAllItemOutputs() {
        List<ItemStack> recipeOutputs = new ArrayList<>(this.outputs);

        for (ChanceEntry entry : this.chancedOutputs) {
            recipeOutputs.add(entry.getItemStack());
        }

        return recipeOutputs;
    }


    public List<ChanceEntry> getChancedOutputs() {
        return chancedOutputs;
    }

    public List<FluidIngredient> getFluidInputs() {
        return fluidInputs;
    }

    public boolean hasInputFluid(FluidStack fluid) {
        for (FluidIngredient fluidInput : fluidInputs) {
            fluidInput.test(fluid);
        }
        return false;
    }

    public List<FluidStack> getFluidOutputs() {
        return fluidOutputs;
    }

    /**
     * Trims the list of fluid outputs based on some passed factor.
     * Similar to {@link GTRecipe<R>#getItemAndChanceOutputs(int)} but does not handle chanced fluid outputs
     *
     * @param outputLimit The limiting factor to trim the fluid outputs to, -1 for disabled.
     * @return A trimmed List of fluid outputs.
     */
    // TODO, implement future chanced fluid outputs here
    public List<FluidStack> getAllFluidOutputs(int outputLimit) {
        return outputLimit == -1 ? fluidOutputs : fluidOutputs.subList(0, Math.min(fluidOutputs.size(), outputLimit));
    }

    public int getDuration() {
        return duration;
    }

    public int getEUt() {
        return EUt;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean hasValidInputsForDisplay() {
        for (Ingredient ingredient : inputs) {
            return Arrays.stream(ingredient.getItems()).noneMatch(ItemStack::isEmpty);
        }
        for (FluidIngredient fluidInput : fluidInputs) {
            FluidStack[] fluidIngredient = fluidInput.getFluids();
            for (var fluid : fluidIngredient) {
                if (fluid != null && fluid.getAmount() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    ///////////////////////////////////////////////////////////
    //               Property Helper Methods                 //
    ///////////////////////////////////////////////////////////
    public <T> T getProperty(RecipeProperty<T> property, T defaultValue) {
        return recipePropertyStorage.getRecipePropertyValue(property, defaultValue);
    }

    public Object getPropertyRaw(String key) {
        return recipePropertyStorage.getRawRecipePropertyValue(key);
    }

    public Set<Map.Entry<RecipeProperty<?>, Object>> getPropertyValues() {
        return recipePropertyStorage.getRecipeProperties();
    }

    public Set<String> getPropertyKeys() {
        return recipePropertyStorage.getRecipePropertyKeys();
    }

    public boolean hasProperty(RecipeProperty<?> property) {
        return recipePropertyStorage.hasRecipeProperty(property);
    }

    public int getPropertyCount() {
        return recipePropertyStorage.getSize();
    }

    public int getUnhiddenPropertyCount() {
        return (int) recipePropertyStorage.getRecipeProperties().stream().filter((property) -> !property.getKey().isHidden()).count();
    }

    public IRecipePropertyStorage getRecipePropertyStorage() {
        return recipePropertyStorage;
    }

    @Override
    public RecipeType<?> getType() {
        return null;
    }

    /**
     * Get the result of this recipe, usually for display purposes (e.g. recipe book). If your recipe has more than one
     * possible result (e.g. it's dynamic and depends on its inputs), then return an empty stack.
     */
    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    private NonNullList<Ingredient> inputsCache = null;

    @Nonnull
    @Override
    public NonNullList<Ingredient> getIngredients() {
        if(inputsCache == null) {
            inputsCache = this.inputs.stream()
                    .map(val -> (Ingredient) val)
                    .collect(
                            NonNullList::create,
                            AbstractList::add,
                            (toCombine, combineTo) -> combineTo.addAll(toCombine));
        }
        return inputsCache;
    }

    @Override
    public boolean matches(CraftingContainer pContainer, Level pLevel) {
        return false;
    }

    /**
     * Returns an Item that is the result of this recipe
     */
    public ItemStack assemble(CraftingContainer pInv) {
        return this.outputs.get(0).copy();
    }

    /**
     * Used to determine if this recipe can fit in a grid of the given width/height
     */
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth * pHeight >= this.inputs.size();
    }

    public static class Serializer<R extends GTRecipe<R>> implements RecipeSerializer<GTRecipe<R>> {
        public GTRecipe<R> fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
            String group = GsonHelper.getAsString(pJson, "group", "");
            String type = GsonHelper.getAsString(pJson, "type", "gregtech:basic");
            long eUt = GsonHelper.getAsLong(pJson, "EUt");
            var typeReal = ForgeRegistries.RECIPE_TYPES.getValue(new ResourceLocation(type));
            ResourceLocation machineId = new ResourceLocation(GsonHelper.getAsString(pJson, "machine"));
            MetaTileEntity mte = MetaTileEntityRegistry.META_TILE_ENTITIES_BUILTIN.get().getValue(machineId);
            NonNullList<Ingredient> nonnulllist = itemsFromJson(GsonHelper.getAsJsonArray(pJson, "ingredients"));
            if (nonnulllist.isEmpty()) {
                throw new JsonParseException("No ingredients for shapeless recipe");
            }
            NonNullList<ItemStack> results = resultsFromJson(GsonHelper.getAsJsonArray(pJson, "results"));
            return new GTRecipe<R>(typeReal, pRecipeId, group, results, nonnulllist, mte, eUt);
        }

        private static NonNullList<Ingredient> itemsFromJson(JsonArray pIngredientArray) {
            NonNullList<Ingredient> nonnulllist = NonNullList.create();

            for(int i = 0; i < pIngredientArray.size(); ++i) {
                Ingredient ingredient = Ingredient.fromJson(pIngredientArray.get(i));
                if (!ingredient.isEmpty()) { // FORGE: Skip checking if an ingredient is empty during shapeless recipe deserialization to prevent complex ingredients from caching tags too early. Can not be done using a config value due to sync issues.
                    nonnulllist.add(ingredient);
                }
            }

            return nonnulllist;
        }

        private static NonNullList<ItemStack> resultsFromJson(JsonArray pIngredientArray) {
            NonNullList<ItemStack> results = NonNullList.create();

            for(int i = 0; i < pIngredientArray.size(); ++i) {
                ItemStack stack = CraftingHelper.getItemStack(pIngredientArray.get(i).getAsJsonObject(), true, true);
                if (!stack.isEmpty()) {
                    results.add(stack);
                }
            }

            return results;
        }

        public GTRecipe<R> fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            RecipeType<?> type = pBuffer.readRegistryId();
            int sizeInputs = pBuffer.readVarInt();
            NonNullList<Ingredient> nonnulllist = NonNullList.withSize(sizeInputs, Ingredient.EMPTY);

            nonnulllist.replaceAll(ignored -> Ingredient.fromNetwork(pBuffer));

            int sizeResults = pBuffer.readVarInt();
            NonNullList<ItemStack> results = NonNullList.withSize(sizeResults, ItemStack.EMPTY);

            results.replaceAll(ignored -> pBuffer.readItem());

            MetaTileEntity mte = pBuffer.readRegistryId();
            long eUt = pBuffer.readVarLong();


            return new GTRecipe<R>(type, s, results, nonnulllist, mte, eUt);
        }

        public void toNetwork(FriendlyByteBuf pBuffer, GTRecipe<R> pRecipe) {
            pBuffer.writeRegistryId(ForgeRegistries.RECIPE_TYPES, pRecipe.type);
            pBuffer.writeVarInt(pRecipe.inputs.size());

            for(ExtendedIngredient ingredient : pRecipe.inputs) {
                ingredient.toNetwork(pBuffer);
            }

            pBuffer.writeVarInt(pRecipe.outputs.size());
            for (ItemStack result : pRecipe.outputs) {
                pBuffer.writeItem(result);
            }

            pBuffer.writeVarInt(pRecipe.chancedOutputs.size());
            for ()

            pBuffer.writeRegistryId(MetaTileEntityRegistry.META_TILE_ENTITIES_BUILTIN.get(), pRecipe.machine);
            pBuffer.writeVarLong(pRecipe.EUt);
        }
    }

    ///////////////////////////////////////////////////////////
    //                   Chanced Output                      //
    ///////////////////////////////////////////////////////////
    public static class ChanceEntry {
        private final ItemStack itemStack;
        private final int chance;
        private final int boostPerTier;

        public ChanceEntry(ItemStack itemStack, int chance, int boostPerTier) {
            this.itemStack = itemStack.copy();
            this.chance = chance;
            this.boostPerTier = boostPerTier;
        }

        public ItemStack getItemStack() {
            return itemStack.copy();
        }

        public ItemStack getItemStackRaw() {
            return itemStack;
        }

        public int getChance() {
            return chance;
        }

        public int getBoostPerTier() {
            return boostPerTier;
        }

        public ChanceEntry copy() {
            return new ChanceEntry(itemStack, chance, boostPerTier);
        }

        public void toNetwork(FriendlyByteBuf pBuffer) {
            pBuffer.writeItem(itemStack);
            pBuffer.writeVarInt(chance);
            pBuffer.writeVarInt(boostPerTier);
        }

        public static ChanceEntry fromNetwork(FriendlyByteBuf pBuffer) {
            return new ChanceEntry(pBuffer.readItem(), pBuffer.readVarInt(), pBuffer.readVarInt());
        }
    }
}

