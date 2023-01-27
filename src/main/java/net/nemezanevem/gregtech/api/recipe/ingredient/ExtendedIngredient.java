package net.nemezanevem.gregtech.api.recipe.ingredient;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Stream;

public class ExtendedIngredient extends Ingredient {

    private final boolean isConsumable;

    private final boolean isTag;

    @Nullable
    private final TagKey<Item> tag;

    protected ExtendedIngredient(Stream<? extends Value> pValues, boolean isConsumable, @Nullable TagKey<Item> tag) {
        super(pValues);
        this.isConsumable = isConsumable;
        this.isTag = pValues.anyMatch(val -> val instanceof TagValue) || pValues.count() > 1;
        this.tag = tag;

    }

    public boolean isConsumable() {
        return isConsumable;
    }

    public boolean isTag() {
        return isTag;
    }

    public TagKey<Item> getTag() {
        return tag;
    }

    public static ExtendedIngredient fromValues(Stream<? extends Ingredient.Value> pStream, boolean isConsumable) {
        ExtendedIngredient ingredient = new ExtendedIngredient(pStream, isConsumable, pStream.findFirst().get() instanceof TagValue tagValue ? tagValue.tag : null);
        return ingredient.values.length == 0 ? (ExtendedIngredient) EMPTY : ingredient;
    }

    public static Ingredient of() {
        return EMPTY;
    }

    public static Ingredient of(boolean isConsumable, ItemLike... pItems) {
        return of(Arrays.stream(pItems).map(ItemStack::new), isConsumable);
    }

    public static Ingredient of(boolean isConsumable, ItemStack... pStacks) {
        return of(Arrays.stream(pStacks));
    }

    public static Ingredient of(Stream<ItemStack> pStacks, boolean isConsumable) {
        return fromValues(pStacks.filter((p_43944_) -> {
            return !p_43944_.isEmpty();
        }).map(Ingredient.ItemValue::new), isConsumable);
    }

    public static Ingredient of(TagKey<Item> pTag) {
        return fromValues(Stream.of(new Ingredient.TagValue(pTag)));
    }
}
