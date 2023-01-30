package net.nemezanevem.gregtech.api.item.armor;

import com.google.common.base.Preconditions;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.nemezanevem.gregtech.api.item.metaitem.MetaItem;
import net.nemezanevem.gregtech.api.item.metaitem.stats.IEnchantabilityHelper;
import net.nemezanevem.gregtech.api.item.metaitem.stats.IItemComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.minecraft.world.entity.EquipmentSlot.*;

public class ArmorMetaItem extends MetaItem implements IArmorItem, IEnchantabilityHelper {

    private IArmorLogic armorLogic = new DummyArmorLogic();

    public ArmorMetaItem() {
        super(new ArmorExtendedProperties(""));
    }

    protected ArmorExtendedProperties constructMetaValueItem(String unlocalizedName) {
        return new ArmorExtendedProperties(unlocalizedName);
    }

    @Nonnull
    private IArmorLogic getArmorLogic(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if(item instanceof ArmorMetaItem armorMetaItem) {
            return armorMetaItem.getArmorLogic();
        }
        return new DummyArmorLogic();
    }

    @Nonnull
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@Nonnull EquipmentSlot slot, @Nonnull ItemStack stack) {
        Multimap<Attribute, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);
        IArmorLogic armorLogic = getArmorLogic(stack);
        multimap.putAll(armorLogic.getAttributeModifiers(slot, stack));
        return multimap;
    }

    public static ArmorExtendedProperties builder(String unlocalizedName) {
        return new ArmorExtendedProperties(unlocalizedName);
    }

    @Override
    public ArmorProperties getProperties(LivingEntity player, @Nonnull ItemStack armor, DamageSource source, double damage, int slot) {
        IArmorLogic armorLogic = getArmorLogic(armor);
        if (armorLogic instanceof ISpecialArmorLogic) {
            return ((ISpecialArmorLogic) armorLogic).getProperties(player, armor, source, damage, getSlotByIndex(slot));
        }
        return new ArmorProperties(0, 0, Integer.MAX_VALUE);
    }

    @Override
    public int getArmorDisplay(Player player, @Nonnull ItemStack armor, int slot) {
        IArmorLogic armorLogic = getArmorLogic(armor);
        if (armorLogic instanceof ISpecialArmorLogic) {
            return ((ISpecialArmorLogic) armorLogic).getArmorDisplay(player, armor, slot);
        }
        return 0;
    }

    @Override
    public void damageArmor(LivingEntity entity, @Nonnull ItemStack stack, DamageSource source, int damage, int slot) {
        IArmorLogic armorLogic = getArmorLogic(stack);
        armorLogic.damageArmor(entity, stack, source, damage, getSlotByIndex(slot));
    }

    @Override
    public boolean handleUnblockableDamage(LivingEntity entity, @Nonnull ItemStack armor, DamageSource source, double damage, int slot) {
        IArmorLogic armorLogic = getArmorLogic(armor);
        if (armorLogic instanceof ISpecialArmorLogic) {
            return ((ISpecialArmorLogic) armorLogic).handleUnblockableDamage(entity, armor, source, damage, getSlotByIndex(slot));
        }
        return false;
    }

    @Override
    public void onArmorTick(@Nonnull Level world, @Nonnull Player player, @Nonnull ItemStack itemStack) {
        IArmorLogic armorLogic = getArmorLogic(itemStack);
        armorLogic.onArmorTick(world, player, itemStack);
    }

    @Override
    public boolean isValidArmor(@Nonnull ItemStack stack, @Nonnull EquipmentSlot armorType, @Nonnull Entity entity) {
        IArmorLogic armorLogic = getArmorLogic(stack);
        return super.isValidArmor(stack, armorType, entity) &&
                armorLogic.isValidArmor(stack, entity, armorType);
    }

    @Nullable
    @Override
    public EquipmentSlot getEquipmentSlot(@Nonnull ItemStack stack) {
        IArmorLogic armorLogic = getArmorLogic(stack);
        return armorLogic.getEquipmentSlot(stack);
    }

    @Nullable
    @Override
    public String getArmorTexture(@Nonnull ItemStack stack, @Nonnull Entity entity, @Nonnull EquipmentSlot slot, @Nonnull String type) {
        IArmorLogic armorLogic = getArmorLogic(stack);
        return armorLogic.getArmorTexture(stack, entity, slot, type);
    }

    @Nullable
    @Override
    public HumanoidModel getArmorModel(@Nonnull LivingEntity entityLiving, @Nonnull ItemStack itemStack, @Nonnull EquipmentSlot armorSlot, @Nonnull HumanoidModel _default) {
        IArmorLogic armorLogic = getArmorLogic(itemStack);
        return armorLogic.getArmorModel(entityLiving, itemStack, armorSlot, _default);
    }

    @Override
    public int getArmorLayersAmount(ItemStack itemStack) {
        IArmorLogic armorLogic = getArmorLogic(itemStack);
        return armorLogic.getArmorLayersAmount(itemStack);
    }

    @Override
    public int getArmorLayerColor(ItemStack itemStack, int layerIndex) {
        IArmorLogic armorLogic = getArmorLogic(itemStack);
        return armorLogic.getArmorLayerColor(itemStack, layerIndex);
    }

    @Override
    public void renderHelmetOverlay(@Nonnull ItemStack stack, @Nonnull Player player, @Nonnull PoseStack poseStack, float partialTicks) {
        IArmorLogic armorLogic = getArmorLogic(stack);
        armorLogic.renderHelmetOverlay(stack, player, poseStack, partialTicks);
    }

    private static EquipmentSlot getSlotByIndex(int index) {
        switch (index) {
            case 0:
                return EquipmentSlot.FEET;
            case 1:
                return EquipmentSlot.LEGS;
            case 2:
                return EquipmentSlot.CHEST;
            default:
                return EquipmentSlot.HEAD;
        }
    }

    @Nonnull
    public IArmorLogic getArmorLogic() {
        return armorLogic;
    }

    public static class ArmorExtendedProperties extends MetaItem.ExtendedProperties {

        private IArmorLogic armorLogic = new DummyArmorLogic();

        protected ArmorExtendedProperties(String unlocalizedName) {
            super(unlocalizedName);
            stacksTo(1);
        }

        @Nonnull
        public IArmorLogic getArmorLogic() {
            return armorLogic;
        }

        public ArmorExtendedProperties setArmorLogic(IArmorLogic armorLogic) {
            Preconditions.checkNotNull(armorLogic, "Cannot set ArmorLogic to null");
            this.armorLogic = armorLogic;
            this.armorLogic.addToolComponents(this);
            return this;
        }


        @Override
        public ArmorExtendedProperties addComponents(IItemComponent... stats) {
            super.addComponents(stats);
            return this;
        }

        @Override
        public ArmorExtendedProperties setModelAmount(int modelAmount) {
            return (ArmorExtendedProperties) super.setModelAmount(modelAmount);
        }

        @Override
        public ArmorExtendedProperties rarity(Rarity rarity) {
            return (ArmorExtendedProperties) super.rarity(rarity);
        }
    }

    @Override
    public boolean isEnchantable(@Nonnull ItemStack stack) {
        return true;
    }

    @Override
    public int getItemEnchantability() {
        return 0;
    }

    @Override
    public int getEnchantmentValue(@Nonnull ItemStack stack) {
        return 50;
    }

    @Override
    public boolean canApplyAtEnchantingTable(@Nonnull ItemStack stack, @Nonnull Enchantment enchantment) {
        EquipmentSlot slot = this.getEquipmentSlot(stack);
        if(slot == null) {
            return false;
        }

        IArmorLogic armorLogic = getArmorLogic(stack);
        if (!armorLogic.canBreakWithDamage(stack) && enchantment == Enchantments.UNBREAKING) {
            return false;
        }

        return switch (slot) {
            case HEAD -> enchantment.canEnchant(new ItemStack(Items.DIAMOND_HELMET));
            case CHEST -> enchantment.canEnchant(new ItemStack(Items.DIAMOND_CHESTPLATE));
            case LEGS -> enchantment.canEnchant(new ItemStack(Items.DIAMOND_LEGGINGS));
            case FEET -> enchantment.canEnchant(new ItemStack(Items.DIAMOND_BOOTS));
            default -> enchantment.isAllowedOnBooks();
        };
    }

}
