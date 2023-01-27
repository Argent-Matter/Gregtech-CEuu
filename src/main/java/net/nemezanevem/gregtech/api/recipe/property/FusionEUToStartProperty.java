package net.nemezanevem.gregtech.api.recipe.property;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.nemezanevem.gregtech.api.registry.recipe.property.RecipePropertyRegistry;
import net.nemezanevem.gregtech.api.util.TextFormattingUtil;
import org.apache.commons.lang3.Validate;

import java.util.Map;
import java.util.TreeMap;

public class FusionEUToStartProperty extends RecipeProperty<Long> {

    public static final String KEY = "eu_to_start";

    private static final TreeMap<Long, String> registeredFusionTiers = new TreeMap<>();

    private static FusionEUToStartProperty INSTANCE;

    protected FusionEUToStartProperty() {
        super(KEY, Long.class);
    }

    public static FusionEUToStartProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FusionEUToStartProperty();
        }

        return INSTANCE;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.font.draw(new PoseStack(), Component.translatable("gregtech.recipe.eu_to_start",
                TextFormattingUtil.formatLongToCompactString(castValue(value))) + getFusionTier(castValue(value)), x, y, color);
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer, Object value) {
        buffer.writeVarLong(this.castValue(value));
    }

    @Override
    public Long readFromNetwork(FriendlyByteBuf buffer) {
        return buffer.readVarLong();
    }

    @Override
    public void writeToJson(JsonObject json, Object value) {
        json.addProperty("type", RecipePropertyRegistry.RECIPE_PROPERTIES_BUILTIN.get().getKey(this).toString());
        json.addProperty("amount", this.castValue(value));
    }

    @Override
    public Long readFromJson(JsonObject json) {
        return GsonHelper.getAsLong(json, "amount", Integer.MAX_VALUE);
    }

    private String getFusionTier(Long eu) {

        Map.Entry<Long, String> mapEntry = registeredFusionTiers.ceilingEntry(eu);

        if (mapEntry == null) {
            throw new IllegalArgumentException("Value is above registered maximum EU values");
        }

        return String.format(" %s", mapEntry.getValue());
    }

    public static void registerFusionTier(int tier, String shortName) {
        Validate.notNull(shortName);
        long maxEU = 16 * 10000000L * (long) Math.pow(2, tier - 6);
        registeredFusionTiers.put(maxEU, shortName);

    }
}
