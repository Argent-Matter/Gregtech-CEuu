package net.nemezanevem.gregtech.common.metatileentities.steam.boiler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.blockentity.IDataInfoProvider;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.capability.impl.FilteredFluidHandler;
import net.nemezanevem.gregtech.api.capability.impl.FluidTankList;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.resources.TextureArea;
import net.nemezanevem.gregtech.api.gui.widgets.FluidContainerSlotWidget;
import net.nemezanevem.gregtech.api.gui.widgets.ProgressWidget;
import net.nemezanevem.gregtech.api.gui.widgets.TankWidget;
import net.nemezanevem.gregtech.api.unification.material.GtMaterials;
import net.nemezanevem.gregtech.api.util.GTTransferUtils;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.client.renderer.ICubeRenderer;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import net.nemezanevem.gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import net.nemezanevem.gregtech.common.ConfigHolder;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static net.nemezanevem.gregtech.api.capability.GregtechDataCodes.IS_WORKING;

public abstract class SteamBoiler extends MetaTileEntity implements IDataInfoProvider {

    private static final Direction[] STEAM_PUSH_DIRECTIONS = ArrayUtils.add(Direction., Direction.UP);

    public final TextureArea bronzeSlotBackgroundTexture;

    public final TextureArea slotFurnaceBackground;

    protected final boolean isHighPressure;
    private final ICubeRenderer renderer;

    protected FluidTank waterFluidTank;
    protected FluidTank steamFluidTank;

    private int fuelBurnTimeLeft;
    private int fuelMaxBurnTime;
    private int currentTemperature;
    private boolean hasNoWater;
    private int timeBeforeCoolingDown;

    private boolean isBurning;
    private boolean wasBurningAndNeedsUpdate;
    private final ItemStackHandler containerInventory;

    public SteamBoiler(ResourceLocation metaTileEntityId, boolean isHighPressure, ICubeRenderer renderer) {
        super(metaTileEntityId);
        this.renderer = renderer;
        this.isHighPressure = isHighPressure;
        this.bronzeSlotBackgroundTexture = getGuiTexture("slot_%s");
        this.slotFurnaceBackground = getGuiTexture("slot_%s_furnace_background");
        this.containerInventory = new ItemStackHandler(2);
    }

    @Override
    public boolean isActive() {
        return isBurning;
    }

