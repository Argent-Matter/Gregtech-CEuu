package net.nemezanevem.gregtech.api.util;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagManager;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ForgeRegistries;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.capability.IMultipleTankHandler;
import net.nemezanevem.gregtech.api.registry.material.MaterialRegistry;
import net.nemezanevem.gregtech.api.registry.material.info.MaterialFlagRegistry;
import net.nemezanevem.gregtech.api.registry.material.info.MaterialIconSetRegistry;
import net.nemezanevem.gregtech.api.registry.material.properties.MaterialPropertyRegistry;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.properties.PropertyKey;
import net.nemezanevem.gregtech.api.unification.material.properties.info.MaterialFlag;
import net.nemezanevem.gregtech.api.unification.material.properties.info.MaterialIconSet;

import java.text.NumberFormat;
import java.util.AbstractList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import static net.nemezanevem.gregtech.api.GTValues.V;

public class Util {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    private static final NavigableMap<Long, Byte> tierByVoltage = new TreeMap<>();

    static {
        for (int i = 0; i < V.length; i++) {
            tierByVoltage.put(V[i], (byte) i);
        }
    }

    public static ResourceLocation gtResource(String path) {
        return new ResourceLocation(GregTech.MODID, path);
    }

    /**
     * Does almost the same thing as .to(LOWER_UNDERSCORE, string), but it also inserts underscores between words and numbers.
     *
     * @param string Any string with ASCII characters.
     * @return A string that is all lowercase, with underscores inserted before word/number boundaries: "maragingSteel300" -> "maraging_steel_300"
     */
    public static String toLowerCaseUnderscore(String string) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            if (i != 0 && (Character.isUpperCase(string.charAt(i)) || (
                    Character.isDigit(string.charAt(i - 1)) ^ Character.isDigit(string.charAt(i)))))
                result.append("_");
            result.append(Character.toLowerCase(string.charAt(i)));
        }
        return result.toString();
    }

    /**
     * Does almost the same thing as LOWER_UNDERSCORE.to(UPPER_CAMEL, string), but it also removes underscores before numbers.
     *
     * @param string Any string with ASCII characters.
     * @return A string that is all lowercase, with underscores inserted before word/number boundaries: "maraging_steel_300" -> "maragingSteel300"
     */
    public static String lowerUnderscoreToUpperCamel(String string) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == '_')
                continue;
            if (i == 0 || string.charAt(i - 1) == '_') {
                result.append(Character.toUpperCase(string.charAt(i)));
            } else {
                result.append(string.charAt(i));
            }
        }
        return result.toString();
    }

    /**
     * @return lowest tier that can handle passed voltage
     */
    public static byte getTierByVoltage(long voltage) {
        if (voltage > V[GTValues.MAX]) return GTValues.MAX;
        return tierByVoltage.ceilingEntry(voltage).getValue();
    }

    /**
     * Ex: This method turns both 1024 and 512 into HV.
     *
     * @return the highest tier below or equal to the voltage value given
     */
    public static byte getFloorTierByVoltage(long voltage) {
        if (voltage < V[GTValues.ULV]) return GTValues.ULV;
        return tierByVoltage.floorEntry(voltage).getValue();
    }

    /**
     * Attempts to merge given ItemStack with ItemStacks in slot list supplied
     * If it's not possible to merge it fully, it will attempt to insert it into first empty slots
     *
     * @param itemStack item stack to merge. It WILL be modified.
     * @param simulate  if true, stack won't actually modify items in other slots
     * @return if merging of at least one item succeed, false otherwise
     */
    public static boolean mergeItemStack(ItemStack itemStack, List<Slot> slots, boolean simulate) {
        if (itemStack.isEmpty())
            return false; //if we are merging empty stack, return

        boolean merged = false;
        //iterate non-empty slots first
        //to try to insert stack into them
        for (Slot slot : slots) {
            if (!slot.mayPlace(itemStack))
                continue; //if itemstack cannot be placed into that slot, continue
            ItemStack stackInSlot = slot.getItem();
            if (!ItemStack.matches(itemStack, stackInSlot) ||
                    !ItemStack.tagMatches(itemStack, stackInSlot))
                continue; //if itemstacks don't match, continue
            int slotMaxStackSize = Math.min(stackInSlot.getMaxStackSize(), slot.getMaxStackSize(stackInSlot));
            int amountToInsert = Math.min(itemStack.getCount(), slotMaxStackSize - stackInSlot.getCount());
            // Need to check <= 0 for the PA, which could have this value negative due to slot limits in the Machine Access Interface
            if (amountToInsert <= 0)
                continue; //if we can't insert anything, continue
            //shrink our stack, grow slot's stack and mark slot as changed
            if (!simulate) {
                stackInSlot.grow(amountToInsert);
            }
            itemStack.shrink(amountToInsert);
            slot.setChanged();
            merged = true;
            if (itemStack.isEmpty())
                return true; //if we inserted all items, return
        }

        //then try to insert itemstack into empty slots
        //breaking it into pieces if needed
        for (Slot slot : slots) {
            if (!slot.mayPlace(itemStack))
                continue; //if itemstack cannot be placed into that slot, continue
            if (slot.hasItem())
                continue; //if slot contains something, continue
            int amountToInsert = Math.min(itemStack.getCount(), slot.getMaxStackSize(itemStack));
            if (amountToInsert == 0)
                continue; //if we can't insert anything, continue
            //split our stack and put result in slot
            ItemStack stackInSlot = itemStack.split(amountToInsert);
            if (!simulate) {
                slot.set(stackInSlot);
            }
            merged = true;
            if (itemStack.isEmpty())
                return true; //if we inserted all items, return
        }
        return merged;
    }


    public static int getRedstonePower(Level world, BlockPos blockPos, Direction side) {
        BlockPos offsetPos = blockPos.offset(side.getNormal());
        int worldPower = world.getSignal(offsetPos, side);
        if (worldPower < 15) {
            BlockState offsetState = world.getBlockState(offsetPos);
            if (offsetState.getBlock() instanceof RedStoneWireBlock) {
                int wirePower = offsetState.getValue(RedStoneWireBlock.POWER);
                return Math.max(worldPower, wirePower);
            }
        }
        return worldPower;
    }

    /**
     * If pos of this world loaded
     */
    public static boolean isPosChunkLoaded(Level world, BlockPos pos) {
        return !world.getChunkAt(pos).isEmpty();
    }


    /**
     * @return a list of itemstack linked with given item handler
     * modifications in list will reflect on item handler and wise-versa
     */
    public static List<ItemStack> itemHandlerToList(IItemHandlerModifiable inputs) {
        return new AbstractList<ItemStack>() {
            @Override
            public ItemStack set(int index, ItemStack element) {
                ItemStack oldStack = inputs.getStackInSlot(index);
                inputs.setStackInSlot(index, element == null ? ItemStack.EMPTY : element);
                return oldStack;
            }

            @Override
            public ItemStack get(int index) {
                return inputs.getStackInSlot(index);
            }

            @Override
            public int size() {
                return inputs.getSlots();
            }
        };
    }

    /**
     * @return a list of fluidstack linked with given fluid handler
     * modifications in list will reflect on fluid handler and wise-versa
     */
    public static List<FluidStack> fluidHandlerToList(IMultipleTankHandler fluidInputs) {
        List<IFluidTank> backedList = fluidInputs.getFluidTanks();
        return new AbstractList<FluidStack>() {
            @Override
            public FluidStack set(int index, FluidStack element) {
                IFluidTank fluidTank = backedList.get(index);
                FluidStack oldStack = fluidTank.getFluid();
                if (!(fluidTank instanceof FluidTank))
                    return oldStack;
                ((FluidTank) backedList.get(index)).setFluid(element);
                return oldStack;
            }

            @Override
            public FluidStack get(int index) {
                return backedList.get(index).getFluid();
            }

            @Override
            public int size() {
                return backedList.size();
            }
        };
    }

    public static NonNullList<ItemStack> copyStackList(List<ItemStack> itemStacks) {
        ItemStack[] stacks = new ItemStack[itemStacks.size()];
        for (int i = 0; i < itemStacks.size(); i++) {
            stacks[i] = copy(itemStacks.get(i));
        }
        return NonNullList.of(ItemStack.EMPTY, stacks);
    }

    public static List<FluidStack> copyFluidList(List<FluidStack> fluidStacks) {
        FluidStack[] stacks = new FluidStack[fluidStacks.size()];
        for (int i = 0; i < fluidStacks.size(); i++) stacks[i] = fluidStacks.get(i).copy();
        return Lists.newArrayList(stacks);
    }

    public static ItemStack copy(ItemStack... stacks) {
        for (ItemStack stack : stacks)
            if (!stack.isEmpty()) return stack.copy();
        return ItemStack.EMPTY;
    }

    /**
     * Attempts to merge given ItemStack with ItemStacks in list supplied
     * growing up to their max stack size
     *
     * @param stackToAdd item stack to merge.
     * @return a list of stacks, with optimized stack sizes
     */

    public static List<ItemStack> addStackToItemStackList(ItemStack stackToAdd, List<ItemStack> itemStackList) {
        if (!itemStackList.isEmpty()) {
            for (int i = 0; i < itemStackList.size(); i++) {
                ItemStack stackInList = itemStackList.get(i);
                if (ItemStackHashStrategy.comparingAllButCount().equals(stackInList, stackToAdd)) {
                    if (stackInList.getCount() < stackInList.getMaxStackSize()) {
                        int insertable = stackInList.getMaxStackSize() - stackInList.getCount();
                        if (insertable >= stackToAdd.getCount()) {
                            stackInList.grow(stackToAdd.getCount());
                            stackToAdd = ItemStack.EMPTY;
                        } else {
                            stackInList.grow(insertable);
                            stackToAdd = stackToAdd.copy();
                            stackToAdd.setCount(stackToAdd.getCount() - insertable);
                        }
                        if (stackToAdd.isEmpty()) {
                            break;
                        }
                    }
                }
            }
            if (!stackToAdd.isEmpty()) {
                itemStackList.add(stackToAdd);
            }
        } else {
            itemStackList.add(stackToAdd.copy());
        }
        return itemStackList;
    }


    public static String formatNumbers(long number) {
        return NUMBER_FORMAT.format(number);
    }

    public static String formatNumbers(double number) {
        return NUMBER_FORMAT.format(number);
    }

    //just because CCL uses a different color format
    //0xRRGGBBAA
    public static int convertRGBtoOpaqueRGBA_CL(int colorValue) {
        return convertRGBtoRGBA_CL(colorValue, 255);
    }

    public static int convertRGBtoRGBA_CL(int colorValue, int opacity) {
        return colorValue << 8 | (opacity & 0xFF);
    }

    public static int convertOpaqueRGBA_CLtoRGB(int colorAlpha) {
        return colorAlpha >>> 8;
    }

    //0xAARRGGBB
    public static int convertRGBtoOpaqueRGBA_MC(int colorValue) {
        return convertRGBtoOpaqueRGBA_MC(colorValue, 255);
    }

    public static int convertRGBtoOpaqueRGBA_MC(int colorValue, int opacity) {
        return opacity << 24 | colorValue;
    }

    public static int convertOpaqueRGBA_MCtoRGB(int alphaColor) {
        return alphaColor & 0xFFFFFF;
    }


    public static ResourceLocation getId(Material flag) {
        return MaterialRegistry.MATERIALS_BUILTIN.get().getKey(flag);
    }
    public static ResourceLocation getId(MaterialFlag flag) {
        return MaterialFlagRegistry.MATERIAL_FLAGS_BUILTIN.get().getKey(flag);
    }
    public static ResourceLocation getId(MaterialIconSet set) {
        return MaterialIconSetRegistry.MATERIAL_ICON_SETS_BUILTIN.get().getKey(set);
    }
    public static ResourceLocation getId(PropertyKey<?> key) {
        return MaterialPropertyRegistry.MATERIAL_PROPERTIES_BUILTIN.get().getKey(key);
    }
    public static ResourceLocation getId(Item key) {
        return ForgeRegistries.ITEMS.getKey(key);
    }
    public static ResourceLocation getId(Block key) {
        return ForgeRegistries.BLOCKS.getKey(key);
    }
}
