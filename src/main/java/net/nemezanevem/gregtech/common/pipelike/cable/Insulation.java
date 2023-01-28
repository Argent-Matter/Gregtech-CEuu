package net.nemezanevem.gregtech.common.pipelike.cable;

import net.nemezanevem.gregtech.api.pipenet.block.material.IMaterialPipeType;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.WireProperty;
import net.nemezanevem.gregtech.api.unification.tag.TagPrefix;

import javax.annotation.Nonnull;

public enum Insulation implements IMaterialPipeType<WireProperty> {

    WIRE_SINGLE("wire_single", 0.125f, 1, 2, TagPrefix.wireGtSingle, -1),
    WIRE_DOUBLE("wire_double", 0.25f, 2, 2, TagPrefix.wireGtDouble, -1),
    WIRE_QUADRUPLE("wire_quadruple", 0.375f, 4, 3, TagPrefix.wireGtQuadruple, -1),
    WIRE_OCTAL("wire_octal", 0.5f, 8, 3, TagPrefix.wireGtOctal, -1),
    WIRE_HEX("wire_hex", 0.75f, 16, 3, TagPrefix.wireGtHex, -1),

    CABLE_SINGLE("cable_single", 0.25f, 1, 1, TagPrefix.cableGtSingle, 0),
    CABLE_DOUBLE("cable_double", 0.375f, 2, 1, TagPrefix.cableGtDouble, 1),
    CABLE_QUADRUPLE("cable_quadruple", 0.5f, 4, 1, TagPrefix.cableGtQuadruple, 2),
    CABLE_OCTAL("cable_octal", 0.75f, 8, 1, TagPrefix.cableGtOctal, 3),
    CABLE_HEX("cable_hex", 1.0f, 16, 1, TagPrefix.cableGtHex, 4);

    public final String name;
    public final float thickness;
    public final int amperage;
    public final int lossMultiplier;
    public final TagPrefix orePrefix;
    public final int insulationLevel;

    Insulation(String name, float thickness, int amperage, int lossMultiplier, TagPrefix orePrefix, int insulated) {
        this.name = name;
        this.thickness = thickness;
        this.amperage = amperage;
        this.orePrefix = orePrefix;
        this.insulationLevel = insulated;
        this.lossMultiplier = lossMultiplier;
    }

    @Nonnull
    @Override
    public String getSerializedName() {
        return name;
    }

    @Override
    public float getThickness() {
        return thickness;
    }

    @Override
    public TagPrefix getTagPrefix() {
        return orePrefix;
    }

    public boolean isCable() {
        return ordinal() > 4;
    }

    @Override
    public WireProperty modifyProperties(WireProperty baseProperties) {

        int lossPerBlock;
        if (!baseProperties.isSuperconductor() && baseProperties.getLossPerBlock() == 0)
            lossPerBlock = (int) (0.75 * lossMultiplier);
        else lossPerBlock = baseProperties.getLossPerBlock() * lossMultiplier;

        return new WireProperty(baseProperties.getVoltage(), baseProperties.getAmperage() * amperage, lossPerBlock, baseProperties.isSuperconductor());
    }

    @Override
    public boolean isPaintable() {
        return true;
    }
}
