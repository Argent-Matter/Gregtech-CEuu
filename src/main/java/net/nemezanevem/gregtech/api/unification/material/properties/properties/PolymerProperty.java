package net.nemezanevem.gregtech.api.unification.material.properties.properties;

import net.nemezanevem.gregtech.api.unification.material.MaterialFlags;
import net.nemezanevem.gregtech.api.unification.material.properties.GtMaterialProperties;
import net.nemezanevem.gregtech.api.unification.material.properties.IMaterialProperty;
import net.nemezanevem.gregtech.api.unification.material.properties.MaterialProperties;
import net.nemezanevem.gregtech.api.unification.material.properties.info.GtMaterialFlags;

public class PolymerProperty implements IMaterialProperty<PolymerProperty> {


    @Override
    public void verifyProperty(MaterialProperties properties) {

        properties.ensureSet(GtMaterialProperties.DUST.getId(), true);
        properties.ensureSet(GtMaterialProperties.INGOT.getId(), true);
        properties.ensureSet(GtMaterialProperties.FLUID.getId(), true);

        properties.getMaterial().addFlags(GtMaterialFlags.FLAMMABLE.get(), GtMaterialFlags.NO_SMASHING.get(), GtMaterialFlags.DISABLE_DECOMPOSITION.get());

    }
}
