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
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.nemezanevem.gregtech.api.item.metaitem.MetaItem;
import net.nemezanevem.gregtech.api.item.metaitem.StandardMetaItem;
import net.nemezanevem.gregtech.api.registry.material.MaterialRegistry;
import net.nemezanevem.gregtech.api.unification.material.GtMaterials;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.TagUnifier;
import net.nemezanevem.gregtech.api.unification.material.properties.GtMaterialProperties;
import net.nemezanevem.gregtech.api.unification.material.properties.info.MaterialIconSet;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.DustProperty;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.ToolProperty;
import net.nemezanevem.gregtech.api.unification.stack.ItemMaterialInfo;
import net.nemezanevem.gregtech.api.unification.tag.TagPrefix;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.util.Util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrefixItem extends StandardMetaItem {

    private final TagPrefix prefix;
    private final Material material;

    public static final Map<TagPrefix, TagPrefix> purifyMap = new HashMap<>() {{
        put(TagPrefix.crushed, TagPrefix.crushedPurified);
        put(TagPrefix.dustImpure, TagPrefix.dust);
        put(TagPrefix.dustPure, TagPrefix.dust);
    }};

    public PrefixItem(ExtendedProperties properties, TagPrefix prefix, Material material) {
        super(properties);
        this.prefix = prefix;
        this.material = material;
    }

    public static void registerItems(DeferredRegister<Item> registry) {
        for (var entry : MaterialRegistry.MATERIALS_BUILTIN.get().getEntries()) {
            ResourceLocation loc = entry.getKey().location();
            var material = entry.getValue();
            for(var prefix : TagPrefix.values()) {
                if (prefix != null && canGenerate(prefix, material)) {
                    registry.register(prefix.name + "/" + loc.getPath(), () -> builder(prefix.name + "/" + loc.getPath())
                            .setUnificationData(prefix, material)
                            .setMaterialInfo(new ItemMaterialInfo(material.getMaterialComponents()))
                            .build(prefix, material));
                }
            }

        }
    }

    public void registerTag() {
        TagUnifier.registerTag(this, prefix, material);
        registerSpecialTag(this, material, prefix);
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

    protected static boolean canGenerate(TagPrefix TagPrefix, Material material) {
        return TagPrefix.doGenerateItem(material);
    }

    @Override
    protected int getColorForItemStack(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            if (material == null)
                return 0xFFFFFF;
            return material.getMaterialRGB();
        }
        return super.getColorForItemStack(stack, tintIndex);
    }

    @SuppressWarnings("ConstantConditions")
    public static void registerModels() {
        for(var item : MetaItem.getMetaItems()) {
            if(item instanceof PrefixItem prefixItem) {
                Map<ResourceLocation, ModelResourceLocation> alreadyRegistered = new Object2ObjectOpenHashMap<>();
                MaterialIconSet materialIconSet = prefixItem.material.getMaterialIconSet();
                TagPrefix prefix = prefixItem.prefix;

                ResourceLocation registrationKey = new ResourceLocation(materialIconSet.id.getNamespace(), materialIconSet.name + "/" + prefix.name);
                if (!alreadyRegistered.containsKey(registrationKey)) {
                    ModelResourceLocation resourceLocation = new ModelResourceLocation(prefix.materialIconType.getItemModelPath(materialIconSet), "inventory");
                    Minecraft.getInstance().getItemRenderer().getItemModelShaper().register(prefixItem, resourceLocation);
                    alreadyRegistered.put(registrationKey, resourceLocation);
                }
                ModelResourceLocation resourceLocation = alreadyRegistered.get(registrationKey);
                metaItemsModels.put(Util.getId(prefixItem), resourceLocation);
            }

        }

    }

    @Override
    public int getItemStackLimit(@Nonnull ItemStack stack) {
        if (prefix == null) return 64;
        return prefix.maxStackSize;
    }

    @Override
    public void inventoryTick(@Nonnull ItemStack itemStack, @Nonnull Level worldIn, @Nonnull Entity entityIn, int itemSlot, boolean isSelected) {
        super.inventoryTick(itemStack, worldIn, entityIn, itemSlot, isSelected);
        if (itemStack.is(this) && entityIn instanceof LivingEntity living) {
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
                    living.hurt(DamageSources.getHeatDamage().setDamageBypassesArmor(), heatDamage);
                } else if (heatDamage < 0.0) {
                    living.hurt(DamageSources.getFrostDamage().setDamageBypassesArmor(), -heatDamage);
                }
            }
        }
    }

    @Override
    public void addTooltip(@Nonnull ItemStack itemStack, @Nullable Level worldIn, @Nonnull List<Component> lines, @Nonnull TooltipFlag tooltipFlag) {
        super.addInformation(itemStack, worldIn, lines, tooltipFlag);
        int damage = itemStack.getItemDamage();
        Material material = GregTechAPI.MATERIAL_REGISTRY.getObjectById(damage);
        if (prefix == null || material == null) return;
        addMaterialTooltip(lines, itemStack);
    }

    public Material getMaterial(ItemStack itemStack) {
        return this.material;
    }

    public TagPrefix getTagPrefix() {
        return this.prefix;
    }

    @Override
    public int getBurnTime(@Nonnull ItemStack itemStack, RecipeType<?> recipeType) {
        DustProperty property = material == null ? null : material.getProperty(GtMaterialProperties.DUST.get());
        if (property != null) return (int) (property.getBurnTime() * prefix.getMaterialAmount(material) / GTValues.M);
        return super.getBurnTime(itemStack, recipeType);

    }

    @Override
    public boolean isBeaconPayment(ItemStack stack) {
        if (material != null && this.prefix != TagPrefix.ingot && this.prefix != TagPrefix.gem) {
            ToolProperty property = material.getProperty(GtMaterialProperties.TOOL.get());
            return property != null && property.getToolHarvestLevel() >= 2;
        }
        return false;
    }

    @Override
    public boolean onEntityItemUpdate(ItemEntity itemEntity) {
        if (itemEntity.getLevel().isClientSide)
            return false;

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

    protected void addMaterialTooltip(List<Component> lines, ItemStack itemStack) {
        if (this.prefix.tooltipFunc != null) {
            lines.addAll(this.prefix.tooltipFunc.apply(material));
        }
    }
}