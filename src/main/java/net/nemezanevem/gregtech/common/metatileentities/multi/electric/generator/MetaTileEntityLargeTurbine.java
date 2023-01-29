package net.nemezanevem.gregtech.common.metatileentities.multi.electric.generator;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.blockentity.ITieredMetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.blockentity.multiblock.FuelMultiblockController;
import net.nemezanevem.gregtech.api.blockentity.multiblock.GtMultiblockAbilities;
import net.nemezanevem.gregtech.api.blockentity.multiblock.IMultiblockPart;
import net.nemezanevem.gregtech.api.blockentity.multiblock.MultiblockAbility;
import net.nemezanevem.gregtech.api.capability.IRotorHolder;
import net.nemezanevem.gregtech.api.capability.impl.FluidTankList;
import net.nemezanevem.gregtech.api.pattern.BlockPattern;
import net.nemezanevem.gregtech.api.pattern.FactoryBlockPattern;
import net.nemezanevem.gregtech.api.pattern.PatternMatchContext;
import net.nemezanevem.gregtech.api.recipe.GTRecipeType;
import net.nemezanevem.gregtech.client.renderer.ICubeRenderer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityLargeTurbine extends FuelMultiblockController implements ITieredMetaTileEntity {

    public final int tier;

    public final BlockState casingState;
    public final BlockState gearboxState;
    public final ICubeRenderer casingRenderer;
    public final boolean hasMufflerHatch;
    public final ICubeRenderer frontOverlay;

    private static final int MIN_DURABILITY_TO_WARN = 10;

    public IFluidHandler exportFluidHandler;

    public MetaTileEntityLargeTurbine(ResourceLocation metaTileEntityId, GTRecipeType<?> recipeMap, int tier, BlockState casingState, BlockState gearboxState, ICubeRenderer casingRenderer, boolean hasMufflerHatch, ICubeRenderer frontOverlay) {
        super(metaTileEntityId, recipeMap, tier);
        this.casingState = casingState;
        this.gearboxState = gearboxState;
        this.casingRenderer = casingRenderer;
        this.hasMufflerHatch = hasMufflerHatch;
        this.frontOverlay = frontOverlay;
        this.tier = tier;
        this.recipeMapWorkable = new LargeTurbineWorkableHandler(this, tier);
        this.recipeMapWorkable.setMaximumOverclockVoltage(GTValues.V[tier]);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLargeTurbine(metaTileEntityId, recipeMap, tier, casingState, gearboxState, casingRenderer, hasMufflerHatch, frontOverlay);
    }

    public IRotorHolder getRotorHolder() {
        List<IRotorHolder> abilities = getAbilities(GtMultiblockAbilities.ROTOR_HOLDER.get());
        if (abilities.isEmpty())
            return null;
        return abilities.get(0);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.exportFluidHandler = null;
    }

    /**
     * @return true if turbine is formed and it's face is free and contains
     * only air blocks in front of rotor holder
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isRotorFaceFree() {
        IRotorHolder rotorHolder = getRotorHolder();
        if (rotorHolder != null)
            return isStructureFormed() && getRotorHolder().isFrontFaceFree();
        return false;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.exportFluidHandler = new FluidTankList(true, getAbilities(GtMultiblockAbilities.EXPORT_FLUIDS.get()));
        ((LargeTurbineWorkableHandler) this.recipeMapWorkable).updateTanks();
    }

    @Override
    protected void addDisplayText(List<Component> textList) {
        if (isStructureFormed()) {
            IRotorHolder rotorHolder = getRotorHolder();
            FluidStack fuelStack = ((LargeTurbineWorkableHandler) recipeMapWorkable).getInputFluidStack();
            int fuelAmount = fuelStack == null ? 0 : fuelStack.getAmount();

            Component fuelName = Component.translatable(fuelAmount == 0 ? "gregtech.fluid.empty" : fuelStack.getTranslationKey());
            textList.add(Component.translatable("gregtech.multiblock.turbine.fuel_amount", fuelAmount, fuelName));

            if (rotorHolder.getRotorEfficiency() > 0) {
                textList.add(Component.translatable("gregtech.multiblock.turbine.rotor_speed", rotorHolder.getRotorSpeed(), rotorHolder.getMaxRotorHolderSpeed()));
                textList.add(Component.translatable("gregtech.multiblock.turbine.efficiency", rotorHolder.getTotalEfficiency()));

                long maxProduction = ((LargeTurbineWorkableHandler) recipeMapWorkable).getMaxVoltage();
                long currentProduction = isActive() ? ((LargeTurbineWorkableHandler) recipeMapWorkable).boostProduction((int) maxProduction) : 0;
                if (currentProduction >= maxProduction) {
                    textList.add(Component.translatable("gregtech.multiblock.turbine.energy_per_tick_maxed", maxProduction));
                } else {
                    textList.add(Component.translatable("gregtech.multiblock.turbine.energy_per_tick", currentProduction, maxProduction));
                }

                int rotorDurability = rotorHolder.getRotorDurabilityPercent();
                if (rotorDurability > MIN_DURABILITY_TO_WARN) {
                    textList.add(Component.translatable("gregtech.multiblock.turbine.rotor_durability", rotorDurability));
                } else {
                    textList.add(Component.translatable("gregtech.multiblock.turbine.rotor_durability", rotorDurability).withStyle(ChatFormatting.RED));
                }
            }
            if (!isRotorFaceFree()) {
                textList.add(Component.translatable("gregtech.multiblock.turbine.obstructed")
                        .withStyle(ChatFormatting.RED));
            }
        }
        super.addDisplayText(textList);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level player, List<Component> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(Component.translatable("gregtech.universal.tooltip.base_production_eut", GTValues.V[tier] * 2));
        tooltip.add(Component.translatable("gregtech.multiblock.turbine.efficiency_tooltip", GTValues.VNF[tier]));
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CCCC", "CHHC", "CCCC")
                .aisle("CHHC", "RGGR", "CHHC")
                .aisle("CCCC", "CSHC", "CCCC")
                .where('S', selfPredicate())
                .where('G', states(getGearBoxState()))
                .where('C', states(getCasingState()))
                .where('R', metaTileEntities(MultiblockAbility.REGISTRY.get(GtMultiblockAbilities.ROTOR_HOLDER.get()).stream()
                        .filter(mte -> (mte instanceof ITieredMetaTileEntity) && (((ITieredMetaTileEntity) mte).getTier() >= tier))
                        .toArray(MetaTileEntity[]::new))
                        .addTooltips("gregtech.multiblock.pattern.clear_amount_3")
                        .addTooltip("gregtech.multiblock.pattern.error.limited.1", GTValues.VN[tier])
                        .setExactLimit(1)
                        .or(abilities(GtMultiblockAbilities.OUTPUT_ENERGY.get())).setExactLimit(1))
                .where('H', states(getCasingState()).or(autoAbilities(false, true, false, false, true, true, true)))
                .build();
    }

    @Override
    public Component[] getDescription() {
        return new Component[]{Component.translatable("gregtech.multiblock.large_turbine.description")};
    }

    public BlockState getCasingState() {
        return casingState;
    }

    public BlockState getGearBoxState() {
        return gearboxState;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return casingRenderer;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return frontOverlay;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return hasMufflerHatch;
    }

    @Override
    public boolean isStructureObstructed() {
        return super.isStructureObstructed() || !isRotorFaceFree();
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public boolean canVoidRecipeItemOutputs() {
        return true;
    }

    @Override
    public boolean canVoidRecipeFluidOutputs() {
        return true;
    }

    @Override
    protected boolean shouldShowVoidingModeButton() {
        return false;
    }
}
