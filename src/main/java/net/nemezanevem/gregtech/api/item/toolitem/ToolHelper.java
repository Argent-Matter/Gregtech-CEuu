package net.nemezanevem.gregtech.api.item.toolitem;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.capability.GregtechCapabilities;
import net.nemezanevem.gregtech.api.capability.IElectricItem;
import net.nemezanevem.gregtech.api.item.metaitem.MetaItem;
import net.nemezanevem.gregtech.api.recipe.GTRecipe;
import net.nemezanevem.gregtech.api.recipe.GtRecipeTypes;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.TagUnifier;
import net.nemezanevem.gregtech.api.unification.material.properties.GtMaterialProperties;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.ToolProperty;
import net.nemezanevem.gregtech.api.unification.tag.TagPrefix;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.common.ConfigHolder;
import net.nemezanevem.gregtech.common.item.metaitem.MetaItems;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;

/**
 * Collection of tool related helper methods
 */
public final class ToolHelper {

    public static final String TOOL_TAG_KEY = "GT.Tool";
    public static final String BEHAVIOURS_TAG_KEY = "GT.Behaviours";

    // Base item keys

    // Electric item keys
    public static final String MAX_CHARGE_KEY = "MaxCharge";
    public static final String CHARGE_KEY = "Charge";

    // Vanilla keys
    public static final String UNBREAKABLE_KEY = "Unbreakable";
    public static final String HIDE_FLAGS = "HideFlags";

    // Misc keys
    public static final String DISALLOW_CONTAINER_ITEM_KEY = "DisallowContainerItem";

    // Keys that resides in tool tag
    public static final String MATERIAL_KEY = "Material";
    public static final String DURABILITY_KEY = "Durability";
    public static final String MAX_DURABILITY_KEY = "MaxDurability";
    public static final String TOOL_SPEED_KEY = "ToolSpeed";
    public static final String ATTACK_DAMAGE_KEY = "AttackDamage";
    public static final String ATTACK_SPEED_KEY = "AttackSpeed";
    public static final String ENCHANTABILITY_KEY = "Enchantability";
    public static final String HARVEST_LEVEL_KEY = "HarvestLevel";
    public static final String LAST_CRAFTING_USE_KEY = "LastCraftingUse";

    // Keys that resides in behaviours tag

    // AoE
    public static final String MAX_AOE_COLUMN_KEY = "MaxAoEColumn";
    public static final String MAX_AOE_ROW_KEY = "MaxAoERow";
    public static final String MAX_AOE_LAYER_KEY = "MaxAoELayer";
    public static final String AOE_COLUMN_KEY = "AoEColumn";
    public static final String AOE_ROW_KEY = "AoERow";
    public static final String AOE_LAYER_KEY = "AoELayer";

    // Others
    public static final String HARVEST_ICE_KEY = "HarvestIce";
    public static final String TORCH_PLACING_KEY = "TorchPlacing";
    public static final String TORCH_PLACING_CACHE_SLOT_KEY = "TorchPlacing$Slot";
    public static final String TREE_FELLING_KEY = "TreeFelling";
    public static final String DISABLE_SHIELDS_KEY = "DisableShields";
    public static final String RELOCATE_MINED_BLOCKS_KEY = "RelocateMinedBlocks";

    // Crafting Symbols
    private static final BiMap<Character, IGTTool> symbols = HashBiMap.create();

