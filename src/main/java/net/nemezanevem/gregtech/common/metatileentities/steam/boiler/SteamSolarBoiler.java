package net.nemezanevem.gregtech.common.metatileentities.steam.boiler;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.widgets.ProgressWidget;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;

public class SteamSolarBoiler extends SteamBoiler {

    public SteamSolarBoiler(ResourceLocation metaTileEntityId, boolean isHighPressure) {
        super(metaTileEntityId, isHighPressure, Textures.SOLAR_BOILER_OVERLAY);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new SteamSolarBoiler(metaTileEntityId, isHighPressure);
    }

    @Override
    protected int getBaseSteamOutput() {
        return isHighPressure ? 360 : 120;
    }

    @Override
    protected void tryConsumeNewFuel() {
        if (Util.canSeeSunClearly(getWorld(), getPos())) {
            setFuelMaxBurnTime(20);
        }
    }

    @Override
    protected int getCooldownInterval() {
        return isHighPressure ? 50 : 45;
    }

    @Override
    protected int getCoolDownRate() {
        return 3;
    }

    @Override
    protected ModularUI createUI(Player entityPlayer) {
        return createUITemplate(entityPlayer)
                .progressBar(() -> Util.canSeeSunClearly(getWorld(), getPos()) ? 1.0 : 0.0, 114, 44, 20, 20,
                        GuiTextures.PROGRESS_BAR_SOLAR_STEAM.get(isHighPressure), ProgressWidget.MoveType.HORIZONTAL)
                .build(getHolder(), entityPlayer);
    }

    @Override
    public void randomDisplayTick() {
        // Solar boilers do not display particles
    }
}
