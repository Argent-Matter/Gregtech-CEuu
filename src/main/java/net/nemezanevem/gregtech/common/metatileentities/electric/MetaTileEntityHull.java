package net.nemezanevem.gregtech.common.metatileentities.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
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
import java.util.List;

public class MetaTileEntityHull extends MetaTileEntityMultiblockPart implements IPassthroughHatch, IMultiblockAbilityPart<IPassthroughHatch> {

    protected IEnergyContainer energyContainer;

    public MetaTileEntityHull(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        reinitializeEnergyContainer();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityHull(metaTileEntityId, getTier());
    }

    protected void reinitializeEnergyContainer() {
        long tierVoltage = GTValues.V[getTier()];
        this.energyContainer = new EnergyContainerHandler(this, tierVoltage * 16L, tierVoltage, 1L, tierVoltage, 1L);
        ((EnergyContainerHandler) this.energyContainer).setSideOutputCondition(s -> s == getFrontFacing());
    }

    @Override
    public int getActualComparatorValue() {
        long energyStored = energyContainer.getEnergyStored();
        long energyCapacity = energyContainer.getEnergyCapacity();
        float f = energyCapacity == 0L ? 0.0f : energyStored / (energyCapacity * 1.0f);
        return Mth.floor(f * 14.0f) + (energyStored > 0 ? 1 : 0);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.ENERGY_OUT.renderSided(getFrontFacing(), renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[getTier()]));
    }

    @Override
    public boolean isValidFrontFacing(Direction facing) {
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
        String tierName = GTValues.VNF[getTier()];
        tooltip.add(Component.translatable("gregtech.machine.hull.tooltip"));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.voltage_in_out", energyContainer.getInputVoltage(), tierName));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.amperage_in_out", 1));
    }

    @Override
    public void tick() {
        super.tick();
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

    @Override
    public void addToolUsages(ItemStack stack, @Nullable Level world, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(Component.translatable("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }
}
