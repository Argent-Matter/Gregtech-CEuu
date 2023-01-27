package net.nemezanevem.gregtech.api.recipe.ingredient;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import net.nemezanevem.gregtech.api.recipe.ingredient.nbtmatch.TagCondition;
import net.nemezanevem.gregtech.api.recipe.ingredient.nbtmatch.TagMatcher;
import net.nemezanevem.gregtech.api.util.Util;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;

/**
 * Definition of ItemStacks, Ore dicts, of ingredients for
 * use on RecipeMaps Recipes go here.
 * <p>
 * Forge uses are nor Hashable neither implement equals for these cases,
 * as they use a list of ItemStacks internally.
 * <p>
 * The behavior of the ingredient is determined by the GTingredient used.
 */
public abstract class GTRecipeInput {

    /**
     * All GTRecipeInput instances will be cached and reused through this collection.
     * This cache will be released on FMLLoadCompleteEvent.
     */

    public static ObjectOpenHashSet<GTRecipeInput> INSTANCES = new ObjectOpenHashSet<>(15072);

    /**
     * All items will initially match the with is NBT (OreDicts have a null tag?)
     * but this behavior can be changed by using a NBTMatcher and an appropriate NBTCondition.
     */

    protected int amount;
    protected boolean isConsumable = true;
    protected TagMatcher nbtMatcher;
    protected TagCondition nbtCondition;

    static GTRecipeInput getFromCache(GTRecipeInput realIngredient) {
        GTRecipeInput cachedIngredient = INSTANCES.get(realIngredient);
        if (cachedIngredient == null) {
            INSTANCES.add(cachedIngredient = realIngredient);
        }
        return cachedIngredient;
    }

    public static GTRecipeInput getOrCreate(GTRecipeInput gtRecipeIngredient) {
        return getFromCache(gtRecipeIngredient);
    }

    public int getAmount() {
        return amount;
    }

    abstract GTRecipeInput copy();

    /**
     * Returns a copy of the ingredient with the given amount.
     * Used by the parallel logic to multiply the amount of the ingredients.
     * If you're not using the parallel logic, you can ignore this.
     *
     * @return returns a copy of the GTRecipeInput with the given amount.
     */
    public abstract GTRecipeInput copyWithAmount(int amount);

    public GTRecipeInput setNonConsumable() {
        GTRecipeInput copy = copy();
        copy.isConsumable = false;
        return getFromCache(copy);
    }

    public GTRecipeInput setNBTMatchingCondition(TagMatcher nbtMatcher, TagCondition nbtCondition) {
        GTRecipeInput copy = copy();
        copy.nbtMatcher = nbtMatcher;
        copy.nbtCondition = nbtCondition;
        return getFromCache(copy);
    }

    public boolean hasNBTMatchingCondition() {
        return nbtMatcher != null;
    }

    public TagMatcher getNBTMatcher() {
        return nbtMatcher;
    }

    public TagCondition getNBTMatchingCondition() {
        return nbtCondition;
    }

    public boolean isNonConsumable() {
        return !isConsumable;
    }

    public ItemStack[] getInputStacks() {
        return null;
    }

    public FluidStack getInputFluidStack() {
        return null;
    }

    public boolean isOreDict() {
        return false;
    }

    public int getOreDict() {
        return -1;
    }

    public boolean acceptsStack(@Nullable ItemStack input) {
        return false;
    }

    public boolean acceptsFluid(@Nullable FluidStack input) {
        return false;
    }

    /**
     * @return true if the input matches another input, while ignoring its amount field and
     * non-consumable status.
     * <p>
     * used for unique input matching in RecipeMap
     * @see gregtech.api.recipes.RecipeMap#uniqueIngredientsList(List) (GTRecipeInput)
     */
    public abstract boolean equalIgnoreAmount(GTRecipeInput input);

    protected static class ItemToMetaList implements Object2ObjectMap.Entry<Item, List<MetaToTAGList>> {
        protected Item item;
        protected List<MetaToTAGList> metaToTAGList;

        public ItemToMetaList(ItemStack stack) {
            this.item = stack.getItem();
            this.metaToTAGList = ObjectLists.singleton(new MetaToTAGList(stack));
        }

        void addStackToLists(ItemStack stack) {
            if (this.metaToTAGList instanceof ObjectLists.Singleton) {
                this.metaToTAGList = new ObjectArrayList<>(this.metaToTAGList);
            }
            this.metaToTAGList.add(new MetaToTAGList(stack));
        }

        @Override
        public Item getKey() {
            return item;
        }

        @Override
        public List<MetaToTAGList> getValue() {
            return metaToTAGList;
        }

        @Override
        public List<MetaToTAGList> setValue(List<MetaToTAGList> value) {
            return metaToTAGList = value;
        }
    }

    protected static class MetaToTAGList implements Object2ObjectMap.Entry<ResourceLocation, List<TagToStack>> {
        protected List<TagToStack> tagToStack;
        protected ResourceLocation id;

        public MetaToTAGList(ItemStack stack) {
            this.tagToStack = ObjectLists.singleton(new TagToStack(stack));
            this.id = Util.getId(stack.getItem());
        }

        void addStackToList(ItemStack stack) {
            if (this.tagToStack instanceof ObjectLists.Singleton) {
                this.tagToStack = new ObjectArrayList<>(this.tagToStack);
            }
            this.tagToStack.add(new TagToStack(stack.getTag(), stack));
        }

        @Override
        public ResourceLocation getKey() {
            return id;
        }

        @Override
        public List<TagToStack> getValue() {
            return tagToStack;
        }

        @Override
        public List<TagToStack> setValue(List<TagToStack> value) {
            return tagToStack = value;
        }
    }

    protected static class TagToStack implements Object2ObjectMap.Entry<CompoundTag, ItemStack> {
        CompoundTag tag;
        ItemStack stack;

        TagToStack(CompoundTag tag, ItemStack stack) {
            this.tag = tag;
            this.stack = stack;
        }

        TagToStack(ItemStack stack) {
            this.tag = stack.getTag();
            this.stack = stack;
        }

        @Override
        public CompoundTag getKey() {
            return tag;
        }

        @Override
        public ItemStack getValue() {
            return stack;
        }

        @Override
        public ItemStack setValue(ItemStack value) {
            return stack = value;
        }
    }
}
