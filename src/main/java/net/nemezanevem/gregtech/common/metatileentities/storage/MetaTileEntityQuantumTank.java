package net.nemezanevem.gregtech.common.metatileentities.storage;

import codechicken.lib.raytracer.VoxelShapeBlockHitResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IActiveOutputSide;
import gregtech.api.capability.impl.*;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.Util;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.core.Direction;
import net.minecraft.entity.player.Player;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Direction;
import net.minecraft.util.InteractionHand;
import net.minecraft.util.Mth;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Mth;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.nemezanevem.gregtech.api.blockentity.ITieredMetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.capability.IActiveOutputSide;
import net.nemezanevem.gregtech.api.capability.impl.FilteredItemHandler;
import net.nemezanevem.gregtech.api.capability.impl.FluidHandlerProxy;
import net.nemezanevem.gregtech.api.capability.impl.FluidTankList;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.widgets.*;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_AUTO_OUTPUT_FLUIDS;
import static gregtech.api.capability.GregtechDataCodes.UPDATE_OUTPUT_FACING;
import static net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack.FLUID_NBT_KEY;
import static net.nemezanevem.gregtech.api.capability.GregtechDataCodes.UPDATE_AUTO_OUTPUT_FLUIDS;

public class MetaTileEntityQuantumTank extends MetaTileEntity implements ITieredMetaTileEntity, IActiveOutputSide {

    // This field (ranging from 1 to 99) is the percentage filled
    // at which the Partial Void feature will start voiding Fluids.
    private final int VOID_PERCENT = 95;

    private final int tier;
    private final int maxFluidCapacity;
    private final int maxPartialFluidCapacity;
    private FluidTank fluidTank;
    private boolean autoOutputFluids;
    private Direction outputFacing;
    private boolean allowInputFromOutputSide = false;
    protected IFluidHandler outputFluidInventory;

    private boolean isLocked;
    private boolean isVoiding;
    private boolean isPartialVoiding;
    private FluidTank lockedFluid;

    public MetaTileEntityQuantumTank(ResourceLocation metaTileEntityId, int tier, int maxFluidCapacity) {
        super(metaTileEntityId);
        this.tier = tier;
        this.maxFluidCapacity = maxFluidCapacity;
        this.maxPartialFluidCapacity = (int) Math.round(maxFluidCapacity * (VOID_PERCENT / 100.0));
        initializeInventory();
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.lockedFluid = new FluidTank(1);
        this.fluidTank = new FluidTank(maxFluidCapacity).setValidator(fs -> lockedFluid.getFluid().isEmpty() || fs.isFluidEqual(lockedFluid.getFluid()));
        this.fluidInventory = fluidTank;
        this.importFluids = new FluidTankList(false, fluidTank);
        this.exportFluids = new FluidTankList(false, fluidTank);
        this.outputFluidInventory = new FluidHandlerProxy(new FluidTankList(false), exportFluids);
    }

    @Override
    public int getActualComparatorValue() {
        FluidTank fluidTank = this.fluidTank;
        int fluidAmount = fluidTank.getFluidAmount();
        int maxCapacity = fluidTank.getCapacity();
        float f = fluidAmount / (maxCapacity * 1.0f);
        return Mth.floor(f * 14.0f) + (fluidAmount > 0 ? 1 : 0);
    }

