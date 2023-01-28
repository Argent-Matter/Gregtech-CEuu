package net.nemezanevem.gregtech.api.pipenet.block;

import net.minecraft.util.StringRepresentable;

public interface IPipeType<NodeDataType> extends StringRepresentable {

    float getThickness();

    NodeDataType modifyProperties(NodeDataType baseProperties);

    boolean isPaintable();

}
