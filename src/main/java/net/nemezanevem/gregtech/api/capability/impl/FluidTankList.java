package net.nemezanevem.gregtech.api.capability.impl;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.nemezanevem.gregtech.api.capability.IMultipleTankHandler;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Recommended to use this with {@link NotifiableFluidTankFromList} to ensure
 * proper behavior of the "allowSameFluidFill" setting, but not required.
 */
public class FluidTankList implements IFluidHandler, IMultipleTankHandler, INBTSerializable<CompoundTag> {

    protected final List<IFluidTank> fluidTanks;
    private final boolean allowSameFluidFill;
    private final int hashCode;

    public FluidTankList(boolean allowSameFluidFill, IFluidTank... fluidTanks) {
        this.fluidTanks = Arrays.asList(fluidTanks);
        this.allowSameFluidFill = allowSameFluidFill;
        this.hashCode = Arrays.hashCode(fluidTanks);
    }

    public FluidTankList(boolean allowSameFluidFill, List<? extends IFluidTank> fluidTanks) {
        this.fluidTanks = new ArrayList<>(fluidTanks);
        this.allowSameFluidFill = allowSameFluidFill;
        this.hashCode = Arrays.hashCode(fluidTanks.toArray());
    }

    public FluidTankList(boolean allowSameFluidFill, FluidTankList parent, IFluidTank... additionalTanks) {
        this.fluidTanks = new ArrayList<>();
        this.fluidTanks.addAll(parent.fluidTanks);
        this.fluidTanks.addAll(Arrays.asList(additionalTanks));
        this.allowSameFluidFill = allowSameFluidFill;
        int hash = Objects.hash(parent);
        hash = 31 * hash + Arrays.hashCode(additionalTanks);
        this.hashCode = hash;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public List<IFluidTank> getFluidTanks() {
        return Collections.unmodifiableList(fluidTanks);
    }

    @Nonnull
    @Override
    public Iterator<IFluidTank> iterator() {
        return getFluidTanks().iterator();
    }

    @Override
    public int getTanks() {
        return fluidTanks.size();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return fluidTanks.get(tank).getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return fluidTanks.get(tank).getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return true;
    }

    @Override
    public IFluidTank getTankAt(int index) {
        return fluidTanks.get(index);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource == null || resource.getAmount() <= 0) {
            return 0;
        }
        return fillTanksImpl(resource.copy(), action);
    }

    //fills exactly one tank if multi-filling is not allowed
    //and as much tanks as possible otherwise
    //note that it will always try to fill tanks with same fluid first
    private int fillTanksImpl(FluidStack resource, FluidAction action) {
        int totalFilled = 0;
        //first, try to fill tanks that already have same fluid type
        for (IFluidTank handler : fluidTanks) {
            if (resource.isFluidEqual(handler.getFluid())) {
                int filledAmount = handler.fill(resource, action);
                totalFilled += filledAmount;
                resource.setAmount(resource.getAmount() - filledAmount);
                //if filling multiple tanks is not allowed, or resource is empty, return now
                if (!allowSameFluidFill() || resource.getAmount() == 0)
                    return totalFilled;
            }
        }
        //otherwise, try to fill empty tanks
        for (IFluidTank handler : fluidTanks) {
            if (handler.getFluidAmount() == 0) {
                int filledAmount = handler.fill(resource, action);
                totalFilled += filledAmount;
                resource.setAmount(resource.getAmount() - filledAmount);
                if (!allowSameFluidFill() || resource.getAmount() == 0)
                    return totalFilled;
            }
        }
        return totalFilled;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource == null || resource.getAmount() <= 0) {
            return null;
        }
        resource = resource.copy();
        FluidStack totalDrained = null;
        for (IFluidTank handler : fluidTanks) {
            if (!resource.isFluidEqual(handler.getFluid())) {
                continue;
            }
            FluidStack drain = handler.drain(resource.getAmount(), action);
            if (drain == null) {
                continue;
            }
            if (totalDrained == null) {
                totalDrained = drain;
            } else totalDrained.setAmount(totalDrained.getAmount() + drain.getAmount());

            resource.setAmount(resource.getAmount() - drain.getAmount());
            if (resource.getAmount() == 0) break;
        }
        return totalDrained;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain == 0) {
            return null;
        }
        FluidStack totalDrained = null;
        for (IFluidTank handler : fluidTanks) {
            if (totalDrained == null) {
                totalDrained = handler.drain(maxDrain, action);
                if (totalDrained != null)
                    maxDrain -= totalDrained.getAmount();
            } else {
                FluidStack copy = totalDrained.copy();
                copy.setAmount(maxDrain);
                if (!copy.isFluidEqual(handler.getFluid())) continue;
                FluidStack drain = handler.drain(copy.getAmount(), action);
                if (drain != null) {
                    totalDrained.setAmount(totalDrained.getAmount() + drain.getAmount());
                    maxDrain -= drain.getAmount();
                }
            }
            if (maxDrain <= 0) break;
        }
        return totalDrained;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag fluidInventory = new CompoundTag();
        fluidInventory.putInt("TankAmount", this.getTanks());

        ListTag tanks = new ListTag();
        for (int i = 0; i < this.getTanks(); i++) {
            Tag writeTag;
            IFluidTank fluidTank = fluidTanks.get(i);
            if (fluidTank instanceof FluidTank) {
                writeTag = ((FluidTank) fluidTank).writeToNBT(new CompoundTag());
            } else if (fluidTank instanceof INBTSerializable) {
                writeTag = ((INBTSerializable) fluidTank).serializeNBT();
            } else writeTag = new CompoundTag();

            tanks.add(writeTag);
        }
        fluidInventory.put("Tanks", tanks);
        return fluidInventory;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ListTag tanks = nbt.getList("Tanks", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(fluidTanks.size(), nbt.getInt("TankAmount")); i++) {
            CompoundTag nbtTag = tanks.getCompound(i);
            IFluidTank fluidTank = fluidTanks.get(i);
            if (fluidTank instanceof FluidTank) {
                ((FluidTank) fluidTank).readFromNBT((CompoundTag) nbtTag);
            } else if (fluidTank instanceof INBTSerializable) {
                ((INBTSerializable<CompoundTag>) fluidTank).deserializeNBT(nbtTag);
            }
        }
    }

    protected void validateTankIndex(int tank) {
        if (tank < 0 || tank >= fluidTanks.size())
            throw new RuntimeException("Tank " + tank + " not in valid range - (0," + fluidTanks.size() + "]");
    }

    @Override
    public int getIndexOfFluid(FluidStack fluidStack) {
        for (int i = 0; i < fluidTanks.size(); i++) {
            FluidStack tankStack = fluidTanks.get(i).getFluid();
            if (tankStack != null && tankStack.isFluidEqual(fluidStack)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean allowSameFluidFill() {
        return allowSameFluidFill;
    }
}
