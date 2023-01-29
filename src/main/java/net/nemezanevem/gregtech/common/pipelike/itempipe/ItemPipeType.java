package net.nemezanevem.gregtech.common.pipelike.itempipe;

import net.nemezanevem.gregtech.api.pipenet.block.material.IMaterialPipeType;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.ItemPipeProperty;
import net.nemezanevem.gregtech.api.unification.tag.TagPrefix;

import javax.annotation.Nonnull;

public enum ItemPipeType implements IMaterialPipeType<ItemPipeProperty> {
    //TINY_OPAQUE("tiny", 0.25f, TagPrefix.pipeTinyItem, 0.25f, 2f),
    SMALL("small", 0.375f, TagPrefix.pipeSmallItem, 0.5f, 1.5f),
    NORMAL("normal", 0.5f, TagPrefix.pipeNormalItem, 1f, 1f),
    LARGE("large", 0.75f, TagPrefix.pipeLargeItem, 2f, 0.75f),
    HUGE("huge", 0.875f, TagPrefix.pipeHugeItem, 4f, 0.5f),

    RESTRICTIVE_SMALL("small/restrictive", 0.375f, TagPrefix.pipeSmallRestrictive, 0.5f, 150f),
    RESTRICTIVE_NORMAL("normal/restrictive", 0.5f, TagPrefix.pipeNormalRestrictive, 1f, 100f),
    RESTRICTIVE_LARGE("large/restrictive", 0.75f, TagPrefix.pipeLargeRestrictive, 2f, 75f),
    RESTRICTIVE_HUGE("huge/restrictive", 0.875f, TagPrefix.pipeHugeRestrictive, 4f, 50f);

    public final String name;
    private final float thickness;
    private final float rateMultiplier;
    private final float resistanceMultiplier;
    private final TagPrefix orePrefix;

    ItemPipeType(String name, float thickness, TagPrefix orePrefix, float rateMultiplier, float resistanceMultiplier) {
        this.name = name;
        this.thickness = thickness;
        this.orePrefix = orePrefix;
        this.rateMultiplier = rateMultiplier;
        this.resistanceMultiplier = resistanceMultiplier;
    }

    public boolean isRestrictive() {
        return ordinal() > 3;
    }

    public String getSizeForTexture() {
        if (!isRestrictive())
            return name;
        else
            return name.substring(0, name.length() - 12);
    }

    @Override
    public float getThickness() {
        return thickness;
    }

    @Override
    public ItemPipeProperty modifyProperties(ItemPipeProperty baseProperties) {
        return new ItemPipeProperty((int) ((baseProperties.getPriority() * resistanceMultiplier) + 0.5), baseProperties.getTransferRate() * rateMultiplier);
    }

    public float getRateMultiplier() {
        return rateMultiplier;
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    @Nonnull
    @Override
    public String getSerializedName() {
        return name;
    }

    @Override
    public TagPrefix getTagPrefix() {
        return orePrefix;
    }
}