    // Effective Vanilla Blocks
    public static final Set<Block> PICKAXE_HARVESTABLE_BLOCKS = ImmutableSet.of(Blocks.ACTIVATOR_RAIL, Blocks.COAL_ORE, Blocks.COBBLESTONE, Blocks.DETECTOR_RAIL, Blocks.DIAMOND_BLOCK, Blocks.DIAMOND_ORE, Blocks.DOUBLE_STONE_SLAB, Blocks.GOLDEN_RAIL, Blocks.GOLD_BLOCK, Blocks.GOLD_ORE, Blocks.ICE, Blocks.IRON_BLOCK, Blocks.IRON_ORE, Blocks.LAPIS_BLOCK, Blocks.LAPIS_ORE, Blocks.LIT_REDSTONE_ORE, Blocks.MOSSY_COBBLESTONE, Blocks.NETHERRACK, Blocks.PACKED_ICE, Blocks.RAIL, Blocks.REDSTONE_ORE, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.STONE, Blocks.STONE_SLAB, Blocks.STONE_BUTTON, Blocks.STONE_PRESSURE_PLATE);
    public static final Set<Block> STONE_PICKAXE_HARVESTABLE_BLOCKS = ImmutableSet.of(Blocks.IRON_BLOCK, Blocks.IRON_ORE, Blocks.LAPIS_BLOCK, Blocks.LAPIS_ORE);
    public static final Set<Block> IRON_PICKAXE_HARVESTABLE_BLOCKS = ImmutableSet.of(Blocks.DIAMOND_BLOCK, Blocks.DIAMOND_ORE, Blocks.EMERALD_ORE, Blocks.EMERALD_BLOCK, Blocks.GOLD_BLOCK, Blocks.GOLD_ORE);
    public static final Set<Block> SHOVEL_HARVESTABLE_BLOCKS = ImmutableSet.of(Blocks.CLAY, Blocks.DIRT, Blocks.FARMLAND, Blocks.GRASS, Blocks.GRAVEL, Blocks.MYCELIUM, Blocks.SAND, Blocks.SNOW, Blocks.SNOW_LAYER, Blocks.SOUL_SAND, Blocks.GRASS_PATH, Blocks.CONCRETE_POWDER);
    public static final Set<Block> AXE_HARVESTABLE_BLOCKS = ImmutableSet.of(Blocks.BOOKSHELF, Blocks.CHEST, Blocks.PUMPKIN, Blocks.JACK_O_LANTERN, Blocks.MELON, Blocks.LADDER, getAllBlocksFromTag(BlockTags.PLANKS), getAllBlocksFromTag(BlockTags.LOGS), getAllBlocksFromTag(BlockTags.WOODEN_BUTTONS), getAllBlocksFromTag(BlockTags.WOODEN_PRESSURE_PLATES));

    // Suppliers for broken tool stacks
    public static final Supplier<ItemStack> SUPPLY_POWER_UNIT_LV = () -> new ItemStack(MetaItems.POWER_UNIT_LV.get());
    public static final Supplier<ItemStack> SUPPLY_POWER_UNIT_MV = () -> new ItemStack(MetaItems.POWER_UNIT_MV.get());;
    public static final Supplier<ItemStack> SUPPLY_POWER_UNIT_HV = () -> new ItemStack(MetaItems.POWER_UNIT_HV.get());;
    public static final Supplier<ItemStack> SUPPLY_POWER_UNIT_EV = () -> new ItemStack(MetaItems.POWER_UNIT_EV.get());;
    public static final Supplier<ItemStack> SUPPLY_POWER_UNIT_IV = () -> new ItemStack(MetaItems.POWER_UNIT_IV.get());;

    // for retrieving the silk touch drop from a block. Cannot be Access-Transformed because it is a Forge method.
    private static final MethodHandle GET_SILK_TOUCH_DROP;

