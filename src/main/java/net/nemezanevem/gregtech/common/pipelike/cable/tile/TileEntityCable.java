package net.nemezanevem.gregtech.common.pipelike.cable.tile;

import codechicken.lib.vec.Cuboid6;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.capability.IEnergyContainer;
import net.nemezanevem.gregtech.api.pipenet.block.material.TileEntityMaterialPipeBase;
import net.nemezanevem.gregtech.api.blockentity.IDataInfoProvider;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.WireProperty;
import net.nemezanevem.gregtech.api.util.PerTickLongCounter;
import net.nemezanevem.gregtech.api.util.TaskScheduler;
import net.nemezanevem.gregtech.common.block.MetaBlocks;
import net.nemezanevem.gregtech.common.pipelike.cable.BlockCable;
import net.nemezanevem.gregtech.common.pipelike.cable.Insulation;
import net.nemezanevem.gregtech.common.pipelike.cable.net.EnergyNet;
import net.nemezanevem.gregtech.common.pipelike.cable.net.EnergyNetHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class TileEntityCable extends TileEntityMaterialPipeBase<Insulation, WireProperty> implements IDataInfoProvider {

    private final EnumMap<Direction, EnergyNetHandler> handlers = new EnumMap<>(Direction.class);
    private final PerTickLongCounter maxVoltageCounter = new PerTickLongCounter(0);
    private final AveragingPerTickCounter averageVoltageCounter = new AveragingPerTickCounter(0, 20);
    private final AveragingPerTickCounter averageAmperageCounter = new AveragingPerTickCounter(0, 20);
    private EnergyNetHandler defaultHandler;
    // the EnergyNetHandler can only be created on the server so we have a empty placeholder for the client
    private final IEnergyContainer clientCapability = IEnergyContainer.DEFAULT;
    private WeakReference<EnergyNet> currentEnergyNet = new WeakReference<>(null);
    private GTOverheatParticle particle;
    private int heatQueue;
    private int temperature = 293;
    private final int meltTemp = 3000;
    private boolean isTicking = false;

    public TileEntityCable(BlockPos pPos, BlockState pBlockState) {
        super(MetaBlocks.CABLE_BE.get(), pPos, pBlockState);
    }

    @Override
    public Class<Insulation> getPipeTypeClass() {
        return Insulation.class;
    }

    @Override
    public boolean supportsTicking() {
        return false;
    }

    @Override
    public boolean canHaveBlockedFaces() {
        return false;
    }

    private void initHandlers() {
        EnergyNet net = getEnergyNet();
        if (net == null) {
            return;
        }
        for (Direction facing : Direction.values()) {
            handlers.put(facing, new EnergyNetHandler(net, this, facing));
        }
        defaultHandler = new EnergyNetHandler(net, this, null);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!level.isClientSide) {
            setTemperature(temperature);
            if (temperature > 293) {
                TaskScheduler.scheduleTask(level, this::update);
            }
        }
    }

    /**
     * Should only be called internally
     *
     * @return if the cable should be destroyed
     */
    public boolean incrementAmperage(long amps, long voltage) {
        if (voltage > maxVoltageCounter.get(level)) {
            maxVoltageCounter.set(level, voltage);
        }
        averageVoltageCounter.increment(level, voltage);
        averageAmperageCounter.increment(level, amps);

        int dif = (int) (averageAmperageCounter.getLast(level) - getMaxAmperage());
        if (dif > 0) {
            applyHeat(dif * 40);
            return true;
        }

        return false;
    }

    public void applyHeat(int amount) {
        heatQueue += amount;
        if (!level.isClientSide && !isTicking && temperature + heatQueue > 293) {
            TaskScheduler.scheduleTask(level, this::update);
            isTicking = true;
        }
    }

    private boolean update() {
        if (heatQueue > 0) {
            // if received heat from overvolting or overamping, add heat
            setTemperature(temperature + heatQueue);
        }

        if (temperature >= meltTemp) {
            // cable melted
            level.setBlock(worldPosition, Blocks.FIRE.defaultBlockState(), 3);
            isTicking = false;
            return false;
        }

        if (temperature <= 293) {
            isTicking = false;
            return false;
        }

        if (getPipeType().insulationLevel >= 0 && temperature >= 1500 && GTValues.RNG.nextFloat() < 0.1) {
            // insulation melted
            uninsulate();
            isTicking = false;
            return false;
        }

        if (heatQueue == 0) {
            // otherwise cool down
            setTemperature((int) (temperature - Math.pow(temperature - 293, 0.35)));
        } else {
            heatQueue = 0;
        }
        return true;
    }

    private void uninsulate() {
        int temp = temperature;
        setTemperature(293);
        int index = getPipeType().insulationLevel;
        BlockCable newBlock = MetaBlocks.CABLES[index];
        level.setBlockState(pos, newBlock.getDefaultState());
        TileEntityCable newCable = (TileEntityCable) world.getBlockEntity(pos);
        if (newCable != null) { // should never be null
            newCable.setPipeData(newBlock, newBlock.getItemPipeType(null), getPipeMaterial());
            for (Direction facing : Direction.VALUES) {
                if (isConnected(facing)) {
                    newCable.setConnection(facing, true, true);
                }
            }
            newCable.setTemperature(temp);
            if (!newCable.isTicking) {
                TaskScheduler.scheduleTask(world, newCable::update);
                newCable.isTicking = true;
            }
        }
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
        world.checkLight(pos);
        if (!world.isClientSide) {
            writeCustomData(100, buf -> buf.writeVarInt(temperature));
        } else {
            if (temperature <= 293) {
                if (isParticleAlive())
                    particle.setExpired();
            } else {
                if (!isParticleAlive()) {
                    createParticle();
                }
                particle.setTemperature(temperature);
            }
        }
    }

    public int getDefaultTemp() {
        return 293;
    }

    public int getTemperature() {
        return temperature;
    }

    public int getMeltTemp() {
        return meltTemp;
    }

    @SideOnly(Side.CLIENT)
    public void createParticle() {
        particle = new GTOverheatParticle(world, pos, meltTemp, getPipeBoxes(), getPipeType().insulationLevel >= 0);
        GTParticleManager.INSTANCE.addEffect(particle);
    }

    @SideOnly(Side.CLIENT)
    public void killParticle() {
        if (isParticleAlive()) {
            particle.setExpired();
            particle = null;
        }
    }

    public double getAverageAmperage() {
        return averageAmperageCounter.getAverage(getWorld());
    }

    public long getCurrentMaxVoltage() {
        return maxVoltageCounter.get(getWorld());
    }

    public double getAverageVoltage() {
        return averageVoltageCounter.getAverage(getWorld());
    }

    public long getMaxAmperage() {
        return getNodeData().getAmperage();
    }

    public long getMaxVoltage() {
        return getNodeData().getVoltage();
    }

    @Nullable
    @Override
    public <T> LazyOptional<T> getCapabilityInternal(Capability<T> capability, @Nullable Direction facing) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            if (world.isClientSide)
                return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(clientCapability);
            if (handlers.size() == 0)
                initHandlers();
            checkNetwork();
            return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(handlers.getOrDefault(facing, defaultHandler));
        }
        return super.getCapabilityInternal(capability, facing);
    }


    public void checkNetwork() {
        if (defaultHandler != null) {
            EnergyNet current = getEnergyNet();
            if (defaultHandler.getNet() != current) {
                defaultHandler.updateNetwork(current);
                for (EnergyNetHandler handler : handlers.values()) {
                    handler.updateNetwork(current);
                }
            }
        }
    }

    private EnergyNet getEnergyNet() {
        if (world == null || world.isClientSide)
            return null;
        EnergyNet currentEnergyNet = this.currentEnergyNet.get();
        if (currentEnergyNet != null && currentEnergyNet.isValid() &&
                currentEnergyNet.containsNode(getPos()))
            return currentEnergyNet; //return current net if it is still valid
        WorldENet worldENet = WorldENet.getWorldENet(getWorld());
        currentEnergyNet = worldENet.getNetFromPos(getPos());
        if (currentEnergyNet != null) {
            this.currentEnergyNet = new WeakReference<>(currentEnergyNet);
        }
        return currentEnergyNet;
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0x404040;
    }

    @Override
    public void receiveCustomData(int discriminator, FriendlyByteBuf buf) {
        if (discriminator == 100) {
            setTemperature(buf.readVarInt());
        } else {
            super.receiveCustomData(discriminator, buf);
            if (isParticleAlive() && discriminator == GregtechDataCodes.UPDATE_CONNECTIONS) {
                particle.updatePipeBoxes(getPipeBoxes());
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean isParticleAlive() {
        return particle != null && particle.isAlive();
    }

    protected List<Cuboid6> getPipeBoxes() {
        List<Cuboid6> pipeBoxes = new ArrayList<>();
        float thickness = getPipeType().getThickness();
        if ((getConnections() & 63) < 63) {
            pipeBoxes.add(BlockPipe.getSideBox(null, thickness));
        }
        for (Direction facing : Direction.VALUES) {
            if (isConnected(facing))
                pipeBoxes.add(BlockPipe.getSideBox(facing, thickness));
        }
        return pipeBoxes;
    }

    @Nonnull
    @Override
    public CompoundTag writeToNBT(@Nonnull CompoundTag compound) {
        super.writeToNBT(compound);
        compound.putInt("Temp", temperature);
        return compound;
    }

    @Override
    public void readFromNBT(@Nonnull CompoundTag compound) {
        super.readFromNBT(compound);
        temperature = compound.getInt("Temp");
    }

    @Nonnull
    @Override
    public List<Component> getDataInfo() {
        List<Component> list = new ArrayList<>();
        list.add(Component.translatable("behavior.tricorder.eut_per_sec",
                Component.translatable(Util.formatNumbers(this.getAverageVoltage())).setStyle(new Style().setColor(TextFormatting.RED))
        ));
        list.add(Component.translatable("behavior.tricorder.amp_per_sec",
                Component.translatable(Util.formatNumbers(this.getAverageAmperage())).setStyle(new Style().setColor(TextFormatting.RED))
        ));
        return list;
    }
}
