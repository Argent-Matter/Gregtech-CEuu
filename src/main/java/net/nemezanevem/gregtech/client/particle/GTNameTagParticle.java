package net.nemezanevem.gregtech.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.lwjgl.opengl.GL11;

public class GTNameTagParticle extends GTParticle {
    private static final Font FONT_RENDERER = Minecraft.getInstance().font;
    public String name;

    public GTNameTagParticle(ClientLevel worldIn, double posXIn, double posYIn, double posZIn, String name) {
        super(worldIn, posXIn, posYIn, posZIn);
        this.setMotionless(true);
        this.setImmortal();
        this.setRenderRange(64);
        this.name = name;
    }

    @Override
    public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
        float rotationYaw = pRenderInfo.getEntity().xRotO + (pRenderInfo.getXRot() - pRenderInfo.getEntity().xRotO) * pPartialTicks;
        float rotationPitch = pRenderInfo.getEntity().yRotO + (pRenderInfo.getYRot() - pRenderInfo.getEntity().yRotO) * pPartialTicks;
        PoseStack poseStack = new PoseStack();


        poseStack.pushPose();
        poseStack.translate(x -xd, y - yd, z - zd);
        GL11.glNormal3f(0.0f, 1.0f, 0.0f);
        poseStack.mulPose(new Quaternion(-rotationYaw, 0.0F, 1.0F, 0.0F));
        poseStack.mulPose(new Quaternion(rotationPitch, 1.0F, 0.0F, 0.0F));
        poseStack.scale(-0.025F, -0.025F, 0.025F);
        RenderSystem.depthMask(false);

        RenderSystem.defaultBlendFunc();
        int width = FONT_RENDERER.width(name) / 2;
        RenderSystem.disableTexture();
        pBuffer.vertex(-width - 1, -1, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        pBuffer.vertex(-width - 1, 8, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        pBuffer.vertex(width + 1, 8, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        pBuffer.vertex(width + 1, -1, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        pBuffer.endVertex();
        RenderSystem.enableTexture();
        RenderSystem.depthMask(true);

        FONT_RENDERER.draw(poseStack, name, -width, 0, -1);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @Override
    protected float getU0() {
        return 0;
    }

    @Override
    protected float getU1() {
        return 0;
    }

    @Override
    protected float getV0() {
        return 0;
    }

    @Override
    protected float getV1() {
        return 0;
    }
}
