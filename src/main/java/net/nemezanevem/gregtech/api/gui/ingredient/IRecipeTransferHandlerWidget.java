package net.nemezanevem.gregtech.api.gui.ingredient;

import gregtech.api.gui.impl.ModularUIContainer;
import mezz.jei.api.gui.IRecipeLayout;
import net.minecraft.entity.player.Player;

public interface IRecipeTransferHandlerWidget {

    String transferRecipe(ModularUIContainer container, IRecipeLayout recipeLayout, Player player, boolean maxTransfer, boolean doTransfer);
}
