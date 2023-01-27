package net.nemezanevem.gregtech.api.recipe.ingredient;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class ExtendedIngredient extends AbstractIngredient {
    public static final ExtendedIngredient EMPTY = new ExtendedIngredient(Stream.empty(), false, null);



    private final boolean isConsumable;

    private final boolean isTag;

    @Nullable
    private final TagKey<Item> tag;

    protected ExtendedIngredient(@Nullable Stream<? extends Value> pValues, boolean isConsumable, @Nullable TagKey<Item> tag) {
        super(pValues == null ? ForgeRegistries.ITEMS.tags().getTag(tag).stream().map(val -> new ItemValue(new ItemStack(val))) : pValues);
        this.isConsumable = isConsumable;
        this.isTag = pValues == null || pValues.anyMatch(val -> val instanceof TagValue);
        this.tag = tag;
    }

    protected ExtendedIngredient(@Nonnull Stream<? extends ItemStack> pValues, boolean isConsumable) {
        this(pValues.map(ItemValue::new), isConsumable, null);
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
        ExtendedIngredient ingredient = new ExtendedIngredient(pStream, isConsumable, null);
        return ingredient.values.length == 0 ? (ExtendedIngredient) EMPTY : ingredient;
    }

    public static ExtendedIngredient fromValues(TagKey<Item> tagKey, boolean isConsumable) {
        ExtendedIngredient ingredient = new ExtendedIngredient(null, isConsumable, tagKey);
        return ingredient.values.length == 0 ? (ExtendedIngredient) EMPTY : ingredient;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", CraftingHelper.getID(PartialNBTIngredient.Serializer.INSTANCE).toString());
        json.addProperty("isConsumable", isConsumable);

        if (this.values.length == 1) {
            var serialized = this.values[0].serialize();
            json.add("item", serialized);
            return serialized;
        } else {
            JsonArray values = new JsonArray();

            for(Ingredient.Value ingredient$value : this.values) {
                values.add(ingredient$value.serialize());
            }

            json.add("items", values);
        }


        return json;
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

    public static ExtendedIngredient of(TagKey<Item> pTag, boolean isConsumable) {
        return fromValues(Stream.of(new ExtendedIngredient.TagValue(pTag)), isConsumable);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExtendedIngredient that)) return false;
        return isConsumable == that.isConsumable && isTag == that.isTag && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isConsumable, isTag, tag);
    }

    public static class Serializer implements IIngredientSerializer<ExtendedIngredient>
    {
        public static final ExtendedIngredient.Serializer INSTANCE = new ExtendedIngredient.Serializer();

        @Override
        public ExtendedIngredient parse(JsonObject json) {
            // parse items
            Stream<Ingredient.Value> items = null;
            TagKey<Item> tag = null;
            if (json.has("item")) {
                Item item = ShapedRecipe.itemFromJson(json);
                items = Stream.of(new Ingredient.ItemValue(new ItemStack(item)));
            }
            else if (json.has("tag"))
                tag = ForgeRegistries.ITEMS.tags().createTagKey(new ResourceLocation(json.get("tag").getAsString()));
            else
                throw new JsonSyntaxException("Must set either 'item' or 'tag'");

            boolean isConsumable = json.get("isConsumable").getAsBoolean();

            return new ExtendedIngredient(items, isConsumable, tag);
        }

        @Override
        public ExtendedIngredient parse(FriendlyByteBuf buffer) {
            if(buffer.readBoolean()) {
                return ExtendedIngredient.fromValues(ForgeRegistries.ITEMS.tags().createTagKey(buffer.readResourceLocation()), buffer.readBoolean());
            } else {
                return ExtendedIngredient.fromValues(Stream.generate(() -> new Ingredient.ItemValue(buffer.readItem())).limit(buffer.readVarInt()), buffer.readBoolean());
            }
        }

        @Override
        public void write(FriendlyByteBuf buffer, ExtendedIngredient ingredient) {
            if(ingredient.isTag) {
                buffer.writeBoolean(true);
                buffer.writeResourceLocation(ingredient.tag.location());
            } else {
                buffer.writeBoolean(false);
                ItemStack[] items = ingredient.getItems();
                buffer.writeVarInt(items.length);

                for (ItemStack stack : items)
                    buffer.writeItem(stack);
            }
            buffer.writeBoolean(ingredient.isConsumable);
        }
    }
}
