package net.nemezanevem.gregtech.api.gui.resources;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.gui.resources.picturetexture.AnimatedPictureTexture;
import net.nemezanevem.gregtech.api.gui.resources.picturetexture.OrdinaryTexture;
import net.nemezanevem.gregtech.api.gui.resources.picturetexture.PictureTexture;
import net.nemezanevem.gregtech.api.gui.resources.utils.GifDecoder;
import net.nemezanevem.gregtech.api.gui.resources.utils.ImageUtils;
import net.nemezanevem.gregtech.api.gui.resources.utils.ProcessedImageData;
import org.apache.commons.compress.utils.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileTexture implements IGuiTexture{
    public final File file;
    private PictureTexture texture;
    private ProcessedImageData imageData;
    private Thread downloadThread;
    private boolean failed;

    public FileTexture(File file) {
        this.file = file;
    }

    public void loadFile(){
        if (imageData != null) {
            if (imageData.isAnimated()) {
                texture = new AnimatedPictureTexture(imageData);
                texture.tick();
            } else {
                texture = new OrdinaryTexture(imageData);
            }
            imageData = null;
            downloadThread = null;
        } else if (downloadThread == null) {
            downloadThread = new Thread(() -> {
                FileInputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(file);
                    String type = ImageUtils.readType(inputStream);
                    if (type.equalsIgnoreCase("gif")) {
                        GifDecoder gif = new GifDecoder();
                        inputStream.close();
                        inputStream = new FileInputStream(file);
                        int status = gif.read(inputStream);
                        if (status == GifDecoder.STATUS_OK) {
                            imageData = new ProcessedImageData(gif);
                        } else {
                            failed = true;
                        }
                    } else {
                        inputStream.close();
                        inputStream = new FileInputStream(file);
                        BufferedImage image = ImageIO.read(inputStream);
                        if (image != null) {
                            imageData = new ProcessedImageData(image);
                        } else {
                            failed = true;
                        }
                    }
                } catch (IOException e) {
                    failed = true;
                    texture = null;
                } finally {
                    IOUtils.closeQuietly(inputStream);
                }
            });
            downloadThread.start();
        }
    }

    @Override
    public void updateTick() {
        if(this.texture != null) {
            texture.tick(); // gif\video update
        }
    }

    @Override
    public void draw(PoseStack poseStack, double x, double y, int width, int height) {
        if (texture != null && texture.hasTexture()) {
            texture.render(poseStack, (float)x, (float)y, width, height, 0, 1, 1, false, false);
        } else {
            if (failed || file == null) {
                Minecraft.getInstance().font.draw(poseStack, Component.translatable("texture.url_texture.fail"), (int)x + 2, (int)(y + height / 2.0 - 4), 0xffff0000);
            } else {
                this.loadFile();
                int s = Math.floorMod(System.currentTimeMillis() / 200, 24);
                Widget.drawSector((float)(x + width / 2.0), (float)(y + height / 2.0), (float)(Math.min(width, height) / 4.0),
                        0xFF94E2C1, 24, s, s + 5);
            }
        }
    }

}
