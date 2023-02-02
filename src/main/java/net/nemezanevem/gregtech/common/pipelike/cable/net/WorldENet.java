package net.nemezanevem.gregtech.common.pipelike.cable.net;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.nemezanevem.gregtech.api.pipenet.WorldPipeNet;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.WireProperty;

public class WorldENet extends WorldPipeNet<WireProperty, EnergyNet> {

    private static final String DATA_ID_BASE = "gregtech.e_net";

    public static WorldENet getWorldENet(LevelReader world) {
        if(!world.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) world;
            final String DATA_ID = getDataID(DATA_ID_BASE, serverLevel);
            WorldENet netWorldData = serverLevel.getDataStorage().computeIfAbsent(WorldENet::load, WorldENet::new, DATA_ID);
            netWorldData.setWorldAndInit(serverLevel);
            return netWorldData;
        }
        return null;
    }

    public WorldENet() {
        super();
    }

    @Override
    protected EnergyNet createNetInstance() {
        return new EnergyNet(this);
    }


    public static WorldENet load(CompoundTag tag) {
        WorldENet instance = new WorldENet();
        instance.readFromNBT(tag);
        return instance;
    }
}
