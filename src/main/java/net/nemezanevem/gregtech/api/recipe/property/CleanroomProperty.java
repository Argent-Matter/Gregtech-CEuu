package net.nemezanevem.gregtech.api.recipe.property;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.nemezanevem.gregtech.api.registry.recipe.property.RecipePropertyRegistry;
import net.nemezanevem.gregtech.api.tileentity.multiblock.CleanroomType;

import javax.annotation.Nonnull;

public class CleanroomProperty extends RecipeProperty<CleanroomType> {

    public static final String KEY = "cleanroom";

    private static CleanroomProperty INSTANCE;

    private CleanroomProperty() {
        super(KEY, CleanroomType.class);
    }

    public static CleanroomProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CleanroomProperty();
        }
        return INSTANCE;
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int x, int y, int color, Object value) {
        CleanroomType type = castValue(value);
        if (type == null) return;

        minecraft.font.draw(new PoseStack(), Component.translatable("gregtech.recipe.cleanroom", getName(type)), x, y, color);
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer, Object value) {
        buffer.writeUtf(this.castValue(value).getName());
    }

    @Override
    public CleanroomType readFromNetwork(FriendlyByteBuf buffer) {
        return CleanroomType.getByName(buffer.readUtf());
    }

    @Override
    public void writeToJson(JsonObject json, Object value) {
        json.addProperty("type", RecipePropertyRegistry.RECIPE_PROPERTIES_BUILTIN.get().getKey(this).toString());
        json.addProperty("value", this.castValue(value).getName());
    }

    @Override
    public CleanroomType readFromJson(JsonObject json) {
        return CleanroomType.getByName(GsonHelper.getAsString(json, "value"));
    }

    @Nonnull
    private String getName(@Nonnull CleanroomType value) {
        String name = Component.translatable(value.getTranslationKey()).getString();
        if (name.length() >= 20) return name.substring(0, 20) + "..";
        return name;
    }
}
