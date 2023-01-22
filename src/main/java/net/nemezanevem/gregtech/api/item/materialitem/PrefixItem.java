package net.nemezanevem.gregtech.api.item.materialitem;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.nemezanevem.gregtech.api.item.metaitem.StandardMetaItem;
import net.nemezanevem.gregtech.api.registry.material.MaterialRegistry;
import net.nemezanevem.gregtech.api.unification.material.GtMaterials;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.TagUnifier;
import net.nemezanevem.gregtech.api.unification.material.properties.GtMaterialProperties;
import net.nemezanevem.gregtech.api.unification.material.properties.info.MaterialIconSet;
import net.nemezanevem.gregtech.api.unification.stack.UnificationEntry;
import net.nemezanevem.gregtech.api.unification.tag.TagPrefix;
import net.nemezanevem.gregtech.api.util.Util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrefixItem extends StandardMetaItem {

    private final TagPrefix prefix;

    public static final Map<TagPrefix, TagPrefix> purifyMap = new HashMap<>() {{
        put(TagPrefix.crushed, TagPrefix.crushedPurified);
        put(TagPrefix.dustImpure, TagPrefix.dust);
        put(TagPrefix.dustPure, TagPrefix.dust);
    }};

    public PrefixItem(TagPrefix TagPrefix) {
        super();
        this.prefix = TagPrefix;
    }

    @Override
    public void registerSubItems() {
        for (Material material : MaterialRegistry.MATERIALS_BUILTIN.get().getValues()) {
            ResourceLocation loc = MaterialRegistry.MATERIALS_BUILTIN.get().getKey(material);
            if (prefix != null && canGenerate(prefix, material)) {
                addItem(new ResourceLocation(new UnificationEntry(prefix, material).toString()), ForgeRegistries.ITEMS);
            }
        }
    }

    public void registerOreDict() {
        for (ResourceLocation metaItem : metaItems.keySet()) {
            Material material = MaterialRegistry.MATERIALS_BUILTIN.get().getValue(metaItem);
            TagUnifier.registerTag(this, prefix, material);
            registerSpecialTag(this, material, prefix);
        }
    }

    private void registerSpecialTag(Item item, Material material, TagPrefix prefix) {
        if (prefix.getAlternativeTagName() != null) {
            TagUnifier.registerTag(item, prefix.getAlternativeTagName(), material);
        }

        if (material == GtMaterials.Plutonium239.get()) {
            TagUnifier.registerTag(item, prefix.name() + material.toLowerUnderscoreString() + "239");
        } else if (material == GtMaterials.Uranium238.get()) {
            TagUnifier.registerTag(item, prefix.name() + material.toLowerUnderscoreString() + "238");
        } else if (material == GtMaterials.Saltpeter.get()) {
            TagUnifier.registerTag(item, prefix.name() + material.toLowerUnderscoreString());
        }
    }

    protected boolean canGenerate(TagPrefix TagPrefix, Material material) {
        return TagPrefix.doGenerateItem(material);
    }

    @Override
    protected int getColorForItemStack(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            Material material = MaterialRegistry.MATERIALS_BUILTIN.get().getValue(stack.getItem());
            if (material == null)
                return 0xFFFFFF;
            return material.getMaterialRGB();
        }
        return super.getColorForItemStack(stack, tintIndex);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void registerModels() {
        Map<ResourceLocation, ModelResourceLocation> alreadyRegistered = new Object2ObjectOpenHashMap<>();
        for (ResourceLocation metaItem : metaItems.keySet()) {
            MaterialIconSet materialIconSet = MaterialRegistry.MATERIALS_BUILTIN.get().getValue(metaItem).getMaterialIconSet();

            ResourceLocation registrationKey = (short) (prefix.id + materialIconSet.id);
            if (!alreadyRegistered.containsKey(registrationKey)) {
                ModelResourceLocation resourceLocation = new ModelResourceLocation(prefix.materialIconType.getItemModelPath(materialIconSet), "inventory");
                Minecraft.getInstance().getItemRenderer().getItemModelShaper().register(this, resourceLocation);
                alreadyRegistered.put(registrationKey, resourceLocation);
            }
            ModelResourceLocation resourceLocation = alreadyRegistered.get(registrationKey);
            metaItemsModels.put(metaItem, resourceLocation);
        }

        // Make some default models for meta prefix items without any materials associated
        if (metaItems.keySet().isEmpty()) {
            MaterialIconSet defaultIcon = MaterialIconSet.DULL;
            ResourceLocation defaultLocation = TagPrefix.ingot.materialIconType.getItemModelPath(defaultIcon);
            ModelBakery.registerItemVariants(this, defaultLocation);
        }
    }

    @Override
    public int getItemStackLimit(@Nonnull ItemStack stack) {
        if (prefix == null) return 64;
        return prefix.maxStackSize;
    }

    @Override
    public void onUpdate(@Nonnull ItemStack itemStack, @Nonnull Level worldIn, @Nonnull Entity entityIn, int itemSlot, boolean isSelected) {
        super.onUpdate(itemStack, worldIn, entityIn, itemSlot, isSelected);
        if (metaItems.containsKey(Util.getId(itemStack.getItem())) && entityIn instanceof LivingEntity living) {
            if (entityIn.tickCount % 20 == 0) {
                if (prefix.heatDamageFunction == null) return;

                Material material = getMaterial(itemStack);
                if (material == null || !material.hasProperty(GtMaterialProperties.BLAST.get())) return;

                float heatDamage = prefix.heatDamageFunction.apply(material.getBlastTemperature());
                ItemStack armor = living.getItemBySlot(EquipmentSlot.CHEST);
                if (!armor.isEmpty() && armor.getItem() instanceof ArmorMetaItem<?>) {
                    heatDamage *= ((ArmorMetaItem<?>) armor.getItem()).getItem(armor).getArmorLogic().getHeatResistance();
                }

                if (heatDamage > 0.0) {
                    entity.attackEntityFrom(DamageSources.getHeatDamage().setDamageBypassesArmor(), heatDamage);
                } else if (heatDamage < 0.0) {
                    entity.attackEntityFrom(DamageSources.getFrostDamage().setDamageBypassesArmor(), -heatDamage);
                }
            }
        }
    }

    @Override
    public void addTo(@Nonnull ItemStack itemStack, @Nullable Level worldIn, @Nonnull List<Component> lines, @Nonnull TooltipFlag tooltipFlag) {
        super.addInformation(itemStack, worldIn, lines, tooltipFlag);
        int damage = itemStack.getItemDamage();
        Material material = GregTechAPI.MATERIAL_REGISTRY.getObjectById(damage);
        if (prefix == null || material == null) return;
        addMaterialTooltip(lines, itemStack);
    }

    public Material getMaterial(ItemStack itemStack) {
        int damage = itemStack.getItemDamage();
        return GregTechAPI.MATERIAL_REGISTRY.getObjectById(damage);
    }

    public TagPrefix getTagPrefix() {
        return this.prefix;
    }

    @Override
    public int getItemBurnTime(@Nonnull ItemStack itemStack) {
        int damage = itemStack.getItemDamage();
        Material material = GregTechAPI.MATERIAL_REGISTRY.getObjectById(damage);
        DustProperty property = material == null ? null : material.getProperty(PropertyKey.DUST);
        if (property != null) return (int) (property.getBurnTime() * prefix.getMaterialAmount(material) / GTValues.M);
        return super.getItemBurnTime(itemStack);

    }

    @Override
    public boolean isBeaconPayment(ItemStack stack) {
        int damage = stack.getMetadata();

        Material material = GregTechAPI.MATERIAL_REGISTRY.getObjectById(damage);
        if (material != null && this.prefix != TagPrefix.ingot && this.prefix != TagPrefix.gem) {
            ToolProperty property = material.getProperty(PropertyKey.TOOL);
            return property != null && property.getToolHarvestLevel() >= 2;
        }
        return false;
    }

    @Override
    public boolean onEntityItemUpdate(ItemEntity itemEntity) {
        if (itemEntity.getLevel().isClientSide)
            return false;

        Material material = GregTechAPI.MATERIAL_REGISTRY.getObjectById(damage);
        if (!purifyMap.containsKey(this.prefix))
            return false;

        BlockPos blockPos = new BlockPos(itemEntity.position());
        BlockState blockState = itemEntity.getLevel().getBlockState(blockPos);

        if (!(blockState.getBlock() instanceof LayeredCauldronBlock cauldronBlock))
            return false;

        int waterLevel = blockState.getValue(LayeredCauldronBlock.LEVEL);
        if (waterLevel == 0)
            return false;

        itemEntity.getLevel().setBlock(blockPos,
                blockState.setValue(LayeredCauldronBlock.LEVEL, waterLevel - 1), 3);
        Item replacementStack = TagUnifier.get(purifyMap.get(prefix), material);
        itemEntity.setItem(new ItemStack(replacementStack, itemEntity.getItem().getCount()));
        return false;
    }

    protected void addMaterialTooltip(List<String> lines, ItemStack itemStack) {
        if (this.prefix.tooltipFunc != null) {
            lines.addAll(this.prefix.tooltipFunc.apply(this.getMaterial(itemStack)));
        }
    }
}