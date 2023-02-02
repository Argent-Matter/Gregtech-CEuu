package net.nemezanevem.gregtech.common.metatileentities.electric;

import codechicken.lib.raytracer.VoxelShapeBlockHitResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.blockentity.IDataInfoProvider;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.TieredMetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.capability.GregtechTileCapabilities;
import net.nemezanevem.gregtech.api.capability.IControllable;
import net.nemezanevem.gregtech.api.capability.IMiner;
import net.nemezanevem.gregtech.api.capability.impl.EnergyContainerHandler;
import net.nemezanevem.gregtech.api.capability.impl.NotifiableItemStackHandler;
import net.nemezanevem.gregtech.api.capability.impl.miner.MinerLogic;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.widgets.AdvancedTextWidget;
import net.nemezanevem.gregtech.api.gui.widgets.SlotWidget;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MetaTileEntityMiner extends TieredMetaTileEntity implements IMiner, IControllable, IDataInfoProvider {

    private final ItemStackHandler chargerInventory;

    private final int inventorySize;
    private final long energyPerTick;
    private boolean isInventoryFull = false;

    private final MinerLogic minerLogic;

    public MetaTileEntityMiner(ResourceLocation metaTileEntityId, int tier, int speed, int maximumRadius, int fortune) {
        super(metaTileEntityId, tier);
        this.inventorySize = (tier + 1) * (tier + 1);
        this.energyPerTick = GTValues.V[tier - 1];
        this.minerLogic = new MinerLogic(this, fortune, speed, maximumRadius, Textures.SOLID_STEEL_CASING);
        this.chargerInventory = new ItemStackHandler(1);
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMiner(metaTileEntityId, getTier(), this.minerLogic.getSpeed(), this.minerLogic.getMaximumRadius(), this.minerLogic.getFortune());
    }

    @Override
    protected void reinitializeEnergyContainer() {
        super.reinitializeEnergyContainer();
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(0, this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(inventorySize, this, true);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.SCREEN.renderSided(Direction.UP, renderState, translation, pipeline);
        for (Direction renderSide : GTValues.HORIZONTAL_DIRECTION) {
            if (renderSide == getFrontFacing()) {
                Textures.PIPE_OUT_OVERLAY.renderSided(renderSide, renderState, translation, pipeline);
            } else
                Textures.CHUNK_MINER_OVERLAY.renderSided(renderSide, renderState, translation, pipeline);
        }
        minerLogic.renderPipe(renderState, translation, pipeline);
    }

    @Override
    protected ModularUI createUI(@Nonnull Player entityPlayer) {
        int rowSize = (int) Math.sqrt(inventorySize);
        ModularUI.Builder builder = new ModularUI.Builder(GuiTextures.BACKGROUND, 195, 176);
        builder.bindPlayerInventory(entityPlayer.getInventory(), 94);

        if (getTier() == GTValues.HV) {
            for (int y = 0; y < rowSize; y++) {
                for (int x = 0; x < rowSize; x++) {
                    int index = y * rowSize + x;
                    builder.widget(new SlotWidget(exportItems, index, 151 - rowSize * 9 + x * 18, 18 + y * 18, true, false)
                            .setBackgroundTexture(GuiTextures.SLOT));
                }
            }
        } else {
            for (int y = 0; y < rowSize; y++) {
                for (int x = 0; x < rowSize; x++) {
                    int index = y * rowSize + x;
                    builder.widget(new SlotWidget(exportItems, index, 142 - rowSize * 9 + x * 18, 18 + y * 18, true, false)
                            .setBackgroundTexture(GuiTextures.SLOT));
                }
            }
        }

        builder.image(7, 16, 105, 75, GuiTextures.DISPLAY)
                .label(6, 6, getMetaFullName());
        builder.widget(new AdvancedTextWidget(10, 19, this::addDisplayText, 0xFFFFFF)
                .setMaxWidthLimit(84));
        builder.widget(new AdvancedTextWidget(70, 19, this::addDisplayText2, 0xFFFFFF)
                .setMaxWidthLimit(84));
        builder.widget(new SlotWidget(chargerInventory, 0, 171, 152)
                .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.CHARGER_OVERLAY));

        return builder.build(getHolder(), entityPlayer);
    }

    private void addDisplayText(@Nonnull List<Component> textList) {
        textList.add(Component.translatable("gregtech.machine.miner.startx", this.minerLogic.getX().get()));
        textList.add(Component.translatable("gregtech.machine.miner.starty", this.minerLogic.getY().get()));
        textList.add(Component.translatable("gregtech.machine.miner.startz", this.minerLogic.getZ().get()));
        textList.add(Component.translatable("gregtech.machine.miner.radius", this.minerLogic.getCurrentRadius()));
        if (this.minerLogic.isDone())
            textList.add(Component.translatable("gregtech.multiblock.large_miner.done").withStyle(ChatFormatting.GREEN));
        else if (this.minerLogic.isWorking())
            textList.add(Component.translatable("gregtech.multiblock.large_miner.working").withStyle(ChatFormatting.GOLD));
        else if (!this.isWorkingEnabled())
            textList.add(Component.translatable("gregtech.multiblock.work_paused"));
        if (isInventoryFull)
            textList.add(Component.translatable("gregtech.multiblock.large_miner.invfull").withStyle(ChatFormatting.RED));
        if (!drainEnergy(true))
            textList.add(Component.translatable("gregtech.multiblock.large_miner.needspower").withStyle(ChatFormatting.RED));
    }

    private void addDisplayText2(@Nonnull List<Component> textList) {
        textList.add(Component.translatable("gregtech.machine.miner.minex", this.minerLogic.getMineX().get()));
        textList.add(Component.translatable("gregtech.machine.miner.miney", this.minerLogic.getMineY().get()));
        textList.add(Component.translatable("gregtech.machine.miner.minez", this.minerLogic.getMineZ().get()));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level player, @Nonnull List<Component> tooltip, boolean advanced) {
        int currentArea = getWorkingArea(minerLogic.getCurrentRadius());
        tooltip.add(Component.translatable("gregtech.machine.miner.tooltip", currentArea, currentArea));
        tooltip.add(Component.literal(Component.translatable("gregtech.universal.tooltip.uses_per_tick", energyPerTick).getString()
                + ChatFormatting.GRAY + ", " + Component.translatable("gregtech.machine.miner.per_block", this.minerLogic.getSpeed() / 20).getString()));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.voltage_in", energyContainer.getInputVoltage(), GTValues.VNF[getTier()]));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        int maxArea = getWorkingArea(minerLogic.getMaximumRadius());
        tooltip.add(Component.translatable("gregtech.universal.tooltip.working_area_max", maxArea, maxArea));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable Level world, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.tool_action.screwdriver.toggle_mode_covers"));
        tooltip.add(Component.translatable("gregtech.tool_action.wrench.set_facing"));
        tooltip.add(Component.translatable("gregtech.tool_action.soft_mallet.reset"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public boolean drainEnergy(boolean simulate) {
        long resultEnergy = energyContainer.getEnergyStored() - energyPerTick;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.removeEnergy(energyPerTick);
            return true;
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        this.minerLogic.performMining();
        if (!getWorld().isClientSide) {
            ((EnergyContainerHandler) this.energyContainer).dischargeOrRechargeEnergyContainers(chargerInventory, 0);

            if (getOffsetTimer() % 5 == 0)
                pushItemsIntoNearbyHandlers(getFrontFacing());

            if (this.minerLogic.wasActiveAndNeedsUpdate()) {
                this.minerLogic.setWasActiveAndNeedsUpdate(false);
                this.minerLogic.setActive(false);
            }
        }
    }

    @Override
    public boolean onScrewdriverClick(Player playerIn, InteractionHand hand, Direction facing, VoxelShapeBlockHitResult hitResult) {
        if (getWorld().isClientSide)
            return true;

        if (!this.minerLogic.isActive()) {
            int currentRadius = this.minerLogic.getCurrentRadius();
            if (currentRadius == 1)
                this.minerLogic.setCurrentRadius(this.minerLogic.getMaximumRadius());
            else if (playerIn.isCrouching())
                this.minerLogic.setCurrentRadius(Math.max(1, Math.round(currentRadius / 2.0f)));
            else
                this.minerLogic.setCurrentRadius(Math.max(1, currentRadius - 1));

            this.minerLogic.resetArea();

            playerIn.sendSystemMessage(Component.translatable("gregtech.multiblock.large_miner.radius", this.minerLogic.getCurrentRadius()));
        } else {
            playerIn.sendSystemMessage(Component.translatable("gregtech.multiblock.large_miner.errorradius"));
        }
        return true;
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.put("ChargerInventory", chargerInventory.serializeNBT());
        return this.minerLogic.writeToNBT(data);
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        this.chargerInventory.deserializeNBT(data.getCompound("ChargerInventory"));
        this.minerLogic.readFromNBT(data);
    }

    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        super.writeInitialSyncData(buf);
        this.minerLogic.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        super.receiveInitialSyncData(buf);
        this.minerLogic.receiveInitialSyncData(buf);
    }

    @Override
    public void receiveCustomData(int dataId, FriendlyByteBuf buf) {
        super.receiveCustomData(dataId, buf);
        this.minerLogic.receiveCustomData(dataId, buf);
    }

    LazyOptional<IControllable> controllableLazy = LazyOptional.of(() -> this);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return controllableLazy.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
        clearInventory(itemBuffer, chargerInventory);
    }

    @Override
    public boolean isInventoryFull() {
        return isInventoryFull;
    }

    @Override
    public void setInventoryFull(boolean isFull) {
        this.isInventoryFull = isFull;
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.minerLogic.isWorkingEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.minerLogic.setWorkingEnabled(isActivationAllowed);
    }

    @Override
    public SoundEvent getSound() {
        return GTSoundEvents.MINER;
    }

    @Override
    public boolean isActive() {
        return minerLogic.isActive() && isWorkingEnabled();
    }

    @Nonnull
    @Override
    public List<Component> getDataInfo() {
        return Collections.singletonList(Component.translatable("gregtech.multiblock.large_miner.radius", this.minerLogic.getCurrentRadius()));
    }
}
