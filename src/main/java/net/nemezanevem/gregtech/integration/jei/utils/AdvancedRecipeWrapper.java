package net.nemezanevem.gregtech.integration.jei.utils;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AdvancedRecipeWrapper {

    protected final List<Button> buttons = new ArrayList<>();

    public AdvancedRecipeWrapper() {
        initExtras();
    }

    public abstract void initExtras();

    public void drawInfo(PoseStack poseStack, @Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        for (Button button : buttons) {
            button.render(poseStack, mouseX, mouseY, minecraft.getPartialTick());
            if (button.isMouseOver(mouseX, mouseY)) {
                List<Component> lines = new ArrayList<>();
                button.renderToolTip(lines);
                if (lines.isEmpty())
                    continue;
                int width = (int) (minecraft.getWindow().getWidth() / 2f + recipeWidth / 2f);
                int maxWidth = Math.min(200, width - mouseX - 5);
                minecraft.screen.renderTooltip(poseStack, lines, Optional.empty(), mouseX, mouseY, minecraft.font);
            }
        }
    }

    @Override
    public boolean handleClick(@Nonnull Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
        for (Button button : buttons) {
            if (button.isMouseOver(mouseX, mouseY) && button.mouseClicked(mouseX, mouseY, mouseButton)) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
        }
        return false;
    }
}
