package net.nemezanevem.gregtech.client.particle;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.function.Consumer;

public abstract class GTParticle extends SingleQuadParticle {
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
    public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
        Quaternion quaternion;
        if (this.roll == 0.0F) {
            quaternion = pRenderInfo.rotation();
        } else {
            quaternion = new Quaternion(pRenderInfo.rotation());
            float f3 = Mth.lerp(pPartialTicks, this.oRoll, this.roll);
            quaternion.mul(Vector3f.ZP.rotation(f3));
        }

        float renderX = Mth.lerp(pPartialTicks, (float) this.xo, (float) this.x);
        float renderY = Mth.lerp(pPartialTicks, (float) this.yo, (float) this.y);
        float renderZ = Mth.lerp(pPartialTicks, (float) this.zo, (float) this.z);
        float size = this.getQuadSize(pPartialTicks);

        Vector3f[] points = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};

        for(int i = 0; i < 4; ++i) {
            Vector3f vector3f = points[i];
            vector3f.transform(quaternion);
            vector3f.mul(size);
            vector3f.add(renderX, renderY, renderZ);
        }

        float f7 = this.getU0();
        float f8 = this.getU1();
        float f5 = this.getV0();
        float f6 = this.getV1();

        int brightnessForRender = this.getLightColor(pPartialTicks);
        int j = brightnessForRender >> 16 & 65535;
        int k = brightnessForRender & 65535;
        pBuffer.vertex(points[0].x(), points[0].y(), points[0].z()).uv(f8, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j, k).endVertex();
        pBuffer.vertex(points[1].x(), points[1].y(), points[1].z()).uv(f8, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j, k).endVertex();
        pBuffer.vertex(points[2].x(), points[2].y(), points[2].z()).uv(f7, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j, k).endVertex();
        pBuffer.vertex(points[3].x(), points[3].y(), points[3].z()).uv(f7, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j, k).endVertex();
    }

    /***
     * Do not create an instance here; use a static instance plz
     */
    public IGTParticleHandler getGLHandler() {
        return IGTParticleHandler.DEFAULT_FX_HANDLER;
    }
}
