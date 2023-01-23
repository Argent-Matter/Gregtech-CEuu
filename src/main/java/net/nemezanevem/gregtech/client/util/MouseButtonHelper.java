package net.nemezanevem.gregtech.client.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class MouseButtonHelper {

    public static boolean isHeld = false;
    public static int button = -1;


    public static void mouseButton(final InputEvent.Key.MouseButton event) {
        isHeld = event.getAction() == InputConstants.PRESS;
        button = event.getButton();
    }
}
