package net.nemezanevem.gregtech.common.pipelike.cable.net;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.nemezanevem.gregtech.api.pipenet.Node;
import net.nemezanevem.gregtech.api.pipenet.PipeNet;
import net.nemezanevem.gregtech.api.pipenet.WorldPipeNet;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.WireProperty;

import java.util.*;

public class EnergyNet extends PipeNet<WireProperty> {

    private long lastEnergyFluxPerSec;
    private long energyFluxPerSec;
    private long lastTime;

    private final Map<BlockPos, List<RoutePath>> NET_DATA = new HashMap<>();

    protected EnergyNet(WorldPipeNet<WireProperty, EnergyNet> world) {
        super(world);
    }

    public List<RoutePath> getNetData(BlockPos pipePos) {
        List<RoutePath> data = NET_DATA.get(pipePos);
        if (data == null) {
            data = EnergyNetWalker.createNetData(getWorldData(), pipePos);
            if (data == null) {
                // walker failed, don't cache so it tries again on next insertion
                return Collections.emptyList();
            }
            data.sort(Comparator.comparingInt(RoutePath::getDistance));
            NET_DATA.put(pipePos, data);
        }
        return data;
    }

    public long getEnergyFluxPerSec() {
        Level world = getWorldData();
        if (world != null && !world.isClientSide && (world.getGameTime() - lastTime) >= 20) {
            lastTime = world.getGameTime();
            clearCache();
        }
        return lastEnergyFluxPerSec;
    }

    public void addEnergyFluxPerSec(long energy) {
        energyFluxPerSec += energy;
    }

    public void clearCache() {
        lastEnergyFluxPerSec = energyFluxPerSec;
        energyFluxPerSec = 0;
    }

    @Override
    public void onNeighbourUpdate(BlockPos fromPos) {
        NET_DATA.clear();
    }

    @Override
    public void onPipeConnectionsUpdate() {
        NET_DATA.clear();
    }

    @Override
    protected void transferNodeData(Map<BlockPos, Node<WireProperty>> transferredNodes, PipeNet<WireProperty> parentNet) {
        super.transferNodeData(transferredNodes, parentNet);
        NET_DATA.clear();
        ((EnergyNet) parentNet).NET_DATA.clear();
    }

    @Override
    protected void writeNodeData(WireProperty nodeData, CompoundTag tagCompound) {
        tagCompound.putInt("voltage", nodeData.getVoltage());
        tagCompound.putInt("amperage", nodeData.getAmperage());
        tagCompound.putInt("loss", nodeData.getLossPerBlock());
    }

    @Override
    protected WireProperty readNodeData(CompoundTag tagCompound) {
        int voltage = tagCompound.getInt("voltage");
        int amperage = tagCompound.getInt("amperage");
        int lossPerBlock = tagCompound.getInt("loss");
        return new WireProperty(voltage, amperage, lossPerBlock);
    }
}
