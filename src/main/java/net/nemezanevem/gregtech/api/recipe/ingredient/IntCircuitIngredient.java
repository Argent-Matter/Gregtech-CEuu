package net.nemezanevem.gregtech.api.recipe.ingredient;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.nemezanevem.gregtech.api.item.gui.PlayerInventoryHolder;
import net.nemezanevem.gregtech.common.item.metaitem.MetaItems;

import javax.annotation.Nullable;

public class IntCircuitIngredient extends ExtendedIngredient {

    public static final int CIRCUIT_MAX = 32;
    private final int matchingConfigurations;

    @Override
    protected IntCircuitIngredient copy() {
        return new IntCircuitIngredient(this.matchingConfigurations, this.isConsumable);
    }

    public IntCircuitIngredient(int matchingConfigurations) {
        super(getIntegratedCircuit(matchingConfigurations), false);
        this.matchingConfigurations = matchingConfigurations;
    }

    public IntCircuitIngredient(int matchingConfigurations, boolean isConsumable) {
        super(getIntegratedCircuit(matchingConfigurations), isConsumable);
        this.matchingConfigurations = matchingConfigurations;
    }

    public static ItemStack getIntegratedCircuit(int configuration) {
        ItemStack stack = new ItemStack(MetaItems.INTEGRATED_CIRCUIT.get(), 1);
        setCircuitConfiguration(stack, configuration);
        return stack;
    }

    public static void setCircuitConfiguration(ItemStack itemStack, int configuration) {
        if (!itemStack.is(MetaItems.INTEGRATED_CIRCUIT.get()))
            throw new IllegalArgumentException("Given item stack is not an integrated circuit!");
        if (configuration < 0 || configuration > CIRCUIT_MAX)
            throw new IllegalArgumentException("Given configuration number is out of range!");
        CompoundTag tagCompound = itemStack.getOrCreateTag();
        tagCompound.putInt("Configuration", configuration);
    }

    public static int getCircuitConfiguration(ItemStack itemStack) {
        if (!isIntegratedCircuit(itemStack)) return 0;
        CompoundTag tagCompound = itemStack.getTag();
        return tagCompound.getInt("Configuration");
    }

    public static boolean isIntegratedCircuit(ItemStack itemStack) {
        boolean isCircuit = itemStack.is(MetaItems.INTEGRATED_CIRCUIT.get());
        if (isCircuit && !itemStack.hasTag()) {
            CompoundTag compound = new CompoundTag();
            compound.putInt("Configuration", 0);
            itemStack.setTag(compound);
        }
        return isCircuit;
    }

    public static void adjustConfiguration(PlayerInventoryHolder holder, int amount) {
        adjustConfiguration(holder.getCurrentItem(), amount);
        holder.markAsDirty();
    }

    public static void adjustConfiguration(ItemStack stack, int amount) {
        if (!IntCircuitIngredient.isIntegratedCircuit(stack)) return;
        int configuration = IntCircuitIngredient.getCircuitConfiguration(stack);
        configuration += amount;
        configuration = Mth.clamp(configuration, 0, IntCircuitIngredient.CIRCUIT_MAX);
        IntCircuitIngredient.setCircuitConfiguration(stack, configuration);
    }

    @Override
    public boolean test(@Nullable ItemStack itemStack) {
        return itemStack != null && itemStack.is(MetaItems.INTEGRATED_CIRCUIT.get()) &&
                matchingConfigurations == getCircuitConfiguration(itemStack);
    }

}
