package net.nemezanevem.gregtech.api.item.toolitem;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.capability.GregtechCapabilities;
import net.nemezanevem.gregtech.api.capability.IElectricItem;
import net.nemezanevem.gregtech.api.capability.impl.CombinedCapabilityProvider;
import net.nemezanevem.gregtech.api.capability.impl.ElectricItem;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.widgets.ClickButtonWidget;
import net.nemezanevem.gregtech.api.gui.widgets.DynamicLabelWidget;
import net.nemezanevem.gregtech.api.item.gui.ItemUIFactory;
import net.nemezanevem.gregtech.api.item.gui.PlayerInventoryHolder;
import net.nemezanevem.gregtech.api.item.toolitem.behavior.IToolBehavior;
import net.nemezanevem.gregtech.api.recipe.ModHandler;
import net.nemezanevem.gregtech.api.unification.material.GtMaterials;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.properties.GtMaterialProperties;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.DustProperty;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.ToolProperty;
import net.nemezanevem.gregtech.api.unification.stack.UnificationEntry;
import net.nemezanevem.gregtech.api.unification.tag.TagPrefix;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.client.util.ToolChargeBarRenderer;
import net.nemezanevem.gregtech.client.util.TooltipHelper;
import net.nemezanevem.gregtech.common.ConfigHolder;
import org.apache.commons.lang3.text.WordUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.minecraft.world.item.enchantment.EnchantmentCategory.*;

public interface IGTTool extends ItemUIFactory {

    /**
     * @return the modid of the tool
     */
    String getDomain();

    /**
     * @return the name of the tool
     */
    String getId();

    boolean isElectric();

    int getElectricTier();

    IGTToolDefinition getToolStats();

    @Nullable
    SoundEvent getSound();

    boolean playSoundOnBlockDestroy();

    @Nullable
    String getOreDictName();

    @Nullable
    Supplier<ItemStack> getMarkerItem();

    default Item get() {
        return (Item) this;
    }

    default ItemStack getRaw() {
        ItemStack stack = new ItemStack(get());
        getToolTag(stack);
        getBehaviorsTag(stack);
        return stack;
    }

    default ItemStack get(Material material) {
        ItemStack stack = new ItemStack(get());

        CompoundTag stackCompound = Util.getOrCreateNbtCompound(stack);
        stackCompound.putBoolean(DISALLOW_CONTAINER_ITEM_KEY, false);

        CompoundTag toolTag = getToolTag(stack);

        // don't show the normal vanilla damage and attack speed tooltips,
        // we handle those ourselves
        stackCompound.putInt(HIDE_FLAGS, 2);

        // Set Material
        toolTag.setString(MATERIAL_KEY, material.toString());

        // Set other tool stats (durability)
        ToolProperty toolProperty = material.getProperty(GtMaterialProperties.TOOL.get());
        toolTag.putInt(MAX_DURABILITY_KEY, toolProperty.getToolDurability());
        toolTag.putInt(DURABILITY_KEY, 0);
        if (toolProperty.getUnbreakable()) {
            stackCompound.putBoolean(UNBREAKABLE_KEY, true);
        }

        // Set material enchantments
        toolProperty.getEnchantments().forEach((enchantment, level) -> {
            if (stack.getItem().canApplyAtEnchantingTable(stack, enchantment)) {
                stack.enchant(enchantment, level);
            }
        });

        // Set behaviours
        CompoundTag behaviourTag = getBehaviorsTag(stack);
        getToolStats().getBehaviors().forEach(behavior -> behavior.addBehaviorNBT(stack, behaviourTag));

        AoESymmetrical aoeDefinition = getToolStats().getAoEDefinition(stack);

        if (aoeDefinition != AoESymmetrical.none()) {
            behaviourTag.putInt(MAX_AOE_COLUMN_KEY, aoeDefinition.column);
            behaviourTag.putInt(MAX_AOE_ROW_KEY, aoeDefinition.row);
            behaviourTag.putInt(MAX_AOE_LAYER_KEY, aoeDefinition.layer);
            behaviourTag.putInt(AOE_COLUMN_KEY, aoeDefinition.column);
            behaviourTag.putInt(AOE_ROW_KEY, aoeDefinition.row);
            behaviourTag.putInt(AOE_LAYER_KEY, aoeDefinition.layer);
        }

        if (toolProperty.isMagnetic()) {
            behaviourTag.putBoolean(RELOCATE_MINED_BLOCKS_KEY, true);
        }

        return stack;
    }

