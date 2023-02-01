package net.nemezanevem.gregtech.common.metatileentities.converter;

import codechicken.lib.raytracer.VoxelShapeBlockHitResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.TieredMetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.capability.FeCompat;
import net.nemezanevem.gregtech.api.capability.GregtechCapabilities;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.util.PipelineUtil;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import net.nemezanevem.gregtech.common.ConfigHolder;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;

import static net.nemezanevem.gregtech.api.capability.GregtechDataCodes.SYNC_TILE_MODE;

public class MetaTileEntityConverter extends TieredMetaTileEntity {

    protected final ConverterTrait converterTrait;
    protected LazyOptional<ConverterTrait> converterLazy;


    protected final int amps;

    public MetaTileEntityConverter(ResourceLocation metaTileEntityId, int tier, int amps) {
        super(metaTileEntityId, tier);
        this.amps = amps;
        this.converterTrait = initializeTrait();
        converterLazy = LazyOptional.of(() -> converterTrait);
        reinitializeEnergyContainer();
    }

    protected ConverterTrait initializeTrait() {
        return new ConverterTrait(this, amps, true);
    }

    @Override
    protected void reinitializeEnergyContainer() {
        if (converterTrait == null) return;
        this.energyContainer = converterTrait.getEnergyEUContainer();
    }

    @Override
    public boolean onSoftMalletClick(Player playerIn, InteractionHand hand, Direction facing, VoxelShapeBlockHitResult hitResult) {
        if (getWorld().isClientSide) {
            scheduleRenderUpdate();
            return true;
        }
        if (converterTrait.isFeToEu()) {
            setFeToEu(false);
            playerIn.sendSystemMessage(Component.translatable("gregtech.machine.energy_converter.message_conversion_eu",
                    converterTrait.getBaseAmps(), converterTrait.getVoltage(),
                    FeCompat.toFe(converterTrait.getVoltage() * converterTrait.getBaseAmps(), FeCompat.ratio(false))));
        } else {
            setFeToEu(true);
            playerIn.sendSystemMessage(Component.translatable("gregtech.machine.energy_converter.message_conversion_fe",
                    FeCompat.toFe(converterTrait.getVoltage() * converterTrait.getBaseAmps(), FeCompat.ratio(true)),
                    converterTrait.getBaseAmps(), converterTrait.getVoltage()));
        }
        return true;
    }

    public void setFeToEu(boolean feToEu) {
        converterTrait.setFeToEu(feToEu);
        if (!getWorld().isClientSide) {
            writeCustomData(SYNC_TILE_MODE, b -> b.writeBoolean(converterTrait.isFeToEu()));
            notifyBlockUpdate();
            markDirty();
        }
    }

    public boolean isFeToEu() {
        return converterTrait.isFeToEu();
    }

    @Override
    public void receiveCustomData(int dataId, FriendlyByteBuf buf) {
        if (dataId == SYNC_TILE_MODE) {
            converterTrait.setFeToEu(buf.readBoolean());
            scheduleRenderUpdate();
        }
        super.receiveCustomData(dataId, buf);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityConverter(metaTileEntityId, getTier(), amps);
    }

    @Override
    public void getSubItems(CreativeModeTab creativeTab, NonNullList<ItemStack> subItems) {
        if (ConfigHolder.compat.energy.enableFEConverters) {
            super.getSubItems(creativeTab, subItems);
        }
    }

    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        buf.writeBoolean(converterTrait.isFeToEu());
        super.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        converterTrait.setFeToEu(buf.readBoolean());
        super.receiveInitialSyncData(buf);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] colouredPipeline = ArrayUtils.add(pipeline, new ColourMultiplier(Util.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        Textures.VOLTAGE_CASINGS[getTier()].render(renderState, translation, colouredPipeline);
        if (converterTrait.isFeToEu()) {
            for (Direction facing : Direction.values()) {
                if (facing == frontFacing)
                    Textures.ENERGY_OUT.renderSided(facing, renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[getTier()]));
                else
                    Textures.CONVERTER_FE_IN.renderSided(facing, renderState, translation, pipeline);
            }
        } else {
            for (Direction facing : Direction.values()) {
                if (facing == frontFacing)
                    Textures.CONVERTER_FE_OUT.renderSided(facing, renderState, translation, pipeline);
                else
                    Textures.ENERGY_IN.renderSided(facing, renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[getTier()]));
            }
        }
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.VOLTAGE_CASINGS[getTier()].getParticleSprite(), getPaintingColorForRendering());
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
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return converterTrait.isFeToEu() == (side == frontFacing) ?
                    converterTrait.energyFELazy.cast() : null;
        }
        if (capability == ForgeCapabilities.ENERGY) {
            return side != (converterTrait.isFeToEu() ? frontFacing : null) ?
                    converterTrait.energyFELazy.cast() : null;
        }
        if (capability == GregtechCapabilities.CAPABILITY_CONVERTER) {
            return converterLazy.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isValidFrontFacing(Direction facing) {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level player, List<Component> tooltip, boolean advanced) {
        long voltage = converterTrait.getVoltage();
        long amps = converterTrait.getBaseAmps();
        tooltip.add(Component.translatable("gregtech.machine.energy_converter.description"));
        tooltip.add(Component.translatable("gregtech.machine.energy_converter.tooltip_tool_usage"));
        tooltip.add(Component.translatable("gregtech.machine.energy_converter.tooltip_conversion_fe", FeCompat.toFe(voltage * amps, FeCompat.ratio(true)), amps, voltage, GTValues.VNF[getTier()]));
        tooltip.add(Component.translatable("gregtech.machine.energy_converter.tooltip_conversion_eu", amps, voltage, GTValues.VNF[getTier()], FeCompat.toFe(voltage * amps, FeCompat.ratio(false))));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable Level world, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(Component.translatable("gregtech.tool_action.wrench.set_facing"));
        tooltip.add(Component.translatable("gregtech.tool_action.soft_mallet.toggle_mode"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    protected long getMaxInputOutputAmperage() {
        return converterTrait.getBaseAmps();
    }

    @Override
    protected boolean isEnergyEmitter() {
        return converterTrait.isFeToEu();
    }
}
