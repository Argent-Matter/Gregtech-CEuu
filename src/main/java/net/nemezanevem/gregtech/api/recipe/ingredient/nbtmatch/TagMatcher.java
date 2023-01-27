package net.nemezanevem.gregtech.api.recipe.ingredient.nbtmatch;

import net.minecraft.nbt.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * This class is used to match NBT tags. Used to match a MapItemStackNBTIngredient NBT tag to a given NBT tag value.
 */
public interface TagMatcher {

    static boolean hasKey(CompoundTag tag, String key, int tagType) {
        if (tag != null) {
            return tag.contains(key, tagType);
        }
        return false;
    }

    /**
     * Return true without checking if the NBT actually tags match or exists.
     */
    TagMatcher ANY = (tag, condition) -> true;

    /**
     * Return true if tag has an entry where the value is less than the condition's value
     */
    TagMatcher LESS_THAN = (tag, condition) -> {
        if (hasKey(tag, condition.nbtKey, condition.tagType.typeId)) {
            if (TagType.isNumeric(condition.tagType)) {
                return tag.getLong(condition.nbtKey) < (long) condition.value;
            }
        }
        return false;
    };

    /**
     * Return true if tag has an entry where the value is less than or equal to the condition's value
     */
    TagMatcher LESS_THAN_OR_EQUAL_TO = (tag, condition) -> {
        if (hasKey(tag, condition.nbtKey, condition.tagType.typeId)) {
            if (TagType.isNumeric(condition.tagType)) {
                return tag.getLong(condition.nbtKey) <= (long) condition.value;
            }
        }
        return false;
    };

    /**
     * Return true if tag has an entry where the value is greater than the condition's value
     */
    TagMatcher GREATER_THAN = (tag, condition) -> {
        if (hasKey(tag, condition.nbtKey, condition.tagType.typeId)) {
            if (TagType.isNumeric(condition.tagType)) {
                return tag.getLong(condition.nbtKey) > (long) condition.value;
            }
        }
        return false;
    };

    /**
     * Return true if tag has an entry where the value is greater than or equal to the condition's value
     */
    TagMatcher GREATER_THAN_OR_EQUAL_TO = (tag, condition) -> {
        if (hasKey(tag, condition.nbtKey, condition.tagType.typeId)) {
            if (TagType.isNumeric(condition.tagType)) {
                return tag.getLong(condition.nbtKey) >= (long) condition.value;
            }
        }
        return false;
    };

    /**
     * Return true if tag has an entry where the value is equal to the condition's value
     */
    TagMatcher EQUAL_TO = (tag, condition) -> {
        if (hasKey(tag, condition.nbtKey, condition.tagType.typeId)) {
            if (TagType.isNumeric(condition.tagType)) {
                return tag.getLong(condition.nbtKey) == (long) condition.value;
            }
            switch (condition.tagType) {
                case BYTE_ARRAY:
                    return tag.getByteArray(condition.nbtKey).equals(condition.value);
                case STRING:
                    return tag.getString(condition.nbtKey).equals(condition.value);
                case LIST:
                    if (condition instanceof ListTagCondition) {
                        return tag.getList(condition.nbtKey, ((ListTagCondition) condition).listTagType.typeId).list.equals(condition.value);
                    } else {
                        return false;
                    }
                case COMPOUND:
                    return tag.getCompound(condition.nbtKey).equals(condition.value);
                case INT_ARRAY:
                    return tag.getIntArray(condition.nbtKey).equals(condition.value);
                case LONG_ARRAY:
                    return ((LongArrayTag) tag.get(condition.nbtKey)).getAsLongArray().equals(condition.value);
            }
        }
        return false;
    };

    /**
     * Return true if NBT isn't present or the value matches with the default value in the tag.
     */
    TagMatcher NOT_PRESENT_OR_DEFAULT = (tag, condition) -> {
        if (tag == null) {
            return true;
        }
        if (TagType.isNumeric(condition.tagType)) {
            return tag.getLong(condition.nbtKey) == 0;
        }
        switch (condition.tagType) {
            case BYTE_ARRAY:
                return tag.getByteArray(condition.nbtKey).length == 0;
            case STRING:
                return tag.getString(condition.nbtKey).isEmpty();
            case LIST:
                if (condition instanceof ListTagCondition) {
                    return tag.getList(condition.nbtKey, ((ListTagCondition) condition).listTagType.typeId).isEmpty();
                } else {
                    return false;
                }
            case COMPOUND:
                return tag.getCompound(condition.nbtKey).isEmpty();
            case INT_ARRAY:
                return tag.getIntArray(condition.nbtKey).length == 0;
            case LONG_ARRAY:
                return ((LongArrayTag) tag.get(condition.nbtKey)).size() == 0;
        }
        return false;
    };

    boolean evaluate(CompoundTag nbtTagCompound, TagCondition nbtCondition);

    default boolean evaluate(ItemStack stack, TagCondition nbtCondition) {
        return evaluate(stack.getTag(), nbtCondition);
    }

    default boolean evaluate(FluidStack stack, TagCondition nbtCondition) {
        return evaluate(stack.getTag(), nbtCondition);
    }

}
