package net.nemezanevem.gregtech.api.unification.material.materials;

import net.nemezanevem.gregtech.api.unification.material.Material;

import static net.nemezanevem.gregtech.api.registry.material.MaterialRegistry.MATERIALS;
import static net.nemezanevem.gregtech.api.unification.material.GtMaterials.*;
import static net.nemezanevem.gregtech.api.unification.material.properties.info.GtMaterialFlags.*;
import static net.nemezanevem.gregtech.api.unification.material.properties.info.GtMaterialIconSets.*;

public class UnknownCompositionMaterials {

    public static void register() {

        WoodGas = MATERIALS.register("wood_gas", () -> new Material.Builder("wood_gas")
                .fluid(GTFluidTypes.GAS).color(0xDECD87).build());

        WoodVinegar = MATERIALS.register("wood_vinegar", () -> new Material.Builder("wood_vinegar")
                .fluid().color(0xD45500).build());

        WoodTar = MATERIALS.register("wood_tar", () -> new Material.Builder("wood_tar")
                .fluid().color(0x28170B)
                .flags(STICKY.get(), FLAMMABLE.get()).build());

        CharcoalByproducts = MATERIALS.register("charcoal_byproducts", () -> new Material.Builder("charcoal_byproducts")
                .fluid().color(0x784421).build());

        Biomass = MATERIALS.register("biomass", () -> new Material.Builder("biomass")
                .fluid().color(0x00FF00).build());

        BioDiesel = MATERIALS.register("bio_diesel", () -> new Material.Builder("bio_diesel")
                .fluid().color(0xFF8000)
                .flags(FLAMMABLE.get(), EXPLOSIVE.get()).build());

        FermentedBiomass = MATERIALS.register("fermented_biomass", () -> new Material.Builder("fermented_biomass")
                .fluid().color(0x445500).fluidTemp(300).build());

        Creosote = MATERIALS.register("creosote", () -> new Material.Builder("creosote")
                .fluid().color(0x804000)
                .flags(STICKY.get()).build());

        Diesel = MATERIALS.register("diesel", () -> new Material.Builder("diesel")
                .fluid().flags(FLAMMABLE.get(), EXPLOSIVE.get()).build());

        RocketFuel = MATERIALS.register("rocket_fuel", () -> new Material.Builder("rocket_fuel")
                .fluid().flags(FLAMMABLE.get(), EXPLOSIVE.get()).color(0xBDB78C).build());

        Glue = MATERIALS.register("glue", () -> new Material.Builder("glue")
                .fluid().flags(STICKY.get()).build());

        Lubricant = MATERIALS.register("lubricant", () -> new Material.Builder("lubricant")
                .fluid().build());

        McGuffium239 = MATERIALS.register("mc_guffium_239", () -> new Material.Builder("mc_guffium_239")
                .fluid().build());

        IndiumConcentrate = MATERIALS.register("indium_concentrate", () -> new Material.Builder("indium_concentrate")
                .fluid(GTFluidTypes.ACID).color(0x0E2950).build());

        SeedOil = MATERIALS.register("seed_oil", () -> new Material.Builder("seed_oil")
                .fluid().color(0xFFFFFF)
                .flags(STICKY.get(), FLAMMABLE.get()).build());

        DrillingFluid = MATERIALS.register("drilling_fluid", () -> new Material.Builder("drilling_fluid")
                .fluid().color(0xFFFFAA).build());

        ConstructionFoam = MATERIALS.register("construction_foam", () -> new Material.Builder("construction_foam")
                .fluid().color(0x808080).build());

        // Free IDs 1517-1521

        SulfuricHeavyFuel = MATERIALS.register("sulfuric_heavy_fuel", () -> new Material.Builder("sulfuric_heavy_fuel")
                .fluid().flags(FLAMMABLE.get()).build());

        HeavyFuel = MATERIALS.register("heavy_fuel", () -> new Material.Builder("heavy_fuel")
                .fluid().flags(FLAMMABLE.get()).build());

        LightlyHydroCrackedHeavyFuel = MATERIALS.register("lightly_hydrocracked_heavy_fuel", () -> new Material.Builder("lightly_hydrocracked_heavy_fuel")
                .fluid().color(0xFFFF00).fluidTemp(775).flags(FLAMMABLE.get()).build());

        SeverelyHydroCrackedHeavyFuel = MATERIALS.register("severely_hydrocracked_heavy_fuel", () -> new Material.Builder("severely_hydrocracked_heavy_fuel")
                .fluid().color(0xFFFF00).fluidTemp(775).flags(FLAMMABLE.get()).build());

        LightlySteamCrackedHeavyFuel = MATERIALS.register("lightly_steamcracked_heavy_fuel", () -> new Material.Builder("lightly_steamcracked_heavy_fuel")
                .fluid().fluidTemp(775).flags(FLAMMABLE.get()).build());

        SeverelySteamCrackedHeavyFuel = MATERIALS.register("severely_steamcracked_heavy_fuel", () -> new Material.Builder("severely_steamcracked_heavy_fuel")
                .fluid().fluidTemp(775).flags(FLAMMABLE.get()).build());

        SulfuricLightFuel = MATERIALS.register("sulfuric_light_fuel", () -> new Material.Builder("sulfuric_light_fuel")
                .fluid().fluidTemp(775).flags(FLAMMABLE.get()).build());

        LightFuel = MATERIALS.register("light_fuel", () -> new Material.Builder("light_fuel")
                .fluid().flags(FLAMMABLE.get()).build());

        LightlyHydroCrackedLightFuel = MATERIALS.register("lightly_hydrocracked_light_fuel", () -> new Material.Builder("lightly_hydrocracked_light_fuel")
                .fluid().color(0xB7AF08).fluidTemp(775).flags(FLAMMABLE.get()).build());

        SeverelyHydroCrackedLightFuel = MATERIALS.register("severely_hydrocracked_light_fuel", () -> new Material.Builder("severely_hydrocracked_light_fuel")
                .fluid().color(0xB7AF08).fluidTemp(775).flags(FLAMMABLE.get()).build());

        LightlySteamCrackedLightFuel = MATERIALS.register("lightly_steamcracked_light_fuel", () -> new Material.Builder("lightly_steamcracked_light_fuel")
                .fluid().fluidTemp(775).flags(FLAMMABLE.get()).build());

        SeverelySteamCrackedLightFuel = MATERIALS.register("severely_steamcracked_light_fuel", () -> new Material.Builder("severely_steamcracked_light_fuel")
                .fluid().fluidTemp(775).flags(FLAMMABLE.get()).build());

        SulfuricNaphtha = MATERIALS.register("sulfuric_naphtha", () -> new Material.Builder("sulfuric_naphtha")
                .fluid().flags(FLAMMABLE.get()).build());

        Naphtha = MATERIALS.register("naphtha", () -> new Material.Builder("naphtha")
                .fluid().flags(FLAMMABLE.get()).build());

        LightlyHydroCrackedNaphtha = MATERIALS.register("lightly_hydrocracked_naphtha", () -> new Material.Builder("lightly_hydrocracked_naphtha")
                .fluid().color(0xBFB608).fluidTemp(775).flags(FLAMMABLE.get()).build());

        SeverelyHydroCrackedNaphtha = MATERIALS.register("severely_hydrocracked_naphtha", () -> new Material.Builder("severely_hydrocracked_naphtha")
                .fluid().color(0xBFB608).fluidTemp(775).flags(FLAMMABLE.get()).build());

        LightlySteamCrackedNaphtha = MATERIALS.register("lightly_steamcracked_naphtha", () -> new Material.Builder("lightly_steamcracked_naphtha")
                .fluid().color(0xBFB608).fluidTemp(775).flags(FLAMMABLE.get()).build());

        SeverelySteamCrackedNaphtha = MATERIALS.register("severely_steamcracked_naphtha", () -> new Material.Builder("severely_steamcracked_naphtha")
                .fluid().color(0xBFB608).fluidTemp(775).flags(FLAMMABLE.get()).build());

        SulfuricGas = MATERIALS.register("sulfuric_gas", () -> new Material.Builder("sulfuric_gas")
                .fluid(GTFluidTypes.GAS).build());

        RefineryGas = MATERIALS.register("refinery_gas", () -> new Material.Builder("refinery_gas")
                .fluid(GTFluidTypes.GAS.get()).flags(FLAMMABLE).build());

        LightlyHydroCrackedGas = MATERIALS.register("lightly_hydrocracked_gas", () -> new Material.Builder("lightly_hydrocracked_gas")
                .fluid(GTFluidTypes.GAS).color(0xB4B4B4)
                .fluidTemp(775).flags(FLAMMABLE.get()).build());

        SeverelyHydroCrackedGas = MATERIALS.register("severely_hydrocracked_gas", () -> new Material.Builder("severely_hydrocracked_gas")
                .fluid(GTFluidTypes.GAS).color(0xB4B4B4)
                .fluidTemp(775).flags(FLAMMABLE.get()).build());

        LightlySteamCrackedGas = MATERIALS.register("lightly_steamcracked_gas", () -> new Material.Builder("lightly_steamcracked_gas")
                .fluid(GTFluidTypes.GAS).color(0xB4B4B4)
                .fluidTemp(775).flags(FLAMMABLE.get()).build());

        SeverelySteamCrackedGas = MATERIALS.register("severely_steamcracked_gas", () -> new Material.Builder("severely_steamcracked_gas")
                .fluid(GTFluidTypes.GAS).color(0xB4B4B4)
                .fluidTemp(775).flags(FLAMMABLE.get()).build());

        HydroCrackedEthane = MATERIALS.register("hydrocracked_ethane", () -> new Material.Builder("hydrocracked_ethane")
                .fluid(GTFluidTypes.GAS).color(0x9696BC)
                .fluidTemp(775).flags(FLAMMABLE.get()).build());

        HydroCrackedEthylene = MATERIALS.register("hydrocracked_ethylene", () -> new Material.Builder("hydrocracked_ethylene")
                .fluid(GTFluidTypes.GAS).color(0xA3A3A0)
                .fluidTemp(775).flags(FLAMMABLE.get()).build());

        HydroCrackedPropene = MATERIALS.register("hydrocracked_propene", () -> new Material.Builder("hydrocracked_propene")
                .fluid(GTFluidTypes.GAS).color(0xBEA540)
                .fluidTemp(775).flags(FLAMMABLE.get()).build());

        HydroCrackedPropane = MATERIALS.register("hydrocracked_propane", () -> new Material.Builder("hydrocracked_propane")
                .fluid(GTFluidTypes.GAS).color(0xBEA540)
                .fluidTemp(775).flags(FLAMMABLE.get()).build());

        HydroCrackedButane = MATERIALS.register("hydrocracked_butane", () -> new Material.Builder("hydrocracked_butane")
                .fluid(GTFluidTypes.GAS).color(0x852C18)
                .fluidTemp(775).flags(FLAMMABLE.get()).build());

        HydroCrackedButene = MATERIALS.register("hydrocracked_butene", () -> new Material.Builder("hydrocracked_butene")
                .fluid(GTFluidTypes.GAS).color(0x993E05)
                .fluidTemp(775).flags(FLAMMABLE.get()).build());

        HydroCrackedButadiene = MATERIALS.register("hydrocracked_butadiene", () -> new Material.Builder("hydrocracked_butadiene")
                .fluid(GTFluidTypes.GAS).color(0xAD5203)
                .fluidTemp(775).flags(FLAMMABLE.get()).build());

        SteamCrackedEthane = MATERIALS.register("steamcracked_ethane", () -> new Material.Builder("steamcracked_ethane")
                .fluid(GTFluidTypes.GAS).color(0x9696BC)
                .fluidTemp(775).flags(FLAMMABLE.get()).build());

        SteamCrackedEthylene = MATERIALS.register("steamcracked_ethylene", () -> new Material.Builder("steamcracked_ethylene")
                .fluid(GTFluidTypes.GAS).color(0xA3A3A0)
                .fluidTemp(775).flags(FLAMMABLE.get()).build());

        SteamCrackedPropene = MATERIALS.register("steamcracked_propene", () -> new Material.Builder("steamcracked_propene")
                .fluid(GTFluidTypes.GAS).color(0xBEA540)
                .fluidTemp(775).flags(FLAMMABLE.get()).build());

        SteamCrackedPropane = MATERIALS.register("steamcracked_propane", () -> new Material.Builder("steamcracked_propane")
                .fluid(GTFluidTypes.GAS).color(0xBEA540)
                .fluidTemp(775).flags(FLAMMABLE.get()).build());

        SteamCrackedButane = MATERIALS.register("steamcracked_butane", () -> new Material.Builder("steamcracked_butane")
                .fluid(GTFluidTypes.GAS).color(0x852C18)
                .fluidTemp(775).flags(FLAMMABLE.get()).build());

        SteamCrackedButene = MATERIALS.register("steamcracked_butene", () -> new Material.Builder("steamcracked_butene")
                .fluid(GTFluidTypes.GAS).color(0x993E05)
                .fluidTemp(775).flags(FLAMMABLE.get()).build());

        SteamCrackedButadiene = MATERIALS.register("steamcracked_butadiene", () -> new Material.Builder("steamcracked_butadiene")
                .fluid(GTFluidTypes.GAS).color(0xAD5203)
                .fluidTemp(775).flags(FLAMMABLE.get()).build());

        //Free IDs 1560-1575

        LPG = MATERIALS.register("lpg", () -> new Material.Builder("lpg")
                .fluid(GTFluidTypes.GAS.get()).flags(FLAMMABLE.get(), EXPLOSIVE).build());

        RawGrowthMedium = MATERIALS.register("raw_growth_medium", () -> new Material.Builder("raw_growth_medium")
                .fluid().color(0xA47351).build());

        SterileGrowthMedium = MATERIALS.register("sterilized_growth_medium", () -> new Material.Builder("sterilized_growth_medium")
                .fluid().color(0xAC876E).build());

        Oil = MATERIALS.register("oil", () -> new Material.Builder("oil")
                .fluid(GTFluidTypes.LIQUID, true)
                .color(0x0A0A0A)
                .flags(STICKY.get(), FLAMMABLE.get())
                .build());

        OilHeavy = MATERIALS.register("oil_heavy", () -> new Material.Builder("oil_heavy")
                .fluid(GTFluidTypes.LIQUID, true)
                .color(0x0A0A0A)
                .flags(STICKY.get(), FLAMMABLE.get())
                .build());

        RawOil = MATERIALS.register("oil_medium", () -> new Material.Builder("oil_medium")
                .fluid(GTFluidTypes.LIQUID, true)
                .color(0x0A0A0A)
                .flags(STICKY.get(), FLAMMABLE.get())
                .build());

        OilLight = MATERIALS.register("oil_light", () -> new Material.Builder("oil_light")
                .fluid(GTFluidTypes.LIQUID, true)
                .color(0x0A0A0A)
                .flags(STICKY.get(), FLAMMABLE.get())
                .build());

        NaturalGas = MATERIALS.register("natural_gas", () -> new Material.Builder("natural_gas")
                .fluid(GTFluidTypes.GAS, true)
                .flags(FLAMMABLE.get(), EXPLOSIVE.get()).build());

        Bacteria = MATERIALS.register("bacteria", () -> new Material.Builder("bacteria")
                .fluid().color(0x808000).build());

        BacterialSludge = MATERIALS.register("bacterial_sludge", () -> new Material.Builder("bacterial_sludge")
                .fluid().color(0x355E3B).build());

        EnrichedBacterialSludge = MATERIALS.register("enriched_bacterial_sludge", () -> new Material.Builder("enriched_bacterial_sludge")
                .fluid().color(0x7FFF00).build());

        // free id: 1587

        Mutagen = MATERIALS.register("mutagen", () -> new Material.Builder("mutagen")
                .fluid().color(0x00FF7F).build());

        GelatinMixture = MATERIALS.register("gelatin_mixture", () -> new Material.Builder("gelatin_mixture")
                .fluid().color(0x588BAE).build());

        RawGasoline = MATERIALS.register("raw_gasoline", () -> new Material.Builder("raw_gasoline")
                .fluid().color(0xFF6400).flags(FLAMMABLE.get()).build());

        Gasoline = MATERIALS.register("gasoline", () -> new Material.Builder("gasoline")
                .fluid().color(0xFAA500).flags(FLAMMABLE.get(), EXPLOSIVE.get()).build());

        HighOctaneGasoline = MATERIALS.register("gasoline_premium", () -> new Material.Builder("gasoline_premium")
                .fluid().color(0xFFA500).flags(FLAMMABLE.get(), EXPLOSIVE.get()).build());

        // free id: 1593

        CoalGas = MATERIALS.register("coal_gas", () -> new Material.Builder("coal_gas")
                .fluid(GTFluidTypes.GAS).color(0x333333).build());

        CoalTar = MATERIALS.register("coal_tar", () -> new Material.Builder("coal_tar")
                .fluid().color(0x1A1A1A).flags(STICKY.get(), FLAMMABLE.get()).build());

        Gunpowder = MATERIALS.register("gunpowder", () -> new Material.Builder("gunpowder")
                .dust(0)
                .color(0x808080).iconSet(ROUGH.get())
                .flags(FLAMMABLE.get(), EXPLOSIVE.get(), NO_SMELTING.get(), NO_SMASHING.get())
                .build());

        Oilsands = MATERIALS.register("oilsands", () -> new Material.Builder("oilsands")
                .dust(1).ore()
                .color(0x0A0A0A).iconSet(SAND.get())
                .flags(FLAMMABLE.get())
                .build());

        RareEarth = MATERIALS.register("rare_earth", () -> new Material.Builder("rare_earth")
                .dust(0)
                .color(0x808064).iconSet(FINE.get())
                .build());

        Stone = MATERIALS.register("stone", () -> new Material.Builder("stone")
                .dust(2)
                .color(0xCDCDCD).iconSet(ROUGH.get())
                .flags(MORTAR_GRINDABLE.get(), GENERATE_GEAR.get(), NO_SMASHING.get(), NO_SMELTING.get())
                .build());

        Lava = MATERIALS.register("lava", () -> new Material.Builder("lava")
                .fluid().color(0xFF4000).fluidTemp(1300).build());

        Glowstone = MATERIALS.register("glowstone", () -> new Material.Builder("glowstone")
                .dust(1).fluid()
                .color(0xFFFF00).iconSet(SHINY.get())
                .flags(NO_SMASHING.get(), GENERATE_PLATE.get(), EXCLUDE_PLATE_COMPRESSOR_RECIPE.get(), EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES.get())
                .fluidTemp(500)
                .build());

        NetherStar = MATERIALS.register("nether_star", () -> new Material.Builder("nether_star")
                .gem(4)
                .iconSet(NETHERSTAR.get())
                .flags(NO_SMASHING.get(), NO_SMELTING.get(), GENERATE_LENS.get())
                .build());

        Endstone = MATERIALS.register("endstone", () -> new Material.Builder("endstone")
                .dust(1)
                .color(0xD9DE9E)
                .flags(NO_SMASHING.get())
                .build());

        Netherrack = MATERIALS.register("netherrack", () -> new Material.Builder("netherrack")
                .dust(1)
                .color(0xC80000)
                .flags(NO_SMASHING.get(), FLAMMABLE.get())
                .build());

        CetaneBoostedDiesel = MATERIALS.register("nitro_fuel", () -> new Material.Builder("nitro_fuel")
                .fluid()
                .color(0xC8FF00)
                .flags(FLAMMABLE.get(), EXPLOSIVE.get())
                .build());

        Collagen = MATERIALS.register("collagen", () -> new Material.Builder("collagen")
                .dust(1)
                .color(0x80471C).iconSet(ROUGH.get())
                .build());

        Gelatin = MATERIALS.register("gelatin", () -> new Material.Builder("gelatin")
                .dust(1)
                .color(0x588BAE).iconSet(ROUGH.get())
                .build());

        Agar = MATERIALS.register("agar", () -> new Material.Builder("agar")
                .dust(1)
                .color(0x4F7942).iconSet(ROUGH.get())
                .build());

        // FREE ID 1609

        // FREE ID 1610

        // FREE ID 1611

        // Free ID 1612

        Milk = MATERIALS.register("milk", () -> new Material.Builder("milk")
                .fluid()
                .color(0xFEFEFE).iconSet(FINE.get())
                .fluidTemp(295)
                .build());

        Cocoa = MATERIALS.register("cocoa", () -> new Material.Builder("cocoa")
                .dust(0)
                .color(0x643200).iconSet(FINE.get())
                .build());

        Wheat = MATERIALS.register("wheat", () -> new Material.Builder("wheat")
                .dust(0)
                .color(0xFFFFC4).iconSet(FINE.get())
                .build());

        Meat = MATERIALS.register("meat", () -> new Material.Builder("meat")
                .dust(1)
                .color(0xC14C4C).iconSet(SAND.get())
                .build());

        Wood = MATERIALS.register("wood", () -> new Material.Builder("wood")
                .dust(0, 300)
                .color(0x643200).iconSet(WOOD.get())
                .flags(GENERATE_PLATE.get(), GENERATE_ROD.get(), GENERATE_BOLT_SCREW.get(), GENERATE_LONG_ROD.get(), FLAMMABLE.get(), GENERATE_GEAR.get(), GENERATE_FRAME.get())
                .build());

        Paper = MATERIALS.register("paper", () -> new Material.Builder("paper")
                .dust(0)
                .color(0xFAFAFA).iconSet(FINE.get())
                .flags(GENERATE_PLATE.get(), FLAMMABLE.get(), NO_SMELTING.get(), NO_SMASHING.get(),
                        MORTAR_GRINDABLE.get(), EXCLUDE_PLATE_COMPRESSOR_RECIPE.get())
                .build());

        FishOil = MATERIALS.register("fish_oil", () -> new Material.Builder("fish_oil")
                .fluid()
                .color(0xDCC15D)
                .flags(STICKY.get(), FLAMMABLE.get())
                .build());

        RubySlurry = MATERIALS.register("ruby_slurry", () -> new Material.Builder("ruby_slurry")
                .fluid().color(0xff6464).build());

        SapphireSlurry = MATERIALS.register("sapphire_slurry", () -> new Material.Builder("sapphire_slurry")
                .fluid().color(0x6464c8).build());

        GreenSapphireSlurry = MATERIALS.register("green_sapphire_slurry", () -> new Material.Builder("green_sapphire_slurry")
                .fluid().color(0x64c882).build());

        // These colors are much nicer looking than those in MC's EnumDyeColor
        DyeBlack = MATERIALS.register("dye_black", () -> new Material.Builder("dye_black")
                .fluid().color(0x202020).build());

        DyeRed = MATERIALS.register("dye_red", () -> new Material.Builder("dye_red")
                .fluid().color(0xFF0000).build());

        DyeGreen = MATERIALS.register("dye_green", () -> new Material.Builder("dye_green")
                .fluid().color(0x00FF00).build());

        DyeBrown = MATERIALS.register("dye_brown", () -> new Material.Builder("dye_brown")
                .fluid().color(0x604000).build());

        DyeBlue = MATERIALS.register("dye_blue", () -> new Material.Builder("dye_blue")
                .fluid().color(0x0020FF).build());

        DyePurple = MATERIALS.register("dye_purple", () -> new Material.Builder("dye_purple")
                .fluid().color(0x800080).build());

        DyeCyan = MATERIALS.register("dye_cyan", () -> new Material.Builder("dye_cyan")
                .fluid().color(0x00FFFF).build());

        DyeLightGray = MATERIALS.register("dye_light_gray", () -> new Material.Builder("dye_light_gray")
                .fluid().color(0xC0C0C0).build());

        DyeGray = MATERIALS.register("dye_gray", () -> new Material.Builder("dye_gray")
                .fluid().color(0x808080).build());

        DyePink = MATERIALS.register("dye_pink", () -> new Material.Builder("dye_pink")
                .fluid().color(0xFFC0C0).build());

        DyeLime = MATERIALS.register("dye_lime", () -> new Material.Builder("dye_lime")
                .fluid().color(0x80FF80).build());

        DyeYellow = MATERIALS.register("dye_yellow", () -> new Material.Builder("dye_yellow")
                .fluid().color(0xFFFF00).build());

        DyeLightBlue = MATERIALS.register("dye_light_blue", () -> new Material.Builder("dye_light_blue")
                .fluid().color(0x6080FF).build());

        DyeMagenta = MATERIALS.register("dye_magenta", () -> new Material.Builder("dye_magenta")
                .fluid().color(0xFF00FF).build());

        DyeOrange = MATERIALS.register("dye_orange", () -> new Material.Builder("dye_orange")
                .fluid().color(0xFF8000).build());

        DyeWhite = MATERIALS.register("dye_white", () -> new Material.Builder("dye_white")
                .fluid().color(0xFFFFFF).build());

        ImpureEnrichedNaquadahSolution = MATERIALS.register("impure_enriched_naquadah_solution", () -> new Material.Builder("impure_enriched_naquadah_solution")
                .fluid().color(0x388438).build());

        EnrichedNaquadahSolution = MATERIALS.register("enriched_naquadah_solution", () -> new Material.Builder("enriched_naquadah_solution")
                .fluid().color(0x3AAD3A).build());

        AcidicEnrichedNaquadahSolution = MATERIALS.register("acidic_enriched_naquadah_solution", () -> new Material.Builder("acidic_enriched_naquadah_solution")
                .fluid(GTFluidTypes.ACID).color(0x3DD63D).build());

        EnrichedNaquadahWaste = MATERIALS.register("enriched_naquadah_waste", () -> new Material.Builder("enriched_naquadah_waste")
                .fluid().color(0x355B35).build());

        ImpureNaquadriaSolution = MATERIALS.register("impure_naquadria_solution", () -> new Material.Builder("impure_naquadria_solution")
                .fluid().color(0x518451).build());

        NaquadriaSolution = MATERIALS.register("naquadria_solution", () -> new Material.Builder("naquadria_solution")
                .fluid().color(0x61AD61).build());

        AcidicNaquadriaSolution = MATERIALS.register("acidic_naquadria_solution", () -> new Material.Builder("acidic_naquadria_solution")
                .fluid(GTFluidTypes.ACID).color(0x70D670).build());

        NaquadriaWaste = MATERIALS.register("naquadria_waste", () -> new Material.Builder("naquadria_waste")
                .fluid().color(0x425B42).build());

        Lapotron = MATERIALS.register("lapotron", () -> new Material.Builder("lapotron")
                .gem()
                .color(0x2C39B1).iconSet(DIAMOND.get())
                .flags(NO_UNIFICATION.get())
                .build());

        TreatedWood = MATERIALS.register("treated_wood", () -> new Material.Builder("treated_wood")
                .dust(0, 300)
                .color(0x502800).iconSet(WOOD.get())
                .flags(GENERATE_PLATE.get(), FLAMMABLE.get(), GENERATE_ROD.get(), GENERATE_FRAME.get())
                .build());

        UUMatter = MATERIALS.register("uu_matter", () -> new Material.Builder("uu_matter")
                .fluid().fluidTemp(300)
                .build());
    }
}
