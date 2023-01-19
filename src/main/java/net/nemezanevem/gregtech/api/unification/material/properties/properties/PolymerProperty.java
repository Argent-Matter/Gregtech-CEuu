package net.nemezanevem.gregtech.api.unification.material.properties.properties;

import net.nemezanevem.gregtech.api.unification.material.MaterialFlags;
import net.nemezanevem.gregtech.api.unification.material.properties.GtMaterialProperties;
import net.nemezanevem.gregtech.api.unification.material.properties.IMaterialProperty;
import net.nemezanevem.gregtech.api.unification.material.properties.MaterialProperties;

public class PolymerProperty implements IMaterialProperty<PolymerProperty> {


    @Override
    public void verifyProperty(MaterialProperties properties) {

        properties.ensureSet(GtMaterialProperties.DUST.getId(), true);
        properties.ensureSet(GtMaterialProperties., true);
        properties.ensureSet(GtMaterialProperties.FLUID.getId(), true);

        properties.getMaterial().addFlags(MaterialFlags.FLAMMABLE, MaterialFlags.NO_SMASHING, MaterialFlags.DISABLE_DECOMPOSITION);

    }
}
