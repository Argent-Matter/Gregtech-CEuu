package net.nemezanevem.gregtech.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.nemezanevem.gregtech.client.renderer.ICustomRenderFast;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/08/31
 * @Description: copyright Created by brandon3055 on 30/11/2016.
 */

public interface IGTParticleHandler extends ICustomRenderFast {
    IGTParticleHandler DEFAULT_FX_HANDLER = new IGTParticleHandler() {
        public void preDraw(VertexConsumer buffer) {

        }

        @Override
        public void postDraw(VertexConsumer buffer) {
        }
    };
}
