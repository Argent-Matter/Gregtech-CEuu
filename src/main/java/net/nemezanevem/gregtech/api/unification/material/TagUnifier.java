package net.nemezanevem.gregtech.api.unification.material;

import com.google.common.base.Joiner;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
import net.nemezanevem.gregtech.api.registry.material.MaterialRegistry;
import net.nemezanevem.gregtech.api.unification.material.properties.GtMaterialProperties;
import net.nemezanevem.gregtech.api.unification.stack.*;
import net.nemezanevem.gregtech.api.unification.tag.TagPrefix;
import net.nemezanevem.gregtech.api.util.CustomModPriorityComparator;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.common.item.GtItemRegistry;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.nemezanevem.gregtech.api.util.GTValues.M;

public class TagUnifier {

    private TagUnifier() {
    }

    //simple version of material registry for marker materials
    private static final Map<String, MarkerMaterial> markerMaterialRegistry = new Object2ObjectOpenHashMap<>();
    private static final Map<Item, ItemMaterialInfo> materialUnificationInfo = new Object2ObjectOpenHashMap<>();
    private static final Map<Item, UnificationEntry> stackUnificationInfo = new Object2ObjectOpenHashMap<>();
    private static final Map<UnificationEntry, ArrayList<Item>> stackUnificationItems = new Object2ObjectOpenHashMap<>();
    private static final Map<Item, Set<String>> stackTagName = new Object2ObjectOpenHashMap<>();
    private static final Map<String, List<Item>> oreDictNameStacks = new Object2ObjectOpenHashMap<>();

    @Nullable
    private static Comparator<Item> stackComparator;

    public static Comparator<Item> getSimpleItemStackComparator() {
        if (stackComparator == null) {
            List<String> modPriorities = Arrays.asList(ConfigHolder.compat.modPriorities);
            if (modPriorities.isEmpty()) {
                //noinspection ConstantConditions
                Function<Item, String> modIdExtractor = item -> Util.getId(item).getNamespace();
                stackComparator = Comparator.comparing(modIdExtractor);
            } else {
                stackComparator = Collections.reverseOrder(new CustomModPriorityComparator(modPriorities));
            }
        }
        return stackComparator;
    }

    public static Comparator<Item> getItemStackComparator() {
        Comparator<Item> comparator = getSimpleItemStackComparator();
        return comparator::compare;
    }

    public static void registerMarkerMaterial(MarkerMaterial markerMaterial) {
        if (markerMaterialRegistry.containsKey(markerMaterial.toString())) {
            throw new IllegalArgumentException(("Marker material with id " + markerMaterial.toString() + " is already registered!"));
        }
        markerMaterialRegistry.put(markerMaterial.toString(), markerMaterial);
    }

    public static void registerTag(Item item, ItemMaterialInfo materialInfo) {
        materialUnificationInfo.put(item, materialInfo);
    }

    public static void registerTag(Item item, TagPrefix tagPrefix, @Nullable Material material) {
        registerTag(item, tagPrefix.name(), material);
    }

    public static void registerTag(Item item, String customTagPrefix, @Nullable Material material) {
        GtItemRegistry.addTagToItem(item, customTagPrefix + "/" + material.toLowerUnderscoreString());
        //TagManager.registerOre(customTagPrefix + (material == null ? "" : material.toLowerUnderscoreString()), item);
    }

    public static void registerTag(Item item, String tag) {
        GtItemRegistry.addTagToItem(item, tag);
        //ForgeRegistries.ITEMS.tags()..registerOre(tag, item);
    }

    public static void init() {
        for (TagKey<Item> registeredTagName : ForgeRegistries.ITEMS.tags().getTagNames().toList()) {
            List<Item> theseOres = ForgeRegistries.ITEMS.tags().getTag(registeredTagName).stream().toList();
            for (Item item : theseOres) {
                onItemRegistration(registeredTagName, item);
            }
        }
        MinecraftForge.EVENT_BUS.register(TagUnifier.class);
    }

