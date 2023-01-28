package net.nemezanevem.gregtech.api.sound;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public interface ISoundManager {

    /**
     * Register a Sound.
     *
     * Must be registered in the {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent} phase.
     *
     * @param soundName The name of the sound in the resources directory.
     * @return The created SoundEvent.
     */
    SoundEvent registerSound(String modName, String soundName);

    /**
     * Register a Sound.
     *
     * Defaults to using the current active module container's ID.
     *
     * Must be registered in the {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent} phase.
     *
     * @param soundName The name of the sound in the resources directory.
     * @return The created SoundEvent.
     */
    SoundEvent registerSound(String soundName);

    /**
     * Starts a positioned sound at a provided BlockPos.
     *
     * @param soundName The name of the sound to play.
     * @param volume    The volume multiplier of the sound.
     * @param pos       The position to play the sound at.
     * @return The sound that was played.
     */
    SoundInstance startTileSound(ResourceLocation soundName, float volume, BlockPos pos);

    /**
     * Stops the positioned sound playing at a given BlockPos (if any).
     */
    void stopTileSound(BlockPos pos);
}
