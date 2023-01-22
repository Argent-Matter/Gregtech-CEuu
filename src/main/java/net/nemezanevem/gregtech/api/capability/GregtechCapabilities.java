package net.nemezanevem.gregtech.api.capability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.capability.impl.EUToFEProvider;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GregTech.MODID)
public class GregtechCapabilities {

    public static Capability<IEnergyContainer> CAPABILITY_ENERGY_CONTAINER = CapabilityManager.get(new CapabilityToken<>(){});

    public static Capability<IElectricItem> CAPABILITY_ELECTRIC_ITEM = CapabilityManager.get(new CapabilityToken<>(){});

    public static Capability<IFuelable> CAPABILITY_FUELABLE = CapabilityManager.get(new CapabilityToken<>(){});

    public static Capability<IMultiblockController> CAPABILITY_MULTIBLOCK_CONTROLLER = CapabilityManager.get(new CapabilityToken<>(){});

    public static Capability<HardwareProvider> CAPABILITY_HARDWARE_PROVIDER = CapabilityManager.get(new CapabilityToken<>(){});

    public static Capability<ConverterTrait> CAPABILITY_CONVERTER = CapabilityManager.get(new CapabilityToken<>(){});

    private static final ResourceLocation CAPABILITY_EU_TO_FE = new ResourceLocation(GregTech.MODID, "fe_capability");

    @SubscribeEvent
    public static void attachTileCapability(AttachCapabilitiesEvent<BlockEntity> event) {
        event.addCapability(CAPABILITY_EU_TO_FE, new EUToFEProvider(event.getObject()));
    }
}
