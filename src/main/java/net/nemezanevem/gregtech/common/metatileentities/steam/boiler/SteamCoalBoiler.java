package net.nemezanevem.gregtech.common.metatileentities.steam.boiler;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.capability.GregtechCapabilities;
import net.nemezanevem.gregtech.api.capability.IFuelInfo;
import net.nemezanevem.gregtech.api.capability.IFuelable;
import net.nemezanevem.gregtech.api.capability.impl.ItemFuelInfo;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

public class SteamCoalBoiler extends SteamBoiler implements IFuelable {

    public SteamCoalBoiler(ResourceLocation metaTileEntityId, boolean isHighPressure) {
        super(metaTileEntityId, isHighPressure, Textures.COAL_BOILER_OVERLAY);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new SteamCoalBoiler(metaTileEntityId, isHighPressure);
    }

    @Override
    protected int getBaseSteamOutput() {
        return isHighPressure ? 300 : 120;
    }

    @Override
    protected void tryConsumeNewFuel() {
        ItemStack fuelInSlot = importItems.extractItem(0, 1, true);
        if (fuelInSlot.isEmpty()) return;
        // Prevent consuming buckets with burn time
        if(FluidUtil.getFluidHandler(fuelInSlot) != null) {
            return;
        }
        int burnTime = ForgeHooks.getBurnTime(fuelInSlot, null);
        if (burnTime <= 0) return;
        importItems.extractItem(0, 1, false);
        ItemStack remainderAsh = ModHandler.getBurningFuelRemainder(fuelInSlot);
        if (!remainderAsh.isEmpty()) { //we don't care if we can't insert ash - it's chanced anyway
            exportItems.insertItem(0, remainderAsh, false);
        }
        setFuelMaxBurnTime(burnTime);
    }

    @Override
    protected int getCooldownInterval() {
        return isHighPressure ? 40 : 45;
    }

    @Override
    protected int getCoolDownRate() {
        return 1;
    }

    @Override
    public IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(1);
    }

    @Override
    public IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(1) {
            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if (TileEntityFurnace.getItemBurnTime(stack) <= 0)
                    return stack;
                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        if (capability == GregtechCapabilities.CAPABILITY_FUELABLE) {
            return GregtechCapabilities.CAPABILITY_FUELABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public Collection<IFuelInfo> getFuels() {
        ItemStack fuelInSlot = importItems.extractItem(0, Integer.MAX_VALUE, true);
        if (fuelInSlot == ItemStack.EMPTY)
            return Collections.emptySet();
        final int fuelRemaining = fuelInSlot.getCount();
        final int fuelCapacity = importItems.getSlotLimit(0);
        final long burnTime = (long) fuelRemaining * TileEntityFurnace.getItemBurnTime(fuelInSlot) * (this.isHighPressure ? 6 : 12);
        return Collections.singleton(new ItemFuelInfo(fuelInSlot, fuelRemaining, fuelCapacity, 1, burnTime));
    }

    @Override
    public ModularUI createUI(Player player) {
        return createUITemplate(player)
                .slot(this.importItems, 0, 115, 62,
                        GuiTextures.SLOT_STEAM.get(isHighPressure), GuiTextures.COAL_OVERLAY_STEAM.get(isHighPressure))
                .slot(this.exportItems, 0, 115, 26, true, false,
                        GuiTextures.SLOT_STEAM.get(isHighPressure), GuiTextures.DUST_OVERLAY_STEAM.get(isHighPressure))
                .progressBar(this::getFuelLeftPercent, 115, 44, 18, 18,
                        GuiTextures.PROGRESS_BAR_BOILER_FUEL.get(isHighPressure), MoveType.VERTICAL)
                .build(getHolder(), player);
    }
}
