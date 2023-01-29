package net.nemezanevem.gregtech.api.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;
import net.nemezanevem.gregtech.api.fluids.GtFluidTypes;
import net.nemezanevem.gregtech.api.fluids.type.GTFluidType;
import net.nemezanevem.gregtech.api.unification.material.GtMaterials;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.properties.GtMaterialProperties;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FluidTooltipUtil {

    /**
     * Registry Mapping of <Fluid, Tooltip>
     */
    private static final Map<FluidType, List<Component>> tooltips = new HashMap<>();

    /**
     * Used to register a tooltip to a Fluid.
     *
     * @param fluid   The fluid to register a tooltip for.
     * @param tooltip The tooltip.
     */
    public static void registerTooltip(FluidType fluid, Component tooltip) {
        if (fluid != null && tooltip != null) {
            tooltips.computeIfAbsent(fluid, k -> new ArrayList<>()).add(tooltip);
        }
    }

    /**
     * Used to register a tooltip to a Fluid.
     *
     * @param fluid   The fluid to register a tooltip for.
     * @param tooltip The tooltip.
     */
    public static void registerTooltip(FluidType fluid, List<Component> tooltip) {
        if (fluid != null && tooltip != null && !tooltip.isEmpty()) {
            tooltips.put(fluid, tooltip);
        }
    }

    /**
     * Used to get a Fluid's tooltip.
     *
     * @param fluid The Fluid to get the tooltip of.
     * @return The tooltip.
     */
    public static List<Component> getFluidTooltip(Fluid fluid) {
        if (fluid == null) {
            return null;
        }

        return tooltips.get(fluid);
    }

    /**
     * Used to get a Fluid's tooltip.
     *
     * @param stack A FluidStack, containing the Fluid to get the tooltip of.
     * @return The tooltip.
     */
    public static List<Component> getFluidTooltip(FluidStack stack) {
        if (stack == null) {
            return null;
        }

        return getFluidTooltip(stack.getFluid());
    }

    /**
     * Used to get a Fluid's tooltip.
     *
     * @param fluidName A String representing a Fluid to get the tooltip of.
     * @return The tooltip.
     */
    public static List<Component> getFluidTooltip(ResourceLocation fluidName) {
        if (fluidName == null) {
            return null;
        }

        return getFluidTooltip(ForgeRegistries.FLUIDS.getValue(fluidName));
    }

    /**
     * A simple helper method to get the tooltip for Water, since it is an edge case of fluids.
     */
    @Nonnull
    public static List<Component> getWaterTooltip() {
        return getMaterialTooltip(GtMaterials.Water.get(), GtMaterials.Water.get().getProperty(GtMaterialProperties.FLUID.get()).getFluidTemperature(), false);
    }

    /**
     * A simple helper method to get the tooltip for Lava, since it is an edge case of fluids.
     */
    @Nonnull
    public static List<Component> getLavaTooltip() {
        return getMaterialTooltip(GtMaterials.Lava.get(), GtMaterials.Lava.get().getProperty(GtMaterialProperties.FLUID.get()).getFluidTemperature(), false);
    }

    @Nonnull
    public static List<Component> getMaterialTooltip(@Nonnull Material material, int temperature, boolean isPlasma) {
        List<Component> tooltip = new ArrayList<>();
        if (!material.getChemicalFormula().isEmpty())
            tooltip.add(Component.literal(material.getChemicalFormula()).withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("gregtech.fluid.temperature", temperature));
        if (isPlasma) {
            tooltip.add(Component.translatable(GtFluidTypes.PLASMA.get().getDescriptionId()));
        } else {
            tooltip.add(Component.translatable(material.getProperty(GtMaterialProperties.FLUID.get()).getFluidType().getDescriptionId()));
        }
        //tooltip.addAll(material.getProperty(GtMaterialProperties.FLUID.get()).getFluidType().getDescriptionId());
        if (temperature < 120) {
            // fluids colder than 120K are cryogenic
            tooltip.add(Component.translatable("gregtech.fluid.temperature.cryogenic"));
        }
        return tooltip;
    }
}