    static {
        try {
            // archaic way to get around access violations for method handles.
            // this was improved in Java 9 with MethodHandles.privateLookupIn(),
            // but that does not exist in Java 8, so we have to unreflect instead.
            Method method = ObfuscationReflectionHelper.findMethod(Block.class, "func_180643_i", ItemStack.class, BlockState.class);
            method.setAccessible(true);
            GET_SILK_TOUCH_DROP = MethodHandles.lookup().unreflect(method);
            method.setAccessible(false);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private ToolHelper() {/**/}

    private static Block[] combineArrays(Block[]... arrays) {
        List<Block> resultAsList = new ArrayList<>();
        for (Block[] array : arrays) {
            resultAsList.addAll(Arrays.asList(array));
        }
        return resultAsList.toArray(Block[]::new);
    }

    private static Block[] getAllBlocksFromTag(TagKey<Block> tagKey) {
        return ForgeRegistries.BLOCKS.tags().getTag(tagKey).stream().toArray(Block[]::new);
    }

    /**
     * @return finds the registered crafting symbol with the tool
     */
    public static Character getSymbolFromTool(IGTTool tool) {
        return symbols.inverse().get(tool);
    }

    /**
     * @return finds the registered tool with the crafting symbol
     */
    public static IGTTool getToolFromSymbol(Character symbol) {
        return symbols.get(symbol);
    }

    /**
     * Registers the tool against a crafting symbol, this is used in {@link gregtech.api.recipes.ModHandler}
     */
    public static void registerToolSymbol(Character symbol, IGTTool tool) {
        symbols.put(symbol, tool);
    }

    public static CompoundTag getToolTag(ItemStack stack) {
        return stack.getOrCreateSubCompound(TOOL_TAG_KEY);
    }

    public static CompoundTag getBehaviorsTag(ItemStack stack) {
        return stack.getOrCreateSubCompound(BEHAVIOURS_TAG_KEY);
    }

    public static ItemStack getAndSetToolData(IGTTool tool, Material material, int maxDurability, int harvestLevel, float toolSpeed, float attackDamage) {
        ItemStack stack = tool.getRaw();
        stack.getOrCreateTag().putInt(HIDE_FLAGS, 2);
        CompoundTag toolTag = getToolTag(stack);
        toolTag.putString(MATERIAL_KEY, material.toString());
        toolTag.putInt(MAX_DURABILITY_KEY, maxDurability);
        toolTag.putInt(HARVEST_LEVEL_KEY, harvestLevel);
        toolTag.putFloat(TOOL_SPEED_KEY, toolSpeed);
        toolTag.putFloat(ATTACK_DAMAGE_KEY, attackDamage);
        ToolProperty toolProperty = material.getProperty(GtMaterialProperties.TOOL.get());
        if (toolProperty != null) {
            toolProperty.getEnchantments().forEach((enchantment, level) -> {
                if (stack.getItem().canApplyAtEnchantingTable(stack, enchantment)) {
                    stack.enchant(enchantment, level);
                }
            });
        }
        return stack;
    }

    /**
     * Damages tools in a context where the tool had been used to craft something.
     * This supports both vanilla-esque and GT tools in case it does get called on a vanilla-esque tool
     *
     * @param stack  stack to be damaged
     * @param entity entity that has damaged this stack
     */
    public static void damageItemWhenCrafting(@Nonnull ItemStack stack, @Nullable LivingEntity entity) {
        int damage = 2;
        if (stack.getItem() instanceof IGTTool) {
            damage = ((IGTTool) stack.getItem()).getToolStats().getToolDamagePerCraft(stack);
        } else {
            if (TagUnifier.getTagNames(stack.getItem()).stream().anyMatch(s -> s.startsWith("craftingTool"))) {
                damage = 1;
            }
        }
        damageItem(stack, entity, damage);
    }

    /**
     * Damages tools appropriately.
     * This supports both vanilla-esque and GT tools in case it does get called on a vanilla-esque tool.
     * <p>
     * This method only takes 1 durability off, it ignores the tool's effectiveness because of the lack of context.
     *
     * @param stack  stack to be damaged
     * @param entity entity that has damaged this stack
     */
    public static void damageItem(@Nonnull ItemStack stack, @Nullable LivingEntity entity) {
        damageItem(stack, entity, 1);
    }

    /**
     * Damages tools appropriately.
     * This supports both vanilla-esque and GT tools in case it does get called on a vanilla-esque tool
     *
     * @param stack  stack to be damaged
     * @param entity entity that has damaged this stack
     * @param damage how much damage the stack will take
     */
    public static void damageItem(@Nonnull ItemStack stack, @Nullable LivingEntity entity, int damage) {
        if (!(stack.getItem() instanceof IGTTool)) {
            if (entity != null) stack.setDamageValue(stack.getDamageValue() + damage);
        } else {
            if (stack.getTag() != null && stack.getTag().getBoolean(UNBREAKABLE_KEY)) {
                return;
            }
            IGTTool tool = (IGTTool) stack.getItem();
            if (!(entity instanceof Player) || !((Player) entity).capabilities.isCreativeMode) {
                Random random = entity == null ? GTValues.RNG : entity.getRNG();
                if (tool.isElectric()) {
                    int electricDamage = damage * ConfigHolder.machines.energyUsageMultiplier;
                    IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                    if (electricItem != null) {
                        electricItem.discharge(electricDamage, tool.getElectricTier(), true, false, false);
                        if (electricItem.getCharge() > 0 && random.nextInt(100) > ConfigHolder.tools.rngDamageElectricTools) {
                            return;
                        }
                    } else {
                        throw new IllegalStateException("Electric tool does not have an attached electric item capability.");
                    }
                }
                int unbreakingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack);
                int negated = 0;
                for (int k = 0; unbreakingLevel > 0 && k < damage; k++) {
                    if (EnchantmentDurability.negateDamage(stack, unbreakingLevel, random)) {
                        negated++;
                    }
                }
                damage -= negated;
                if (damage <= 0) {
                    return;
                }
                int newDurability = stack.getItemDamage() + damage;
                if (entity instanceof ServerPlayer) {
                    CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger((ServerPlayer) entity, stack, newDurability);
                }
                stack.setItemDamage(newDurability);
                if (newDurability > stack.getMaxDamage()) {
                    if (entity instanceof Player) {
                        StatBase stat = StatList.getObjectBreakStats(stack.getItem());
                        if (stat != null) {
                            ((Player) entity).addStat(stat);
                        }
                    }
                    if (entity != null) {
                        entity.renderBrokenItemStack(stack);
                    }
                    stack.shrink(1);
                }
            }
        }
    }

    /**
     * Can be called to do a default set of "successful use" actions.
     * Damages the item, plays the tool sound (if available), and swings the player's arm.
     *
     * @param player the player clicking the item
     * @param world  the world in which the click happened
     * @param hand   the hand holding the item
     */
    public static void onActionDone(@Nonnull Player player, @Nonnull Level world, @Nonnull EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        IGTTool tool = (IGTTool) stack.getItem();
        ToolHelper.damageItem(stack, player);
        if (tool.getSound() != null) {
            world.playSound(null, player.posX, player.posY, player.posZ, tool.getSound(), SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
        player.swingArm(hand);
    }

    /**
     * @return if any of the specified tool classes exists in the tool
     */
    public static boolean isTool(ItemStack tool, ToolClass... toolClasses) {
        var toolItem = tool.getItem();
        if(toolItem instanceof MetaItem item) {
            if (toolClasses.length == 1) {
                return item.getToolClasses(tool).contains(toolClasses[0]);
            }
            for (ToolClass toolClass : item.getToolClasses(tool)) {
                for (ToolClass specified : toolClasses) {
                    if (toolClass.equals(specified)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Return if all the specified tool classes exists in the tool
     */
    public static boolean areTools(ItemStack tool, String... toolClasses) {
        if (toolClasses.length == 1) {
            return tool.getItem().getToolClasses(tool).contains(toolClasses[0]);
        }
        return tool.getItem().getToolClasses(tool).containsAll(new ObjectArraySet<String>(toolClasses));
    }

    /**
     * Retrieves the Fortune or Looting level for the passed in GregTech Tool
     *
     * @param tool The GregTech tool to retrieve the enchantment level from
     * @return The level of Fortune or Looting that the tool is enchanted with, or zero
     */
    public static int getFortuneOrLootingLevel(ItemStack tool) {
        if (tool.getItem() instanceof ItemGTSword) {
            return EnchantmentHelper.getEnchantmentLevel(Enchantments.LOOTING, tool);
        } else if (tool.getItem() instanceof IGTTool) {
            return EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, tool);
        }

        return 0;
    }

    public static AoESymmetrical getMaxAoEDefinition(ItemStack stack) {
        return AoESymmetrical.readMax(getBehaviorsTag(stack));
    }

    public static AoESymmetrical getAoEDefinition(ItemStack stack) {
        return AoESymmetrical.read(getBehaviorsTag(stack), getMaxAoEDefinition(stack));
    }

    /**
     * AoE Block Breaking Routine.
     */
    public static boolean areaOfEffectBlockBreakRoutine(ItemStack stack, ServerPlayer player) {
        int currentDurability = getToolTag(stack).getInteger(DURABILITY_KEY);
        int maximumDurability = getToolTag(stack).getInteger(MAX_DURABILITY_KEY);
        int remainingUses = maximumDurability - currentDurability;
        Set<BlockPos> harvestableBlocks = getHarvestableBlocks(stack, player);
        if (!harvestableBlocks.isEmpty()) {
            for (BlockPos pos : harvestableBlocks) {
                if (!breakBlockRoutine(player, stack, pos)) {
                    return true;
                }

                remainingUses--;
                if (stack.getItem() instanceof IGTTool && !((IGTTool) stack.getItem()).isElectric() && remainingUses == 0) {
                    return true;
                }
                // If the tool is an electric tool, catch the tool breaking and cancel the remaining AOE
                else if (!player.getHeldItemMainhand().isItemEqualIgnoreDurability(stack)) {
                    return true;
                }
            }
            return true;
        }
        return false;
    }

    public static Set<BlockPos> iterateAoE(ItemStack stack, AoESymmetrical aoeDefinition, Level world, Player player, RayTraceResult rayTraceResult, QuintFunction<ItemStack, Level, Player, BlockPos, BlockPos, Boolean> function) {
        if (aoeDefinition != AoESymmetrical.none() && rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK && rayTraceResult.sideHit != null) {
            int column = aoeDefinition.column;
            int row = aoeDefinition.row;
            int layer = aoeDefinition.layer;
            EnumFacing playerFacing = player.getHorizontalFacing();
            EnumFacing.Axis playerAxis = playerFacing.getAxis();
            EnumFacing.Axis sideHitAxis = rayTraceResult.sideHit.getAxis();
            EnumFacing.AxisDirection sideHitAxisDir = rayTraceResult.sideHit.getAxisDirection();
            Set<BlockPos> validPositions = new ObjectOpenHashSet<>();
            if (sideHitAxis.isVertical()) {
                boolean isX = playerAxis == EnumFacing.Axis.X;
                boolean isDown = sideHitAxisDir == EnumFacing.AxisDirection.NEGATIVE;
                for (int y = 0; y <= layer; y++) {
                    for (int x = isX ? -row : -column; x <= (isX ? row : column); x++) {
                        for (int z = isX ? -column : -row; z <= (isX ? column : row); z++) {
                            if (!(x == 0 && y == 0 && z == 0)) {
                                BlockPos pos = rayTraceResult.getBlockPos().add(x, isDown ? y : -y, z);
                                if (player.canPlayerEdit(pos.offset(rayTraceResult.sideHit), rayTraceResult.sideHit, stack)) {
                                    if (function.apply(stack, world, player, pos, rayTraceResult.getBlockPos())) {
                                        validPositions.add(pos);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                boolean isX = sideHitAxis == EnumFacing.Axis.X;
                boolean isNegative = sideHitAxisDir == EnumFacing.AxisDirection.NEGATIVE;
                for (int x = 0; x <= layer; x++) {
                    // Special case for any additional column > 1: https://i.imgur.com/Dvcx7Vg.png
                    // Same behaviour as the Flux Bore
                    for (int y = (row == 0 ? 0 : -1); y <= (row == 0 ? 0 : row * 2 - 1); y++) {
                        for (int z = -column; z <= column; z++) {
                            if (!(x == 0 && y == 0 && z == 0)) {
                                BlockPos pos = rayTraceResult.getBlockPos().add(isX ? (isNegative ? x : -x) : (isNegative ? z : -z), y, isX ? (isNegative ? z : -z) : (isNegative ? x : -x));
                                if (function.apply(stack, world, player, pos, rayTraceResult.getBlockPos())) {
                                    validPositions.add(pos);
                                }
                            }
                        }
                    }
                }
            }
            return validPositions;
        }
        return Collections.emptySet();
    }

    public static Set<BlockPos> getHarvestableBlocks(ItemStack stack, AoESymmetrical aoeDefinition, Level world, Player player, RayTraceResult rayTraceResult) {
        return iterateAoE(stack, aoeDefinition, world, player, rayTraceResult, ToolHelper::isBlockAoEHarvestable);
    }

    private static boolean isBlockAoEHarvestable(ItemStack stack, Level world, Player player, BlockPos pos, BlockPos hitBlockPos) {
        if (world.isAirBlock(pos)) return false;

        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockLiquid) return false;

        BlockState hitBlockState = world.getBlockState(hitBlockPos);
        if (state.getBlockHardness(world, pos) < 0 || state.getBlockHardness(world, pos) - hitBlockState.getBlockHardness(world, hitBlockPos) > 8) {
            // If mining a block takes significantly longer than the center block, do not mine it.
            // Originally this was just a check for if it is at all harder of a block, however that
            // would cause some annoyances, like Grass Block not being broken if a Dirt Block was the
            // hit block for AoE. This value is somewhat arbitrary, but should cause things to feel
            // natural to mine, but avoid exploits like mining Obsidian quickly by instead targeting Stone.
            return false;
        }
        return stack.canHarvestBlock(state);
    }

    // encompasses all vanilla special case tool checks for harvesting
    public static boolean isToolEffective(BlockState state, Set<String> toolClasses, int harvestLevel) {
        Block block = state.getBlock();
        if (toolClasses.contains(block.getHarvestTool(state))) {
            return block.getHarvestLevel(state) <= harvestLevel;
        }

        net.minecraft.block.material.Material material = state.getMaterial();
        if (toolClasses.contains(ToolClasses.PICKAXE)) {
            if (Blocks.OBSIDIAN == block && harvestLevel >= 3) return true;
            if (IRON_PICKAXE_HARVESTABLE_BLOCKS.contains(block) && harvestLevel >= 2) return true;
            if (STONE_PICKAXE_HARVESTABLE_BLOCKS.contains(block) && harvestLevel >= 1) return true;
            if (PICKAXE_HARVESTABLE_BLOCKS.contains(block)) return true;
            if (material == net.minecraft.block.material.Material.ROCK ||
                    material == net.minecraft.block.material.Material.IRON ||
                    material == net.minecraft.block.material.Material.ANVIL) return true;
        }
        if (toolClasses.contains(ToolClasses.SHOVEL)) {
            if (SHOVEL_HARVESTABLE_BLOCKS.contains(block)) return true;
            if (block == Blocks.SNOW_LAYER || block == Blocks.SNOW) return true;
        }
        if (toolClasses.contains(ToolClasses.AXE)) {
            if (AXE_HARVESTABLE_BLOCKS.contains(block)) return true;
            if (material == net.minecraft.block.material.Material.WOOD ||
                    material == net.minecraft.block.material.Material.PLANTS ||
                    material == net.minecraft.block.material.Material.VINE) return true;
        }
        if (toolClasses.contains(ToolClasses.SWORD)) {
            if (block instanceof BlockWeb) return true;
            if (material == net.minecraft.block.material.Material.PLANTS ||
                    material == net.minecraft.block.material.Material.VINE ||
                    material == net.minecraft.block.material.Material.CORAL ||
                    material == net.minecraft.block.material.Material.LEAVES ||
                    material == net.minecraft.block.material.Material.GOURD) return true;
        }
        if (toolClasses.contains(ToolClasses.SCYTHE)) {
            if (material == net.minecraft.block.material.Material.LEAVES ||
                    material == net.minecraft.block.material.Material.VINE ||
                    material == net.minecraft.block.material.Material.CACTUS ||
                    material == net.minecraft.block.material.Material.PLANTS) {
                return true;
            }
        }
        if (toolClasses.contains(ToolClasses.FILE)) {
            if (block instanceof BlockPane && material == net.minecraft.block.material.Material.IRON) {
                return true;
            }
        }
        if (toolClasses.contains(ToolClasses.CROWBAR)) {
            return block instanceof BlockRailBase || material == net.minecraft.block.material.Material.CIRCUITS;
        }
        return false;
    }

    /**
     * Special cases for vanilla destroy speed changes.
     * If return -1, no special case was found, and some other method
     * should be used to determine the destroy speed.
     */
    public static float getDestroySpeed(BlockState state, Set<String> toolClasses) {
        if (toolClasses.contains(ToolClasses.SWORD)) {
            Block block = state.getBlock();
            if (block instanceof BlockWeb) {
                return 15.0F;
            } else {
                net.minecraft.block.material.Material material = state.getMaterial();
                if (material == net.minecraft.block.material.Material.PLANTS ||
                        material == net.minecraft.block.material.Material.VINE ||
                        material == net.minecraft.block.material.Material.CORAL ||
                        material == net.minecraft.block.material.Material.LEAVES ||
                        material == net.minecraft.block.material.Material.GOURD) {
                    return 1.5F;
                }
            }
        }
        return -1;
    }

    public static Set<BlockPos> getHarvestableBlocks(ItemStack stack, Level world, Player player, RayTraceResult rayTraceResult) {
        return getHarvestableBlocks(stack, getAoEDefinition(stack), world, player, rayTraceResult);
    }

    public static Set<BlockPos> getHarvestableBlocks(ItemStack stack, Player player) {
        AoESymmetrical aoeDefiniton = getAoEDefinition(stack);
        if (aoeDefiniton == AoESymmetrical.none()) {
            return Collections.emptySet();
        }

        RayTraceResult rayTraceResult = getPlayerDefaultRaytrace(player);
        return getHarvestableBlocks(stack, aoeDefiniton, player.world, player, rayTraceResult);
    }

    public static RayTraceResult getPlayerDefaultRaytrace(@Nonnull Player player) {

        Vec3d lookPos = player.getPositionEyes(1F);
        Vec3d rotation = player.getLook(1);
        Vec3d realLookPos = lookPos.add(rotation.x * 5, rotation.y * 5, rotation.z * 5);
        return player.world.rayTraceBlocks(lookPos, realLookPos);
    }

    /**
     * Tree Felling routine. Improved from GTI, GTCE, TiCon and other tree felling solutions:
     * - Works with weird Oak Trees (thanks to Syrcan for pointing out)
     * - Brought back tick-spread behaviour:
     * - Tree-felling is validated in the same tick as the stem being broken
     * - 1 block broken per tick, akin to chorus fruit
     * - Fix cheating durability loss
     * - Eliminates leaves as well as logs
     */
    public static void treeFellingRoutine(ServerPlayer player, ItemStack stack, BlockPos start) {
        BlockState state = player.world.getBlockState(start);
        if (state.getBlock().isWood(player.world, start)) {
            TreeFellingListener.start(state, stack, start, player);
        }
    }

    /**
     * Applies Forge Hammer recipes to block broken, used for hammers or tools with hard hammer enchant applied.
     */
    public static void applyHammerDropConversion(ItemStack tool, BlockState state, List<ItemStack> drops, int fortune, float dropChance, Random random) {
        if (tool.getItem().getToolClasses(tool).contains(ToolClass.HARD_HAMMER) || EnchantmentHelper.getEnchantmentLevel(EnchantmentHardHammer.INSTANCE, tool) > 0) {
            ItemStack silktouchDrop = getSilkTouchDrop(state);
            if (!silktouchDrop.isEmpty()) {
                // Stack lists can be immutable going into Recipe#matches barring no rewrites
                List<ItemStack> dropAsList = Collections.singletonList(silktouchDrop);
                // Search for forge hammer recipes from all drops individually (only LV or under)
                GTRecipe hammerRecipe = GtRecipeTypes.FORGE_HAMMER_RECIPES.get().findRecipe(GTValues.V[1], dropAsList, Collections.emptyList(), 0, false);
                if (hammerRecipe != null && hammerRecipe.matches(true, dropAsList, Collections.emptyList())) {
                    drops.clear();
                    TagPrefix prefix = TagUnifier.getPrefix(silktouchDrop.getItem());
                    if (prefix == null) {
                        for (ItemStack output : hammerRecipe.getOutputs()) {
                            if (dropChance == 1.0F || random.nextFloat() <= dropChance) {
                                drops.add(output.copy());
                            }
                        }
                    } else if (prefix.name.startsWith("ore")) {
                        for (ItemStack output : hammerRecipe.getOutputs()) {
                            if (dropChance == 1.0F || random.nextFloat() <= dropChance) {
                                // Only apply fortune on ore -> crushed forge hammer recipes
                                if (OreDictUnifier.getPrefix(output) == OrePrefix.crushed) {
                                    output = output.copy();
                                    output.grow(random.nextInt(fortune));
                                    drops.add(output);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean breakBlockRoutine(ServerPlayer player, ItemStack tool, BlockPos pos) {
        // This is *not* a vanilla/forge convention, Forge never added "shears" to ItemShear's tool classes.
        if (isTool(tool, ToolClass.SHEARS) && shearBlockRoutine(player, tool, pos) == 0) {
            return false;
        }
        Level world = player.world;
        int exp = ForgeHooks.onBlockBreakEvent(world, player.interactionManager.getGameType(), player, pos);
        if (exp == -1) {
            return false;
        }
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        TileEntity tile = world.getTileEntity(pos);
        if ((block instanceof CommandBlock || block instanceof StructureBlock) && !player.canUseCommandBlock()) {
            world.notifyBlockUpdate(pos, state, state, 3);
            return false;
        } else {
            world.playEvent(player, 2001, pos, Block.getStateId(state));
            boolean successful;
            if (player.isCreative()) {
                successful = removeBlockRoutine(state, world, player, pos, false);
                player.connection.sendPacket(new SPacketBlockChange(world, pos));
            } else {
                ItemStack copiedTool = tool.isEmpty() ? ItemStack.EMPTY : tool.copy();
                boolean canHarvest = block.canHarvestBlock(world, pos, player);
                if (!tool.isEmpty()) {
                    tool.onBlockDestroyed(world, state, pos, player);
                    if (tool.isEmpty()) {
                        ForgeEventFactory.onPlayerDestroyItem(player, copiedTool, EnumHand.MAIN_HAND);
                    }
                }
                successful = removeBlockRoutine(null, world, player, pos, canHarvest);
                if (successful && canHarvest) {
                    block.harvestBlock(world, player, pos, state, tile, copiedTool);
                }
            }
            if (!player.isCreative() && successful && exp > 0) {
                block.dropXpOnBlockBreak(world, pos, exp);
            }
            return successful;
        }
    }

    /**
     * Shearing a Block.
     *
     * @return -1 if not shearable, otherwise return 0 or 1, 0 if tool is now broken.
     */
    public static int shearBlockRoutine(ServerPlayer player, ItemStack tool, BlockPos pos) {
        if (!player.isCreative()) {
            Level world = player.world;
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof IShearable) {
                IShearable shearable = (IShearable) state.getBlock();
                if (shearable.isShearable(tool, world, pos)) {
                    List<ItemStack> shearedDrops = shearable.onSheared(tool, world, pos, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, tool));
                    boolean relocateMinedBlocks = getBehaviorsTag(tool).getBoolean(RELOCATE_MINED_BLOCKS_KEY);
                    Iterator<ItemStack> iter = shearedDrops.iterator();
                    while (iter.hasNext()) {
                        ItemStack stack = iter.next();
                        if (relocateMinedBlocks && player.addItemStackToInventory(stack)) {
                            iter.remove();
                        } else {
                            float f = 0.7F;
                            double xo = world.rand.nextFloat() * f + 0.15D;
                            double yo = world.rand.nextFloat() * f + 0.15D;
                            double zo = world.rand.nextFloat() * f + 0.15D;
                            EntityItem entityItem = new EntityItem(world, pos.getX() + xo, pos.getY() + yo, pos.getZ() + zo, stack);
                            entityItem.setDefaultPickupDelay();
                            player.world.spawnEntity(entityItem);
                        }
                    }
                    ToolHelper.damageItem(tool, player);
                    player.addStat(StatList.getBlockStats((Block) shearable));
                    player.world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
                    return tool.isEmpty() ? 0 : 1;
                }
            }
        }
        return -1;
    }

    public static boolean removeBlockRoutine(@Nullable BlockState state, Level world, ServerPlayer player, BlockPos pos, boolean canHarvest) {
        state = state == null ? world.getBlockState(pos) : state;
        boolean successful = state.getBlock().onDestroyedByPlayer(state, world, pos, player, canHarvest, world.getFluidState(pos));
        if (successful) {
            state.getBlock().playerWillDestroy(world, pos, state, player);
        }
        return successful;
    }

    /**
     * @param state the BlockState of the block
     * @return the silk touch drop
     */
    @Nonnull
    public static ItemStack getSilkTouchDrop(@Nonnull BlockState state) {
        try {
            LootContext.Builder lootcontext$builder = (new LootContext.Builder(pLevel)).withRandom(pLevel.random).withParameter(LootContextParams.ORIGIN, null).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, null);
            Block.dropResources();
            ResourceLocation blockId = Util.getId(state.getBlock());
            ServerLifecycleHooks.getCurrentServer().getLootTables().get(new ResourceLocation(blockId.getNamespace(), "blocks/" + blockId.getPath())).getRandomItems(new LootContextParams());
            return (ItemStack) GET_SILK_TOUCH_DROP.invokeExact(state.getBlock(), state);
        } catch (Throwable ignored) {
            return ItemStack.EMPTY;
        }
    }

    public static void playToolSound(ItemStack stack, Player player) {
        if (stack.getItem() instanceof IGTTool) {
            ((IGTTool) stack.getItem()).playSound(player);
        }

    }
}
