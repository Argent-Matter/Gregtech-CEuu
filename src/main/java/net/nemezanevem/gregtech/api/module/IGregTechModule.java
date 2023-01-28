package net.nemezanevem.gregtech.api.module;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.server.*;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public interface IGregTechModule {

    @Nonnull
    default Set<ResourceLocation> getDependencyUids() {
        return Collections.emptySet();
    }

    @Nonnull
    default Set<String> getModDependencyIDs() {
        return Collections.emptySet();
    }

    default void init(FMLCommonSetupEvent event) {
    }

    default void loadComplete(FMLLoadCompleteEvent event) {
    }

    default void serverStarting(ServerStartingEvent event) {
    }

    default void serverStarted(ServerStartedEvent event) {
    }

    default void serverStopped(ServerStoppedEvent event) {
    }

    default void registerPackets() {
    }

    @Nonnull
    default List<Class<?>> getEventBusSubscribers() {
        return Collections.emptyList();
    }

    default boolean processIMC(InterModComms.IMCMessage message) {
        return false;
    }

    @Nonnull
    Logger getLogger();
}
