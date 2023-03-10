package net.nemezanevem.gregtech.api.gui.resources;

import codechicken.lib.render.shader.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.loading.FMLLoader;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.common.ConfigHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ShaderTexture implements IGuiTexture{
    private static final Map<String, ShaderTexture> PROGRAMS = new HashMap<>();
    private ShaderProgram program;
    private ShaderObject object;
    private float resolution = (float) ConfigHolder.client.resolution;

    public static void clear(){
        PROGRAMS.values().forEach(ShaderTexture::dispose);
        PROGRAMS.clear();
    }

    private ShaderTexture() {

    }

    public void dispose() {
        if (object != null) {
            program.release();
        }
    }

    public void updateRawShader(String rawShader) {
        dispose();
        program = ShaderProgramBuilder.builder()
                .addShader(rawShader, (consumer) ->
                        consumer.source(rawShader)
                                .type(ShaderObject.StandardShaderType.FRAGMENT))
                .build();
    }

    private ShaderTexture(ShaderProgram program, ShaderObject object) {
        this.program = program;
        this.object = object;
    }

    public static ShaderTexture createShader(String location) {
        if (FMLLoader.getDist().isClient() && Shaders.allowedShader()) {
            if (!PROGRAMS.containsKey(location)) {
                ShaderObject object = Shaders.loadShader(ShaderObject.StandardShaderType.FRAGMENT, location);
                if (object != null) {
                    ShaderProgram program = new ShaderProgram();
                    program.attachShader(object);
                    PROGRAMS.put(location, new ShaderTexture(program, object));
                } else {
                    return new ShaderTexture();
                }
            }
            return PROGRAMS.get(location);
        } else {
            return new ShaderTexture();
        }
    }

    public static ShaderTexture createRawShader(String rawShader) {
        if (FMLCommonHandler.instance().getSide().isClient() && Shaders.allowedShader()) {
            ShaderProgram program = new ShaderProgram();
            ShaderObject object = new ShaderObject(ShaderObject.ShaderType.FRAGMENT, rawShader).compileShader();
            program.attachShader(object);
            return new ShaderTexture(program, object);
        } else {
            return new ShaderTexture();
        }
    }

    public ShaderTexture setResolution(float resolution) {
        this.resolution = resolution;
        return this;
    }

    public float getResolution() {
        return resolution;
    }

    @Override
    public void draw(PoseStack poseStack, double x, double y, int width, int height) {
        this.draw(x, y, width, height, null);
    }

    public void draw(double x, double y, int width, int height, Consumer<CCUniform> uniformCache) {
        if (program != null) {
            program.getUniform(cache->{
                cache.glUniform2F("u_resolution", width * resolution, height * resolution);
                if (uniformCache != null) {
                    uniformCache.accept(cache);
                }
            });
            Widget.drawTextureRect(x, y, width, height);
            program.releaseShader();
        }
    }
}
