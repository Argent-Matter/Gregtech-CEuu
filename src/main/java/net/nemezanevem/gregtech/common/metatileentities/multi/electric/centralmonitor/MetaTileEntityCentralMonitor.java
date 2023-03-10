package net.nemezanevem.gregtech.common.metatileentities.multi.electric.centralmonitor;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.nemezanevem.gregtech.api.blockentity.IFastRenderMetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.blockentity.multiblock.GtMultiblockAbilities;
import net.nemezanevem.gregtech.api.blockentity.multiblock.IMultiblockPart;
import net.nemezanevem.gregtech.api.blockentity.multiblock.MultiblockAbility;
import net.nemezanevem.gregtech.api.blockentity.multiblock.MultiblockWithDisplayBase;
import net.nemezanevem.gregtech.api.capability.GregtechCapabilities;
import net.nemezanevem.gregtech.api.capability.GregtechDataCodes;
import net.nemezanevem.gregtech.api.capability.IEnergyContainer;
import net.nemezanevem.gregtech.api.capability.impl.EnergyContainerList;
import net.nemezanevem.gregtech.api.cover.CoverBehavior;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.gui.widgets.AdvancedTextWidget;
import net.nemezanevem.gregtech.api.gui.widgets.WidgetGroup;
import net.nemezanevem.gregtech.api.pattern.BlockPattern;
import net.nemezanevem.gregtech.api.pattern.FactoryBlockPattern;
import net.nemezanevem.gregtech.api.pattern.PatternMatchContext;
import net.nemezanevem.gregtech.api.pipenet.tile.TileEntityPipeBase;
import net.nemezanevem.gregtech.api.util.BlockPosFace;
import net.nemezanevem.gregtech.client.renderer.ICubeRenderer;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import net.nemezanevem.gregtech.client.util.RenderUtil;
import net.nemezanevem.gregtech.common.ConfigHolder;
import net.nemezanevem.gregtech.common.block.BlockMetalCasing;
import net.nemezanevem.gregtech.common.block.MetaBlocks;
import net.nemezanevem.gregtech.common.metatileentities.MetaTileEntities;
import net.nemezanevem.gregtech.common.pipelike.cable.net.EnergyNet;
import net.nemezanevem.gregtech.common.pipelike.cable.net.WorldENet;
import net.nemezanevem.gregtech.common.pipelike.cable.tile.TileEntityCable;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;

