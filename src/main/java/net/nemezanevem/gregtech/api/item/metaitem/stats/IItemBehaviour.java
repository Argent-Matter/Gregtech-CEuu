package net.nemezanevem.gregtech.api.item.metaitem.stats;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.List;

public interface IItemBehaviour extends IItemComponent {

    default boolean onLeftClickEntity(ItemStack itemStack, Player player, Entity entity) {
        return false;
    }

    default boolean itemInteractionForEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand hand) {
        return false;
    }

    default InteractionResult onItemUseFirst(Player player, Level world, BlockPos pos, Direction side, double hitX, double hitY, double hitZ, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    default InteractionResultHolder<ItemStack> onItemUse(Player player, Level world, BlockPos pos, InteractionHand hand, Direction facing, double hitX, double hitY, double hitZ) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    default void addInformation(ItemStack itemStack, List<Component> lines) {
    }

    default void onUpdate(ItemStack itemStack, Entity entity) {
    }

    default Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return HashMultimap.create();
    }

    default InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    default void addPropertyOverride(@Nonnull Item item) {
    }
}
