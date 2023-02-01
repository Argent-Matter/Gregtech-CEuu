package net.nemezanevem.gregtech.api.gui.resources;

import com.mojang.blaze3d.vertex.PoseStack;

public interface IGuiTexture {
    void draw(PoseStack poseStack, double x, double y, int width, int height);
    default void updateTick() { }
    IGuiTexture EMPTY = (poseStack, x, y, width, height) -> {};
}
