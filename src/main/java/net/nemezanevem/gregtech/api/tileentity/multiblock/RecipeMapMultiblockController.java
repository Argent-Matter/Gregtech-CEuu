package net.nemezanevem.gregtech.api.tileentity.multiblock;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.google.common.collect.Lists;
import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeType;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.nemezanevem.gregtech.api.capability.IEnergyContainer;
import net.nemezanevem.gregtech.api.capability.IMultipleTankHandler;
import net.nemezanevem.gregtech.api.capability.impl.EnergyContainerList;
import net.nemezanevem.gregtech.api.capability.impl.FluidTankList;
import net.nemezanevem.gregtech.api.capability.impl.ItemHandlerList;
import net.nemezanevem.gregtech.api.capability.impl.MultiblockRecipeLogic;
import net.nemezanevem.gregtech.api.recipe.GTRecipe;
import net.nemezanevem.gregtech.api.tileentity.IDataInfoProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class RecipeTypeMultiblockController extends MultiblockWithDisplayBase implements IDataInfoProvider, ICleanroomReceiver {

    public final RecipeType<?> recipeMap;
    protected MultiblockRecipeLogic recipeMapWorkable;
    protected IItemHandlerModifiable inputInventory;
    protected IItemHandlerModifiable outputInventory;
    protected IMultipleTankHandler inputFluidInventory;
    protected IMultipleTankHandler outputFluidInventory;
    protected IEnergyContainer energyContainer;

    private boolean isDistinct = false;

    private ICleanroomProvider cleanroom;

    public RecipeTypeMultiblockController(ResourceLocation metaTileEntityId, RecipeType<?> recipeMap) {
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
        this.inputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.inputFluidInventory = new FluidTankList(allowSameFluidFillForOutputs(), getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.outputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.EXPORT_ITEMS));
        this.outputFluidInventory = new FluidTankList(allowSameFluidFillForOutputs(), getAbilities(MultiblockAbility.EXPORT_FLUIDS));
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
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
                String voltageName = GTValues.VNF[GTUtility.getFloorTierByVoltage(maxVoltage)];
                textList.add(Component.translatable("gregtech.multiblock.max_energy_per_tick", maxVoltage, voltageName));
            }

            if (canBeDistinct() && inputInventory.getSlots() > 0) {
                ITextComponent buttonText = Component.translatable("gregtech.multiblock.universal.distinct");
                buttonText.appendText(" ");
                ITextComponent button = AdvancedTextWidget.withButton(isDistinct() ?
                        Component.translatable("gregtech.multiblock.universal.distinct.yes").withStyle(ChatFormatting.GREEN) :
                        Component.translatable("gregtech.multiblock.universal.distinct.no").withStyle(ChatFormatting.RED), "distinct");
                AdvancedTextWidget.withHoverTextTranslate(button, "gregtech.multiblock.universal.distinct.info");
                buttonText.appendSibling(button);
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
    protected void addExtraDisplayInfo(List<ITextComponent> textList) {
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
                .or(checkEnergyIn ? abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(3).setPreviewCount(1) : new TraceabilityPredicate());

        if (checkItemIn) {
            if (recipeMap.getMinInputs() > 0) {
                predicate = predicate.or(abilities(MultiblockAbility.IMPORT_ITEMS).setMinGlobalLimited(1).setPreviewCount(1));
            } else if (recipeMap.getMaxInputs() > 0) {
                predicate = predicate.or(abilities(MultiblockAbility.IMPORT_ITEMS).setPreviewCount(1));
            }
        }
        if (checkItemOut) {
            if (recipeMap.getMinOutputs() > 0) {
                predicate = predicate.or(abilities(MultiblockAbility.EXPORT_ITEMS).setMinGlobalLimited(1).setPreviewCount(1));
            } else if (recipeMap.getMaxOutputs() > 0) {
                predicate = predicate.or(abilities(MultiblockAbility.EXPORT_ITEMS).setPreviewCount(1));
            }
        }
        if (checkFluidIn) {
            if (recipeMap.getMinFluidInputs() > 0) {
                predicate = predicate.or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(recipeMap.getMinFluidInputs()));
            } else if (recipeMap.getMaxFluidInputs() > 0) {
                predicate = predicate.or(abilities(MultiblockAbility.IMPORT_FLUIDS).setPreviewCount(1));
            }
        }
        if (checkFluidOut) {
            if (recipeMap.getMinFluidOutputs() > 0) {
                predicate = predicate.or(abilities(MultiblockAbility.EXPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(recipeMap.getMinFluidOutputs()));
            } else if (recipeMap.getMaxFluidOutputs() > 0) {
                predicate = predicate.or(abilities(MultiblockAbility.EXPORT_FLUIDS).setPreviewCount(1));
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
        data.setBoolean("isDistinct", isDistinct);
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
            this.notifiedItemInputList.addAll(this.getAbilities(MultiblockAbility.IMPORT_ITEMS));
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
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = new ArrayList<>();
        if (recipeMapWorkable.getMaxProgress() > 0) {
            list.add(Component.translatable("behavior.tricorder.workable_progress",
                    Component.translatable(GTUtility.formatNumbers(recipeMapWorkable.getProgress() / 20)).withStyle(ChatFormatting.GREEN),
                    Component.translatable(GTUtility.formatNumbers(recipeMapWorkable.getMaxProgress() / 20)).withStyle(ChatFormatting.YELLOW)
            ));
        }

        list.add(Component.translatable("behavior.tricorder.energy_container_storage",
                Component.translatable(GTUtility.formatNumbers(energyContainer.getEnergyStored())).withStyle(ChatFormatting.GREEN),
                Component.translatable(GTUtility.formatNumbers(energyContainer.getEnergyCapacity())).withStyle(ChatFormatting.YELLOW)
        ));

        if (recipeMapWorkable.getRecipeEUt() > 0) {
            list.add(Component.translatable("behavior.tricorder.workable_consumption",
                    Component.translatable(GTUtility.formatNumbers(recipeMapWorkable.getRecipeEUt())).withStyle(ChatFormatting.RED),
                    Component.translatable(GTUtility.formatNumbers(recipeMapWorkable.getRecipeEUt() == 0 ? 0 : 1)).withStyle(ChatFormatting.RED)
            ));
        }

        list.add(Component.translatable("behavior.tricorder.multiblock_energy_input",
                Component.translatable(GTUtility.formatNumbers(energyContainer.getInputVoltage())).withStyle(ChatFormatting.YELLOW),
                Component.translatable(GTValues.VN[GTUtility.getTierByVoltage(energyContainer.getInputVoltage())]).withStyle(ChatFormatting.YELLOW)
        ));

        if (ConfigHolder.machines.enableMaintenance && hasMaintenanceMechanics()) {
            list.add(Component.translatable("behavior.tricorder.multiblock_maintenance",
                    Component.translatable(GTUtility.formatNumbers(getNumMaintenanceProblems())).withStyle(ChatFormatting.RED)
            ));
        }

        if (recipeMapWorkable.getParallelLimit() > 1) {
            list.add(Component.translatable("behavior.tricorder.multiblock_parallel",
                    Component.translatable(GTUtility.formatNumbers(recipeMapWorkable.getParallelLimit())).withStyle(ChatFormatting.GREEN)
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