    protected SimpleSidedCubeRenderer getBaseRenderer() {
        if (isHighPressure) {
            return Textures.STEAM_BRICKED_CASING_STEEL;
        } else {
            return Textures.STEAM_BRICKED_CASING_BRONZE;
        }
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(getBaseRenderer().getParticleSprite(), getPaintingColorForRendering());
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] colouredPipeline = ArrayUtils.add(pipeline, new ColourMultiplier(Util.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        getBaseRenderer().render(renderState, translation, colouredPipeline);
        renderer.renderOrientedState(renderState, translation, pipeline, getFrontFacing(), isBurning(), true);
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.putInt("FuelBurnTimeLeft", fuelBurnTimeLeft);
        data.putInt("FuelMaxBurnTime", fuelMaxBurnTime);
        data.putInt("CurrentTemperature", currentTemperature);
        data.putBoolean("HasNoWater", hasNoWater);
        data.put("ContainerInventory", containerInventory.serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        this.fuelBurnTimeLeft = data.getInt("FuelBurnTimeLeft");
        this.fuelMaxBurnTime = data.getInt("FuelMaxBurnTime");
        this.currentTemperature = data.getInt("CurrentTemperature");
        this.hasNoWater = data.getBoolean("HasNoWater");
        this.containerInventory.deserializeNBT(data.getCompound("ContainerInventory"));
        this.isBurning = fuelBurnTimeLeft > 0;
    }

    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isBurning);
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        super.receiveInitialSyncData(buf);
        this.isBurning = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, FriendlyByteBuf buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == IS_WORKING) {
            this.isBurning = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    public void setFuelMaxBurnTime(int fuelMaxBurnTime) {
        this.fuelMaxBurnTime = fuelMaxBurnTime;
        this.fuelBurnTimeLeft = fuelMaxBurnTime;
        if (!getWorld().isClientSide) {
            markDirty();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isClientSide) {
            updateCurrentTemperature();
            if (getOffsetTimer() % 10 == 0) {
                generateSteam();
            }

            GTTransferUtils.fillInternalTankFromFluidContainer(importFluids, containerInventory, 0, 1);

            if (getOffsetTimer() % 5 == 0) {
                pushFluidsIntoNearbyHandlers(STEAM_PUSH_DIRECTIONS);
            }

            if (fuelMaxBurnTime <= 0) {
                tryConsumeNewFuel();
                if (fuelBurnTimeLeft > 0) {
                    if (wasBurningAndNeedsUpdate) {
                        this.wasBurningAndNeedsUpdate = false;
                    } else setBurning(true);
                }
            }

            if (wasBurningAndNeedsUpdate) {
                this.wasBurningAndNeedsUpdate = false;
                setBurning(false);
            }
        }
    }

    private void updateCurrentTemperature() {
        if (fuelMaxBurnTime > 0) {
            if (getOffsetTimer() % 12 == 0) {
                if (fuelBurnTimeLeft % 2 == 0 && currentTemperature < getMaxTemperate())
                    currentTemperature++;
                fuelBurnTimeLeft -= isHighPressure ? 2 : 1;
                if (fuelBurnTimeLeft == 0) {
                    this.fuelMaxBurnTime = 0;
                    this.timeBeforeCoolingDown = getCooldownInterval();
                    //boiler has no fuel now, so queue burning state update
                    this.wasBurningAndNeedsUpdate = true;
                }
            }
        } else if (timeBeforeCoolingDown == 0) {
            if (currentTemperature > 0) {
                currentTemperature -= getCoolDownRate();
                timeBeforeCoolingDown = getCooldownInterval();
            }
        } else --timeBeforeCoolingDown;
    }

    protected abstract int getBaseSteamOutput();

    private void generateSteam() {
        if (currentTemperature >= 100) {
            int fillAmount = (int) (getBaseSteamOutput() * (currentTemperature / (getMaxTemperate() * 1.0)) / 2);
            boolean hasDrainedWater = !waterFluidTank.drain(1, IFluidHandler.FluidAction.EXECUTE).isEmpty();
            int filledSteam = 0;
            if (hasDrainedWater) {
                filledSteam = steamFluidTank.fill(ModHandler.getSteam(fillAmount), IFluidHandler.FluidAction.EXECUTE);
            }
            if (this.hasNoWater && hasDrainedWater) {
                doExplosion(2.0f);
            } else this.hasNoWater = !hasDrainedWater;
            if (filledSteam == 0 && hasDrainedWater) {
                final float x = getPos().getX() + 0.5F;
                final float y = getPos().getY() + 0.5F;
                final float z = getPos().getZ() + 0.5F;

                ((ServerLevel) getWorld()).sendParticles(ParticleTypes.CLOUD,
                        x + getFrontFacing().getStepX() * 0.6,
                        y + getFrontFacing().getStepY() * 0.6,
                        z + getFrontFacing().getStepZ() * 0.6,
                        7 + GTValues.RNG.nextInt(3),
                        getFrontFacing().getStepX() / 2.0,
                        getFrontFacing().getStepY() / 2.0,
                        getFrontFacing().getStepZ() / 2.0, 0.1);

                if (ConfigHolder.machines.machineSounds && !this.isMuffled()) {
                    getWorld().playSound(null, x, y, z, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);
                }

                steamFluidTank.drain(4000, IFluidHandler.FluidAction.EXECUTE);
            }
        } else this.hasNoWater = false;
    }

    public boolean isBurning() {
        return isBurning;
    }

    public void setBurning(boolean burning) {
        this.isBurning = burning;
        if (!getWorld().isClientSide) {
            markDirty();
            writeCustomData(IS_WORKING, buf -> buf.writeBoolean(burning));
        }
    }

    protected abstract void tryConsumeNewFuel();

    protected abstract int getCooldownInterval();

    protected abstract int getCoolDownRate();

    public int getMaxTemperate() {
        return isHighPressure ? 1000 : 500;
    }

    public double getTemperaturePercent() {
        return currentTemperature / (getMaxTemperate() * 1.0);
    }

    public double getFuelLeftPercent() {
        return fuelMaxBurnTime == 0 ? 0.0 : fuelBurnTimeLeft / (fuelMaxBurnTime * 1.0);
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        this.waterFluidTank = new FilteredFluidHandler(16000).setFillPredicate(ModHandler::isWater);
        return new FluidTankList(false, waterFluidTank);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        this.steamFluidTank = new FluidTank(16000);
        return new FluidTankList(false, steamFluidTank);
    }

    protected TextureArea getGuiTexture(String pathTemplate) {
        String type = isHighPressure ? "steel" : "bronze";
        return TextureArea.fullImage(String.format("textures/gui/steam/%s/%s.png",
                type, pathTemplate.replace("%s", type)));
    }

    public ModularUI.Builder createUITemplate(Player player) {
        return ModularUI.builder(GuiTextures.BACKGROUND_STEAM.get(isHighPressure), 176, 166)
                .label(6, 6, getMetaFullName()).shouldColor(false)
                .widget(new ProgressWidget(this::getTemperaturePercent, 96, 26, 10, 54)
                        .setProgressBar(GuiTextures.PROGRESS_BAR_BOILER_EMPTY.get(isHighPressure),
                                GuiTextures.PROGRESS_BAR_BOILER_HEAT,
                                ProgressWidget.MoveType.VERTICAL))

                .widget(new TankWidget(waterFluidTank, 83, 26, 10, 54)
                        .setBackgroundTexture(GuiTextures.PROGRESS_BAR_BOILER_EMPTY.get(isHighPressure)))
                .widget(new TankWidget(steamFluidTank, 70, 26, 10, 54)
                        .setBackgroundTexture(GuiTextures.PROGRESS_BAR_BOILER_EMPTY.get(isHighPressure)))

                .widget(new FluidContainerSlotWidget(containerInventory, 0, 43, 26, true)
                        .setBackgroundTexture(GuiTextures.SLOT_STEAM.get(isHighPressure), GuiTextures.IN_SLOT_OVERLAY_STEAM.get(isHighPressure)))
                .slot(containerInventory, 1, 43, 62, true, false,
                        GuiTextures.SLOT_STEAM.get(isHighPressure), GuiTextures.OUT_SLOT_OVERLAY_STEAM.get(isHighPressure))
                .image(43, 44, 18, 18, GuiTextures.CANISTER_OVERLAY_STEAM.get(isHighPressure))

                .bindPlayerInventory(player.getInventory(), GuiTextures.SLOT_STEAM.get(isHighPressure), 0);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level player, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.literal(String.format("%s %s",
                Component.translatable("gregtech.universal.tooltip.produces_fluid", getBaseSteamOutput() / 20),
                GtMaterials.Steam.get().getLocalizedName())));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable Level world, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(Component.translatable("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public SoundEvent getSound() {
        return GTSoundEvents.BOILER;
    }

    @Override
    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
        clearInventory(itemBuffer, containerInventory);
    }

    @Nonnull
    @Override
    public List<Component> getDataInfo() {
        return Collections.singletonList(Component.translatable("gregtech.machine.steam_boiler.heat_amount", Util.formatNumbers((int) (this.getTemperaturePercent() * 100))));
    }

    @Override
    public void randomDisplayTick() {
        if (this.isActive()) {
            final BlockPos pos = getPos();
            double x = pos.getX() + 0.5F;
            double z = pos.getZ() + 0.5F;

            if (GTValues.RNG.nextDouble() < 0.1) {
                getWorld().playSound(Minecraft.getInstance().player, x, pos.getY(), z + 0.5D, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            final Direction facing = getFrontFacing();
            final float horizontalOffset = GTValues.RNG.nextFloat() * 0.6F - 0.3F;
            final float y = pos.getY() + GTValues.RNG.nextFloat() * 0.375F;

            if (facing.getAxis() == Direction.Axis.X) {
                if (facing.getAxisDirection() == Direction.AxisDirection.POSITIVE) x += 0.52F;
                else x -= 0.52F;
                z += horizontalOffset;
            } else if (facing.getAxis() == Direction.Axis.Z) {
                if (facing.getAxisDirection() == Direction.AxisDirection.POSITIVE) z += 0.52F;
                else z -= 0.52F;
                x += horizontalOffset;
            }
            randomDisplayTick(x, y, z);
        }
    }

    protected void randomDisplayTick(float x, float y, float z) {
        getWorld().addParticle(isHighPressure ? ParticleTypes.LARGE_SMOKE : ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
        getWorld().addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
    }
}
