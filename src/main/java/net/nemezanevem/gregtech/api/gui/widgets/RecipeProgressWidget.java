package net.nemezanevem.gregtech.api.gui.widgets;

import gregtech.api.GTValues;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.recipes.RecipeType;
import gregtech.api.recipes.RecipeTypes;
import gregtech.integration.jei.GTJeiPlugin;
import gregtech.integration.jei.recipe.RecipeTypeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.Loader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleSupplier;

public class RecipeProgressWidget extends ProgressWidget {

    private final RecipeType<?> recipeMap;
    private final static int HOVER_TEXT_WIDTH = 200;

    public RecipeProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height, RecipeType<?> recipeMap) {
        super(progressSupplier, x, y, width, height);
        this.recipeMap = recipeMap;
    }

    public RecipeProgressWidget(DoubleSupplier progressSupplier, int x, int y, int width, int height, TextureArea fullImage, MoveType moveType, RecipeType<?> recipeMap) {
        super(progressSupplier, x, y, width, height, fullImage, moveType);
        this.recipeMap = recipeMap;
    }

    public RecipeProgressWidget(int ticksPerCycle, int x, int y, int width, int height, TextureArea fullImage, MoveType moveType, RecipeType<?> recipeMap) {
        super(ticksPerCycle, x, y, width, height, fullImage, moveType);
        this.recipeMap = recipeMap;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!Loader.isModLoaded(GregTech.MODID_JEI))
            return false;
        if (isMouseOverElement(mouseX, mouseY) && RecipeTypeCategory.getCategoryMap().containsKey(recipeMap)) {
            // Since categories were even registered at all, we know JEI is active.
            List<String> categoryID = new ArrayList<>();
            if(recipeMap == RecipeTypes.FURNACE_RECIPES) {
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
    public void drawInForeground(int mouseX, int mouseY) {
        super.drawInForeground(mouseX, mouseY);
        if (isMouseOverElement(mouseX, mouseY) && Loader.isModLoaded(GregTech.MODID_JEI)) {
            Minecraft mc = Minecraft.getMinecraft();
            GuiUtils.drawHoveringText(Collections.singletonList(Component.translatable("gui.widget.recipeProgressWidget.default_tooltip")), mouseX, mouseY,
                    sizes.getScreenWidth(),
                    sizes.getScreenHeight(), HOVER_TEXT_WIDTH, mc.fontRenderer);
        }
    }

}
