package net.nemezanevem.gregtech.api.blockentity.multiblock;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultiblockAbility<T> {
    public static final Map<MultiblockAbility<?>, List<MetaTileEntity>> REGISTRY = new Object2ObjectOpenHashMap<>();

    public static <T> MultiblockAbility<T> registerMultiblockAbility(MultiblockAbility<T> ability, MetaTileEntity part) {
        if (!REGISTRY.containsKey(ability)) {
            REGISTRY.put(ability, new ArrayList<>());
        }
        REGISTRY.get(ability).add(part);
        return ability;
    }

    public MultiblockAbility(){
    }
}
