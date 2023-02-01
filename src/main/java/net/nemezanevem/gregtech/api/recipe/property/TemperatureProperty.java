package net.nemezanevem.gregtech.api.recipe.property;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.nemezanevem.gregtech.api.registry.recipe.property.RecipePropertyRegistry;
import net.nemezanevem.gregtech.api.unification.material.Material;
import org.apache.commons.lang3.Validate;

import java.util.Map;
import java.util.TreeMap;

public class TemperatureProperty extends RecipeProperty<Integer> {
    public static final String KEY = "temperature";

    private static final TreeMap<Integer, Object> registeredCoilTypes = new TreeMap<>((x, y) -> y - x);

    private static TemperatureProperty INSTANCE;

    private TemperatureProperty() {
        super(KEY, Integer.class);
    }

    public static TemperatureProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TemperatureProperty();
        }
        return INSTANCE;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.font.draw(new PoseStack(), Component.translatable("gregtech.recipe.temperature",
                value, getMinTierForTemperature(castValue(value))), x, y, color);
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer, Object value) {
        buffer.writeVarInt(this.castValue(value));
    }

    @Override
    public Integer readFromNetwork(FriendlyByteBuf buffer) {
        return buffer.readVarInt();
    }

    @Override
    public void writeToJson(JsonObject json, Object value) {
        json.addProperty("type", RecipePropertyRegistry.RECIPE_PROPERTIES_BUILTIN.get().getKey(this).toString());
        json.addProperty("value", this.castValue(value));
    }

    @Override
    public Integer readFromJson(JsonObject json) {
        return json.get("value").getAsInt();
    }

    private Component getMinTierForTemperature(Integer value) {
        Component name = CommonComponents.EMPTY;
        for (Map.Entry<Integer, Object> coil : registeredCoilTypes.entrySet()) {
            if (value <= coil.getKey()) {
                Object mapValue = coil.getValue();
                if (mapValue instanceof Material) {
                    name = ((Material) mapValue).getLocalizedName();
                } else if (mapValue instanceof String string) {
                    name = Component.translatable(string);
                }
            }
        }
        if (name.getString().length() >= 13) {
            name = Component.literal(name.getString().substring(0, 10) + "..");
        }
        return name;
    }

    /**
     * This Maps coil Materials to its Integer temperatures.
     * In case the coil was not constructed with a Material you can pass a String name,
     * ideally an unlocalized name
     */
    public static void registerCoilType(int temperature, Material coilMaterial, String coilName) {
        Validate.notNull(coilName);
        if (coilMaterial == null) {
            registeredCoilTypes.put(temperature, coilName);
        } else {
            registeredCoilTypes.put(temperature, coilMaterial);
        }
    }

}
