package net.nemezanevem.gregtech.api.fluids.type;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.properties.GtMaterialProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GTFluidType {

    private static final Map<String, GTFluidType> FLUID_TYPES = new HashMap<>();

    private final String name;
    private final String prefix;
    private final String suffix;
    protected final String localization;

    public GTFluidType(@Nonnull String name, @Nullable String prefix, @Nullable String suffix, @Nonnull String localization) {
        if (FLUID_TYPES.get(name) != null)
            throw new IllegalArgumentException("Cannot register FluidType with duplicate name: " + name);

        this.name = name;
        this.prefix = prefix;
        this.suffix = suffix;
        this.localization = localization;
        FLUID_TYPES.put(name, this);
    }

    public String getNameForMaterial(@Nonnull Material material) {
        StringBuilder builder = new StringBuilder();

        if (this.prefix != null)
            builder.append(this.prefix).append(".");

        builder.append(material);

        if (this.suffix != null)
            builder.append(".").append(this.suffix);

        return builder.toString();
    }

    public static void setFluidProperties(@Nonnull GTFluidType fluidType, @Nonnull Fluid fluid) {
        fluidType.setFluidProperties(fluid);
    }

    protected abstract void setFluidProperties(@Nonnull Fluid fluid);

    public void setFluidPropertiesCT(GTFluidType fluidType, Material material) {
        if (material == null) {
            GregTech.LOGGER.warn("Material cannot be null!");
            return;
        }
        if (!material.hasProperty(GtMaterialProperties.FLUID.get())) {
            GregTech.LOGGER.warn("Material {} does not have a FluidProperty!", material.getUnlocalizedName());
            return;
        }

        fluidType.setFluidProperties(material.getFluid());
    }

    public String getLocalization() {
        return this.localization;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getName() {
        return this.name;
    }

    public abstract String getUnlocalizedTooltip();

    public List<Component> getAdditionalTooltips() {
        return new ArrayList<>();
    }

    @Nullable
    public static GTFluidType getByName(@Nonnull String name) {
        return FLUID_TYPES.get(name);
    }
}
