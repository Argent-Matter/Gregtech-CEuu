package net.nemezanevem.gregtech.api.capability.impl;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.capability.FeCompat;
import net.nemezanevem.gregtech.api.capability.GregtechCapabilities;
import net.nemezanevem.gregtech.api.capability.IElectricItem;
import net.nemezanevem.gregtech.api.capability.IEnergyContainer;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.common.ConfigHolder;

import java.util.ArrayList;
import java.util.List;

public class EnergyContainerBatteryBuffer extends EnergyContainerHandler {

    public static final long AMPS_PER_BATTERY = 2L;

    private final int tier;

    public EnergyContainerBatteryBuffer(MetaTileEntity metaTileEntity, int tier, int inventorySize) {
        super(metaTileEntity, GTValues.V[tier] * inventorySize * 32L, GTValues.V[tier], inventorySize * AMPS_PER_BATTERY, GTValues.V[tier], inventorySize);
        this.tier = tier;
    }

    @Override
    public long acceptEnergyFromNetwork(Direction side, long voltage, long amperage) {
        if (amperage <= 0 || voltage <= 0)
            return 0;

        List<Object> batteries = getNonFullBatteries();
        long maxAmps = batteries.size() * AMPS_PER_BATTERY - amps;
        long usedAmps = Math.min(maxAmps, amperage);
        if (maxAmps <= 0)
            return 0;

        if (side == null || inputsEnergy(side)) {
            if (voltage > getInputVoltage()) {
                metaTileEntity.doExplosion(Util.getExplosionPower(voltage));
                return usedAmps;
            }

            //Prioritizes as many packets as available from the buffer
            long internalAmps = Math.min(maxAmps, Math.max(0, getInternalStorage() / voltage));

            usedAmps = Math.min(usedAmps, maxAmps - internalAmps);
            amps += usedAmps;
            energyInputPerSec += usedAmps * voltage;

            long energy = (usedAmps + internalAmps) * voltage;
            long distributed = energy / batteries.size();

            for (Object item : batteries) {
                if (item instanceof IElectricItem) {
                    IElectricItem electricItem = (IElectricItem) item;
                    energy -= electricItem.charge(Math.min(distributed, GTValues.V[electricItem.getTier()] * AMPS_PER_BATTERY), getTier(), true, false);
                } else if (item instanceof IEnergyStorage) {
                    IEnergyStorage energyStorage = (IEnergyStorage) item;
                    energy -= FeCompat.insertEu(energyStorage, Math.min(distributed, GTValues.V[getTier()] * AMPS_PER_BATTERY));
                }
            }

            //Remove energy used and then transfer overflow energy into the internal buffer
            setEnergyStored(getInternalStorage() - internalAmps * voltage + energy);
            return usedAmps;
        }
        return 0;
    }

    @Override
    public void tick() {
        amps = 0;
        if (metaTileEntity.getWorld().isClientSide) {
            return;
        }
        if (metaTileEntity.getOffsetTimer() % 20 == 0) {
            lastEnergyInputPerSec = energyInputPerSec;
            lastEnergyOutputPerSec = energyOutputPerSec;
            energyInputPerSec = 0;
            energyOutputPerSec = 0;
        }

        Direction outFacing = metaTileEntity.getFrontFacing();
        BlockEntity tileEntity = metaTileEntity.getWorld().getBlockEntity(metaTileEntity.getPos().offset(outFacing.getNormal()));
        if (tileEntity == null) {
            return;
        }
        LazyOptional<IEnergyContainer> energyContainerLazy = tileEntity.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, outFacing.getOpposite());
        if (!energyContainerLazy.isPresent()) {
            return;
        }
        IEnergyContainer energyContainer = energyContainerLazy.resolve().get();

