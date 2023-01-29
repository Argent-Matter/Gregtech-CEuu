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
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.capability.impl.EnergyContainerHandler;
import net.nemezanevem.gregtech.api.util.PipelineUtil;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import net.nemezanevem.gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import static net.nemezanevem.gregtech.api.capability.GregtechDataCodes.AMP_INDEX;

public class MetaTileEntityAdjustableTransformer extends MetaTileEntityTransformer {

    private static final int[] hiAmpsRange = {1, 2, 4, 16};
    private static final int[] loAmpsRange = {4, 8, 16, 64};
    private int ampIndex;

    public MetaTileEntityAdjustableTransformer(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.ampIndex = 2;
        reinitializeEnergyContainer();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityAdjustableTransformer(metaTileEntityId, getTier());
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.putInt("ampIndex", ampIndex);
        return data;
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        this.ampIndex = data.getInt("ampIndex");
        reinitializeEnergyContainer();
    }

    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(ampIndex);
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        super.receiveInitialSyncData(buf);
        this.ampIndex = buf.readInt();
    }

    @Override
    public void receiveCustomData(int dataId, FriendlyByteBuf buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == AMP_INDEX) {
            this.ampIndex = buf.readInt();
            scheduleRenderUpdate();
        }
    }

    protected void incrementAmpIndex() {
        this.ampIndex = (this.ampIndex + 1) % hiAmpsRange.length;
        if (!getWorld().isClientSide) {
            reinitializeEnergyContainer();
            writeCustomData(AMP_INDEX, b -> b.writeInt(ampIndex));
            notifyBlockUpdate();
            markDirty();
        }
    }

    @Override
    protected void reinitializeEnergyContainer() {
        long tierVoltage = GTValues.V[getTier()];
        if (isInverted()) {
            //storage = 1 amp high; input = tier / 4; amperage = loAmpsRange[ampIndex]; output = tier; amperage = hiAmpsRange[ampIndex]
            this.energyContainer = new EnergyContainerHandler(this, tierVoltage * 128L, tierVoltage, loAmpsRange[ampIndex], tierVoltage * 4, hiAmpsRange[ampIndex]);
            ((EnergyContainerHandler) this.energyContainer).setSideInputCondition(s -> s != getFrontFacing());
            ((EnergyContainerHandler) this.energyContainer).setSideOutputCondition(s -> s == getFrontFacing());
        } else {
            //storage = 1 amp high; input = tier; amperage = hiAmpsRange[ampIndex]; output = tier / 4; amperage = loAmpsRange[ampIndex]
            this.energyContainer = new EnergyContainerHandler(this, tierVoltage * 128L, tierVoltage * 4, hiAmpsRange[ampIndex], tierVoltage, loAmpsRange[ampIndex]);
            ((EnergyContainerHandler) this.energyContainer).setSideInputCondition(s -> s == getFrontFacing());
            ((EnergyContainerHandler) this.energyContainer).setSideOutputCondition(s -> s != getFrontFacing());
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);

        SimpleOverlayRenderer otherFaceTexture;
        SimpleOverlayRenderer frontFaceTexture;
        switch (this.ampIndex) {
            case 1:
                otherFaceTexture = isInverted() ? Textures.ENERGY_IN_MULTI : Textures.ENERGY_OUT_MULTI;
                frontFaceTexture = isInverted() ? Textures.ENERGY_IN_HI : Textures.ENERGY_IN_HI;
                break;
            case 2:
                otherFaceTexture = isInverted() ? Textures.ENERGY_IN_HI : Textures.ENERGY_OUT_HI;
                frontFaceTexture = isInverted() ? Textures.ENERGY_OUT_ULTRA : Textures.ENERGY_IN_ULTRA;
                break;
            case 3:
                otherFaceTexture = isInverted() ? Textures.ENERGY_IN_ULTRA : Textures.ENERGY_OUT_ULTRA;
                frontFaceTexture = isInverted() ? Textures.ENERGY_OUT_ULTRA : Textures.ENERGY_IN_ULTRA;
                break;
            default:
                otherFaceTexture = isInverted() ? Textures.ENERGY_IN : Textures.ENERGY_OUT;
                frontFaceTexture = isInverted() ? Textures.ENERGY_OUT_MULTI : Textures.ENERGY_IN_MULTI;
        }

        frontFaceTexture.renderSided(frontFacing, renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[getTier() + 1]));
        Arrays.stream(Direction.values()).filter(f -> f != frontFacing)
                .forEach((f -> otherFaceTexture.renderSided(f, renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[getTier()]))));
    }

    @Override
    public boolean onScrewdriverClick(Player playerIn, InteractionHand hand, Direction facing, VoxelShapeBlockHitResult hitResult) {
        if (getWorld().isClientSide) {
            scheduleRenderUpdate();
            return true;
        }

        incrementAmpIndex();
        playerIn.sendSystemMessage(Component.translatable("gregtech.machine.transformer_adjustable.message_adjust",
                energyContainer.getInputVoltage(), energyContainer.getInputAmperage(), energyContainer.getOutputVoltage(), energyContainer.getOutputAmperage()));

        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level player, List<Component> tooltip, boolean advanced) {
        String lowerTierName = GTValues.VNF[getTier()];
        String higherTierName = GTValues.VNF[getTier() + 1];
        long lowerVoltage = energyContainer.getOutputVoltage();
        long higherVoltage = energyContainer.getInputVoltage();
        long lowerAmperage = energyContainer.getInputAmperage();
        long higherAmperage = energyContainer.getOutputAmperage();

        tooltip.add(Component.translatable("gregtech.machine.transformer_adjustable.description"));
        tooltip.add(Component.translatable("gregtech.machine.transformer.tooltip_tool_usage"));
        tooltip.add(Component.translatable("gregtech.machine.transformer_adjustable.tooltip_tool_usage"));
        tooltip.add(Component.translatable("gregtech.machine.transformer.tooltip_transform_down", lowerAmperage, higherVoltage, higherTierName, higherAmperage, lowerVoltage, lowerTierName));
        tooltip.add(Component.translatable("gregtech.machine.transformer.tooltip_transform_up", higherAmperage, lowerVoltage, lowerTierName, lowerAmperage, higherVoltage, higherTierName));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable Level world, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.tool_action.screwdriver.toggle_mode_covers"));
        tooltip.add(Component.translatable("gregtech.tool_action.wrench.set_facing"));
        tooltip.add(Component.translatable("gregtech.tool_action.soft_mallet.toggle_mode"));
        tooltip.add(Component.translatable("gregtech.tool_action.crowbar"));
    }
}
