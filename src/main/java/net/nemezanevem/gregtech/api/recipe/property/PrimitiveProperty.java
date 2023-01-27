package net.nemezanevem.gregtech.api.recipe.property;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.nemezanevem.gregtech.api.registry.recipe.property.RecipePropertyRegistry;

/**
 * Simple Marker Property to tell JEI to not display Total EU and EU/t.
 */
public class PrimitiveProperty extends RecipeProperty<Boolean> {

    public static final String KEY = "primitive_property";
    private static PrimitiveProperty INSTANCE;

    private PrimitiveProperty() {
        super(KEY, Boolean.class);
    }

    public static PrimitiveProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PrimitiveProperty();
        }
        return INSTANCE;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {

    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer, Object value) {
        buffer.writeBoolean(this.castValue(value));
    }

    @Override
    public Boolean readFromNetwork(FriendlyByteBuf buffer) {
        return buffer.readBoolean();
    }

    @Override
    public void writeToJson(JsonObject json, Object value) {
        json.addProperty("type", RecipePropertyRegistry.RECIPE_PROPERTIES_BUILTIN.get().getKey(this).toString());
        json.addProperty("value", this.castValue(value));
    }

    @Override
    public Boolean readFromJson(JsonObject json) {
        return GsonHelper.getAsBoolean(json, "value", false);
    }
}
