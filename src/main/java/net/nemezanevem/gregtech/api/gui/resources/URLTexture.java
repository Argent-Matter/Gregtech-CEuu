package net.nemezanevem.gregtech.api.gui.resources;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.gui.resources.picturetexture.PictureTexture;
import net.nemezanevem.gregtech.api.gui.resources.utils.DownloadThread;

public class URLTexture implements IGuiTexture{
    public final String url;
    private DownloadThread downloader;
    private PictureTexture texture;
    private boolean failed;
    private String error;


    public URLTexture(String url) {
        this.url = url;
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
            if (failed || url == null || this.url.equals("")) {
                Minecraft.getInstance().font.draw(poseStack, Component.translatable("texture.url_texture.fail"), (int)x + 2, (int)(y + height / 2.0 - 4), 0xffff0000);
            } else {
                this.loadTexture();
                int s = (int) Math.floorMod(System.currentTimeMillis() / 200, 24);
                Widget.drawSector((float)(x + width / 2.0), (float)(y + height / 2.0), (float)(Math.min(width, height) / 4.0),
                        0xFF94E2C1, 24, s, s + 5);
            }
        }
    }

    public void loadTexture() {
        if (texture == null && !failed) {
            if (downloader == null && DownloadThread.activeDownloads < DownloadThread.MAXIMUM_ACTIVE_DOWNLOADS) {
                PictureTexture loadedTexture = DownloadThread.loadedImages.get(url);
                if (loadedTexture == null) {
                    synchronized (DownloadThread.LOCK) {
                        if (!DownloadThread.loadingImages.contains(url)) {
                            downloader = new DownloadThread(url);
                            return;
                        }
                    }
                } else {
                    texture = loadedTexture;
                }
            }
            if (downloader != null && downloader.hasFinished()) {
                if (downloader.hasFailed()) {
                    failed = true;
                    error = downloader.getError();
                    DownloadThread.LOGGER.error("Could not load image of " + url + " : " + error);
                } else {
                    texture = DownloadThread.loadImage(downloader);
                }
                downloader = null;
            }
        }
    }
}
