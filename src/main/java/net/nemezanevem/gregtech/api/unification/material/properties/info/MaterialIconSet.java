package net.nemezanevem.gregtech.api.unification.material.properties.info;

import com.google.common.base.Preconditions;
import net.minecraft.resources.ResourceLocation;
import net.nemezanevem.gregtech.GregTech;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MaterialIconSet {

    public static final Map<String, MaterialIconSet> ICON_SETS = new HashMap<>();


    // Implementation -----------------------------------------------------------------------------------------------

    private static int idCounter = 0;

    public final String name;
    public final ResourceLocation id;
    public final boolean isRootIconset;

    /**
     * This can be null if {@link MaterialIconSet#isRootIconset} is true,
     * otherwise it will be Nonnull
     */
    public final MaterialIconSet parentIconset;

    /**
     * Create a new MaterialIconSet whose parent is {@link GtMaterialIconSets#DULL}
     *
     * @param name the name of the iconset
     */
    public MaterialIconSet(@Nonnull String name) {
        this(name, GtMaterialIconSets.DULL.get());
    }

    /**
     * Create a new MaterialIconSet whose parent is one of your choosing
     *
     * @param name          the name of the iconset
     * @param parentIconset the parent iconset
     */
    public MaterialIconSet(@Nonnull String name, @Nonnull MaterialIconSet parentIconset) {
        this(name, parentIconset, false);
    }

    /**
     * Create a new MaterialIconSet which is a root
     * @param name          the name of the iconset
     * @param parentIconset the parent iconset, should be null if this should be a root iconset
     * @param isRootIconset true if this should be a root iconset, otherwise false
     */
    public MaterialIconSet(@Nonnull String name, @Nullable MaterialIconSet parentIconset, boolean isRootIconset) {
        this.name = name.toLowerCase(Locale.ENGLISH);
        Preconditions.checkArgument(!ICON_SETS.containsKey(this.name), "MaterialIconSet " + this.name + " already registered!");
        this.id = name.contains(":") ? new ResourceLocation(name) : new ResourceLocation(GregTech.MODID, name);
        this.isRootIconset = isRootIconset;
        this.parentIconset = parentIconset;
        ICON_SETS.put(this.name, this);
    }

    public static MaterialIconSet getByName(@Nonnull String name) {
        return ICON_SETS.get(name.toLowerCase(Locale.ENGLISH));
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }
}