package net.nemezanevem.gregtech.api.cover;

import gregtech.api.gui.ModularUI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public interface CoverWithUI {

    default void openUI(EntityPlayerMP player) {
        CoverBehaviorUIFactory.INSTANCE.openUI((CoverBehavior) this, player);
    }

    ModularUI createUI(EntityPlayer player);

}
