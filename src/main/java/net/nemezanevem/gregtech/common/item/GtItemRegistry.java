package net.nemezanevem.gregtech.common.item;

import com.google.common.base.CaseFormat;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.item.materialitem.PrefixItem;
import net.nemezanevem.gregtech.api.item.metaitem.MetaItem;
import net.nemezanevem.gregtech.api.registry.material.MaterialRegistry;
import net.nemezanevem.gregtech.api.unification.tag.TagPrefix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GtItemRegistry {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, GregTech.MODID);

    private static final Map<TagKey<Item>, List<Holder<Item>>> itemTagMap = new HashMap<>();

    public static void addTagToItem(Item item, String tagPath) {
        itemTagMap.put(TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), new ResourceLocation("forge", tagPath)), List.of(ForgeRegistries.ITEMS.getHolder(item).get()));
    }

    public static void register() {
        PrefixItem.registerItems(ITEMS);
    }

    public static Map<TagKey<Item>, List<Holder<Item>>> getItemTagMap() {
        return itemTagMap;
    }
}
