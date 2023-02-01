package net.nemezanevem.gregtech.common.metatileentities.multi.multiblockpart;

import codechicken.lib.raytracer.VoxelShapeBlockHitResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.blockentity.multiblock.*;
import net.nemezanevem.gregtech.api.capability.IMaintenanceHatch;
import net.nemezanevem.gregtech.api.capability.impl.ItemHandlerProxy;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.gui.widgets.AdvancedTextWidget;
import net.nemezanevem.gregtech.api.gui.widgets.ClickButtonWidget;
import net.nemezanevem.gregtech.api.gui.widgets.SlotWidget;
import net.nemezanevem.gregtech.api.item.toolitem.ToolClass;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import net.nemezanevem.gregtech.common.ConfigHolder;
import net.nemezanevem.gregtech.common.item.metaitem.MetaItems;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.nemezanevem.gregtech.api.capability.GregtechDataCodes.*;

public class MetaTileEntityMaintenanceHatch extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IMaintenanceHatch>, IMaintenanceHatch {

    private final boolean isConfigurable;
    private boolean isTaped;

    // Used to store state temporarily if the Controller is broken
    private byte maintenanceProblems = -1;
    private int timeActive = -1;

    private BigDecimal durationMultiplier = BigDecimal.ONE;

    // Some stats used for the Configurable Maintenance Hatch
    private static final BigDecimal MAX_DURATION_MULTIPLIER = BigDecimal.valueOf(1.1);
    private static final BigDecimal MIN_DURATION_MULTIPLIER = BigDecimal.valueOf(0.9);
    private static final BigDecimal DURATION_ACTION_AMOUNT = BigDecimal.valueOf(0.01);
    private static final Function<Double, Double> TIME_ACTION = (d) -> {
        if (d < 1.0)
            return -20.0 * d + 21;
        else
            return -8.0 * d + 9;
    };

    public MetaTileEntityMaintenanceHatch(ResourceLocation metaTileEntityId, boolean isConfigurable) {
        super(metaTileEntityId, isConfigurable ? 3 : 1);
        this.isConfigurable = isConfigurable;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new MetaTileEntityMaintenanceHatch(metaTileEntityId, isConfigurable);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);

        if (shouldRenderOverlay()) {
            (isConfigurable ? Textures.MAINTENANCE_OVERLAY_CONFIGURABLE : isTaped ? Textures.MAINTENANCE_OVERLAY_TAPED : Textures.MAINTENANCE_OVERLAY)
                    .renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new TapeItemStackHandler(1);
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.itemInventory = new ItemHandlerProxy(importItems, importItems);
    }

    /**
     * Sets this Maintenance Hatch as being duct taped
     * @param isTaped is the state of the hatch being taped or not
     */
    @Override
    public void setTaped(boolean isTaped) {
        this.isTaped = isTaped;
        if (!getWorld().isClientSide) {
            writeCustomData(IS_TAPED, buf -> buf.writeBoolean(isTaped));
            markDirty();
        }
    }

    /**
     * Stores maintenance data to this MetaTileEntity
     * @param maintenanceProblems is the byte value representing the problems
     * @param timeActive is the int value representing the total time the parent multiblock has been active
     */
    @Override
    public void storeMaintenanceData(byte maintenanceProblems, int timeActive) {
        this.maintenanceProblems = maintenanceProblems;
        this.timeActive = timeActive;
        if (!getWorld().isClientSide) {
            writeCustomData(STORE_MAINTENANCE, buf -> {
                buf.writeByte(maintenanceProblems);
                buf.writeInt(timeActive);
            });
        }
    }

    /**
     *
     * @return whether this maintenance hatch has maintenance data
     */
    @Override
    public boolean hasMaintenanceData() {
        return this.maintenanceProblems != -1;
    }

