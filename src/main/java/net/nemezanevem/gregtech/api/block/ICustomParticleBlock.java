package net.nemezanevem.gregtech.api.block;

import codechicken.lib.vec.Vector3;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ICustomParticleBlock {

    void handleCustomParticle(Level worldObj, BlockPos blockPos, ParticleEngine particleManager, Vector3 entityPos, int numberOfParticles);
}