    default ItemStack get(Material material, long defaultCharge, long defaultMaxCharge) {
        ItemStack stack = get(material);
        if (isElectric()) {
            LazyOptional<IElectricItem> electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            if (electricItem.isPresent()) {
                if(electricItem.resolve().get() instanceof ElectricItem item) {
                    item.setMaxChargeOverride(defaultMaxCharge);
                    item.setCharge(defaultCharge);
                }
            }
        }
        return stack;
    }

    default ItemStack get(Material material, long defaultMaxCharge) {
        return get(material, defaultMaxCharge, defaultMaxCharge);
    }

    default Material getToolMaterial(ItemStack stack) {
        CompoundTag toolTag = getToolTag(stack);
        String string = toolTag.getString(MATERIAL_KEY);
        Material material = GregTechAPI.MaterialRegistry.get(string);
        if (material == null) {
            toolTag.putString(MATERIAL_KEY, (material = GtMaterials.Iron.get()).toString());
        }
        return material;
    }

    @Nullable
    default ToolProperty getToolProperty(ItemStack stack) {
        return getToolMaterial(stack).getProperty(GtMaterialProperties.TOOL.get());
    }

    @Nullable
    default DustProperty getDustProperty(ItemStack stack) {
        return getToolMaterial(stack).getProperty(GtMaterialProperties.DUST.get());
    }

    default float getMaterialToolSpeed(ItemStack stack) {
        ToolProperty toolProperty = getToolProperty(stack);
        return toolProperty == null ? 0F : toolProperty.getToolSpeed();
    }

    default float getMaterialAttackDamage(ItemStack stack) {
        ToolProperty toolProperty = getToolProperty(stack);
        return toolProperty == null ? 0F : toolProperty.getToolAttackDamage();
    }

    default float getMaterialAttackSpeed(ItemStack stack) {
        ToolProperty toolProperty = getToolProperty(stack);
        return toolProperty == null ? 0F : toolProperty.getToolAttackSpeed();
    }

    default int getMaterialDurability(ItemStack stack) {
        ToolProperty toolProperty = getToolProperty(stack);
        return toolProperty == null ? 0 : toolProperty.getToolDurability();
    }

    default int getMaterialEnchantability(ItemStack stack) {
        ToolProperty toolProperty = getToolProperty(stack);
        return toolProperty == null ? 0 : toolProperty.getToolEnchantability();
    }

    default int getMaterialHarvestLevel(ItemStack stack) {
        ToolProperty toolProperty = getToolProperty(stack);
        return toolProperty == null ? 0 : toolProperty.getToolHarvestLevel();
    }

