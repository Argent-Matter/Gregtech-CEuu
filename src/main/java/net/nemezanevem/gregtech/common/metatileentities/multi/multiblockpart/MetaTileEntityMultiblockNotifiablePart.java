package net.nemezanevem.gregtech.common.metatileentities.multi.multiblockpart;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.IFluidTank;
import net.nemezanevem.gregtech.api.blockentity.multiblock.MultiblockControllerBase;
import net.nemezanevem.gregtech.api.capability.INotifiableHandler;
import net.nemezanevem.gregtech.api.capability.impl.FluidTankList;
import net.nemezanevem.gregtech.api.capability.impl.NotifiableFluidTank;
import net.nemezanevem.gregtech.api.capability.impl.NotifiableItemStackHandler;

import java.util.ArrayList;
import java.util.List;

public abstract class MetaTileEntityMultiblockNotifiablePart extends MetaTileEntityMultiblockPart {
    protected final boolean isExportHatch;

    public MetaTileEntityMultiblockNotifiablePart(ResourceLocation metaTileEntityId, int tier, boolean isExportHatch) {
        super(metaTileEntityId, tier);
        this.isExportHatch = isExportHatch;
    }

    private NotifiableItemStackHandler getItemHandler() {
        NotifiableItemStackHandler handler = null;
        if (isExportHatch && getExportItems() instanceof NotifiableItemStackHandler) {
            handler = (NotifiableItemStackHandler) getExportItems();
        } else if (!isExportHatch && getImportItems() instanceof NotifiableItemStackHandler) {
            handler = (NotifiableItemStackHandler) getImportItems();
        }
        return handler;
    }

    private FluidTankList getFluidHandlers() {
        FluidTankList handler = null;
        if (isExportHatch && getExportFluids().getFluidTanks().size() > 0) {
            handler = getExportFluids();
        } else if (!isExportHatch && getImportFluids().getFluidTanks().size() > 0) {
            handler = getImportFluids();
        }
        return handler;
    }

    private List<INotifiableHandler> getPartHandlers() {
        List<INotifiableHandler> handlerList = new ArrayList<>();

        if (this.itemInventory.getSlots() > 0) {
            NotifiableItemStackHandler itemHandler = getItemHandler();
            if (itemHandler != null) {
                handlerList.add(itemHandler);
            }
        }

        if (this.fluidInventory.getTanks() > 0) {
            FluidTankList fluidTankList = getFluidHandlers();
            if (fluidTankList != null) {
                for (IFluidTank fluidTank : fluidTankList) {
                    if (fluidTank instanceof NotifiableFluidTank) {
                        handlerList.add((INotifiableHandler) fluidTank);
                    }
                }
            }
        }
        return handlerList;
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        super.addToMultiBlock(controllerBase);
        List<INotifiableHandler> handlerList = getPartHandlers();
        for (INotifiableHandler handler : handlerList) {
            handler.addNotifiableMetaTileEntity(controllerBase);
            handler.addToNotifiedList(this, handler, isExportHatch);
        }
    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        super.removeFromMultiBlock(controllerBase);
        List<INotifiableHandler> handlerList = getPartHandlers();
        for (INotifiableHandler handler : handlerList) {
            handler.removeNotifiableMetaTileEntity(controllerBase);
        }
    }
}
