package net.nemezanevem.gregtech.common.metatileentities.multi.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.nemezanevem.gregtech.api.blockentity.ITieredMetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.multiblock.IMultiblockPart;
import net.nemezanevem.gregtech.api.blockentity.multiblock.MultiblockControllerBase;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.client.renderer.ICubeRenderer;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import net.nemezanevem.gregtech.client.renderer.texture.custom.FireboxActiveRenderer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import static net.nemezanevem.gregtech.api.capability.GregtechDataCodes.SYNC_CONTROLLER;

public abstract class MetaTileEntityMultiblockPart extends MetaTileEntity implements IMultiblockPart, ITieredMetaTileEntity {

    private final int tier;
    private BlockPos controllerPos;
    private MultiblockControllerBase controllerTile;
    protected ICubeRenderer hatchTexture = null;

    public MetaTileEntityMultiblockPart(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId);
        this.tier = tier;
        initializeInventory();
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(getBaseTexture().getParticleSprite(), getPaintingColorForRendering());
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        ICubeRenderer baseTexture = getBaseTexture();
        if (baseTexture instanceof FireboxActiveRenderer) {
            baseTexture.renderOriented(renderState, translation, ArrayUtils.add(pipeline,
                    new ColourMultiplier(Util.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()))), getFrontFacing());
        } else {
            baseTexture.render(renderState, translation, ArrayUtils.add(pipeline,
                    new ColourMultiplier(Util.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()))));
        }
    }

    public int getTier() {
        return tier;
    }

    public MultiblockControllerBase getController() {
        if (getWorld() != null && getWorld().isClientSide) { //check this only clientside
            if (controllerTile == null && controllerPos != null) {
                this.controllerTile = (MultiblockControllerBase) Util.getMetaTileEntity(getWorld(), controllerPos);
            }
        }
        if (controllerTile != null && (controllerTile.getHolder() == null ||
                !controllerTile.isValid() || !(getWorld().isClientSide || controllerTile.getMultiblockParts().contains(this)))) {
            //tile can become invalid for many reasons, and can also forgot to remove us once we aren't in structure anymore
            //so check it here to prevent bugs with dangling controller reference and wrong texture
            this.controllerTile = null;
        }
        return controllerTile;
    }

    public ICubeRenderer getBaseTexture() {
        MultiblockControllerBase controller = getController();
        if (controller != null) {
            return this.hatchTexture = controller.getBaseTexture(this);
        } else if (this.hatchTexture != null) {
            if (hatchTexture != Textures.getInactiveTexture(hatchTexture)) {
                return this.hatchTexture = Textures.getInactiveTexture(hatchTexture);
            }
            return this.hatchTexture;
        } else {
            return Textures.VOLTAGE_CASINGS[tier];
        }
    }

    public boolean shouldRenderOverlay() {
        MultiblockControllerBase controller = getController();
        return controller == null || controller.shouldRenderOverlay(this);
    }

    @Override
    public boolean isValidFrontFacing(Direction facing) {
        return true;
    }

    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        super.writeInitialSyncData(buf);
        MultiblockControllerBase controller = getController();
        buf.writeBoolean(controller != null);
        if (controller != null) {
            buf.writeBlockPos(controller.getPos());
        }
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        super.receiveInitialSyncData(buf);
        if (buf.readBoolean()) {
            this.controllerPos = buf.readBlockPos();
            this.controllerTile = null;
        }
    }

    @Override
    public void receiveCustomData(int dataId, FriendlyByteBuf buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == SYNC_CONTROLLER) {
            if (buf.readBoolean()) {
                this.controllerPos = buf.readBlockPos();
                this.controllerTile = null;
            } else {
                this.controllerPos = null;
                this.controllerTile = null;
            }
            scheduleRenderUpdate();
        }
    }

    private void setController(MultiblockControllerBase controller1) {
        this.controllerTile = controller1;
        if (!getWorld().isClientSide) {
            writeCustomData(SYNC_CONTROLLER, writer -> {
                writer.writeBoolean(controllerTile != null);
                if (controllerTile != null) {
                    writer.writeBlockPos(controllerTile.getPos());
                }
            });
        }
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        MultiblockControllerBase controller = getController();
        if (!getWorld().isClientSide && controller != null) {
            controller.invalidateStructure();
        }
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        setController(controllerBase);
        scheduleRenderUpdate();
    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        setController(null);
        scheduleRenderUpdate();
    }

    @Override
    public boolean isAttachedToMultiBlock() {
        return getController() != null;
    }

    @Override
    public int getDefaultPaintingColor() {
        return !isAttachedToMultiBlock() && hatchTexture == null ? super.getDefaultPaintingColor() : 0xFFFFFF;
    }
}
