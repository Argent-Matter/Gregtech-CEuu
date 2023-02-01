package net.nemezanevem.gregtech.api.recipe.property;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public class DefaultProperty<T> extends RecipeProperty<T> {

    public DefaultProperty(String key, Class<T> type) {
        super(key, type);
    }

    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.font.draw(new PoseStack(), Component.translatable("gregtech.recipe." + getKey(),
                castValue(value)), x, y, color);
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer, Object value) {
        //no-op
    }

    @Override
    public T readFromNetwork(FriendlyByteBuf buffer) {
        return null;
    }

    @Override
    public void writeToJson(JsonObject json, Object value) {
        //no-op
    }

    @Override
    public T readFromJson(JsonObject json) {
        return null;
    }

}
