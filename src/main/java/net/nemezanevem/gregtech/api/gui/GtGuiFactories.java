package net.nemezanevem.gregtech.api.gui;

import net.minecraftforge.registries.RegistryObject;
import net.nemezanevem.gregtech.api.item.gui.PlayerInventoryHolder;
import net.nemezanevem.gregtech.api.item.gui.PlayerInventoryUIFactory;

import static net.nemezanevem.gregtech.api.registry.gui.UIFactoryRegistry.UI_FACTORIES;

public class GtGuiFactories {

    public static final RegistryObject<UIFactory<PlayerInventoryHolder>> PLAYER_INV = UI_FACTORIES.register("player_inventory_factory", PlayerInventoryUIFactory::new);
}
