package net.nemezanevem.gregtech.api.blockentity.multiblock;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.nemezanevem.gregtech.api.blockentity.MTETrait;
import net.nemezanevem.gregtech.api.capability.IMultipleTankHandler;
import net.nemezanevem.gregtech.api.capability.impl.FluidTankList;
import net.nemezanevem.gregtech.api.capability.impl.ItemHandlerList;
import net.nemezanevem.gregtech.api.capability.impl.SteamMultiblockRecipeLogic;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.widgets.AdvancedTextWidget;
import net.nemezanevem.gregtech.api.pattern.PatternMatchContext;
import net.nemezanevem.gregtech.api.pattern.TraceabilityPredicate;
import net.nemezanevem.gregtech.api.recipe.GTRecipe;
import net.nemezanevem.gregtech.common.ConfigHolder;

import java.util.List;

public abstract class RecipeTypeSteamMultiblockController extends MultiblockWithDisplayBase {

    protected static final double CONVERSION_RATE = ConfigHolder.machines.multiblockSteamToEU;

    public final GTRecipeType<?> recipeMap;
    protected SteamMultiblockRecipeLogic recipeMapWorkable;

    protected IItemHandlerModifiable inputInventory;
    protected IItemHandlerModifiable outputInventory;
    protected IMultipleTankHandler steamFluidTank;

    public RecipeTypeSteamMultiblockController(ResourceLocation metaTileEntityId, GTRecipeType<?> recipeMap, double conversionRate) {
        super(metaTileEntityId);
        this.recipeMap = recipeMap;
        this.recipeMapWorkable = new SteamMultiblockRecipeLogic(this, recipeMap, steamFluidTank, conversionRate);
        resetTileAbilities();
    }

    public IItemHandlerModifiable getInputInventory() {
        return inputInventory;
    }

    public IItemHandlerModifiable getOutputInventory() {
        return outputInventory;
    }

    public IMultipleTankHandler getSteamFluidTank() {
        return steamFluidTank;
    }

    /**
     * Performs extra checks for validity of given recipe before multiblock
     * will start it's processing.
     */
    public boolean checkRecipe(GTRecipe recipe, boolean consumeIfProcess) {
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
    }

    @Override
    protected void updateFormedValid() {
        recipeMapWorkable.tick();
    }

    private void initializeAbilities() {
        this.inputInventory = new ItemHandlerList(getAbilities(GtMultiblockAbilities.STEAM_IMPORT_ITEMS.get()));
        this.outputInventory = new ItemHandlerList(getAbilities(GtMultiblockAbilities.STEAM_EXPORT_ITEMS.get()));
        this.steamFluidTank = new FluidTankList(true, getAbilities(GtMultiblockAbilities.STEAM.get()));
    }

    private void resetTileAbilities() {
        this.inputInventory = new ItemStackHandler(0);
        this.outputInventory = new ItemStackHandler(0);
        this.steamFluidTank = new FluidTankList(true);
    }

    @Override
    protected void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isStructureFormed()) {
            IFluidTank steamFluidTank = recipeMapWorkable.getSteamFluidTankCombined();
            if (steamFluidTank != null && steamFluidTank.getCapacity() > 0) {
                int steamStored = steamFluidTank.getFluidAmount();
                textList.add(Component.translatable("gregtech.multiblock.steam.steam_stored", steamStored, steamFluidTank.getCapacity()));
            }

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
                textList.add(Component.translatable("gregtech.multiblock.steam.low_steam").withStyle(ChatFormatting.RED));
            }
        }
    }

    @Override
    public TraceabilityPredicate autoAbilities() {
        return autoAbilities(true, true, true, true, true);
    }

    public TraceabilityPredicate autoAbilities(boolean checkSteam,
                                               boolean checkMaintainer,
                                               boolean checkItemIn,
                                               boolean checkItemOut,
                                               boolean checkMuffler) {
        TraceabilityPredicate predicate = super.autoAbilities(checkMaintainer, checkMuffler)
                .or(checkSteam ? abilities(GtMultiblockAbilities.STEAM.get()).setMinGlobalLimited(1).setPreviewCount(1) : new TraceabilityPredicate());
        if (checkItemIn) {
            if (recipeMap.getMinInputs() > 0) {
                predicate = predicate.or(abilities(GtMultiblockAbilities.STEAM_IMPORT_ITEMS.get()).setMinGlobalLimited(1).setPreviewCount(1));
            }
            else if (recipeMap.getMaxInputs() > 0) {
                predicate = predicate.or(abilities(GtMultiblockAbilities.STEAM_IMPORT_ITEMS.get()).setPreviewCount(1));
            }
        }
        if (checkItemOut) {
            if (recipeMap.getMinOutputs() > 0) {
                predicate = predicate.or(abilities(GtMultiblockAbilities.STEAM_EXPORT_ITEMS.get()).setMinGlobalLimited(1).setPreviewCount(1));
            }
            else if (recipeMap.getMaxOutputs() > 0) {
                predicate =  predicate.or(abilities(GtMultiblockAbilities.STEAM_EXPORT_ITEMS.get()).setPreviewCount(1));
            }
        }
        return predicate;
    }

    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        return !(trait instanceof SteamMultiblockRecipeLogic);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), recipeMapWorkable.isActive(), recipeMapWorkable.isWorkingEnabled());
    }

    @Override
    public SoundEvent getSound() {
        return recipeMap.getSound();
    }

    @Override
    public boolean isActive() {
        return super.isActive() && recipeMapWorkable.isActive() && recipeMapWorkable.isWorkingEnabled();
    }

    @Override
    protected ModularUI.Builder createUITemplate(Player entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND_STEAM.get(ConfigHolder.machines.steelSteamMultiblocks), 176, 216);
        builder.shouldColor(false);
        builder.image(7, 4, 162, 121, GuiTextures.DISPLAY_STEAM.get(ConfigHolder.machines.steelSteamMultiblocks));
        builder.label(11, 9, getMetaFullName(), 0xFFFFFF);
        builder.widget(new AdvancedTextWidget(11, 19, this::addDisplayText, 0xFFFFFF)
                .setMaxWidthLimit(156)
                .setClickHandler(this::handleDisplayClick));
        builder.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT_STEAM.get(ConfigHolder.machines.steelSteamMultiblocks), 7, 134);
        return builder;
    }
}
