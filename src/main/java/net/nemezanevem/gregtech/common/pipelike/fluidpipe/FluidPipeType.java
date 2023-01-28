package net.nemezanevem.gregtech.common.pipelike.fluidpipe;

import gregtech.api.pipenet.block.material.IMaterialPipeType;
import gregtech.api.unification.material.properties.FluidPipeProperty;
import gregtech.api.unification.ore.TagPrefix;
import net.nemezanevem.gregtech.api.pipenet.block.material.IMaterialPipeType;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.FluidPipeProperty;
import net.nemezanevem.gregtech.api.unification.tag.TagPrefix;

import javax.annotation.Nonnull;

public enum FluidPipeType implements IMaterialPipeType<FluidPipeProperty> {

    TINY("tiny", 0.25f, 1, TagPrefix.pipeTinyFluid, true),
    SMALL("small", 0.375f, 2, TagPrefix.pipeSmallFluid, true),
    NORMAL("normal", 0.5f, 6, TagPrefix.pipeNormalFluid, true),
    LARGE("large", 0.75f, 12, TagPrefix.pipeLargeFluid, true),
    HUGE("huge", 0.875f, 24, TagPrefix.pipeHugeFluid, true),

    QUADRUPLE("quadruple", 0.95f, 2, TagPrefix.pipeQuadrupleFluid, true, 4),
    NONUPLE("nonuple", 0.95f, 2, TagPrefix.pipeNonupleFluid, true, 9);

    public final String name;
    public final float thickness;
    public final int capacityMultiplier;
    public final TagPrefix orePrefix;
    public final boolean opaque;
    public final int channels;

    FluidPipeType(String name, float thickness, int capacityMultiplier, TagPrefix orePrefix, boolean opaque) {
        this(name, thickness, capacityMultiplier, orePrefix, opaque, 1);
    }

    FluidPipeType(String name, float thickness, int capacityMultiplier, TagPrefix orePrefix, boolean opaque, int channels) {
        this.name = name;
        this.thickness = thickness;
        this.capacityMultiplier = capacityMultiplier;
        this.orePrefix = orePrefix;
        this.opaque = opaque;
        this.channels = channels;
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

    @Override
    public FluidPipeProperty modifyProperties(FluidPipeProperty baseProperties) {
        return new FluidPipeProperty(
                baseProperties.getMaxFluidTemperature(),
                baseProperties.getThroughput() * capacityMultiplier,
                baseProperties.isGasProof(),
                baseProperties.isAcidProof(),
                baseProperties.isCryoProof(),
                baseProperties.isPlasmaProof(),
                channels);
    }

    @Override
    public boolean isPaintable() {
        return true;
    }
}