    public static void onItemRegistration(TagKey<Item> tagKey, Item item) {
        String oreName = tagKey.location().toString();
        //cache this registration by name
        stackTagName.computeIfAbsent(item, k -> new HashSet<>()).add(oreName);
        List<Item> itemStackListForOreDictName = oreDictNameStacks.computeIfAbsent(oreName, k -> new ArrayList<>());
        addAndSort(itemStackListForOreDictName, item, getItemStackComparator());

        //and try to transform registration name into TagPrefix + Material pair
        TagPrefix tagPrefix = TagPrefix.getPrefix(oreName);
        Material material = null;
        if (tagPrefix == null) {
            //split ore dict name to parts
            //oreBasalticMineralSand -> ore, Basaltic, Mineral, Sand
            ArrayList<String> splits = new ArrayList<>();
            StringBuilder builder = new StringBuilder();
            for (char character : oreName.toCharArray()) {
                if (Character.isUpperCase(character)) {
                    if (builder.length() > 0) {
                        splits.add(builder.toString());
                        builder = new StringBuilder().append(character);
                    } else splits.add(Character.toString(character));
                } else builder.append(character);
            }
            if (builder.length() > 0) {
                splits.add(builder.toString());
            }
            //try to combine in different manners
            //oreBasaltic MineralSand , ore BasalticMineralSand
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < splits.size(); i++) {
                buffer.append(splits.get(i));
                TagPrefix maybePrefix = TagPrefix.getPrefix(buffer.toString()); //ore -> TagPrefix.ore
                String possibleMaterialName = Joiner.on("").join(splits.subList(i + 1, splits.size())); //BasalticMineralSand
                String underscoreName = Util.toLowerCaseUnderscore(possibleMaterialName); //basaltic_mineral_sand
                Material possibleMaterial = MaterialRegistry.MATERIALS_BUILTIN.get().getValue(new ResourceLocation(underscoreName)); //Materials.BasalticSand
                if (possibleMaterial == null) {
                    //if we didn't found real material, try using marker material registry
                    possibleMaterial = markerMaterialRegistry.get(underscoreName);
                }
                if (maybePrefix != null && possibleMaterial != null) {
                    tagPrefix = maybePrefix;
                    material = possibleMaterial;
                    break;
                }
            }
        }

