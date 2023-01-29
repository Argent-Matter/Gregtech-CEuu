package net.nemezanevem.gregtech.common.sound.internal;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.registries.ForgeRegistries;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.sound.ISoundManager;

public class SoundManager implements ISoundManager {

    private static final SoundManager INSTANCE = new SoundManager();

    // This cannot be marked `@SideOnly(Side.CLIENT)`, because the server will report it as a missing field
    // when `INSTANCE` is instantiated on the server side
    private final Object2ObjectMap<BlockPos, SoundInstance> soundMap = new Object2ObjectOpenHashMap<>();

    private SoundManager() {/**/}

    public static SoundManager getInstance() {
        return INSTANCE;
    }

    @Override
    public SoundEvent registerSound(String modName, String soundName) {
        ResourceLocation location = new ResourceLocation(modName, soundName);
        SoundEvent event = new SoundEvent(location);
        ForgeRegistries.SOUND_EVENTS.register(location, event);
        return event;
    }

    @Override
    public SoundEvent registerSound(String soundName) {
        String containerId = GregTech.spriteRegistryHelper.getLoadedContainer().getID();
        if (containerId == null) containerId = GregTech.MODID;
        return registerSound(containerId, soundName);
    }

    @Override
    public SoundInstance startTileSound(ResourceLocation soundName, float volume, BlockPos pos) {
        SoundInstance sound = soundMap.get(pos);
        if (sound == null || !Minecraft.getInstance().getSoundManager().isActive(sound)) {
            sound = new SimpleSoundInstance(ForgeRegistries.SOUND_EVENTS.getValue(soundName), SoundSource.BLOCKS, volume, 1.0F,
                    SoundInstance.createUnseededRandom(), pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);

            soundMap.put(pos, sound);
            Minecraft.getInstance().getSoundManager().play(sound);
        }
        return sound;
    }

    @Override
    public void stopTileSound(BlockPos pos) {
        SoundInstance sound = soundMap.get(pos);
        if (sound != null) {
            Minecraft.getInstance().getSoundManager().stop(sound);
            soundMap.remove(pos);
        }
    }
}