    @Override
    public void tick() {
        super.tick();
        Direction currentOutputFacing = getOutputFacing();
        if (!getWorld().isClientSide) {
            if (isVoiding) {
                fluidTank.setFluid(null);
            } else if (isPartialVoiding && fluidTank.getFluid() != null) {
                if (fluidTank.getFluidAmount() > maxPartialFluidCapacity) {
                    fluidTank.setFluid(Util.copyAmount(maxPartialFluidCapacity, fluidTank.getFluid()));
                }
            }
            if (isLocked && lockedFluid.getFluid() == null && fluidTank.getFluid() != null) {
                this.lockedFluid.setFluid(Util.copyAmount(0, fluidTank.getFluid()));
            }
            if (lockedFluid.getFluid() != null && !isLocked) {
                setLocked(true);
            }
            fillContainerFromInternalTank();
            fillInternalTankFromFluidContainer();
            if (isAutoOutputFluids()) {
                pushFluidsIntoNearbyHandlers(currentOutputFacing);
            }
        }
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.put("FluidInventory", fluidTank.writeToNBT(new CompoundTag()));
        data.putBoolean("AutoOutputFluids", autoOutputFluids);
        data.putInt("OutputFacing", getOutputFacing().ordinal());
        data.putBoolean("IsVoiding", isVoiding);
        data.putBoolean("IsPartialVoiding", isPartialVoiding);
        data.putBoolean("IsLocked", isLocked);
        data.put("LockedFluid", lockedFluid.writeToNBT(new CompoundTag()));
        data.putBoolean("AllowInputFromOutputSideF", allowInputFromOutputSide);
        return data;
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        if (data.contains("ContainerInventory")) {
            legacyTankItemHandlerNBTReading(this, data.getCompound("ContainerInventory"), 0, 1);
        }
        this.fluidTank.readFromNBT(data.getCompound("FluidInventory"));
        this.autoOutputFluids = data.getBoolean("AutoOutputFluids");
        this.outputFacing = Direction.values()[data.getInt("OutputFacing")];
        this.isVoiding = data.getBoolean("IsVoiding");
        this.isPartialVoiding = data.getBoolean("IsPartialVoiding");
        this.isLocked = data.getBoolean("IsLocked");
        this.lockedFluid.readFromNBT(data.getCompound("LockedFluid"));
        this.allowInputFromOutputSide = data.getBoolean("AllowInputFromOutputSideF");
    }

    public static void legacyTankItemHandlerNBTReading(MetaTileEntity mte, CompoundTag nbt, int inputSlot, int outputSlot) {
        if (mte == null || nbt == null) {
            return;
        }
        ListTag items = nbt.getList("Items", Tag.TAG_COMPOUND);
        if (mte.getExportItems().getSlots() < 1 || mte.getImportItems().getSlots() < 1 || inputSlot < 0 || outputSlot < 0 || inputSlot == outputSlot) {
            return;
        }
        for (int i = 0; i < items.size(); ++i) {
            CompoundTag itemTags = items.getCompound(i);
            int slot = itemTags.getInt("Slot");
            if (slot == inputSlot) {
                mte.getImportItems().setStackInSlot(0, ItemStack.of(itemTags));
            } else if (slot == outputSlot) {
                mte.getExportItems().setStackInSlot(0, ItemStack.of(itemTags));
            }
        }
    }

    @Override
    public void initFromItemStackData(CompoundTag itemStack) {
        super.initFromItemStackData(itemStack);
        if (itemStack.contains(FLUID_NBT_KEY, Tag.TAG_COMPOUND)) {
            fluidTank.setFluid(FluidStack.loadFluidStackFromNBT(itemStack.getCompound(FLUID_NBT_KEY)));
        }
        if (itemStack.contains("IsVoiding")) {
            setVoiding(true);
        }
        else if (itemStack.contains("IsPartialVoiding")) {
            setPartialVoid(true);
        }

        if (itemStack.contains("LockedFluid")) {
            setLocked(true);

            // Additional check here because locked fluid and void all mode will not properly update locked fluid, due to there being no fluid
            if (this.lockedFluid.getFluid() == null) {
                lockedFluid.setFluid(FluidStack.loadFluidStackFromNBT(itemStack.getCompound("LockedFluid")));
            }
        }
    }

