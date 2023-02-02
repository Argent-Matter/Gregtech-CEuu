package net.nemezanevem.gregtech.api.cover;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.nemezanevem.gregtech.api.capability.GregtechTileCapabilities;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.UIFactory;
import net.nemezanevem.gregtech.api.registry.gui.UIFactoryRegistry;

public class CoverBehaviorUIFactory extends UIFactory<CoverBehavior> {

    public static final CoverBehaviorUIFactory INSTANCE = new CoverBehaviorUIFactory();

    private CoverBehaviorUIFactory() {
    }

    public void init() {
        UIFactoryRegistry.UI_FACTORIES.register("cover_behavior_factory", () -> this);
    }

    @Override
    protected ModularUI createUITemplate(CoverBehavior holder, Player entityPlayer) {
        return ((CoverWithUI) holder).createUI(entityPlayer);
    }

    @Override
    protected CoverBehavior readHolderFromSyncData(FriendlyByteBuf syncData) {
        BlockPos blockPos = syncData.readBlockPos();
        Direction attachedSide = Direction.values()[syncData.readByte()];
        BlockEntity tileEntity = Minecraft.getInstance().level.getBlockEntity(blockPos);
        LazyOptional<ICoverable> coverable = tileEntity == null ? null : tileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, attachedSide);
        if (coverable.isPresent()) {
            return coverable.resolve().get().getCoverAtSide(attachedSide);
        }
        return null;
    }

    @Override
    protected void writeHolderToSyncData(FriendlyByteBuf syncData, CoverBehavior holder) {
        syncData.writeBlockPos(holder.coverHolder.getPos());
        syncData.writeByte(holder.attachedSide.ordinal());
    }
}
