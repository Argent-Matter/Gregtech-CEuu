package net.nemezanevem.gregtech.api.capability.impl;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.capability.FeCompat;
import net.nemezanevem.gregtech.api.capability.GregtechCapabilities;
import net.nemezanevem.gregtech.api.capability.IElectricItem;
import net.nemezanevem.gregtech.api.capability.IEnergyContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.nemezanevem.gregtech.api.blockentity.MTETrait;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.common.ConfigHolder;

import java.util.function.Predicate;

public class EnergyContainerHandler extends MTETrait implements IEnergyContainer {

    protected final long maxCapacity;
    protected long energyStored;

    private final long maxInputVoltage;
    private final long maxInputAmperage;

    private final long maxOutputVoltage;
    private final long maxOutputAmperage;

    protected long lastEnergyInputPerSec = 0;
    protected long lastEnergyOutputPerSec = 0;
    protected long energyInputPerSec = 0;
    protected long energyOutputPerSec = 0;

    private Predicate<Direction> sideInputCondition;
    private Predicate<Direction> sideOutputCondition;

    protected long amps = 0;

    public EnergyContainerHandler(MetaTileEntity tileEntity, long maxCapacity, long maxInputVoltage, long maxInputAmperage, long maxOutputVoltage, long maxOutputAmperage) {
        super(tileEntity);
        this.maxCapacity = maxCapacity;
        this.maxInputVoltage = maxInputVoltage;
        this.maxInputAmperage = maxInputAmperage;
        this.maxOutputVoltage = maxOutputVoltage;
        this.maxOutputAmperage = maxOutputAmperage;
    }

    public void setSideInputCondition(Predicate<Direction> sideInputCondition) {
        this.sideInputCondition = sideInputCondition;
    }

    public void setSideOutputCondition(Predicate<Direction> sideOutputCondition) {
        this.sideOutputCondition = sideOutputCondition;
    }

    public static EnergyContainerHandler emitterContainer(MetaTileEntity tileEntity, long maxCapacity, long maxOutputVoltage, long maxOutputAmperage) {
        return new EnergyContainerHandler(tileEntity, maxCapacity, 0L, 0L, maxOutputVoltage, maxOutputAmperage);
    }

    public static EnergyContainerHandler receiverContainer(MetaTileEntity tileEntity, long maxCapacity, long maxInputVoltage, long maxInputAmperage) {
        return new EnergyContainerHandler(tileEntity, maxCapacity, maxInputVoltage, maxInputAmperage, 0L, 0L);
    }

    @Override
    public long getInputPerSec() {
        return lastEnergyInputPerSec;
    }

    @Override
    public long getOutputPerSec() {
        return lastEnergyOutputPerSec;
    }

    @Override
    public String getName() {
        return "EnergyContainer";
    }

    @Override
    public int getNetworkID() {
        return TraitNetworkIds.TRAIT_ID_ENERGY_CONTAINER;
    }

