package net.nemezanevem.gregtech.api.capability;

public interface IMultiblockController {

    boolean isStructureFormed();

    default boolean isStructureObstructed() {
        return false;
    }
}
