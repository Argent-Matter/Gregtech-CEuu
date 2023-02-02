package net.nemezanevem.gregtech.common.pipelike.itempipe.net;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.nemezanevem.gregtech.api.pipenet.WorldPipeNet;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.ItemPipeProperty;

public class WorldItemPipeNet extends WorldPipeNet<ItemPipeProperty, ItemPipeNet> {

    private static final String DATA_ID = "gregtech.item_pipe_net";

    public static WorldItemPipeNet getWorldPipeNet(Level world) {
        if(!world.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) world;
            WorldItemPipeNet netWorldData = serverLevel.getDataStorage().computeIfAbsent(WorldItemPipeNet::load, WorldItemPipeNet::new, DATA_ID);
            netWorldData.setWorldAndInit(world);
            return netWorldData;
        }
        return null;
    }

    public WorldItemPipeNet() {
        super();
    }

    @Override
    protected ItemPipeNet createNetInstance() {
        return new ItemPipeNet(this);
    }

    public static WorldItemPipeNet load(CompoundTag tag) {
        WorldItemPipeNet instance = new WorldItemPipeNet();
        instance.readFromNBT(tag);
        return instance;
    }
}
