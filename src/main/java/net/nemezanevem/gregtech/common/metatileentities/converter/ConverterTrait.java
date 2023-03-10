package net.nemezanevem.gregtech.common.metatileentities.converter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.blockentity.MTETrait;
import net.nemezanevem.gregtech.api.capability.FeCompat;
import net.nemezanevem.gregtech.api.capability.GregtechCapabilities;
import net.nemezanevem.gregtech.api.capability.IEnergyContainer;
import net.nemezanevem.gregtech.api.util.Util;

public class ConverterTrait extends MTETrait {

    private final int amps;
    private final long voltage;

    /**
     * If TRUE, the front facing of the machine will OUTPUT EU, other sides INPUT FE.
     *
     * If FALSE, the front facing of the machine will OUTPUT FE, other sides INPUT EU.
     */
    private boolean feToEu;

    private final IEnergyStorage energyFE = new FEContainer();
    private final IEnergyContainer energyEU = new EUContainer();
    protected long storedEU;

    private final long baseCapacity;

    private long usedAmps;

    private BlockPos frontPos;

    public ConverterTrait(MetaTileEntityConverter mte, int amps, boolean feToEu) {
        super(mte);
        this.amps = amps;
        this.feToEu = feToEu;
        this.voltage = GTValues.V[mte.getTier()];
        this.baseCapacity = this.voltage * 16 * amps;
    }

    protected IEnergyContainer getEnergyEUContainer() {
        return energyEU;
    }

    protected IEnergyStorage getEnergyFEContainer() {
        return energyFE;
    }

    public LazyOptional<IEnergyStorage> energyFELazy = LazyOptional.of(() -> energyFE);

    public boolean isFeToEu() {
        return feToEu;
    }

    protected void setFeToEu(boolean feToEu) {
        this.feToEu = feToEu;
    }

    public int getBaseAmps() {
        return amps;
    }

    public long getVoltage() {
        return voltage;
    }

    @Override
    public String getName() {
        return "EnergyConvertTrait";
    }

    @Override
    public int getNetworkID() {
        return 1;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability) {
        return null;
    }

    private long extractInternal(long amount) {
        if (amount <= 0) return 0;
        long change = Math.min(storedEU, amount);
        storedEU -= change;
        return change;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putLong("StoredEU", storedEU);
        nbt.putBoolean("feToEu", feToEu);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.storedEU = nbt.getLong("StoredEU");
        this.feToEu = nbt.getBoolean("feToEu");
    }

    @Override
    public void tick() {
        super.tick();
        this.usedAmps = 0;
        if (!metaTileEntity.getWorld().isClientSide) {
            pushEnergy();
        }
    }

    protected void pushEnergy() {
        long energyInserted;
        if (feToEu) { // push out EU
            // Get the EU capability in front of us
            LazyOptional<IEnergyContainer> container = getCapabilityAtFront(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER);
            if (!container.isPresent()) return;

            // make sure we can output at least 1 amp
            long ampsToInsert = Math.min(amps, storedEU / voltage);
            if (ampsToInsert == 0) return;

            // send out energy
            energyInserted = container.resolve().get().acceptEnergyFromNetwork(metaTileEntity.getFrontFacing().getOpposite(), voltage, ampsToInsert) * voltage;
        } else { // push out FE
            // Get the FE capability in front of us
            LazyOptional<IEnergyStorage> storage = getCapabilityAtFront(ForgeCapabilities.ENERGY);
            if (!storage.isPresent()) return;

            // send out energy
            energyInserted = FeCompat.insertEu(storage.resolve().get(), storedEU);
        }
        extractInternal(energyInserted);
    }

    protected <T> LazyOptional<T> getCapabilityAtFront(Capability<T> capability) {
        BlockEntity tile = metaTileEntity.getWorld().getBlockEntity(frontPos == null ? frontPos = metaTileEntity.getPos().offset(metaTileEntity.getFrontFacing().getNormal()) : frontPos);
        if (tile == null) return null;
        Direction opposite = metaTileEntity.getFrontFacing().getOpposite();
        return tile.getCapability(capability, opposite);
    }

    @Override
    public void onFrontFacingSet(Direction newFrontFacing) {
        this.frontPos = null;
    }

    // -- GTCEu Energy--------------------------------------------

    public class EUContainer implements IEnergyContainer {

        @Override
        public long acceptEnergyFromNetwork(Direction side, long voltage, long amperage) {
            if (amperage <= 0 || voltage <= 0 || feToEu || side == metaTileEntity.getFrontFacing())
                return 0;
            if (usedAmps >= amps) return 0;
            if (voltage > getInputVoltage()) {
                metaTileEntity.doExplosion(Util.getExplosionPower(voltage));
                return Math.min(amperage, amps - usedAmps);
            }

            long space = baseCapacity - storedEU;
            if (space < voltage) return 0;
            long maxAmps = Math.min(Math.min(amperage, amps - usedAmps), space / voltage);
            storedEU += voltage * maxAmps;
            usedAmps += maxAmps;
            return maxAmps;
        }

        @Override
        public boolean inputsEnergy(Direction side) {
            return !feToEu && side != metaTileEntity.getFrontFacing();
        }

        @Override
        public long changeEnergy(long amount) {
            if (amount == 0) return 0;
            return amount > 0 ? addEnergy(amount) : removeEnergy(-amount);
        }

        @Override
        public long addEnergy(long energyToAdd) {
            if (energyToAdd <= 0) return 0;
            long original = energyToAdd;

            // add energy to internal buffer
            long change = Math.min(baseCapacity - storedEU, energyToAdd);
            storedEU += change;
            energyToAdd -= change;

            return original - energyToAdd;
        }

        @Override
        public long removeEnergy(long energyToRemove) {
            return extractInternal(energyToRemove);
        }

        @Override
        public long getEnergyStored() {
            return storedEU;
        }

        @Override
        public long getEnergyCapacity() {
            return baseCapacity;
        }

        @Override
        public long getInputAmperage() {
            return feToEu ? 0 : amps;
        }

        @Override
        public long getInputVoltage() {
            return voltage;
        }

        @Override
        public long getOutputAmperage() {
            return feToEu ? amps : 0;
        }

        @Override
        public long getOutputVoltage() {
            return voltage;
        }
    }

    // -- Forge Energy--------------------------------------------

    public class FEContainer implements IEnergyStorage {

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (!feToEu || maxReceive <= 0) return 0;
            int received = Math.min(getMaxEnergyStored() - getEnergyStored(), maxReceive);
            received -= received % FeCompat.ratio(true); // avoid rounding issues
            if (!simulate) storedEU += FeCompat.toEu(received, FeCompat.ratio(true));
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return FeCompat.toFeBounded(storedEU, FeCompat.ratio(feToEu), Integer.MAX_VALUE);
        }

        @Override
        public int getMaxEnergyStored() {
            return FeCompat.toFeBounded(baseCapacity, FeCompat.ratio(feToEu), Integer.MAX_VALUE);
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return feToEu;
        }
    }
}