    LazyOptional<IEnergyContainer> lazyOptional = LazyOptional.of(() -> this);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return lazyOptional.cast();
        }
        return null;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compound = new CompoundTag();
        compound.putLong("EnergyStored", energyStored);
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundTag compound) {
        this.energyStored = compound.getLong("EnergyStored");
        notifyEnergyListener(true);
    }

    @Override
    public long getEnergyStored() {
        return this.energyStored;
    }

    public void setEnergyStored(long energyStored) {
        if (energyStored > this.energyStored) {
            energyInputPerSec += energyStored - this.energyStored;
        } else {
            energyOutputPerSec += this.energyStored - energyStored;
        }
        this.energyStored = energyStored;
        if (!metaTileEntity.getWorld().isClientSide) {
            metaTileEntity.markDirty();
            notifyEnergyListener(false);
        }
    }

    protected void notifyEnergyListener(boolean isInitialChange) {
        if (metaTileEntity instanceof IEnergyChangeListener) {
            ((IEnergyChangeListener) metaTileEntity).onEnergyChanged(this, isInitialChange);
        }
    }

    public boolean dischargeOrRechargeEnergyContainers(IItemHandlerModifiable itemHandler, int slotIndex) {
        ItemStack stackInSlot = itemHandler.getStackInSlot(slotIndex);
        if (stackInSlot.isEmpty()) { // no stack to charge/discharge
            return false;
        }

        LazyOptional<IElectricItem> electricItem = stackInSlot.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem.isPresent()) {
            return handleElectricItem(electricItem.resolve().get());
        } else if (ConfigHolder.Compatibility.energy.nativeEUToFE) {
            LazyOptional<IEnergyStorage> energyStorage = stackInSlot.getCapability(ForgeCapabilities.ENERGY, null);
            if (energyStorage.isPresent()) {
                return handleForgeEnergyItem(energyStorage.resolve().get());
            }
        }
        return false;
    }

    private boolean handleElectricItem(IElectricItem electricItem) {
        int machineTier = Util.getTierByVoltage(Math.max(getInputVoltage(), getOutputVoltage()));
        int chargeTier = Math.min(machineTier, electricItem.getTier());
        double chargePercent = getEnergyStored() / (getEnergyCapacity() * 1.0);

        // Check if the item is a battery (or similar), and if we can receive some amount of energy
        if (electricItem.canProvideChargeExternally() && getEnergyCanBeInserted() > 0) {

            // Drain from the battery if we are below half energy capacity, and if the tier matches
            if (chargePercent <= 0.5 && chargeTier == machineTier) {
                long dischargedBy = electricItem.discharge(getEnergyCanBeInserted(), machineTier, false, true, false);
                addEnergy(dischargedBy);
                return dischargedBy > 0L;
            }
        }

        // Else, check if we have above 50% power
        if (chargePercent > 0.5) {
            long chargedBy = electricItem.charge(getEnergyStored(), chargeTier, false, false);
            removeEnergy(chargedBy);
            return chargedBy > 0;
        }
        return false;
    }

    private boolean handleForgeEnergyItem(IEnergyStorage energyStorage) {
        int machineTier = Util.getTierByVoltage(Math.max(getInputVoltage(), getOutputVoltage()));
        double chargePercent = getEnergyStored() / (getEnergyCapacity() * 1.0);

        if (chargePercent > 0.5) {
            long chargedBy = FeCompat.insertEu(energyStorage, GTValues.V[machineTier]);
            removeEnergy(chargedBy);
            return chargedBy > 0;
        }
        return false;
    }

    @Override
    public void tick() {
        amps = 0;
        if (getMetaTileEntity().getWorld().isClientSide)
            return;
        if (metaTileEntity.getOffsetTimer() % 20 == 0) {
            lastEnergyOutputPerSec = energyOutputPerSec;
            lastEnergyInputPerSec = energyInputPerSec;
            energyOutputPerSec = 0;
            energyInputPerSec = 0;
        }
        if (getEnergyStored() >= getOutputVoltage() && getOutputVoltage() > 0 && getOutputAmperage() > 0) {
            long outputVoltage = getOutputVoltage();
            long outputAmperes = Math.min(getEnergyStored() / outputVoltage, getOutputAmperage());
            if (outputAmperes == 0) return;
            long amperesUsed = 0;
            for (Direction side : Direction.values()) {
                if (!outputsEnergy(side)) continue;
                BlockEntity tileEntity = metaTileEntity.getWorld().getBlockEntity(metaTileEntity.getPos().offset(side.getNormal()));
                Direction oppositeSide = side.getOpposite();
                if (tileEntity != null && tileEntity.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, oppositeSide).isPresent()) {
                    IEnergyContainer energyContainer = tileEntity.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, oppositeSide).resolve().get();
                    if (!energyContainer.inputsEnergy(oppositeSide)) continue;
                    amperesUsed += energyContainer.acceptEnergyFromNetwork(oppositeSide, outputVoltage, outputAmperes - amperesUsed);
                    if (amperesUsed == outputAmperes) break;
                }
            }
            if (amperesUsed > 0) {
                setEnergyStored(getEnergyStored() - amperesUsed * outputVoltage);
            }
        }
    }

    @Override
    public long acceptEnergyFromNetwork(Direction side, long voltage, long amperage) {
        if (amps >= getInputAmperage()) return 0;
        long canAccept = getEnergyCapacity() - getEnergyStored();
        if (voltage > 0L && (side == null || inputsEnergy(side))) {
            if (voltage > getInputVoltage()) {
                metaTileEntity.doExplosion(Util.getExplosionPower(voltage));
                return Math.min(amperage, getInputAmperage() - amps);
            }
            if (canAccept >= voltage) {
                long amperesAccepted = Math.min(canAccept / voltage, Math.min(amperage, getInputAmperage() - amps));
                if (amperesAccepted > 0) {
                    setEnergyStored(getEnergyStored() + voltage * amperesAccepted);
                    amps += amperesAccepted;
                    return amperesAccepted;
                }
            }
        }
        return 0;
    }

    @Override
    public long getEnergyCapacity() {
        return this.maxCapacity;
    }

    @Override
    public boolean inputsEnergy(Direction side) {
        return !outputsEnergy(side) && getInputVoltage() > 0 && (sideInputCondition == null || sideInputCondition.test(side));
    }

    @Override
    public boolean outputsEnergy(Direction side) {
        return getOutputVoltage() > 0 && (sideOutputCondition == null || sideOutputCondition.test(side));
    }

    @Override
    public long changeEnergy(long energyToAdd) {
        long oldEnergyStored = getEnergyStored();
        long newEnergyStored = (maxCapacity - oldEnergyStored < energyToAdd) ? maxCapacity : (oldEnergyStored + energyToAdd);
        if (newEnergyStored < 0)
            newEnergyStored = 0;
        setEnergyStored(newEnergyStored);
        return newEnergyStored - oldEnergyStored;
    }

    @Override
    public long getOutputVoltage() {
        return this.maxOutputVoltage;
    }

    @Override
    public long getOutputAmperage() {
        return this.maxOutputAmperage;
    }

    @Override
    public long getInputAmperage() {
        return this.maxInputAmperage;
    }

    @Override
    public long getInputVoltage() {
        return this.maxInputVoltage;
    }

    public interface IEnergyChangeListener {
        void onEnergyChanged(IEnergyContainer container, boolean isInitialChange);
    }
}
