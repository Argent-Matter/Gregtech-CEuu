package net.nemezanevem.gregtech.api.capability;

import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.nemezanevem.gregtech.api.capability.impl.AbstractRecipeLogic;

public class SimpleCapabilityManager {

    public static void init(RegisterCapabilitiesEvent event) {
        event.register(IEnergyContainer.class);
        event.register(IElectricItem.class);
        event.register(IWorkable.class);
        event.register(ICoverable.class);
        event.register(IControllable.class);
        event.register(IActiveOutputSide.class);
        event.register(IFuelable.class);
        event.register(IMultiblockController.class);
        event.register(IMaintenance.class);
        event.register(IMultipleRecipeTypes.class);
        event.register(AbstractRecipeLogic.class);
        event.register(HardwareProvider.class);
        event.register(ConverterTrait.class);

        //internal capabilities
        event.register(GTWorldGenCapability.class, GTWorldGenCapability.STORAGE, GTWorldGenCapability.FACTORY);
    }
}
