package net.nemezanevem.gregtech.common.item;

import com.google.common.base.CaseFormat;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.registry.material.MaterialRegistry;
import net.nemezanevem.gregtech.api.unification.tag.TagPrefix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GtItemRegistry {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, GregTech.MODID);

    private static Map<TagKey<Item>, List<Holder<Item>>> itemTagMap = new HashMap<>();

    public static void addTagToItem(Item item, String tagPath) {
        itemTagMap.put(new TagKey<>(ForgeRegistries.ITEMS.getRegistryKey(), new ResourceLocation("forge", "items/" + tagPath)), List.of(ForgeRegistries.ITEMS.getHolder(item).get()));
    }

    public static void register() {
        for (TagPrefix prefix : TagPrefix.values()) {
            String regName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, prefix.name());
            for(var entry : MaterialRegistry.MATERIALS_BUILTIN.get().getEntries()) {
                if(prefix.doGenerateItem(entry.getValue())) {
                    ITEMS.register(regName + "_" + entry.getKey(), () -> new Item(new Item.Properties()));
                }
            }
        }
    }

    public static Map<TagKey<Item>, List<Holder<Item>>> getItemTagMap() {
        return itemTagMap;
    }
}
