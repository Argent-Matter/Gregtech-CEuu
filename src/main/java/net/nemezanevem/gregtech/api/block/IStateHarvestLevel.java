package net.nemezanevem.gregtech.api.block;

import net.minecraft.world.level.block.state.BlockState;

public interface IStateHarvestLevel {

    int getHarvestLevel(BlockState state);

    default String getHarvestTool(BlockState state) {
        return ToolType.PICKAXE;
    }
}
