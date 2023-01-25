package net.nemezanevem.gregtech.api.util;

import codechicken.lib.raytracer.IndexedVoxelShape;
import codechicken.lib.raytracer.VoxelShapeBlockHitResult;
import codechicken.lib.render.particle.CustomBreakingParticle;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.nemezanevem.gregtech.api.GTValues;

public class ParticleHandlerUtil {

    public static void addBlockRunningEffects(ClientLevel worldObj, Entity entity, TextureAtlasSprite atlasSprite, int spriteColor) {
        if (atlasSprite == null) return;
        double posX = entity.getX() + (GTValues.RNG.nextFloat() - 0.5) * entity.getBbWidth();
        double posY = entity.getBoundingBox().minY + 0.1;
        double posZ = entity.getZ() + (GTValues.RNG.nextFloat() - 0.5) * entity.getBbWidth();
        ParticleEngine manager = Minecraft.getInstance().particleEngine;

        float red = (spriteColor >> 16 & 255) / 255.0F;
        float green = (spriteColor >> 8 & 255) / 255.0F;
        float blue = (spriteColor & 255) / 255.0F;

        CustomBreakingParticle digIconParticle = new CustomBreakingParticle(worldObj, posX, posY, posZ, -entity.getDeltaMovement().x * 4.0, 1.5, -entity.getDeltaMovement().z * 4.0, atlasSprite);
        digIconParticle.setColor(red, green, blue);
        manager.add(digIconParticle);
    }

    public static void addBlockLandingEffects(ClientLevel worldObj, Vector3 entityPos, TextureAtlasSprite atlasSprite, int spriteColor, ParticleEngine manager, int numParticles) {
        if (atlasSprite == null) return;
        Vector3 start = entityPos.copy();
        Vector3 end = start.copy().add(Vector3.Y_NEG.copy().multiply(4));
        HitResult traceResult = worldObj.clip(new ClipContext(start.vec3(), end.vec3(), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, null));
        double speed = 0.15;

        float red = (spriteColor >> 16 & 255) / 255.0F;
        float green = (spriteColor >> 8 & 255) / 255.0F;
        float blue = (spriteColor & 255) / 255.0F;

        if (traceResult != null && traceResult.getType() == HitResult.Type.BLOCK && numParticles != 0) {
            for (int i = 0; i < numParticles; i++) {
                double mX = GTValues.RNG.nextGaussian() * speed;
                double mY = GTValues.RNG.nextGaussian() * speed;
                double mZ = GTValues.RNG.nextGaussian() * speed;
                CustomBreakingParticle digIconParticle = CustomBreakingParticle.newLandingParticle(worldObj, entityPos.x, entityPos.y, entityPos.z, mX, mY, mZ, atlasSprite);
                digIconParticle.setColor(red, green, blue);
                manager.add(digIconParticle);
            }
        }
    }

    public static void addBlockDestroyEffects(ClientLevel worldObj, VoxelShapeBlockHitResult result, TextureAtlasSprite atlasSprite, int spriteColor, ParticleEngine manager) {
        addBlockDestroyEffects(worldObj, result.shape, atlasSprite, spriteColor, manager);
    }

    public static void addBlockDestroyEffects(BlockState blockState, ClientLevel worldObj, BlockPos blockPos, TextureAtlasSprite atlasSprite, int spriteColor, ParticleEngine manager) {
        Cuboid6 cuboid6 = new Cuboid6(blockState.getShape(worldObj, blockPos).bounds()).add(blockPos);
        addBlockDestroyEffects(worldObj, new IndexedVoxelShape(Shapes.create(cuboid6.aabb()), null), atlasSprite, spriteColor, manager);
    }

    public static void addHitEffects(ClientLevel worldObj, VoxelShapeBlockHitResult result, TextureAtlasSprite atlasSprite, int spriteColor, ParticleEngine manager) {
        addBlockHitEffects(worldObj, result.shape, result.getDirection(), atlasSprite, spriteColor, manager);
    }

