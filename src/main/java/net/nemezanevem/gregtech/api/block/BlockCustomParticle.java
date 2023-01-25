package net.nemezanevem.gregtech.api.block;

import codechicken.lib.vec.Vector3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.util.ParticleHandlerUtil;
import net.nemezanevem.gregtech.common.network.NetworkUtils;
import net.nemezanevem.gregtech.common.network.packets.PacketBlockParticle;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public abstract class BlockCustomParticle extends Block implements ICustomParticleBlock {

    public BlockCustomParticle(BlockBehaviour.Properties properties) {
        super(properties);
    }

    protected abstract Pair<TextureAtlasSprite, Integer> getParticleTexture(Level world, BlockPos blockPos);

    @Override
    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(new IClientBlockExtensions() {
            @Override
            public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
                return BlockCustomParticle.this.addHitEffects(state, level, target, manager);
            }

            @Override
            public boolean addDestroyEffects(BlockState state, Level Level, BlockPos pos, ParticleEngine manager) {
                return BlockCustomParticle.this.addDestroyEffects(state, Level, pos, manager);
            }
        });
    }

    public boolean addHitEffects(@Nonnull BlockState state, @Nonnull Level worldObj, HitResult target, @Nonnull ParticleEngine manager) {
        Pair<TextureAtlasSprite, Integer> atlasSprite = getParticleTexture(worldObj, new BlockPos(target.getLocation()));
        ParticleHandlerUtil.addHitEffects(state, (ClientLevel) worldObj, target.getType() == HitResult.Type.BLOCK ? (BlockHitResult) target : null, atlasSprite.getFirst(), atlasSprite.getSecond(), manager);
        return true;
    }

    public boolean addDestroyEffects(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull ParticleEngine manager) {
        Pair<TextureAtlasSprite, Integer> atlasSprite = getParticleTexture(world, pos);
        ParticleHandlerUtil.addBlockDestroyEffects(state, (ClientLevel) world, pos, atlasSprite.getFirst(), atlasSprite.getSecond(), manager);
        return true;
    }

    @Override
    public void handleCustomParticle(Level worldObj, BlockPos blockPos, ParticleEngine particleManager, Vector3 entityPos, int numberOfParticles) {
        Pair<TextureAtlasSprite, Integer> atlasSprite = getParticleTexture(worldObj, blockPos);
        ParticleHandlerUtil.addBlockLandingEffects((ClientLevel) worldObj, entityPos, atlasSprite.getFirst(), atlasSprite.getSecond(), particleManager, numberOfParticles);
    }

    @Override
    public boolean addRunningEffects(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (world.isClientSide) {
            Pair<TextureAtlasSprite, Integer> atlasSprite = getParticleTexture(world, pos);
            ParticleHandlerUtil.addBlockRunningEffects((ClientLevel) world, entity,atlasSprite.getFirst(), atlasSprite.getSecond());
        }
        return true;
    }

    @Override
    public boolean addLandingEffects(@Nonnull BlockState state, @Nonnull ServerLevel worldObj, @Nonnull BlockPos blockPosition, @Nonnull BlockState iblockstate, LivingEntity entity, int numberOfParticles) {
        PacketBlockParticle packet = new PacketBlockParticle(blockPosition, new Vector3(entity.getX(), entity.getY(), entity.getZ()), numberOfParticles);
        GregTech.NETWORK_HANDLER.send(NetworkUtils.blockPoint(worldObj, blockPosition), packet);
        return true;
    }
}