    @Override
    public void writeItemStackData(CompoundTag itemStack) {
        super.writeItemStackData(itemStack);
        FluidStack stack = fluidTank.getFluid();
        if (stack != null && stack.getAmount() > 0) {
            itemStack.put(FLUID_NBT_KEY, stack.writeToNBT(new CompoundTag()));
        }

        if (this.isVoiding) {
            itemStack.putBoolean("IsVoiding", this.isVoiding);
        }
        else if (this.isPartialVoiding) {
            itemStack.putBoolean("IsPartialVoiding", this.isPartialVoiding);
        }

        if (this.isLocked && this.lockedFluid != null) {
            itemStack.put("LockedFluid", lockedFluid.writeToNBT(new CompoundTag()));
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityQuantumTank(metaTileEntityId, tier, maxFluidCapacity);
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(false, fluidTank);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(false, fluidTank);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new FilteredItemHandler(1).setFillPredicate(FilteredItemHandler.getCapabilityFilter(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY));
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(1);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Textures.VOLTAGE_CASINGS[tier].render(renderState, translation, ArrayUtils.add(pipeline,
                new ColourMultiplier(Util.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()))));
        Textures.QUANTUM_TANK_OVERLAY.renderSided(Direction.UP, renderState, translation, pipeline);
        if (outputFacing != null) {
            Textures.PIPE_OUT_OVERLAY.renderSided(outputFacing, renderState, translation, pipeline);
            if (isAutoOutputFluids()) {
                Textures.FLUID_OUTPUT_OVERLAY.renderSided(outputFacing, renderState, translation, pipeline);
            }
        }
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.VOLTAGE_CASINGS[tier].getParticleSprite(), getPaintingColorForRendering());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level player, List<Component> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(Component.translatable("gregtech.machine.quantum_tank.tooltip"));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.fluid_storage_capacity", maxFluidCapacity));
        CompoundTag compound = stack.getTag();
        if (compound != null && compound.contains(FLUID_NBT_KEY, Tag.TAG_COMPOUND)) {
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(compound.getCompound(FLUID_NBT_KEY));
            if (fluidStack != null) {
                tooltip.add(Component.translatable("gregtech.universal.tooltip.fluid_stored", fluidStack.getDisplayName(), fluidStack.getAmount()));
            }
        }
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable Level world, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.tool_action.screwdriver.auto_output_covers"));
        tooltip.add(Component.translatable("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    protected ModularUI createUI(Player entityPlayer) {
        TankWidget tankWidget = new PhantomTankWidget(fluidTank, 69, 43, 18, 18, lockedFluid)
                .setAlwaysShowFull(true).setDrawHoveringText(false);

        return ModularUI.defaultBuilder()
                .widget(new ImageWidget(7, 16, 81, 46, GuiTextures.DISPLAY))
                .widget(new LabelWidget(11, 20, "gregtech.gui.fluid_amount", 0xFFFFFF))
                .widget(tankWidget)
                .dynamicLabel(11, 30, tankWidget::getFormattedFluidAmount, 0xFFFFFF)
                .dynamicLabel(11, 40, tankWidget::getFluidLocalizedName, 0xFFFFFF)
                .label(6, 6, getMetaFullName())
                .widget(new FluidContainerSlotWidget(importItems, 0, 90, 17, false)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.IN_SLOT_OVERLAY))
                .widget(new SlotWidget(exportItems, 0, 90, 44, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.OUT_SLOT_OVERLAY))
                .widget(new ToggleButtonWidget(7, 64, 18, 18,
                        GuiTextures.BUTTON_FLUID_OUTPUT, this::isAutoOutputFluids, this::setAutoOutputFluids)
                        .setTooltipText("gregtech.gui.fluid_auto_output.tooltip")
                        .shouldUseBaseBackground())
                .widget(new ToggleButtonWidget(25, 64, 18, 18,
                        GuiTextures.BUTTON_LOCK, this::isLocked, this::setLocked)
                        .setTooltipText("gregtech.gui.fluid_lock.tooltip")
                        .shouldUseBaseBackground())
                .widget(new ToggleButtonWidget(43, 64, 18, 18,
                        GuiTextures.BUTTON_VOID_PARTIAL, this::isPartialVoid, this::setPartialVoid)
                        .setTooltipText("gregtech.gui.fluid_voiding_partial.tooltip", VOID_PERCENT)
                        .shouldUseBaseBackground())
                .widget(new ToggleButtonWidget(61, 64, 18, 18,
                        GuiTextures.BUTTON_VOID, this::isVoiding, this::setVoiding)
                        .setTooltipText("gregtech.gui.fluid_voiding_all.tooltip")
                        .shouldUseBaseBackground())
                .bindPlayerInventory(entityPlayer.getInventory())
                .build(getHolder(), entityPlayer);
    }

    public Direction getOutputFacing() {
        return outputFacing == null ? frontFacing.getOpposite() : outputFacing;
    }

    @Override
    public void setFrontFacing(Direction frontFacing) {
        super.setFrontFacing(Direction.UP);
        if (this.outputFacing == null) {
            //set initial output facing as opposite to front
            setOutputFacing(frontFacing.getOpposite());
        }
    }

    @Override
    public boolean isAutoOutputItems() {
        return false;
    }

    public boolean isAutoOutputFluids() {
        return autoOutputFluids;
    }

    @Override
    public boolean isAllowInputFromOutputSideItems() {
        return false;
    }

    @Override
    public boolean isAllowInputFromOutputSideFluids() {
        return allowInputFromOutputSide;
    }

