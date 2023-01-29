package net.nemezanevem.gregtech.api.blockentity.interfaces;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.nemezanevem.gregtech.api.util.IDirtyNotifiable;

public interface IHasWorldObjectAndCoords extends IDirtyNotifiable {

    Level world();

    BlockPos pos();

    default boolean isServerSide() {
        return world() != null && !world().isClientSide;
    }

    default boolean isClientSide() {
        return world() != null && world().isClientSide;
    }

    void notifyBlockUpdate();

    default void scheduleRenderUpdate() {
        BlockPos pos = pos();
        if(world().isClientSide) {
            Runnable run = () -> Minecraft.getInstance().levelRenderer.setBlocksDirty(
                    pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1,
                    pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            run.run();
        }
    }
}
