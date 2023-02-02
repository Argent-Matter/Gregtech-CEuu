package net.nemezanevem.gregtech.api.blockentity.multiblock;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.capability.IEnergyContainer;
import net.nemezanevem.gregtech.api.capability.impl.EnergyContainerList;
import net.nemezanevem.gregtech.api.capability.impl.MultiblockFuelRecipeLogic;
import net.nemezanevem.gregtech.api.recipe.GTRecipeType;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.common.ConfigHolder;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class FuelMultiblockController extends RecipeTypeMultiblockController {

    public FuelMultiblockController(ResourceLocation metaTileEntityId, GTRecipeType<?> recipeMap, int tier) {
        super(metaTileEntityId, recipeMap);
        this.recipeMapWorkable = new MultiblockFuelRecipeLogic(this);
        this.recipeMapWorkable.setMaximumOverclockVoltage(GTValues.V[tier]);
    }

    @Override
    protected void initializeAbilities() {
        super.initializeAbilities();
        this.energyContainer = new EnergyContainerList(getAbilities(GtMultiblockAbilities.OUTPUT_ENERGY.get()));
    }

    @Override
    protected void addDisplayText(List<Component> textList) {
        if (!isStructureFormed()) {
            MutableComponent tooltip = Component.translatable("gregtech.multiblock.invalid_structure.tooltip");
            tooltip.withStyle(style ->  style.withColor(ChatFormatting.GRAY));
            textList.add(Component.translatable("gregtech.multiblock.invalid_structure")
                    .withStyle(style ->  style.withColor(ChatFormatting.RED)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))));
        } else {
            if (ConfigHolder.machines.enableMaintenance && hasMaintenanceMechanics())
                addMaintenanceText(textList);

            if (hasMufflerMechanics() && !isMufflerFaceFree())
                textList.add(Component.translatable("gregtech.multiblock.universal.muffler_obstructed")
                        .withStyle(style ->  style.withColor(ChatFormatting.RED)
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
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
