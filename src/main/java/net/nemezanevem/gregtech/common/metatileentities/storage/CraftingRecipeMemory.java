package net.nemezanevem.gregtech.common.metatileentities.storage;

import gregtech.api.util.Util;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import static gregtech.api.util.Util.copyInventoryItems;

public class CraftingRecipeMemory {

    private final MemorizedRecipe[] memorizedRecipes;

    public CraftingRecipeMemory(int memorySize) {
        this.memorizedRecipes = new MemorizedRecipe[memorySize];
    }

    public void loadRecipe(int index, IItemHandlerModifiable craftingGrid) {
        MemorizedRecipe recipe = memorizedRecipes[index];
        if (recipe != null) {
            copyInventoryItems(recipe.craftingMatrix, craftingGrid);
        }
    }

    @Nullable
    public MemorizedRecipe getRecipeAtIndex(int index) {
        return memorizedRecipes[index];
    }

    private boolean isNullOrUnlockedRecipe(int index) {
        return memorizedRecipes[index] == null ||
                !memorizedRecipes[index].recipeLocked;
    }

    private void insertRecipe(MemorizedRecipe insertedRecipe, int startIndex) {
        MemorizedRecipe currentRecipe = memorizedRecipes[startIndex];
        for (int i = startIndex + 1; i < memorizedRecipes.length; i++) {
            MemorizedRecipe recipe = memorizedRecipes[i];
            if (recipe != null && recipe.recipeLocked) continue;
            memorizedRecipes[i] = currentRecipe;
            currentRecipe = recipe;
        }
        memorizedRecipes[startIndex] = insertedRecipe;
    }

    private MemorizedRecipe findOrCreateRecipe(ItemStack itemStack) {
        Optional<MemorizedRecipe> result = Arrays.stream(memorizedRecipes)
                .filter(Objects::nonNull)
                .filter(recipe -> ItemStack.areItemStacksEqual(recipe.recipeResult, itemStack))
                .findFirst();
        return result.orElseGet(() -> {
            MemorizedRecipe recipe = new MemorizedRecipe();
            recipe.recipeResult = itemStack.copy();
            int firstFreeIndex = Util.indices(memorizedRecipes)
                    .filter(this::isNullOrUnlockedRecipe)
                    .findFirst().orElse(-1);
            if (firstFreeIndex == -1) {
                return null;
            }
            insertRecipe(recipe, firstFreeIndex);
            return recipe;
        });
    }

    public void notifyRecipePerformed(IItemHandler craftingGrid, ItemStack resultStack) {
        MemorizedRecipe recipe = findOrCreateRecipe(resultStack);
        if (recipe != null) {
            recipe.updateCraftingMatrix(craftingGrid);
            recipe.timesUsed++;
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag tagCompound = new CompoundTag();
        NBTTagList resultList = new NBTTagList();
        tagCompound.put("Memory", resultList);
        for (int i = 0; i < memorizedRecipes.length; i++) {
            MemorizedRecipe recipe = memorizedRecipes[i];
            if (recipe == null) continue;
            CompoundTag entryComponent = new CompoundTag();
            entryComponent.putInt("Slot", i);
            entryComponent.put("Recipe", recipe.serializeNBT());
            resultList.appendTag(entryComponent);
        }
        return tagCompound;
    }

    public void deserializeNBT(CompoundTag tagCompound) {
        NBTTagList resultList = tagCompound.getTagList("Memory", NBT.TAG_COMPOUND);
        for (int i = 0; i < resultList.tagCount(); i++) {
            CompoundTag entryComponent = resultList.getCompoundAt(i);
            int slotIndex = entryComponent.getInt("Slot");
            MemorizedRecipe recipe = MemorizedRecipe.deserializeNBT(entryComponent.getCompound("Recipe"));
            this.memorizedRecipes[slotIndex] = recipe;
        }
    }

    public static class MemorizedRecipe {
        private final ItemStackHandler craftingMatrix = new ItemStackHandler(9);
        private ItemStack recipeResult;
        private boolean recipeLocked = false;
        private int timesUsed = 0;

        private MemorizedRecipe() {
        }

        private CompoundTag serializeNBT() {
            CompoundTag result = new CompoundTag();
            result.put("Result", recipeResult.serializeNBT());
            result.put("Matrix", craftingMatrix.serializeNBT());
            result.putBoolean("Locked", recipeLocked);
            result.putInt("TimesUsed", timesUsed);
            return result;
        }

        private static MemorizedRecipe deserializeNBT(CompoundTag tagCompound) {
            MemorizedRecipe recipe = new MemorizedRecipe();
            recipe.recipeResult = new ItemStack(tagCompound.getCompound("Result"));
            recipe.craftingMatrix.deserializeNBT(tagCompound.getCompound("Matrix"));
            recipe.recipeLocked = tagCompound.getBoolean("Locked");
            recipe.timesUsed = tagCompound.getInt("TimesUsed");
            return recipe;
        }

        private void updateCraftingMatrix(IItemHandler craftingGrid) {
            //do not modify crafting grid for locked recipes
            if (!recipeLocked) {
                copyInventoryItems(craftingGrid, craftingMatrix);
            }
        }

        public ItemStack getRecipeResult() {
            return recipeResult;
        }

        public boolean isRecipeLocked() {
            return recipeLocked;
        }

        public void setRecipeLocked(boolean recipeLocked) {
            this.recipeLocked = recipeLocked;
        }
    }

}