    default long getMaxCharge(ItemStack stack) {
        if (isElectric()) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains(MAX_CHARGE_KEY, Tag.TAG_LONG)) {
                return tag.getLong(MAX_CHARGE_KEY);
            }
        }
        return -1L;
    }

    default long getCharge(ItemStack stack) {
        if (isElectric()) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains(CHARGE_KEY, Tag.TAG_LONG)) {
                return tag.getLong(CHARGE_KEY);
            }
        }
        return -1L;
    }

    default float getTotalToolSpeed(ItemStack stack) {
        CompoundTag toolTag = getToolTag(stack);
        if (toolTag.contains(TOOL_SPEED_KEY, Tag.TAG_FLOAT)) {
            return toolTag.getFloat(TOOL_SPEED_KEY);
        }
        float toolSpeed = getToolStats().getEfficiencyMultiplier(stack) * getMaterialToolSpeed(stack) + getToolStats().getBaseEfficiency(stack);
        toolTag.putFloat(TOOL_SPEED_KEY, toolSpeed);
        return toolSpeed;
    }

    default float getTotalAttackDamage(ItemStack stack) {
        CompoundTag toolTag = getToolTag(stack);
        if (toolTag.contains(ATTACK_DAMAGE_KEY, Tag.TAG_FLOAT)) {
            return toolTag.getFloat(ATTACK_DAMAGE_KEY);
        }
        float baseDamage = getToolStats().getBaseDamage(stack);
        float attackDamage = 0;
        // represents a tool that should always have an attack damage value of 0
        if (baseDamage != Float.MIN_VALUE) {
            attackDamage = getMaterialAttackDamage(stack) + baseDamage;
        }
        toolTag.putFloat(ATTACK_DAMAGE_KEY, attackDamage);
        return attackDamage;
    }

    default float getTotalAttackSpeed(ItemStack stack) {
        CompoundTag toolTag = getToolTag(stack);
        if (toolTag.contains(ATTACK_SPEED_KEY, Tag.TAG_FLOAT)) {
            return toolTag.getFloat(ATTACK_SPEED_KEY);
        }
        float attackSpeed = getMaterialAttackSpeed(stack) + getToolStats().getAttackSpeed(stack);
        toolTag.putFloat(ATTACK_SPEED_KEY, attackSpeed);
        return attackSpeed;
    }

    default int getTotalMaxDurability(ItemStack stack) {
        CompoundTag toolTag = getToolTag(stack);
        if (toolTag.contains(MAX_DURABILITY_KEY, Tag.TAG_INT)) {
            return toolTag.getInt(MAX_DURABILITY_KEY);
        }
        int maxDurability = getMaterialDurability(stack) + getToolStats().getBaseDurability(stack);
        toolTag.putInt(MAX_DURABILITY_KEY, maxDurability);
        return maxDurability;
    }

    default int getTotalEnchantability(ItemStack stack) {
        CompoundTag toolTag = getToolTag(stack);
        if (toolTag.contains(ENCHANTABILITY_KEY, Tag.TAG_INT)) {
            return toolTag.getInt(ENCHANTABILITY_KEY);
        }
        int enchantability = getMaterialEnchantability(stack);
        toolTag.putInt(ENCHANTABILITY_KEY, enchantability);
        return enchantability;
    }

    default int getTotalHarvestLevel(ItemStack stack) {
        CompoundTag toolTag = getToolTag(stack);
        if (toolTag.contains(HARVEST_LEVEL_KEY, Tag.TAG_INT)) {
            return toolTag.getInt(HARVEST_LEVEL_KEY);
        }
        int harvestLevel = getMaterialHarvestLevel(stack) + getToolStats().getBaseQuality(stack);
        toolTag.putInt(HARVEST_LEVEL_KEY, harvestLevel);
        return harvestLevel;
    }

    default AoESymmetrical getMaxAoEDefinition(ItemStack stack) {
        return AoESymmetrical.readMax(getBehaviorsTag(stack));
    }

    default AoESymmetrical getAoEDefinition(ItemStack stack) {
        return AoESymmetrical.read(getToolTag(stack), getMaxAoEDefinition(stack));
    }

    // Item.class methods
    default float definition$getDestroySpeed(ItemStack stack, BlockState state) {
        // special case check (mostly for the sword)
        float specialValue = getDestroySpeed(state, getToolClasses(stack));
        if (specialValue != -1) return specialValue;

        if (isToolEffective(state, getToolClasses(stack), getTotalHarvestLevel(stack))) {
            return getTotalToolSpeed(stack);
        }

        return getToolStats().isToolEffective(state) ? getTotalToolSpeed(stack) : 1.0F;
    }

    default boolean definition$hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        getToolStats().getBehaviors().forEach(behavior -> behavior.hitEntity(stack, target, attacker));
        damageItem(stack, attacker, getToolStats().getToolDamagePerAttack(stack));
        return true;
    }

    default boolean definition$onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        if (player.level.isClientSide) return false;
        getToolStats().getBehaviors().forEach(behavior -> behavior.onBlockStartBreak(stack, pos, player));

        if (!player.isCrouching()) {
            ServerPlayer playerMP = (ServerPlayer) player;
            int result = -1;
            if (isTool(stack, ToolClass.SHEARS)) {
                result = shearBlockRoutine(playerMP, stack, pos);
            }
            if (result != 0) {
                // prevent exploits with instantly breakable blocks
                BlockState state = player.level.getBlockState(pos);
                boolean effective = false;
                for (String type : getToolClasses(stack)) {
                    if (state.getBlock().isToolEffective(type, state)) {
                        effective = true;
                        break;
                    }
                }

                effective |= isToolEffective(state, getToolClasses(stack), getTotalHarvestLevel(stack));

                if (effective) {
                    if (areaOfEffectBlockBreakRoutine(stack, playerMP)) {
                        if (playSoundOnBlockDestroy()) playSound(player);
                    } else {
                        if (result == -1) {
                            treeFellingRoutine(playerMP, stack, pos);
                            if (playSoundOnBlockDestroy()) playSound(player);
                        } else {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    default boolean definition$onBlockDestroyed(ItemStack stack, Level worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        if (!worldIn.isClientSide) {
            getToolStats().getBehaviors().forEach(behavior -> behavior.onBlockDestroyed(stack, worldIn, state, pos, entityLiving));

            if ((double) state.getDestroySpeed(worldIn, pos) != 0.0D) {
                damageItem(stack, entityLiving, getToolStats().getToolDamagePerBlockBreak(stack));
            }
            if (entityLiving instanceof Player && playSoundOnBlockDestroy()) {
                // sneaking disables AOE, which means it is okay to play the sound
                // not checking this means the sound will play for every AOE broken block, which is very loud
                if (entityLiving.isCrouching()) {
                    playSound((Player) entityLiving);
                }
            }
        }
        return true;
    }

    default boolean definition$getIsRepairable(ItemStack toRepair, ItemStack repair) {
        // full durability tools in the left slot are not repairable
        // this is needed so enchantment merging works when both tools are full durability
        if (toRepair.getItemDamage() == 0) return false;
        if (repair.getItem() instanceof IGTTool) {
            return getToolMaterial(toRepair) == ((IGTTool) repair.getItem()).getToolMaterial(repair);
        }
        UnificationEntry entry = OreDictUnifier.getUnificationEntry(repair);
        if (entry == null || entry.material == null) return false;
        if (entry.material == getToolMaterial(toRepair)) {
            // special case wood to allow Wood Planks
            if (ModHandler.isMaterialWood(entry.material)) {
                return entry.tagPrefix == TagPrefix.plank;
            }
            // Gems can use gem and plate, Ingots can use ingot and plate
            if (entry.orePrefix == TagPrefix.plate) {
                return true;
            }
            if (entry.material.hasProperty(GtMaterialProperties.INGOT.get())) {
                return entry.orePrefix == TagPrefix.ingot;
            }
            if (entry.material.hasProperty(GtMaterialProperties.GEM.get())) {
                return entry.orePrefix == TagPrefix.gem;
            }
        }
        return false;
    }

    default Multimap<String, AttributeModifier> definition$getAttributeModifiers(EntityEquipmentSlot equipmentSlot, ItemStack stack) {
        Multimap<String, AttributeModifier> multimap = HashMultimap.create();
        if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", getTotalAttackDamage(stack), 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", Math.max(-3.9D, getTotalAttackSpeed(stack)), 0));
        }
        return multimap;
    }

    default int definition$getHarvestLevel(ItemStack stack, String toolClass, @Nullable Player player, @Nullable BlockState blockState) {
        return get().getToolClasses(stack).contains(toolClass) ? getTotalHarvestLevel(stack) : -1;
    }

    default boolean definition$canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return getToolStats().getBehaviors().stream().anyMatch(behavior -> behavior.canDisableShield(stack, shield, entity, attacker));
    }

    default boolean definition$doesSneakBypassUse(@Nonnull ItemStack stack, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull Player player) {
        return getToolStats().doesSneakBypassUse();
    }

    default boolean definition$shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return oldStack.getItem() != newStack.getItem() || oldStack.getItemDamage() < newStack.getItemDamage();
    }

    default boolean definition$hasContainerItem(ItemStack stack) {
        return stack.getTagCompound() == null || !stack.getTagCompound().getBoolean(DISALLOW_CONTAINER_ITEM_KEY);
    }

    default ItemStack definition$getContainerItem(ItemStack stack) {
        // Sanity-check, callers should really validate with hasContainerItem themselves...
        if (!definition$hasContainerItem(stack)) {
            return ItemStack.EMPTY;
        }
        stack = stack.copy();
        Player player = ForgeHooks.getCraftingPlayer();
        damageItemWhenCrafting(stack, player);
        playCraftingSound(player, stack);
        // We cannot simply return the copied stack here because Forge's bug
        // Introduced here: https://github.com/MinecraftForge/MinecraftForge/pull/3388
        // Causing PlayerDestroyItemEvent to never be fired under correct circumstances.
        // While preliminarily fixing ItemStack being null in ForgeHooks#getContainerItem in the PR
        // The semantics was misunderstood, any stack that are "broken" (damaged beyond maxDamage)
        // Will be "empty" ItemStacks (while not == ItemStack.EMPTY, but isEmpty() == true)
        // PlayerDestroyItemEvent will not be fired correctly because of this oversight.
        if (stack.isEmpty()) { // Equal to listening to PlayerDestroyItemEvent
            return getToolStats().getBrokenStack();
        }
        return stack;
    }

    default boolean definition$shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (getCharge(oldStack) != getCharge(newStack)) {
            return slotChanged;
        }
        return !oldStack.equals(newStack);
    }

    default boolean definition$onEntitySwing(LivingEntity entityLiving, ItemStack stack) {
        getToolStats().getBehaviors().forEach(behavior -> behavior.onEntitySwing(entityLiving, stack));
        return false;
    }

    default boolean definition$canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack, Player player) {
        return true;
    }

    default boolean definition$isDamaged(ItemStack stack) {
        return definition$getDamage(stack) > 0;
    }

    default int definition$getDamage(ItemStack stack) {
        CompoundTag toolTag = getToolTag(stack);
        if (toolTag.hasKey(DURABILITY_KEY, Tag.TAG_INT)) {
            return toolTag.getInt(DURABILITY_KEY);
        }
        toolTag.putInt(DURABILITY_KEY, 0);
        return 0;
    }

    default int definition$getMaxDamage(ItemStack stack) {
        return getTotalMaxDurability(stack);
    }

    default void definition$setDamage(ItemStack stack, int durability) {
        CompoundTag toolTag = getToolTag(stack);
        toolTag.putInt(DURABILITY_KEY, durability);
    }

    default double definition$getDurabilityForDisplay(ItemStack stack) {
        int damage = stack.getItem().getDamage(stack);
        int maxDamage = stack.getItem().getMaxDamage(stack);
        if (damage == 0) return 1.0;
        return (double) (maxDamage - damage) / (double) maxDamage;
    }

    @Nullable
    default ICapabilityProvider definition$initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        List<ICapabilityProvider> providers = new ArrayList<>();
        if (isElectric()) {
            providers.add(ElectricStats.createElectricItem(0L, getElectricTier()).createProvider(stack));
        }
        for (IToolBehavior behavior : getToolStats().getBehaviors()) {
            ICapabilityProvider behaviorProvider = behavior.createProvider(stack, nbt);
            if (behaviorProvider != null) {
                providers.add(behaviorProvider);
            }
        }
        if (providers.isEmpty()) return null;
        if (providers.size() == 1) return providers.get(0);
        return new CombinedCapabilityProvider(providers);
    }

    default EnumActionResult definition$onItemUseFirst(@Nonnull Player player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Direction facing, float hitX, float hitY, float hitZ, @Nonnull EnumHand hand) {
        for (IToolBehavior behavior : getToolStats().getBehaviors()) {
            if (behavior.onItemUseFirst(player, world, pos, facing, hitX, hitY, hitZ, hand) == EnumActionResult.SUCCESS)  {
                return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.PASS;
    }

    default EnumActionResult definition$onItemUse(Player player, World world, BlockPos pos, EnumHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        for (IToolBehavior behavior : getToolStats().getBehaviors()) {
            if (behavior.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ) == EnumActionResult.SUCCESS)  {
                return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.PASS;
    }

    default ActionResult<ItemStack> definition$onItemRightClick(World world, Player player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isClientSide) {
            // TODO: relocate to keybind action when keybind PR happens
            if (player.isSneaking() && getMaxAoEDefinition(stack) != AoESymmetrical.none()) {
                PlayerInventoryHolder.openHandItemUI(player, hand);
                return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
            }
        }

        for (IToolBehavior behavior : getToolStats().getBehaviors()) {
            if (behavior.onItemRightClick(world, player, hand).getType() == EnumActionResult.SUCCESS) {
                return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
            }
        }
        return ActionResult.newResult(EnumActionResult.PASS, stack);
    }

    default void definition$getSubItems(@Nonnull NonNullList<ItemStack> items) {
        if (getMarkerItem() != null) {
            items.add(getMarkerItem().get());
        } else if (isElectric()) {
            items.add(get(Materials.Iron, Integer.MAX_VALUE));
        } else {
            items.add(get(Materials.Iron));
        }
    }

    // Client-side methods

    @SideOnly(Side.CLIENT)
    default void definition$addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, ITooltipFlag flag) {
        if (!(stack.getItem() instanceof IGTTool)) return;
        IGTTool tool = (IGTTool) stack.getItem();

        CompoundTag tagCompound = stack.getTagCompound();
        if (tagCompound == null) return;

        IGTToolDefinition toolStats = tool.getToolStats();

        // electric info
        if (this.isElectric()) {
            tooltip.add(I18n.format("metaitem.generic.electric_item.tooltip",
                    getCharge(stack),
                    getMaxCharge(stack),
                    GTValues.VNF[getElectricTier()]));
        }

        // durability info
        if (!tagCompound.getBoolean(UNBREAKABLE_KEY)) {
            int damageRemaining = tool.getTotalMaxDurability(stack) - stack.getItemDamage();
            if (toolStats.isSuitableForCrafting(stack)) {
                tooltip.add(I18n.format("item.gt.tool.tooltip.crafting_uses", GTUtility.formatNumbers(damageRemaining / Math.max(1, toolStats.getToolDamagePerCraft(stack)))));
            }

            // Plus 1 to match vanilla behavior where tools can still be used once at zero durability
            tooltip.add(I18n.format("item.gt.tool.tooltip.general_uses", GTUtility.formatNumbers(damageRemaining)));
        }

        // attack info
        if (toolStats.isSuitableForAttacking(stack)) {
            tooltip.add(I18n.format("item.gt.tool.tooltip.attack_damage", GTUtility.formatNumbers(2 + tool.getTotalAttackDamage(stack))));
            tooltip.add(I18n.format("item.gt.tool.tooltip.attack_speed", GTUtility.formatNumbers(4 + tool.getTotalAttackSpeed(stack))));
        }

        // mining info
        if (toolStats.isSuitableForBlockBreak(stack)) {
            tooltip.add(I18n.format("item.gt.tool.tooltip.mining_speed", GTUtility.formatNumbers(tool.getTotalToolSpeed(stack))));

            int harvestLevel = tool.getTotalHarvestLevel(stack);
            String harvestName = "item.gt.tool.harvest_level." + harvestLevel;
            if (I18n.hasKey(harvestName)) { // if there's a defined name for the harvest level, use it
                tooltip.add(I18n.format("item.gt.tool.tooltip.harvest_level_extra", harvestLevel, I18n.format(harvestName)));
            } else {
                tooltip.add(I18n.format("item.gt.tool.tooltip.harvest_level", harvestLevel));
            }
        }

        // behaviors
        boolean addedBehaviorNewLine = false;
        AoESymmetrical aoeDefinition = ToolHelper.getAoEDefinition(stack);

        if (aoeDefinition != AoESymmetrical.none()) {
            addedBehaviorNewLine = tooltip.add("");
            tooltip.add(I18n.format("item.gt.tool.behavior.aoe_mining",
                    aoeDefinition.column * 2 + 1, aoeDefinition.row * 2 + 1, aoeDefinition.layer + 1));
        }

        CompoundTag behaviorsTag = getBehaviorsTag(stack);
        if (behaviorsTag.getBoolean(RELOCATE_MINED_BLOCKS_KEY)) {
            if (!addedBehaviorNewLine) {
                addedBehaviorNewLine = true;
                tooltip.add("");
            }
            tooltip.add(I18n.format("item.gt.tool.behavior.relocate_mining"));
        }

        if (!addedBehaviorNewLine && !toolStats.getBehaviors().isEmpty()) {
            tooltip.add("");
        }
        toolStats.getBehaviors().forEach(behavior -> behavior.addInformation(stack, world, tooltip, flag));

        // unique tooltip
        String uniqueTooltip = "item.gt.tool." + getId() + ".tooltip";
        if (I18n.hasKey(uniqueTooltip)) {
            tooltip.add("");
            tooltip.add(I18n.format(uniqueTooltip));
        }

        tooltip.add("");

        // valid tools
        tooltip.add(I18n.format("item.gt.tool.usable_as", stack.getItem().getToolClasses(stack).stream()
                .map(GTUtility::convertUnderscoreToSpace)
                .map(WordUtils::capitalize)
                .collect(Collectors.joining(", "))));

        // repair info
        if (TooltipHelper.isShiftDown()) {
            Material material = getToolMaterial(stack);
            String materialName = I18n.format(getToolMaterial(stack).getUnlocalizedName());

            Collection<String> repairItems = new ArrayList<>();
            if (ModHandler.isMaterialWood(material)) {
                repairItems.add(I18n.format("item.material.oreprefix.plank", materialName));
            } else {
                if (material.hasProperty(GtMaterialProperties.INGOT.get())) {
                    repairItems.add(I18n.format("item.material.oreprefix.ingot", materialName));
                } else if (material.hasProperty(GtMaterialProperties.GEM.get())) {
                    repairItems.add(I18n.format("item.material.oreprefix.gem", materialName));
                }
                repairItems.add(I18n.format("item.material.oreprefix.plate", materialName));
            }
            tooltip.add(I18n.format("item.gt.tool.tooltip.repair_material", String.join(", ", repairItems)));

            if (this.isElectric()) {
                tooltip.add(I18n.format("item.gt.tool.replace_tool_head"));
            }
        } else {
            tooltip.add(I18n.format("item.gt.tool.tooltip.repair_info"));
        }
    }

    default boolean definition$canApplyAtEnchantingTable(@Nonnull ItemStack stack, Enchantment enchantment) {
        if (stack.isEmpty()) return false;

        // special case enchants from other mods
        switch (enchantment.getDescriptionId()) {
            case "enchantment.cofhcore.smashing":
                // block cofhcore smashing enchant from all tools
                return false;
            case "enchantment.autosmelt": // endercore
            case "enchantment.cofhcore.smelting": // cofhcore
            case "enchantment.as.smelting": // astral sorcery
                // block autosmelt enchants from AoE and Tree-Felling tools
                return getToolStats().getAoEDefinition(stack) == AoESymmetrical.none() && !getBehaviorsTag(stack).hasKey(TREE_FELLING_KEY);
        }

        // Block Mending and Unbreaking on Electric tools
        if (isElectric() && (enchantment instanceof EnchantmentMending || enchantment instanceof EnchantmentDurability)) {
            return false;
        }

        if (enchantment.rt == null) return true;
        // bypass EnumEnchantmentType#canEnchantItem and define custom stack-aware logic.
        // the Minecraft method takes an Item, and does not respect NBT nor meta.
        switch (enchantment.type) {
            case DIGGER: {
                return getToolStats().isSuitableForBlockBreak(stack);
            }
            case WEAPON: {
                return getToolStats().isSuitableForAttacking(stack);
            }
            case BREAKABLE:
                return stack.getTagCompound() != null && !stack.getTagCompound().getBoolean(UNBREAKABLE_KEY);
            case ALL: {
                return true;
            }
        }

        ToolProperty property = getToolProperty(stack);
        if (property == null) return false;

        // Check for any special enchantments specified by the material of this Tool
        if (!property.getEnchantments().isEmpty() && property.getEnchantments().containsKey(enchantment)) {
            return true;
        }

        // Check for any additional Enchantment Types added in the builder
        return getToolStats().isEnchantable(stack) && getToolStats().canApplyEnchantment(stack, enchantment);
    }

    @SideOnly(Side.CLIENT)
    default int getColor(ItemStack stack, int tintIndex) {
        return tintIndex % 2 == 1 ? getToolMaterial(stack).getMaterialRGB() : 0xFFFFFF;
    }

    @SideOnly(Side.CLIENT)
    default String getModelPath() {
        return getDomain() + ":" + "tools/" + getId();
    }

    @SideOnly(Side.CLIENT)
    default ModelResourceLocation getModelLocation() {
        return new ModelResourceLocation(getModelPath(), "inventory");
    }

    // Sound Playing
    default void playCraftingSound(Player player, ItemStack stack) {
        // player null check for things like auto-crafters
        if (ConfigHolder.client.toolCraftingSounds && getSound() != null && player != null) {
            if (canPlaySound(stack)) {
                setLastCraftingSoundTime(stack);
                player.getEntityWorld().playSound(null, player.posX, player.posY, player.posZ, getSound(), SoundCategory.PLAYERS, 1F, 1F);
            }
        }
    }

    default void setLastCraftingSoundTime(ItemStack stack) {
        getToolTag(stack).putInt(LAST_CRAFTING_USE_KEY, (int) System.currentTimeMillis());
    }

    default boolean canPlaySound(ItemStack stack) {
        return Math.abs((int) System.currentTimeMillis() - getToolTag(stack).getInt(LAST_CRAFTING_USE_KEY)) > 1000;
    }

    default void playSound(Player player) {
        if (ConfigHolder.client.toolUseSounds && getSound() != null) {
            player.getEntityWorld().playSound(null, player.posX, player.posY, player.posZ, getSound(), SoundCategory.PLAYERS, 1F, 1F);
        }
    }

    default ModularUI createUI(PlayerInventoryHolder holder, Player entityPlayer) {
        CompoundTag tag = getBehaviorsTag(holder.getCurrentItem());
        AoESymmetrical defaultDefinition = getMaxAoEDefinition(holder.getCurrentItem());
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 120, 80)
                .label(6, 10, "item.gt.tool.aoe.columns")
                .label(49, 10, "item.gt.tool.aoe.rows")
                .label(79, 10, "item.gt.tool.aoe.layers")
                .widget(new ClickButtonWidget(15, 24, 20, 20, "+", data -> {
                    AoESymmetrical.increaseColumn(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new ClickButtonWidget(15, 44, 20, 20, "-", data -> {
                    AoESymmetrical.decreaseColumn(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new ClickButtonWidget(50, 24, 20, 20, "+", data -> {
                    AoESymmetrical.increaseRow(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new ClickButtonWidget(50, 44, 20, 20, "-", data -> {
                    AoESymmetrical.decreaseRow(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new ClickButtonWidget(85, 24, 20, 20, "+", data -> {
                    AoESymmetrical.increaseLayer(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new ClickButtonWidget(85, 44, 20, 20, "-", data -> {
                    AoESymmetrical.decreaseLayer(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new DynamicLabelWidget(23, 65, () ->
                        Integer.toString(1 + 2 * AoESymmetrical.getColumn(getBehaviorsTag(holder.getCurrentItem()), defaultDefinition))))
                .widget(new DynamicLabelWidget(58, 65, () ->
                        Integer.toString(1 + 2 * AoESymmetrical.getRow(getBehaviorsTag(holder.getCurrentItem()), defaultDefinition))))
                .widget(new DynamicLabelWidget(93, 65, () ->
                        Integer.toString(1 + AoESymmetrical.getLayer(getBehaviorsTag(holder.getCurrentItem()), defaultDefinition))))
                .build(holder, entityPlayer);
    }

    Set<String> getToolClasses(ItemStack stack);

    // Extended Interfaces

    // IAEWrench

    /**
     * Check if the wrench can be used.
     *
     * @param player wrenching player
     * @param pos    of block.
     * @return true if wrench can be used
     */
    @Override
    default boolean canWrench(ItemStack wrench, Player player, BlockPos pos) {
        return get().getToolClasses(wrench).contains(ToolClasses.WRENCH);
    }

    // IToolWrench

    /*** Called to ensure that the wrench can be used.
     *
     * @param player - The player doing the wrenching
     * @param hand - Which hand was holding the wrench
     * @param wrench - The item stack that holds the wrench
     * @param rayTrace - The object that is being wrenched
     *
     * @return true if wrenching is allowed, false if not */
    @Override
    default boolean canWrench(Player player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace) {
        return get().getToolClasses(wrench).contains(ToolClasses.WRENCH);
    }

    /*** Callback after the wrench has been used. This can be used to decrease durability or for other purposes.
     *
     * @param player - The player doing the wrenching
     * @param hand - Which hand was holding the wrench
     * @param wrench - The item stack that holds the wrench
     * @param rayTrace - The object that is being wrenched */
    @Override
    default void wrenchUsed(Player player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace) {
        damageItem(player.getHeldItem(hand), player);
        playSound(player);
    }

    // IToolHammer
    @Override
    default boolean isUsable(ItemStack item, LivingEntity user, BlockPos pos) {
        return get().getToolClasses(item).contains(ToolClasses.WRENCH);
    }

    @Override
    default boolean isUsable(ItemStack item, LivingEntity user, Entity entity) {
        return get().getToolClasses(item).contains(ToolClasses.WRENCH);
    }

    @Override
    default void toolUsed(ItemStack item, LivingEntity user, BlockPos pos) {
        damageItem(item, user);
        if (user instanceof Player) {
            playSound((Player) user);
        }
    }

    @Override
    default void toolUsed(ItemStack item, LivingEntity user, Entity entity) {
        damageItem(item, user);
    }

    // ITool
    @Override
    default boolean canUse(@Nonnull EnumHand hand, @Nonnull Player player, @Nonnull BlockPos pos) {
        return get().getToolClasses(player.getHeldItem(hand)).contains(ToolClasses.WRENCH);
    }

    @Override
    default void used(@Nonnull EnumHand hand, @Nonnull Player player, @Nonnull BlockPos pos) {
        damageItem(player.getHeldItem(hand), player);
        playSound(player);
    }

    // IHideFacades
    @Override
    default boolean shouldHideFacades(@Nonnull ItemStack stack, @Nonnull Player player) {
        return get().getToolClasses(stack).contains(ToolClasses.WRENCH);
    }

    // IToolGrafter

    /**
     * Called by leaves to determine the increase in sapling droprate.
     *
     * @param stack ItemStack containing the grafter.
     * @param world Minecraft world the player and the target block inhabit.
     * @param pos   Coordinate of the broken leaf block.
     * @return Float representing the factor the usual drop chance is to be multiplied by.
     */
    @Override
    default float getSaplingModifier(ItemStack stack, World world, Player player, BlockPos pos) {
        return getToolClasses(stack).contains(ToolClasses.GRAFTER) ? 100F : 1.0F;
    }

    // IOverlayRenderAware
    @Override
    default void renderItemOverlayIntoGUI(@Nonnull ItemStack stack, int xPosition, int yPosition) {
        ToolChargeBarRenderer.renderBarsTool(this, stack, xPosition, yPosition);
    }
}
