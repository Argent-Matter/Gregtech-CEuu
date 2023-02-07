package net.nemezanevem.gregtech.common.covers.filter;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.nemezanevem.gregtech.common.covers.filter.ItemFilter;
import net.nemezanevem.gregtech.common.item.metaitem.MetaItems;

import java.util.Map;

public class FilterTypeRegistry {

    private static final Map<Item, Integer> itemFilterIdByStack = new Object2IntOpenHashMap<>();
    private static final Map<Item, Integer> fluidFilterIdByStack = new Object2IntOpenHashMap<>();
    private static final BiMap<Integer, Class<? extends ItemFilter>> itemFilterById = HashBiMap.create();
    private static final BiMap<Integer, Class<? extends FluidFilter>> fluidFilterById = HashBiMap.create();

    public static void init() {
        registerFluidFilter(1, SimpleFluidFilter.class, MetaItems.FLUID_FILTER.getStackForm());
        registerItemFilter(2, SimpleItemFilter.class, MetaItems.ITEM_FILTER.getStackForm());
        registerItemFilter(3, OreDictionaryItemFilter.class, MetaItems.ORE_DICTIONARY_FILTER.getStackForm());
        registerItemFilter(4, SmartItemFilter.class, MetaItems.SMART_FILTER.getStackForm());
    }

    public static void registerFluidFilter(int id, Class<? extends FluidFilter> fluidFilterClass, ItemStack itemStack) {
        if (fluidFilterById.containsKey(id)) {
            throw new IllegalArgumentException("Id is already occupied: " + id);
        }
        fluidFilterIdByStack.put(new ItemAndMetadata(itemStack), id);
        fluidFilterById.put(id, fluidFilterClass);
    }

    public static void registerItemFilter(int id, Class<? extends ItemFilter> itemFilterClass, ItemStack itemStack) {
        if (itemFilterById.containsKey(id)) {
            throw new IllegalArgumentException("Id is already occupied: " + id);
        }
        itemFilterIdByStack.put(new ItemAndMetadata(itemStack), id);
        itemFilterById.put(id, itemFilterClass);
    }

    public static int getIdForItemFilter(ItemFilter itemFilter) {
        Integer filterId = itemFilterById.inverse().get(itemFilter.getClass());
        if (filterId == null) {
            throw new IllegalArgumentException("Unknown filter type " + itemFilter.getClass());
        }
        return filterId;
    }

    public static int getIdForFluidFilter(FluidFilter fluidFilter) {
        Integer filterId = fluidFilterById.inverse().get(fluidFilter.getClass());
        if (filterId == null) {
            throw new IllegalArgumentException("Unknown filter type " + fluidFilter.getClass());
        }
        return filterId;
    }

    public static ItemFilter createItemFilterById(int filterId) {
        Class<? extends ItemFilter> filterClass = itemFilterById.get(filterId);
        if (filterClass == null) {
            throw new IllegalArgumentException("Unknown filter id: " + filterId);
        }
        return createNewFilterInstance(filterClass);
    }

    public static FluidFilter createFluidFilterById(int filterId) {
        Class<? extends FluidFilter> filterClass = fluidFilterById.get(filterId);
        if (filterClass == null) {
            throw new IllegalArgumentException("Unknown filter id: " + filterId);
        }
        return createNewFilterInstance(filterClass);
    }

    public static ItemFilter getItemFilterForStack(ItemStack itemStack) {
        Integer filterId = itemFilterIdByStack.get(new ItemAndMetadata(itemStack));
        if (filterId == null) {
            return null;
        }
        Class<? extends ItemFilter> filterClass = itemFilterById.get(filterId);
        return createNewFilterInstance(filterClass);
    }

    public static FluidFilter getFluidFilterForStack(ItemStack itemStack) {
        Integer filterId = fluidFilterIdByStack.get(new ItemAndMetadata(itemStack));
        if (filterId == null) {
            return null;
        }
        Class<? extends FluidFilter> filterClass = fluidFilterById.get(filterId);
        return createNewFilterInstance(filterClass);
    }

    private static <T> T createNewFilterInstance(Class<T> filterClass) {
        try {
            return filterClass.newInstance();
        } catch (ReflectiveOperationException exception) {
            GregTech.LOGGER.error("Failed to create filter instance for class {}", filterClass, exception);
            return null;
        }
    }
}