package net.nemezanevem.gregtech.api.blockentity;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.UIFactory;
import net.nemezanevem.gregtech.api.registry.gui.UIFactoryRegistry;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;

/**
 * {@link UIFactory} implementation for {@link MetaTileEntity}
 */
public class MetaTileEntityUIFactory extends UIFactory<IGregTechTileEntity> {

    public static final MetaTileEntityUIFactory INSTANCE = new MetaTileEntityUIFactory();

    private MetaTileEntityUIFactory() {
    }

    public void init() {
        UIFactoryRegistry.UI_FACTORIES.register("meta_tile_entity_factory", () -> this);
    }

    @Override
    protected ModularUI createUITemplate(IGregTechTileEntity tileEntity, Player entityPlayer) {
        return tileEntity.getMetaTileEntity().createUI(entityPlayer);
    }

    @Override
    protected IGregTechTileEntity readHolderFromSyncData(FriendlyByteBuf syncData) {
        return (IGregTechTileEntity) Minecraft.getInstance().level.getBlockEntity(syncData.readBlockPos());
    }

    @Override
    protected void writeHolderToSyncData(FriendlyByteBuf syncData, IGregTechTileEntity holder) {
        syncData.writeBlockPos(holder.pos());
    }
}
