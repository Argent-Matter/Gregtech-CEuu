package net.nemezanevem.gregtech.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;

public interface ICustomRenderFast {
    /**
     * Run any pre render gl code here.
     * You can also start drawing quads.
     */
    void preDraw(VertexConsumer buffer);

    /**
     * Run any post render gl code here.
     * This is where you would draw if you started drawing in preDraw
     */
    void postDraw(VertexConsumer buffer);
}
