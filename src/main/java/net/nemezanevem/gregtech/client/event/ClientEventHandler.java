package net.nemezanevem.gregtech.client.event;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nemezanevem.gregtech.api.tileentity.MetaTileEntityHolder;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.client.particle.GTParticleManager;
import net.nemezanevem.gregtech.client.util.TooltipHelper;
import net.nemezanevem.gregtech.common.ConfigHolder;

import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDrawBlockHighlight(RenderHighlightEvent event) {
        event.getTarget().getLocation();
        BlockEntity tileEntity = event.getCamera().getEntity().level.getBlockEntity(new BlockPos(event.getTarget().getLocation()));
        if (tileEntity instanceof MetaTileEntityHolder) {
            if (((MetaTileEntityHolder) tileEntity).getMetaTileEntity() instanceof MetaTileEntityMonitorScreen) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onPreWorldRender(TickEvent.RenderTickEvent event) {
        DepthTextureUtil.onPreWorldRender(event);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        GTParticleManager.clientTick(event);
        TerminalARRenderer.onClientTick(event);
        TooltipHelper.onClientTick(event);
        GTValues.CLIENT_TIME++;
    }

    @SubscribeEvent
    public static void onRenderWorldLast(RenderLevelStageEvent event) {
        if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            DepthTextureUtil.renderWorld(event);
            MultiblockPreviewRenderer.renderWorldLastEvent(event);
            BlockPosHighlightRenderer.renderWorldLastEvent(event);
            TerminalARRenderer.renderWorldLastEvent(event);
            GTParticleManager.renderWorld(event);
        }
    }

    @SubscribeEvent
    public static void onRenderGameOverlayPre(RenderGuiOverlayEvent.Pre event) {
        TerminalARRenderer.renderGameOverlayEvent(event);
        if (ConfigHolder.misc.debug && event.getOverlay().id().equals(VanillaGuiOverlay.DEBUG_TEXT.id())) {
            //GTParticleManager.debugOverlay((RenderGameOverlayEvent.Text) event);
        }
    }

    @SubscribeEvent
    public static void onRenderSpecificHand(RenderHandEvent event) {
        TerminalARRenderer.renderHandEvent(event);
    }

    private static final Map<UUID, ResourceLocation> DEFAULT_CAPES = new Object2ObjectOpenHashMap<>();

    @SubscribeEvent
    public static void onPlayerRender(RenderPlayerEvent.Pre event) {
        AbstractClientPlayer clientPlayer = (AbstractClientPlayer) event.getEntity();
        UUID uuid = clientPlayer.getUUID();
        var info = Minecraft.getInstance().getConnection().getPlayerInfo(uuid);
        if (info != null) {
            ResourceLocation defaultPlayerCape = info.getCapeLocation();
            if (!DEFAULT_CAPES.containsKey(uuid)) {
                DEFAULT_CAPES.put(uuid, defaultPlayerCape);
            } else {
                defaultPlayerCape = DEFAULT_CAPES.get(uuid);
            }
            ResourceLocation cape = CapesRegistry.getPlayerCape(uuid);
            playerTextures.put(MinecraftProfileTexture.Type.CAPE, cape == null ? defaultPlayerCape : cape);
        }
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.PostConfigChangedEvent event) {
        if (GregTech.MODID.equals(event.getModID()) && event.isWorldRunning()) {
            Minecraft.getMinecraft().renderGlobal.loadRenderers();
        }
    }
}
