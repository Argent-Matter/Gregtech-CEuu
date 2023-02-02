package net.nemezanevem.gregtech.api.cover;


import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.nemezanevem.gregtech.api.gui.ModularUI;

public interface CoverWithUI {

    default void openUI(ServerPlayer player) {
        CoverBehaviorUIFactory.INSTANCE.openUI((CoverBehavior) this, player);
    }

    ModularUI createUI(Player player);

}
