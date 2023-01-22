package net.nemezanevem.gregtech.api.unification.material.materials;

import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.BlastProperty.GasTier;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.ToolProperty;
import net.nemezanevem.gregtech.api.util.GTValues;

import static net.nemezanevem.gregtech.api.registry.material.MaterialRegistry.MATERIALS;
import static net.nemezanevem.gregtech.api.unification.material.GtMaterials.*;
import static net.nemezanevem.gregtech.api.unification.material.properties.info.GtMaterialFlags.*;
import static net.nemezanevem.gregtech.api.unification.material.properties.info.GtMaterialIconSets.*;
import static net.nemezanevem.gregtech.api.util.GTValues.*;

public class HigherDegreeMaterials {

    public static void register() {

        Electrotine = MATERIALS.register("electrotine", () -> new Material.Builder("electrotine")
                .dust().ore(5, 1, true)
                .color(0x3CB4C8).iconSet(SHINY.get())
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Redstone, 1, Electrum, 1)
                .build());

        EnderEye = MATERIALS.register("ender_eye", () -> new Material.Builder("ender_eye")
                .gem(1)
                .color(0x66FF66)
                .flags(NO_SMASHING.get(), NO_SMELTING.get(), DECOMPOSITION_BY_CENTRIFUGING.get())
                .build());

        Diatomite = MATERIALS.register("diatomite", () -> new Material.Builder("diatomite")
                .dust(1).ore()
                .color(0xE1E1E1)
                .components(Flint, 8, BandedIron, 1, Sapphire, 1)
                .build());

        RedSteel = MATERIALS.register("red_steel", () -> new Material.Builder("red_steel")
                .ingot(3).fluid()
                .color(0x8C6464).iconSet(METALLIC.get())
                .flags(EXT_METAL, GENERATE_GEAR.get())
                .components(SterlingSilver, 1, BismuthBronze, 1, Steel, 2, BlackSteel, 4)
                .toolStats(ToolProperty.Builder.of(7.0F, 6.0F, 2560, 3)
                        .attackSpeed(0.1F).enchantability(21).build())
                .blastTemp(1300, GasTier.LOW, VA[HV], 1000)
                .build());

        BlueSteel = MATERIALS.register("blue_steel", () -> new Material.Builder("blue_steel")
                .ingot(3).fluid()
                .color(0x64648C).iconSet(METALLIC.get())
                .flags(EXT_METAL, GENERATE_FRAME.get(), GENERATE_GEAR.get())
                .components(RoseGold, 1, Brass, 1, Steel, 2, BlackSteel, 4)
                .toolStats(ToolProperty.Builder.of(15.0F, 6.0F, 1024, 3)
                        .attackSpeed(0.1F).enchantability(33).build())
                .blastTemp(1400, GasTier.LOW, VA[HV], 1000)
                .build());