    /**
     * reads this MetaTileEntity's maintenance data
     * @return Tuple of Byte, Integer corresponding to the maintenance problems, and total time active
     */
    @Override
    public Tuple<Byte, Integer> readMaintenanceData() {
        Tuple<Byte, Integer> data = new Tuple<>(this.maintenanceProblems, this.timeActive);
        storeMaintenanceData((byte) -1, -1);
        return data;
    }

    @Override
    public boolean startWithoutProblems() {
        return isConfigurable;
    }

    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isClientSide && getOffsetTimer() % 20 == 0) {
            MultiblockControllerBase controller = getController();
            if (controller instanceof IMaintenance) {
                if (((IMaintenance) controller).hasMaintenanceProblems()) {
                    if (consumeDuctTape(this.itemInventory, 0)) {
                        fixAllMaintenanceProblems();
                        setTaped(true);
                    }
                }
            }
        }
    }

    /**
     * Fixes the maintenance problems of this hatch's Multiblock Controller
     * @param entityPlayer the player performing the fixing
     */
    private void fixMaintenanceProblems(@Nullable Player entityPlayer) {
        if (!(this.getController() instanceof IMaintenance))
            return;

        if (!((IMaintenance) this.getController()).hasMaintenanceProblems())
            return;

        if (entityPlayer != null) {
            // Fix automatically on slot click by player in Creative Mode
            if (entityPlayer.isCreative()) {
                fixAllMaintenanceProblems();
                return;
            }
            // Then for every slot in the player's main inventory, try to duct tape fix
            for (int i = 0; i < entityPlayer.getInventory().items.size(); i++) {
                if (consumeDuctTape(new ItemStackHandler(entityPlayer.getInventory().items), i)) {
                    fixAllMaintenanceProblems();
                    setTaped(true);
                    return;
                }
            }
            // Lastly for each problem the multi has, try to fix with tools
            fixProblemsWithTools(((IMaintenance) this.getController()).getMaintenanceProblems(), entityPlayer);
        }
    }

    /**
     *
     * Handles duct taping for manual and auto-taping use
     *
     * @param handler is the handler to get duct tape from
     * @param slot is the inventory slot to check for tape
     * @return true if tape was consumed, else false
     */
    private boolean consumeDuctTape(@Nullable IItemHandler handler, int slot) {
        if (handler == null)
            return false;
        return consumeDuctTape(null, handler.getStackInSlot(slot));
    }

    private boolean consumeDuctTape(@Nullable Player player, ItemStack itemStack) {
        if (!itemStack.isEmpty() && itemStack.is(MetaItems.DUCT_TAPE.get())) {
            if (player == null || !player.isCreative()) {
                itemStack.shrink(1);
            }
            return true;
        }
        return false;
    }

    /**
     * Attempts to fix a provided maintenance problem with a tool in the player's
     * inventory, if the tool exists.
     *
     * @param problems Problem Flags
     * @param entityPlayer Target Player which their inventory would be scanned for tools to fix
     */
    private void fixProblemsWithTools(byte problems, Player entityPlayer) {
        List<ToolClass> toolsToMatch = Arrays.asList(new ToolClass[6]);
        boolean proceed = false;
        for (byte index = 0; index < 6; index++) {
            if (((problems >> index) & 1) == 0) {
                proceed = true;
                switch (index) {
                    case 0:
                        toolsToMatch.set(0, ToolClass.WRENCH);
                        break;
                    case 1:
                        toolsToMatch.set(1, ToolClass.SCREWDRIVER);
                        break;
                    case 2:
                        toolsToMatch.set(2, ToolClass.SOFT_MALLET);
                        break;
                    case 3:
                        toolsToMatch.set(3, ToolClass.HARD_HAMMER);
                        break;
                    case 4:
                        toolsToMatch.set(4, ToolClass.WIRE_CUTTER);
                        break;
                    case 5:
                        toolsToMatch.set(5, ToolClass.CROWBAR);
                        break;
                }
            }
        }
        if (!proceed) {
            return;
        }

        for (int i = 0; i < toolsToMatch.size(); i++) {
            ToolClass toolToMatch = toolsToMatch.get(i);
            if (toolToMatch != null) {
                // Try to use the item in the player's "hand" (under the cursor)
                ItemStack heldItem = entityPlayer.getInventory().getSelected();
                if (ToolHelper.isTool(heldItem, toolToMatch)) {
                    fixProblemWithTool(i, heldItem, entityPlayer);

                    if (toolsToMatch.stream().allMatch(Objects::isNull)) {
                        return;
                    }
                }

                // Then try all the remaining inventory slots
                for (ItemStack itemStack : entityPlayer.getInventory().items) {
                    if (ToolHelper.isTool(itemStack, toolToMatch)) {
                        fixProblemWithTool(i, itemStack, entityPlayer);

                        if (toolsToMatch.stream().allMatch(Objects::isNull)) {
                            return;
                        }
                    }
                }

                for (ItemStack stack : entityPlayer.getInventory().items) {
                    if (ToolHelper.isTool(stack, toolToMatch)) {
                        ((IMaintenance) this.getController()).setMaintenanceFixed(i);
                        ToolHelper.damageItemWhenCrafting(stack, entityPlayer);
                        if (toolsToMatch.stream().allMatch(Objects::isNull)) {
                            return;
                        }
                    }
                }
            }
        }
    }

    private void fixProblemWithTool(int problemIndex, ItemStack stack, Player player) {
        ((IMaintenance) getController()).setMaintenanceFixed(problemIndex);
        ToolHelper.damageItemWhenCrafting(stack, player);
        setTaped(false);
    }

    /**
     * Fixes every maintenance problem of the controller
     */
    public void fixAllMaintenanceProblems() {
        if (this.getController() instanceof IMaintenance)
            for (int i = 0; i < 6; i++) ((IMaintenance) this.getController()).setMaintenanceFixed(i);
    }

    @Override
    public boolean isFullAuto() {
        return false;
    }

    @Override
    public double getDurationMultiplier() {
        return durationMultiplier.doubleValue();
    }

    @Override
    public double getTimeMultiplier() {
        return BigDecimal.valueOf(TIME_ACTION.apply(durationMultiplier.doubleValue()))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private void incInternalMultiplier(Widget.ClickData data) {
        if (durationMultiplier.compareTo(MAX_DURATION_MULTIPLIER) == 0) return;
        durationMultiplier = durationMultiplier.add(DURATION_ACTION_AMOUNT);
        writeCustomData(MAINTENANCE_MULTIPLIER, b -> b.writeDouble(durationMultiplier.doubleValue()));
    }

    private void decInternalMultiplier(Widget.ClickData data) {
        if (durationMultiplier.compareTo(MIN_DURATION_MULTIPLIER) == 0) return;
        durationMultiplier = durationMultiplier.subtract(DURATION_ACTION_AMOUNT);
        writeCustomData(MAINTENANCE_MULTIPLIER, b -> b.writeDouble(durationMultiplier.doubleValue()));
    }

    @Override
    public void onRemoval() {
        if (getController() instanceof IMaintenance) {
            IMaintenance controller = (IMaintenance) getController();
            if (!getWorld().isClientSide && controller != null)
                controller.storeTaped(isTaped);
        }
        super.onRemoval();
    }

    @Override
    public boolean onRightClick(Player playerIn, InteractionHand hand, Direction facing, VoxelShapeBlockHitResult hitResult) {
        if (getController() instanceof IMaintenance && ((IMaintenance) getController()).hasMaintenanceProblems()) {
            if (consumeDuctTape(playerIn, playerIn.getItemInHand(hand))) {
                fixAllMaintenanceProblems();
                setTaped(true);
                return true;
            }
        }
        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    protected ModularUI createUI(Player entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 18 * 3 + 98)
                .label(5, 5, getMetaFullName())
                .bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 18 * 3 + 16);

        if (!isConfigurable && GTValues.FOOLS.get()) {
            builder.widget(new FixWiringTaskWidget(48, 15, 80, 50)
                    .setOnFinished(this::fixAllMaintenanceProblems)
                    .setCanInteractPredicate(this::isAttachedToMultiBlock));
        } else {
            builder.widget(new SlotWidget(importItems, 0, 89 - 10, 18 - 1)
                    .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.DUCT_TAPE_OVERLAY).setTooltipText("gregtech.machine.maintenance_hatch_tape_slot.tooltip"))
                    .widget(new ClickButtonWidget(89 - 10 - 1, 18 * 2 + 3, 20, 20, "", data -> fixMaintenanceProblems(entityPlayer))
                            .setButtonTexture(GuiTextures.MAINTENANCE_ICON).setTooltipText("gregtech.machine.maintenance_hatch_tool_slot.tooltip"));
        }
        if (isConfigurable) {
            builder.widget(new AdvancedTextWidget(5, 25, getTextWidgetText("duration", this::getDurationMultiplier), 0x404040))
                    .widget(new AdvancedTextWidget(5, 39, getTextWidgetText("time", this::getTimeMultiplier), 0x404040))
                    .widget(new ClickButtonWidget(9, 18 * 3 + 16 - 18, 12, 12, "-", this::decInternalMultiplier))
                    .widget(new ClickButtonWidget(9 + 18 * 2, 18 * 3 + 16 - 18, 12, 12, "+", this::incInternalMultiplier));
        }
        return builder.build(getHolder(), entityPlayer);
    }

    private Consumer<List<Component>> getTextWidgetText(String type, Supplier<Double> multiplier) {
        return (list) -> {
            Component tooltip;
            if (multiplier.get() == 1.0) {
                tooltip = Component.translatable("gregtech.maintenance.configurable_" + type + ".unchanged_description");
            } else {
                tooltip = Component.translatable("gregtech.maintenance.configurable_" + type + ".changed_description", multiplier.get());
            }
            list.add(Component.translatable("gregtech.maintenance.configurable_" + type, multiplier.get())
                    .withStyle(style ->  style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))));
        };
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.putBoolean("IsTaped", isTaped);
        if (isConfigurable) data.putDouble("DurationMultiplier", durationMultiplier.doubleValue());
        return data;
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        isTaped = data.getBoolean("IsTaped");
        if (isConfigurable) durationMultiplier = BigDecimal.valueOf(data.getDouble("DurationMultiplier"));
    }

    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isTaped);
        if (isConfigurable) buf.writeDouble(durationMultiplier.doubleValue());
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        super.receiveInitialSyncData(buf);
        isTaped = buf.readBoolean();
        if (isConfigurable) durationMultiplier = BigDecimal.valueOf(buf.readDouble());
    }

    @Override
    public void receiveCustomData(int dataId, FriendlyByteBuf buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == STORE_MAINTENANCE) {
            this.maintenanceProblems = buf.readByte();
            this.timeActive = buf.readInt();
            markDirty();
        } else if (dataId == IS_TAPED) {
            this.isTaped = buf.readBoolean();
            scheduleRenderUpdate();
            markDirty();
        } else if (dataId == MAINTENANCE_MULTIPLIER) {
            this.durationMultiplier = BigDecimal.valueOf(buf.readDouble());
            markDirty();
        }
    }

    @Override
    public MultiblockAbility<IMaintenanceHatch> getAbility() {
        return GtMultiblockAbilities.MAINTENANCE_HATCH.get();
    }

    @Override
    public void registerAbilities(List<IMaintenanceHatch> abilityList) {
        abilityList.add(this);
    }

    @Override
    public boolean canPartShare() {
        return false;
    }

    @Override
    public void getSubItems(CreativeModeTab creativeTab, NonNullList<ItemStack> subItems) {
        if (ConfigHolder.machines.enableMaintenance) {
            super.getSubItems(creativeTab, subItems);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level player, List<Component> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(Component.translatable("gregtech.universal.disabled"));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable Level world, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(Component.translatable("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
        tooltip.add(Component.translatable("gregtech.tool_action.tape"));
    }
}
