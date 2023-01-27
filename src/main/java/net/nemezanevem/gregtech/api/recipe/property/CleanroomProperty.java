package net.nemezanevem.gregtech.api.recipe.property;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
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

    @Nonnull
    private String getName(@Nonnull CleanroomType value) {
        Component nameKey = Component.translatable(value.getTranslationKey());
        String name = nameKey.getString();
        if (name.length() >= 20) return name.substring(0, 20) + "..";
        return name;
    }
}
