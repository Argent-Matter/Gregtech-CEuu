package net.nemezanevem.gregtech.api.blockentity.multiblock;

import java.util.List;

public interface IMultiblockAbilityPart<T> extends IMultiblockPart {

    MultiblockAbility<T> getAbility();

    void registerAbilities(List<T> abilityList);

}
