package net.nemezanevem.gregtech.api.recipe.property;

import codechicken.lib.util.ServerUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.fml.loading.FMLLoader;

import java.util.Locale;
import java.util.Map;

public class GasCollectorDimensionProperty extends RecipeProperty<DimensionType[]> {
    public static final String KEY = "dimension";

    public static Cache<DimensionType, ResourceLocation> keysCache = CacheBuilder.newBuilder().weakKeys().weakValues().build();

    private static GasCollectorDimensionProperty INSTANCE;

    private GasCollectorDimensionProperty() {
        super(KEY, DimensionType[].class);
    }

    public static GasCollectorDimensionProperty getInstance() {
        if (INSTANCE == null)
            INSTANCE = new GasCollectorDimensionProperty();
        return INSTANCE;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.font.draw(new PoseStack(), Component.translatable("gregtech.recipe.dimensions",
                getDimensionsForRecipe(castValue(value))), x, y, color);
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer, Object value) {
        var real = this.castValue(value);
        buffer.writeVarInt(real.length);
        for (DimensionType type : real) {
            var loc = getFromCache(type);
            buffer.writeResourceLocation(loc);
        }
    }

    @Override
    public DimensionType[] readFromNetwork(FriendlyByteBuf buffer) {
        var registry = ServerUtils.getServer().registryAccess().registry(Registry.DIMENSION_TYPE_REGISTRY).get();
        int length = buffer.readVarInt();
        DimensionType[] types = new DimensionType[length];
        for(int i = 0; i < length; ++i) {
            types[i] = registry.get(buffer.readResourceLocation());
        }
        return types;
    }

    @Override
    public void writeToJson(JsonObject json, Object value) {
        var real = this.castValue(value);
        JsonArray array = new JsonArray();
        for (DimensionType type : real) {
            var loc = getFromCache(type);
            array.add(loc.toString());
        }
        json.add("dimensions", array);
    }

    @Override
    public DimensionType[] readFromJson(JsonObject json) {
        var registry = ServerUtils.getServer().registryAccess().registry(Registry.DIMENSION_TYPE_REGISTRY).get();
        JsonArray dimensions = GsonHelper.getAsJsonArray(json, "dimensions");
        DimensionType[] types = new DimensionType[dimensions.size()];
        for(int i = 0; i < dimensions.size(); ++i) {
            types[i] = registry.get(new ResourceLocation(dimensions.get(i).getAsString()));
        }
        return types;
    }

    public ResourceLocation getFromCache(DimensionType type) {
        var loc = keysCache.getIfPresent(type);
        if(loc == null) {
            var registry = ServerUtils.getServer().registryAccess().registry(Registry.DIMENSION_TYPE_REGISTRY).get();
            Map<DimensionType, ResourceLocation> dimNames = registry.entrySet().stream().collect(Object2ObjectOpenHashMap::new, (map, entry) -> map.put(entry.getValue(), entry.getKey().location()), Object2ObjectOpenHashMap::putAll);
            keysCache.putAll(dimNames);
            return dimNames.get(type);
        }
        return loc;
    }

    private String getDimensionsForRecipe(DimensionType[] values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            DimensionType dim = values[i];
            ResourceLocation dimId = getFromCache(dim);

            String[] splits = dimId.getPath().split("_");
            builder.append(splits[0].substring(0, 1).toUpperCase(Locale.ROOT)).append(splits[0].substring(1));
            for (int j = 1; j < splits.length; ++j) {
                String str = splits[j].substring(0, 1);

                builder.append(" ").append(str.toUpperCase(Locale.ROOT)).append(splits[j].substring(1));
            }

            builder.append(" (").append(Component.translatable("message.gregtech.from_mod").getString());
            if(dimId.getNamespace().equals("minecraft")) builder.append(Component.translatable("message.gregtech.vanilla_modid").getString());
            else builder.append(FMLLoader.getLoadingModList().getModFileById(dimId.getNamespace()).getMods().get(0).getDisplayName());
            builder.append(')');

            if (i != values.length - 1)
                builder.append(", ");
        }
        String str = builder.toString();

        if (str.length() >= 13) {
            str = str.substring(0, 10) + "..";
        }
        return str;
    }

}
