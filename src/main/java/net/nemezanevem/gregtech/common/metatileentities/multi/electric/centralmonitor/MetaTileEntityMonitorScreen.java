package net.nemezanevem.gregtech.common.metatileentities.multi.electric.centralmonitor;

import codechicken.lib.raytracer.VoxelShapeBlockHitResult;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntityUIFactory;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.blockentity.multiblock.MultiblockControllerBase;
import net.nemezanevem.gregtech.api.capability.GregtechDataCodes;
import net.nemezanevem.gregtech.api.cover.CoverBehavior;
import net.nemezanevem.gregtech.api.cover.ICoverable;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.widgets.*;
import net.nemezanevem.gregtech.api.pipenet.tile.TileEntityPipeBase;
import net.nemezanevem.gregtech.api.util.BlockPosFace;
import net.nemezanevem.gregtech.client.util.RenderUtil;
import net.nemezanevem.gregtech.common.metatileentities.MetaTileEntities;
import net.nemezanevem.gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

public class MetaTileEntityMonitorScreen extends MetaTileEntityMultiblockPart {

    // run-time data
    public CoverDigitalInterface coverTMP;
    private long lastClickTime;
    private UUID lastClickUUID;
    public MonitorPluginBaseBehavior plugin;
    // persistent data
    public BlockPosFace coverPos;
    public CoverDigitalInterface.MODE mode = CoverDigitalInterface.MODE.FLUID;
    public int slot = 0;
    public float scale = 1;
    public int frameColor = 0XFF00FF00;
    private ItemStackHandler inventory;

