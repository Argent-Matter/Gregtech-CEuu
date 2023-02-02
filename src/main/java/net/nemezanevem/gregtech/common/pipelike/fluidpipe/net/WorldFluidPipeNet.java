package net.nemezanevem.gregtech.common.pipelike.fluidpipe.net;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.nemezanevem.gregtech.api.pipenet.WorldPipeNet;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.FluidPipeProperty;

public class WorldFluidPipeNet extends WorldPipeNet<FluidPipeProperty, FluidPipeNet> {

    private static final String DATA_ID_BASE = "gregtech.fluid_pipe_net";

    public static WorldFluidPipeNet getWorldPipeNet(Level level) {
        String DATA_ID = getDataID(DATA_ID_BASE, level);
        if(!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            WorldFluidPipeNet netWorldData = serverLevel.getDataStorage().computeIfAbsent(WorldFluidPipeNet::load, WorldFluidPipeNet::new, DATA_ID);
            netWorldData.setWorldAndInit(level);
            return netWorldData;
        }
        return null;
    }

    public WorldFluidPipeNet() {
        super();
    }

    @Override
    protected FluidPipeNet createNetInstance() {
        return new FluidPipeNet(this);
    }

    public static WorldFluidPipeNet load(CompoundTag tag) {
        WorldFluidPipeNet instance = new WorldFluidPipeNet();
        instance.readFromNBT(tag);
        return instance;
    }

}
