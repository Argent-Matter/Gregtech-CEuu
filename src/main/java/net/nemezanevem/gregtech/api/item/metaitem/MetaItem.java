package net.nemezanevem.gregtech.api.item.metaitem;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.IForgeRegistry;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.capability.GregtechCapabilities;
import net.nemezanevem.gregtech.api.capability.IElectricItem;
import net.nemezanevem.gregtech.api.capability.IThermalFluidHandlerItemStack;
import net.nemezanevem.gregtech.api.capability.impl.ElectricItem;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.item.gui.ItemUIFactory;
import net.nemezanevem.gregtech.api.item.metaitem.stats.*;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.TagUnifier;
import net.nemezanevem.gregtech.api.unification.stack.ItemMaterialInfo;
import net.nemezanevem.gregtech.api.unification.stack.MaterialStack;
import net.nemezanevem.gregtech.api.unification.tag.TagPrefix;
import net.nemezanevem.gregtech.api.util.GTValues;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.common.item.GtItemRegistry;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.List;

/**
 * MetaItem is item that can have up to Short.MAX_VALUE items inside one id.
 * These items even can be edible, have custom behaviours, be electric or act like fluid containers!
 * They can also have different burn time, plus be handheld, oredicted or invisible!
 * They also can be reactor components.
 * <p>
 * You can also extend this class and occupy some of it's MetaData, and just pass an meta offset in constructor, and everything will work properly.
 * <p>
 * Items are added in MetaItem via {@link #addItem(int, String)}. You will get {@link ExtendedProperties} instance, which you can configure in builder-alike pattern:
 * {@code addItem(0, "test_item").addStats(new ElectricStats(10000, 1,  false)) }
 * This will add single-use (not rechargeable) LV battery with initial capacity 10000 EU
 */
public abstract class MetaItem extends Item implements ItemUIFactory {

    private static final List<MetaItem> META_ITEMS = new ArrayList<>();

    public static List<MetaItem> getMetaItems() {
        return Collections.unmodifiableList(META_ITEMS);
    }

    protected final Map<ResourceLocation, MetaItem> metaItems = new HashMap<>();
    protected final Map<ResourceLocation, ModelResourceLocation> metaItemsModels = new HashMap<>();
    protected final Map<ResourceLocation, ModelResourceLocation[]> specialItemsModels = new HashMap<>();
    protected static final ModelResourceLocation MISSING_LOCATION = new ModelResourceLocation("builtin/missing", "inventory");

    public MetaItem(ExtendedProperties properties) {
        super(properties);
        META_ITEMS.add(this);
    }

    @SubscribeEvent
    public static void registerItemColors(final RegisterColorHandlersEvent.Item event) {
        for(var item : getMetaItems()) {
            event.register(item::getColorForItemStack, item);
        }
    }

    public void registerModels() {
        for (var itemMetaKey : metaItems.keySet()) {
            MetaItem metaValueItem = metaItems.get(itemMetaKey);
            int numberOfModels = metaValueItem.getModelAmount();
            if (numberOfModels > 1) {
                ModelResourceLocation[] resourceLocations = new ModelResourceLocation[numberOfModels];
                for (int i = 0; i < resourceLocations.length; i++) {
                    ResourceLocation resourceLocation = createItemModelPath(metaValueItem, "/" + (i + 1));
                    resourceLocations[i] = new ModelResourceLocation(resourceLocation, "inventory");
                    Minecraft.getInstance().getItemRenderer().getItemModelShaper().register(this, resourceLocations[i]);
                }
                continue;
            }
            ResourceLocation resourceLocation = createItemModelPath(metaValueItem, "");
            if (numberOfModels > 0) {
                Minecraft.getInstance().getItemRenderer().getItemModelShaper().register(this, new ModelResourceLocation(resourceLocation, "inventory"));
            }
            metaItemsModels.put(itemMetaKey, new ModelResourceLocation(resourceLocation, "inventory"));
        }
    }

    public ResourceLocation createItemModelPath(MetaItem metaValueItem, String postfix) {
        return new ResourceLocation(GregTech.MODID, formatModelPath(metaValueItem) + postfix);
    }

