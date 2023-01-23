package net.nemezanevem.gregtech.api.unification.tag;

import net.nemezanevem.gregtech.api.unification.material.Material;

@FunctionalInterface
public interface IOreRegistrationHandler {

    void processMaterial(TagPrefix orePrefix, Material material);

}