import static net.nemezanevem.gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityCentralMonitor extends MultiblockWithDisplayBase implements IFastRenderMetaTileEntity {
    private final static long ENERGY_COST = -ConfigHolder.machines.centralMonitorEuCost;
    public final static int MAX_HEIGHT = 9;
    public final static int MAX_WIDTH = 14;
    // run-time data
    public int width;
    private long lastUpdate;
    private WeakReference<EnergyNet> currentEnergyNet;
    private List<BlockPos> activeNodes;
    private Set<BlockPosFace> netCovers;
    private Set<BlockPosFace> remoteCovers;
    public List<BlockPos> parts;
    public MetaTileEntityMonitorScreen[][] screens;
    private boolean isActive;
    private EnergyContainerList inputEnergy;
    // persistent data
    public int height = 3;

    public MetaTileEntityCentralMonitor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    private EnergyNet getEnergyNet() {
        if (!this.getWorld().isClientSide) {
            BlockEntity te = this.getWorld().getBlockEntity(this.getPos().offset(frontFacing.getOpposite().getNormal()));
            if (te instanceof TileEntityCable) {
                TileEntityPipeBase<?, ?> tileEntityCable = (TileEntityCable) te;
                EnergyNet currentEnergyNet = this.currentEnergyNet.get();
                if (currentEnergyNet != null && currentEnergyNet.isValid() && currentEnergyNet.containsNode(tileEntityCable.getPipePos())) {
                    return currentEnergyNet; //return current net if it is still valid
                }
                WorldENet worldENet = (WorldENet) tileEntityCable.getPipeBlock().getWorldPipeNet(tileEntityCable.getPipeWorld());
                currentEnergyNet = worldENet.getNetFromPos(tileEntityCable.getPipePos());
                if (currentEnergyNet != null) {
                    this.currentEnergyNet = new WeakReference<>(currentEnergyNet);
                }
                return currentEnergyNet;
            }
        }
        return null;
    }

    private void updateNodes() {
        EnergyNet energyNet = getEnergyNet();
        if (energyNet == null) {
            activeNodes.clear();
            return;
        }
        if (energyNet.getLastUpdate() == lastUpdate) {
            return;
        }
        lastUpdate = energyNet.getLastUpdate();
        activeNodes.clear();
        energyNet.getAllNodes().forEach((pos, node) -> {
            if (node.isActive) {
                activeNodes.add(pos);
            }
        });
    }

    public void addRemoteCover(BlockPosFace cover) {
        if (remoteCovers != null) {
            if (remoteCovers.add(cover)) {
                writeCustomData(GregtechDataCodes.UPDATE_COVERS, this::writeCovers);
            }
        }
    }

    private boolean checkCovers() {
        boolean dirty = false;
        updateNodes();
        Set<BlockPosFace> checkCovers = new HashSet<>();
        Level world = this.getWorld();
        for (BlockPos pos : activeNodes) {
            BlockEntity tileEntityCable = world.getBlockEntity(pos);
            if (!(tileEntityCable instanceof TileEntityPipeBase)) {
                continue;
            }
            for (Direction facing : Direction.values()) {
                if (((TileEntityPipeBase<?,?>) tileEntityCable).isConnected(facing)) {
                    BlockEntity tileEntity = world.getBlockEntity(pos.offset(facing.getNormal()));
                    if (tileEntity instanceof IGregTechTileEntity) {
                        MetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
                        if (metaTileEntity != null) {
                            CoverBehavior cover = metaTileEntity.getCoverAtSide(facing.getOpposite());
                            if (cover instanceof CoverDigitalInterface && ((CoverDigitalInterface) cover).isProxy()) {
                                checkCovers.add(new BlockPosFace(metaTileEntity.getPos(), cover.attachedSide));
                            }
                        }
                    }
                }
            }
        }
        Iterator<BlockPosFace> iterator = remoteCovers.iterator();
        while (iterator.hasNext()) {
            BlockPosFace blockPosFace = iterator.next();
            BlockEntity tileEntity = world.getBlockEntity(blockPosFace.pos);
            if (tileEntity instanceof IGregTechTileEntity) {
                MetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
                if (metaTileEntity != null) {
                    CoverBehavior cover = metaTileEntity.getCoverAtSide(blockPosFace.facing);
                    if (cover instanceof CoverDigitalInterface && ((CoverDigitalInterface) cover).isProxy()) {
                        continue;
                    }
                }
            }
            iterator.remove();
            dirty = true;
        }
        if (checkCovers.size() != netCovers.size() || !netCovers.containsAll(checkCovers)) {
            netCovers = checkCovers;
            dirty = true;
        }
        return dirty;
    }

    private void writeCovers(FriendlyByteBuf buf) {
        if(netCovers == null) {
            buf.writeInt(0);
        } else {
            buf.writeInt(netCovers.size());
            for (BlockPosFace cover : netCovers){
                buf.writeBlockPos(cover.pos);
                buf.writeByte(cover.facing.o());
            }
        }
        if(remoteCovers == null) {
            buf.writeInt(0);
        } else {
            buf.writeInt(remoteCovers.size());
            for (BlockPosFace cover : remoteCovers){
                buf.writeBlockPos(cover.pos);
                buf.writeByte(cover.facing.ordinal());
            }
        }

    }

    private void readCovers(FriendlyByteBuf buf) {
        netCovers = new HashSet<>();
        remoteCovers = new HashSet<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            netCovers.add(new BlockPosFace(buf.readBlockPos(), Direction.from3DDataValue(buf.readByte())));
        }
        size = buf.readInt();
        for (int i = 0; i < size; i++) {
            remoteCovers.add(new BlockPosFace(buf.readBlockPos(), Direction.from3DDataValue(buf.readByte())));
        }
    }

    private void writeParts(FriendlyByteBuf buf) {
        buf.writeInt((int) this.getMultiblockParts().stream().filter(MetaTileEntityMonitorScreen.class::isInstance).count());
        this.getMultiblockParts().forEach(part->{
            if (part instanceof MetaTileEntityMonitorScreen) {
                buf.writeBlockPos(((MetaTileEntityMonitorScreen) part).getPos());
            }
        });
    }

    private void readParts(FriendlyByteBuf buf) {
        parts = new ArrayList<>();
        clearScreens();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            parts.add(buf.readBlockPos());
        }
    }

    public void setHeight(int height) {
        if(this.height == height || height < 2 || height > MAX_HEIGHT) return;
        this.height = height;
        reinitializeStructurePattern();
        checkStructurePattern();
        writeCustomData(GregtechDataCodes.UPDATE_HEIGHT, buf-> buf.writeInt(height));
    }

    private void setActive(boolean isActive) {
        if(isActive == this.isActive) return;
        this.isActive = isActive;
        writeCustomData(GregtechDataCodes.UPDATE_ACTIVE, buf -> buf.writeBoolean(this.isActive));
    }

    public boolean isActive() {
        return isStructureFormed() && isActive;
    }

    private void clearScreens() {
        if (screens != null) {
            for (MetaTileEntityMonitorScreen[] screen : screens) {
                for (MetaTileEntityMonitorScreen s : screen) {
                    if(s != null) s.removeFromMultiBlock(this);
                }
            }
        }
        screens = new MetaTileEntityMonitorScreen[width][height];
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.SCREEN.renderSided(getFrontFacing(), renderState, translation, pipeline);
        Textures.COVER_INTERFACE_PROXY.renderSided(getFrontFacing().getOpposite(), renderState, translation, pipeline);
    }

    @Override
    protected void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        textList.add(Component.translatable("gregtech.multiblock.central_monitor.height", this.height));
        if (!isStructureFormed()) {
            MutableComponent buttonText = Component.translatable("gregtech.multiblock.central_monitor.height_modify", height);
            buttonText.append(" ");
            buttonText.append(AdvancedTextWidget.withButton(Component.literal("[-]"), "sub"));
            buttonText.append(" ");
            buttonText.append(AdvancedTextWidget.withButton(Component.literal("[+]"), "add"));
            textList.add(buttonText);
        } else {
            textList.add(Component.translatable("gregtech.multiblock.central_monitor.width", this.width));
            textList.add(Component.translatable("gregtech.multiblock.central_monitor.low_power"));
        }
    }

    @Override
    protected boolean shouldShowVoidingModeButton() {
        return false;
    }

    @Override
    protected void handleDisplayClick(String componentData, Widget.ClickData clickData) {
        super.handleDisplayClick(componentData, clickData);
        int modifier = componentData.equals("add") ? 1 : -1;
        setHeight(this.height + modifier);
    }

    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(width);
        buf.writeInt(height);
        buf.writeBoolean(isActive);
        writeCovers(buf);
        writeParts(buf);
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        super.receiveInitialSyncData(buf);
        this.width = buf.readInt();
        this.height = buf.readInt();
        this.isActive = buf.readBoolean();
        readCovers(buf);
        readParts(buf);
    }

    @Override
    public void receiveCustomData(int id, FriendlyByteBuf buf) {
        super.receiveCustomData(id, buf);
        if (id == GregtechDataCodes.UPDATE_ALL) {
            this.width = buf.readInt();
            this.height = buf.readInt();
            readCovers(buf);
            readParts(buf);
        } else if (id == GregtechDataCodes.UPDATE_COVERS) {
            readCovers(buf);
        } else if (id == GregtechDataCodes.UPDATE_HEIGHT) {
            this.height = buf.readInt();
            this.reinitializeStructurePattern();
        } else if (id == GregtechDataCodes.UPDATE_ACTIVE) {
            this.isActive = buf.readBoolean();
        } else if (id == GregtechDataCodes.STRUCTURE_FORMED) {
            if (!this.isStructureFormed()) {
                clearScreens();
            }
        }
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag data) {
        data.putInt("screenH", this.height);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        this.height = data.contains("screenH") ? data.getInt("screenH") : this.height;
        reinitializeStructurePattern();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new MetaTileEntityCentralMonitor(metaTileEntityId);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    protected void updateFormedValid() {
        if (this.getOffsetTimer() % 20 ==0) {
            setActive(inputEnergy.changeEnergy(ENERGY_COST * this.getMultiblockParts().size()) == ENERGY_COST * this.getMultiblockParts().size());
            if (checkCovers()) {
                this.getMultiblockParts().forEach(part -> {
                    Set<BlockPosFace> covers = getAllCovers();
                    if (part instanceof MetaTileEntityMonitorScreen) {
                        ((MetaTileEntityMonitorScreen) part).updateCoverValid(covers);
                    }
                });
                writeCustomData(GregtechDataCodes.UPDATE_COVERS, this::writeCovers);
            }
        }
    }

    public Set<BlockPosFace> getAllCovers() {
        Set<BlockPosFace> allCovers = new HashSet<>();
        if (netCovers != null) {
            allCovers.addAll(netCovers);
        }
        if (remoteCovers != null) {
            allCovers.addAll(remoteCovers);
        }
        return allCovers;
    }

    @Override
    protected BlockPattern createStructurePattern() {
        StringBuilder start = new StringBuilder("AS");
        StringBuilder slice = new StringBuilder("BB");
        StringBuilder end = new StringBuilder("AA");
        for (int i = 0; i < height - 2; i++) {
            start.append('A');
            slice.append('B');
            end.append('A');
        }
        return FactoryBlockPattern.start(UP, BACK, RIGHT)
                .aisle(start.toString())
                .aisle(slice.toString()).setRepeatable(3, MAX_WIDTH)
                .aisle(end.toString())
                .where('S', selfPredicate())
                .where('A', states(MetaBlocks.METAL_CASING.get().getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID))
                        .or(abilities(GtMultiblockAbilities.INPUT_ENERGY.get()).setMinGlobalLimited(1).setMaxGlobalLimited(3).setPreviewCount(1)))
                .where('B', metaTileEntities(MetaTileEntities.MONITOR_SCREEN))
                .build();
    }

    @Override
    public Component[] getDescription() {
        return new Component[]{Component.translatable("gregtech.multiblock.central_monitor.tooltip.1")};
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        lastUpdate = 0;
        currentEnergyNet = new WeakReference<>(null);
        activeNodes = new ArrayList<>();
        netCovers = new HashSet<>();
        remoteCovers = new HashSet<>();
        inputEnergy = new EnergyContainerList(this.getAbilities(GtMultiblockAbilities.INPUT_ENERGY.get()));
        width = 0;
        checkCovers();
        for (IMultiblockPart part : this.getMultiblockParts()) {
            if (part instanceof MetaTileEntityMonitorScreen) {
                width++;
            }
        }
        width = width / height;
        screens = new MetaTileEntityMonitorScreen[width][height];
        for (IMultiblockPart part : this.getMultiblockParts()) {
            if (part instanceof MetaTileEntityMonitorScreen) {
                MetaTileEntityMonitorScreen screen = (MetaTileEntityMonitorScreen) part;
                screens[screen.getX()][screen.getY()] = screen;
            }
        }
        writeCustomData(GregtechDataCodes.UPDATE_ALL, packetBuffer -> {
            packetBuffer.writeInt(width);
            packetBuffer.writeInt(height);
            writeCovers(packetBuffer);
            writeParts(packetBuffer);
        });
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        if(side == this.frontFacing.getOpposite() && capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return IEnergyContainer.defaultLazy.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        if (this.isStructureFormed()) {
            return pass == 0;
        }
        return false;
    }

    @Override
    public boolean isGlobalRenderer() {
        return true;
    }

    @Override
    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        if (!this.isStructureFormed()) return;
        PoseStack poseStack = RenderSystem.getModelViewStack();
        RenderUtil.useStencil(()->{
            poseStack.pushPose();
            RenderUtil.moveToFace(poseStack, x, y, z, this.frontFacing);
            RenderUtil.rotateToFace(poseStack, this.frontFacing, Direction.NORTH);
            RenderUtil.renderRect(0.5f, -0.5f - (height - 2), width, height, 0.001f, 0xFF000000);
            poseStack.popPose();
        }, ()->{
            if (isActive) {
                poseStack.pushPose();
                /* hack the lightmap */
                GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
                net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
                disableStan
                float lastBrightnessX = OpenGlHelper.lastBrightnessX;
                float lastBrightnessY = OpenGlHelper.lastBrightnessY;
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
                Player player = Minecraft.getInstance().player;
                Minecraft.getInstance().hitResult
                HitResult rayTraceResult = player == null ? null : player.getPickedResult(Minecraft.getInstance().playerController.getBlockReachDistance(), partialTicks);
                int size = 0;
                for (int w = 0; w < width; w++) {
                    for (int h = 0; h < height; h++) {
                        MetaTileEntityMonitorScreen screen = screens[w][h];
                        if (screen != null) {
                            size++;
                            if (screen.isActive()) {
                                BlockPos pos = screen.getPos();
                                BlockPos pos2 = this.getPos();
                                GlStateManager.pushMatrix();
                                RenderUtil.moveToFace(x + pos.getX() - pos2.getX(), y + pos.getY() - pos2.getY(), z + pos.getZ() - pos2.getZ(), this.frontFacing);
                                RenderUtil.rotateToFace(this.frontFacing, Direction.NORTH);
                                screen.renderScreen(partialTicks, rayTraceResult);
                                GlStateManager.popMatrix();
                            }
                        }
                    }
                }

                if (size != parts.size()) {
                    clearScreens();
                    for (BlockPos pos : parts) {
                        TileEntity tileEntity = getWorld().getTileEntity(pos);
                        if(tileEntity instanceof IGregTechTileEntity && ((IGregTechTileEntity) tileEntity).getMetaTileEntity() instanceof MetaTileEntityMonitorScreen) {
                            MetaTileEntityMonitorScreen screen = (MetaTileEntityMonitorScreen) ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
                            screen.addToMultiBlock(this);
                            int sx = screen.getX(), sy = screen.getY();
                            if (sx < 0 || sx >= width || sy < 0 || sy >= height) {
                                parts.clear();
                                clearScreens();
                                break;
                            }
                            screens[sx][sy] = screen;
                        }
                    }
                }

                /* restore the lightmap  */
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
                net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
                GL11.glPopAttrib();
                GlStateManager.popMatrix();
            }
        }, true);
    }

    @Override
    public AABB getRenderBoundingBox() {
        BlockPos sp = this.getPos().offset(Direction.DOWN);
        BlockPos ep = sp.offset(this.frontFacing.rotateY(), -width - 2).offset(Direction.UP, height);
        return new AABB(sp, ep);
    }

    @Override
    protected ModularUI createUI(Player entityPlayer) {
        if (!isActive()) {
            return super.createUI(entityPlayer);
        } else {
            WidgetScreenGrid[][] screenGrids = new WidgetScreenGrid[width][height];
            WidgetGroup group = new WidgetGroup();
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    screenGrids[i][j] = new WidgetScreenGrid(4 * width, 4 * height, i, j);
                    group.addWidget(screenGrids[i][j]);
                }
            }
            if (!this.getWorld().isClientSide) {
                this.getMultiblockParts().forEach(part->{
                    if (part instanceof MetaTileEntityMonitorScreen) {
                        int x = ((MetaTileEntityMonitorScreen) part).getX();
                        int y = ((MetaTileEntityMonitorScreen) part).getY();
                        screenGrids[x][y].setScreen((MetaTileEntityMonitorScreen) part);
                    }
                });
            } else {
                parts.forEach(partPos->{
                    TileEntity tileEntity = this.getWorld().getTileEntity(partPos);
                    if (tileEntity instanceof IGregTechTileEntity && ((IGregTechTileEntity) tileEntity).getMetaTileEntity() instanceof MetaTileEntityMonitorScreen) {
                        MetaTileEntityMonitorScreen part = (MetaTileEntityMonitorScreen) ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
                        int x = part.getX();
                        int y = part.getY();
                        screenGrids[x][y].setScreen(part);
                    }
                });
            }
            return ModularUI.builder(GuiTextures.BOXED_BACKGROUND, 28 * width, 28 * height)
                    .widget(group)
                    .build(this.getHolder(), entityPlayer);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level player, List<Component> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(Component.translatable("gregtech.multiblock.central_monitor.tooltip.1"));
        tooltip.add(Component.translatable("gregtech.multiblock.central_monitor.tooltip.2", MAX_WIDTH, MAX_HEIGHT));
        tooltip.add(Component.translatable("gregtech.multiblock.central_monitor.tooltip.3"));
        tooltip.add(Component.translatable("gregtech.multiblock.central_monitor.tooltip.4", -ENERGY_COST));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable Level world, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.tool_action.wrench.set_facing"));
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }
}
