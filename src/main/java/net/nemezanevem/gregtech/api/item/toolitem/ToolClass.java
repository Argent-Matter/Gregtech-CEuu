package net.nemezanevem.gregtech.api.item.toolitem;

import com.google.common.collect.ImmutableSet;
import net.minecraft.util.StringRepresentable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public enum ToolClass implements StringRepresentable {

    SWORD("sword"),
    PICKAXE("pickaxe", null),
    SHOVEL("shovel"),
    AXE("axe"),
    HOE("hoe"),
    SAW("saw"),
    HARD_HAMMER("hammer"),
    SOFT_MALLET("mallet"),
    WRENCH("wrench"),
    FILE("file"),
    CROWBAR("crowbar"),
    SCREWDRIVER("screwdriver"),
    MORTAR("mortar"),
    WIRE_CUTTER("wirecutter"),
    SCYTHE("scythe"),
    SHEARS("shears"),
    KNIFE("knife"),
    BUTCHERY_KNIFE("butchery_knife"),
    GRAFTER("grafter"),
    PLUNGER("plunger"),

    DRILL("drill", ImmutableSet.of(PICKAXE, SHOVEL));

    ToolClass(String name) {
        this.name = name;
        this.subTypes = null;
    }

    ToolClass(String name, @Nullable Set<ToolClass> subTypes) {
        this.name = name;
        this.subTypes = subTypes;
    }

    private final String name;
    @Nullable
    private final Set<ToolClass> subTypes;

    @Nullable
    public Set<ToolClass> getSubTypes() {
        return subTypes;
    }

    @Nonnull
    @Override
    public String getSerializedName() {
        return name;
    }
}
