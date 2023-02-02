package net.nemezanevem.gregtech.common.metatileentities.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.TieredMetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.capability.impl.FluidTankList;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.widgets.FluidContainerSlotWidget;
import net.nemezanevem.gregtech.api.gui.widgets.ImageWidget;
import net.nemezanevem.gregtech.api.gui.widgets.SlotWidget;
import net.nemezanevem.gregtech.api.gui.widgets.TankWidget;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import net.nemezanevem.gregtech.common.ConfigHolder;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static net.nemezanevem.gregtech.api.capability.GregtechDataCodes.PUMP_HEAD_LEVEL;


public class MetaTileEntityPump extends TieredMetaTileEntity {

    private static final Cuboid6 PIPE_CUBOID = new Cuboid6(6 / 16.0, 0.0, 6 / 16.0, 10 / 16.0, 1.0, 10 / 16.0);
    private static final int BASE_PUMP_RANGE = 32;
    private static final int EXTRA_PUMP_RANGE = 8;
    private static final int PUMP_SPEED_BASE = 80;

    private final Deque<BlockPos> fluidSourceBlocks = new ArrayDeque<>();
    private final Deque<BlockPos> blocksToCheck = new ArrayDeque<>();
    private boolean initializedQueue = false;
    private int pumpHeadY;

