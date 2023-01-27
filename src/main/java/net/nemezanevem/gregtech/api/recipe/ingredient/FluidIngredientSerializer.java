package net.nemezanevem.gregtech.api.recipe.ingredient;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

public class FluidIngredientSerializer implements IIngredientSerializer<FluidIngredient> {
    public static final FluidIngredientSerializer INSTANCE  = new FluidIngredientSerializer();

    @Nonnull
    @Override
    public FluidIngredient parse(FriendlyByteBuf buffer)
    {
        return FluidIngredient.fromValuesFluid(buffer.readBoolean(), Stream.generate(() -> new FluidIngredient.FluidValue(buffer.readFluidStack())).limit(buffer.readVarInt()));
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

        FluidStack[] fluids = ingredient.getFluids();
        buffer.writeVarInt(fluids.length);

        for (FluidStack stack : fluids)
            buffer.writeFluidStack(stack);
    }
}
