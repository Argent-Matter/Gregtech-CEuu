package net.nemezanevem.gregtech.api.registry.tileentity;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.tileentity.MetaTileEntity;

import java.util.function.Supplier;

public class MetaTileEntityRegistry {

    public static final DeferredRegister<MetaTileEntity> META_TILE_ENTITIES = DeferredRegister.create(new ResourceLocation(GregTech.MODID, "meta_tile_entity"), GregTech.MODID);

    public static Supplier<IForgeRegistry<MetaTileEntity>> META_TILE_ENTITIES_BUILTIN = META_TILE_ENTITIES.makeRegistry(() -> new RegistryBuilder<MetaTileEntity>().setDefaultKey(new ResourceLocation(GregTech.MODID, "null")));

    public static void init() {}
}
