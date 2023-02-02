package net.nemezanevem.gregtech.api.cover;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.nemezanevem.gregtech.api.registry.GregTechRegistries;

import java.util.function.BiFunction;

public final class CoverDefinition {

    public static CoverDefinition getCoverById(ResourceLocation id) {
        return GregTechRegistries.COVER.get().getValue(id);
    }

    public static int getNetworkIdForCover(CoverDefinition definition) {
        return GregTechAPI.COVER_REGISTRY.getIDForObject(definition);
    }

    private final ResourceLocation coverId;
    private final BiFunction<ICoverable, Direction, CoverBehavior> behaviorCreator;
    private final ItemStack dropItemStack;

    public CoverDefinition(ResourceLocation coverId, BiFunction<ICoverable, Direction, CoverBehavior> behaviorCreator, ItemStack dropItemStack) {
        this.coverId = coverId;
        this.behaviorCreator = behaviorCreator;
        this.dropItemStack = dropItemStack.copy();
    }

    public ResourceLocation getCoverId() {
        return coverId;
    }

    public ItemStack getDropItemStack() {
        return dropItemStack.copy();
    }

    public CoverBehavior createCoverBehavior(ICoverable metaTileEntity, Direction side) {
        CoverBehavior coverBehavior = behaviorCreator.apply(metaTileEntity, side);
        coverBehavior.setCoverDefinition(this);
        return coverBehavior;
    }

}
