package net.nemezanevem.gregtech.common.pipelike.cable.net;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.capability.IEnergyContainer;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.common.pipelike.cable.tile.TileEntityCable;

import java.util.List;
import java.util.Objects;

public class EnergyNetHandler implements IEnergyContainer {

    private EnergyNet net;
    private boolean transfer;
    private final TileEntityCable cable;
    private final Direction facing;

    public EnergyNetHandler(EnergyNet net, TileEntityCable cable, Direction facing) {
        this.net = Objects.requireNonNull(net);
        this.cable = Objects.requireNonNull(cable);
        this.facing = facing;
    }

    public void updateNetwork(EnergyNet net) {
        this.net = net;
    }

    public EnergyNet getNet() {
        return net;
    }

    @Override
    public long getInputPerSec() {
        return net.getEnergyFluxPerSec();
    }

    @Override
    public long getOutputPerSec() {
        return net.getEnergyFluxPerSec();
    }

    @Override
    public long getEnergyCanBeInserted() {
        return transfer ? 0 : getEnergyCapacity();
    }

    @Override
    public long acceptEnergyFromNetwork(Direction side, long voltage, long amperage) {
        if (transfer) return 0;
        if (side == null) {
            if (facing == null) return 0;
            side = facing;
        }

        long amperesUsed = 0L;
        List<RoutePath> paths = net.getNetData(cable.getBlockPos());
        outer:
        for (RoutePath path : paths) {
            if (path.getMaxLoss() >= voltage)
                continue;
            if (Util.arePosEqual(cable.getBlockPos(), path.getPipePos()) && side == path.getFaceToHandler()) {
                //Do not insert into source handler
                continue;
            }
            IEnergyContainer dest = path.getHandler(cable.getLevel());
            Direction facing = path.getFaceToHandler().getOpposite();
            if (dest == null || !dest.inputsEnergy(facing) || dest.getEnergyCanBeInserted() <= 0) continue;
            long v = voltage - path.getMaxLoss();
            if (v <= 0)
                continue;

            for (TileEntityCable cable : path.getPath()) {
                if (cable.getMaxVoltage() < voltage) {
                    int heat = (int) (Math.log(Util.getTierByVoltage(voltage) - Util.getTierByVoltage(cable.getMaxVoltage())) * 45 + 36.5);
                    boolean cableBroken = false;
                    for (TileEntityCable cable1 : path.getPath()) {
                        cable1.applyHeat(heat);
                        cableBroken |= cable1.isRemoved();
                    }
                    if (cableBroken) {
                        // a cable burned away (or insulation melted)
                        break outer;
                    }
                    v = Math.min(cable.getMaxVoltage(), v); // limit transfer to cables max and void rest
                }
            }

            transfer = true;
            long amps = dest.acceptEnergyFromNetwork(facing, v, amperage - amperesUsed);
            transfer = false;
            if(amps == 0)
                continue;
            amperesUsed += amps;

            long voltageTraveled = voltage;
            boolean cableBroken = false;
            for (TileEntityCable cable : path.getPath()) {
                voltageTraveled -= cable.getNodeData().getLossPerBlock();
                if (voltageTraveled <= 0)
                    break;
                if (cable.isRemoved()) {
                    cableBroken = true;
                } else {
                    cable.incrementAmperage(amps, voltageTraveled);
                }
            }

            if (cableBroken) {
                // a cable burned away (or insulation melted)
                // recompute net data
                break;
            }

            if (amperage == amperesUsed)
                break;
        }
        net.addEnergyFluxPerSec(amperesUsed * voltage);
        return amperesUsed;
    }

    private void burnCable(Level world, BlockPos pos) {
        world.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
        if (!world.isClientSide) {
            ((ServerLevel) world).sendParticles(ParticleTypes.LARGE_SMOKE,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    5 + world.random.nextInt(3), 0.0, 0.0, 0.0, 0.1);
        }
    }

    @Override
    public long getInputAmperage() {
        return cable.getNodeData().getAmperage();
    }

    @Override
    public long getInputVoltage() {
        return cable.getNodeData().getVoltage();
    }

    @Override
    public long getEnergyCapacity() {
        return getInputVoltage() * getInputAmperage();
    }

    @Override
    public long changeEnergy(long energyToAdd) {
        GregTech.LOGGER.error("Do not use changeEnergy() for cables! Use acceptEnergyFromNetwork()");
        return acceptEnergyFromNetwork(facing == null ? Direction.UP : facing,
                energyToAdd / getInputAmperage(),
                energyToAdd / getInputVoltage()) * getInputVoltage();
    }

    @Override
    public boolean outputsEnergy(Direction side) {
        return true;
    }

    @Override
    public boolean inputsEnergy(Direction side) {
        return true;
    }

    @Override
    public long getEnergyStored() {
        return 0;
    }

    @Override
    public boolean isOneProbeHidden() {
        return true;
    }
}
