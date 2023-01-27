package net.nemezanevem.gregtech.api.tileentity;

import codechicken.lib.raytracer.VoxelShapeBlockHitResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IActiveOutputSide;
import gregtech.api.capability.impl.*;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeType;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.RenderUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.core.Direction;
import net.minecraft.entity.player.Player;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Direction;
import net.minecraft.util.InteractionHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.World;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.nemezanevem.gregtech.api.capability.IActiveOutputSide;
import net.nemezanevem.gregtech.api.capability.impl.*;
import net.nemezanevem.gregtech.api.cover.CoverBehavior;
import net.nemezanevem.gregtech.api.cover.CoverDefinition;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.gui.resources.TextureArea;
import net.nemezanevem.gregtech.api.gui.widgets.ClickButtonWidget;
import net.nemezanevem.gregtech.api.gui.widgets.ImageWidget;
import net.nemezanevem.gregtech.api.gui.widgets.SlotWidget;
import net.nemezanevem.gregtech.api.tileentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.client.renderer.ICubeRenderer;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import net.nemezanevem.gregtech.client.util.RenderUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static gregtech.api.capability.GregtechDataCodes.*;

public class SimpleMachineMetaTileEntity extends WorkableTieredMetaTileEntity implements IActiveOutputSide {

    private final boolean hasFrontFacing;

    protected final ItemStackHandler chargerInventory;
    protected ItemStackHandler circuitInventory;
    private Direction outputFacingItems;
    private Direction outputFacingFluids;

    private boolean autoOutputItems;
    private boolean autoOutputFluids;
    private boolean allowInputFromOutputSideItems = false;
    private boolean allowInputFromOutputSideFluids = false;

    protected IItemHandler outputItemInventory;
    protected IFluidHandler outputFluidInventory;
    protected IItemHandlerModifiable importItemsWithCircuit;

    private static final int FONT_HEIGHT = 9; // Minecraft's FontRenderer FONT_HEIGHT value

    public SimpleMachineMetaTileEntity(ResourceLocation metaTileEntityId, RecipeType<?> recipeMap, ICubeRenderer renderer, int tier, boolean hasFrontFacing) {
        this(metaTileEntityId, recipeMap, renderer, tier, hasFrontFacing, GTUtility.defaultTankSizeFunction);
    }

    public SimpleMachineMetaTileEntity(ResourceLocation metaTileEntityId, RecipeType<?> recipeMap, ICubeRenderer renderer, int tier, boolean hasFrontFacing,
                                       Function<Integer, Integer> tankScalingFunction) {
        super(metaTileEntityId, recipeMap, renderer, tier, tankScalingFunction);
        this.hasFrontFacing = hasFrontFacing;
        this.chargerInventory = new ItemStackHandler(1);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new SimpleMachineMetaTileEntity(metaTileEntityId, workable.getRecipeType(), renderer, getTier(), hasFrontFacing, getTankScalingFunction());
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.outputItemInventory = new ItemHandlerProxy(new ItemStackHandler(0), exportItems);
        this.outputFluidInventory = new FluidHandlerProxy(new FluidTankList(false), exportFluids);
        this.circuitInventory = new NotifiableItemStackHandler(1, this, false);

        List<IItemHandlerModifiable> temp = new ArrayList<>();
        temp.add(importItems);
        temp.add(circuitInventory);
        this.importItemsWithCircuit = new ItemHandlerList(temp);
    }

    @Override
    public IItemHandlerModifiable getImportItems() {
        ItemStack circStack = circuitInventory != null ? circuitInventory.getStackInSlot(0) : ItemStack.EMPTY;
        if (circStack != ItemStack.EMPTY && IntCircuitIngredient.isIntegratedCircuit(circStack)) {
            return importItemsWithCircuit;
        } else {
            return super.getImportItems();
        }
    }

    @Override
    public boolean hasFrontFacing() {
        return hasFrontFacing;
    }

