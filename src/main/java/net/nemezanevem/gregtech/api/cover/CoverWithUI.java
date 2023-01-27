package net.nemezanevem.gregtech.api.cover;

import gregtech.api.gui.ModularUI;
import net.minecraft.entity.player.Player;
import net.minecraft.entity.player.PlayerMP;

public interface CoverWithUI {

    default void openUI(PlayerMP player) {
        CoverBehaviorUIFactory.INSTANCE.openUI((CoverBehavior) this, player);
    }

    ModularUI createUI(Player player);

}
