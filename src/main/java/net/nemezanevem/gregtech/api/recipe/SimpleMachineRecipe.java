package net.nemezanevem.gregtech.api.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.nemezanevem.gregtech.api.util.Util;

public class SimpleMachineRecipe implements Recipe<CraftingContainer> {
    private final ResourceLocation id;
    final String group;
    final NonNullList<ItemStack> results;
    final NonNullList<Ingredient> ingredients;
    private final boolean isSimple;

    private final Machine machine;
    private final long EUt;

    public SimpleMachineRecipe(ResourceLocation pId, String pGroup, NonNullList<ItemStack> results, NonNullList<Ingredient> pIngredients, Machine machine, long EUt) {
        this.machine = machine;
        this.id = pId;
        this.group = pGroup;
        this.results = results;
        this.ingredients = pIngredients;
        this.isSimple = pIngredients.stream().allMatch(Ingredient::isSimple);
        this.EUt = EUt;
    }

    public long getEUt() {
        return EUt;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return GtRecipes.SIMPLE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return null;
    }

    /**
     * Recipes with equal group are combined into one button in the recipe book
     */
    @Override
    public String getGroup() {
        return this.group;
    }

    /**
     * Get the result of this recipe, usually for display purposes (e.g. recipe book). If your recipe has more than one
     * possible result (e.g. it's dynamic and depends on its inputs), then return an empty stack.
     */
    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    /**
     * Used to check if a recipe matches current crafting inventory
     */
    @Override
    public boolean matches(CraftingContainer pInv, Level pLevel) {
        StackedContents stackedcontents = new StackedContents();
        java.util.List<ItemStack> inputs = new java.util.ArrayList<>();
        int i = 0;

        for(int j = 0; j < pInv.getContainerSize(); ++j) {
            ItemStack itemstack = pInv.getItem(j);
            if (!itemstack.isEmpty()) {
                ++i;
                if (isSimple)
                    stackedcontents.accountStack(itemstack, 1);
                else inputs.add(itemstack);
            }
        }

        return i == this.ingredients.size() && (isSimple ? stackedcontents.canCraft(this, (IntList)null) : net.minecraftforge.common.util.RecipeMatcher.findMatches(inputs,  this.ingredients) != null);
    }

    /**
     * Returns an Item that is the result of this recipe
     */
    public ItemStack assemble(CraftingContainer pInv) {
        return this.results.get(0).copy();
    }

    /**
     * Used to determine if this recipe can fit in a grid of the given width/height
     */
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth * pHeight >= this.ingredients.size();
    }

    public static class Serializer implements RecipeSerializer<SimpleMachineRecipe> {
        public SimpleMachineRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
            String s = GsonHelper.getAsString(pJson, "group", "");
            long eUt = GsonHelper.getAsLong(pJson, "EUt");
            NonNullList<Ingredient> nonnulllist = itemsFromJson(GsonHelper.getAsJsonArray(pJson, "ingredients"));
            if (nonnulllist.isEmpty()) {
                throw new JsonParseException("No ingredients for shapeless recipe");
            }
            NonNullList<ItemStack> results = resultsFromJson(GsonHelper.getAsJsonArray(pJson, "results"));
            return new SimpleMachineRecipe(pRecipeId, s, results, nonnulllist,  eUt);
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

        public SimpleMachineRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            String s = pBuffer.readUtf();
            int i = pBuffer.readVarInt();
            NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i, Ingredient.EMPTY);

            for(int j = 0; j < nonnulllist.size(); ++j) {
                nonnulllist.set(j, Ingredient.fromNetwork(pBuffer));
            }

            int sizeResults = pBuffer.readVarInt();
            NonNullList<ItemStack> results = NonNullList.withSize(sizeResults, ItemStack.EMPTY);

            for (int j = 0; j < results.size(); ++j) {
                results.set(j, pBuffer.readItem());
            }

            ResourceLocation machineId = new ResourceLocation(pBuffer.readUtf());

            return new SimpleMachineRecipe(pRecipeId, s, results, nonnulllist, MachineRegistry.MACHINES_INTERNAL.get().getValue(machineId));
        }

        public void toNetwork(FriendlyByteBuf pBuffer, SimpleMachineRecipe pRecipe) {
            pBuffer.writeUtf(pRecipe.group);
            pBuffer.writeVarInt(pRecipe.ingredients.size());

            for(Ingredient ingredient : pRecipe.ingredients) {
                ingredient.toNetwork(pBuffer);
            }

            pBuffer.writeVarInt(pRecipe.results.size());
            for (ItemStack result : pRecipe.results) {
                pBuffer.writeItem(result);
            }

        }
    }
}

