package net.nemezanevem.gregtech.api.item.armor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Defines abstract armor logic that can be added to ArmorMetaItem to control it
 * It can implement {@link net.minecraftforge.common.ISpecialArmor} for extended damage calculations
 * supported instead of using vanilla attributes
 */
public interface IArmorLogic {

    UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("CB3F55D3-645C-4F38-A144-9C13A33DB5CF");
    UUID ATTACK_SPEED_MODIFIER = UUID.fromString("FA233E1C-4180-4288-B05C-BCCE9785ACA3");

    default void addToolComponents(ArmorMetaItem metaValueItem) {
    }

    EquipmentSlot getEquipmentSlot(ItemStack itemStack);

    default boolean canBreakWithDamage(ItemStack stack) {
        return false;
    }

    default void damageArmor(LivingEntity entity, ItemStack itemStack, DamageSource source, int damage, EquipmentSlot equipmentSlot) {

    }

    default Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return ImmutableMultimap.of();
    }

    default boolean isValidArmor(ItemStack itemStack, Entity entity, EquipmentSlot equipmentSlot) {
        return getEquipmentSlot(itemStack) == equipmentSlot;
    }

    default void onArmorTick(Level world, Player player, ItemStack itemStack) {
    }

    default void renderHelmetOverlay(ItemStack itemStack, Player player, PoseStack poseStack, float partialTicks) {
    }

    default int getArmorLayersAmount(ItemStack itemStack) {
        return 1;
    }

    default int getArmorLayerColor(ItemStack itemStack, int layerIndex) {
        return 0xFFFFFF;
    }

    String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type);

    @Nullable
    default HumanoidModel getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot armorSlot, HumanoidModel defaultModel) {
        return null;
    }

    /**
     *
     * @return the value to multiply heat damage by
     */
    default float getHeatResistance() {
        return 1.0f;
    }
}
