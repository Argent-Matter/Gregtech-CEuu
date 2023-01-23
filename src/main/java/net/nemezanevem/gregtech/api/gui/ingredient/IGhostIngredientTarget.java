package net.nemezanevem.gregtech.api.gui.ingredient;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler.Target;

import java.util.List;

public interface IGhostIngredientTarget {

    List<Target<?>> getPhantomTargets(Object ingredient);

}
