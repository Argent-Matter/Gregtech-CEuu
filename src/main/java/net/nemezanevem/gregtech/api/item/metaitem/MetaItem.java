package net.nemezanevem.gregtech.api.item.metaitem;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
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
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.ItemHandlerHelper;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.capability.GregtechCapabilities;
import net.nemezanevem.gregtech.api.capability.IElectricItem;
import net.nemezanevem.gregtech.api.capability.IThermalFluidHandlerItemStack;
import net.nemezanevem.gregtech.api.capability.impl.CombinedCapabilityProvider;
import net.nemezanevem.gregtech.api.capability.impl.ElectricItem;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.item.gui.ItemUIFactory;
import net.nemezanevem.gregtech.api.item.gui.PlayerInventoryHolder;
import net.nemezanevem.gregtech.api.item.materialitem.PrefixItem;
import net.nemezanevem.gregtech.api.item.metaitem.stats.*;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.TagUnifier;
import net.nemezanevem.gregtech.api.unification.stack.ItemMaterialInfo;
import net.nemezanevem.gregtech.api.unification.tag.TagPrefix;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.common.item.GtItemRegistry;

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
 * Items are added in MetaItem via {@link #builder(String)}. You will get {@link ExtendedProperties} instance, which you can configure in builder-alike pattern:
 * {@code addItem(0, "test_item").addStats(new ElectricStats(10000, 1,  false)) }
 * This will add single-use (not rechargeable) LV battery with initial capacity 10000 EU
 */
public abstract class MetaItem extends Item implements ItemUIFactory {

    private static final List<MetaItem> META_ITEMS = new ArrayList<>();
    protected static final Map<ResourceLocation, ModelResourceLocation> metaItemsModels = new HashMap<>();
    protected static final Map<ResourceLocation, ModelResourceLocation[]> specialItemsModels = new HashMap<>();

    public static List<MetaItem> getMetaItems() {
        return Collections.unmodifiableList(META_ITEMS);
    }


    public final String unlocalizedName;
    private IItemNameProvider nameProvider;
    private IItemContainerItemProvider containerItemProvider;

    private final List<IItemComponent> allStats = new ArrayList<>();
    private final List<IItemBehaviour> behaviours = new ArrayList<>();
    private IItemUseManager useManager;
    private ItemUIFactory uiManager;
    private IItemColorProvider colorProvider;
    private IItemDurabilityManager durabilityManager;
    private IEnchantabilityHelper enchantabilityHelper;

    private int burnValue = 0;
    private boolean visible = true;
    private int modelAmount = 1;


    public MetaItem(ExtendedProperties properties) {
        super(properties);
        this.unlocalizedName = properties.unlocalizedName;
        this.nameProvider = properties.nameProvider;
        META_ITEMS.add(this);
    }

    @SubscribeEvent
    public static void registerItemColors(final RegisterColorHandlersEvent.Item event) {
        for(var item : getMetaItems()) {
            event.register(item::getColorForItemStack, item);
        }
    }

    public static void registerModels() {
        for (var item : META_ITEMS) {
            ResourceLocation resourceLocation = createItemModelPath(item, "");
            var modelLoc = new ModelResourceLocation(resourceLocation, "inventory");
            Minecraft.getInstance().getItemRenderer().getItemModelShaper().register(item, modelLoc);
            metaItemsModels.put(Util.getId(item), modelLoc);
        }
    }

    public static ResourceLocation createItemModelPath(MetaItem metaValueItem, String postfix) {
        return new ResourceLocation(GregTech.MODID, formatModelPath(metaValueItem) + postfix);
    }

    protected static String formatModelPath(MetaItem metaValueItem) {
        return "metaitems/" + metaValueItem.unlocalizedName;
    }

    protected int getColorForItemStack(ItemStack stack, int tintIndex) {
        Item item = stack.getItem();
        if(!(item instanceof MetaItem metaValueItem) || metaValueItem.getColorProvider() != null) {
            return 0xFFFFFF;
        }
        return metaValueItem.getColorProvider().getItemStackColor(stack, tintIndex);
    }

    @Override
    public int getDamage(@Nonnull ItemStack stack) {
        Item item = stack.getItem();
        if(!(item instanceof MetaItem metaValueItem) || metaValueItem.getDurabilityManager() == null) {
            return -1;
        }
        return (int) (metaValueItem.getDurabilityManager().getDurabilityForDisplay(stack) * this.getMaxDamage(stack));
    }

    public static ExtendedProperties builder(String unlocalizedName) {
        return new ExtendedProperties(unlocalizedName);
    }

    @Override
    public ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable CompoundTag nbt) {
        Item item = stack.getItem();
        if (item instanceof MetaItem metaItem) {
            ArrayList<ICapabilityProvider> providers = new ArrayList<>();
            for (IItemComponent itemComponent : metaItem.getAllStats()) {
                if (itemComponent instanceof IItemCapabilityProvider) {
                    IItemCapabilityProvider provider = (IItemCapabilityProvider) itemComponent;
                    providers.add(provider.createProvider(stack));
                }
            }
            return new CombinedCapabilityProvider(providers);
        }
        return null;
    }

    //////////////////////////////////////////////////////////////////

    @Override
    public int getBurnTime(@Nonnull ItemStack itemStack, RecipeType<?> recipeType) {
        Item item = itemStack.getItem();
        if (item instanceof MetaItem metaItem) {
            return metaItem.getBurnValue();
        }
        return super.getBurnTime(itemStack, recipeType);
    }

    //////////////////////////////////////////////////////////////////
    //      Behaviours and Use Manager Implementation               //
    //////////////////////////////////////////////////////////////////

    private IItemUseManager getUseManager(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item instanceof MetaItem metaItem) {
            return metaItem.getUseManager();
        }
        return null;
    }

    public List<IItemBehaviour> getBehaviours(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item instanceof MetaItem metaItem) {
            metaItem.getBehaviours();
        }
        return ImmutableList.<IItemBehaviour>of();
    }

    @Override
    public int getMaxStackSize(@Nonnull ItemStack stack) {
        /*Item item = stack.getItem();
        if (item instanceof MetaItem metaItem) {
            return item.getMaxStackSize(stack);
        }*/
        return super.getMaxStackSize(stack);
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
    public int getUseDuration(@Nonnull ItemStack stack) {
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
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int timeLeft) {
        if (user instanceof Player player) {
            IItemUseManager useManager = getUseManager(stack);
            if (useManager != null) {
                useManager.onPlayerStoppedItemUsing(stack, player, timeLeft);
            }
        }
    }

    @Nullable
    @Override
    public ItemStack finishUsingItem(@Nonnull ItemStack stack, @Nonnull Level world, @Nonnull LivingEntity player) {
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
    public InteractionResult interactLivingEntity(@Nonnull ItemStack stack, @Nonnull Player playerIn, @Nonnull LivingEntity target, @Nonnull InteractionHand hand) {
        InteractionResult returnValue = InteractionResult.PASS;
        for (IItemBehaviour behaviour : getBehaviours(stack)) {
            if (behaviour.itemInteractionForEntity(stack, playerIn, target, hand)) {
                returnValue = InteractionResult.SUCCESS;
            }
        }
        return returnValue;
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level world, Player player, @Nonnull InteractionHand hand) {
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
        Item item = stack.getItem();
        if (item instanceof MetaItem metaItem) {
            IEnchantabilityHelper helper = metaItem.getEnchantabilityHelper();
            return helper != null && helper.isEnchantable(stack);
        }
        return super.isEnchantable(stack);
    }

    @Override
    public int getEnchantmentValue() {
        IEnchantabilityHelper helper = this.getEnchantabilityHelper();
        return helper == null ? super.getEnchantmentValue() : helper.getItemEnchantability();
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
    public void inventoryTick(@Nonnull ItemStack stack, @Nonnull Level worldIn, @Nonnull Entity entityIn, int itemSlot, boolean isSelected) {
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
        Item item = stack.getItem();
        if (!(item instanceof MetaItem metaItem)) {
            return Component.literal("invalid item");
        }
        String unlocalizedName = String.format("metaitem.%s.name", metaItem.unlocalizedName);
        if (metaItem.getNameProvider() != null) {
            return metaItem.getNameProvider().getItemStackDisplayName(stack, unlocalizedName);
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
        return Component.translatable(unlocalizedName);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack itemStack, @Nullable Level worldIn, @Nonnull List<Component> lines, @Nonnull TooltipFlag pIsAdvanced) {
        Item item = itemStack.getItem();
        if (!(item instanceof MetaItem metaItem)) {
            return;
        }
        var unlocalizedTooltip = Component.translatable("metaitem." + metaItem.unlocalizedName + ".tooltip");
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
            IFluidTank fluidTankProperties = realFluidHandler.getTankProperties()[0];
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
            lines.add(Component.literal("MetaItem Id: " + metaItem.unlocalizedName));
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
        Item item = itemStack.getItem();
        if (!(item instanceof MetaItem metaItem)) {
            return false;
        }
        return metaItem.getContainerItemProvider() != null;
    }

    @Nonnull
    @Override
    public ItemStack getContainerItem(@Nonnull ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (!(item instanceof MetaItem metaItem)) {
            return ItemStack.EMPTY;
        }
        itemStack = itemStack.copy();
        itemStack.setCount(1);
        IItemContainerItemProvider provider = metaItem.getContainerItemProvider();
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
        Item item = itemStack.getItem();
        ItemUIFactory uiFactory = null;
        if(item instanceof MetaItem metaItem) {
            uiFactory = metaItem.getUIManager();
        }
        return uiFactory == null ? null : uiFactory.createUI(holder, Player);
    }

    // IOverlayRenderAware
    @Override
    public void renderItemOverlayIntoGUI(@Nonnull ItemStack stack, int xPosition, int yPosition) {
        ToolChargeBarRenderer.renderBarsItem(this, stack, xPosition, yPosition);
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


    /**
     * Attempts to get an fully charged variant of this electric item
     *
     * @param chargeAmount amount of charge
     * @return charged electric item stack
     * @throws java.lang.IllegalStateException if this item is not electric item
     */
    public ItemStack getChargedStack(long chargeAmount) {
        ItemStack itemStack = new ItemStack(this, 1);
        LazyOptional<IElectricItem> electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (!electricItem.isPresent()) {
            throw new IllegalStateException("Not a supported electric item.");
        }
        electricItem.resolve().get().charge(chargeAmount, Integer.MAX_VALUE, true, false);
        return itemStack;
    }

    public ItemStack getInfiniteChargedStack() {
        ItemStack itemStack = new ItemStack(this, 1);
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
        ItemStack itemStack = new ItemStack(this, 1);
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
        ItemStack itemStack = new ItemStack(this, 1);
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

    public static class ExtendedProperties extends Item.Properties {
        public final String unlocalizedName;
        private IItemNameProvider nameProvider;
        private IItemContainerItemProvider containerItemProvider;

        private final List<IItemComponent> allStats = new ArrayList<>();
        private final List<IItemBehaviour> behaviours = new ArrayList<>();
        private IItemUseManager useManager;
        private ItemUIFactory uiManager;
        private IItemColorProvider colorProvider;
        private IItemDurabilityManager durabilityManager;
        private IEnchantabilityHelper enchantabilityHelper;

        private int burnValue = 0;
        private boolean visible = true;
        private int modelAmount = 1;

        private List<ItemMaterialInfo> infosToAdd;
        private List<String> tagsToAdd;
        private Map<TagPrefix, Material> unificationData;
        private List<IItemComponent> components;


        protected ExtendedProperties(String unlocalizedName) {
            this.unlocalizedName = unlocalizedName;
            this.infosToAdd = new ArrayList<>();
            this.tagsToAdd = new ArrayList<>();
            this.unificationData = new Object2ObjectOpenHashMap<>();
            this.components = new ArrayList<>();
        }

        public MetaItem build() {
            var item = new StandardMetaItem(this);
            for(var info : infosToAdd) {
                TagUnifier.registerTag(item, info);
            }
            for (var tag : tagsToAdd) {
                GtItemRegistry.addTagToItem(item, tag);
            }
            for (var data : unificationData.entrySet()) {
                TagUnifier.registerTag(item, data.getKey(), data.getValue());
            }
            addItemComponentsInternal(item, components);
            return item;
        }

        public PrefixItem build(TagPrefix tagPrefix, Material material) {
            var item = new PrefixItem(this, tagPrefix, material);
            for(var info : infosToAdd) {
                TagUnifier.registerTag(item, info);
            }
            for (var tag : tagsToAdd) {
                GtItemRegistry.addTagToItem(item, tag);
            }
            for (var data : unificationData.entrySet()) {
                TagUnifier.registerTag(item, data.getKey(), data.getValue());
            }
            addItemComponentsInternal(item, components);
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

        public ExtendedProperties setVisible(boolean isVisible) {
            this.visible = isVisible;
            return this;
        }

        public ExtendedProperties setInvisible() {
            this.visible = false;
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

        public ExtendedProperties addComponents(IItemComponent... stats) {
            //addItemComponentsInternal(stats);
            components.addAll(Arrays.asList(stats));
            return this;
        }

        protected void addItemComponentsInternal(MetaItem item, List<IItemComponent> stats) {
            for (IItemComponent itemComponent : stats) {
                if (itemComponent instanceof IItemNameProvider) {
                    this.nameProvider = (IItemNameProvider) itemComponent;
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
                    ((IItemBehaviour) itemComponent).addPropertyOverride(item);
                }
                if (itemComponent instanceof IEnchantabilityHelper) {
                    this.enchantabilityHelper = (IEnchantabilityHelper) itemComponent;
                }
                this.allStats.add(itemComponent);
            }
        }
    }

}
