package net.nemezanevem.gregtech.api.util;

import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CustomModPriorityComparator implements Comparator<Item> {

    private final List<String> modPriorityList;

    public CustomModPriorityComparator(List<String> modPriorities) {
        this.modPriorityList = new ArrayList<>(modPriorities);
    }

    @Override
    public int compare(Item first, Item second) {
        String firstModId = Util.getId(first).getNamespace();
        String secondModId = Util.getId(second).getNamespace();
        int firstModIndex = modPriorityList.indexOf(firstModId);
        int secondModIndex = modPriorityList.indexOf(secondModId);
        if (firstModIndex == -1 && secondModIndex == -1) {
            //if both mod ids are not in mod priority list, compare them alphabetically
            return firstModId.compareTo(secondModId);
        } else if (firstModIndex == -1) {
            //if first mod is not in priority list, it has lower priority than second
            return -1;
        } else if (secondModIndex == -1) {
            //if second mod is not in priority list, it has lower priority than first
            return 1;
        } else {
            //otherwise, both mods are in priority list, so compare their indexes
            //we invert compare arguments, because lower index should have higher priority
            return Integer.compare(secondModIndex, firstModIndex);
        }
    }

}