package net.nemezanevem.gregtech.api.block;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.nemezanevem.gregtech.api.unification.material.Material;

import javax.annotation.Nonnull;

public abstract class BuiltInRenderBlock extends BlockCustomParticle {

    public BuiltInRenderBlock(BlockBehaviour.Properties materialIn) {
        super(materialIn);
    }

    @Nonnull
    @Override
    public RenderType getRenderType() {
        return RenderType.cutoutMipped();
    }

}
