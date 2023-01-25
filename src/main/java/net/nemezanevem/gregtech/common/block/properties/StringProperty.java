package net.nemezanevem.gregtech.common.block.properties;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.Property;
import net.nemezanevem.gregtech.api.registry.material.MaterialRegistry;
import net.nemezanevem.gregtech.api.unification.material.GtMaterials;
import net.nemezanevem.gregtech.api.unification.material.Material;

import javax.annotation.Nonnull;
import java.util.*;

public class StringProperty extends Property<String> {

    private final ImmutableList<String> allowedValues;

    private final BiMap<String, String> values;

    protected StringProperty(String name, Collection<String> allowedValues) {
        super(name, String.class);
        this.allowedValues = ImmutableList.copyOf(allowedValues);
        this.values = HashBiMap.create();
    }

    public static StringProperty create(String name, Collection<String> allowedValues) {
        return new StringProperty(name, allowedValues);
    }

    public static StringProperty create(String name, String[] allowedValues) {
        return new StringProperty(name, Arrays.asList(allowedValues));
    }

    @Nonnull
    @Override
    public Optional<String> getValue(@Nonnull String key) {
        return Optional.ofNullable(values.get(key));
    }

    @Nonnull
    @Override
    public Collection<String> getPossibleValues() {
        return allowedValues;
    }

    @Nonnull
    @Override
    public String getName(String value) {
        return values.inverse().get(value);
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
