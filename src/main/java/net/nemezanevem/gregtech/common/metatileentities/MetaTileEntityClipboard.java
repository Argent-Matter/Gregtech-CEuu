package net.nemezanevem.gregtech.common.metatileentities;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Vector3;
import io.netty.buffer.Unpooled;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.impl.FakeModularGui;
import net.nemezanevem.gregtech.api.item.metaitem.MetaItem;
import net.nemezanevem.gregtech.api.item.metaitem.stats.IItemBehaviour;
import net.nemezanevem.gregtech.api.blockentity.IFastRenderMetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.common.gui.impl.FakeModularUIContainerClipboard;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static codechicken.lib.raytracer.RayTracer.getEndVec;
import static codechicken.lib.raytracer.RayTracer.getStartVec;
import static net.nemezanevem.gregtech.api.capability.GregtechDataCodes.*;

public class MetaTileEntityClipboard extends MetaTileEntity implements IFastRenderMetaTileEntity {
    private static final AABB CLIPBOARD_AABB = new AABB(2.75 / 16.0, 0.0, 0.0, 13.25 / 16.0, 1.0, 0.4 / 16.0);
    public static final float scale = 1;
    public FakeModularGui guiCache;
    public FakeModularUIContainerClipboard guiContainerCache;
    private static final Cuboid6 pageBox = new Cuboid6(3 / 16.0, 0.25 / 16.0, 0.25 / 16.0, 13 / 16.0, 14.25 / 16.0, 0.3 / 16.0);
    private static final Tag NO_CLIPBOARD_SIG = IntTag.valueOf(0);
    private boolean didSetFacing = false;

    public MetaTileEntityClipboard(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public void tick() {
        super.tick();
        if (guiContainerCache == null) {
            createFakeGui();
            scheduleRenderUpdate();
        }
        if (this.getWorld().isClientSide) {
            if (guiCache != null)
                guiCache.updateScreen();
        }
        if (guiContainerCache != null)
            guiContainerCache.detectAndSendChanges();
    }

    @Override
    public int getLightOpacity() {
        return 0;
    }

    @Override
    public void renderMetaTileEntityFast(CCRenderState renderState, Matrix4 translation, float partialTicks) {
        CLIPBOARD_RENDERER.renderBoard(renderState, translation.copy(), new IVertexOperation[]{}, getFrontFacing(), this, partialTicks);
    }

    @Override
    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        if (this.getClipboard() != null)
            CLIPBOARD_RENDERER.renderGUI(x, y, z, this.getFrontFacing(), this, partialTicks);
    }

    public AABB getRenderBoundingBox() {
        return new AABB(getPos().offset(-1, 0, -1), getPos().offset(2, 2, 2));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        return null;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityClipboard(metaTileEntityId);
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        if (getClipboard().is(CLIPBOARD)) {
            List<IItemBehaviour> behaviours = ((MetaItem<?>) getClipboard().getItem()).getBehaviours(getClipboard());
            Optional<IItemBehaviour> clipboardBehaviour = behaviours.stream().filter((x) -> x instanceof ClipboardBehavior).findFirst();
            if (!clipboardBehaviour.isPresent())
                return null;
            if (clipboardBehaviour.get() instanceof ClipboardBehavior) {
                PlayerInventoryHolder holder = new PlayerInventoryHolder(new GregFakePlayer(entityPlayer.world), InteractionHand.MAIN_HAND); // We can't have this actually set the player's hand
                holder.setCustomValidityCheck(this::isValid).setCurrentItem(this.getClipboard());
                if (entityPlayer instanceof GregFakePlayer) { // This is how to tell if this is being called in-world or not
                    return ((ClipboardBehavior) clipboardBehaviour.get()).createMTEUI(holder, entityPlayer);
                } else {
                    return ((ClipboardBehavior) clipboardBehaviour.get()).createUI(holder, entityPlayer);
                }
            }
        }
        return null;
    }

