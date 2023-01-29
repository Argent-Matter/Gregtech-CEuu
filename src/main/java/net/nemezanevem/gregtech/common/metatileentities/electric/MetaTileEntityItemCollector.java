package net.nemezanevem.gregtech.common.metatileentities.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.ModularUI.Builder;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.SimpleTextWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.covers.filter.ItemFilterContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.Player;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AABB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.IS_WORKING;

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
                this.areaCenterPos = selfPos.up();
                this.areaBoundingBox = new AABB(areaCenterPos).grow(itemSuckingRange, 1.0, itemSuckingRange);
            }
            moveItemsInEffectRange();
        }

        if (isWorkingNow != isWorking) {
            this.isWorking = isWorkingNow;
            writeCustomData(IS_WORKING, buffer -> buffer.writeBoolean(isWorkingNow));
        }
    }

    protected void moveItemsInEffectRange() {
        List<EntityItem> itemsInRange = getWorld().getEntitiesWithinAABB(EntityItem.class, areaBoundingBox);
        for (EntityItem entityItem : itemsInRange) {
            if (entityItem.isDead) continue;
            double distanceX = (areaCenterPos.getX() + 0.5) - entityItem.posX;
            double distanceZ = (areaCenterPos.getZ() + 0.5) - entityItem.posZ;
            double distance = MathHelper.sqrt(distanceX * distanceX + distanceZ * distanceZ);
            if (!itemFilter.testItemStack(entityItem.getItem())) {
                continue;
            }
            if (distance >= 0.7) {
                if (!entityItem.cannotPickup()) {
                    double directionX = distanceX / distance;
                    double directionZ = distanceZ / distance;
                    entityItem.motionX = directionX * MOTION_MULTIPLIER * getTier();
                    entityItem.motionZ = directionZ * MOTION_MULTIPLIER * getTier();
                    entityItem.velocityChanged = true;
                    entityItem.setPickupDelay(1);
                }
            } else {
                ItemStack itemStack = entityItem.getItem();
                ItemStack remainder = GTTransferUtils.insertItem(exportItems, itemStack, false);
                if (remainder.isEmpty()) {
                    entityItem.setDead();
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
    @SideOnly(Side.CLIENT)
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
        return new ItemStackHandler(INVENTORY_SIZES[MathHelper.clamp(getTier(), 0, INVENTORY_SIZES.length - 1)]);
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
        data.setInteger("CollectRange", itemSuckingRange);
        data.setTag("Filter", itemFilter.serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        this.itemSuckingRange = data.getInteger("CollectRange");
        this.itemFilter.deserializeNBT(data.getCompoundTag("Filter"));
    }

    protected void setItemSuckingRange(int itemSuckingRange) {
        this.itemSuckingRange = itemSuckingRange;
        this.areaBoundingBox = null;
        markDirty();
    }

    protected void adjustSuckingRange(int amount) {
        setItemSuckingRange(MathHelper.clamp(itemSuckingRange + amount, 1, maxItemSuckingRange));
    }

    @Override
    protected ModularUI createUI(Player entityPlayer) {
        int rowSize = (int) Math.sqrt(exportItems.getSlots());
        Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176,
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
        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 45 + rowSize * 18 + 105);
        return builder.build(getHolder(), entityPlayer);
    }
}