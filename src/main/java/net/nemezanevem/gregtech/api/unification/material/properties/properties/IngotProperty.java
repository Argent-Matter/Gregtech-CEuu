package net.nemezanevem.gregtech.api.unification.material.properties.properties;

import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.properties.GtMaterialProperties;
import net.nemezanevem.gregtech.api.unification.material.properties.IMaterialProperty;
import net.nemezanevem.gregtech.api.unification.material.properties.MaterialProperties;

import javax.annotation.Nullable;

public class IngotProperty implements IMaterialProperty<IngotProperty> {

    /**
     * Specifies a material into which this material parts turn when heated
     */
    private Material smeltInto;

    /**
     * Specifies a material into which this material parts turn when heated in arc furnace
     */
    private Material arcSmeltInto;

    /**
     * Specifies a Material into which this Material Macerates into.
     * <p>
     * Default: this Material.
     */
    private Material macerateInto;

    /**
     * Material which obtained when this material is polarized
     */
    @Nullable
    private Material magneticMaterial;

    public void setSmeltingInto(Material smeltInto) {
        this.smeltInto = smeltInto;
    }

    public Material getSmeltingInto() {
        return this.smeltInto;
    }

    public void setArcSmeltingInto(Material arcSmeltingInto) {
        this.arcSmeltInto = arcSmeltingInto;
    }

    public Material getArcSmeltInto() {
        return this.arcSmeltInto;
    }

    public void setMagneticMaterial(@Nullable Material magneticMaterial) {
        this.magneticMaterial = magneticMaterial;
    }

    @Nullable
    public Material getMagneticMaterial() {
        return magneticMaterial;
    }

    public void setMacerateInto(Material macerateInto) {
        this.macerateInto = macerateInto;
    }

    public Material getMacerateInto() {
        return macerateInto;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(GtMaterialProperties.DUST.get(), true);
        if (properties.hasProperty(GtMaterialProperties.GEM.get())) {
            throw new IllegalStateException(
                    "Material " + properties.getMaterial() +
                            " has both Ingot and Gem Property, which is not allowed!");
        }

        if (smeltInto == null) smeltInto = properties.getMaterial();
        else smeltInto.getProperties().ensureSet(GtMaterialProperties.INGOT.get(), true);

        if (arcSmeltInto == null) arcSmeltInto = properties.getMaterial();
        else arcSmeltInto.getProperties().ensureSet(GtMaterialProperties.INGOT.get(), true);

        if (macerateInto == null) macerateInto = properties.getMaterial();
        else macerateInto.getProperties().ensureSet(GtMaterialProperties.INGOT.get(), true);

        if (magneticMaterial != null) magneticMaterial.getProperties().ensureSet(GtMaterialProperties.INGOT.get(), true);
    }
}
