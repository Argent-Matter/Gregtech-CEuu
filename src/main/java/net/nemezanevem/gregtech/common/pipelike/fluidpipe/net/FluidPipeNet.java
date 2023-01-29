package net.nemezanevem.gregtech.common.pipelike.fluidpipe.net;

import net.minecraft.nbt.CompoundTag;
import net.nemezanevem.gregtech.api.pipenet.PipeNet;
import net.nemezanevem.gregtech.api.pipenet.WorldPipeNet;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.FluidPipeProperty;

public class FluidPipeNet extends PipeNet<FluidPipeProperty> {

    public FluidPipeNet(WorldPipeNet<FluidPipeProperty, FluidPipeNet> world) {
        super(world);
    }

    @Override
    protected void writeNodeData(FluidPipeProperty nodeData, CompoundTag tagCompound) {
        tagCompound.putInt("max_temperature", nodeData.getMaxFluidTemperature());
        tagCompound.putInt("throughput", nodeData.getThroughput());
        tagCompound.putBoolean("gas_proof", nodeData.isGasProof());
        tagCompound.putBoolean("acid_proof", nodeData.isAcidProof());
        tagCompound.putBoolean("cryo_proof", nodeData.isCryoProof());
        tagCompound.putBoolean("plasma_proof", nodeData.isPlasmaProof());
        tagCompound.putInt("channels", nodeData.getTanks());
    }

    @Override
    protected FluidPipeProperty readNodeData(CompoundTag tagCompound) {
        int maxTemperature = tagCompound.getInt("max_temperature");
        int throughput = tagCompound.getInt("throughput");
        boolean gasProof = tagCompound.getBoolean("gas_proof");
        boolean acidProof = tagCompound.getBoolean("acid_proof");
        boolean cryoProof = tagCompound.getBoolean("cryo_proof");
        boolean plasmaProof = tagCompound.getBoolean("plasma_proof");
        int channels = tagCompound.getInt("channels");
        return new FluidPipeProperty(maxTemperature, throughput, gasProof, acidProof, cryoProof, plasmaProof, channels);
    }
}