        long voltage = getOutputVoltage();
        List<IElectricItem> batteries = getNonEmptyBatteries();
        if (batteries.size() > 0) {
            //Prioritize as many packets as available of energy created
            long internalAmps = Math.abs(Math.min(0, getInternalStorage() / voltage));
            long genAmps = Math.max(0, batteries.size() - internalAmps);
            long outAmps = 0L;

            if (genAmps > 0) {
                outAmps = energyContainer.acceptEnergyFromNetwork(outFacing.getOpposite(), voltage, genAmps);
                if (outAmps == 0 && internalAmps == 0)
                    return;
                energyOutputPerSec += outAmps * voltage;
            }

            long energy = (outAmps + internalAmps) * voltage;
            long distributed = energy / batteries.size();

            for (IElectricItem electricItem : batteries) {
                energy -= electricItem.discharge(distributed, getTier(), false, true, false);
            }

            //Subtract energy created out of thin air from the buffer
            setEnergyStored(getInternalStorage() + internalAmps * voltage - energy);
        }
    }

    private long getInternalStorage() {
        return energyStored;
    }

    private List<Object> getNonFullBatteries() {
        IItemHandlerModifiable inventory = getInventory();
        List<Object> batteries = new ArrayList<>();
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack batteryStack = inventory.getStackInSlot(i);
            IElectricItem electricItem = getBatteryContainer(batteryStack);
            if (electricItem != null) {
                if (electricItem.getCharge() < electricItem.getMaxCharge()) {
                    batteries.add(electricItem);
                }
            } else if (ConfigHolder.compat.energy.nativeEUToFE) {
                LazyOptional<IEnergyStorage> energyStorage = batteryStack.getCapability(ForgeCapabilities.ENERGY, null);
                if (energyStorage.isPresent()) {
                    IEnergyStorage storage = energyStorage.resolve().get();
                    if (storage.getEnergyStored() < storage.getMaxEnergyStored()) {
                        batteries.add(energyStorage);
                    }
                }
            }
        }
        return batteries;
    }

    private List<IElectricItem> getNonEmptyBatteries() {
        IItemHandlerModifiable inventory = getInventory();
        List<IElectricItem> batteries = new ArrayList<>();
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack batteryStack = inventory.getStackInSlot(i);
            IElectricItem electricItem = getBatteryContainer(batteryStack);
            if (electricItem == null) continue;
            if (electricItem.canProvideChargeExternally() && electricItem.getCharge() > 0) {
                batteries.add(electricItem);
            }
        }
        return batteries;
    }

    @Override
    public long getEnergyCapacity() {
        long energyCapacity = 0L;
        IItemHandlerModifiable inventory = getInventory();
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack batteryStack = inventory.getStackInSlot(i);
            IElectricItem electricItem = getBatteryContainer(batteryStack);
            if (electricItem != null) {
                energyCapacity += electricItem.getMaxCharge();
            } else if (ConfigHolder.compat.energy.nativeEUToFE) {
                LazyOptional<IEnergyStorage> energyStorage = batteryStack.getCapability(ForgeCapabilities.ENERGY, null);
                if (energyStorage.isPresent()) {
                    IEnergyStorage storage = energyStorage.resolve().get();
                    energyCapacity += FeCompat.toEu(storage.getMaxEnergyStored(), FeCompat.ratio(false));
                }
            }
        }
        return energyCapacity;
    }

    @Override
    public long getEnergyStored() {
        long energyStored = 0L;
        IItemHandlerModifiable inventory = getInventory();
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack batteryStack = inventory.getStackInSlot(i);
            IElectricItem electricItem = getBatteryContainer(batteryStack);
            if (electricItem != null) {
                energyStored += electricItem.getCharge();
            } else if (ConfigHolder.compat.energy.nativeEUToFE) {
                LazyOptional<IEnergyStorage> energyStorage = batteryStack.getCapability(ForgeCapabilities.ENERGY, null);
                if (energyStorage.isPresent()) {
                    IEnergyStorage storage = energyStorage.resolve().get();
                    energyStored += FeCompat.toEu(storage.getEnergyStored(), FeCompat.ratio(false));
                }
            }
        }
        return energyStored;
    }

    @Override
    public void setEnergyStored(long energyStored) {
        this.energyStored = energyStored;
        if (!metaTileEntity.getWorld().isClientSide) {
            metaTileEntity.markDirty();
            notifyEnergyListener(false);
        }
    }

    public IElectricItem getBatteryContainer(ItemStack itemStack) {
        LazyOptional<IElectricItem> electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem.isPresent() && getTier() >= electricItem.resolve().get().getTier())
            return electricItem.resolve().get();
        return null;
    }

    public void notifyEnergyListener(boolean isInitialChange) {
        if (metaTileEntity instanceof IEnergyChangeListener) {
            ((IEnergyChangeListener) metaTileEntity).onEnergyChanged(this, isInitialChange);
        }
    }

    @Override
    public boolean inputsEnergy(Direction side) {
        return getMetaTileEntity().getFrontFacing() != side;
    }

    @Override
    public boolean outputsEnergy(Direction side) {
        return !inputsEnergy(side);
    }

    @Override
    public String getName() {
        return "BatteryEnergyContainer";
    }

    protected IItemHandlerModifiable getInventory() {
        return metaTileEntity.getImportItems();
    }

    protected int getTier() {
        return tier;
    }
}
