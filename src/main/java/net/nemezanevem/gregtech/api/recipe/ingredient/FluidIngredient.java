package net.nemezanevem.gregtech.api.recipe.ingredient;

import com.google.common.collect.Lists;
import com.google.gson.*;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class FluidIngredient extends Ingredient {

    public static final FluidIngredient EMPTY = new FluidIngredient(Stream.empty(), false);
    private final FluidIngredient.Value[] values;
    @Nullable
    private FluidStack[] fluidStacks;
    @Nullable
    private IntList stackingIds;
    private int invalidationCounter;

    private final boolean isConsumable;

    protected FluidIngredient(Stream<? extends FluidIngredient.Value> pValues, boolean isConsumable) {
        super(Stream.empty());
        this.values = pValues.toArray(FluidIngredient.Value[]::new);
        this.isConsumable = isConsumable;
    }

    public FluidStack[] getFluids() {
        this.dissolve();
        return this.fluidStacks;
    }

    public boolean isConsumable() {
        return isConsumable;
    }

    public int getAmount() {
        this.dissolve();
        return this.fluidStacks[0].getAmount();
    }

    private void dissolve() {
        if (this.fluidStacks == null) {
            this.fluidStacks = Arrays.stream(this.values).flatMap((value) -> value.getItems().stream()).distinct().toArray(FluidStack[]::new);
        }
    }

    public boolean test(@Nullable FluidStack pStack) {
        if (pStack == null) {
            return false;
        } else {
            this.dissolve();
            if (this.fluidStacks.length == 0) {
                return pStack.isEmpty();
            } else {
                for(FluidStack itemstack : this.fluidStacks) {
                    if (itemstack.isFluidEqual(pStack)) {
                        return true;
                    }
                }

                return false;
            }
        }
    }

    public void toNetworkFluid(FriendlyByteBuf pBuffer) {
        this.dissolve();
        pBuffer.writeCollection(Arrays.asList(this.fluidStacks), FriendlyByteBuf::writeFluidStack);
    }

    public JsonElement toJson() {
        if (this.values.length == 1) {
            return this.values[0].serialize();
        } else {
            JsonArray jsonarray = new JsonArray();

            for(FluidIngredient.Value value : this.values) {
                jsonarray.add(value.serialize());
            }

            return jsonarray;
        }
    }

    public boolean isEmpty() {
        return this.values.length == 0 && (this.fluidStacks == null || this.fluidStacks.length == 0) && (this.stackingIds == null || this.stackingIds.isEmpty());
    }

    protected void invalidate() {
        this.fluidStacks = null;
        this.stackingIds = null;
    }

    public boolean isSimple() {
        return true;
    }

    public IIngredientSerializer<FluidIngredient> getSerializer() {
        return FluidIngredientSerializer.INSTANCE;
    }

    public static FluidIngredient fromValuesFluid(boolean isConsumable, Stream<? extends FluidIngredient.Value> pStream) {
        FluidIngredient ingredient = new FluidIngredient(pStream, isConsumable);
        return ingredient.values.length == 0 ? EMPTY : ingredient;
    }

    public static FluidIngredient ofFluid() {
        return EMPTY;
    }

    public static FluidIngredient ofFluid(boolean isConsumable, Fluid... fluids) {
        return ofFluid(isConsumable, Arrays.stream(fluids).map((fluid) -> new FluidStack(fluid, 1000)));
    }

    public static FluidIngredient ofFluid(boolean isConsumable, FluidStack... pStacks) {
        return ofFluid(isConsumable, Arrays.stream(pStacks));
    }

    public static FluidIngredient ofFluid(boolean isConsumable, Stream<FluidStack> pStacks) {
        return fromValuesFluid(isConsumable, pStacks.filter((stack) -> {
            return !stack.isEmpty();
        }).map(FluidIngredient.FluidValue::new));
    }

    public static FluidIngredient ofFluid(boolean isConsumable, TagKey<Fluid> pTag) {
        return fromValuesFluid(isConsumable, Stream.of(new FluidIngredient.FluidTagValue(pTag, 1000)));
    }

    public static FluidIngredient ofFluid(boolean isConsumable, TagKey<Fluid> pTag, int amount) {
        return fromValuesFluid(isConsumable, Stream.of(new FluidIngredient.FluidTagValue(pTag, amount)));
    }

    public static FluidIngredient fromNetwork(FriendlyByteBuf pBuffer) {
        var size = pBuffer.readVarInt();
        if (size == -1) return FluidIngredientSerializer.INSTANCE.parse(pBuffer);
        return fromValuesFluid(pBuffer.readBoolean(), Stream.generate(() -> new FluidIngredient.FluidValue(pBuffer.readFluidStack())).limit(size));
    }

    public static FluidIngredient fromJson(@Nullable JsonElement pJson) {
        if (pJson != null && !pJson.isJsonNull()) {
            if (pJson.isJsonObject()) {
                var obj = pJson.getAsJsonObject();
                return fromValuesFluid(obj.get("consumable").getAsBoolean(), Stream.of(valueFromJsonFluid(obj)));
            } else if (pJson.isJsonArray()) {
                JsonArray jsonarray = pJson.getAsJsonArray();
                if (jsonarray.size() == 0) {
                    throw new JsonSyntaxException("Item array cannot be empty, at least one item must be defined");
                } else {
                    return fromValuesFluid(true, StreamSupport.stream(jsonarray.spliterator(), false).map((p_151264_) -> {
                        return valueFromJsonFluid(GsonHelper.convertToJsonObject(p_151264_, "item"));
                    }));
                }
            } else {
                throw new JsonSyntaxException("Expected item to be object or array of objects");
            }
        } else {
            throw new JsonSyntaxException("Item cannot be null");
        }
    }

    public static FluidIngredient.Value valueFromJsonFluid(JsonObject pJson) {
        int amount = pJson.get("amount").getAsInt();
        if (pJson.has("fluid") && pJson.has("tag")) {
            throw new JsonParseException("An ingredient entry is either a tag or an item, not both");
        } else if (pJson.has("fluid")) {
            Fluid fluid = fluidFromJson(pJson);
            return new FluidIngredient.FluidValue(new FluidStack(fluid, amount));
        } else if (pJson.has("tag")) {
            ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(pJson, "tag"));
            TagKey<Fluid> tagkey = TagKey.create(Registry.FLUID_REGISTRY, resourcelocation);
            return new FluidTagValue(tagkey, amount);
        } else {
            throw new JsonParseException("An ingredient entry needs either a tag or an item");
        }
    }

    public static Fluid fluidFromJson(JsonObject pItemObject) {
        String s = GsonHelper.getAsString(pItemObject, "fluid");
        Fluid fluid = Registry.FLUID.getOptional(new ResourceLocation(s)).orElseThrow(() -> {
            return new JsonSyntaxException("Unknown fluid '" + s + "'");
        });
        if (fluid.getFluidType() == ForgeMod.EMPTY_TYPE.get()) {
            throw new JsonSyntaxException("Invalid fluid: " + s);
        } else {
            return fluid;
        }
    }

    public static FluidIngredient mergeFluid(Collection<FluidIngredient> parts) {
        return fromValuesFluid(parts.stream().anyMatch(val -> val.isConsumable), parts.stream().flatMap(i -> Arrays.stream(i.values)));
    }

    public static class FluidValue implements FluidIngredient.Value {
        private final FluidStack fluid;

        public FluidValue(FluidStack pItem) {
            this.fluid = pItem;
        }

        public Collection<FluidStack> getItems() {
            return Collections.singleton(this.fluid);
        }

        public JsonObject serialize() {
            JsonObject jsonobject = new JsonObject();
            jsonobject.addProperty("item", ForgeRegistries.FLUIDS.getKey(this.fluid.getFluid()).toString());
            return jsonobject;
        }
    }

    public static class FluidTagValue implements FluidIngredient.Value {
        private final TagKey<Fluid> tag;
        private final int amount;
        public FluidTagValue(TagKey<Fluid> pTag, int amount) {
            this.tag = pTag;
            this.amount = amount;
        }

        public Collection<FluidStack> getItems() {
            List<FluidStack> list = Lists.newArrayList();

            for(Fluid fluid : ForgeRegistries.FLUIDS.tags().getTag(this.tag)) {
                list.add(new FluidStack(fluid, 1000));
            }

            if (list.size() == 0) {
                list.add(new FluidStack(Fluids.EMPTY, 1000));
            }
            return list;
        }

        public JsonObject serialize() {
            JsonObject jsonobject = new JsonObject();
            jsonobject.addProperty("tag", this.tag.location().toString());
            return jsonobject;
        }
    }

    public interface Value {
        Collection<FluidStack> getItems();

        JsonObject serialize();
    }
}
