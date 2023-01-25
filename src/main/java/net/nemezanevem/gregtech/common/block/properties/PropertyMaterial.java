package net.nemezanevem.gregtech.common.block.properties;

import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.Property;
import net.nemezanevem.gregtech.api.registry.material.MaterialRegistry;
import net.nemezanevem.gregtech.api.unification.material.GtMaterials;
import net.nemezanevem.gregtech.api.unification.material.Material;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public class PropertyMaterial extends Property<Material> {

    private final ImmutableList<Material> allowedValues;

    protected PropertyMaterial(String name, Collection<? extends Material> allowedValues) {
        super(name, Material.class);
        this.allowedValues = ImmutableList.copyOf(allowedValues);
    }

    public static PropertyMaterial create(String name, Collection<? extends Material> allowedValues) {
        return new PropertyMaterial(name, allowedValues);
    }

    public static PropertyMaterial create(String name, Material[] allowedValues) {
        return new PropertyMaterial(name, Arrays.asList(allowedValues));
    }

    @Nonnull
    @Override
    public Optional<Material> getValue(@Nonnull String value) {
        Material material = MaterialRegistry.MATERIALS_BUILTIN.get().getValue(new ResourceLocation(value));
        if (this.allowedValues.contains(material)) {
            return Optional.of(material);
        }
        return Optional.of(GtMaterials.NULL);
    }

    @Nonnull
    @Override
    public Collection<Material> getPossibleValues() {
        return allowedValues;
    }

    @Nonnull
    @Override
    public String getName(Material material) {
        return material.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof PropertyMaterial) {
            PropertyMaterial propertyMaterial = (PropertyMaterial) obj;
            return this.allowedValues.equals(propertyMaterial.allowedValues);
        } else {
            return false;
        }
    }

}
