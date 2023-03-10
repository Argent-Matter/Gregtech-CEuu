package net.nemezanevem.gregtech.client.renderer.cclop;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;

public class ColourOperation implements IVertexOperation {
    public static final int operationIndex = IVertexOperation.registerOperation();
    int colour;

    public ColourOperation(int colour) {
        this.colour = colour;
    }

    @Override
    public boolean load(CCRenderState ccRenderState) {
        return true;
    }

    @Override
    public void operate(CCRenderState ccRenderState) {
        ccRenderState.colour = colour;
    }

    @Override
    public int operationID() {
        return operationIndex;
    }
}