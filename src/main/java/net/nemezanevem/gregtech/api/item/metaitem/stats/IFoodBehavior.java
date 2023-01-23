package net.nemezanevem.gregtech.api.item.metaitem.stats;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

import javax.annotation.Nullable;
import java.util.List;

public interface IFoodBehavior extends IItemComponent {

    int getFoodLevel(ItemStack itemStack, @Nullable Player player);

    float getSaturation(ItemStack itemStack, @Nullable Player player);

    boolean alwaysEdible(ItemStack itemStack, @Nullable Player player);

    UseAnim getFoodAction(ItemStack itemStack);

    default ItemStack onFoodEaten(ItemStack stack, Player player) {
        return stack;
    }

    void addInformation(ItemStack itemStack, List<String> lines);

}
