package net.nemezanevem.gregtech.api.unification.material.properties.properties;

import net.nemezanevem.gregtech.api.unification.material.properties.GtMaterialProperties;
import net.nemezanevem.gregtech.api.unification.material.properties.IMaterialProperty;
import net.nemezanevem.gregtech.api.unification.material.properties.MaterialProperties;

public class GemProperty implements IMaterialProperty<GemProperty> {

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(GtMaterialProperties.DUST.get(), true);
        if (properties.hasProperty(GtMaterialProperties.INGOT.getId())) {
            throw new IllegalStateException(
                    "Material " + properties.getMaterial() +
                            " has both Ingot and Gem Property, which is not allowed!");
        }
    }
}