    @Override
    public boolean onWrenchClick(Player playerIn, InteractionHand hand, Direction facing, VoxelShapeBlockHitResult hitResult) {
        if (!playerIn.isCrouching()) {
            //TODO Separate into two output getters
            if (getOutputFacing() == facing) return false;
            if (hasFrontFacing() && facing == getFrontFacing()) return false;
            if (!getWorld().isClientSide) {
                //TODO Separate into two output setters
                setOutputFacing(facing);
            }
            return true;
        }
        return super.onWrenchClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public boolean placeCoverOnSide(Direction side, ItemStack itemStack, CoverDefinition coverDefinition, Player player) {
        boolean coverPlaced = super.placeCoverOnSide(side, itemStack, coverDefinition, player);
        if (coverPlaced) {
            CoverBehavior cover = getCoverAtSide(side);
            if (cover != null && cover.shouldCoverInteractWithOutputside()) {
                if (getOutputFacingItems() == side) {
                    setAllowInputFromOutputSideItems(true);
                }
                if (getOutputFacingFluids() == side) {
                    setAllowInputFromOutputSideFluids(true);
                }
            }
        }
        return coverPlaced;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (outputFacingFluids != null && getExportFluids().getTanks() > 0) {
            Textures.PIPE_OUT_OVERLAY.renderSided(outputFacingFluids, renderState, RenderUtil.adjustTrans(translation, outputFacingFluids, 2), pipeline);
        }
        if (outputFacingItems != null && getExportItems().getSlots() > 0) {
            Textures.PIPE_OUT_OVERLAY.renderSided(outputFacingItems, renderState, RenderUtil.adjustTrans(translation, outputFacingItems, 2), pipeline);
        }
        if (isAutoOutputItems() && outputFacingItems != null) {
            Textures.ITEM_OUTPUT_OVERLAY.renderSided(outputFacingItems, renderState, RenderUtil.adjustTrans(translation, outputFacingItems, 2), pipeline);
        }
        if (isAutoOutputFluids() && outputFacingFluids != null) {
            Textures.FLUID_OUTPUT_OVERLAY.renderSided(outputFacingFluids, renderState, RenderUtil.adjustTrans(translation, outputFacingFluids, 2), pipeline);
        }
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isClientSide) {
            ((EnergyContainerHandler) this.energyContainer).dischargeOrRechargeEnergyContainers(chargerInventory, 0);

            if (getOffsetTimer() % 5 == 0) {
                if (isAutoOutputFluids()) {
                    pushFluidsIntoNearbyHandlers(getOutputFacingFluids());
                }
                if (isAutoOutputItems()) {
                    pushItemsIntoNearbyHandlers(getOutputFacingItems());
                }
            }
        }
    }

