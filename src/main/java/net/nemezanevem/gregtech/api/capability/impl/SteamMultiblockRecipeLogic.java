package net.nemezanevem.gregtech.api.capability.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.blockentity.multiblock.RecipeTypeSteamMultiblockController;
import net.nemezanevem.gregtech.api.capability.IMultipleTankHandler;
import net.nemezanevem.gregtech.api.recipe.GTRecipe;
import net.nemezanevem.gregtech.common.ConfigHolder;

import javax.annotation.Nonnull;

public class SteamMultiblockRecipeLogic extends AbstractRecipeLogic {

    private IMultipleTankHandler steamFluidTank;
    private IFluidTank steamFluidTankCombined;

    // EU per mB
    private final double conversionRate;

    public SteamMultiblockRecipeLogic(RecipeTypeSteamMultiblockController tileEntity, GTRecipeType<?> recipeMap, IMultipleTankHandler steamFluidTank, double conversionRate) {
        super(tileEntity, recipeMap);
        this.steamFluidTank = steamFluidTank;
        this.conversionRate = conversionRate;
        setAllowOverclocking(false);
        combineSteamTanks();
    }

    public IFluidTank getSteamFluidTankCombined() {
        combineSteamTanks();
        return steamFluidTankCombined;
    }

    @Override
    protected IItemHandlerModifiable getInputInventory() {
        RecipeTypeSteamMultiblockController controller = (RecipeTypeSteamMultiblockController) metaTileEntity;
        return controller.getInputInventory();
    }

    @Override
    protected IItemHandlerModifiable getOutputInventory() {
        RecipeTypeSteamMultiblockController controller = (RecipeTypeSteamMultiblockController) metaTileEntity;
        return controller.getOutputInventory();
    }

    protected IMultipleTankHandler getSteamFluidTank() {
        RecipeTypeSteamMultiblockController controller = (RecipeTypeSteamMultiblockController) metaTileEntity;
        return controller.getSteamFluidTank();
    }

    private void combineSteamTanks() {
        steamFluidTank = getSteamFluidTank();
        if (steamFluidTank == null)
            steamFluidTankCombined = new FluidTank(0);
        else {
            int capacity = steamFluidTank.getTanks() * 64000;
            steamFluidTankCombined = new FluidTank(capacity);
            steamFluidTankCombined.fill(steamFluidTank.drain(capacity, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
        }
    }

    @Override
    public void tick() {

        // Fixes an annoying GTCE bug in AbstractRecipeLogic
        RecipeTypeSteamMultiblockController controller = (RecipeTypeSteamMultiblockController) metaTileEntity;
        if (isActive && !controller.isStructureFormed()) {
            progressTime = 0;
            wasActiveAndNeedsUpdate = true;
        }

        combineSteamTanks();
        super.tick();
    }

    @Override
    protected long getEnergyInputPerSecond() {
        return 0;
    }

    @Override
    protected long getEnergyStored() {
        combineSteamTanks();
        return (long) Math.ceil(steamFluidTankCombined.getFluidAmount() * conversionRate);
    }

    @Override
    protected long getEnergyCapacity() {
        combineSteamTanks();
        return (long) Math.floor(steamFluidTankCombined.getCapacity() * conversionRate);
    }

    @Override
    protected boolean drawEnergy(int recipeEUt, boolean simulate) {
        combineSteamTanks();
        int resultDraw = (int) Math.ceil(recipeEUt / conversionRate);
        return resultDraw >= 0 && steamFluidTankCombined.getFluidAmount() >= resultDraw &&
                steamFluidTank.drain(resultDraw, simulate ? IFluidHandler.FluidAction.EXECUTE : IFluidHandler.FluidAction.SIMULATE) != FluidStack.EMPTY;
    }

    @Override
    protected long getMaxVoltage() {
        return GTValues.V[GTValues.LV];
    }

    @Override
    public boolean isAllowOverclocking() {
        return false;
    }

    @Override
    protected boolean setupAndConsumeRecipeInputs(@Nonnull GTRecipe recipe, @Nonnull IItemHandlerModifiable importInventory) {
        RecipeTypeSteamMultiblockController controller = (RecipeTypeSteamMultiblockController) metaTileEntity;
        if (controller.checkRecipe(recipe, false) &&
                super.setupAndConsumeRecipeInputs(recipe, importInventory)) {
            controller.checkRecipe(recipe, true);
            return true;
        } else return false;
    }

    @Override
    protected void completeRecipe() {
        super.completeRecipe();
        ventSteam();
    }

    private void ventSteam() {
        BlockPos machinePos = metaTileEntity.getPos();
        Direction ventingSide = metaTileEntity.getFrontFacing();
        BlockPos ventingBlockPos = machinePos.offset(ventingSide.getNormal());
        BlockState blockOnPos = metaTileEntity.getWorld().getBlockState(ventingBlockPos);
        if (blockOnPos.getCollisionShape(metaTileEntity.getWorld(), ventingBlockPos) == Shapes.empty()) {
            performVentingAnimation(machinePos, ventingSide);
        }
        else if(blockOnPos.getBlock() == Blocks.SNOW && blockOnPos.getValue(SnowLayerBlock.LAYERS) == 1) {
            performVentingAnimation(machinePos, ventingSide);
            metaTileEntity.getWorld().destroyBlock(ventingBlockPos, false);
        }
    }

    private void performVentingAnimation(BlockPos machinePos, Direction ventingSide) {
        ServerLevel world = (ServerLevel) metaTileEntity.getWorld();
        double posX = machinePos.getX() + 0.5 + ventingSide.getStepX() * 0.6;
        double posY = machinePos.getY() + 0.5 + ventingSide.getStepY() * 0.6;
        double posZ = machinePos.getZ() + 0.5 + ventingSide.getStepZ() * 0.6;

        world.sendParticles(ParticleTypes.CLOUD, posX, posY, posZ,
                7 + GTValues.RNG.nextInt(3),
                ventingSide.getStepX() / 2.0,
                ventingSide.getStepY() / 2.0,
                ventingSide.getStepZ() / 2.0, 0.1);
        if (ConfigHolder.machines.machineSounds && !metaTileEntity.isMuffled()){
            world.playSound(null, posX, posY, posZ, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }
}
