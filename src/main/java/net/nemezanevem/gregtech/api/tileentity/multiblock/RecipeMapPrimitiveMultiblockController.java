package net.nemezanevem.gregtech.api.tileentity.multiblock;

import gregtech.api.capability.impl.*;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.recipes.RecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fluids.FluidTank;

import java.util.ArrayList;
import java.util.List;

public abstract class RecipeTypePrimitiveMultiblockController extends MultiblockWithDisplayBase {

    protected PrimitiveRecipeLogic recipeMapWorkable;

    public RecipeTypePrimitiveMultiblockController(ResourceLocation metaTileEntityId, RecipeType<?> recipeMap) {
        super(metaTileEntityId);
        this.recipeMapWorkable = new PrimitiveRecipeLogic(this, recipeMap);
        initializeAbilities();
    }

    // just initialize inventories based on RecipeType values by default
    protected void initializeAbilities() {
        this.importItems = new NotifiableItemStackHandler(recipeMapWorkable.getRecipeType().getMaxInputs(), this, false);
        this.importFluids = new FluidTankList(true, makeFluidTanks(recipeMapWorkable.getRecipeType().getMaxFluidInputs(), false));
        this.exportItems = new NotifiableItemStackHandler(recipeMapWorkable.getRecipeType().getMaxOutputs(), this, true);
        this.exportFluids = new FluidTankList(false, makeFluidTanks(recipeMapWorkable.getRecipeType().getMaxFluidOutputs(), true));

        this.itemInventory = new ItemHandlerProxy(this.importItems, this.exportItems);
        this.fluidInventory = new FluidHandlerProxy(this.importFluids, this.exportFluids);
    }

    private List<FluidTank> makeFluidTanks(int length, boolean isExport) {
        List<FluidTank> fluidTankList = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            fluidTankList.add(new NotifiableFluidTank(32000, this, isExport));
        }
        return fluidTankList;
    }

    @Override
    protected void updateFormedValid() {
        recipeMapWorkable.update();
    }

    @Override
    public boolean isActive() {
        return super.isActive() && this.recipeMapWorkable.isWorkingEnabled() && this.recipeMapWorkable.isActive();
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        recipeMapWorkable.invalidate();
    }

    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        return !(trait instanceof PrimitiveRecipeLogic);
    }

    @Override
    public SoundEvent getSound() {
        return recipeMapWorkable.getRecipeType().getSound();
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return isStructureFormed();
    }
}
