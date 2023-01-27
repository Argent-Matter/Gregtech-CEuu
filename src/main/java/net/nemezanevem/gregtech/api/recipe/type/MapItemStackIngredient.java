package net.nemezanevem.gregtech.api.recipe.type;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.nemezanevem.gregtech.api.recipe.ingredient.ExtendedIngredient;
import net.nemezanevem.gregtech.api.util.Util;

import java.util.Collection;

public class MapItemStackIngredient extends AbstractMapIngredient {


    protected ItemStack stack;
    protected CompoundTag tag;
    protected ExtendedIngredient gtRecipeInput = null;

    public MapItemStackIngredient(ItemStack stack, CompoundTag tag) {
        this.stack = stack;
        this.tag = tag;
    }

    public MapItemStackIngredient(ItemStack stack, ExtendedIngredient gtRecipeInput) {
        this.stack = stack;
        this.tag = stack.getTag();
        this.gtRecipeInput = gtRecipeInput;
    }

    public static Collection<AbstractMapIngredient> from(ExtendedIngredient r) {
        ObjectArrayList<AbstractMapIngredient> list = new ObjectArrayList<>();
        for (ItemStack s : r.getItems()) {
            list.add(new MapItemStackIngredient(s,r));
        }
        return list;
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            MapItemStackIngredient other = (MapItemStackIngredient) o;
            return other.gtRecipeInput.test(this.stack);
        }
        return false;
    }

    @Override
    protected int hash() {
        int hash = stack.getItem().hashCode() * 31;
        hash += 31 * (this.tag != null ? this.tag.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "MapItemStackIngredient{" + "item=" + Util.getId(stack.getItem()) + "} {tag=" + tag + "}";
    }
}
