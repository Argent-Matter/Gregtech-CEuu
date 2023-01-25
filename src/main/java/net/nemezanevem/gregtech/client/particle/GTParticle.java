package net.nemezanevem.gregtech.client.particle;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public abstract class GTParticle extends TextureSheetParticle {
    protected int texturesCount = 1;
    protected int squareRenderRange = -1;
    protected boolean motionless = false;
    protected Consumer<GTParticle> onUpdate;

    public GTParticle(ClientLevel worldIn, double posXIn, double posYIn, double posZIn) {
        super(worldIn, posXIn, posYIn, posZIn);
    }

    public GTParticle(ClientLevel worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
    }

    public boolean shouldRendered(Entity entityIn, float partialTicks) {
        if (squareRenderRange < 0) return true;
        return entityIn.getEyePosition(partialTicks).distanceToSqr(x, y, z) <= squareRenderRange;
    }

    /**
     * Set the render range, over the range do not render.
     * <P>
     *     -1 -- always render.
     * </P>
     */
    public void setRenderRange(int renderRange) {
        this.squareRenderRange = renderRange * renderRange;
    }

    /**
     * Particles can live forever now.
     */
    public void setImmortal() {
        this.age = -1;
    }

    /**
     * It can stop motion. It always has a motion before {@link Particle#tick()}
     */
    public void setMotionless(boolean motionless) {
        this.motionless = motionless;
    }

    /**
     * Set color blend of this particle.
     */
    public void setColor(int color) {
        this.setColor((color >> 16 & 255) / 255.0F,
                (color >> 8 & 255) / 255.0F,
                (color & 255) / 255.0F);
        this.setAlpha((color >> 24 & 255) / 255.0F);
    }

    /**
     * Set scale of this particle.
     */
    public void setScale(float scale) {
        this.scale(scale);
    }

    /**
     * Set Gravity of this particle.
     */
    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    /**
     * How many sub-textures in the current texture. it always 16 in the {@link Particle}. but we allow the bigger or smaller texture in the GTParticle.
     */
    public void setTexturesCount(int texturesCount) {
        this.texturesCount = texturesCount;
    }

    /**
     * Update each tick
     */
    public void setOnUpdate(Consumer<GTParticle> onUpdate) {
        this.onUpdate = onUpdate;
    }

    public int getTexturesCount() {
        return texturesCount;
    }

    public boolean isMotionless() {
        return motionless;
    }

    public int getRenderRange() {
        return squareRenderRange >= 0 ? -1 : (int) Math.sqrt(squareRenderRange);
    }

    @Override
    public void tick() {
        if (this.onUpdate != null) {
            onUpdate.accept(this);
        }
        if (this.age >= 0 && this.age++ >= this.lifetime) {
            this.remove();
        }

        if (!motionless) {
            this.xo = this.x;
            this.yo = this.y;
            this.zo = this.z;

            this.yd -= 0.04D * (double)this.gravity;
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.9800000190734863D;
            this.yd *= 0.9800000190734863D;
            this.zd *= 0.9800000190734863D;

            if (this.onGround) {
                this.zd *= 0.699999988079071D;
                this.zd *= 0.699999988079071D;
            }
        }
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        float scale = 0.1F * this.quadSize;

        if (this.te != null) {
            minU = this.particleTexture.getMinU();
            maxU = this.particleTexture.getMaxU();
            minV = this.particleTexture.getMinV();
            maxV = this.particleTexture.getMaxV();
        }

        float renderX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX);
        float renderY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY);
        float renderZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ);
        int brightnessForRender = this.getBrightnessForRender(partialTicks);
        int j = brightnessForRender >> 16 & 65535;
        int k = brightnessForRender & 65535;
        buffer.pos(renderX - rotationX * scale - rotationXY * scale, renderY - rotationZ * scale,  (renderZ - rotationYZ * scale - rotationXZ * scale)).tex(maxU, maxV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos(renderX - rotationX * scale + rotationXY * scale, renderY + rotationZ * scale,  (renderZ - rotationYZ * scale + rotationXZ * scale)).tex(maxU, minV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos(renderX + rotationX * scale + rotationXY * scale,  (renderY + rotationZ * scale),  (renderZ + rotationYZ * scale + rotationXZ * scale)).tex(minU, minV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos(renderX + rotationX * scale - rotationXY * scale,  (renderY - rotationZ * scale),  (renderZ + rotationYZ * scale - rotationXZ * scale)).tex(minU, maxV).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
    }

    /***
     * Do not create an instance here; use a static instance plz
     */
    public IGTParticleHandler getGLHandler() {
        return IGTParticleHandler.DEFAULT_FX_HANDLER;
    }
}