    @Override
    public boolean onScrewdriverClick(Player playerIn, InteractionHand hand, Direction facing, VoxelShapeBlockHitResult hitResult) {
        if (!getWorld().isClientSide) {
            if (isAllowInputFromOutputSideItems()) {
                setAllowInputFromOutputSideItems(false);
                setAllowInputFromOutputSideFluids(false);
                playerIn.sendMessage(Component.translatable("gregtech.machine.basic.input_from_output_side.disallow"));
            } else {
                setAllowInputFromOutputSideItems(true);
                setAllowInputFromOutputSideFluids(true);
                playerIn.sendMessage(Component.translatable("gregtech.machine.basic.input_from_output_side.allow"));
            }
        }
        return true;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, Direction side) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            IFluidHandler fluidHandler = (side == getOutputFacingFluids() && !isAllowInputFromOutputSideFluids()) ? outputFluidInventory : fluidInventory;
            if (fluidHandler.getTankProperties().length > 0) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);
            }
            return null;
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            IItemHandler itemHandler = (side == getOutputFacingItems() && !isAllowInputFromOutputSideFluids()) ? outputItemInventory : itemInventory;
            if (itemHandler.getSlots() > 0) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler);
            }
            return null;
        } else if (capability == GregtechTileCapabilities.CAPABILITY_ACTIVE_OUTPUT_SIDE) {
            if (side == getOutputFacingItems() || side == getOutputFacingFluids()) {
                return GregtechTileCapabilities.CAPABILITY_ACTIVE_OUTPUT_SIDE.cast(this);
            }
            return null;
        }
        return super.getCapability(capability, side);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.setTag("ChargerInventory", chargerInventory.serializeNBT());
        data.setTag("CircuitInventory", circuitInventory.serializeNBT());
        data.setInteger("OutputFacing", getOutputFacingItems().getIndex());
        data.setInteger("OutputFacingF", getOutputFacingFluids().getIndex());
        data.setBoolean("AutoOutputItems", autoOutputItems);
        data.setBoolean("AutoOutputFluids", autoOutputFluids);
        data.setBoolean("AllowInputFromOutputSide", allowInputFromOutputSideItems);
        data.setBoolean("AllowInputFromOutputSideF", allowInputFromOutputSideFluids);
        return data;
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        this.chargerInventory.deserializeNBT(data.getCompoundTag("ChargerInventory"));
        if (data.hasKey("CircuitInventory")) {
            this.circuitInventory.deserializeNBT(data.getCompoundTag("CircuitInventory"));
        }
        this.outputFacingItems = Direction.VALUES[data.getInteger("OutputFacing")];
        this.outputFacingFluids = Direction.VALUES[data.getInteger("OutputFacingF")];
        this.autoOutputItems = data.getBoolean("AutoOutputItems");
        this.autoOutputFluids = data.getBoolean("AutoOutputFluids");
        this.allowInputFromOutputSideItems = data.getBoolean("AllowInputFromOutputSide");
        this.allowInputFromOutputSideFluids = data.getBoolean("AllowInputFromOutputSideF");
    }

    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(getOutputFacingItems().getIndex());
        buf.writeByte(getOutputFacingFluids().getIndex());
        buf.writeBoolean(autoOutputItems);
        buf.writeBoolean(autoOutputFluids);
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        super.receiveInitialSyncData(buf);
        this.outputFacingItems = Direction.VALUES[buf.readByte()];
        this.outputFacingFluids = Direction.VALUES[buf.readByte()];
        this.autoOutputItems = buf.readBoolean();
        this.autoOutputFluids = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, FriendlyByteBuf buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_OUTPUT_FACING) {
            this.outputFacingItems = Direction.VALUES[buf.readByte()];
            this.outputFacingFluids = Direction.VALUES[buf.readByte()];
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_AUTO_OUTPUT_ITEMS) {
            this.autoOutputItems = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_AUTO_OUTPUT_FLUIDS) {
            this.autoOutputFluids = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @Override
    public boolean isValidFrontFacing(Direction facing) {
        //use direct outputFacing field instead of getter method because otherwise
        //it will just return SOUTH for null output facing
        return super.isValidFrontFacing(facing) && facing != outputFacingItems && facing != outputFacingFluids;
    }

    @Deprecated
    public void setOutputFacing(Direction outputFacing) {
        this.outputFacingItems = outputFacing;
        this.outputFacingFluids = outputFacing;
        if (!getWorld().isClientSide) {
            notifyBlockUpdate();
            writeCustomData(UPDATE_OUTPUT_FACING, buf -> {
                buf.writeByte(outputFacingItems.getIndex());
                buf.writeByte(outputFacingFluids.getIndex());
            });
            markDirty();
        }
    }

    public void setOutputFacingItems(Direction outputFacing) {
        this.outputFacingItems = outputFacing;
        if (!getWorld().isClientSide) {
            notifyBlockUpdate();
            writeCustomData(UPDATE_OUTPUT_FACING, buf -> {
                buf.writeByte(outputFacingItems.getIndex());
                buf.writeByte(outputFacingFluids.getIndex());
            });
            markDirty();
        }
    }

    public void setOutputFacingFluids(Direction outputFacing) {
        this.outputFacingFluids = outputFacing;
        if (!getWorld().isClientSide) {
            notifyBlockUpdate();
            writeCustomData(UPDATE_OUTPUT_FACING, buf -> {
                buf.writeByte(outputFacingItems.getIndex());
                buf.writeByte(outputFacingFluids.getIndex());
            });
            markDirty();
        }
    }

    public void setAutoOutputItems(boolean autoOutputItems) {
        this.autoOutputItems = autoOutputItems;
        if (!getWorld().isClientSide) {
            writeCustomData(UPDATE_AUTO_OUTPUT_ITEMS, buf -> buf.writeBoolean(autoOutputItems));
            markDirty();
        }
    }

    public void setAutoOutputFluids(boolean autoOutputFluids) {
        this.autoOutputFluids = autoOutputFluids;
        if (!getWorld().isClientSide) {
            writeCustomData(UPDATE_AUTO_OUTPUT_FLUIDS, buf -> buf.writeBoolean(autoOutputFluids));
            markDirty();
        }
    }

    public void setAllowInputFromOutputSideItems(boolean allowInputFromOutputSide) {
        this.allowInputFromOutputSideItems = allowInputFromOutputSide;
        if (!getWorld().isClientSide) {
            markDirty();
        }
    }

    public void setAllowInputFromOutputSideFluids(boolean allowInputFromOutputSide) {
        this.allowInputFromOutputSideFluids = allowInputFromOutputSide;
        if (!getWorld().isClientSide) {
            markDirty();
        }
    }

    @Override
    public void setFrontFacing(Direction frontFacing) {
        super.setFrontFacing(frontFacing);
        if (this.outputFacingItems == null || this.outputFacingFluids == null) {
            //set initial output facing as opposite to front
            setOutputFacing(frontFacing.getOpposite());
        }
    }

    @Deprecated
    public Direction getOutputFacing() {
        return getOutputFacingItems();
    }

    public Direction getOutputFacingItems() {
        return outputFacingItems == null ? Direction.SOUTH : outputFacingItems;
    }

    public Direction getOutputFacingFluids() {
        return outputFacingFluids == null ? Direction.SOUTH : outputFacingFluids;
    }

    public boolean isAutoOutputItems() {
        return autoOutputItems;
    }

    public boolean isAutoOutputFluids() {
        return autoOutputFluids;
    }

    public boolean isAllowInputFromOutputSideItems() {
        return allowInputFromOutputSideItems;
    }

    public boolean isAllowInputFromOutputSideFluids() {
        return allowInputFromOutputSideFluids;
    }

    @Override
    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
        clearInventory(itemBuffer, chargerInventory);
        clearInventory(itemBuffer, circuitInventory);
    }

    protected ModularUI.Builder createGuiTemplate(Player player) {
        RecipeType<?> workableRecipeType = workable.getRecipeType();
        int yOffset = 0;
        if (workableRecipeType.getMaxInputs() >= 6 || workableRecipeType.getMaxFluidInputs() >= 6 || workableRecipeType.getMaxOutputs() >= 6 || workableRecipeType.getMaxFluidOutputs() >= 6) {
            yOffset = FONT_HEIGHT;
        }

        ModularUI.Builder builder = workableRecipeType.createUITemplate(workable::getProgressPercent, importItems, exportItems, importFluids, exportFluids, yOffset)
                .widget(new LabelWidget(5, 5, getMetaFullName()))
                .widget(new SlotWidget(chargerInventory, 0, 79, 62 + yOffset, true, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.CHARGER_OVERLAY)
                        .setTooltipText("gregtech.gui.charger_slot.tooltip", GTValues.VNF[getTier()], GTValues.VNF[getTier()]))
                .widget(new ImageWidget(79, 42 + yOffset, 18, 18, GuiTextures.INDICATOR_NO_ENERGY).setIgnoreColor(true)
                        .setPredicate(workable::isHasNotEnoughEnergy))
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, yOffset);

        int leftButtonStartX = 7;

        if (exportItems.getSlots() > 0) {
            builder.widget(new ToggleButtonWidget(leftButtonStartX, 62 + yOffset, 18, 18,
                    GuiTextures.BUTTON_ITEM_OUTPUT, this::isAutoOutputItems, this::setAutoOutputItems)
                    .setTooltipText("gregtech.gui.item_auto_output.tooltip")
                    .shouldUseBaseBackground());
            leftButtonStartX += 18;
        }
        if (exportFluids.getTanks() > 0) {
            builder.widget(new ToggleButtonWidget(leftButtonStartX, 62 + yOffset, 18, 18,
                    GuiTextures.BUTTON_FLUID_OUTPUT, this::isAutoOutputFluids, this::setAutoOutputFluids)
                    .setTooltipText("gregtech.gui.fluid_auto_output.tooltip")
                    .shouldUseBaseBackground());
            leftButtonStartX += 18;
        }

        builder.widget(new CycleButtonWidget(leftButtonStartX, 62 + yOffset, 18, 18,
                workable.getAvailableOverclockingTiers(), workable::getOverclockTier, workable::setOverclockTier)
                .setTooltipHoverString("gregtech.gui.overclock.description")
                .setButtonTexture(GuiTextures.BUTTON_OVERCLOCK));

        if (exportItems.getSlots() + exportFluids.getTanks() <= 9) {
            ImageWidget logo = new ImageWidget(152, 63 + yOffset, 17, 17, GTValues.XMAS.get() ? GuiTextures.GREGTECH_LOGO_XMAS : GuiTextures.GREGTECH_LOGO).setIgnoreColor(true);
            SlotWidget circuitSlot = new SlotWidget(circuitInventory, 0, 124, 62 + yOffset, true, true, false)
                    .setBackgroundTexture(GuiTextures.SLOT, getCircuitSlotOverlay());
            builder.widget(getCircuitSlotTooltip(circuitSlot)).widget(logo)
                    .widget(new ClickButtonWidget(115, 62 + yOffset, 9, 9, "", this::circuitConfigPlus)
                            .setShouldClientCallback(true)
                            .setButtonTexture(GuiTextures.BUTTON_INT_CIRCUIT_PLUS)
                            .setDisplayFunction(() -> circuitInventory != null && IntCircuitIngredient.isIntegratedCircuit(circuitInventory.getStackInSlot(0))))
                    .widget(new ClickButtonWidget(115, 71 + yOffset, 9, 9, "", this::circuitConfigMinus)
                            .setShouldClientCallback(true)
                            .setButtonTexture(GuiTextures.BUTTON_INT_CIRCUIT_MINUS)
                            .setDisplayFunction(() -> circuitInventory != null && IntCircuitIngredient.isIntegratedCircuit(circuitInventory.getStackInSlot(0))));
        }
        return builder;
    }

    private void circuitConfigPlus(Widget.ClickData data) {
        ItemStack stack;
        if (circuitInventory != null && IntCircuitIngredient.isIntegratedCircuit(stack = circuitInventory.getStackInSlot(0))) {
            IntCircuitIngredient.adjustConfiguration(stack, data.isShiftClick ? 5 : 1);
            this.notifiedItemInputList.add(circuitInventory);
        }
    }

    private void circuitConfigMinus(Widget.ClickData data) {
        ItemStack stack;
        if (circuitInventory != null && IntCircuitIngredient.isIntegratedCircuit(stack = circuitInventory.getStackInSlot(0))) {
            IntCircuitIngredient.adjustConfiguration(stack, data.isShiftClick ? -5 : -1);
            this.notifiedItemInputList.add(circuitInventory);
        }
    }

    // Method provided to override
    protected TextureArea getCircuitSlotOverlay() {
        return GuiTextures.INT_CIRCUIT_OVERLAY;
    }

    // Method provided to override
    protected SlotWidget getCircuitSlotTooltip(SlotWidget widget) {
        return widget.setTooltipText("gregtech.gui.configurator_slot.tooltip");
    }

    @Override
    protected ModularUI createUI(Player entityPlayer) {
        return createGuiTemplate(entityPlayer).build(getHolder(), entityPlayer);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        String key = this.metaTileEntityId.getPath().split("\\.")[0];
        String mainKey = String.format("gregtech.machine.%s.tooltip", key);
        if (I18n.hasKey(mainKey)) {
            tooltip.add(1, mainKey);
        }
    }

    @Override
    public boolean needsSneakToRotate() {
        return true;
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.tool_action.screwdriver.auto_output_covers"));
        tooltip.add(Component.translatable("gregtech.tool_action.wrench.set_facing"));
        tooltip.add(Component.translatable("gregtech.tool_action.soft_mallet.reset"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }
}