    protected String formatModelPath(MetaItem metaValueItem) {
        return "metaitems/" + metaValueItem.unlocalizedName;
    }

    protected int getModelIndex(ItemStack itemStack) {
        MetaItem metaItem = getItem(Util.getId(itemStack.getItem()).toString());

        // Electric Items
        IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem != null) {
            return (int) Math.min(((electricItem.getCharge() / (electricItem.getMaxCharge() * 1.0)) * 7), 7);
        }

        // Integrated (Config) Circuit
        if (metaItem != null) {
            return IntCircuitIngredient.getCircuitConfiguration(itemStack);
        }
        return 0;
    }

    protected int getColorForItemStack(ItemStack stack, int tintIndex) {
        MetaItem metaValueItem = getItem(stack);
        if (metaValueItem != null && metaValueItem.getColorProvider() != null)
            return metaValueItem.getColorProvider().getItemStackColor(stack, tintIndex);
        return 0xFFFFFF;
    }

    @Override
    public boolean showDurabilityBar(@Nonnull ItemStack stack) {
        // meta items now handle durability bars via custom rendering
        return false;
    }

    @Override
    public double getDurabilityForDisplay(@Nonnull ItemStack stack) {
        MetaItem metaValueItem = getItem(stack);
        if (metaValueItem != null && metaValueItem.getDurabilityManager() != null) {
            return metaValueItem.getDurabilityManager().getDurabilityForDisplay(stack);
        }
        return -1.0;
    }

    protected abstract ExtendedProperties constructMetaValueItem(ResourceLocation id, IForgeRegistry<Item> registry);

    public final ExtendedProperties addItem(ResourceLocation id, IForgeRegistry<Item> registry) {
        ExtendedProperties metaValueItem = constructMetaValueItem(id, registry);
        if (metaItems.containsKey(id)) {
            MetaItem registeredItem = metaItems.get(id);
            throw new IllegalArgumentException(String.format("MetaId %d is already occupied by item %s (requested by item %s)", id, registeredItem.unlocalizedName, unlocalizedName));
        }
        metaItems.put(id, metaValueItem);
        names.put(unlocalizedName, metaValueItem);
        return metaValueItem;
    }

    public final Collection<MetaItem> getAllItems() {
        return Collections.unmodifiableCollection(metaItems.values());
    }

    public final MetaItem getItem(String valueName) {
        return names.get(valueName);
    }

    protected short formatRawItemDamage(short metaValue) {
        return metaValue;
    }

    public void registerSubItems() {
    }

    @Override
    public ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable CompoundTag nbt) {
        Item item = stack.getItem();
        if (item == null || !(item instanceof MetaItem metaItem)) {
            return null;
        }
        ArrayList<ICapabilityProvider> providers = new ArrayList<>();
        for (IItemComponent itemComponent : metaItem.getAllStats()) {
            if (itemComponent instanceof IItemCapabilityProvider) {
                IItemCapabilityProvider provider = (IItemCapabilityProvider) itemComponent;
                providers.add(provider.createProvider(stack));
            }
        }
        return new CombinedCapabilityProvider(providers);
    }

    //////////////////////////////////////////////////////////////////

    @Override
    public int getItemBurnTime(@Nonnull ItemStack itemStack) {
        MetaItem metaValueItem = getItem(itemStack);
        if (metaValueItem == null) {
            return super.getItemBurnTime(itemStack);
        }
        return metaValueItem.getBurnValue();
    }

    //////////////////////////////////////////////////////////////////
    //      Behaviours and Use Manager Implementation               //
    //////////////////////////////////////////////////////////////////

    private IItemUseManager getUseManager(ItemStack itemStack) {
        MetaItem metaValueItem = getItem(itemStack);
        if (metaValueItem == null) {
            return null;
        }
        return metaValueItem.getUseManager();
    }

    public List<IItemBehaviour> getBehaviours(ItemStack itemStack) {
        MetaItem metaValueItem = getItem(itemStack);
        if (metaValueItem == null) {
            return ImmutableList.<IItemBehaviour>of();
        }
        return metaValueItem.getBehaviours();
    }

    @Override
    public int getItemStackLimit(@Nonnull ItemStack stack) {
        MetaItem metaValueItem = getItem(stack);
        if (metaValueItem == null) {
            return 64;
        }
        return metaValueItem.getMaxStackSize(stack);
    }

    @Nonnull
    @Override
    public UseAnim getUseAnimation(@Nonnull ItemStack stack) {
        IItemUseManager useManager = getUseManager(stack);
        if (useManager != null) {
            return useManager.getUseAction(stack);
        }
        return UseAnim.NONE;
    }

    @Override
    public int getMaxItemUseDuration(@Nonnull ItemStack stack) {
        IItemUseManager useManager = getUseManager(stack);
        if (useManager != null) {
            return useManager.getMaxItemUseDuration(stack);
        }
        return 0;
    }

    @Override
    public void onUsingTick(@Nonnull ItemStack stack, @Nonnull LivingEntity player, int count) {
        if (player instanceof Player) {
            IItemUseManager useManager = getUseManager(stack);
            if (useManager != null) {
                useManager.onItemUsingTick(stack, (Player) player, count);
            }
        }
    }

    @Override
    public void onPlayerStoppedUsing(@Nonnull ItemStack stack, @Nonnull Level world, @Nonnull LivingEntity player, int timeLeft) {
        if (player instanceof Player) {
            IItemUseManager useManager = getUseManager(stack);
            if (useManager != null) {
                useManager.onPlayerStoppedItemUsing(stack, (Player) player, timeLeft);
            }
        }
    }

    @Nullable
    @Override
    public ItemStack onItemUseFinish(@Nonnull ItemStack stack, @Nonnull Level world, @Nonnull LivingEntity player) {
        if (player instanceof Player) {
            IItemUseManager useManager = getUseManager(stack);
            if (useManager != null) {
                return useManager.onItemUseFinish(stack, (Player) player);
            }
        }
        return stack;
    }

    @Override
    public boolean onLeftClickEntity(@Nonnull ItemStack stack, @Nonnull Player player, @Nonnull Entity entity) {
        boolean returnValue = false;
        for (IItemBehaviour behaviour : getBehaviours(stack)) {
            if (behaviour.onLeftClickEntity(stack, player, entity)) {
                returnValue = true;
            }
        }
        return returnValue;
    }

    @Override
    public boolean itemInteractionForEntity(@Nonnull ItemStack stack, @Nonnull Player playerIn, @Nonnull LivingEntity target, @Nonnull InteractionHand hand) {
        boolean returnValue = false;
        for (IItemBehaviour behaviour : getBehaviours(stack)) {
            if (behaviour.itemInteractionForEntity(stack, playerIn, target, hand)) {
                returnValue = true;
            }
        }
        return returnValue;
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(@Nonnull Level world, Player player, @Nonnull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        for (IItemBehaviour behaviour : getBehaviours(itemStack)) {
            InteractionResultHolder<ItemStack> behaviourResult = behaviour.onItemRightClick(world, player, hand);
            itemStack = behaviourResult.getObject();
            if (behaviourResult.getResult() != InteractionResult.PASS) {
                return behaviourResult;
            } else if (itemStack.isEmpty()) {
                return InteractionResultHolder.pass(ItemStack.EMPTY);
            }
        }
        IItemUseManager useManager = getUseManager(itemStack);
        if (useManager != null && useManager.canStartUsing(itemStack, player)) {
            useManager.onItemUseStart(itemStack, player);
            player.startUsingItem(hand);
            return InteractionResultHolder.success(itemStack);
        }
        return InteractionResultHolder.pass(itemStack);
    }

    @Nonnull
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        ItemStack itemStack = context.getPlayer().getItemInHand(context.getHand());
        for (IItemBehaviour behaviour : getBehaviours(itemStack)) {
            var hitLoc = context.getClickLocation();
            InteractionResult behaviourResult = behaviour.onItemUseFirst(context.getPlayer(), context.getLevel(), context.getClickedPos(), context.getClickedFace(), hitLoc.x, hitLoc.y, hitLoc.z, context.getHand());
            if (behaviourResult != InteractionResult.PASS) {
                return behaviourResult;
            } else if (itemStack.isEmpty()) {
                return InteractionResult.PASS;
            }
        }
        return InteractionResult.PASS;
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getPlayer().getItemInHand(context.getHand());
        ItemStack originalStack = stack.copy();
        for (IItemBehaviour behaviour : getBehaviours(stack)) {
            var hitLoc = context.getClickLocation();
            InteractionResultHolder<ItemStack> behaviourResult = behaviour.onItemUse(context.getPlayer(), context.getLevel(), context.getClickedPos(), context.getHand(), context.getClickedFace(), hitLoc.x, hitLoc.y, hitLoc.z);
            stack = behaviourResult.getObject();
            if (behaviourResult.getResult() != InteractionResult.PASS) {
                if (!ItemStack.matches(originalStack, stack))
                    context.getPlayer().setItemInHand(context.getHand(), stack);
                return behaviourResult.getResult();
            } else if (stack.isEmpty()) {
                context.getPlayer().setItemInHand(context.getHand(), ItemStack.EMPTY);
                return InteractionResult.PASS;
            }
        }
        return InteractionResult.PASS;
    }

    @Nonnull
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@Nonnull EquipmentSlot slot, @Nonnull ItemStack stack) {
        HashMultimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        Item metaValueItem = stack.getItem();
        if (metaValueItem instanceof MetaItem) {
            for (IItemBehaviour behaviour : getBehaviours(stack)) {
                modifiers.putAll(behaviour.getAttributeModifiers(slot, stack));
            }
        }
        return modifiers;
    }

    @Override
    public boolean isEnchantable(@Nonnull ItemStack stack) {
        Item metaValueItem = stack.getItem();
        if (metaValueItem instanceof MetaItem item) {
            IEnchantabilityHelper helper = metaValueItem.getEnchantabilityHelper();
            return helper != null && helper.isEnchantable(stack);
        }
        return super.isEnchantable(stack);
    }

    @Override
    public int getEnchantmentValue() {
        IEnchantabilityHelper helper = this.getEnchantabilityHelper();
        return helper == null ? 0 : helper.getItemEnchantability();
        return super.getEnchantmentValue();
    }

    @Override
    public boolean canApplyAtEnchantingTable(@Nonnull ItemStack stack, @Nonnull Enchantment enchantment) {
        Item metaValueItem = stack.getItem();
        if (metaValueItem instanceof MetaItem item) {
            IEnchantabilityHelper helper = item.getEnchantabilityHelper();
            return helper != null && helper.canApplyAtEnchantingTable(stack, enchantment);
        }
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public void onUpdate(@Nonnull ItemStack stack, @Nonnull Level worldIn, @Nonnull Entity entityIn, int itemSlot, boolean isSelected) {
        for (IItemBehaviour behaviour : getBehaviours(stack)) {
            behaviour.onUpdate(stack, entityIn);
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(@Nonnull ItemStack oldStack, @Nonnull ItemStack newStack, boolean slotChanged) {
        //if item is equal, and old item has electric item capability, remove charge tags to stop reequip animation when charge is altered
        if (ItemStack.matches(oldStack, newStack) && oldStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null).isPresent() &&
                oldStack.hasTag() && newStack.hasTag()) {
            oldStack = oldStack.copy();
            newStack = newStack.copy();
            oldStack.getTag().remove("Charge");
            newStack.getTag().remove("Charge");
            if (oldStack.getTag().contains("terminal")) {
                oldStack.getTag().getCompound("terminal").getCompound("_hw").remove("battery");
                newStack.getTag().getCompound("terminal").getCompound("_hw").remove("battery");
            }
        }
        return !ItemStack.matches(oldStack, newStack);
    }

    @Nonnull
    @Override
    public Component getName(ItemStack stack) {
        if (stack.getItemDamage() >= metaItemOffset) {
            MetaItem item = getItem(stack);
            if (item == null) {
                return Component.literal("invalid item");
            }
            String unlocalizedName = String.format("metaitem.%s.name", item.getDescriptionId());
            if (item.getNameProvider() != null) {
                return item.getNameProvider().getItemStackDisplayName(stack, unlocalizedName);
            }
            LazyOptional<IFluidHandlerItem> fluidHandlerItem = ItemHandlerHelper.copyStackWithSize(stack, 1)
                    .getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null);
            if (fluidHandlerItem.isPresent()) {
                var realFluidHandlerItem = fluidHandlerItem.resolve().get();
                FluidStack fluidInside = realFluidHandlerItem.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
                return Component.translatable(unlocalizedName, fluidInside == null ?
                        Component.translatable("metaitem.fluid_cell.empty") :
                        fluidInside.getDisplayName());
            }
            return LocalizationUtils.format(unlocalizedName);
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack itemStack, @Nullable Level worldIn, @Nonnull List<Component> lines, @Nonnull TooltipFlag pIsAdvanced) {
        MetaItem item = getItem(itemStack);
        if (item == null) return;
        var unlocalizedTooltip = Component.translatable("metaitem." + item.unlocalizedName + ".tooltip");
        if (ComponentUtils.isTranslationResolvable(unlocalizedTooltip)) {
            lines.add(unlocalizedTooltip);
        }

        LazyOptional<IElectricItem> electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem.isPresent()) {
            var realElectricItem = electricItem.resolve().get();
            if (realElectricItem.canProvideChargeExternally()) {
                addDischargeItemTooltip(lines, realElectricItem.getMaxCharge(), realElectricItem.getCharge(), realElectricItem.getTier());
            } else {
                lines.add(Component.translatable("metaitem.generic.electric_item.tooltip",
                        realElectricItem.getCharge(),
                        realElectricItem.getMaxCharge(),
                        GTValues.VNF[realElectricItem.getTier()]));
            }
        }

        LazyOptional<IFluidHandlerItem> fluidHandler = ItemHandlerHelper.copyStackWithSize(itemStack, 1)
                .getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null);
        if (fluidHandler.isPresent()) {
            var realFluidHandler = fluidHandler.resolve().get();
            IFluidTankProperties fluidTankProperties = realFluidHandler.getTankProperties()[0];
            FluidStack fluid = fluidTankProperties.getContents();

            lines.add(Component.translatable("metaitem.generic.fluid_container.tooltip",
                    fluid == null ? 0 : fluid.amount,
                    fluidTankProperties.getCapacity(),
                    fluid == null ? "" : fluid.getLocalizedName()));

            if (fluidHandler instanceof IThermalFluidHandlerItemStack) {
                IThermalFluidHandlerItemStack thermalFluidHandler = (IThermalFluidHandlerItemStack) fluidHandler;
                if (TooltipHelper.isShiftDown()) {
                    lines.add(Component.translatable("gregtech.fluid_pipe.max_temperature", thermalFluidHandler.getMaxFluidTemperature()));
                    if (thermalFluidHandler.isGasProof()) lines.add(Component.translatable("gregtech.fluid_pipe.gas_proof"));
                    if (thermalFluidHandler.isAcidProof()) lines.add(Component.translatable("gregtech.fluid_pipe.acid_proof"));
                    if (thermalFluidHandler.isCryoProof()) lines.add(Component.translatable("gregtech.fluid_pipe.cryo_proof"));
                    if (thermalFluidHandler.isPlasmaProof()) lines.add(Component.translatable("gregtech.fluid_pipe.plasma_proof"));
                } else if (thermalFluidHandler.isGasProof() || thermalFluidHandler.isAcidProof() || thermalFluidHandler.isCryoProof() || thermalFluidHandler.isPlasmaProof()) {
                    lines.add(Component.translatable("gregtech.tooltip.fluid_pipe_hold_shift"));
                }
            }
        }

        for (IItemBehaviour behaviour : getBehaviours(itemStack)) {
            behaviour.addInformation(itemStack, lines);
        }

        if (ConfigHolder.misc.debug) {
            lines.add("MetaItem Id: " + item.unlocalizedName);
        }
    }

    private static void addDischargeItemTooltip(List<Component> tooltip, long maxCharge, long currentCharge, int tier) {
        if (currentCharge == 0) { // do not display when empty
            tooltip.add(Component.translatable("metaitem.generic.electric_item.tooltip", currentCharge, maxCharge, GTValues.VNF[tier]));
            return;
        }
        Instant start = Instant.now();
        Instant end = Instant.now().plusSeconds((long) ((currentCharge * 1.0) / GTValues.V[tier] / 20));
        Duration duration = Duration.between(start, end);
        double percentRemaining = currentCharge * 1.0 / maxCharge * 100; // used for color

        long timeRemaining;
        Component unit;
        if (duration.getSeconds() <= 180) {
            timeRemaining = duration.getSeconds();
            unit = Component.translatable("metaitem.battery.charge_unit.second");
        } else if (duration.toMinutes() <= 180) {
            timeRemaining = duration.toMinutes();
            unit = Component.translatable("metaitem.battery.charge_unit.minute");
        } else {
            timeRemaining = duration.toHours();
            unit = Component.translatable("metaitem.battery.charge_unit.hour");
        }
        tooltip.add(Component.translatable("metaitem.battery.charge_detailed", currentCharge, maxCharge, GTValues.VNF[tier],
                percentRemaining < 30 ? 'c' : percentRemaining < 60 ? 'e' : 'a',
                timeRemaining, unit));
    }

    @Override
    public boolean hasContainerItem(@Nonnull ItemStack itemStack) {
        MetaItem item = getItem(itemStack);
        if (item == null) {
            return false;
        }
        return item.getContainerItemProvider() != null;
    }

    @Nonnull
    @Override
    public ItemStack getContainerItem(@Nonnull ItemStack itemStack) {
        MetaItem item = getItem(itemStack);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        itemStack = itemStack.copy();
        itemStack.setCount(1);
        IItemContainerItemProvider provider = item.getContainerItemProvider();
        return provider == null ? ItemStack.EMPTY : provider.getContainerItem(itemStack);
    }

    @Nonnull
    @Override
    public Collection<CreativeModeTab> getCreativeTabs() {
        return new CreativeModeTab[]{GregTech.TAB_GREGTECH, GregTech.TAB_GREGTECH_MATERIALS};
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, Player Player) {
        ItemStack itemStack = holder.getCurrentItem();
        MetaItem metaValueItem = getItem(itemStack);
        ItemUIFactory uiFactory = metaValueItem == null ? null : metaValueItem.getUIManager();
        return uiFactory == null ? null : uiFactory.createUI(holder, Player);
    }

    // IOverlayRenderAware
    @Override
    public void renderItemOverlayIntoGUI(@Nonnull ItemStack stack, int xPosition, int yPosition) {
        ToolChargeBarRenderer.renderBarsItem(this, stack, xPosition, yPosition);
    }

    public ItemStack getStackForm(int amount) {
        return new ItemStack(MetaItem.this, amount);
    }

    public ItemStack getStackForm() {
        return getStackForm(1);
    }

    /**
     * Attempts to get an fully charged variant of this electric item
     *
     * @param chargeAmount amount of charge
     * @return charged electric item stack
     * @throws java.lang.IllegalStateException if this item is not electric item
     */
    public ItemStack getChargedStack(long chargeAmount) {
        ItemStack itemStack = getStackForm(1);
        LazyOptional<IElectricItem> electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (!electricItem.isPresent()) {
            throw new IllegalStateException("Not a supported electric item.");
        }
        electricItem.resolve().get().charge(chargeAmount, Integer.MAX_VALUE, true, false);
        return itemStack;
    }

    public ItemStack getInfiniteChargedStack() {
        ItemStack itemStack = getStackForm(1);
        LazyOptional<IElectricItem> electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (!electricItem.isPresent()) {
            throw new IllegalStateException("Not a supported electric item.");
        }
        if(!(electricItem.resolve().get() instanceof ElectricItem item)) {
            throw new IllegalStateException("Not a supported electric item.");
        }
        item.setInfiniteCharge(true);
        return itemStack;
    }

    /**
     * Attempts to get an electric item variant with override of max charge
     *
     * @param maxCharge new max charge of this electric item
     * @return item stack with given max charge
     * @throws java.lang.IllegalStateException if this item is not electric item or uses custom implementation
     */
    public ItemStack getMaxChargeOverrideStack(long maxCharge) {
        ItemStack itemStack = getStackForm(1);
        LazyOptional<IElectricItem> electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (!electricItem.isPresent()) {
            throw new IllegalStateException("Not a supported electric item.");
        }
        if(!(electricItem.resolve().get() instanceof ElectricItem item)) {
            throw new IllegalStateException("Not a supported electric item.");
        }
        item.setMaxChargeOverride(maxCharge);
        return itemStack;
    }

    public ItemStack getChargedStackWithOverride(IElectricItem source) {
        ItemStack itemStack = getStackForm(1);
        LazyOptional<IElectricItem> electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (!electricItem.isPresent()) {
            throw new IllegalStateException("Not a supported electric item.");
        }
        if(!(electricItem.resolve().get() instanceof ElectricItem item)) {
            throw new IllegalStateException("Not a supported electric item.");
        }
        item.setMaxChargeOverride(source.getMaxCharge());
        long charge = source.discharge(Long.MAX_VALUE, Integer.MAX_VALUE, true, false, true);
        item.charge(charge, Integer.MAX_VALUE, true, false);
        return itemStack;
    }

    public class ExtendedProperties extends Item.Properties {
        public final String unlocalizedName;
        private IItemNameProvider nameProvider;
        private IItemMaxStackSizeProvider stackSizeProvider;
        private IItemContainerItemProvider containerItemProvider;

        private final List<IItemComponent> allStats = new ArrayList<>();
        private final List<IItemBehaviour> behaviours = new ArrayList<>();
        private IItemUseManager useManager;
        private ItemUIFactory uiManager;
        private IItemColorProvider colorProvider;
        private IItemDurabilityManager durabilityManager;
        private IEnchantabilityHelper enchantabilityHelper;
        private Rarity rarity;

        private int burnValue = 0;
        private boolean visible = true;
        private int maxStackSize = 64;
        private int modelAmount = 1;

        private final ResourceLocation id;
        private final IForgeRegistry<Item> registry;

        private List<ItemMaterialInfo> infosToAdd;
        private List<String> tagsToAdd;
        private Map<TagPrefix, Material> unificationData;

        protected ExtendedProperties(ResourceLocation id, IForgeRegistry<Item> registry) {
            this.id = id;
            this.registry = registry;
            this.unlocalizedName = id.getPath().replace('/', '_');
            this.infosToAdd = new ArrayList<>();
            this.tagsToAdd = new ArrayList<>();
            this.unificationData = new Object2ObjectOpenHashMap<>();
        }

        public MetaItem build() {
            var item = new StandardMetaItem();
            for(var info : infosToAdd) {
                TagUnifier.registerTag(item, info);
            }
            for (var tag : tagsToAdd) {
                GtItemRegistry.addTagToItem(item, tag);
            }
            for (var data : unificationData.entrySet()) {
                TagUnifier.registerTag(item, data.getKey(), data.getValue());
            }
            return item;
        }

        public ExtendedProperties setMaterialInfo(ItemMaterialInfo materialInfo) {
            if (materialInfo == null) {
                throw new IllegalArgumentException("Cannot add null ItemMaterialInfo.");
            }
            infosToAdd.add(materialInfo);
            //TagUnifier.registerTag(getMetaItem(), materialInfo);
            return this;
        }

        public ExtendedProperties setUnificationData(TagPrefix prefix, @Nullable Material material) {
            if (prefix == null) {
                throw new IllegalArgumentException("Cannot add null OrePrefix.");
            }
            unificationData.put(prefix, material);
            //TagUnifier.registerTag(getMetaItem(), prefix, material);
            return this;
        }

        public ExtendedProperties addTag(String tagName) {
            if (tagName == null) {
                throw new IllegalArgumentException("Cannot add null OreDictName.");
            }
            tagsToAdd.add(tagName);
            //GtItemRegistry.addTagToItem(this.getMetaItem(), tagName);
            return this;
        }

        public ExtendedProperties setInvisible(boolean isVisible) {
            this.visible = isVisible;
            return this;
        }

        public ExtendedProperties setInvisible() {
            this.visible = false;
            return this;
        }

        public ExtendedProperties setMaxStackSize(int maxStackSize) {
            if (maxStackSize <= 0) {
                throw new IllegalArgumentException("Cannot set Max Stack Size to negative or zero value.");
            }
            this.maxStackSize = maxStackSize;
            return this;
        }

        public ExtendedProperties setBurnValue(int burnValue) {
            if (burnValue <= 0) {
                throw new IllegalArgumentException("Cannot set Burn Value to negative or zero number.");
            }
            this.burnValue = burnValue;
            return this;
        }

        public ExtendedProperties disableModelLoading() {
            this.modelAmount = 0;
            return this;
        }

        public ExtendedProperties setModelAmount(int modelAmount) {
            if (modelAmount <= 0) {
                throw new IllegalArgumentException("Cannot set amount of models to negative or zero number.");
            }
            this.modelAmount = modelAmount;
            return this;
        }

        public ExtendedProperties setRarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }

        public ExtendedProperties addComponents(IItemComponent... stats) {
            addItemComponentsInternal(stats);
            return this;
        }

        protected void addItemComponentsInternal(IItemComponent... stats) {
            for (IItemComponent itemComponent : stats) {
                if (itemComponent instanceof IItemNameProvider) {
                    this.nameProvider = (IItemNameProvider) itemComponent;
                }
                if (itemComponent instanceof IItemMaxStackSizeProvider) {
                    this.stackSizeProvider = (IItemMaxStackSizeProvider) itemComponent;
                }
                if (itemComponent instanceof IItemContainerItemProvider) {
                    this.containerItemProvider = (IItemContainerItemProvider) itemComponent;
                }
                if (itemComponent instanceof IItemDurabilityManager) {
                    this.durabilityManager = (IItemDurabilityManager) itemComponent;
                }
                if (itemComponent instanceof IItemUseManager) {
                    this.useManager = (IItemUseManager) itemComponent;
                }
                if (itemComponent instanceof IFoodBehavior) {
                    this.useManager = new FoodUseManager((IFoodBehavior) itemComponent);
                }
                if (itemComponent instanceof ItemUIFactory)
                    this.uiManager = (ItemUIFactory) itemComponent;

                if (itemComponent instanceof IItemColorProvider) {
                    this.colorProvider = (IItemColorProvider) itemComponent;
                }
                if (itemComponent instanceof IItemBehaviour) {
                    this.behaviours.add((IItemBehaviour) itemComponent);
                    ((IItemBehaviour) itemComponent).addPropertyOverride(getMetaItem());
                }
                if (itemComponent instanceof IEnchantabilityHelper) {
                    this.enchantabilityHelper = (IEnchantabilityHelper) itemComponent;
                }
                this.allStats.add(itemComponent);
            }
        }

        public List<IItemComponent> getAllStats() {
            return Collections.unmodifiableList(allStats);
        }

        public List<IItemBehaviour> getBehaviours() {
            return Collections.unmodifiableList(behaviours);
        }

        @Nullable
        public IItemDurabilityManager getDurabilityManager() {
            return durabilityManager;
        }

        @Nullable
        public IItemUseManager getUseManager() {
            return useManager;
        }

        @Nullable
        public ItemUIFactory getUIManager() {
            return uiManager;
        }

        @Nullable
        public IItemColorProvider getColorProvider() {
            return colorProvider;
        }

        @Nullable
        public IItemNameProvider getNameProvider() {
            return nameProvider;
        }

        @Nullable
        public IItemContainerItemProvider getContainerItemProvider() {
            return containerItemProvider;
        }

        @Nullable
        public IEnchantabilityHelper getEnchantabilityHelper() {
            return enchantabilityHelper;
        }

        public int getBurnValue() {
            return burnValue;
        }

        public int getMaxStackSize(ItemStack stack) {
            return stackSizeProvider == null ? maxStackSize : stackSizeProvider.getMaxStackSize(stack, maxStackSize);
        }

        public boolean isVisible() {
            return visible;
        }

        public int getModelAmount() {
            return modelAmount;
        }

        public Rarity getRarity() {
            return rarity;
        }
    }

}