    @Override
    public void receiveCustomData(int dataId, FriendlyByteBuf buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_OUTPUT_FACING) {
            this.outputFacing = Direction.VALUES[buf.readByte()];
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_AUTO_OUTPUT_FLUIDS) {
            this.autoOutputFluids = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @Override
    public boolean isValidFrontFacing(Direction facing) {
        return super.isValidFrontFacing(facing) && facing != outputFacing;
    }

    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(getOutputFacing().getIndex());
        buf.writeBoolean(autoOutputFluids);
        buf.writeBoolean(isLocked);
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        super.receiveInitialSyncData(buf);
        this.outputFacing = Direction.VALUES[buf.readByte()];
        this.autoOutputFluids = buf.readBoolean();
        this.isLocked = buf.readBoolean();
    }

    public void setOutputFacing(Direction outputFacing) {
        this.outputFacing = outputFacing;
        if (!getWorld().isClientSide) {
            notifyBlockUpdate();
            writeCustomData(UPDATE_OUTPUT_FACING, buf -> buf.writeByte(outputFacing.getIndex()));
            markDirty();
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_ACTIVE_OUTPUT_SIDE) {
            if (side == getOutputFacing()) {
                return GregtechTileCapabilities.CAPABILITY_ACTIVE_OUTPUT_SIDE.cast(this);
            }
            return null;
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            IFluidHandler fluidHandler = (side == getOutputFacing() && !isAllowInputFromOutputSideFluids()) ? outputFluidInventory : fluidInventory;
            if (fluidHandler.getTankProperties().length > 0) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);
            }

            return null;
        }
        return super.getCapability(capability, side);
    }

    @Override
    public ICapabilityProvider initItemStackCapabilities(ItemStack itemStack) {
        return new ThermalFluidHandlerItemStack(itemStack, maxFluidCapacity, Integer.MAX_VALUE, true, true, true, true);
    }

    @Override
    public boolean onWrenchClick(Player playerIn, InteractionHand hand, Direction facing, VoxelShapeBlockHitResult hitResult) {
        if (!playerIn.isSneaking()) {
            if (getOutputFacing() == facing || getFrontFacing() == facing) {
                return false;
            }
            if (!getWorld().isClientSide) {
                setOutputFacing(facing);
            }
            return true;
        }
        return super.onWrenchClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public boolean onScrewdriverClick(Player playerIn, InteractionHand hand, Direction facing, VoxelShapeBlockHitResult hitResult) {
        Direction hitFacing = ICoverable.determineGridSideHit(hitResult);
        if (facing == getOutputFacing() || (hitFacing == getOutputFacing() && playerIn.isSneaking())) {
            if (!getWorld().isClientSide) {
                if (isAllowInputFromOutputSideFluids()) {
                    setAllowInputFromOutputSide(false);
                    playerIn.sendSystemMessage(Component.translatable("gregtech.machine.basic.input_from_output_side.disallow"));
                } else {
                    setAllowInputFromOutputSide(true);
                    playerIn.sendSystemMessage(Component.translatable("gregtech.machine.basic.input_from_output_side.allow"));
                }
            }
            return true;
        }
        return super.onScrewdriverClick(playerIn, hand, facing, hitResult);
    }

    public void setAllowInputFromOutputSide(boolean allowInputFromOutputSide) {
        this.allowInputFromOutputSide = allowInputFromOutputSide;
        if (!getWorld().isClientSide) {
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

    private boolean isLocked() {
        return isLocked;
    }

    private void setLocked(boolean locked) {
        this.isLocked = locked;
        if (locked && fluidTank.getFluid() != null) {
            this.lockedFluid.setFluid(Util.copyAmount(1, fluidTank.getFluid()));
        }
        if (!locked && lockedFluid.getFluid() != null) {
            this.lockedFluid.setFluid(null);
        }
        if (!getWorld().isClientSide) {
            markDirty();
        }
    }

    private boolean isVoiding() {
        return isVoiding;
    }

    private void setVoiding(boolean isVoiding) {
        this.isVoiding = isVoiding;
        if (isVoiding && isPartialVoiding) {
            this.isPartialVoiding = false;
        }
        if (!getWorld().isClientSide) {
            markDirty();
        }
    }

    private boolean isPartialVoid() {
        return isPartialVoiding;
    }

    private void setPartialVoid(boolean isPartialVoid) {
        this.isPartialVoiding = isPartialVoid;
        if (isPartialVoid && isVoiding) {
            this.isVoiding = false;
        }
        if (!getWorld().isClientSide) {
            markDirty();
        }
    }

    @Override
    public boolean needsSneakToRotate() {
        return true;
    }
}
