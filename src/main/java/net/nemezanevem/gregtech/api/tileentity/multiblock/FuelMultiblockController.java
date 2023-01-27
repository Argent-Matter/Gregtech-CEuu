package net.nemezanevem.gregtech.api.tileentity.multiblock;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.MultiblockFuelRecipeLogic;
import gregtech.api.recipes.RecipeType;
import gregtech.api.util.Util;
import gregtech.common.ConfigHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class FuelMultiblockController extends RecipeTypeMultiblockController {

    public FuelMultiblockController(ResourceLocation metaTileEntityId, RecipeType<?> recipeMap, int tier) {
        super(metaTileEntityId, recipeMap);
        this.recipeMapWorkable = new MultiblockFuelRecipeLogic(this);
        this.recipeMapWorkable.setMaximumOverclockVoltage(GTValues.V[tier]);
    }

    @Override
    protected void initializeAbilities() {
        super.initializeAbilities();
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.OUTPUT_ENERGY));
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        if (!isStructureFormed()) {
            ITextComponent tooltip = Component.translatable("gregtech.multiblock.invalid_structure.tooltip");
            tooltip.setStyle(new Style().setColor(TextFormatting.GRAY));
            textList.add(Component.translatable("gregtech.multiblock.invalid_structure")
                    .setStyle(new Style().setColor(TextFormatting.RED)
                            .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))));
        } else {
            if (ConfigHolder.machines.enableMaintenance && hasMaintenanceMechanics())
                addMaintenanceText(textList);

            if (hasMufflerMechanics() && !isMufflerFaceFree())
                textList.add(Component.translatable("gregtech.multiblock.universal.muffler_obstructed")
                        .setStyle(new Style().setColor(TextFormatting.RED)
                                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Component.translatable("gregtech.multiblock.universal.muffler_obstructed.tooltip")))));

            IEnergyContainer energyContainer = recipeMapWorkable.getEnergyContainer();
            if (energyContainer != null && energyContainer.getEnergyCapacity() > 0) {
                long maxVoltage = Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage());
                String voltageName = GTValues.VN[Util.getFloorTierByVoltage(maxVoltage)];
                textList.add(Component.translatable("gregtech.multiblock.max_energy_per_tick", maxVoltage, voltageName));
            }

            if (!recipeMapWorkable.isWorkingEnabled()) {
                textList.add(Component.translatable("gregtech.multiblock.work_paused"));
            } else if (recipeMapWorkable.isActive()) {
                textList.add(Component.translatable("gregtech.multiblock.running"));
                int currentProgress = (int) (recipeMapWorkable.getProgressPercent() * 100);
                textList.add(Component.translatable("gregtech.multiblock.progress", currentProgress));
            } else {
                textList.add(Component.translatable("gregtech.multiblock.idling"));
            }
        }
    }

    @Nonnull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = new ArrayList<>();
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

        if (recipeMapWorkable.getRecipeEUt() < 0) {
            list.add(Component.translatable("behavior.tricorder.workable_production",
                    Component.translatable(Util.formatNumbers(recipeMapWorkable.getRecipeEUt() * -1)).withStyle(ChatFormatting.RED),
                    Component.translatable(Util.formatNumbers(recipeMapWorkable.getRecipeEUt() == 0 ? 0 : 1)).withStyle(ChatFormatting.RED)
            ));
        }

        list.add(Component.translatable("behavior.tricorder.multiblock_energy_output",
                Component.translatable(Util.formatNumbers(energyContainer.getOutputVoltage())).withStyle(ChatFormatting.YELLOW),
                Component.translatable(GTValues.VN[Util.getTierByVoltage(energyContainer.getOutputVoltage())]).withStyle(ChatFormatting.YELLOW)
        ));

        if (ConfigHolder.machines.enableMaintenance && hasMaintenanceMechanics()) {
            list.add(Component.translatable("behavior.tricorder.multiblock_maintenance",
                    Component.translatable(Util.formatNumbers(getNumMaintenanceProblems())).withStyle(ChatFormatting.RED)
            ));
        }

        return list;
    }
}