    public static void addHitEffects(BlockState blockState, ClientLevel worldObj, BlockHitResult target, TextureAtlasSprite atlasSprite, int spriteColor, ParticleEngine manager) {
        IndexedVoxelShape cuboid6 = getShape(blockState, worldObj, target);
        addBlockHitEffects(worldObj, cuboid6, target.getDirection(), atlasSprite, spriteColor, manager);
    }

    private static IndexedVoxelShape getShape(BlockState blockState, ClientLevel world, HitResult target) {
        BlockPos blockPos = new BlockPos(target.getLocation());
        if (target instanceof VoxelShapeBlockHitResult _target) {
            return _target.shape;
        }
        return new IndexedVoxelShape(blockState.getShape(world, blockPos), null);
    }

    //Straight copied from CustomParticleHandler with color parameter added

    public static void addBlockHitEffects(ClientLevel world, IndexedVoxelShape bounds, Direction side, TextureAtlasSprite icon, int spriteColor, ParticleEngine particleManager) {
        if (icon == null) return;
        float border = 0.1F;
        AABB aabb = bounds.bounds();
        Vector3 max = new Vector3(aabb.maxX, aabb.maxY, aabb.maxZ);
        Vector3 min = new Vector3(aabb.minX, aabb.minY, aabb.minZ);
        Vector3 diff = max.copy().subtract(min).add(-2 * border);
        diff.x *= world.random.nextDouble();
        diff.y *= world.random.nextDouble();
        diff.z *= world.random.nextDouble();
        Vector3 pos = diff.add(min).add(border);

        float red = (spriteColor >> 16 & 255) / 255.0F;
        float green = (spriteColor >> 8 & 255) / 255.0F;
        float blue = (spriteColor & 255) / 255.0F;

        if (side == Direction.DOWN) {
            diff.y = min.y - border;
        }
        if (side == Direction.UP) {
            diff.y = max.y + border;
        }
        if (side == Direction.NORTH) {
            diff.z = min.z - border;
        }
        if (side == Direction.SOUTH) {
            diff.z = max.z + border;
        }
        if (side == Direction.WEST) {
            diff.x = min.x - border;
        }
        if (side == Direction.EAST) {
            diff.x = max.x + border;
        }

        CustomBreakingParticle digIconParticle = new CustomBreakingParticle(world, pos.x, pos.y, pos.z, 0, 0, 0, icon);
        digIconParticle.setPower(0.2F);
        digIconParticle.scale(0.6F);
        digIconParticle.setColor(red, green, blue);
        particleManager.add(digIconParticle);
    }

    public static void addBlockDestroyEffects(ClientLevel world, IndexedVoxelShape bounds, TextureAtlasSprite icon, int spriteColor, ParticleEngine particleManager) {
        if (icon == null) return;
        AABB aabb = bounds.bounds();
        Vector3 max = new Vector3(aabb.maxX, aabb.maxY, aabb.maxZ);
        Vector3 min = new Vector3(aabb.minX, aabb.minY, aabb.minZ);
        Vector3 diff = max.copy().subtract(min);
        Vector3 center = min.copy().add(max).multiply(0.5);
        Vector3 density = diff.copy().multiply(4).ceil();

        float red = (spriteColor >> 16 & 255) / 255.0F;
        float green = (spriteColor >> 8 & 255) / 255.0F;
        float blue = (spriteColor & 255) / 255.0F;

        for (int i = 0; i < density.x; ++i) {
            for (int j = 0; j < density.y; ++j) {
                for (int k = 0; k < density.z; ++k) {
                    double x = min.x + (i + 0.5) * diff.x / density.x;
                    double y = min.y + (j + 0.5) * diff.y / density.y;
                    double z = min.z + (k + 0.5) * diff.z / density.z;
                    CustomBreakingParticle digIconParticle = new CustomBreakingParticle(world, x, y, z, x - center.x, y - center.y, z - center.z, icon);
                    digIconParticle.setColor(red, green, blue);
                    particleManager.add(digIconParticle);
                }
            }
        }
    }
}