    public MetaTileEntityPump(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityPump(metaTileEntityId, getTier());
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        ColourMultiplier multiplier = new ColourMultiplier(Util.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()));
        IVertexOperation[] coloredPipeline = ArrayUtils.add(pipeline, multiplier);
        for (Direction renderSide : GTValues.HORIZONTAL_DIRECTION) {
            if (renderSide == getFrontFacing()) {
                Textures.PIPE_OUT_OVERLAY.renderSided(renderSide, renderState, translation, pipeline);
            } else {
                Textures.ADV_PUMP_OVERLAY.renderSided(renderSide, renderState, translation, coloredPipeline);
            }
        }
        Textures.SCREEN.renderSided(Direction.UP, renderState, translation, pipeline);
        Textures.PIPE_IN_OVERLAY.renderSided(Direction.DOWN, renderState, translation, pipeline);
    }

    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        super.writeInitialSyncData(buf);
        buf.writeVarInt(pumpHeadY);
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        super.receiveInitialSyncData(buf);
        this.pumpHeadY = buf.readVarInt();
    }

    @Override
    public void receiveCustomData(int dataId, FriendlyByteBuf buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == PUMP_HEAD_LEVEL) {
            this.pumpHeadY = buf.readVarInt();
            scheduleRenderUpdate();
        }
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(false, new FluidTank(16000 * Math.max(1, getTier())));
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(1);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(1);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        return (side == null || side.getAxis() != Direction.Axis.Y) ? super.getCapability(capability, side) : null;
    }

    @Override
    protected ModularUI createUI(Player entityPlayer) {
        ModularUI.Builder builder = ModularUI.defaultBuilder();
        builder.image(7, 16, 81, 55, GuiTextures.DISPLAY);
        TankWidget tankWidget = new TankWidget(exportFluids.getTankAt(0), 69, 52, 18, 18)
                .setHideTooltip(true).setAlwaysShowFull(true);
        builder.widget(tankWidget);
        builder.label(11, 20, "gregtech.gui.fluid_amount", 0xFFFFFF);
        builder.dynamicLabel(11, 30, tankWidget::getFormattedFluidAmount, 0xFFFFFF);
        builder.dynamicLabel(11, 40, tankWidget::getFluidLocalizedName, 0xFFFFFF);
        return builder.label(6, 6, getMetaFullName())
                .widget(new FluidContainerSlotWidget(importItems, 0, 90, 17, false)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.IN_SLOT_OVERLAY))
                .widget(new ImageWidget(91, 36, 14, 15, GuiTextures.TANK_ICON))
                .widget(new SlotWidget(exportItems, 0, 90, 54, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.OUT_SLOT_OVERLAY))
                .bindPlayerInventory(entityPlayer.getInventory())
                .build(getHolder(), entityPlayer);
    }

    private int getMaxPumpRange() {
        return BASE_PUMP_RANGE + EXTRA_PUMP_RANGE * getTier();
    }

    private boolean isStraightInPumpRange(BlockPos checkPos) {
        BlockPos pos = getPos();
        return checkPos.getX() == pos.getX() &&
                checkPos.getZ() == pos.getZ() &&
                pos.getY() < checkPos.getY() &&
                pos.getY() + pumpHeadY >= checkPos.getY();
    }

    private void updateQueueState(int blocksToCheckAmount) {
        BlockPos selfPos = getPos().below(pumpHeadY);

        for (int i = 0; i < blocksToCheckAmount; i++) {
            BlockPos checkPos = null;
            int amountIterated = 0;
            do {
                if (checkPos != null) {
                    blocksToCheck.push(checkPos);
                    amountIterated++;
                }
                checkPos = blocksToCheck.poll();

            } while (checkPos != null &&
                    !getWorld().isLoaded(checkPos) &&
                    amountIterated < blocksToCheck.size());
            if (checkPos != null) {
                checkFluidBlockAt(selfPos, checkPos);
            } else break;
        }

        if (fluidSourceBlocks.isEmpty()) {
            if (getOffsetTimer() % 20 == 0) {
                BlockPos downPos = selfPos.below(1);
                if (downPos.getY() >= 0) {
                    BlockState downBlock = getWorld().getBlockState(downPos);
                    if (downBlock.getBlock() instanceof LiquidBlock ||
                            downBlock.getBlock() instanceof IFluidBlock ||
                            downBlock.skipRendering(getWorld().getBlockState(downPos), Direction.UP)) {
                        this.pumpHeadY++;
                    }
                }

                // Always recheck next time
                writeCustomData(PUMP_HEAD_LEVEL, b -> b.writeVarInt(pumpHeadY));
                markDirty();
                //schedule queue rebuild because we changed our position and no fluid is available
                this.initializedQueue = false;
            }

            if (!initializedQueue || getOffsetTimer() % 6000 == 0 || isFirstTick()) {
                this.initializedQueue = true;
                //just add ourselves to check list and see how this will go
                this.blocksToCheck.add(selfPos);
            }
        }
    }

    private void checkFluidBlockAt(BlockPos pumpHeadPos, BlockPos checkPos) {
        BlockState blockHere = getWorld().getBlockState(checkPos);
        boolean shouldCheckNeighbours = isStraightInPumpRange(checkPos);

        if (blockHere.getBlock() instanceof LiquidBlock ||
                blockHere.getBlock() instanceof IFluidBlock) {
            LazyOptional<IFluidHandler> fluidHandler = FluidUtil.getFluidHandler(getWorld(), checkPos, null);
            if (!fluidHandler.isPresent()) {
                return;
            }
            FluidStack drainStack = fluidHandler.resolve().get().drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
            if (drainStack != FluidStack.EMPTY && drainStack.getAmount() > 0) {
                this.fluidSourceBlocks.add(checkPos);
            }
            shouldCheckNeighbours = true;
        }

        if (shouldCheckNeighbours) {
            int maxPumpRange = getMaxPumpRange();
            for (Direction facing : Direction.values()) {
                BlockPos offsetPos = checkPos.offset(facing.getNormal());
                if (offsetPos.distSqr(pumpHeadPos) > maxPumpRange * maxPumpRange)
                    continue; //do not add blocks outside bounds
                if (!fluidSourceBlocks.contains(offsetPos) &&
                        !blocksToCheck.contains(offsetPos)) {
                    this.blocksToCheck.add(offsetPos);
                }
            }
        }
    }

    private void tryPumpFirstBlock() {
        BlockPos fluidBlockPos = fluidSourceBlocks.poll();
        if (fluidBlockPos == null) return;
        BlockState blockHere = getWorld().getBlockState(fluidBlockPos);
        if (blockHere.getBlock() instanceof LiquidBlock ||
                blockHere.getBlock() instanceof IFluidBlock) {
            LazyOptional<IFluidHandler> fluidHandler = FluidUtil.getFluidHandler(getWorld(), fluidBlockPos, null);
            if (!fluidHandler.isPresent()) {
                return;
            }
            FluidStack drainStack = fluidHandler.resolve().get().drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
            if (drainStack != FluidStack.EMPTY && exportFluids.fill(drainStack, IFluidHandler.FluidAction.SIMULATE) == drainStack.getAmount()) {
                exportFluids.fill(drainStack, IFluidHandler.FluidAction.EXECUTE);
                fluidHandler.resolve().get().drain(drainStack.getAmount(), IFluidHandler.FluidAction.EXECUTE);
                this.fluidSourceBlocks.remove(fluidBlockPos);
                energyContainer.changeEnergy(-GTValues.V[getTier()] * 2);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClientSide) {
            return;
        }
        pushFluidsIntoNearbyHandlers(getFrontFacing());
        fillContainerFromInternalTank();

        //do not do anything without enough energy supplied
        if (energyContainer.getEnergyStored() < GTValues.V[getTier()] * 2) {
            return;
        }
        updateQueueState(getTier());
        if (getOffsetTimer() % getPumpingCycleLength() == 0 && !fluidSourceBlocks.isEmpty()) {
            tryPumpFirstBlock();
        }
    }

    private int getPumpingCycleLength() {
        return PUMP_SPEED_BASE / (1 << (getTier() - 1));
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.putInt("PumpHeadDepth", pumpHeadY);
        return data;
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        this.pumpHeadY = data.getInt("PumpHeadDepth");
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level player, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.machine.pump.tooltip"));
        if (ConfigHolder.machines.doTerrainExplosion)
            tooltip.add(Component.translatable("gregtech.universal.tooltip.terrain_resist"));
        tooltip.add(Component.literal(Component.translatable("gregtech.universal.tooltip.uses_per_op", GTValues.V[getTier()] * 2).getString()
                + ChatFormatting.GRAY + ", " + Component.translatable("gregtech.machine.pump.tooltip_buckets", getPumpingCycleLength()).getString()));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.voltage_in", energyContainer.getInputVoltage(), GTValues.VNF[getTier()]));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.fluid_storage_capacity", exportFluids.getTankAt(0).getCapacity()));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.working_area", getMaxPumpRange(), getMaxPumpRange()));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable Level world, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(Component.translatable("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }
}
