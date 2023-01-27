package net.nemezanevem.gregtech.api.recipe.ingredient.nbtmatch;

import net.minecraft.nbt.ListTag;
import net.nemezanevem.gregtech.GregTech;

import java.util.Objects;

/**
 * This class is used to check if a NBT tag matches a condition, not necessarily matching the original item tag
 */
public class TagCondition {

    public static final TagCondition ANY = new TagCondition(); // Special-case

    public static TagCondition create(TagType tagType, String nbtKey, Object value) {
        if (tagType == TagType.LIST) {
            throw new IllegalArgumentException("Use ListNBTCondition::create instead of NBTCondition::create");
        }
        return new TagCondition(tagType, nbtKey, value);
    }

    public final TagType tagType;
    public final String nbtKey;
    public final Object value;

    private TagCondition() {
        this.tagType = null;
        this.nbtKey = null;
        this.value = null;
    }

    protected TagCondition(TagType tagType, String nbtKey, Object value) {
        this.tagType = tagType;
        this.nbtKey = nbtKey;
        this.value = value;
        if (tagType == null || nbtKey == null || value == null) {
            GregTech.LOGGER.error("NBTCondition must not have null parameters.");
            GregTech.LOGGER.error("Stacktrace:", new IllegalArgumentException());
        }
    }

    @Override
    public String toString() {
        return nbtKey + ": " + value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagType, nbtKey, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof TagCondition tagCondition) {
            return this.tagType == tagCondition.tagType && this.nbtKey.equals(tagCondition.nbtKey) && this.value.equals(tagCondition.value);
        }
        return false;
    }
}
