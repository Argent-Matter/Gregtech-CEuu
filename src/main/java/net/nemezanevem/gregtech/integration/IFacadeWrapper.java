package net.nemezanevem.gregtech.integration;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

//@Optional.Interface(modid = "ctm", iface = "team.chisel.ctm.api.IFacade")
public interface IFacadeWrapper extends IFacade {

    @Nonnull
    @Override
    BlockState getFacade(@Nonnull BlockGetter world, @Nonnull BlockPos pos, Direction side);

    @Nonnull
    @Override
    BlockState getFacade(@Nonnull BlockGetter world, @Nonnull BlockPos pos, Direction side, @Nonnull BlockPos connection);
}