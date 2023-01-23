package net.nemezanevem.gregtech.api.item.metaitem.stats;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

public interface IItemUseManager extends IItemComponent {

    default boolean canStartUsing(ItemStack stack, Player player) {
        return true;
    }

    default void onItemUseStart(ItemStack stack, Player player) {
    }

    UseAnim getUseAction(ItemStack stack);

    int getMaxItemUseDuration(ItemStack stack);

    default void onItemUsingTick(ItemStack stack, Player player, int count) {
    }

    default void onPlayerStoppedItemUsing(ItemStack stack, Player player, int timeLeft) {
    }

    default ItemStack onItemUseFinish(ItemStack stack, Player player) {
        return stack;
    }
}
