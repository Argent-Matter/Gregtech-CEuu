package net.nemezanevem.gregtech.api.unification.stack;

import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.tag.TagPrefix;

import javax.annotation.Nullable;
import java.util.Objects;

public class UnificationEntry {

    public final TagPrefix tagPrefix;
    @Nullable
    public final Material material;

    public UnificationEntry(TagPrefix orePrefix, @Nullable Material material) {
        this.tagPrefix = orePrefix;
        this.material = material;
    }

    public UnificationEntry(TagPrefix orePrefix) {
        this.tagPrefix = orePrefix;
        this.material = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UnificationEntry that = (UnificationEntry) o;

        if (tagPrefix != that.tagPrefix) return false;
        return Objects.equals(material, that.material);
    }

    @Override
    public int hashCode() {
        int result = tagPrefix.hashCode();
        result = 31 * result + (material != null ? material.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return tagPrefix.name() + (material != null ? material.toLowerUnderscoreString() : "");
    }

}