        Basalt = MATERIALS.register("basalt", () -> new Material.Builder("basalt")
                .dust(1)
                .color(0x3C3232).iconSet(ROUGH.get())
                .flags(NO_SMASHING.get(), DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(Olivine, 1, Calcite, 3, Flint, 8, DarkAsh, 4)
                .build());

        GraniticMineralSand = MATERIALS.register("granitic_mineral_sand", () -> new Material.Builder("granitic_mineral_sand")
                .dust(1).ore()
                .color(0x283C3C).iconSet(SAND.get())
                .components(Magnetite, 1, GraniteBlack, 1)
                .flags(BLAST_FURNACE_CALCITE_DOUBLE.get())
                .build());

        Redrock = MATERIALS.register("redrock", () -> new Material.Builder("redrock")
                .dust(1)
                .color(0xFF5032).iconSet(ROUGH.get())
                .flags(NO_SMASHING.get(), DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(Calcite, 2, Flint, 1)
                .build());

        GarnetSand = MATERIALS.register("garnet_sand", () -> new Material.Builder("garnet_sand")
                .dust(1).ore()
                .color(0xC86400).iconSet(SAND.get())
                .flags(DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(Almandine, 1, Andradite, 1, Grossular, 1, Pyrope, 1, Spessartine, 1, Uvarovite, 1)
                .build());

        HSSG = MATERIALS.register("hssg", () -> new Material.Builder("hssg")
                .ingot(3).fluid()
                .color(0x999900).iconSet(METALLIC.get())
                .flags(EXT2_METAL, GENERATE_SMALL_GEAR.get(), GENERATE_FRAME.get(), GENERATE_SPRING.get(), GENERATE_FINE_WIRE.get(), GENERATE_FOIL.get(), GENERATE_GEAR.get())
                .components(TungstenSteel, 5, Chrome, 1, Molybdenum, 2, Vanadium, 1)
                .rotorStats(10.0f, 5.5f, 4000)
                .cableProperties(GTValues.V[6], 4, 2)
                .blastTemp(4200, GasTier.MID, VA[EV], 1300)
                .build());

        RedAlloy = MATERIALS.register("red_alloy", () -> new Material.Builder("red_alloy")
                .ingot(0).fluid()
                .color(0xC80000)
                .flags(STD_METAL, GENERATE_FINE_WIRE.get(), GENERATE_BOLT_SCREW.get(), DISABLE_DECOMPOSITION.get())
                .components(Copper, 1, Redstone, 4)
                .cableProperties(GTValues.V[0], 1, 0)
                .fluidTemp(1400)
                .build());

        BasalticMineralSand = MATERIALS.register("basaltic_mineral_sand", () -> new Material.Builder("basaltic_mineral_sand")
                .dust(1).ore()
                .color(0x283228).iconSet(SAND.get())
                .components(Magnetite, 1, Basalt, 1)
                .flags(BLAST_FURNACE_CALCITE_DOUBLE.get())
                .build());

        HSSE = MATERIALS.register("hsse", () -> new Material.Builder("hsse")
                .ingot(4).fluid()
                .color(0x336600).iconSet(METALLIC.get())
                .flags(EXT2_METAL, GENERATE_FRAME.get(), GENERATE_RING.get())
                .components(HSSG, 6, Cobalt, 1, Manganese, 1, Silicon, 1)
                .toolStats(ToolProperty.Builder.of(5.0F, 10.0F, 3072, 4)
                        .attackSpeed(0.3F).enchantability(33).build())
                .rotorStats(10.0f, 8.0f, 5120)
                .blastTemp(5000, GasTier.HIGH, VA[EV], 1400)
                .build());

        HSSS = MATERIALS.register("hsss", () -> new Material.Builder("hsss")
                .ingot(4).fluid()
                .color(0x660033).iconSet(METALLIC.get())
                .flags(EXT2_METAL, GENERATE_SMALL_GEAR.get(), GENERATE_RING.get(), GENERATE_FRAME.get(), GENERATE_ROTOR.get(), GENERATE_ROUND.get(), GENERATE_FOIL.get(), GENERATE_GEAR.get())
                .components(HSSG, 6, Iridium, 2, Osmium, 1)
                .rotorStats(15.0f, 7.0f, 3000)
                .blastTemp(5000, GasTier.HIGH, VA[EV], 1500)
                .build());

        // FREE ID: 2521

        IridiumMetalResidue = MATERIALS.register("iridium_metal_residue", () -> new Material.Builder("iridium_metal_residue")
                .dust()
                .color(0x5C5D68).iconSet(METALLIC.get())
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Iridium, 1, Chlorine, 3, PlatinumSludgeResidue, 1)
                .build());

        Granite = MATERIALS.register("granite", () -> new Material.Builder("granite")
                .dust()
                .color(0xCFA18C).iconSet(ROUGH.get())
                .flags(DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(SiliconDioxide, 4, Redrock, 1)
                .build());

        Brick = MATERIALS.register("brick", () -> new Material.Builder("brick")
                .dust()
                .color(0x9B5643).iconSet(ROUGH.get())
                .flags(EXCLUDE_BLOCK_CRAFTING_RECIPES.get(), NO_SMELTING.get(), DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(Clay, 1)
                .build());

        Fireclay = MATERIALS.register("fireclay", () -> new Material.Builder("fireclay")
                .dust()
                .color(0xADA09B).iconSet(ROUGH.get())
                .flags(DECOMPOSITION_BY_CENTRIFUGING.get(), NO_SMELTING.get())
                .components(Clay, 1, Brick, 1)
                .build());

        Diorite = MATERIALS.register("diorite", () -> new Material.Builder("diorite")
                .dust()
                .iconSet(ROUGH.get())
                .flags(DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(Mirabilite, 2, Clay, 7)
                .build());

        BlueAlloy = MATERIALS.register("blue_alloy", () -> new Material.Builder("blue_alloy")
                .ingot().fluid()
                .color(0x64B4FF).iconSet(DULL.get())
                .flags(GENERATE_PLATE.get(), GENERATE_BOLT_SCREW.get(), DISABLE_DECOMPOSITION.get())
                .components(Electrotine, 4, Silver, 1)
                .cableProperties(GTValues.V[GTValues.HV], 2, 1)
                .fluidTemp(1400)
                .build());
    }
}