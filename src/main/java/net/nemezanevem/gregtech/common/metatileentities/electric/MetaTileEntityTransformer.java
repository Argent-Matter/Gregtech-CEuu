package net.nemezanevem.gregtech.common.metatileentities.electric;

import codechicken.lib.raytracer.VoxelShapeBlockHitResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.TieredMetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.capability.impl.EnergyContainerHandler;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.util.PipelineUtil;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import net.nemezanevem.gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import static net.nemezanevem.gregtech.api.capability.GregtechDataCodes.SYNC_TILE_MODE;

public class MetaTileEntityTransformer extends TieredMetaTileEntity {

    private boolean isTransformUp;

    public MetaTileEntityTransformer(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityTransformer(metaTileEntityId, getTier());
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.putBoolean("Inverted", isTransformUp);
        return data;
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        this.isTransformUp = data.getBoolean("Inverted");
        reinitializeEnergyContainer();
    }

    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isTransformUp);
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        super.receiveInitialSyncData(buf);
        this.isTransformUp = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, FriendlyByteBuf buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == SYNC_TILE_MODE) {
            this.isTransformUp = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    public boolean isInverted() {
        return isTransformUp;
    }

    public void setTransformUp(boolean inverted) {
        isTransformUp = inverted;
        if (!getWorld().isClientSide) {
            reinitializeEnergyContainer();
            writeCustomData(SYNC_TILE_MODE, b -> b.writeBoolean(isTransformUp));
            notifyBlockUpdate();
            markDirty();
        }
    }

    @Override
    protected void reinitializeEnergyContainer() {
        long tierVoltage = GTValues.V[getTier()];
        if (isTransformUp) {
            //storage = 1 amp high; input = tier / 4; amperage = 4; output = tier; amperage = 1
            this.energyContainer = new EnergyContainerHandler(this, tierVoltage * 8L, tierVoltage, 4, tierVoltage * 4, 1);
            ((EnergyContainerHandler) this.energyContainer).setSideInputCondition(s -> s != getFrontFacing());
            ((EnergyContainerHandler) this.energyContainer).setSideOutputCondition(s -> s == getFrontFacing());
        } else {
            //storage = 1 amp high; input = tier; amperage = 1; output = tier / 4; amperage = 4
            this.energyContainer = new EnergyContainerHandler(this, tierVoltage * 8L, tierVoltage * 4, 1, tierVoltage, 4);
            ((EnergyContainerHandler) this.energyContainer).setSideInputCondition(s -> s == getFrontFacing());
            ((EnergyContainerHandler) this.energyContainer).setSideOutputCondition(s -> s != getFrontFacing());
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);

        SimpleOverlayRenderer otherFaceTexture = isTransformUp ? Textures.ENERGY_IN : Textures.ENERGY_OUT;
        SimpleOverlayRenderer frontFaceTexture = isTransformUp ? Textures.ENERGY_OUT_MULTI : Textures.ENERGY_IN_MULTI;
        frontFaceTexture.renderSided(frontFacing, renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[getTier() + 1]));
        Arrays.stream(Direction.values()).filter(f -> f != frontFacing)
                .forEach((f -> otherFaceTexture.renderSided(f, renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[getTier()]))));
    }


    @Override
    public boolean isValidFrontFacing(Direction facing) {
        return true;
    }

    @Override
    public boolean onSoftMalletClick(Player playerIn, InteractionHand hand, Direction facing, VoxelShapeBlockHitResult hitResult) {
        if (getWorld().isClientSide) {
            scheduleRenderUpdate();
            return true;
        }
        if (isTransformUp) {
            setTransformUp(false);
            playerIn.sendSystemMessage(Component.translatable("gregtech.machine.transformer.message_transform_down",
                    energyContainer.getInputVoltage(), energyContainer.getInputAmperage(), energyContainer.getOutputVoltage(), energyContainer.getOutputAmperage()));
        } else {
            setTransformUp(true);
            playerIn.sendSystemMessage(Component.translatable("gregtech.machine.transformer.message_transform_up",
                    energyContainer.getInputVoltage(), energyContainer.getInputAmperage(), energyContainer.getOutputVoltage(), energyContainer.getOutputAmperage()));
        }
        return true;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    protected ModularUI createUI(Player entityPlayer) {
        return null;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level player, List<Component> tooltip, boolean advanced) {
        String lowerTierName = GTValues.VNF[getTier()];
        String higherTierName = GTValues.VNF[getTier() + 1];
        long lowerVoltage = energyContainer.getOutputVoltage();
        long higherVoltage = energyContainer.getInputVoltage();
        long lowerAmperage = energyContainer.getInputAmperage();
        long higherAmperage = energyContainer.getOutputAmperage();

        tooltip.add(Component.translatable("gregtech.machine.transformer.description"));
        tooltip.add(Component.translatable("gregtech.machine.transformer.tooltip_tool_usage"));
        tooltip.add(Component.translatable("gregtech.machine.transformer.tooltip_transform_down", lowerAmperage, higherVoltage, higherTierName, higherAmperage, lowerVoltage, lowerTierName));
        tooltip.add(Component.translatable("gregtech.machine.transformer.tooltip_transform_up", higherAmperage, lowerVoltage, lowerTierName, lowerAmperage, higherVoltage, higherTierName));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable Level world, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(Component.translatable("gregtech.tool_action.wrench.set_facing"));
        tooltip.add(Component.translatable("gregtech.tool_action.soft_mallet.toggle_mode"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }
}
