package net.nemezanevem.gregtech;

import codechicken.lib.texture.SpriteRegistryHelper;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;
import net.nemezanevem.gregtech.api.block.IHeatingCoilBlockStats;
import net.nemezanevem.gregtech.api.block.machine.BlockMachine;
import net.nemezanevem.gregtech.api.module.IModuleManager;
import net.nemezanevem.gregtech.api.recipe.ingredient.ExtendedIngredient;
import net.nemezanevem.gregtech.api.recipe.ingredient.FluidIngredientSerializer;
import net.nemezanevem.gregtech.api.registry.material.MaterialRegistry;
import net.nemezanevem.gregtech.api.registry.material.info.MaterialFlagRegistry;
import net.nemezanevem.gregtech.api.registry.material.info.MaterialIconSetRegistry;
import net.nemezanevem.gregtech.api.registry.material.info.MaterialIconTypeRegistry;
import net.nemezanevem.gregtech.api.registry.tileentity.MetaTileEntityRegistry;
import net.nemezanevem.gregtech.api.sound.ISoundManager;
import net.nemezanevem.gregtech.api.unification.material.GtMaterials;
import net.nemezanevem.gregtech.api.unification.material.properties.info.GtMaterialFlags;
import net.nemezanevem.gregtech.api.unification.material.properties.info.GtMaterialIconSets;
import net.nemezanevem.gregtech.api.unification.material.properties.info.GtMaterialIconTypes;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import net.nemezanevem.gregtech.common.block.MetaBlocks;
import net.nemezanevem.gregtech.common.item.GtItemRegistry;
import net.nemezanevem.gregtech.common.item.metaitem.MetaItems;
import net.nemezanevem.gregtech.common.metatileentities.MetaTileEntities;
import net.nemezanevem.gregtech.common.network.packets.PacketBlockParticle;
import net.nemezanevem.gregtech.common.network.packets.PacketRecoverMTE;
import net.nemezanevem.gregtech.common.network.packets.PacketUIOpen;
import net.nemezanevem.gregtech.common.network.packets.PacketUIWidgetUpdate;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(GregTech.MODID)
public class GregTech {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "gregtech";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK_HANDLER = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static RegistryObject<BlockMachine> MACHINE;

    public static ISoundManager soundManager;
    public static IModuleManager moduleManager;
    public static SpriteRegistryHelper spriteRegistryHelper;

    public static final Object2ObjectOpenHashMap<BlockState, IHeatingCoilBlockStats> HEATING_COILS = new Object2ObjectOpenHashMap<>();

    public static final CreativeModeTab TAB_GREGTECH = new CreativeModeTab("tab.gregtech") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(MetaItems.BATTERY_HULL_HV);
        }
    };

    public static final CreativeModeTab TAB_GREGTECH_MATERIALS = new CreativeModeTab("tab.gregtech.matarials") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(MetaItems.CT_OREDICT_ITEM);
        }
    };


    public GregTech() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::register);
        spriteRegistryHelper = new SpriteRegistryHelper(modEventBus);

        MaterialFlagRegistry.init();
        MaterialFlagRegistry.MATERIAL_FLAGS.register(modEventBus);
        GtMaterialFlags.init();

        MaterialIconTypeRegistry.init();
        MaterialIconTypeRegistry.MATERIAL_ICON_TYPES.register(modEventBus);
        GtMaterialIconTypes.init();

        MaterialIconSetRegistry.init();
        MaterialIconSetRegistry.MATERIAL_ICON_SETS.register(modEventBus);
        GtMaterialIconSets.init();

        MaterialRegistry.init();
        MaterialRegistry.MATERIALS.register(modEventBus);
        GtMaterials.register();

        MetaTileEntityRegistry.init();
        MetaTileEntityRegistry.META_TILE_ENTITIES.register(modEventBus);
        MetaTileEntities.init();

        MetaBlocks.init();

        GtItemRegistry.ITEMS.register(modEventBus);
        GtItemRegistry.register();

        MinecraftForge.EVENT_BUS.register(this);

        MinecraftForge.EVENT_BUS.addListener(this::tagsUpdatedEvent);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        Textures.registerIconRegisters(spriteRegistryHelper);

        int packetIndex = 0;
        NETWORK_HANDLER.registerMessage(packetIndex++, PacketUIWidgetUpdate.class, PacketUIWidgetUpdate::encode, PacketUIWidgetUpdate::decode, PacketUIWidgetUpdate::handle);
        NETWORK_HANDLER.registerMessage(packetIndex++, PacketUIWidgetUpdate.class, PacketUIWidgetUpdate::encode, PacketUIWidgetUpdate::decode, PacketUIWidgetUpdate::handle);
        NETWORK_HANDLER.registerMessage(packetIndex++, PacketUIOpen.class, PacketUIOpen::encode, PacketUIOpen::decode, PacketUIOpen::handle);
        NETWORK_HANDLER.registerMessage(packetIndex++, PacketBlockParticle.class, PacketBlockParticle::encode, PacketBlockParticle::decode, PacketBlockParticle::handle);
        NETWORK_HANDLER.registerMessage(packetIndex++, PacketRecoverMTE.class, PacketRecoverMTE::encode, PacketRecoverMTE::decode, PacketRecoverMTE::handle);
    }


    private void register(RegisterEvent event) {
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.RECIPE_SERIALIZERS))
        {
            CraftingHelper.register(new ResourceLocation(MODID, "fluid"), FluidIngredientSerializer.INSTANCE);
            CraftingHelper.register(new ResourceLocation(MODID, "extended"), ExtendedIngredient.Serializer.INSTANCE);

        }
    }

    private void tagsUpdatedEvent(TagsUpdatedEvent event) {
        var reg = event.getRegistryAccess().registry(ForgeRegistries.ITEMS.getRegistryKey());
        reg.ifPresent(registry -> {
            GtItemRegistry.getItemTagMap().keySet().forEach(registry::getOrCreateTag);
            registry.bindTags(GtItemRegistry.getItemTagMap());
        });
    }


    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        private void clientSetup(final FMLClientSetupEvent event) {

        }

    }
}
