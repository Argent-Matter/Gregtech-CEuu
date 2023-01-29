package net.nemezanevem.gregtech.api.blockentity.interfaces;

import net.minecraft.network.FriendlyByteBuf;
import net.nemezanevem.gregtech.api.gui.IUIHolder;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;

import java.util.function.Consumer;

/**
 * A simple compound Interface for all GregTech TileEntities.
 * <p/>
 * Also delivers most of the Informations about TileEntities.
 * <p/>
 */
public interface IGregTechTileEntity extends IHasWorldObjectAndCoords, IUIHolder {

    MetaTileEntity getMetaTileEntity();

    MetaTileEntity setMetaTileEntity(MetaTileEntity metaTileEntity);

    void writeCustomData(int discriminator, Consumer<FriendlyByteBuf> dataWriter);

    long getOffsetTimer(); // todo might not keep this one

    @Deprecated
    boolean isFirstTick();
}