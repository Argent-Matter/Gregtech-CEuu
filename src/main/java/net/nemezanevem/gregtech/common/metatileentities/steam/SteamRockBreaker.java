package net.nemezanevem.gregtech.common.metatileentities.steam;

import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.capability.impl.RecipeLogicSteam;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ProgressWidget.MoveType;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SteamMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeType;
import gregtech.api.recipes.RecipeTypes;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.block.Block;
import net.minecraft.entity.player.Player;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

public class SteamRockBreaker extends SteamMetaTileEntity {

    private boolean hasValidFluids;

    public SteamRockBreaker(ResourceLocation metaTileEntityId, boolean isHighPressure) {
        super(metaTileEntityId, RecipeTypes.ROCK_BREAKER_RECIPES, Textures.ROCK_BREAKER_OVERLAY, isHighPressure);
        this.workableHandler = new SteamRockBreakerRecipeLogic(this,
                workableHandler.getRecipeType(), isHighPressure, steamFluidTank, 1.0);
        if (getWorld() != null && !getWorld().isClientSide) {
            checkAdjacentFluids();
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new SteamRockBreaker(metaTileEntityId, isHighPressure);
    }

    @Override
    public void onNeighborChanged() {
        super.onNeighborChanged();
        checkAdjacentFluids();
    }

    private void checkAdjacentFluids() {
        boolean hasLava = false;
        boolean hasWater = false;
        for (Direction side : Direction.VALUES) {
            if (hasLava && hasWater) {
                break;
            }

            if (side == frontFacing || side.getAxis().isVertical()) {
                continue;
            }

            Block block = getWorld().getBlockState(getPos().offset(side)).getBlock();
            if (block == Blocks.FLOWING_LAVA || block == Blocks.LAVA) {
                hasLava = true;
            } else if (block == Blocks.FLOWING_WATER || block == Blocks.WATER) {
                hasWater = true;
            }
        }
        this.hasValidFluids = hasLava && hasWater;
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(1, this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(4, this, true);
    }

    @Override
    public ModularUI createUI(Player player) {
        return createUITemplate(player)
                .slot(importItems, 0, 53, 34, GuiTextures.SLOT_STEAM.get(isHighPressure), GuiTextures.DUST_OVERLAY_STEAM.get(isHighPressure))
                .progressBar(workableHandler::getProgressPercent, 79, 35, 21, 18,
                        GuiTextures.PROGRESS_BAR_MACERATE_STEAM.get(isHighPressure), MoveType.HORIZONTAL, workableHandler.getRecipeType())
                .slot(exportItems, 0, 107, 25, true, false, GuiTextures.SLOT_STEAM.get(isHighPressure), GuiTextures.CRUSHED_ORE_OVERLAY_STEAM.get(isHighPressure))
                .slot(exportItems, 1, 125, 25, true, false, GuiTextures.SLOT_STEAM.get(isHighPressure), GuiTextures.CRUSHED_ORE_OVERLAY_STEAM.get(isHighPressure))
                .slot(exportItems, 2, 107, 43, true, false, GuiTextures.SLOT_STEAM.get(isHighPressure), GuiTextures.CRUSHED_ORE_OVERLAY_STEAM.get(isHighPressure))
                .slot(exportItems, 3, 125, 43, true, false, GuiTextures.SLOT_STEAM.get(isHighPressure), GuiTextures.CRUSHED_ORE_OVERLAY_STEAM.get(isHighPressure))
                .build(getHolder(), player);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.putBoolean("hasValidFluids", hasValidFluids);
        return data;
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        if (data.hasKey("hasValidFluids")) {
            this.hasValidFluids = data.getBoolean("hasValidFluids");
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected void randomDisplayTick(float x, float y, float z, EnumParticleTypes flame, EnumParticleTypes smoke) {
        getWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y + 0.4F, z, 0, 0, 0);
    }

    protected class SteamRockBreakerRecipeLogic extends RecipeLogicSteam {

        public SteamRockBreakerRecipeLogic(MetaTileEntity tileEntity, GTRecipeType<?> recipeMap, boolean isHighPressure, IFluidTank steamFluidTank, double conversionRate) {
            super(tileEntity, recipeMap, isHighPressure, steamFluidTank, conversionRate);
        }

        @Override
        protected boolean shouldSearchForRecipes() {
            return hasValidFluids && super.shouldSearchForRecipes();
        }
    }
}
