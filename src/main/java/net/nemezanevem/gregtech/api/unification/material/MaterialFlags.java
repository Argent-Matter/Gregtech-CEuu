package net.nemezanevem.gregtech.api.unification.material;

import net.nemezanevem.gregtech.api.unification.material.properties.info.MaterialFlag;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MaterialFlags {
    private final Set<MaterialFlag> flags = new HashSet<>();

    public MaterialFlags addFlags(MaterialFlag... flags) {
        this.flags.addAll(Arrays.asList(flags));
        return this;
    }

    public void verify(Material material) {
        flags.addAll(flags.stream()
                .map(f -> f.verifyFlag(material))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet()));
    }

    public boolean hasFlag(MaterialFlag flag) {
        return flags.contains(flag);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        flags.forEach(f -> sb.append(f.toString()).append("\n"));
        return sb.toString();
    }

}
