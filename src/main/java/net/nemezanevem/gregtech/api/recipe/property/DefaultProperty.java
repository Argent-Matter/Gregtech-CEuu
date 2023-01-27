package net.nemezanevem.gregtech.api.recipe.property;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class DefaultProperty<T> extends RecipeProperty<T> {

    public DefaultProperty(String key, Class<T> type) {
        super(key, type);
    }

    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.font.draw(new PoseStack(), Component.translatable("gregtech.recipe." + getKey(),
                castValue(value)), x, y, color);
    }

}
