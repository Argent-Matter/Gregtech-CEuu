package net.nemezanevem.gregtech.api.cover;


import codechicken.lib.raytracer.VoxelShapeBlockHitResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.google.common.collect.Lists;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.nemezanevem.gregtech.api.gui.IUIHolder;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import net.nemezanevem.gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import org.checkerframework.checker.units.qual.C;

import java.util.List;
import java.util.function.Consumer;

/**
 * Represents cover instance attached on the specific side of meta tile entity
 * Cover filters out interaction and logic of meta tile entity
 * <p>
 * Can implement {@link BlockEntityTicker <CoverBehavior>} to listen to meta tile entity updates
 */
@SuppressWarnings("unused")
public abstract class CoverBehavior implements IUIHolder {

    private CoverDefinition coverDefinition;
    public final ICoverable coverHolder;
    public final Direction attachedSide;
    private int redstoneSignalOutput;

    public CoverBehavior(ICoverable coverHolder, Direction attachedSide) {
        this.coverHolder = coverHolder;
        this.attachedSide = attachedSide;
    }

    final void setCoverDefinition(CoverDefinition coverDefinition) {
        this.coverDefinition = coverDefinition;
    }

    public final CoverDefinition getCoverDefinition() {
        return coverDefinition;
    }

    public final void setRedstoneSignalOutput(int redstoneSignalOutput) {
        this.redstoneSignalOutput = redstoneSignalOutput;
        coverHolder.notifyBlockUpdate();
        coverHolder.markDirty();
    }

    public final int getRedstoneSignalOutput() {
        return redstoneSignalOutput;
    }

    public final int getRedstoneSignalInput() {
        return coverHolder.getInputRedstoneSignal(attachedSide, true);
    }

    public void onRedstoneInputSignalChange(int newSignalStrength) {
    }

    public boolean canConnectRedstone() {
        return false;
    }

    public CompoundTag writeToNBT(CompoundTag tagCompound) {
        if (redstoneSignalOutput > 0) {
            tagCompound.putInt("RedstoneSignal", redstoneSignalOutput);
        }

        return tagCompound;
    }

    public void readFromNBT(CompoundTag tagCompound) {
        this.redstoneSignalOutput = tagCompound.getInt("RedstoneSignal");
    }

    public void writeInitialSyncData(FriendlyByteBuf packetBuffer) {
    }

    public void readInitialSyncData(FriendlyByteBuf packetBuffer) {
    }

    public void readUpdateData(int id, FriendlyByteBuf packetBuffer) {
    }

    public final void writeUpdateData(Consumer<FriendlyByteBuf> writer) {
        coverHolder.writeCoverData(this, writer);
    }

    /**
     * Called on server side to check whether cover can be attached to given meta tile entity
     *
     * @return true if cover can be attached, false otherwise
     */
    public abstract boolean canAttach();

    public abstract boolean isTickable();

    /**
     * Will be called on server side after the cover attachment to the meta tile entity
     * Cover can change it's internal state here and it will be synced to client with {@link #writeInitialSyncData(FriendlyByteBuf)}
     *
     * @param itemStack the item cover was attached from
     */
    public void onAttached(ItemStack itemStack) {
    }

    public void onAttached(ItemStack itemStack, Player player) {
        onAttached(itemStack);
    }

    public boolean shouldCoverInteractWithOutputside() {
        return false;
    }

    public ItemStack getPickItem() {
        return coverDefinition.getDropItemStack();
    }

    public List<ItemStack> getDrops() {
        return Lists.newArrayList(getPickItem());
    }

    /**
     * Called prior to cover removing on the server side
     * Will also be called during machine dismantling, as machine loses installed covers after that
     */
    public void onRemoved() {
    }

    /**
     * @return If the pipe this is placed on should render a connection to the cover
     */
    public boolean shouldAutoConnect() {
        return true;
    }

    /**
     * @return If the pipe this is placed on and a pipe on the other side should be able to connect
     */
    public boolean canPipePassThrough() {
        return false;
    }

    public boolean canRenderBackside() {
        return true;
    }

    public boolean onLeftClick(Player entityPlayer, VoxelShapeBlockHitResult hitResult) {
        return false;
    }

    public InteractionResult onRightClick(Player playerIn, InteractionHand hand, VoxelShapeBlockHitResult hitResult) {
        return InteractionResult.PASS;
    }

    public InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, VoxelShapeBlockHitResult hitResult) {
        return InteractionResult.PASS;
    }

    public InteractionResult onSoftMalletClick(Player playerIn, InteractionHand hand, VoxelShapeBlockHitResult hitResult) {
        return InteractionResult.PASS;
    }

    /**
     * Will be called for each capability request to meta tile entity
     * Cover can override meta tile entity capabilities, modify their values, or deny accessing them
     *
     * @param defaultValue value of the capability from meta tile entity itself
     * @return result capability value external caller will receive
     */
    public <T> LazyOptional<T> getCapability(Capability<T> capability, LazyOptional<T> defaultValue) {
        return defaultValue;
    }

    /**
     * Called on client side to render this cover on the machine's face
     * It will be automatically translated to prevent Z-fighting with machine faces
     */
    public abstract void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, VoxelShape plateBox, RenderType layer);

    public boolean canRenderInLayer(RenderType renderLayer) {
        return renderLayer == RenderType.cutoutMipped() || renderLayer == BloomEffectUtil.getRealBloomLayer();
    }

    public void renderCoverPlate(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, VoxelShape plateBox, RenderType layer) {
        TextureAtlasSprite casingSide = getPlateSprite();
        for (Direction coverPlateSide : Direction.values()) {
            boolean isAttachedSide = attachedSide.getAxis() == coverPlateSide.getAxis();
            if (isAttachedSide || coverHolder.getCoverAtSide(coverPlateSide) == null) {
                for(AABB bounds : plateBox.toAabbs()) {
                    Textures.renderFace(renderState, translation, pipeline, coverPlateSide, new Cuboid6(bounds), casingSide, RenderType.cutoutMipped());
                }
            }
        }
    }

    protected TextureAtlasSprite getPlateSprite() {
        return Textures.VOLTAGE_CASINGS[GTValues.LV].getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.SIDE);
    }

    @Override
    public final boolean isValid() {
        return coverHolder.isValid() && coverHolder.getCoverAtSide(attachedSide) == this;
    }

    @Override
    public boolean isClientSide() {
        return coverHolder.getWorld().isClientSide;
    }

    @Override
    public final void markAsDirty() {
        coverHolder.markDirty();
    }
}
