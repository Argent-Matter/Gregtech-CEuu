package net.nemezanevem.gregtech.api.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.gui.resources.TextureArea;
import net.nemezanevem.gregtech.api.recipe.GTRecipeType;
import net.nemezanevem.gregtech.api.recipe.GtRecipeTypes;
import net.nemezanevem.gregtech.integration.jei.recipe.RecipeTypeCategory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.DoubleSupplier;

public class RecipeProgressWidget extends ProgressWidget {

    private final GTRecipeType<?> recipeMap;
    private final static int HOVER_TEXT_WIDTH = 200;

    public RecipeProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height, GTRecipeType<?> recipeMap) {
        super(progressSupplier, x, y, width, height);
        this.recipeMap = recipeMap;
    }

    public RecipeProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height, TextureArea fullImage, MoveType moveType, GTRecipeType<?> recipeMap) {
        super(progressSupplier, x, y, width, height, fullImage, moveType);
        this.recipeMap = recipeMap;
    }

    public RecipeProgressWidget(int ticksPerCycle, int x, int y, int width, int height, TextureArea fullImage, MoveType moveType, GTRecipeType<?> recipeMap) {
        super(ticksPerCycle, x, y, width, height, fullImage, moveType);
        this.recipeMap = recipeMap;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!ModList.get().isLoaded(GregTech.MODID_JEI))
            return false;
        if (isMouseOverElement(mouseX, mouseY) && RecipeTypeCategory.getCategoryMap().containsKey(recipeMap)) {
            // Since categories were even registered at all, we know JEI is active.
            List<String> categoryID = new ArrayList<>();
            if(recipeMap == GtRecipeTypes.FURNACE_RECIPES) {
                categoryID.add("minecraft.smelting");
            }
            else {
                categoryID.add(RecipeTypeCategory.getCategoryMap().get(recipeMap).getUid());
            }
            GTJeiPlugin.jeiRuntime.getRecipesGui().showCategories(categoryID);
            return true;
        }
        return false;
    }


    @Override
    public void drawInForeground(PoseStack poseStack, int mouseX, int mouseY) {
        super.drawInForeground(poseStack, mouseX, mouseY);
        if (isMouseOverElement(mouseX, mouseY) && ModList.get().isLoaded(GregTech.MODID_JEI)) {
            Minecraft mc = Minecraft.getInstance();
            mc.screen.renderTooltip(poseStack, Collections.singletonList(Component.translatable("gui.widget.recipeProgressWidget.default_tooltip")), Optional.empty(), mouseX, mouseY, mc.font);
            /*GuiUtils.drawHoveringText(Collections.singletonList(Component.translatable("gui.widget.recipeProgressWidget.default_tooltip")), mouseX, mouseY,
                    sizes.getScreenWidth(),
                    sizes.getScreenHeight(), HOVER_TEXT_WIDTH, mc.font);*/
        }
    }

}
