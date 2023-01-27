package net.nemezanevem.gregtech.api.recipe.property;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.nemezanevem.gregtech.api.registry.recipe.property.RecipePropertyRegistry;
import net.nemezanevem.gregtech.api.util.Util;

public class ImplosionExplosiveProperty extends RecipeProperty<ItemStack> {

    public static final String KEY = "explosives";

    private static ImplosionExplosiveProperty INSTANCE;


    private ImplosionExplosiveProperty() {
        super(KEY, ItemStack.class);
    }

    public static ImplosionExplosiveProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ImplosionExplosiveProperty();
        }

        return INSTANCE;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.font.draw(new PoseStack(), Component.translatable("gregtech.recipe.explosive",
                ((ItemStack) value).getDisplayName()), x, y, color);
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer, Object value) {
        buffer.writeItem(this.castValue(value));
    }

    @Override
    public ItemStack readFromNetwork(FriendlyByteBuf buffer) {
        return buffer.readItem();
    }

    @Override
    public void writeToJson(JsonObject json, Object value) {
        json.addProperty("type", RecipePropertyRegistry.RECIPE_PROPERTIES_BUILTIN.get().getKey(this).toString());
        var stack = this.castValue(value);
        String itemName = Util.getId(stack.getItem()).toString();
        json.addProperty("item", itemName);
        json.addProperty("count", stack.getCount());
    }

    @Override
    public ItemStack readFromJson(JsonObject json) {
        return CraftingHelper.getItemStack(json, false);
    }
}
