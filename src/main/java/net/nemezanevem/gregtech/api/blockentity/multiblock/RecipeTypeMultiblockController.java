package net.nemezanevem.gregtech.api.blockentity.multiblock;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.blockentity.IDataInfoProvider;
import net.nemezanevem.gregtech.api.capability.IEnergyContainer;
import net.nemezanevem.gregtech.api.capability.IMultipleTankHandler;
import net.nemezanevem.gregtech.api.capability.impl.EnergyContainerList;
import net.nemezanevem.gregtech.api.capability.impl.FluidTankList;
import net.nemezanevem.gregtech.api.capability.impl.ItemHandlerList;
import net.nemezanevem.gregtech.api.capability.impl.MultiblockRecipeLogic;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.gui.widgets.AdvancedTextWidget;
import net.nemezanevem.gregtech.api.pattern.PatternMatchContext;
import net.nemezanevem.gregtech.api.pattern.TraceabilityPredicate;
import net.nemezanevem.gregtech.api.recipe.GTRecipe;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.common.ConfigHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class RecipeTypeMultiblockController extends MultiblockWithDisplayBase implements IDataInfoProvider, ICleanroomReceiver {

    public final GTRecipeType<?> recipeMap;
    protected MultiblockRecipeLogic recipeMapWorkable;
    protected IItemHandlerModifiable inputInventory;
    protected IItemHandlerModifiable outputInventory;
    protected IMultipleTankHandler inputFluidInventory;
    protected IMultipleTankHandler outputFluidInventory;
    protected IEnergyContainer energyContainer;

    private boolean isDistinct = false;

    private ICleanroomProvider cleanroom;

    public RecipeTypeMultiblockController(ResourceLocation metaTileEntityId, GTRecipeType<?> recipeMap) {
        super(metaTileEntityId);
        this.recipeMap = recipeMap;
        this.recipeMapWorkable = new MultiblockRecipeLogic(this);
        resetTileAbilities();
    }

    public IEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    public IItemHandlerModifiable getInputInventory() {
        return inputInventory;
    }

    public IItemHandlerModifiable getOutputInventory() {
        return outputInventory;
    }

    public IMultipleTankHandler getInputFluidInventory() {
        return inputFluidInventory;
    }

    public IMultipleTankHandler getOutputFluidInventory() {
        return outputFluidInventory;
    }

    public MultiblockRecipeLogic getRecipeTypeWorkable() {
        return recipeMapWorkable;
    }

    /**
     * Performs extra checks for validity of given recipe before multiblock
     * will start it's processing.
     */
    public boolean checkRecipe(@Nonnull GTRecipe recipe, boolean consumeIfSuccess) {
        return true;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeAbilities();
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();
        this.recipeMapWorkable.invalidate();
    }

    @Override
    protected void updateFormedValid() {
        if (!hasMufflerMechanics() || isMufflerFaceFree()){
            this.recipeMapWorkable.updateWorkable();
        }
    }

    @Override
    public boolean isActive() {
        return isStructureFormed() && recipeMapWorkable.isActive() && recipeMapWorkable.isWorkingEnabled();
    }

    protected void initializeAbilities() {
        this.inputInventory = new ItemHandlerList(getAbilities(GtMultiblockAbilities.IMPORT_ITEMS.get()));
        this.inputFluidInventory = new FluidTankList(allowSameFluidFillForOutputs(), getAbilities(GtMultiblockAbilities.IMPORT_FLUIDS.get()));
        this.outputInventory = new ItemHandlerList(getAbilities(GtMultiblockAbilities.EXPORT_ITEMS.get()));
        this.outputFluidInventory = new FluidTankList(allowSameFluidFillForOutputs(), getAbilities(GtMultiblockAbilities.EXPORT_FLUIDS.get()));
        this.energyContainer = new EnergyContainerList(getAbilities(GtMultiblockAbilities.INPUT_ENERGY.get()));
    }

    private void resetTileAbilities() {
        this.inputInventory = new ItemStackHandler(0);
        this.inputFluidInventory = new FluidTankList(true);
        this.outputInventory = new ItemStackHandler(0);
        this.outputFluidInventory = new FluidTankList(true);
        this.energyContainer = new EnergyContainerList(Lists.newArrayList());
    }

    protected boolean allowSameFluidFillForOutputs() {
        return true;
    }

    @Override
    protected void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isStructureFormed()) {
            IEnergyContainer energyContainer = recipeMapWorkable.getEnergyContainer();
            if (energyContainer != null && energyContainer.getEnergyCapacity() > 0) {
                long maxVoltage = Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage());
                String voltageName = GTValues.VNF[Util.getFloorTierByVoltage(maxVoltage)];
                textList.add(Component.translatable("gregtech.multiblock.max_energy_per_tick", maxVoltage, voltageName));
            }

            if (canBeDistinct() && inputInventory.getSlots() > 0) {
                Component buttonText = Component.translatable("gregtech.multiblock.universal.distinct");
                buttonText.getSiblings().add(Component.literal(" "));
                Component button = AdvancedTextWidget.withButton(isDistinct() ?
                        Component.translatable("gregtech.multiblock.universal.distinct.yes").withStyle(ChatFormatting.GREEN) :
                        Component.translatable("gregtech.multiblock.universal.distinct.no").withStyle(ChatFormatting.RED), "distinct");
                AdvancedTextWidget.withHoverTextTranslate(button, "gregtech.multiblock.universal.distinct.info");
                buttonText.getSiblings().add(button);
                textList.add(buttonText);
            }

            addExtraDisplayInfo(textList);

            if (!recipeMapWorkable.isWorkingEnabled()) {
                textList.add(Component.translatable("gregtech.multiblock.work_paused"));

            } else if (recipeMapWorkable.isActive()) {
                textList.add(Component.translatable("gregtech.multiblock.running"));
                int currentProgress = (int) (recipeMapWorkable.getProgressPercent() * 100);
                if (this.recipeMapWorkable.getParallelLimit() != 1) {
                    textList.add(Component.translatable("gregtech.multiblock.parallel", this.recipeMapWorkable.getParallelLimit()));
                }
                textList.add(Component.translatable("gregtech.multiblock.progress", currentProgress));
            } else {
                textList.add(Component.translatable("gregtech.multiblock.idling"));
            }

            if (recipeMapWorkable.isHasNotEnoughEnergy()) {
                textList.add(Component.translatable("gregtech.multiblock.not_enough_energy").withStyle(ChatFormatting.RED));
            }
        }
    }

    /**
     * Used for when you want a Multiblock to have extra info in the text, but not put that info after
     * the working status, progress percent, etc.
     */
    protected void addExtraDisplayInfo(List<Component> textList) {
    }

    @Override
    protected void handleDisplayClick(String componentData, Widget.ClickData clickData) {
        super.handleDisplayClick(componentData, clickData);
        toggleDistinct();
    }

    @Override
    public TraceabilityPredicate autoAbilities() {
        return autoAbilities(true, true, true, true, true, true, true);
    }

    public TraceabilityPredicate autoAbilities(boolean checkEnergyIn,
                                               boolean checkMaintenance,
                                               boolean checkItemIn,
                                               boolean checkItemOut,
                                               boolean checkFluidIn,
                                               boolean checkFluidOut,
                                               boolean checkMuffler) {
        TraceabilityPredicate predicate = super.autoAbilities(checkMaintenance, checkMuffler)
                .or(checkEnergyIn ? abilities(GtMultiblockAbilities.INPUT_ENERGY.get()).setMinGlobalLimited(1).setMaxGlobalLimited(3).setPreviewCount(1) : new TraceabilityPredicate());

        if (checkItemIn) {
            if (recipeMap.getMinInputs() > 0) {
                predicate = predicate.or(abilities(GtMultiblockAbilities.IMPORT_ITEMS.get()).setMinGlobalLimited(1).setPreviewCount(1));
            } else if (recipeMap.getMaxInputs() > 0) {
                predicate = predicate.or(abilities(GtMultiblockAbilities.IMPORT_ITEMS.get()).setPreviewCount(1));
            }
        }
        if (checkItemOut) {
            if (recipeMap.getMinOutputs() > 0) {
                predicate = predicate.or(abilities(GtMultiblockAbilities.EXPORT_ITEMS.get()).setMinGlobalLimited(1).setPreviewCount(1));
            } else if (recipeMap.getMaxOutputs() > 0) {
                predicate = predicate.or(abilities(GtMultiblockAbilities.EXPORT_ITEMS.get()).setPreviewCount(1));
            }
        }
        if (checkFluidIn) {
            if (recipeMap.getMinFluidInputs() > 0) {
                predicate = predicate.or(abilities(GtMultiblockAbilities.IMPORT_FLUIDS.get()).setMinGlobalLimited(1).setPreviewCount(recipeMap.getMinFluidInputs()));
            } else if (recipeMap.getMaxFluidInputs() > 0) {
                predicate = predicate.or(abilities(GtMultiblockAbilities.IMPORT_FLUIDS.get()).setPreviewCount(1));
            }
        }
        if (checkFluidOut) {
            if (recipeMap.getMinFluidOutputs() > 0) {
                predicate = predicate.or(abilities(GtMultiblockAbilities.EXPORT_FLUIDS.get()).setMinGlobalLimited(1).setPreviewCount(recipeMap.getMinFluidOutputs()));
            } else if (recipeMap.getMaxFluidOutputs() > 0) {
                predicate = predicate.or(abilities(GtMultiblockAbilities.EXPORT_FLUIDS.get()).setPreviewCount(1));
            }
        }
        return predicate;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), recipeMapWorkable.isActive(), recipeMapWorkable.isWorkingEnabled());
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.putBoolean("isDistinct", isDistinct);
        return data;
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        isDistinct = data.getBoolean("isDistinct");
    }

    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isDistinct);
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        super.receiveInitialSyncData(buf);
        isDistinct = buf.readBoolean();
    }

    public boolean canBeDistinct() {
        return false;
    }

    public boolean isDistinct() {
        return isDistinct && inputInventory.getSlots() > 0;
    }

    protected void toggleDistinct() {
        isDistinct = !isDistinct;
        recipeMapWorkable.onDistinctChanged();
        //mark buses as changed on distinct toggle
        if (isDistinct) {
            this.notifiedItemInputList.addAll(this.getAbilities(GtMultiblockAbilities.IMPORT_ITEMS.get()));
        } else {
            this.notifiedItemInputList.add(this.inputInventory);
        }
    }

    @Override
    public SoundEvent getSound() {
        return recipeMap.getSound();
    }

    @Nonnull
    @Override
    public List<Component> getDataInfo() {
        List<Component> list = new ArrayList<>();
        if (recipeMapWorkable.getMaxProgress() > 0) {
            list.add(Component.translatable("behavior.tricorder.workable_progress",
                    Component.translatable(Util.formatNumbers(recipeMapWorkable.getProgress() / 20)).withStyle(ChatFormatting.GREEN),
                    Component.translatable(Util.formatNumbers(recipeMapWorkable.getMaxProgress() / 20)).withStyle(ChatFormatting.YELLOW)
            ));
        }

        list.add(Component.translatable("behavior.tricorder.energy_container_storage",
                Component.translatable(Util.formatNumbers(energyContainer.getEnergyStored())).withStyle(ChatFormatting.GREEN),
                Component.translatable(Util.formatNumbers(energyContainer.getEnergyCapacity())).withStyle(ChatFormatting.YELLOW)
        ));

        if (recipeMapWorkable.getRecipeEUt() > 0) {
            list.add(Component.translatable("behavior.tricorder.workable_consumption",
                    Component.translatable(Util.formatNumbers(recipeMapWorkable.getRecipeEUt())).withStyle(ChatFormatting.RED),
                    Component.translatable(Util.formatNumbers(recipeMapWorkable.getRecipeEUt() == 0 ? 0 : 1)).withStyle(ChatFormatting.RED)
            ));
        }

        list.add(Component.translatable("behavior.tricorder.multiblock_energy_input",
                Component.translatable(Util.formatNumbers(energyContainer.getInputVoltage())).withStyle(ChatFormatting.YELLOW),
                Component.translatable(GTValues.VN[Util.getTierByVoltage(energyContainer.getInputVoltage())]).withStyle(ChatFormatting.YELLOW)
        ));

        if (ConfigHolder.machines.enableMaintenance && hasMaintenanceMechanics()) {
            list.add(Component.translatable("behavior.tricorder.multiblock_maintenance",
                    Component.translatable(Util.formatNumbers(getNumMaintenanceProblems())).withStyle(ChatFormatting.RED)
            ));
        }

        if (recipeMapWorkable.getParallelLimit() > 1) {
            list.add(Component.translatable("behavior.tricorder.multiblock_parallel",
                    Component.translatable(Util.formatNumbers(recipeMapWorkable.getParallelLimit())).withStyle(ChatFormatting.GREEN)
            ));
        }

        return list;
    }

    @Nullable
    @Override
    public ICleanroomProvider getCleanroom() {
        return this.cleanroom;
    }

    @Override
    public void setCleanroom(ICleanroomProvider provider) {
        this.cleanroom = provider;
    }
}
