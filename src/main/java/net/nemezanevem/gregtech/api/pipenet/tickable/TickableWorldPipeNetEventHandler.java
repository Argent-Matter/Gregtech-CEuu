package net.nemezanevem.gregtech.api.pipenet.tickable;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.nemezanevem.gregtech.GregTech;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@EventBusSubscriber(modid = GregTech.MODID)
public class TickableWorldPipeNetEventHandler {

    private static final List<Function<LevelAccessor, TickableWorldPipeNet<?, ?>>> pipeNetAccessors = new ArrayList<>();

    public static void registerTickablePipeNet(Function<LevelAccessor, TickableWorldPipeNet<?, ?>> pipeNetAccessor) {
        pipeNetAccessors.add(pipeNetAccessor);
    }

    private static Stream<TickableWorldPipeNet<?, ?>> getPipeNetsForWorld(LevelAccessor world) {
        return pipeNetAccessors.stream().map(accessor -> accessor.apply(world));
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if(event.phase == TickEvent.Phase.START) {
            Level world = event.level;
            if (world.isClientSide)
                return;
            getPipeNetsForWorld(world).forEach(TickableWorldPipeNet::update);
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        LevelAccessor world = event.getLevel();
        if (world.isClientSide())
            return;
        getPipeNetsForWorld(world).forEach(it -> it.onChunkLoaded(event.getChunk()));
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        LevelAccessor world = event.getLevel();
        if (world.isClientSide())
            return;
        getPipeNetsForWorld(world).forEach(it -> it.onChunkUnloaded(event.getChunk()));
    }
}
