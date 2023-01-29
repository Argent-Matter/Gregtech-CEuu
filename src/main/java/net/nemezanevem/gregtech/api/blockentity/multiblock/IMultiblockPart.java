package net.nemezanevem.gregtech.api.blockentity.multiblock;

public interface IMultiblockPart {

    boolean isAttachedToMultiBlock();

    void addToMultiBlock(MultiblockControllerBase controllerBase);

    void removeFromMultiBlock(MultiblockControllerBase controllerBase);

    default boolean canPartShare() {
        return true;
    }

}
