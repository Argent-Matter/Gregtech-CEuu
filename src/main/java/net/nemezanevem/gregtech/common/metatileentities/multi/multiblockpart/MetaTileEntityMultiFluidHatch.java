package net.nemezanevem.gregtech.common.metatileentities.multi.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableFluidTankFromList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.Player;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class MetaTileEntityMultiFluidHatch extends MetaTileEntityMultiblockNotifiablePart implements IMultiblockAbilityPart<IFluidTank>, IControllable {

    private static final int TANK_SIZE = 16000;

    protected FluidTankList fluidTanks;
    private boolean workingEnabled;

    public MetaTileEntityMultiFluidHatch(ResourceLocation metaTileEntityId, int tier, boolean isExportHatch) {
        super(metaTileEntityId, tier, isExportHatch);
        this.workingEnabled = true;
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new MetaTileEntityMultiFluidHatch(metaTileEntityId, this.getTier(), this.isExportHatch);
    }

    @Override
    protected void initializeInventory() {
        FluidTank[] fluidsHandlers = new FluidTank[(int) Math.pow(this.getTier(), 2)];
        for (int i = 0; i < fluidsHandlers.length; i++) {
            fluidsHandlers[i] = new NotifiableFluidTankFromList(TANK_SIZE, this, isExportHatch, i) {
                @Override
                public Supplier<IMultipleTankHandler> getFluidTankList() {
                    return () -> MetaTileEntityMultiFluidHatch.this.fluidTanks;
                }
            };
        }
        this.fluidTanks = new FluidTankList(false, fluidsHandlers);
        this.fluidInventory = fluidTanks;
        super.initializeInventory();
    }

    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isClientSide) {
            if (workingEnabled) {
                if (isExportHatch) {
                    pushFluidsIntoNearbyHandlers(getFrontFacing());
                } else {
                    pullFluidsFromNearbyHandlers(getFrontFacing());
                }
            }
        }
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
        Level world = getWorld();
        if (world != null && !world.isClientSide) {
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(workingEnabled));
        }
    }

    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(workingEnabled);
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        super.receiveInitialSyncData(buf);
        this.workingEnabled = buf.readBoolean();
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.putBoolean("workingEnabled", workingEnabled);
        return data;
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        if (data.hasKey("workingEnabled")) {
            this.workingEnabled = data.getBoolean("workingEnabled");
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            SimpleOverlayRenderer renderer = getTier() == 2 ? Textures.PIPE_4X_OVERLAY : Textures.PIPE_9X_OVERLAY;
            renderer.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
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
            return Textures.VOLTAGE_CASINGS[getTier() == 2 ? 3 : 5];
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level player, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable(isExportHatch ? "gregtech.machine.fluid_hatch.export.tooltip" : "gregtech.machine.fluid_hatch.import.tooltip"));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.fluid_storage_capacity_mult", (int) Math.pow(this.getTier(), 2), TANK_SIZE));
        tooltip.add(Component.translatable("gregtech.universal.enabled"));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable Level world, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(Component.translatable("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return isExportHatch ? new FluidTankList(false) : new FluidTankList(false, fluidTanks);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        return isExportHatch ? new FluidTankList(false, fluidTanks) : new FluidTankList(false);
    }

    @Override
    public MultiblockAbility<IFluidTank> getAbility() {
        return isExportHatch ? MultiblockAbility.EXPORT_FLUIDS : MultiblockAbility.IMPORT_FLUIDS;
    }

    @Override
    public void registerAbilities(List<IFluidTank> abilityList) {
        abilityList.addAll(fluidTanks.getFluidTanks());
    }

    @Override
    protected ModularUI createUI(Player entityPlayer) {
        int rowSize = getTier();
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176,
                        18 + 18 * rowSize + 94)
                .label(10, 5, getMetaFullName());

        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                builder.widget(new TankWidget(fluidTanks.getTankAt(index), 89 - rowSize * 9 + x * 18, 18 + y * 18, 18, 18)
                        .setBackgroundTexture(GuiTextures.FLUID_SLOT)
                        .setContainerClicking(true, !isExportHatch)
                        .setAlwaysShowFull(true));
            }
        }
        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 18 + 18 * rowSize + 12);
        return builder.build(getHolder(), entityPlayer);
    }
}
