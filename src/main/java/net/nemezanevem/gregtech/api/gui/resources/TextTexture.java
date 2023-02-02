package net.nemezanevem.gregtech.api.gui.resources;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.fml.loading.FMLLoader;

import java.util.Collections;
import java.util.List;

import static com.mojang.blaze3d.Blaze3D.getTime;

public class TextTexture implements IGuiTexture{
    public Component text;
    public int color;
    public int width;
    public boolean dropShadow;
    public TextType type;
    private List<FormattedCharSequence> texts;

    public TextTexture(String text, int color) {
        this.color = color;
        this.type = TextType.NORMAL;
        if (FMLLoader.getDist().isClient()) {
            this.text = Component.translatable(text);
            texts = Collections.singletonList(this.text.getVisualOrderText());
        }
    }

    public TextTexture setColor(int color) {
        this.color = color;
        return this;
    }

    public TextTexture setDropShadow(boolean dropShadow) {
        this.dropShadow = dropShadow;
        return this;
    }

    public TextTexture setWidth(int width) {
        this.width = width;
        if (FMLLoader.getDist().isClient()) {
            if (this.width > 0) {
                texts = Minecraft.getInstance().font.split(text, width);
            } else {
                texts = Collections.singletonList(text.getVisualOrderText());
            }
        }
        return this;
    }

    public TextTexture setType(TextType type) {
        this.type = type;
        return this;
    }

    @Override
    public void draw(PoseStack poseStack, double x, double y, int width, int height) {
        Font fontRenderer = Minecraft.getInstance().font;
        int textH = fontRenderer.lineHeight;
        if (type == TextType.NORMAL) {
            textH *= texts.size();
            for (int i = 0; i < texts.size(); i++) {
                FormattedCharSequence resultText = texts.get(i);
                int textW = fontRenderer.width(resultText);
                float _x = (float) (x + (width - textW) / 2f);
                float _y = (float) (y + (height - textH) / 2f + i * fontRenderer.lineHeight);
                if(dropShadow) fontRenderer.drawShadow(poseStack, resultText, _x, _y, color);
                else fontRenderer.draw(poseStack, resultText, _x, _y, color);
            }
        } else if (type == TextType.HIDE) {
            String resultText = texts.get(0) + (texts.size() > 1 ? ".." : "");
            int textW = fontRenderer.width(resultText);
            float _x = (float) (x + (width - textW) / 2f);
            float _y = (float) (y + (height - textH) / 2f);
            if(dropShadow) fontRenderer.drawShadow(poseStack, resultText, _x, _y, color);
            else fontRenderer.draw(poseStack, resultText, _x, _y, color);
        } else if (type == TextType.ROLL) {
            int i = (int) ((getTime() / 1000) % texts.size());
            FormattedCharSequence resultText = texts.get(i);
            int textW = fontRenderer.width(resultText);
            float _x = (float) (x + (width - textW) / 2f);
            float _y = (float) (y + (height - textH) / 2f);
            if(dropShadow) fontRenderer.drawShadow(poseStack, resultText, _x, _y, color);
            else fontRenderer.draw(poseStack, resultText, _x, _y, color);
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    public enum TextType{
        NORMAL,
        HIDE,
        ROLL
    }
}
