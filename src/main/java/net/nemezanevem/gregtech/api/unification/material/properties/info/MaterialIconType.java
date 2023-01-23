package net.nemezanevem.gregtech.api.unification.material.properties.info;

import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.fml.loading.FMLLoader;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.registry.material.info.MaterialIconTypeRegistry;

import javax.annotation.Nonnull;
import java.io.IOException;

public class MaterialIconType {

    private static final Table<MaterialIconType, MaterialIconSet, ResourceLocation> ITEM_MODEL_CACHE = HashBasedTable.create();
    private static final Table<MaterialIconType, MaterialIconSet, ResourceLocation> BLOCK_TEXTURE_CACHE = HashBasedTable.create();

    public final String name;

    public MaterialIconType(String name) {
        this.name = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
    }

    @Nonnull
    public ResourceLocation getBlockTexturePath(@Nonnull MaterialIconSet materialIconSet) {
        if (BLOCK_TEXTURE_CACHE.contains(this, materialIconSet)) {
            return BLOCK_TEXTURE_CACHE.get(this, materialIconSet);
        }

        MaterialIconSet iconSet = materialIconSet;
        //noinspection ConstantConditions
        if (!iconSet.isRootIconset && FMLLoader.getDist().isClient() &&
                Minecraft.getInstance() != null) { // check minecraft for null for CI environments
            ResourceManager manager = Minecraft.getInstance().getResourceManager();
            while (!iconSet.isRootIconset) {
                // check if the texture file exists
                if(manager.getResource(new ResourceLocation(GregTech.MODID, String.format("textures/blocks/material_sets/%s/%s.png", iconSet.name, this.name))).isPresent()) iconSet = iconSet.parentIconset;
            }
        }
        ResourceLocation location = new ResourceLocation(GregTech.MODID, String.format("blocks/material_sets/%s/%s", iconSet.name, this.name));
        BLOCK_TEXTURE_CACHE.put(this, materialIconSet, location);

        return location;
    }

    @Nonnull
    public ResourceLocation getItemModelPath(@Nonnull MaterialIconSet materialIconSet) {
        if (ITEM_MODEL_CACHE.contains(this, materialIconSet)) {
            return ITEM_MODEL_CACHE.get(this, materialIconSet);
        }

        MaterialIconSet iconSet = materialIconSet;
        //noinspection ConstantConditions
        if (!iconSet.isRootIconset && FMLLoader.getDist().isClient() &&
                Minecraft.getInstance() != null) { // check minecraft for null for CI environments
            ResourceManager manager = Minecraft.getInstance().getResourceManager();
            while (!iconSet.isRootIconset) {
                // check if the model file exists
                if(manager.getResource(new ResourceLocation(GregTech.MODID, String.format("models/item/material_sets/%s/%s.json", iconSet.name, this.name))).isPresent()) iconSet = iconSet.parentIconset;
            }
        }

        ResourceLocation location = new ResourceLocation(GregTech.MODID, String.format("material_sets/%s/%s", iconSet.name, this.name));
        ITEM_MODEL_CACHE.put(this, materialIconSet, location);

        return location;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
