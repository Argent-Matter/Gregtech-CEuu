package net.nemezanevem.gregtech.client.model;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nemezanevem.gregtech.GregTech;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT, modid = GregTech.MODID)
public interface IModelSupplier {

    @SubscribeEvent
    void onTextureStitch(TextureStitchEvent.Pre event);

    @SubscribeEvent
    void onModelRegister(ModelEvent.RegisterAdditional event);
}