        //finally register item
        if (tagPrefix != null && (material != null || tagPrefix.isSelfReferencing)) {
            UnificationEntry unificationEntry = new UnificationEntry(tagPrefix, material);
            ArrayList<Item> itemListForUnifiedEntry = stackUnificationItems.computeIfAbsent(unificationEntry, p -> new ArrayList<>());
            addAndSort(itemListForUnifiedEntry, item, getSimpleItemStackComparator());

            if (!unificationEntry.tagPrefix.isMarkerPrefix()) {
                stackUnificationInfo.put(item, unificationEntry);
            }
            tagPrefix.processTagRegistration(material);
        }
    }

    public static Set<String> getOreDictionaryNames(Item itemStack) {
        if (stackTagName.containsKey(itemStack))
            return Collections.unmodifiableSet(stackTagName.get(itemStack));
        return Collections.emptySet();
    }

    public static List<Item> getAllWithTagName(String tagName) {
        return new ArrayList<>(oreDictNameStacks.get(tagName));
    }

    @Nullable
    public static MaterialStack getMaterial(Item item) {
        UnificationEntry entry = stackUnificationInfo.get(item);
        if (entry != null) {
            Material entryMaterial = entry.material;
            if (entryMaterial == null) {
                entryMaterial = entry.tagPrefix.materialType;
            }
            if (entryMaterial != null) {
                return new MaterialStack(entryMaterial, entry.tagPrefix.getMaterialAmount(entryMaterial));
            }
        }
        ItemMaterialInfo info = materialUnificationInfo.get(item);
        return info == null ? null : info.getMaterial().copy();
    }

    @Nullable
    public static TagPrefix getPrefix(Item item) {
        UnificationEntry entry = stackUnificationInfo.get(item);
        if (entry != null) return entry.tagPrefix;
        return null;
    }

    public static TagPrefix getPrefix(Block block) {
        return getPrefix(block.asItem());
    }

    @Nullable
    public static UnificationEntry getUnificationEntry(Item item) {
        return stackUnificationInfo.get(item);
    }

    public static Item getUnificated(Item item) {
        UnificationEntry unificationEntry = stackUnificationInfo.get(item);
        if (unificationEntry == null || !stackUnificationItems.containsKey(unificationEntry) || !unificationEntry.tagPrefix.isUnificationEnabled)
            return item;
        ArrayList<Item> keys = stackUnificationItems.get(unificationEntry);
        return keys.size() > 0 ? keys.get(0) : item;
    }

    public static Item get(UnificationEntry unificationEntry) {
        if (!stackUnificationItems.containsKey(unificationEntry))
            return null;
        ArrayList<Item> keys = stackUnificationItems.get(unificationEntry);
        return keys.size() > 0 ? keys.get(0) : null;
    }

    public static Item get(TagPrefix TagPrefix, Material material) {
        UnificationEntry unificationEntry = new UnificationEntry(TagPrefix, material);
        if (!stackUnificationItems.containsKey(unificationEntry))
            return null;
        ArrayList<Item> keys = stackUnificationItems.get(unificationEntry);
        return keys.size() > 0 ? keys.get(0) : null;
    }

    public static Item get(String oreDictName) {
        List<Item> itemStacks = oreDictNameStacks.get(oreDictName);
        if (itemStacks == null || itemStacks.size() == 0) return null;
        return itemStacks.get(0);
    }

    public static List<Map.Entry<Item, ItemMaterialInfo>> getAllItemInfos() {
        return materialUnificationInfo.entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public static List<Item> getAll(UnificationEntry unificationEntry) {
        if (!stackUnificationItems.containsKey(unificationEntry))
            return Collections.emptyList();
        ArrayList<Item> keys = stackUnificationItems.get(unificationEntry);
        return new ArrayList<>(keys);
    }

    public static Item getDust(Material material, long materialAmount) {
        if (!material.hasProperty(GtMaterialProperties.DUST.get()) || materialAmount <= 0)
            return null;
        if (materialAmount % M == 0 || materialAmount >= M * 16)
            return get(TagPrefix.dust, material);
        else if ((materialAmount * 4) % M == 0 || materialAmount >= M * 8)
            return get(TagPrefix.dustSmall, material);
        else if ((materialAmount * 9) >= M)
            return get(TagPrefix.dustTiny, material);
        return null;
    }

    public static Item getDust(MaterialStack materialStack) {
        return getDust(materialStack.material, materialStack.amount);
    }

    public static Item getIngot(Material material, long materialAmount) {
        if (!material.hasProperty(GtMaterialProperties.INGOT.get()) || materialAmount <= 0)
            return null;
        if (materialAmount % (M * 9) == 0)
            return get(TagPrefix.block, material);
        if (materialAmount % M == 0 || materialAmount >= M * 16)
            return get(TagPrefix.ingot, material);
        else if ((materialAmount * 9) >= M)
            return get(TagPrefix.nugget, material);
        return null;
    }

    public static Item getIngot(MaterialStack materialStack) {
        return getIngot(materialStack.material, materialStack.amount);
    }

    /**
     * Returns an Ingot of the material if it exists. Otherwise it returns a Dust.
     * Returns ItemStack.EMPTY if neither exist.
     */
    public static Item getIngotOrDust(Material material, long materialAmount) {
        Item ingotStack = getIngot(material, materialAmount);
        if (ingotStack != null) return ingotStack;
        return getDust(material, materialAmount);
    }

    public static Item getIngotOrDust(MaterialStack materialStack) {
        return getIngotOrDust(materialStack.material, materialStack.amount);
    }

    public static Item getGem(MaterialStack materialStack) {
        if (materialStack.material.hasProperty(GtMaterialProperties.GEM.get())
                && !TagPrefix.gem.isIgnored(materialStack.material)
                && materialStack.amount == TagPrefix.gem.getMaterialAmount(materialStack.material)) {
            return get(TagPrefix.gem, materialStack.material);
        }
        return null;
    }

    synchronized private static <T> void addAndSort(List<T> list, T itemToAdd, Comparator<T> comparator) {
        list.add(itemToAdd);

        if (list.size() > 1)
            list.sort(comparator);
    }
}
