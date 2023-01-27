package net.nemezanevem.gregtech.api.recipe.ingredient.nbtmatch;

import net.minecraft.nbt.Tag;
import net.nemezanevem.gregtech.GregTech;

import java.util.List;
import java.util.Objects;

public class ListTagCondition extends TagCondition {

    public static ListTagCondition create(TagType listTagType, String nbtKey, List<Tag> value) {
        return new ListTagCondition(listTagType, nbtKey, value);
    }

    public final TagType listTagType;

    protected ListTagCondition(TagType listTagType, String nbtKey, Object value) {
        super(TagType.LIST, nbtKey, value);
        this.listTagType = listTagType;
        if (listTagType == null) {
            GregTech.LOGGER.error("ListNBTCondition must not have null parameters.");
            GregTech.LOGGER.error("Stacktrace:", new IllegalArgumentException());
        }
    }

    @Override
    public String toString() {
        return nbtKey + " (type " + listTagType + ") :" +  value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagType, nbtKey, value, listTagType);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ListTagCondition condition) {
            return this.tagType == condition.tagType && this.nbtKey.equals(condition.nbtKey) && this.value.equals(condition.value) &&
                    this.listTagType == condition.listTagType;
        }
        return false;
    }

}
