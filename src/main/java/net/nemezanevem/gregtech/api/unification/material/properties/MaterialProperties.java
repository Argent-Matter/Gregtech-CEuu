package net.nemezanevem.gregtech.api.unification.material.properties;

import net.minecraft.resources.ResourceLocation;
import net.nemezanevem.gregtech.api.registry.material.properties.MaterialPropertyRegistry;
import net.nemezanevem.gregtech.api.unification.material.Material;

import java.util.*;

public class MaterialProperties {

    private static final Set<IMaterialProperty<?>> baseTypes = new HashSet<>(Arrays.asList(
            GtMaterialProperties.PLASMA.get(), GtMaterialProperties.FLUID.get(), GtMaterialProperties.DUST.get(),
            GtMaterialProperties.INGOT.get(), GtMaterialProperties.GEM.get(), GtMaterialProperties.EMPTY.get()
    ));

    @SuppressWarnings("unused")
    public static void addBaseType(IMaterialProperty<?> baseType) {
        baseTypes.add(baseType);
    }

    private final Map<ResourceLocation, IMaterialProperty<?>> propertySet;
    private Material material;

    public MaterialProperties() {
        propertySet = new HashMap<>();
    }

    public boolean isEmpty() {
        return propertySet.isEmpty();
    }

    public <T extends IMaterialProperty<T>> T getProperty(ResourceLocation key) {
        return (T) MaterialPropertyRegistry.MATERIAL_PROPERTIES_BUILTIN.get().getValue(key);
    }

    public <T extends IMaterialProperty<T>> boolean hasProperty(ResourceLocation key) {
        return propertySet.get(key) != null;
    }

    public <T extends IMaterialProperty<T>> void setProperty(ResourceLocation key, IMaterialProperty<T> value) {
        if (value == null) throw new IllegalArgumentException("Material Property must not be null!");
        if (hasProperty(key))
            throw new IllegalArgumentException("Material Property " + key.toString() + " already registered!");
        propertySet.put(key, value);
        propertySet.remove(GtMaterialProperties.EMPTY.getId());
    }

    public <T extends IMaterialProperty<T>> void ensureSet(ResourceLocation key, boolean verify) {
        if (!hasProperty(key)) {
            propertySet.put(key, MaterialPropertyRegistry.MATERIAL_PROPERTIES_BUILTIN.get().getValue(key));
            propertySet.remove(GtMaterialProperties.EMPTY.getId());
            if (verify) verify();
        }
    }

    public <T extends IMaterialProperty<T>> void ensureSet(ResourceLocation key) {
        ensureSet(key, false);
    }

    public void verify() {
        List<IMaterialProperty<?>> oldList;
        do {
            oldList = new ArrayList<>(propertySet.values());
            oldList.forEach(p -> p.verifyProperty(this));
        } while (oldList.size() != propertySet.size());

        if (propertySet.keySet().stream().noneMatch(baseTypes::contains)) {
            if (propertySet.isEmpty()) {
                /*if (ConfigHolder.misc.debug) {
                    GTLog.logger.debug("Creating empty placeholder Material {}", material);
                }*/
                propertySet.put(GtMaterialProperties.EMPTY.getId(), GtMaterialProperties.EMPTY.get());
            } else throw new IllegalArgumentException("Material must have at least one of: " + baseTypes + " specified!");
        }
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        propertySet.forEach((k, v) -> sb.append(k.toString()).append("\n"));
        return sb.toString();
    }
}
