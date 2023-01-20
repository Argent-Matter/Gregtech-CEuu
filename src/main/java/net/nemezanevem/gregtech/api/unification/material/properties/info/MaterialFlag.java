package net.nemezanevem.gregtech.api.unification.material.properties.info;

import net.minecraft.resources.ResourceLocation;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.properties.PropertyKey;
import net.nemezanevem.gregtech.api.util.Util;

import java.util.*;
import java.util.stream.Collectors;

public class MaterialFlag {

    private static final Set<MaterialFlag> FLAG_REGISTRY = new HashSet<>();

    private final Set<MaterialFlag> requiredFlags;
    private final Set<PropertyKey<?>> requiredProperties;

    private MaterialFlag(Set<MaterialFlag> requiredFlags, Set<PropertyKey<?>> requiredProperties) {
        this.requiredFlags = requiredFlags;
        this.requiredProperties = requiredProperties;
        FLAG_REGISTRY.add(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MaterialFlag flag) {
            return Objects.equals(Util.getId(flag), Util.getId(this));
        }
        return false;
    }

    public Set<MaterialFlag> verifyFlag(Material material) {
        requiredProperties.forEach(key -> {
            if (!material.hasProperty(key)) {
                GregTech.LOGGER.warn("Material {} does not have required property {} for flag {}!", material.getUnlocalizedName(), key.toString(), Util.getId(this));
            }
        });

        Set<MaterialFlag> thisAndDependencies = new HashSet<>(requiredFlags);
        thisAndDependencies.addAll(requiredFlags.stream()
                .map(f -> f.verifyFlag(material))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet()));

        return thisAndDependencies;
    }

    @Override
    public String toString() {
        return Util.getId(this).toString();
    }

    public static MaterialFlag getByName(String name) {
        return FLAG_REGISTRY.stream().filter(f -> f.toString().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static class Builder {

        final Set<MaterialFlag> requiredFlags = new HashSet<>();
        final Set<PropertyKey<?>> requiredProperties = new HashSet<>();

        public Builder() { }

        public Builder requireFlags(MaterialFlag... flags) {
            requiredFlags.addAll(Arrays.asList(flags));
            return this;
        }

        public Builder requireProps(PropertyKey<?>... propertyKeys) {
            requiredProperties.addAll(Arrays.asList(propertyKeys));
            return this;
        }

        public MaterialFlag build() {
            return new MaterialFlag(requiredFlags, requiredProperties);
        }
    }
}
