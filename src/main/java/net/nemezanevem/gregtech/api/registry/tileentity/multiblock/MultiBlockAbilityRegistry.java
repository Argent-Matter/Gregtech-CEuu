package net.nemezanevem.gregtech.api.registry.tileentity.multiblock;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.blockentity.multiblock.MultiblockAbility;

import java.util.function.Supplier;

public class MultiBlockAbilityRegistry {

    public static final DeferredRegister<MultiblockAbility<?>> MULTIBLOCK_ABILITIES = DeferredRegister.create(new ResourceLocation(GregTech.MODID, "multi_block_ability"), GregTech.MODID);

    public static Supplier<IForgeRegistry<MultiblockAbility<?>>> MULTIBLOCK_ABILITIES_BUILTIN = MULTIBLOCK_ABILITIES.makeRegistry(() -> new RegistryBuilder<MultiblockAbility<?>>().setDefaultKey(new ResourceLocation(GregTech.MODID, "null")));

    public static void init() {}
}