    public void createFakeGui() {
        // Basically just the original function from the PluginBehavior, but with a lot of now useless stuff stripped out.
        try {
            GregFakePlayer fakePlayer = new GregFakePlayer(this.getWorld());
            fakePlayer.setHeldItem(InteractionHand.MAIN_HAND, this.getClipboard());
            ModularUI ui = this.createUI(fakePlayer);

            ModularUI.Builder builder = new ModularUI.Builder(ui.backgroundPath, ui.getWidth(), ui.getHeight());
            builder.shouldColor(false);

            List<Widget> widgets = new ArrayList<>(ui.guiWidgets.values());

            for (Widget widget : widgets) {
                builder.widget(widget);
            }
            ui = builder.build(ui.holder, ui.entityPlayer);
            FakeModularUIContainerClipboard fakeModularUIContainer = new FakeModularUIContainerClipboard(ui, this);
            this.guiContainerCache = fakeModularUIContainer;
            if (getWorld().isClientSide)
                this.guiCache = new FakeModularGui(ui, fakeModularUIContainer);
            this.writeCustomData(CREATE_FAKE_UI, buffer -> {});
        } catch (Exception e) {
            GTLog.logger.error(e);
        }
    }


    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.itemInventory = new InaccessibleItemStackHandler();
    }

    public ItemStack getClipboard() {
        if (this.itemInventory.getStackInSlot(0) == ItemStack.EMPTY) {
            ((InaccessibleItemStackHandler) this.itemInventory).setStackInSlot(0, CLIPBOARD.getStackForm());
        }
        return this.itemInventory.getStackInSlot(0);
    }

    public void initializeClipboard(ItemStack stack) {
        ((InaccessibleItemStackHandler) this.itemInventory).setStackInSlot(0, stack.copy());
        writeCustomData(INIT_CLIPBOARD_NBT, buf -> {
            buf.writeCompoundTag(stack.getTagCompound());
        });
    }

    public void setClipboard(ItemStack stack) {
        ((InaccessibleItemStackHandler) this.itemInventory).setStackInSlot(0, stack.copy());
    }

    @Override
    public void getDrops(NonNullList<ItemStack> dropsList, @Nullable Player harvester) {
        dropsList.clear();
        dropsList.add(this.getClipboard());
    }

    @Override
    public float getBlockHardness() {
        return 100;
    }

    @Override
    public int getHarvestLevel() {
        return 4;
    }

    @Override
    public boolean onRightClick(Player playerIn, InteractionHand hand, Direction facing, VoxelShapeBlockHitResult hitResult) {
        if (!playerIn.isSneaking()) {
            if (getWorld() != null && !getWorld().isClientSide) {
                MetaTileEntityUIFactory.INSTANCE.openUI(getHolder(), (PlayerMP) playerIn);
            }
        } else {
            breakClipboard(playerIn);
        }
        return true;
    }

    @Override
    public boolean onWrenchClick(Player playerIn, InteractionHand hand, Direction wrenchSide, VoxelShapeBlockHitResult hitResult) {
        return false;
    }

    private void breakClipboard(@Nullable Player player) {
        if (!getWorld().isClientSide) {
            BlockPos pos = this.getPos(); // Saving this for later so it doesn't get mangled
            Level world = this.getWorld(); // Same here

            NonNullList<ItemStack> drops = NonNullList.create();
            getDrops(drops, player);

            Block.spawnAsEntity(getWorld(), pos, drops.get(0));
            this.dropAllCovers();
            this.onRemoval();

            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
        }
    }

    @Override
    public void onNeighborChanged() {
        if (!getWorld().isClientSide && didSetFacing) {
            BlockPos pos = getPos().offset(getFrontFacing());
            BlockState state = getWorld().getBlockState(pos);
            if (state.getBlock().isAir(state, getWorld(), pos) || !state.isSideSolid(getWorld(), pos, getFrontFacing())) {
                breakClipboard(null);
            }
        }
    }

    @Override
    public void setFrontFacing(Direction frontFacing) {
        super.setFrontFacing(frontFacing);
        this.didSetFacing = true;
    }

    @Override
    public String getHarvestTool() {
        return ToolClasses.AXE;
    }

    @Override
    public void addCollisionBoundingBox(List<IndexedCuboid6> collisionList) {
        collisionList.add(new IndexedCuboid6(null, Util.rotateAroundYAxis(CLIPBOARD_AABB, Direction.NORTH, this.getFrontFacing())));
    }

    public IndexedCuboid6 getPageCuboid() {
        return new IndexedCuboid6(null, Util.rotateAroundYAxis(pageBox.aabb(), Direction.NORTH, this.getFrontFacing()));
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(CLIPBOARD_RENDERER.getParticleTexture(), 0xFFFFFF);
    }

    public Pair<Double, Double> checkLookingAt(Player player) {
        if (this.getWorld() != null && player != null) {
            Vec3d startVec = getStartVec(player);
            Vec3d endVec = getEndVec(player);
            VoxelShapeBlockHitResult rayTraceResult = rayTrace(this.getPos(), new Vector3(startVec), new Vector3(endVec), getPageCuboid());
            if (rayTraceResult != null && rayTraceResult.sideHit == this.getFrontFacing().getOpposite()) {
                TileEntity tileEntity = this.getWorld().getTileEntity(rayTraceResult.getBlockPos());
                if (tileEntity instanceof IGregTechTileEntity && ((IGregTechTileEntity) tileEntity).getMetaTileEntity() instanceof MetaTileEntityClipboard) {
                    double[] pos = handleRayTraceResult(rayTraceResult, this.getFrontFacing().getOpposite());
                    if (pos[0] >= 0 && pos[0] <= 1 && pos[1] >= 0 && pos[1] <= 1)
                        return Pair.of(pos[0], pos[1]);
                }
            }
        }
        return null;
    }

    private double[] handleRayTraceResult(VoxelShapeBlockHitResult rayTraceResult, Direction spin) {
        double x, y;
        double dX = rayTraceResult.sideHit.getAxis() == Direction.Axis.X
                ? rayTraceResult.hitVec.z - rayTraceResult.getBlockPos().getZ()
                : rayTraceResult.hitVec.x - rayTraceResult.getBlockPos().getX();
        double dY = rayTraceResult.sideHit.getAxis() == Direction.Axis.Y
                ? rayTraceResult.hitVec.z - rayTraceResult.getBlockPos().getZ()
                : rayTraceResult.hitVec.y - rayTraceResult.getBlockPos().getY();
        if (spin == Direction.NORTH) {
            x = 1 - dX;
        } else if (spin == Direction.SOUTH) {
            x = dX;
        } else if (spin == Direction.EAST) {
            x = 1 - dX;
            if (rayTraceResult.sideHit.getXOffset() < 0 || rayTraceResult.sideHit.getZOffset() > 0) {
                x = 1 - x;
            }
        } else {
            x = 1 - dX;
            if (rayTraceResult.sideHit.getXOffset() < 0 || rayTraceResult.sideHit.getZOffset() > 0) {
                x = 1 - x;
            }
        }

        y = 1 - dY; // Since y values are quite weird here

        // Scale these to be 0 - 1
        x -= 3.0 / 16;
        y -= 1.75 / 16;
        x /= 14.0 / 16;
        y /= 14.0 / 16;

        return new double[]{x, y};
    }

    @Override
    public boolean isValidFrontFacing(Direction facing) {
        return false;
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        if (this.getClipboard() != null && this.getClipboard().getTagCompound() != null)
            data.setTag("clipboardNBT", this.getClipboard().getTagCompound());
        else
            data.setTag("clipboardNBT", NO_CLIPBOARD_SIG);
        return data;
    }


    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        NBTBase clipboardNBT = data.getTag("clipboardNBT");
        if (clipboardNBT != NO_CLIPBOARD_SIG && clipboardNBT instanceof CompoundTag) {
            ItemStack clipboard = this.getClipboard();
            clipboard.setTagCompound((CompoundTag) clipboardNBT);
            this.setClipboard(clipboard);
        }
    }

    public void setClipboardNBT(CompoundTag data) {
        ItemStack clipboard = this.getClipboard();
        clipboard.setTagCompound(data);
        this.setClipboard(clipboard);
    }

    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        super.writeInitialSyncData(buf);
        if (this.getClipboard() != null && this.getClipboard().getTagCompound() != null)
            buf.writeCompoundTag(this.getClipboard().getTagCompound());
        else {
            buf.writeCompoundTag(new CompoundTag());
        }
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        super.receiveInitialSyncData(buf);
        try {
            CompoundTag clipboardNBT = buf.readCompoundTag();
            if (clipboardNBT != null && !clipboardNBT.equals(new CompoundTag())) {
                ItemStack clipboard = this.getClipboard();
                clipboard.setTagCompound(clipboardNBT);
                this.setClipboard(clipboard);
            }
        } catch (Exception e) {
            GTLog.logger.error("Could not initialize Clipboard from InitialSyncData buffer", e);
        }
    }

    @Override
    public void receiveCustomData(int dataId, FriendlyByteBuf buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId >= UPDATE_UI) {
            int windowID = buf.readVarInt();
            int widgetID = buf.readVarInt();
            if (guiCache != null)
                guiCache.handleWidgetUpdate(windowID, widgetID, buf);
            this.scheduleRenderUpdate();
            this.sendNBTToServer();
        } else if (dataId == CREATE_FAKE_UI) {
            createFakeGui();
            this.scheduleRenderUpdate();
        } else if (dataId == MOUSE_POSITION) {
            int mouseX = buf.readVarInt();
            int mouseY = buf.readVarInt();
            if (guiCache != null && guiContainerCache != null) {
                guiCache.mouseClicked(mouseX, mouseY, 0); // Left mouse button
            }
            this.scheduleRenderUpdate();
            this.sendNBTToServer();
        } else if (dataId == INIT_CLIPBOARD_NBT) {
            try {
                CompoundTag clipboardNBT = buf.readCompoundTag();
                if (clipboardNBT != NO_CLIPBOARD_SIG) {
                    ItemStack clipboard = this.getClipboard();
                    clipboard.setTagCompound(clipboardNBT);
                    this.setClipboard(clipboard);
                }
            } catch (Exception e) {
                GTLog.logger.error("Could not read Clipboard Init NBT from CustomData buffer", e);
            }
        }
    }

    private void sendNBTToServer() {
        FriendlyByteBuf packetBuffer = new FriendlyByteBuf(Unpooled.buffer());
        packetBuffer.writeCompoundTag(this.getClipboard().getTagCompound());
        GregTechAPI.networkHandler.sendToServer(new PacketClipboardNBTUpdate(
                this.getWorld().provider.getDimension(),
                this.getPos(),
                1, packetBuffer));
    }


    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
    }

    public void readUIAction(ServerPlayer player, int id, FriendlyByteBuf buf) {
        if (id == 1) {
            if (this.guiContainerCache != null) {
                guiContainerCache.handleClientAction(buf);
            }
        }
    }

    @Override
    public void onLeftClick(Player player, Direction facing, VoxelShapeBlockHitResult hitResult) {
        if (this.getWorld().isClientSide) return;
        Pair<Double, Double> clickCoords = this.checkLookingAt(player);
        int width = 178; // These should always be correct.
        int height = 230;
        double scale = 1.0 / Math.max(width, height);
        int mouseX = (int) ((clickCoords.getLeft() / scale));
        int mouseY = (int) ((clickCoords.getRight() / scale));
        if (0 <= mouseX && mouseX <= width && 0 <= mouseY && mouseY <= height) {
            this.writeCustomData(MOUSE_POSITION, buf -> {
                buf.writeVarInt(mouseX);
                buf.writeVarInt(mouseY);
            });
        }
    }

    @Override
    public boolean canPlaceCoverOnSide(Direction side) {
        return false;
    }

    @Override
    public boolean canRenderMachineGrid() {
        return false;
    }

    @Override
    public boolean showToolUsages() {
        return false;
    }

    @Override
    public ItemStack getPickItem(VoxelShapeBlockHitResult result, Player player) {
        return this.getClipboard();
    }
}
