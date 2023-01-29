package net.nemezanevem.gregtech.common.metatileentities.multi.electric.generator;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.blockentity.multiblock.*;
import net.nemezanevem.gregtech.api.capability.GregtechCapabilities;
import net.nemezanevem.gregtech.api.capability.IEnergyContainer;
import net.nemezanevem.gregtech.api.capability.IMultipleTankHandler;
import net.nemezanevem.gregtech.api.capability.impl.MultiblockFuelRecipeLogic;
import net.nemezanevem.gregtech.api.pattern.BlockPattern;
import net.nemezanevem.gregtech.api.pattern.FactoryBlockPattern;
import net.nemezanevem.gregtech.api.pattern.PatternMatchContext;
import net.nemezanevem.gregtech.api.unification.material.GtMaterials;
import net.nemezanevem.gregtech.client.renderer.ICubeRenderer;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import net.nemezanevem.gregtech.common.block.BlockMetalCasing.MetalCasingType;
import net.nemezanevem.gregtech.common.block.BlockMultiblockCasing.MultiblockCasingType;
import net.nemezanevem.gregtech.common.block.BlockTurbineCasing.TurbineCasingType;
import net.nemezanevem.gregtech.common.block.MetaBlocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityLargeCombustionEngine extends FuelMultiblockController {

    private final int tier;
    private final boolean isExtreme;
    private boolean boostAllowed;

    public MetaTileEntityLargeCombustionEngine(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, GTRecipeTypes.COMBUSTION_GENERATOR_FUELS, tier);
        this.recipeMapWorkable = new LargeCombustionEngineWorkableHandler(this, tier > GTValues.EV);
        this.recipeMapWorkable.setMaximumOverclockVoltage(GTValues.V[tier]);
        this.tier = tier;
        this.isExtreme = tier > GTValues.EV;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLargeCombustionEngine(metaTileEntityId, tier);
    }

    @Override
    protected void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isStructureFormed()) {
            if (getInputFluidInventory() != null) {
                FluidStack lubricantStack = getInputFluidInventory().drain(GtMaterials.Lubricant.get().getFluid(Integer.MAX_VALUE), IFluidHandler.FluidAction.EXECUTE);
                FluidStack oxygenStack = getInputFluidInventory().drain(GtMaterials.Oxygen.get().getFluid(Integer.MAX_VALUE), IFluidHandler.FluidAction.EXECUTE);
                FluidStack liquidOxygenStack = getInputFluidInventory().drain(GtMaterials.LiquidOxygen.get().getFluid(Integer.MAX_VALUE), IFluidHandler.FluidAction.EXECUTE);
                int lubricantAmount = lubricantStack.isEmpty() ? 0 : lubricantStack.getAmount();
                textList.add(Component.translatable("gregtech.multiblock.large_combustion_engine.lubricant_amount", lubricantAmount));
                if (boostAllowed) {
                    if (!isExtreme) {
                        if (((LargeCombustionEngineWorkableHandler) recipeMapWorkable).isOxygenBoosted) {
                            int oxygenAmount = oxygenStack.isEmpty() ? 0 : oxygenStack.getAmount();
                            textList.add(Component.translatable("gregtech.multiblock.large_combustion_engine.oxygen_amount", oxygenAmount));
                            textList.add(Component.translatable("gregtech.multiblock.large_combustion_engine.oxygen_boosted"));
                        } else {
                            textList.add(Component.translatable("gregtech.multiblock.large_combustion_engine.supply_oxygen_to_boost"));
                        }
                    }
                    else {
                        if (((LargeCombustionEngineWorkableHandler) recipeMapWorkable).isOxygenBoosted) {
                            int liquidOxygenAmount = liquidOxygenStack.isEmpty() ? 0 : liquidOxygenStack.getAmount();
                            textList.add(Component.translatable("gregtech.multiblock.large_combustion_engine.liquid_oxygen_amount", liquidOxygenAmount));
                            textList.add(Component.translatable("gregtech.multiblock.large_combustion_engine.liquid_oxygen_boosted"));
                        } else {
                            textList.add(Component.translatable("gregtech.multiblock.large_combustion_engine.supply_liquid_oxygen_to_boost"));
                        }
                    }
                }
                else {
                    textList.add(Component.translatable("gregtech.multiblock.large_combustion_engine.boost_disallowed"));
                }
            }

            if (isStructureObstructed())
                textList.add(Component.translatable("gregtech.multiblock.large_combustion_engine.obstructed").withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level player, List<Component> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(Component.translatable("gregtech.universal.tooltip.base_production_eut", GTValues.V[tier]));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.uses_per_hour_lubricant", 1000));
        if (isExtreme) {
            tooltip.add(Component.translatable("gregtech.machine.large_combustion_engine.tooltip.boost_extreme", GTValues.V[tier] * 4));
        } else {
            tooltip.add(Component.translatable("gregtech.machine.large_combustion_engine.tooltip.boost_regular", GTValues.V[tier] * 3));
        }
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XDX", "XXX")
                .aisle("XCX", "CGC", "XCX")
                .aisle("XCX", "CGC", "XCX")
                .aisle("AAA", "AYA", "AAA")
                .where('X', states(getCasingState()))
                .where('G', states(getGearboxState()))
                .where('C', states(getCasingState()).setMinGlobalLimited(3).or(autoAbilities(false, true, true, true, true, true, true)))
                .where('D', metaTileEntities(MultiblockAbility.REGISTRY.get(GtMultiblockAbilities.OUTPUT_ENERGY.get()).stream()
                        .filter(mte -> {
                            LazyOptional<IEnergyContainer> container = mte.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, null);
                            return container.isPresent() && container.resolve().get().getOutputVoltage() * container.resolve().get().getOutputAmperage() >= GTValues.V[tier];
                        })
                        .toArray(MetaTileEntity[]::new))
                        .addTooltip("gregtech.multiblock.pattern.error.limited.1", GTValues.VN[tier]))
                .where('A', states(getIntakeState()).addTooltips("gregtech.multiblock.pattern.clear_amount_1"))
                .where('Y', selfPredicate())
                .build();
    }

    public BlockState getCasingState() {
        return isExtreme ? MetaBlocks.METAL_CASING.get().getState(MetalCasingType.TUNGSTENSTEEL_ROBUST) :
                MetaBlocks.METAL_CASING.get().getState(MetalCasingType.TITANIUM_STABLE);
    }

    public BlockState getGearboxState() {
        return isExtreme ? MetaBlocks.TURBINE_CASING.get().getState(TurbineCasingType.TUNGSTENSTEEL_GEARBOX) :
                MetaBlocks.TURBINE_CASING.get().getState(TurbineCasingType.TITANIUM_GEARBOX);
    }

    public BlockState getIntakeState() {
        return isExtreme ? MetaBlocks.MULTIBLOCK_CASING.get().getState(MultiblockCasingType.EXTREME_ENGINE_INTAKE_CASING) :
                MetaBlocks.MULTIBLOCK_CASING.get().getState(MultiblockCasingType.ENGINE_INTAKE_CASING);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return isExtreme ? Textures.ROBUST_TUNGSTENSTEEL_CASING : Textures.STABLE_TITANIUM_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return isExtreme ? Textures.EXTREME_COMBUSTION_ENGINE_OVERLAY : Textures.LARGE_COMBUSTION_ENGINE_OVERLAY;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return true;
    }

    @Override
    public boolean isStructureObstructed() {
        return super.isStructureObstructed() || checkIntakesObstructed();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        IEnergyContainer energyContainer = getEnergyContainer();
        this.boostAllowed = energyContainer != null && energyContainer.getOutputVoltage() >= GTValues.V[this.tier + 1];
    }

    private boolean checkIntakesObstructed() {
        Direction facing = this.getFrontFacing();
        boolean permuteXZ = facing.getAxis() == Direction.Axis.Z;
        BlockPos centerPos = this.getPos().offset(facing.getNormal());
        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                //Skip the controller block itself
                if (x == 0 && y == 0)
                    continue;
                BlockPos blockPos = centerPos.offset(permuteXZ ? x : 0, y, permuteXZ ? 0 : x);
                BlockState blockState = this.getWorld().getBlockState(blockPos);
                if (!blockState.isAir())
                    return true;
            }
        }
        return false;
    }

    @Override
    protected boolean shouldShowVoidingModeButton() {
        return false;
    }

    public boolean isBoostAllowed() {
        return boostAllowed;
    }

    private static class LargeCombustionEngineWorkableHandler extends MultiblockFuelRecipeLogic {

        private boolean isOxygenBoosted = false;

        private final MetaTileEntityLargeCombustionEngine combustionEngine;
        private final boolean isExtreme;
        private final int tier;

        private static final FluidStack OXYGEN_STACK = GtMaterials.Oxygen.get().getFluid(20);
        private static final FluidStack LIQUID_OXYGEN_STACK = GtMaterials.LiquidOxygen.get().getFluid(80);
        private static final FluidStack LUBRICANT_STACK = GtMaterials.Lubricant.get().getFluid(1);

        public LargeCombustionEngineWorkableHandler(RecipeTypeMultiblockController tileEntity, boolean isExtreme) {
            super(tileEntity);
            this.combustionEngine = (MetaTileEntityLargeCombustionEngine) tileEntity;
            this.isExtreme = isExtreme;
            this.tier = isExtreme ? GTValues.IV : GTValues.EV;
        }

        @Override
        protected void updateRecipeProgress() {
            if (canRecipeProgress && drawEnergy(recipeEUt, true)) {

                //drain lubricant and invalidate if it fails
                if (totalContinuousRunningTime == 1 || totalContinuousRunningTime % 72 == 0) {
                    IMultipleTankHandler inputTank = combustionEngine.getInputFluidInventory();
                    if (LUBRICANT_STACK.isFluidStackIdentical(inputTank.drain(LUBRICANT_STACK, IFluidHandler.FluidAction.SIMULATE))) {
                        inputTank.drain(LUBRICANT_STACK, IFluidHandler.FluidAction.EXECUTE);
                    } else {
                        invalidate();
                        return;
                    }
                }

                //drain oxygen if present to boost production, and if the dynamo hatch supports it
                if (combustionEngine.isBoostAllowed() && (totalContinuousRunningTime == 1 || totalContinuousRunningTime % 20 == 0)) {
                    IMultipleTankHandler inputTank = combustionEngine.getInputFluidInventory();
                    FluidStack boosterStack = isExtreme ? LIQUID_OXYGEN_STACK : OXYGEN_STACK;
                    if (boosterStack.isFluidStackIdentical(inputTank.drain(boosterStack, IFluidHandler.FluidAction.SIMULATE))) {
                        isOxygenBoosted = true;
                        inputTank.drain(boosterStack, IFluidHandler.FluidAction.EXECUTE);
                    } else {
                        isOxygenBoosted = false;
                    }
                }

                drawEnergy(recipeEUt, false);

                //as recipe starts with progress on 1 this has to be > only not => to compensate for it
                if (++progressTime > maxProgressTime) {
                    completeRecipe();
                }
            }
        }

        @Override
        protected boolean shouldSearchForRecipes() {
            return super.shouldSearchForRecipes() && LUBRICANT_STACK.isFluidStackIdentical(((RecipeTypeMultiblockController) metaTileEntity).getInputFluidInventory().drain(LUBRICANT_STACK, false));
        }

        @Override
        protected long getMaxVoltage() {
            //this multiplies consumption through parallel
            if (isOxygenBoosted)
                return GTValues.V[tier] * 2;
            else
                return GTValues.V[tier];
        }

        @Override
        protected long boostProduction(long production) {
            //this multiplies production without increasing consumption
            if (isOxygenBoosted)
                if (!isExtreme)
                    //recipe gives 2A EV and we want 3A EV, for 150% efficiency
                    return production * 3 / 2;
                else
                    //recipe gives 2A IV and we want 4A IV, for 200% efficiency
                    return production * 2;
            return production;
        }

        @Override
        public void invalidate() {
            isOxygenBoosted = false;
            super.invalidate();
        }
    }
}
