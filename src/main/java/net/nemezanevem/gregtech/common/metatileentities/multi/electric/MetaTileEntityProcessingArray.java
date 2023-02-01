package net.nemezanevem.gregtech.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.IMachineHatchMultiblock;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeTypeMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeType;
import gregtech.api.util.Util;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.core.sound.GTSoundEvents;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.Component;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static gregtech.api.GTValues.ULV;
import static gregtech.api.recipes.logic.OverclockingLogic.standardOverclockingLogic;

public class MetaTileEntityProcessingArray extends RecipeTypeMultiblockController implements IMachineHatchMultiblock {

    private final int tier;
    private boolean machineChanged;

    public MetaTileEntityProcessingArray(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, null);
        this.tier = tier;
        this.recipeMapWorkable = new ProcessingArrayWorkable(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityProcessingArray(metaTileEntityId, tier);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        ((ProcessingArrayWorkable) this.recipeMapWorkable).findMachineStack();
    }

    @Override
    public int getMachineLimit() {
        return tier == 0 ? 16 : 64;
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "X#X", "XXX")
                .aisle("XXX", "XSX", "XXX")
                .where('L', states(getCasingState()))
                .where('S', selfPredicate())
                .where('X', states(getCasingState()).setMinGlobalLimited(tier == 0 ? 11 : 4).or(autoAbilities())
                        .or(abilities(MultiblockAbility.MACHINE_HATCH).setExactLimit(1)))
                .where('#', air())
                .build();
    }

    public BlockState getCasingState() {
        return tier == 0
                ? MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST)
                : MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.HSSE_STURDY);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return tier == 0
                ? Textures.ROBUST_TUNGSTENSTEEL_CASING
                : Textures.ROBUST_HSSE_CASING;
    }

    @Override
    protected void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if(this.isActive()) {
            textList.add(Component.translatable("gregtech.machine.machine_hatch.locked").setStyle(new Style().setColor(TextFormatting.RED)));
        }
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return tier == 0
                ? Textures.PROCESSING_ARRAY_OVERLAY
                : Textures.ADVANCED_PROCESSING_ARRAY_OVERLAY;
    }

    @Override
    public boolean canBeDistinct() {
        return true;
    }

    @Override
    public void notifyMachineChanged() {
        machineChanged = true;
    }

    @Override
    public String[] getBlacklist() {
        return ConfigHolder.machines.processingArrayBlacklist;
    }

    @Override
    public SoundEvent getSound() {
        return GTSoundEvents.ARC;
    }

    @Override
    public TraceabilityPredicate autoAbilities(boolean checkEnergyIn, boolean checkMaintenance, boolean checkItemIn, boolean checkItemOut, boolean checkFluidIn, boolean checkFluidOut, boolean checkMuffler) {
        TraceabilityPredicate predicate = super.autoAbilities(checkMaintenance, checkMuffler)
                .or(checkEnergyIn ? abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(4).setPreviewCount(1) : new TraceabilityPredicate());

        predicate = predicate.or(abilities(MultiblockAbility.IMPORT_ITEMS).setPreviewCount(1));

        predicate = predicate.or(abilities(MultiblockAbility.EXPORT_ITEMS).setPreviewCount(1));

        predicate = predicate.or(abilities(MultiblockAbility.IMPORT_FLUIDS).setPreviewCount(1));

        predicate = predicate.or(abilities(MultiblockAbility.EXPORT_FLUIDS).setPreviewCount(1));

        return predicate;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level player, List<Component> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(Component.translatable("gregtech.universal.tooltip.parallel", getMachineLimit()));
    }

    @Override
    public int getItemOutputLimit() {
        ItemStack machineStack = ((ProcessingArrayWorkable) this.recipeMapWorkable).getMachineStack();
        MetaTileEntity mte = Util.getMetaTileEntity(machineStack);
        return mte == null ? 0 : mte.getItemOutputLimit();

    }

    @SuppressWarnings("InnerClassMayBeStatic")
    protected class ProcessingArrayWorkable extends MultiblockRecipeLogic {

        ItemStack currentMachineStack = ItemStack.EMPTY;
        //The Voltage Tier of the machines the PA is operating upon, from GTValues.V
        private int machineTier;
        //The maximum Voltage of the machines the PA is operating upon
        private long machineVoltage;
        //The Recipe Map of the machines the PA is operating upon
        private GTRecipeType<?> activeRecipeType;

        public ProcessingArrayWorkable(RecipeTypeMultiblockController tileEntity) {
            super(tileEntity);
        }

        @Override
        public void invalidate() {
            super.invalidate();
            // Reset locally cached variables upon invalidation
            currentMachineStack = ItemStack.EMPTY;
            machineChanged = true;
            machineTier = 0;
            machineVoltage = 0L;
            activeRecipeType = null;
        }

        /**
         * Checks if a provided Recipe Map is valid to be used in the processing array
         * Will filter out anything in the config blacklist, and also any non-singleblock machines
         *
         * @param recipeMap The recipeMap to check
         * @return {@code true} if the provided recipeMap is valid for use
         */
        @Override
        public boolean isRecipeTypeValid(@Nonnull GTRecipeType<?> recipeMap) {
            if (Util.findMachineInBlacklist(recipeMap.getUnlocalizedName(), ((IMachineHatchMultiblock) metaTileEntity).getBlacklist())) {
                return false;
            }

            return Util.isMachineValidForMachineHatch(currentMachineStack, ((IMachineHatchMultiblock) metaTileEntity).getBlacklist());
        }

        @Override
        protected boolean shouldSearchForRecipes() {
            return canWorkWithMachines() && super.shouldSearchForRecipes();
        }

        public boolean canWorkWithMachines() {
            if (machineChanged) {
                findMachineStack();
                machineChanged = false;
                previousRecipe = null;
                if (isDistinct()) {
                    invalidatedInputList.clear();
                } else {
                    invalidInputsForRecipes = false;
                }
            }
            return (!currentMachineStack.isEmpty() && this.activeRecipeType != null);
        }

        @Nullable
        @Override
        public GTRecipeType<?> getRecipeType() {
            return activeRecipeType;
        }

        public void findMachineStack() {
            RecipeTypeMultiblockController controller = (RecipeTypeMultiblockController) this.metaTileEntity;

            //The Processing Array is limited to 1 Machine Interface per multiblock, and only has 1 slot
            ItemStack machine = controller.getAbilities(MultiblockAbility.MACHINE_HATCH).get(0).getStackInSlot(0);


            MetaTileEntity mte = Util.getMetaTileEntity(machine);

            if (mte == null)
                this.activeRecipeType = null;
            else
                this.activeRecipeType = mte.getRecipeType();


            //Find the voltage tier of the machine.
            this.machineTier = mte instanceof ITieredMetaTileEntity ? ((ITieredMetaTileEntity) mte).getTier() : 0;

            this.machineVoltage = GTValues.V[this.machineTier];

            this.currentMachineStack = machine;
        }

        @Override
        protected int getOverclockForTier(long voltage) {
            return super.getOverclockForTier(Math.min(machineVoltage, getMaximumOverclockVoltage()));
        }

        @Override
        public int getParallelLimit() {
            return (currentMachineStack == null || currentMachineStack.isEmpty()) ? getMachineLimit() : Math.min(currentMachineStack.getCount(), getMachineLimit());
        }

        @Override
        protected Recipe findRecipe(long maxVoltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs) {
            return super.findRecipe(Math.min(super.getMaxVoltage(), this.machineVoltage), inputs, fluidInputs);
        }

        @Override
        protected int[] calculateOverclock(@Nonnull Recipe recipe) {
            int recipeEUt = recipe.getEUt();
            int recipeDuration = recipe.getDuration();
            if (!isAllowOverclocking()) {
                return new int[]{recipeEUt, recipeDuration};
            }

            // apply maintenance penalties
            Tuple<Integer, Double> maintenanceValues = getMaintenanceValues();

            int originalTier = Math.max(0, Util.getTierByVoltage(recipeEUt / Math.max(1, this.parallelRecipesPerformed)));
            int numOverclocks = Math.min(this.machineTier, Util.getTierByVoltage(getMaxVoltage())) - originalTier;

            if (originalTier == ULV) numOverclocks--; // no ULV overclocking

            // cannot overclock, so return the starting values
            if (numOverclocks <= 0) return new int[]{recipe.getEUt(), recipe.getDuration()};

            return standardOverclockingLogic(
                    recipeEUt,
                    getMaximumOverclockVoltage(),
                    (int) Math.round(recipeDuration * maintenanceValues.getSecond()),
                    numOverclocks,
                    getOverclockingDurationDivisor(),
                    getOverclockingVoltageMultiplier()
            );
        }

        private ItemStack getMachineStack() {
            return currentMachineStack;
        }
    }
}
