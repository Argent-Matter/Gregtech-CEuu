package net.nemezanevem.gregtech.api.block;

import net.minecraft.world.level.block.state.BlockState;
import net.nemezanevem.gregtech.api.item.toolitem.ToolClass;

public interface IStateHarvestLevel {

    int getHarvestLevel(BlockState state);

    default ToolClass getHarvestTool(BlockState state) {
        return ToolClass.PICKAXE;
    }
}
