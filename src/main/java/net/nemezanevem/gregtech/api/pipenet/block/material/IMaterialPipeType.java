package net.nemezanevem.gregtech.api.pipenet.block.material;

import net.nemezanevem.gregtech.api.pipenet.block.IPipeType;
import net.nemezanevem.gregtech.api.unification.tag.TagPrefix;

public interface IMaterialPipeType<NodeDataType> extends IPipeType<NodeDataType> {

    /**
     * Determines ore prefix used for this pipe type, which gives pipe ore dictionary key
     * when combined with pipe's material
     *
     * @return ore prefix used for this pipe type
     */
    TagPrefix getTagPrefix();
}
