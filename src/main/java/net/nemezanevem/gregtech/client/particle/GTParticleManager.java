package net.nemezanevem.gregtech.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.nemezanevem.gregtech.GregTech;
import org.lwjgl.opengl.GL11;

import java.util.*;

/**
 * //TODO - One day switch to using GPU instances for rendering when particle is under pressure.
 *
 * @Author: KilaBash
 * @Date: 2021/08/31
 * @Description: ParticleManger register, spawn, efficient rendering, update our custom particles.
 */
public class GTParticleManager {
    public final static GTParticleManager INSTANCE = new GTParticleManager();

    private static Level currentWorld = null;
    private static final Minecraft mc = Minecraft.getInstance();

    private final Map<IGTParticleHandler, ArrayDeque<GTParticle>> renderQueueBack = new HashMap<>();
    private final Map<IGTParticleHandler, ArrayDeque<GTParticle>> renderQueueFront = new HashMap<>();
    private final Queue<Tuple<IGTParticleHandler, GTParticle>> newParticleQueue = new ArrayDeque<>();

    public void addEffect(GTParticle... particles) {
        for (GTParticle particle : particles) {
            if (particle.getGLHandler() != null) {
                newParticleQueue.add(new Tuple<>(particle.getGLHandler(), particle));
            }
        }
    }

    public void updateEffects() {
        updateEffectLayer();
        if (!newParticleQueue.isEmpty()) {
            for (Tuple<IGTParticleHandler, GTParticle> handlerParticle = newParticleQueue.poll(); handlerParticle != null; handlerParticle = newParticleQueue.poll()) {
                IGTParticleHandler handler = handlerParticle.getA();
                GTParticle particle = handlerParticle.getB();
                Map<IGTParticleHandler, ArrayDeque<GTParticle>> renderQueue = particle.getRenderRange() > 0 ? renderQueueFront : renderQueueBack;
                if (!renderQueue.containsKey(handler)) {
                    renderQueue.put(handler, new ArrayDeque<>());
                }
                ArrayDeque<GTParticle> arrayDeque = renderQueue.get(handler);
                if (arrayDeque.size() > 6000) {
                    arrayDeque.removeFirst().remove();
                }
                arrayDeque.add(particle);
            }
        }
    }

    private void updateEffectLayer() {
        if (!renderQueueBack.isEmpty()) {
            updateQueue(renderQueueBack);
        }
        if (!renderQueueFront.isEmpty()) {
            updateQueue(renderQueueFront);
        }
    }

    private void updateQueue(Map<IGTParticleHandler, ArrayDeque<GTParticle>> renderQueue) {
        Iterator<Map.Entry<IGTParticleHandler, ArrayDeque<GTParticle>>> entryIterator = renderQueue.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<IGTParticleHandler, ArrayDeque<GTParticle>> entry = entryIterator.next();
            Iterator<GTParticle> iterator = entry.getValue().iterator();
            while (iterator.hasNext()) {
                Particle particle = iterator.next();
                tickParticle(particle);
                if (!particle.isAlive()) {
                    iterator.remove();
                }
            }
            if (entry.getValue().isEmpty()) {
                entryIterator.remove();
            }
        }
    }

    public void clearAllEffects(boolean cleanNewQueue) {
        if (cleanNewQueue) {
            for (Tuple<IGTParticleHandler, GTParticle> tuple : newParticleQueue) {
                tuple.getB().remove();
            }
            newParticleQueue.clear();
        }
        for (ArrayDeque<GTParticle> particles : renderQueueBack.values()) {
            particles.forEach(Particle::remove);
        }
        for (ArrayDeque<GTParticle> particles : renderQueueFront.values()) {
            particles.forEach(Particle::remove);
        }
        renderQueueBack.clear();
        renderQueueFront.clear();
    }

    private void tickParticle(final Particle particle) {
        try {
            particle.tick();
        }
        catch (Throwable throwable) {
            GregTech.LOGGER.error("particle update error: {}", particle.toString(), throwable);
            particle.remove();
        }
    }

    public void renderParticles(Entity entityIn, float partialTicks) {
        if (renderQueueBack.isEmpty() && renderQueueFront.isEmpty()) return;
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0);

        Tesselator tessellator = Tesselator.getInstance();
        RenderSystem.disableColorLogicOp();

        renderGlParticlesInLayer(renderQueueBack, tessellator, entityIn, partialTicks);

        RenderSystem.depthMask(false);
        renderGlParticlesInLayer(renderQueueFront, tessellator, entityIn, partialTicks);

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
    }

    private void renderGlParticlesInLayer(Map<IGTParticleHandler, ArrayDeque<GTParticle>> renderQueue, Tesselator tesselator, Entity entityIn, float partialTicks) {
        for (IGTParticleHandler handler : renderQueue.keySet()) {
            ArrayDeque<GTParticle> particles = renderQueue.get(handler);
            if (particles.isEmpty()) continue;
            VertexConsumer buffer = tesselator.getBuilder();
            handler.preDraw(buffer);
            for (final GTParticle particle : particles) {
                if (particle.shouldRendered(entityIn, partialTicks)) {
                    try {
                        particle.render(buffer, Minecraft.getInstance().gameRenderer.getMainCamera(), partialTicks);
                    }
                    catch (Throwable throwable) {
                        GregTech.LOGGER.error("particle render error: {}", particle.toString(), throwable);
                        particle.remove();
                    }
                }
            }
            handler.postDraw(buffer);
        }
    }

    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.isPaused()) {
            return;
        }

        if (currentWorld != mc.level) {
            INSTANCE.clearAllEffects(currentWorld != null);
            currentWorld = mc.level;
        }

        if (currentWorld != null) {
            INSTANCE.updateEffects();
        }
    }

    public static void renderWorld(RenderLevelStageEvent event) {
        if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
            Entity entity = mc.getCameraEntity();
            INSTANCE.renderParticles(entity == null ? mc.player : entity, event.getPartialTick());
        }
    }

    /*public static void debugOverlay(DebugScreenOverlay event) {
        var overlay = event.getOverlay();
        if (overlay.id().equals(VanillaGuiOverlay.DEBUG_TEXT.id())) {
            String particleTxt = event.getLeft().get(4);
            overlay.overlay().render();
            particleTxt += "." + TextFormatting.GOLD + " PARTICLE-BACK: " + INSTANCE.getStatistics(INSTANCE.renderQueueBack) + "PARTICLE-FRONt: " + INSTANCE.getStatistics(INSTANCE.renderQueueFront);
            event.getLeft().set(4, particleTxt);
        }
    }*/

    public String getStatistics(Map<IGTParticleHandler, ArrayDeque<GTParticle>> renderQueue) {
        int g = 0;
        for (ArrayDeque<GTParticle> queue : renderQueue.values()) {
            g += queue.size();
        }
        return " GLFX: " + g;
    }

}
