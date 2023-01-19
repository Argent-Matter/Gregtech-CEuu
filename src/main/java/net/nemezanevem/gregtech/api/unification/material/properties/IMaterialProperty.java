package net.nemezanevem.gregtech.api.unification.material.properties;

@FunctionalInterface
public interface IMaterialProperty<T extends IMaterialProperty<T>> {

    void verifyProperty(MaterialProperties properties);
}
