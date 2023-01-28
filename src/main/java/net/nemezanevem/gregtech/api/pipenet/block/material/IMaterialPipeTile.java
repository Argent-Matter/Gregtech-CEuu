package net.nemezanevem.gregtech.api.pipenet.block.material;

import net.nemezanevem.gregtech.api.pipenet.block.IPipeType;
import net.nemezanevem.gregtech.api.pipenet.tile.IPipeTile;
import net.nemezanevem.gregtech.api.unification.material.Material;

public interface IMaterialPipeTile<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType> extends IPipeTile<PipeType, NodeDataType> {

    Material getPipeMaterial();
}
