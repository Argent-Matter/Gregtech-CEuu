package net.nemezanevem.gregtech.api.tileentity.interfaces;

import gregtech.api.util.IDirtyNotifiable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