    public MetaTileEntityMonitorScreen(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 1);
    }

    public void setMode(BlockPosFace cover, CoverDigitalInterface.MODE mode) {
        CoverDigitalInterface last_cover = this.getCoverFromPosSide(coverPos);
        CoverDigitalInterface now_cover = this.getCoverFromPosSide(cover);
        if (this.mode == mode) {
            if (Objects.equals(cover, coverPos) && last_cover == null && cover == null || last_cover != null && last_cover == now_cover) {
                return;
            }
        }
        if (last_cover != null && this.mode != CoverDigitalInterface.MODE.PROXY) {
            last_cover.unSubProxyMode(this.mode);
        }
        if (cover != null && mode != CoverDigitalInterface.MODE.PROXY) {
            now_cover.subProxyMode(mode);
        }
        this.coverPos = cover;
        this.mode = mode;
        updateProxyPlugin();
        writeCustomData(GregtechDataCodes.UPDATE_ALL, this::writeSync);
        this.markDirty();
    }

    public void setMode(BlockPosFace cover) {
        setMode(cover, this.mode);
    }

    public void setMode(CoverDigitalInterface.MODE mode) {
        this.setMode(this.coverPos, mode);
    }

    public void setConfig(int slot, float scale, int color) {
        if ((this.scale == scale || scale < 1 || scale > 8) && (this.slot == slot || slot < 0) && this.frameColor == color)
            return;
        this.slot = slot;
        this.scale = scale;
        this.frameColor = color;
        writeCustomData(GregtechDataCodes.UPDATE_ALL, this::writeSync);
        markDirty();
    }

    public CoverDigitalInterface getCoverFromPosSide(BlockPosFace posFacing) {
        if (posFacing == null) return null;
        ICoverable mte = null;
        IGregTechTileEntity holder = getHolderFromPos(posFacing.pos);
        if (holder == null) {
            BlockEntity te = this.getWorld() == null ? null : this.getWorld().getBlockEntity(posFacing.pos);
            if (te instanceof TileEntityPipeBase) {
                mte = ((TileEntityPipeBase<?, ?>) te).getCoverableImplementation();
            }
        } else {
            mte = holder.getMetaTileEntity();
        }
        if (mte != null) {
            CoverBehavior cover = mte.getCoverAtSide(posFacing.facing);
            if (cover instanceof CoverDigitalInterface) {
                return (CoverDigitalInterface) cover;
            }
        }
        return null;
    }

    public IGregTechTileEntity getHolderFromPos(BlockPos pos) {
        BlockEntity te = this.getWorld() == null || pos == null ? null : this.getWorld().getBlockEntity(pos);
        if (te instanceof IGregTechTileEntity && ((IGregTechTileEntity) te).isValid()) {
            return (IGregTechTileEntity) te;
        }
        return null;
    }

    public void updateCoverValid(Set<BlockPosFace> covers) {
        if (this.coverPos != null) {
            if (!covers.contains(this.coverPos)) {
                setMode(null, CoverDigitalInterface.MODE.PROXY);
            }
        }
    }

    private void writeSync(FriendlyByteBuf buf) {
        buf.writeBoolean(this.coverPos != null);
        if (this.coverPos != null) {
            buf.writeBlockPos(coverPos.pos);
            buf.writeByte(coverPos.facing.ordinal());
        }
        buf.writeByte(this.mode.ordinal());
        buf.writeVarInt(this.slot);
        buf.writeFloat(this.scale);
        buf.writeVarInt(this.frameColor);
    }

    private void readSync(FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            BlockPos pos = buf.readBlockPos();
            Direction side = Direction.from3DDataValue(buf.readByte());
            BlockPosFace pair = new BlockPosFace(pos, side);
            if (!pair.equals(this.coverPos)) {
                this.coverTMP = null;
                this.coverPos = pair;
            }
        } else {
            this.coverPos = null;
            this.coverTMP = null;
        }
        this.mode = CoverDigitalInterface.MODE.VALUES[buf.readByte()];
        this.slot = buf.readVarInt();
        this.scale = buf.readFloat();
        this.frameColor = buf.readVarInt();
        updateProxyPlugin();
    }

    private void updateProxyPlugin() {
        if (this.plugin instanceof ProxyHolderPluginBehavior) {
            if (this.mode == CoverDigitalInterface.MODE.PROXY && coverPos != null) {
                ((ProxyHolderPluginBehavior) this.plugin).onHolderPosUpdated(coverPos.pos);
            } else {
                ((ProxyHolderPluginBehavior) this.plugin).onHolderPosUpdated(null);
            }
        }
    }

    public int getX() {
        if (this.getController() != null) {
            if (this.getController().getPos().getX() - this.getPos().getX() != 0) {
                return Math.abs(this.getController().getPos().getX() - this.getPos().getX()) - 1;
            } else {
                return Math.abs(this.getController().getPos().getZ() - this.getPos().getZ()) - 1;
            }
        }
        return -1;
    }

    public int getY() {
        if (this.getController() != null) {
            return ((MetaTileEntityCentralMonitor) this.getController()).height - (this.getPos().getY() + 1 - this.getController().getPos().getY()) - 1;
        }
        return -1;
    }


    public boolean isActive() {
        if (this.coverPos != null && this.mode != CoverDigitalInterface.MODE.PROXY) {
            CoverDigitalInterface cover = coverTMP != null ? coverTMP : this.getCoverFromPosSide(this.coverPos);
            if (cover != null) {
                if (cover.isProxy()) {
                    coverTMP = cover;
                    return true;
                }
            }
            this.coverPos = null;
        }
        return plugin != null;
    }

    public void pluginDirty() {
        if (plugin != null) {
            plugin.writeToNBT(this.itemInventory.getStackInSlot(0).getOrCreateSubCompound("monitor_plugin"));
        }
    }

    private void loadPlugin(MonitorPluginBaseBehavior plugin) {
        if (plugin !=null && this.plugin != plugin) {
            this.plugin = plugin.createPlugin();
            this.plugin.readFromNBT(this.itemInventory.getStackInSlot(0).getOrCreateSubCompound("monitor_plugin"));
            this.plugin.onMonitorValid(this, true);
        }
        updateProxyPlugin();
    }

    private void unloadPlugin() {
        if (plugin != null) {
            this.plugin.onMonitorValid(null, false);
        }
        this.plugin = null;
    }

    @Override
    public void tick() {
        super.tick();
        if (plugin != null && this.getController() != null && this.isActive()) {
            plugin.update();
        }
    }

    public void renderScreen(float partialTicks, HitResult rayTraceResult) {
        if (getController() == null) return;
        Direction side = getController().getFrontFacing();
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.translate((scale - 1) * 0.5, (scale - 1) * 0.5, 0);
        poseStack.scale(this.scale, this.scale, 1);

        if (plugin != null) {
            plugin.renderPlugin(partialTicks, rayTraceResult);
        }

        if (coverTMP != null) {
            boolean flag = true;
            if (checkLookingAt(rayTraceResult) != null && plugin == null && this.mode != CoverDigitalInterface.MODE.PROXY) {
                if (coverTMP.renderSneakingLookAt(rayTraceResult.getBlockPos(), side, slot, partialTicks)) {
                    flag = false;
                }
            }
            if (this.mode == CoverDigitalInterface.MODE.PROXY) return;
            if (flag) {
                coverTMP.renderMode(this.mode, this.slot, partialTicks);

                BlockEntity te = coverTMP.getCoveredTE();
                if (te != null) {
                    ItemStack itemStack;
                    if (te instanceof IGregTechTileEntity) {
                        itemStack = ((IGregTechTileEntity) te).getMetaTileEntity().getStackForm();
                    } else {
                        BlockPos pos = te.getBlockPos();
                        itemStack = te.getBlockState().getCloneItemStack(new BlockHitResult(new Vec3(0.5, 0.5, 0.5), coverTMP.getCoveredFacing(), pos), te.getLevel(), pos, Minecraft.getInstance().player);
                    }
                    Component name = itemStack.getDisplayName();
                    // render machine
                    RenderUtil.renderItemOverLay(poseStack, -2.6f, -2.65f, 0.003f, 1 / 100f, itemStack);
                    // render name
                    RenderUtil.renderText(poseStack, 0, -3.5f / 16, 0, 1.0f / 200, 0XFFFFFFFF, name, true);
                }

            }
            // render frame
            RenderUtil.renderRect(-7f / 16, -7f / 16, 14f / 16, 0.01f, 0.003f, frameColor);
            RenderUtil.renderRect(-7f / 16, -4f / 16 - 0.01f, 14f / 16, 0.01f, 0.003f, frameColor);
            RenderUtil.renderRect(-7f / 16, -7f / 16 + 0.01f, 0.01f, 3f / 16 - 0.02f, 0.003f, frameColor);
            RenderUtil.renderRect(7f / 16 - 0.01f, -7f / 16 + 0.01f, 0.01f, 3f / 16 - 0.02f, 0.003f, frameColor);

            RenderUtil.renderRect(-7f / 16, -3f / 16, 14f / 16, 0.01f, 0.003f, frameColor);
            RenderUtil.renderRect(-7f / 16, 7f / 16 - 0.01f, 14f / 16, 0.01f, 0.003f, frameColor);
            RenderUtil.renderRect(-7f / 16, -3f / 16 + 0.01f, 0.01f, 10f / 16 - 0.02f, 0.003f, frameColor);
            RenderUtil.renderRect(7f / 16 - 0.01f, -3f / 16 + 0.01f, 0.01f, 10f / 16 - 0.02f, 0.003f, frameColor);
        }
    }

    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        super.writeInitialSyncData(buf);
        writeSync(buf);
        buf.writeItem(this.inventory.getStackInSlot(0));
        if (plugin != null) {
            plugin.writeInitialSyncData(buf);
        }
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        super.receiveInitialSyncData(buf);
        readSync(buf);
        try {
            ItemStack itemStack = buf.readItem();
            MonitorPluginBaseBehavior behavior = MonitorPluginBaseBehavior.getBehavior(itemStack);
            if (behavior == null) {
                unloadPlugin();
            } else {
                this.inventory.setStackInSlot(0, itemStack);
                loadPlugin(behavior);
                plugin.receiveInitialSyncData(buf);
            }
        } catch (IOException e) {
            GregTech.LOGGER.error("Could not initialize Monitor Screen from InitialSyncData buffer", e);
        }
    }

    @Override
    public void receiveCustomData(int dataId, FriendlyByteBuf buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.UPDATE_ALL) {
            readSync(buf);
        } else if (dataId == GregtechDataCodes.UPDATE_PLUGIN_DATA) { //plugin
            if (plugin != null) {
                plugin.readPluginData(buf.readVarInt(), buf);
            }
        } else if (dataId == GregtechDataCodes.UPDATE_PLUGIN_ITEM) {
            try {
                ItemStack itemStack = buf.readItem();
                MonitorPluginBaseBehavior behavior = MonitorPluginBaseBehavior.getBehavior(itemStack);
                if (behavior == null) {
                    unloadPlugin();
                } else {
                    this.inventory.setStackInSlot(0, itemStack);
                    loadPlugin(behavior);
                }
            } catch (IOException e) {
                GregTech.LOGGER.error("Could not initialize Monitor Screen from CustomData buffer", e);
            }
        }
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag data) {
        if (this.coverPos != null) {
            data.put("coverPos", NbtUtils.writeBlockPos(this.coverPos.pos));
            data.putByte("coverSide", (byte) this.coverPos.facing.ordinal());
        }
        data.putByte("mode", (byte) this.mode.ordinal());
        data.putFloat("scale", this.scale);
        data.putInt("color", this.frameColor);
        data.putInt("slot", this.slot);
        data.put("Inventory", this.inventory.serializeNBT());
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        this.frameColor = data.contains("color") ? data.getInt("color") : 0XFF00Ff00;
        this.scale = data.contains("scale") ? data.getFloat("scale") : 1;
        this.slot = data.contains("slot") ? data.getInt("slot") : 0;
        this.mode = CoverDigitalInterface.MODE.VALUES[data.contains("mode") ? data.getByte("mode") : 0];
        this.inventory.deserializeNBT(data.getCompound("Inventory"));
        if (data.contains("coverPos") && data.contains("coverSide")) {
            BlockPos pos = NBTUtils.getPosFromTag(data.getCompound("coverPos"));
            Direction side = Direction.from3DDataValue(data.getByte("coverSide"));
            this.coverPos = new BlockPosFace(pos, side);
        } else {
            this.coverPos = null;
        }
        MonitorPluginBaseBehavior behavior = MonitorPluginBaseBehavior.getBehavior(this.inventory.getStackInSlot(0));
        if (behavior == null) {
            unloadPlugin();
        } else {
            loadPlugin(behavior);
        }
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.inventory = new ItemStackHandler() {
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                MonitorPluginBaseBehavior behavior = MonitorPluginBaseBehavior.getBehavior(stack);
                return behavior != null;
            }

            @Nonnull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (!getWorld().isClientSide && !getStackInSlot(slot).isEmpty() && !simulate) {
                    unloadPlugin();
                    writeCustomData(GregtechDataCodes.UPDATE_PLUGIN_ITEM, packetBuffer -> {
                        packetBuffer.writeItem(ItemStack.EMPTY);
                    });
                }
                return super.extractItem(slot, amount, simulate);
            }
        };
        this.itemInventory = this.inventory;
    }

    @Override
    public boolean shouldRenderOverlay() {
        MultiblockControllerBase controller = this.getController();
        return controller instanceof MetaTileEntityCentralMonitor && ((MetaTileEntityCentralMonitor) controller).isActive();
    }

    @Override
    public boolean canPlaceCoverOnSide(Direction side) {
        return this.getController() != null && this.getController().getFrontFacing() != side;
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (!this.getWorld().isClientSide) {
            this.setMode(null, this.mode);
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        if (capability == ForgeCapabilities.FLUID_HANDLER || capability == ForgeCapabilities.ITEM_HANDLER) {
            CoverBehavior coverBehavior = getCoverFromPosSide(this.coverPos);
            if (coverBehavior != null && coverBehavior.coverHolder != null) {
                return coverBehavior.coverHolder.getCapability(capability, coverBehavior.attachedSide);
            }
        }
        return LazyOptional.empty();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new MetaTileEntityMonitorScreen(this.metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(Player entityPlayer) {
        MultiblockControllerBase controller = this.getController();
        if (controller instanceof MetaTileEntityCentralMonitor && ((MetaTileEntityCentralMonitor) controller).isActive()) {
            int width = 330;
            int height = 260;
            ToggleButtonWidget[] buttons = new ToggleButtonWidget[5];
            buttons[0] = new ToggleButtonWidget(width - 135, 25, 20, 20, GuiTextures.BUTTON_FLUID, () -> this.mode == CoverDigitalInterface.MODE.FLUID, (isPressed) -> {
                if (isPressed) setMode(CoverDigitalInterface.MODE.FLUID);
            }).setTooltipText("metaitem.cover.digital.mode.fluid");
            buttons[1] = new ToggleButtonWidget(width - 115, 25, 20, 20, GuiTextures.BUTTON_ITEM, () -> this.mode == CoverDigitalInterface.MODE.ITEM, (isPressed) -> {
                if (isPressed) setMode(CoverDigitalInterface.MODE.ITEM);
            }).setTooltipText("metaitem.cover.digital.mode.item");
            buttons[2] = new ToggleButtonWidget(width - 95, 25, 20, 20, GuiTextures.BUTTON_ENERGY, () -> this.mode == CoverDigitalInterface.MODE.ENERGY, (isPressed) -> {
                if (isPressed) setMode(CoverDigitalInterface.MODE.ENERGY);
            }).setTooltipText("metaitem.cover.digital.mode.energy");
            buttons[3] = new ToggleButtonWidget(width - 75, 25, 20, 20, GuiTextures.BUTTON_MACHINE, () -> this.mode == CoverDigitalInterface.MODE.MACHINE, (isPressed) -> {
                if (isPressed) setMode(CoverDigitalInterface.MODE.MACHINE);
            }).setTooltipText("metaitem.cover.digital.mode.machine");
            buttons[4] = new ToggleButtonWidget(width - 35, 25, 20, 20, GuiTextures.BUTTON_INTERFACE, () -> this.mode == CoverDigitalInterface.MODE.PROXY, (isPressed) -> {
                if (isPressed) setMode(CoverDigitalInterface.MODE.PROXY);
            }).setTooltipText("metaitem.cover.digital.mode.proxy");
            List<CoverDigitalInterface> covers = new ArrayList<>();
            ((MetaTileEntityCentralMonitor) controller).getAllCovers().forEach(coverPos -> covers.add(getCoverFromPosSide(coverPos)));
            WidgetPluginConfig pluginWidget = new WidgetPluginConfig();
            WidgetPluginConfig mainGroup = new WidgetPluginConfig().setSize(width, height);
            mainGroup.widget(new LabelWidget(15, 55, "monitor.gui.title.scale", 0xFFFFFFFF))
                    .widget(new ClickButtonWidget(50, 50, 20, 20, "-1", (data) -> setConfig(this.slot, ((float) Math.round((scale - (data.isShiftClick ? 1.0f : 0.1f)) * 10) / 10), this.frameColor)))
                    .widget(new ClickButtonWidget(130, 50, 20, 20, "+1", (data) -> setConfig(this.slot, ((float) Math.round((scale + (data.isShiftClick ? 1.0f : 0.1f)) * 10) / 10), this.frameColor)))
                    .widget(new ImageWidget(70, 50, 60, 20, GuiTextures.DISPLAY))
                    .widget(new SimpleTextWidget(100, 60, "", 16777215, () -> Float.toString(scale)))

                    .widget(new LabelWidget(15, 85, "monitor.gui.title.argb", 0xFFFFFFFF))
                    .widget(new WidgetARGB(50, 80, 20, this.frameColor, (color) -> setConfig(this.slot, this.scale, color)))

                    .widget(new LabelWidget(15, 110, "monitor.gui.title.slot", 0xFFFFFFFF))
                    .widget(new ClickButtonWidget(50, 105, 20, 20, "-1", (data) -> setConfig(this.slot - 1, this.scale, this.frameColor)))
                    .widget(new ClickButtonWidget(130, 105, 20, 20, "+1", (data) -> setConfig(this.slot + 1, this.scale, this.frameColor)))
                    .widget(new ImageWidget(70, 105, 60, 20, GuiTextures.DISPLAY))
                    .widget(new SimpleTextWidget(100, 115, "", 16777215, () -> Integer.toString(slot)))

                    .widget(new LabelWidget(15, 135, "monitor.gui.title.plugin", 0xFFFFFFFF))
                    .widget(new SlotWidget(inventory, 0, 50, 130, true, true)
                            .setBackgroundTexture(GuiTextures.SLOT)
                            .setChangeListener(() -> {
                                if (this.getWorld() != null && !this.getWorld().isClientSide) {
                                    MonitorPluginBaseBehavior behavior = MonitorPluginBaseBehavior.getBehavior(inventory.getStackInSlot(0));
                                    if (behavior == null) {
                                        unloadPlugin();
                                    } else {
                                        loadPlugin(behavior);
                                    }
                                    writeCustomData(GregtechDataCodes.UPDATE_PLUGIN_ITEM, packetBuffer -> packetBuffer.writeItemStack(inventory.getStackInSlot(0)));
                                }
                            }))
                    .widget(new ClickButtonWidget(80, 130, 40, 20, "monitor.gui.title.config", (data) -> {
                        if (plugin != null && mainGroup.isVisible()) {
                            plugin.customUI(pluginWidget, this.getHolder(), entityPlayer);
                            mainGroup.setVisible(false);
                        }
                    }) {
                        @Override
                        protected void triggerButton() {
                            super.triggerButton();
                            if (plugin != null && mainGroup.isVisible()) {
                                plugin.customUI(pluginWidget, getHolder(), entityPlayer);
                                mainGroup.setVisible(false);
                            }
                        }
                    })

                    .widget(new WidgetCoverList(width - 140, 50, 120, 11, covers, getCoverFromPosSide(this.coverPos), (coverPos) -> {
                        if (coverPos == null) {
                            this.setMode(null, this.mode);
                        } else {
                            this.setMode(new BlockPosFace(coverPos.coverHolder.getPos(), coverPos.attachedSide));
                        }
                    }))

                    .widget(buttons[0])
                    .widget(buttons[1])
                    .widget(buttons[2])
                    .widget(buttons[3])
                    .widget(buttons[4])
                    .bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 15, 170);

            return ModularUI.builder(GuiTextures.BOXED_BACKGROUND, width, height)
                    .widget(pluginWidget)
                    .widget(mainGroup)
                    .widget(new WidgetMonitorScreen(330, 0, 150, this))
                    .widget(new LabelWidget(15, 13, "gregtech.machine.monitor_screen.name", 0XFFFFFFFF))
                    .widget(new ClickButtonWidget(15, 25, 40, 20, "monitor.gui.title.back", data -> {
                        if (mainGroup.isVisible() && ((MetaTileEntityCentralMonitor) controller).isActive() && controller.isValid()) {
                            MetaTileEntityUIFactory.INSTANCE.openUI(controller.getHolder(), (PlayerMP) entityPlayer);
                        } else if (!mainGroup.isVisible()) {
                            pluginWidget.removePluginWidget();
                            mainGroup.setVisible(true);
                            if (plugin != null) {
                                plugin.markAsDirty();
                            }
                        }
                    }) {
                        @Override
                        protected void triggerButton() {
                            super.triggerButton();
                            if (!mainGroup.isVisible()) {
                                pluginWidget.removePluginWidget();
                                mainGroup.setVisible(true);
                                if (plugin != null) {
                                    plugin.markAsDirty();
                                }
                            }
                        }
                    })
                    .bindCloseListener(() -> {
                        if (plugin != null) {
                            plugin.markAsDirty();
                        }
                    })
                    .build(this.getHolder(), entityPlayer);
        }
        return null;
    }

    // adaptive click, supports scaling. x and y is the pos of the origin screen (scale = 1). this func must be called when screen is active.
    public boolean onClickLogic(Player playerIn, InteractionHand hand, Direction facing, boolean isRight, double x, double y) {
        if (this.plugin != null) {
            boolean flag = this.plugin.onClickLogic(playerIn, hand, facing, isRight, x, y);
            if (flag) return true;
        }
        if (this.getWorld().isClientSide) return true;
        CoverDigitalInterface coverBehavior = getCoverFromPosSide(this.coverPos);
        if (isRight) {
            if (coverBehavior != null && coverBehavior.isProxy() && coverBehavior.coverHolder != null && this.mode != CoverDigitalInterface.MODE.PROXY) {
                if (playerIn.isSneaking() && playerIn.getHeldItemMainhand().isEmpty() && this.plugin == null) {
                    if (1f / 16 < x && x < 4f / 16 && 1f / 16 < y && y < 4f / 16) {
                        this.setConfig(this.slot - 1, this.scale, this.frameColor);
                        return true;
                    } else if (12f / 16 < x && x < 15f / 16 && 1f / 16 < y && y < 4f / 16) {
                        this.setConfig(this.slot + 1, this.scale, this.frameColor);
                        return true;
                    }
                }
                if (coverBehavior.modeRightClick(playerIn, hand, this.mode, this.slot) == EnumActionResult.PASS) {
                    if (!playerIn.isSneaking() && this.openGUIOnRightClick()) {
                        TileEntity te = coverBehavior.getCoveredTE();
                        if (te != null) {
                            BlockPos pos = te.getPos();
                            BlockState state = te.getWorld().getBlockState(pos);
                            state.getBlock().onBlockActivated(coverBehavior.coverHolder.getWorld(), pos, state, playerIn, hand, coverBehavior.getCoveredFacing(), 0.5f, 0.5f, 0.5f);
                        }
                        return true;
                    } else {
                        return false;
                    }
                }
                return true;
            }
        } else {
            if (coverBehavior != null && coverBehavior.isProxy() && coverBehavior.coverHolder != null && this.mode != CoverDigitalInterface.MODE.PROXY) {
                return coverBehavior.modeLeftClick(playerIn, this.mode, this.slot);
            }
        }
        return false;
    }

    private double[] handleRayTraceResult(RayTraceResult rayTraceResult) {
        double dX = rayTraceResult.sideHit.getAxis() == Direction.Axis.X
                ? rayTraceResult.hitVec.z - rayTraceResult.getBlockPos().getZ()
                : rayTraceResult.hitVec.x - rayTraceResult.getBlockPos().getX();
        double dY = rayTraceResult.sideHit.getAxis() == Direction.Axis.Y
                ? rayTraceResult.hitVec.z - rayTraceResult.getBlockPos().getZ()
                : rayTraceResult.hitVec.y - rayTraceResult.getBlockPos().getY();
        dX = 1 - dX;
        dY = 1 - dY;
        if(rayTraceResult.sideHit.getYOffset() < 0) {
            dY = 1 - dY;
        }
        if (rayTraceResult.sideHit == Direction.WEST || rayTraceResult.sideHit == Direction.SOUTH) {
            dX = 1 - dX;
        } else if (rayTraceResult.sideHit == Direction.UP) {
            dX = 1 - dX;
            dY = 1 - dY;
        }
        return new double[]{dX, dY};
    }

    private boolean handleHitResultWithScale(Player playerIn, InteractionHand hand, Direction facing, boolean isRight, VoxelShapeBlockHitResult rayTraceResult) {
        boolean flag = false;
        if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK && this.getController() != null) {
            double[] pos = handleRayTraceResult(rayTraceResult);
            MetaTileEntityMonitorScreen[][] screens = ((MetaTileEntityCentralMonitor) this.getController()).screens;
            int mX = this.getX(), mY = this.getY();
            int max = Math.max(mX, mY);
            for (int i = 0; i <= max && mX - i >= 0; i++) {
                for (int j = 0; j <= max && mY - j >= 0; j++) {
                    MetaTileEntityMonitorScreen screen = screens[mX - i][mY - j];
                    if (screen != null && screen.isActive()) {
                        double xR = (pos[0] + i) / screen.scale;
                        double yR = (pos[1] + j) / screen.scale;
                        if (xR >= 0 && xR <= 1 && yR >= 0 && yR <= 1)
                            if (screen.onClickLogic(playerIn, hand, facing, isRight, xR, yR)) {
                                flag = true;
                            }
                    }
                }
            }
        }
        return flag;
    }

    @SideOnly(Side.CLIENT)
    public double[] checkLookingAt(RayTraceResult rayTraceResult) {
        if (this.getWorld() != null) {
            MultiblockControllerBase controller = this.getController();
            if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK && controller != null && rayTraceResult.sideHit == controller.getFrontFacing()) {
                int i, j;
                TileEntity tileEntity = this.getWorld().getTileEntity(rayTraceResult.getBlockPos());
                if (tileEntity instanceof IGregTechTileEntity && ((IGregTechTileEntity) tileEntity).getMetaTileEntity() instanceof MetaTileEntityMonitorScreen) {
                    MetaTileEntityMonitorScreen screenHit = (MetaTileEntityMonitorScreen) ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
                    if (controller == screenHit.getController()) {
                        i = ((MetaTileEntityMonitorScreen) ((IGregTechTileEntity) tileEntity).getMetaTileEntity()).getX() - this.getX();
                        j = ((MetaTileEntityMonitorScreen) ((IGregTechTileEntity) tileEntity).getMetaTileEntity()).getY() - this.getY();
                        double[] pos = handleRayTraceResult(rayTraceResult);
                        if ((i >= 0 && j >= 0)) {
                            pos[0] = (pos[0] + i) / this.scale;
                            pos[1] = (pos[1] + j) / this.scale;
                            if (pos[0] >= 0 && pos[0] <= 1 && pos[1] >= 0 && pos[1] <= 1)
                                return new double[]{pos[0], pos[1]};
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean onRightClick(Player playerIn, InteractionHand hand, Direction facing, VoxelShapeBlockHitResult hitResult) {
        if (!(!playerIn.isSneaking() && ToolHelper.isTool(playerIn.getHeldItem(hand), ToolClasses.SCREWDRIVER))
                && !MetaTileEntities.MONITOR_SCREEN.getStackForm().isItemEqual(playerIn.getHeldItem(hand))) {
            if (playerIn.world.getTotalWorldTime() - lastClickTime < 2 &&
                    playerIn.getPersistentID().equals(lastClickUUID)) {
                return true;
            }
            lastClickTime = playerIn.world.getTotalWorldTime();
            lastClickUUID = playerIn.getPersistentID();

            MultiblockControllerBase controller = this.getController();
            if (controller instanceof MetaTileEntityCentralMonitor &&
                    ((MetaTileEntityCentralMonitor) controller).isActive() && controller.getFrontFacing() == facing) {
                return handleHitResultWithScale(playerIn, hand, facing, true, hitResult);
            }
        }
        return false;
    }

    @Override
    public boolean onScrewdriverClick(Player playerIn, InteractionHand hand, Direction facing, VoxelShapeBlockHitResult hitResult) {
        if (!playerIn.isSneaking() && this.getWorld() != null && !this.getWorld().isClientSide) {
            MetaTileEntityUIFactory.INSTANCE.openUI(this.getHolder(), (PlayerMP) playerIn);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onLeftClick(Player playerIn, Direction facing, VoxelShapeBlockHitResult hitResult) {
        if (playerIn.world.getTotalWorldTime() - lastClickTime < 2 && playerIn.getPersistentID().equals(lastClickUUID)) {
            return;
        }
        lastClickTime = playerIn.world.getTotalWorldTime();
        lastClickUUID = playerIn.getPersistentID();
        MultiblockControllerBase controller = this.getController();
        if (controller != null && controller.getFrontFacing() == facing) {
            handleHitResultWithScale(playerIn, null, facing, false, hitResult);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level player, List<Component> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(Component.translatable("gregtech.multiblock.monitor_screen.tooltip.1"));
        tooltip.add(Component.translatable("gregtech.multiblock.monitor_screen.tooltip.2"));
        tooltip.add(Component.translatable("gregtech.multiblock.monitor_screen.tooltip.3"));
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(null, -1);
    }

    @Override
    public boolean showToolUsages() {
        return false;
    }
}
