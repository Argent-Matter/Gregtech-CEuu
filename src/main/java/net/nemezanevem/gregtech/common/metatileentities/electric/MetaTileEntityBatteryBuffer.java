package net.nemezanevem.gregtech.common.metatileentities.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.blockentity.IDataInfoProvider;
import net.nemezanevem.gregtech.api.blockentity.MTETrait;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.TieredMetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.capability.GregtechCapabilities;
import net.nemezanevem.gregtech.api.capability.GregtechTileCapabilities;
import net.nemezanevem.gregtech.api.capability.IControllable;
import net.nemezanevem.gregtech.api.capability.IElectricItem;
import net.nemezanevem.gregtech.api.capability.impl.EnergyContainerBatteryBuffer;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.widgets.SlotWidget;
import net.nemezanevem.gregtech.api.util.PipelineUtil;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import net.nemezanevem.gregtech.common.ConfigHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityBatteryBuffer extends TieredMetaTileEntity implements IControllable, IDataInfoProvider {

    private final int inventorySize;
    private boolean allowEnergyOutput = true;

    public MetaTileEntityBatteryBuffer(ResourceLocation metaTileEntityId, int tier, int inventorySize) {
        super(metaTileEntityId, tier);
        this.inventorySize = inventorySize;
        initializeInventory();
        reinitializeEnergyContainer();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityBatteryBuffer(metaTileEntityId, getTier(), inventorySize);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.ENERGY_OUT.renderSided(getFrontFacing(), renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[getTier()]));
    }

    @Override
    protected void reinitializeEnergyContainer() {
        this.energyContainer = new EnergyContainerBatteryBuffer(this, getTier(), inventorySize);
    }

    private LazyOptional<IControllable> lazy = LazyOptional.of(() -> this);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return lazy.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isWorkingEnabled() {
        return allowEnergyOutput;
    }

    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        return !(trait instanceof EnergyContainerBatteryBuffer) || allowEnergyOutput;
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.allowEnergyOutput = isActivationAllowed;
        notifyBlockUpdate();
    }

    @Override
    public boolean isValidFrontFacing(Direction facing) {
        return true;
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(inventorySize) {
            @Override
            protected void onContentsChanged(int slot) {
                ((EnergyContainerBatteryBuffer) energyContainer).notifyEnergyListener(false);
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                LazyOptional<IElectricItem> electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                if ((electricItem != null && getTier() >= electricItem.resolve().get().getTier()) ||
                        (ConfigHolder.compat.energy.nativeEUToFE && stack.getCapability(ForgeCapabilities.ENERGY, null).isPresent())) {
                    return super.insertItem(slot, stack, simulate);
                }
                return stack;
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(0);
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.itemInventory = importItems;
    }

    @Override
    protected ModularUI createUI(Player entityPlayer) {
        int rowSize = (int) Math.sqrt(inventorySize);
        int colSize = rowSize;
        if (inventorySize == 8) {
            rowSize = 4;
            colSize = 2;
        }
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176,
                        18 + 18 * colSize + 94)
                .label(6, 6, getMetaFullName());

        int index = 0;
        for (int y = 0; y < colSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                builder.widget(new SlotWidget(importItems, index++, 88 - rowSize * 9 + x * 18, 18 + y * 18, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.BATTERY_OVERLAY));
            }
        }

        builder.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 18 + 18 * colSize + 12);
        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level player, List<Component> tooltip, boolean advanced) {
        String tierName = GTValues.VNF[getTier()];

        tooltip.add(Component.translatable("gregtech.universal.tooltip.item_storage_capacity", inventorySize));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.voltage_in_out", energyContainer.getInputVoltage(), tierName));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.amperage_in_till", energyContainer.getInputAmperage()));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.amperage_out_till", energyContainer.getOutputAmperage()));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable Level world, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(Component.translatable("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag data) {
        CompoundTag tagCompound = super.writeToNBT(data);
        tagCompound.putBoolean("AllowEnergyOutput", allowEnergyOutput);
        return tagCompound;
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        if (data.contains("AllowEnergyOutput", Tag.TAG_ANY_NUMERIC)) {
            this.allowEnergyOutput = data.getBoolean("AllowEnergyOutput");
        }
    }

    @Nonnull
    @Override
    public List<Component> getDataInfo() {
        List<Component> list = new ArrayList<>();
        list.add(Component.translatable("gregtech.battery_buffer.average_input",
                Component.translatable(Util.formatNumbers(energyContainer.getInputPerSec() / 20)).withStyle(ChatFormatting.YELLOW)));
        list.add(Component.translatable("gregtech.battery_buffer.average_output",
                Component.translatable(Util.formatNumbers(energyContainer.getOutputPerSec() / 20)).withStyle(ChatFormatting.YELLOW)));
        return list;
    }
}
