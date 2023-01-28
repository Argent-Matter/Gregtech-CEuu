package net.nemezanevem.gregtech.api.recipe.ingredient;

import com.google.gson.*;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.registries.ForgeRegistries;
import net.nemezanevem.gregtech.GregTech;
import org.checkerframework.checker.units.qual.C;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Stream;

public class ExtendedIngredient extends AbstractIngredient {
    public static final ExtendedIngredient EMPTY = new ExtendedIngredient((Value) null, false);

    private int amount;
    protected final boolean isConsumable;
    private final boolean isTag;
    public final Value value;

    protected ExtendedIngredient(@Nullable Value pValue, boolean isConsumable) {
        super(Stream.of(pValue));
        this.value = pValue;
        this.isConsumable = isConsumable;
        this.isTag = pValue instanceof TagValue;
    }

    protected ExtendedIngredient(@Nonnull ItemStack pValue, boolean isConsumable) {
        this(new CountValue(new ItemValue(pValue), pValue.getCount()), isConsumable);
    }

    protected ExtendedIngredient(@Nonnull TagValue pValue, int count, boolean isConsumable) {
        this(new CountValue(pValue, count), isConsumable);
    }

    protected ExtendedIngredient(@Nonnull TagKey<Item> pValue, int count, boolean isConsumable) {
        this(new CountValue(new TagValue(pValue), count), isConsumable);
    }

    public boolean isConsumable() {
        return isConsumable;
    }

    public boolean isTag() {
        return isTag;
    }

    @Nullable
    public TagKey<Item> getTag() {
        return this.values[0] instanceof TagValue tag ? tag.tag : null;
    }

    public static ExtendedIngredient fromValue(Value pStream, boolean isConsumable) {
        ExtendedIngredient ingredient = new ExtendedIngredient(pStream, isConsumable);
        return ingredient.values.length == 0 ? (ExtendedIngredient) EMPTY : ingredient;
    }

    public static ExtendedIngredient fromValue(TagKey<Item> tagKey, boolean isConsumable) {
        ExtendedIngredient ingredient = new ExtendedIngredient(new TagValue(tagKey), isConsumable);
        return ingredient.values.length == 0 ? EMPTY : ingredient;
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

        var serialized = this.value.serialize();
        json.add("value", serialized);


        return json;
    }

    @Nonnull
    public static ExtendedIngredient of() {
        return EMPTY;
    }

    public static ExtendedIngredient of(ItemStack stack, boolean isConsumable) {
        return stack.isEmpty() ? EMPTY : fromValue(new ItemValue(stack), isConsumable);
    }

    public static ExtendedIngredient of(TagKey<Item> pTag, boolean isConsumable) {
        return fromValue(pTag, isConsumable);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExtendedIngredient that)) return false;
        return isConsumable == that.isConsumable && isTag == that.isTag && ItemStack.matches(this.getItems()[0], that.getItems()[0]);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isConsumable, isTag);
    }

    protected ExtendedIngredient copy() {
        return new ExtendedIngredient(this.values[0], this.isConsumable);
    }

    public ExtendedIngredient copyWithAmount(int amount) {
        return new ExtendedIngredient(new CountValue(this.value, amount), isConsumable);
    }

    public static class CountValue implements Ingredient.Value {
        private final Value value;
        private int count;
        public CountValue(Value pItem, int count) {
            this.value = pItem;
            this.count = count;
        }

        public Collection<ItemStack> getItems() {
            return this.value.getItems();
        }

        public int getCount() {
            return count;
        }

        public JsonObject serialize() {
            JsonObject jsonobject = this.value.serialize();
            jsonobject.addProperty("type", GregTech.MODID + ":" + "count");
            if(count > 1) {
                jsonobject.addProperty("count", this.getCount());
            }
            return jsonobject;
        }

        public static CountValue fromJson(JsonObject json) {
            if(json.has("item")) {
                ItemStack stack = CraftingHelper.getItemStack(json, false);
                return new CountValue(new ItemValue(stack), stack.getCount());
            } else if (json.has("tag")) {
                ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(json, "tag"));
                TagKey<Item> tagkey = TagKey.create(Registry.ITEM_REGISTRY, resourcelocation);
                return new CountValue(new Ingredient.TagValue(tagkey), GsonHelper.getAsInt(json, "count"));
            }
            return new CountValue(new ItemValue(new ItemStack(Items.BARRIER)), 0);
        }
    }

    public static class Serializer implements IIngredientSerializer<ExtendedIngredient>
    {
        public static final ExtendedIngredient.Serializer INSTANCE = new ExtendedIngredient.Serializer();

        @Override
        public ExtendedIngredient parse(JsonObject json) {
            // parse items
            Ingredient.Value value = null;
            TagKey<Item> tag = null;
            if (json.has("item")) {
                Item item = ShapedRecipe.itemFromJson(json);
                value = new Ingredient.ItemValue(new ItemStack(item));
            }
            else if (json.has("tag") && json.has("count"))
                value = CountValue.fromJson(json.getAsJsonObject());
            else if (json.has("tag"))
                tag = ForgeRegistries.ITEMS.tags().createTagKey(new ResourceLocation(json.get("tag").getAsString()));
            else
                throw new JsonSyntaxException("Must set either 'item' or 'tag'");

            boolean isConsumable = json.get("isConsumable").getAsBoolean();

            return new ExtendedIngredient(value == null ? new TagValue(tag) : value, isConsumable);
        }

        @Override
        public ExtendedIngredient parse(FriendlyByteBuf buffer) {
            if(buffer.readBoolean()) {
                return ExtendedIngredient.fromValue(ForgeRegistries.ITEMS.tags().createTagKey(buffer.readResourceLocation()), buffer.readBoolean());
            } else {
                return ExtendedIngredient.fromValue(new Ingredient.ItemValue(buffer.readItem()), buffer.readBoolean());
            }
        }

        @Override
        public void write(FriendlyByteBuf buffer, ExtendedIngredient ingredient) {
            if(ingredient.isTag) {
                buffer.writeBoolean(true);
                buffer.writeResourceLocation(ingredient.getTag().location());
            } else {
                buffer.writeBoolean(false);
                buffer.writeItem(ingredient.getItems()[0]);
            }
            buffer.writeBoolean(ingredient.isConsumable);
        }
    }
}
