package net.nemezanevem.gregtech.api.util;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.nemezanevem.gregtech.api.capability.IMultipleTankHandler;
import net.nemezanevem.gregtech.api.recipe.FluidKey;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class GTTransferUtils {

    public static int transferFluids(@Nonnull LazyOptional<IFluidHandler> sourceHandler, @Nonnull LazyOptional<IFluidHandler> destHandler) {
        if (sourceHandler.isPresent() && destHandler.isPresent()) {
            return transferFluids(sourceHandler.resolve().get(), destHandler.resolve().get());
        }
        return 0;
    }

    public static int transferFluids(@Nonnull IFluidHandler sourceHandler, @Nonnull IFluidHandler destHandler) {
        return transferFluids(sourceHandler, destHandler, Integer.MAX_VALUE, fluidStack -> true);
    }

    public static int transferFluids(@Nonnull IFluidHandler sourceHandler, @Nonnull IFluidHandler destHandler, int transferLimit) {
        return transferFluids(sourceHandler, destHandler, transferLimit, fluidStack -> true);
    }

    public static int transferFluids(@Nonnull IFluidHandler sourceHandler, @Nonnull IFluidHandler destHandler, int transferLimit, @Nonnull Predicate<FluidStack> fluidFilter) {
        int fluidLeftToTransfer = transferLimit;

        for (int i = 0; i < sourceHandler.getTanks(); ++i) {
            FluidStack currentFluid = sourceHandler.getFluidInTank(i);
            if (currentFluid.getAmount() == 0 || !fluidFilter.test(currentFluid)) {
                continue;
            }

            currentFluid.setAmount(fluidLeftToTransfer);
            FluidStack fluidStack = sourceHandler.drain(currentFluid, IFluidHandler.FluidAction.SIMULATE);
            if (fluidStack.getAmount() == 0 || fluidStack.isEmpty()) {
                continue;
            }

            int canInsertAmount = destHandler.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE);
            if (canInsertAmount > 0) {
                fluidStack.setAmount(canInsertAmount);
                fluidStack = sourceHandler.drain(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                if (fluidStack != null && fluidStack.getAmount() > 0) {
                    destHandler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);

                    fluidLeftToTransfer -= fluidStack.getAmount();
                    if (fluidLeftToTransfer == 0) {
                        break;
                    }
                }
            }
        }
        return transferLimit - fluidLeftToTransfer;
    }

    public static boolean transferExactFluidStack(@Nonnull IFluidHandler sourceHandler, @Nonnull IFluidHandler destHandler, FluidStack fluidStack) {
        int amount = fluidStack.getAmount();
        FluidStack sourceFluid = sourceHandler.drain(fluidStack, IFluidHandler.FluidAction.SIMULATE);
        if (sourceFluid.isEmpty() || sourceFluid.getAmount() != amount) {
            return false;
        }
        int canInsertAmount = destHandler.fill(sourceFluid, IFluidHandler.FluidAction.SIMULATE);
        if (canInsertAmount == amount) {
            sourceFluid = sourceHandler.drain(sourceFluid, IFluidHandler.FluidAction.EXECUTE);
            if (!sourceFluid.isEmpty() && sourceFluid.getAmount() > 0) {
                destHandler.fill(sourceFluid, IFluidHandler.FluidAction.EXECUTE);
                return true;
            }
        }
        return false;
    }

    public static void moveInventoryItems(LazyOptional<IItemHandler> sourceInventory, LazyOptional<IItemHandler> targetInventory) {
        if (sourceInventory.isPresent() && targetInventory.isPresent()) {
            moveInventoryItems(sourceInventory.resolve().get(), targetInventory.resolve().get());
        }
    }

    public static void moveInventoryItems(IItemHandler sourceInventory, IItemHandler targetInventory) {
        for (int srcIndex = 0; srcIndex < sourceInventory.getSlots(); srcIndex++) {
            ItemStack sourceStack = sourceInventory.extractItem(srcIndex, Integer.MAX_VALUE, true);
            if (sourceStack.isEmpty()) {
                continue;
            }
            ItemStack remainder = insertItem(targetInventory, sourceStack, true);
            int amountToInsert = sourceStack.getCount() - remainder.getCount();
            if (amountToInsert > 0) {
                sourceStack = sourceInventory.extractItem(srcIndex, amountToInsert, false);
                insertItem(targetInventory, sourceStack, false);
            }
        }
    }

    /**
     * Simulates the insertion of items into a target inventory, then optionally performs the insertion.
     * <br /><br />
     * Simulating will not modify any of the input parameters. Insertion will either succeed completely, or fail
     * without modifying anything.
     * This method should be called with {@code simulate} {@code true} first, then {@code simulate} {@code false},
     * only if it returned {@code true}.
     *
     * @param handler  the target inventory
     * @param simulate whether to simulate ({@code true}) or actually perform the insertion ({@code false})
     * @param items    the items to insert into {@code handler}.
     * @return {@code true} if the insertion succeeded, {@code false} otherwise.
     */
    public static boolean addItemsToItemHandler(final IItemHandler handler,
                                                final boolean simulate,
                                                final List<ItemStack> items) {
        // determine if there is sufficient room to insert all items into the target inventory
        if (simulate) {
            OverlayedItemHandler overlayedItemHandler = new OverlayedItemHandler(handler);
            Map<ItemStackKey, Integer> stackKeyMap = GTHashMaps.fromItemStackCollection(items);

            for (Map.Entry<ItemStackKey, Integer> entry : stackKeyMap.entrySet()) {
                int amountToInsert = entry.getValue();
                int amount = overlayedItemHandler.insertStackedItemStackKey(entry.getKey(), amountToInsert);
                if (amount > 0) {
                    return false;
                }
            }
            return true;
        }

        // perform the merge.
        items.forEach(stack -> insertItem(handler, stack, false));
        return true;
    }

    /**
     * Simulates the insertion of fluid into a target fluid handler, then optionally performs the insertion.
     * <br /><br />
     * Simulating will not modify any of the input parameters. Insertion will either succeed completely, or fail
     * without modifying anything.
     * This method should be called with {@code simulate} {@code true} first, then {@code simulate} {@code false},
     * only if it returned {@code true}.
     *
     * @param fluidHandler the target inventory
     * @param simulate     whether to simulate ({@code true}) or actually perform the insertion ({@code false})
     * @param fluidStacks  the items to insert into {@code fluidHandler}.
     * @return {@code true} if the insertion succeeded, {@code false} otherwise.
     */
    public static boolean addFluidsToFluidHandler(IMultipleTankHandler fluidHandler,
                                                  boolean simulate,
                                                  List<FluidStack> fluidStacks) {
        if (simulate) {
            OverlayedFluidHandler overlayedFluidHandler = new OverlayedFluidHandler(fluidHandler);
            Map<FluidKey, Integer> fluidKeyMap = GTHashMaps.fromFluidCollection(fluidStacks);
            for (Map.Entry<FluidKey, Integer> entry : fluidKeyMap.entrySet()) {
                int amountToInsert = entry.getValue();
                int inserted = overlayedFluidHandler.insertStackedFluidKey(entry.getKey(), amountToInsert);
                if (inserted != amountToInsert) {
                    return false;
                }
            }
            return true;
        }

        fluidStacks.forEach(fluidStack -> fluidHandler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE));
        return true;
    }

    /**
     * Inserts items by trying to fill slots with the same item first, and then fill empty slots.
     */
    public static ItemStack insertItem(IItemHandler handler, ItemStack stack, boolean simulate) {
        if (handler == null || stack.isEmpty()) {
            return stack;
        }
        if (!stack.isStackable()) {
            return insertToEmpty(handler, stack, simulate);
        }

        IntList emptySlots = new IntArrayList();
        int slots = handler.getSlots();

        for (int i = 0; i < slots; i++) {
            ItemStack slotStack = handler.getStackInSlot(i);
            if (slotStack.isEmpty()) {
                emptySlots.add(i);
            }
            if (ItemHandlerHelper.canItemStacksStackRelaxed(stack, slotStack)) {
                stack = handler.insertItem(i, stack, simulate);
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }

        for (int slot : emptySlots) {
            stack = handler.insertItem(slot, stack, simulate);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        return stack;
    }

    /**
     * Only inerts to empty slots. Perfect for not stackable items
     */
    public static ItemStack insertToEmpty(IItemHandler handler, ItemStack stack, boolean simulate) {
        if (handler == null || stack.isEmpty()) {
            return stack;
        }
        int slots = handler.getSlots();
        for (int i = 0; i < slots; i++) {
            ItemStack slotStack = handler.getStackInSlot(i);
            if (slotStack.isEmpty()) {
                stack = handler.insertItem(i, stack, simulate);
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return stack;
    }

    public static boolean areStackable(FluidStack stack, FluidStack stack2) {
        return stack != null && stack.getAmount() > 0 &&
                stack2 != null && stack2.getAmount() > 0 &&
                stack.isFluidEqual(stack2);
    }

    // TODO try to remove this one day
    public static void fillInternalTankFromFluidContainer(IFluidHandler fluidHandler, IItemHandlerModifiable itemHandler, int inputSlot, int outputSlot) {
        ItemStack inputContainerStack = itemHandler.extractItem(inputSlot, 1, true);
        FluidActionResult result = FluidUtil.tryEmptyContainer(inputContainerStack, fluidHandler, Integer.MAX_VALUE, null, false);
        if (result.isSuccess()) {
            ItemStack remainingItem = result.getResult();
            if (ItemStack.matches(inputContainerStack, remainingItem))
                return; //do not fill if item stacks match
            if (!remainingItem.isEmpty() && !itemHandler.insertItem(outputSlot, remainingItem, true).isEmpty())
                return; //do not fill if can't put remaining item
            FluidUtil.tryEmptyContainer(inputContainerStack, fluidHandler, Integer.MAX_VALUE, null, true);
            itemHandler.extractItem(inputSlot, 1, false);
            itemHandler.insertItem(outputSlot, remainingItem, false);
        }
    }
}
