package net.nemezanevem.gregtech.api.recipe.ingredient;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class FluidIngredientSerializer implements IIngredientSerializer<FluidIngredient> {
    public static final FluidIngredientSerializer INSTANCE  = new FluidIngredientSerializer();

    @Nonnull
    @Override
    public FluidIngredient parse(FriendlyByteBuf buffer)
    {
        return FluidIngredient.fromValuesFluid(buffer.readBoolean(), new FluidIngredient.FluidValue(buffer.readFluidStack()));
    }

    @Nonnull
    @Override
    public FluidIngredient parse(JsonObject json)
    {
        return FluidIngredient.fromJson(json);
        //return FluidIngredient.fromValuesFluid(json.get("consumable").getAsBoolean(), Stream.of(FluidIngredient.valueFromJsonFluid(json)));
    }

    @Override
    public void write(FriendlyByteBuf buffer, FluidIngredient ingredient)
    {
        buffer.writeBoolean(ingredient.isConsumable());
        buffer.writeFluidStack(Arrays.stream(ingredient.getFluids()).findFirst().get());
    }
}
