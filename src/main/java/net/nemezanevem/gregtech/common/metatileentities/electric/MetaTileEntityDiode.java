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
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.blockentity.multiblock.GtMultiblockAbilities;
import net.nemezanevem.gregtech.api.blockentity.multiblock.IMultiblockAbilityPart;
import net.nemezanevem.gregtech.api.blockentity.multiblock.IPassthroughHatch;
import net.nemezanevem.gregtech.api.blockentity.multiblock.MultiblockAbility;
import net.nemezanevem.gregtech.api.capability.IEnergyContainer;
import net.nemezanevem.gregtech.api.capability.impl.EnergyContainerHandler;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.util.PipelineUtil;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import net.nemezanevem.gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import static net.nemezanevem.gregtech.api.capability.GregtechDataCodes.AMP_INDEX;

public class MetaTileEntityDiode extends MetaTileEntityMultiblockPart implements IPassthroughHatch, IMultiblockAbilityPart<IPassthroughHatch> {

    protected IEnergyContainer energyContainer;

    private static final String AMP_NBT_KEY = "amp_mode";
    private int amps;

    public MetaTileEntityDiode(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        amps = 1;
        reinitializeEnergyContainer();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityDiode(metaTileEntityId, getTier());
    }

    @Override
    public int getActualComparatorValue() {
        long energyStored = energyContainer.getEnergyStored();
        long energyCapacity = energyContainer.getEnergyCapacity();
        float f = energyCapacity == 0L ? 0.0f : energyStored / (energyCapacity * 1.0f);
        return Mth.floor(f * 14.0f) + (energyStored > 0 ? 1 : 0);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.putInt(AMP_NBT_KEY, amps);
        return data;
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        this.amps = data.getInt(AMP_NBT_KEY);
        reinitializeEnergyContainer();
    }

    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(amps);
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        super.receiveInitialSyncData(buf);
        this.amps = buf.readInt();
    }

    @Override
    public void receiveCustomData(int dataId, FriendlyByteBuf buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == AMP_INDEX) {
            this.amps = buf.readInt();
        }
    }

    private void setAmpMode() {
        amps = amps == getMaxAmperage() ? 1 : amps << 1;
        if (!getWorld().isClientSide) {
            reinitializeEnergyContainer();
            writeCustomData(AMP_INDEX, b -> b.writeInt(amps));
            notifyBlockUpdate();
            markDirty();
        }
    }

    /** Change this value (or override) to make the Diode able to handle more amps. Must be a power of 2 */
    protected int getMaxAmperage() {
        return 16;
    }

    protected void reinitializeEnergyContainer() {
        long tierVoltage = GTValues.V[getTier()];
        this.energyContainer = new EnergyContainerHandler(this, tierVoltage * 16, tierVoltage, amps, tierVoltage, amps);
        ((EnergyContainerHandler) this.energyContainer).setSideInputCondition(s -> s != getFrontFacing());
        ((EnergyContainerHandler) this.energyContainer).setSideOutputCondition(s -> s == getFrontFacing());
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.ENERGY_IN_MULTI.renderSided(getFrontFacing(), renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[getTier()]));
        Arrays.stream(Direction.values()).filter(f -> f != frontFacing).forEach(f ->
                Textures.ENERGY_OUT.renderSided(f, renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[getTier()])));
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
        setAmpMode();
        playerIn.sendSystemMessage(Component.translatable("gregtech.machine.diode.message", amps));
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
    public void addInformation(ItemStack stack, @Nullable Level player, @Nonnull List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.machine.diode.tooltip_general"));
        tooltip.add(Component.translatable("gregtech.machine.diode.tooltip_starts_at"));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.voltage_in_out", energyContainer.getInputVoltage(), GTValues.VNF[getTier()]));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.amperage_in_out_till", getMaxAmperage()));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable Level world, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(Component.translatable("gregtech.tool_action.wrench.set_facing"));
        tooltip.add(Component.translatable("gregtech.tool_action.soft_mallet.toggle_mode"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public MultiblockAbility<IPassthroughHatch> getAbility() {
        return GtMultiblockAbilities.PASSTHROUGH_HATCH.get();
    }

    @Override
    public void registerAbilities(@Nonnull List<IPassthroughHatch> abilityList) {
        abilityList.add(this);
    }

    @Nonnull
    @Override
    public Class<?> getPassthroughType() {
        return IEnergyContainer.class;
    }
}
