package net.nemezanevem.gregtech.common.metatileentities.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.TieredMetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.widgets.ClickButtonWidget;
import net.nemezanevem.gregtech.api.gui.widgets.ImageWidget;
import net.nemezanevem.gregtech.api.gui.widgets.SimpleTextWidget;
import net.nemezanevem.gregtech.api.gui.widgets.SlotWidget;
import net.nemezanevem.gregtech.api.util.GTTransferUtils;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import net.nemezanevem.gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import net.nemezanevem.gregtech.common.covers.filter.ItemFilterContainer;

import javax.annotation.Nullable;
import java.util.List;

import static net.nemezanevem.gregtech.api.capability.GregtechDataCodes.IS_WORKING;

public class MetaTileEntityItemCollector extends TieredMetaTileEntity {

    private static final int[] INVENTORY_SIZES = {4, 9, 16, 25, 25};
    private static final double MOTION_MULTIPLIER = 0.04;
    private static final int BASE_EU_CONSUMPTION = 6;

    private final int maxItemSuckingRange;
    private int itemSuckingRange;
    private AABB areaBoundingBox;
    private BlockPos areaCenterPos;
    private boolean isWorking;
    private final ItemFilterContainer itemFilter;

    public MetaTileEntityItemCollector(ResourceLocation metaTileEntityId, int tier, int maxItemSuckingRange) {
        super(metaTileEntityId, tier);
        this.maxItemSuckingRange = maxItemSuckingRange;
        this.itemSuckingRange = maxItemSuckingRange;
        this.itemFilter = new ItemFilterContainer(this::markDirty);
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityItemCollector(metaTileEntityId, getTier(), maxItemSuckingRange);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        SimpleOverlayRenderer renderer = isWorking ? Textures.BLOWER_ACTIVE_OVERLAY : Textures.BLOWER_OVERLAY;
        renderer.renderSided(Direction.UP, renderState, translation, pipeline);
        Textures.AIR_VENT_OVERLAY.renderSided(Direction.DOWN, renderState, translation, pipeline);
        Textures.PIPE_OUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
    }

    protected int getEnergyConsumedPerTick() {
        return BASE_EU_CONSUMPTION * (1 << (getTier() - 1));
    }

    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isWorking);
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        super.receiveInitialSyncData(buf);
        this.isWorking = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, FriendlyByteBuf buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == IS_WORKING) {
            this.isWorking = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @Override
    protected boolean canMachineConnectRedstone(Direction side) {
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        if (getWorld().isClientSide) {
            return;
        }

        boolean isWorkingNow = energyContainer.getEnergyStored() >= getEnergyConsumedPerTick() && isBlockRedstonePowered();

        if (isWorkingNow) {
            energyContainer.removeEnergy(getEnergyConsumedPerTick());
            BlockPos selfPos = getPos();
            if (areaCenterPos == null || areaBoundingBox == null || areaCenterPos.getX() != selfPos.getX() ||
                    areaCenterPos.getZ() != selfPos.getZ() || areaCenterPos.getY() != selfPos.getY() + 1) {
                this.areaCenterPos = selfPos.above();
                this.areaBoundingBox = new AABB(areaCenterPos).inflate(itemSuckingRange, 1.0, itemSuckingRange);
            }
            moveItemsInEffectRange();
        }

        if (isWorkingNow != isWorking) {
            this.isWorking = isWorkingNow;
            writeCustomData(IS_WORKING, buffer -> buffer.writeBoolean(isWorkingNow));
        }
    }

    protected void moveItemsInEffectRange() {
        List<ItemEntity> itemsInRange = getWorld().getEntitiesOfClass(ItemEntity.class, areaBoundingBox);
        for (ItemEntity entityItem : itemsInRange) {
            if (entityItem.isRemoved()) continue;
            double distanceX = (areaCenterPos.getX() + 0.5) - entityItem.position().x;
            double distanceZ = (areaCenterPos.getZ() + 0.5) - entityItem.position().z;
            double distance = Math.sqrt(distanceX * distanceX + distanceZ * distanceZ);
            if (!itemFilter.testItemStack(entityItem.getItem())) {
                continue;
            }
            if (distance >= 0.7) {
                if (!entityItem.isPickable()) {
                    double directionX = distanceX / distance;
                    double directionZ = distanceZ / distance;
                    entityItem.setDeltaMovement(directionX * MOTION_MULTIPLIER * getTier(), entityItem.getDeltaMovement().y, directionZ * MOTION_MULTIPLIER * getTier());
                    entityItem.setPickUpDelay(1);
                }
            } else {
                ItemStack itemStack = entityItem.getItem();
                ItemStack remainder = GTTransferUtils.insertItem(exportItems, itemStack, false);
                if (remainder.isEmpty()) {
                    entityItem.discard();
                } else if (itemStack.getCount() > remainder.getCount()) {
                    entityItem.setItem(remainder);
                }
            }
        }
        if (getOffsetTimer() % 5 == 0) {
            pushItemsIntoNearbyHandlers(getFrontFacing());
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level player, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.machine.item_collector.tooltip"));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.uses_per_tick", getEnergyConsumedPerTick()));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.max_voltage_in", energyContainer.getInputVoltage(), GTValues.VNF[getTier()]));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.working_area", maxItemSuckingRange, maxItemSuckingRange));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable Level world, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(Component.translatable("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(INVENTORY_SIZES[Mth.clamp(getTier(), 0, INVENTORY_SIZES.length - 1)]);
    }

    @Override
    public boolean canPlaceCoverOnSide(Direction side) {
        return side != Direction.DOWN && side != Direction.UP;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        return canPlaceCoverOnSide(side) ? super.getCapability(capability, side) : null;
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.putInt("CollectRange", itemSuckingRange);
        data.put("Filter", itemFilter.serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        this.itemSuckingRange = data.getInt("CollectRange");
        this.itemFilter.deserializeNBT(data.getCompound("Filter"));
    }

    protected void setItemSuckingRange(int itemSuckingRange) {
        this.itemSuckingRange = itemSuckingRange;
        this.areaBoundingBox = null;
        markDirty();
    }

    protected void adjustSuckingRange(int amount) {
        setItemSuckingRange(Mth.clamp(itemSuckingRange + amount, 1, maxItemSuckingRange));
    }

    @Override
    protected ModularUI createUI(Player entityPlayer) {
        int rowSize = (int) Math.sqrt(exportItems.getSlots());
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176,
                45 + rowSize * 18 + 105 + 82)
                .label(10, 5, getMetaFullName());

        builder.widget(new ClickButtonWidget(10, 20, 20, 20, "-1", data -> adjustSuckingRange(-1)));
        builder.widget(new ClickButtonWidget(146, 20, 20, 20, "+1", data -> adjustSuckingRange(+1)));
        builder.widget(new ImageWidget(30, 20, 116, 20, GuiTextures.DISPLAY));
        builder.widget(new SimpleTextWidget(88, 30, "gregtech.machine.item_collector.gui.collect_range", 0xFFFFFF, () -> Integer.toString(itemSuckingRange)));

        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                builder.widget(new SlotWidget(exportItems, index, 89 - rowSize * 9 + x * 18, 45 + y * 18, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT));
            }
        }

        this.itemFilter.initUI(45 + rowSize * 18 + 5, builder::widget);
        builder.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 45 + rowSize * 18 + 105);
        return builder.build(getHolder(), entityPlayer);
    }
}
