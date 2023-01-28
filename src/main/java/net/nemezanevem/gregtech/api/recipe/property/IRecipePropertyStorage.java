package net.nemezanevem.gregtech.api.recipe.property;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.nemezanevem.gregtech.api.registry.recipe.property.RecipePropertyRegistry;

import java.util.Map;
import java.util.Set;

public interface IRecipePropertyStorage {

    String STACKTRACE = "Stacktrace:";

    /**
     * Stores new {@link RecipeProperty} with value
     *
     * @param recipeProperty {@link RecipeProperty}
     * @param value          value
     * @return <code>true</code> if store succeeds; otherwise <code>false</code>
     */
    boolean store(RecipeProperty<?> recipeProperty, Object value);

    boolean remove(RecipeProperty<?> recipeProperty);

    void freeze(boolean frozen);

    IRecipePropertyStorage copy();

    /**
     * Provides information how many {@link RecipeProperty} are stored
     *
     * @return number of stored {@link RecipeProperty}
     */
    int getSize();

    /**
     * Provides all stored {@link RecipeProperty}
     *
     * @return all stored {@link RecipeProperty} and values
     */
    Set<Map.Entry<RecipeProperty<?>, Object>> getRecipeProperties();

    /**
     * Provides casted value for one specific {@link RecipeProperty} if is stored or defaultValue
     *
     * @param recipeProperty {@link RecipeProperty}
     * @param defaultValue   Default value if recipeProperty is not found
     * @param <T>            Type of returned value
     * @return value tied with provided recipeProperty on success; otherwise defaultValue
     */
    <T> T getRecipePropertyValue(RecipeProperty<T> recipeProperty, T defaultValue);

    boolean hasRecipeProperty(RecipeProperty<?> recipeProperty);

    Set<String> getRecipePropertyKeys();

    /**
     * Provides un-casted value for one specific {@link RecipeProperty} searched by key
     *
     * @param key Key of stored {@link RecipeProperty}
     * @return {@link Object} value on success; otherwise <code>null</code>
     */
    Object getRawRecipePropertyValue(String key);

    void toNetwork(FriendlyByteBuf buffer);

    static IRecipePropertyStorage fromNetwork(FriendlyByteBuf buffer) {
        if(!buffer.readBoolean()) {
            RecipePropertyStorage storage = new RecipePropertyStorage();
            var length = buffer.readVarInt();
            for (int i = 0; i < length; ++i) {
                RecipeProperty<?> property = buffer.readRegistryId();
                storage.store(property, property.readFromNetwork(buffer));
            }
            return storage;
        }
        return EmptyRecipePropertyStorage.INSTANCE;
    }

    void toJson(JsonObject json);

    static IRecipePropertyStorage fromJson(JsonObject json) {
        if(json.has("properties")) {
            RecipePropertyStorage storage = new RecipePropertyStorage();
            var array = GsonHelper.getAsJsonArray(json, "properties");
            for (int i = 0; i < array.size(); ++i) {
                var object = array.get(i).getAsJsonObject();
                var key = RecipePropertyRegistry.RECIPE_PROPERTIES_BUILTIN.get().getValue(new ResourceLocation(GsonHelper.getAsString(object, "type")));
                if(key == null) throw new IllegalStateException("key " + GsonHelper.getAsString(object, "type") + " was invalid!");
                storage.store(key, object);
            }
            return storage;
        }
        return EmptyRecipePropertyStorage.INSTANCE;
    }
}
