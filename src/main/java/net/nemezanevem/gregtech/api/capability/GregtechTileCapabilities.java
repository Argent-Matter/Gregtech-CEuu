package net.nemezanevem.gregtech.api.capability;

import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.nemezanevem.gregtech.api.capability.impl.AbstractRecipeLogic;
import net.minecraftforge.common.capabilities.Capability;

public class GregtechTileCapabilities {

    public static Capability<IWorkable> CAPABILITY_WORKABLE = CapabilityManager.get(new CapabilityToken<>(){});

    public static Capability<ICoverable> CAPABILITY_COVERABLE = CapabilityManager.get(new CapabilityToken<>(){});

    public static Capability<IControllable> CAPABILITY_CONTROLLABLE = CapabilityManager.get(new CapabilityToken<>(){});

    public static Capability<IActiveOutputSide> CAPABILITY_ACTIVE_OUTPUT_SIDE = CapabilityManager.get(new CapabilityToken<>(){});

    public static Capability<AbstractRecipeLogic> CAPABILITY_RECIPE_LOGIC = CapabilityManager.get(new CapabilityToken<>(){});

    public static Capability<IMultipleRecipeMaps> CAPABILITY_MULTIPLE_RECIPEMAPS = CapabilityManager.get(new CapabilityToken<>(){});

    public static Capability<IMaintenance> CAPABILITY_MAINTENANCE = CapabilityManager.get(new CapabilityToken<>(){});

}
