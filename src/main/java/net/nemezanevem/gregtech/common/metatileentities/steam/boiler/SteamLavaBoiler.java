package net.nemezanevem.gregtech.common.metatileentities.steam.boiler;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.capability.GregtechCapabilities;
import net.nemezanevem.gregtech.api.capability.IFuelInfo;
import net.nemezanevem.gregtech.api.capability.IFuelable;
import net.nemezanevem.gregtech.api.capability.impl.FluidFuelInfo;
import net.nemezanevem.gregtech.api.capability.impl.FluidTankList;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.widgets.TankWidget;
import net.nemezanevem.gregtech.api.unification.material.GtMaterials;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SteamLavaBoiler extends SteamBoiler implements IFuelable {

    private FluidTank fuelFluidTank;

    private final Map<Fluid, Integer> boilerFuels;

    public SteamLavaBoiler(ResourceLocation metaTileEntityId, boolean isHighPressure) {
        super(metaTileEntityId, isHighPressure, Textures.LAVA_BOILER_OVERLAY);
        this.boilerFuels = getBoilerFuels();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new SteamLavaBoiler(metaTileEntityId, isHighPressure);
    }

    @Override
    protected int getBaseSteamOutput() {
        return isHighPressure ? 600 : 240;
    }

    private Map<Fluid, Integer> getBoilerFuels() {
        Map<Fluid, Integer> fuels = new HashMap<>();
        fuels.put(GtMaterials.Lava.get().getFluid(), 100);
        fuels.put(GtMaterials.Creosote.get().getFluid(), 250);

        return fuels;
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        FluidTankList superHandler = super.createImportFluidHandler();
        this.fuelFluidTank = new FilteredFluidHandler(16000)
                .setFillPredicate(fs -> boilerFuels.containsKey(fs.getFluid()));
        return new FluidTankList(false, superHandler, fuelFluidTank);

    }

    @Override
    protected void tryConsumeNewFuel() {
        for(Map.Entry<Fluid, Integer> fuels : boilerFuels.entrySet()) {
            if(fuelFluidTank.getFluid() != null && fuelFluidTank.getFluid().isFluidEqual(new FluidStack(fuels.getKey(), fuels.getValue())) && fuelFluidTank.getFluidAmount() >= fuels.getValue()) {
                fuelFluidTank.drain(fuels.getValue(), IFluidHandler.FluidAction.EXECUTE);
                setFuelMaxBurnTime(100);
            }
        }
    }

    @Override
    protected int getCooldownInterval() {
        return isHighPressure ? 40 : 45;
    }

    @Override
    protected int getCoolDownRate() {
        return 1;
    }

    private LazyOptional<IFuelable> fuelableLazyOptional = LazyOptional.of(() -> this);

    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        LazyOptional<T> result = super.getCapability(capability, side);
        if (result != null)
            return result;
        if (capability == GregtechCapabilities.CAPABILITY_FUELABLE) {
            return fuelableLazyOptional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public Collection<IFuelInfo> getFuels() {
        FluidStack fuel = fuelFluidTank.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
        if (fuel == null || fuel.getAmount() == 0)
            return Collections.emptySet();
        final int fuelRemaining = fuel.getAmount();
        final int fuelCapacity = fuelFluidTank.getCapacity();
        final long burnTime = (long) fuelRemaining * (this.isHighPressure ? 6 : 12); // 100 mb lasts 600 or 1200 ticks
        return Collections.singleton(new FluidFuelInfo(fuel, fuelRemaining, fuelCapacity, boilerFuels.get(fuel.getFluid()), burnTime));
    }

    @Override
    protected ModularUI createUI(Player entityPlayer) {
        return createUITemplate(entityPlayer)
                .widget(new TankWidget(fuelFluidTank, 119, 26, 10, 54)
                        .setBackgroundTexture(GuiTextures.PROGRESS_BAR_BOILER_EMPTY.get(isHighPressure)))
                .build(getHolder(), entityPlayer);
    }

    @Override
    public void randomDisplayTick(float x, float y, float z) {
        super.randomDisplayTick(x, y, z);
        if (GTValues.RNG.nextFloat() < 0.3F) {
            getWorld().addParticle(ParticleTypes.LAVA, x + GTValues.RNG.nextFloat(), y, z + GTValues.RNG.nextFloat(), 0.0F, 0.0F, 0.0F);
        }
    }
}
