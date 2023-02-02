package net.nemezanevem.gregtech.api.unification.material.materials;

import net.minecraft.world.item.enchantment.Enchantments;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.properties.GtMaterialProperties;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.BlastProperty.GasTier;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.ToolProperty;
import net.nemezanevem.gregtech.api.GTValues;

import static net.nemezanevem.gregtech.api.registry.material.MaterialRegistry.MATERIALS;
import static net.nemezanevem.gregtech.api.unification.material.GtMaterials.*;
import static net.nemezanevem.gregtech.api.unification.material.properties.info.GtMaterialFlags.*;
import static net.nemezanevem.gregtech.api.unification.material.properties.info.GtMaterialIconSets.*;
import static net.nemezanevem.gregtech.api.GTValues.*;

public class SecondDegreeMaterials {

    public static void register() {

        Glass = MATERIALS.register("glass", () -> new Material.Builder("glass")
                .gem(0).fluid()
                .color(0xFAFAFA).iconSet(GLASS.get())
                .flags(GENERATE_LENS.get(), NO_SMASHING.get(), EXCLUDE_BLOCK_CRAFTING_RECIPES.get(), DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(SiliconDioxide, 1)
                .fluidTemp(1200)
                .build());

        Perlite = MATERIALS.register("perlite", () -> new Material.Builder("perlite")
                .dust(1)
                .color(0x1E141E)
                .components(Obsidian, 2, Water, 1)
                .build());

        Borax = MATERIALS.register("borax", () -> new Material.Builder("borax")
                .dust(1)
                .color(0xFAFAFA).iconSet(FINE.get())
                .components(Sodium, 2, Boron, 4, Water, 10, Oxygen, 7)
                .build());

        SaltWater = MATERIALS.register("salt_water", () -> new Material.Builder("salt_water")
                .fluid()
                .color(0x0000C8)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Salt, 1, Water, 1)
                .build());

        Olivine = MATERIALS.register("olivine", () -> new Material.Builder("olivine")
                .gem().ore(2, 1)
                .color(0x96FF96).iconSet(RUBY.get())
                .flags(EXT_METAL, NO_SMASHING.get(), NO_SMELTING.get(), HIGH_SIFTER_OUTPUT.get())
                .components(Magnesium, 2, Iron, 1, SiliconDioxide, 2)
                .build());

        Opal = MATERIALS.register("opal", () -> new Material.Builder("opal")
                .gem().ore()
                .color(0x0000FF).iconSet(OPAL.get())
                .flags(EXT_METAL, NO_SMASHING.get(), NO_SMELTING.get(), HIGH_SIFTER_OUTPUT.get(), DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(SiliconDioxide, 1)
                .build());

        Amethyst = MATERIALS.register("amethyst", () -> new Material.Builder("amethyst")
                .gem(3).ore()
                .color(0xD232D2).iconSet(RUBY.get())
                .flags(EXT_METAL, NO_SMASHING.get(), NO_SMELTING.get(), HIGH_SIFTER_OUTPUT.get())
                .components(SiliconDioxide, 4, Iron, 1)
                .build());

        Lapis = MATERIALS.register("lapis", () -> new Material.Builder("lapis")
                .gem(1).ore(6, 4)
                .color(0x4646DC).iconSet(LAPIS.get())
                .flags(NO_SMASHING.get(), NO_SMELTING.get(), CRYSTALLIZABLE.get(), NO_WORKING.get(), DECOMPOSITION_BY_ELECTROLYZING.get(), EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES.get(),
                        GENERATE_PLATE.get(), GENERATE_ROD.get())
                .components(Lazurite, 12, Sodalite, 2, Pyrite, 1, Calcite, 1)
                .build());

        Blaze = MATERIALS.register("blaze", () -> new Material.Builder("blaze")
                .dust(1).fluid()
                .color(0xFFC800, false).iconSet(FINE.get())
                .flags(NO_SMELTING.get(), MORTAR_GRINDABLE.get(), DECOMPOSITION_BY_CENTRIFUGING.get()) //todo burning flag
                .components(DarkAsh, 1, Sulfur, 1)
                .fluidTemp(4000)
                .build());
        
        Apatite = MATERIALS.register("apatite", () -> new Material.Builder("apatite")
                .gem(1).ore(4, 2)
                .color(0xC8C8FF).iconSet(DIAMOND.get())
                .flags(NO_SMASHING.get(), NO_SMELTING.get(), CRYSTALLIZABLE.get(), GENERATE_BOLT_SCREW.get())
                .components(Calcium, 5, Phosphate, 3, Chlorine, 1)
                .build());

        BlackSteel = MATERIALS.register("black_steel", () -> new Material.Builder("black_steel")
                .ingot().fluid()
                .color(0x646464).iconSet(METALLIC.get())
                .flags(EXT_METAL, GENERATE_FINE_WIRE.get(), GENERATE_GEAR.get(), GENERATE_FRAME.get())
                .components(Nickel, 1, BlackBronze, 1, Steel, 3)
                .cableProperties(GTValues.V[4], 3, 2)
                .blastTemp(1200, GasTier.LOW)
                .build());

        DamascusSteel = MATERIALS.register("damascus_steel", () -> new Material.Builder("damascus_steel")
                .ingot(3).fluid()
                .color(0x6E6E6E).iconSet(METALLIC.get())
                .flags(EXT_METAL)
                .components(Steel, 1)
                .toolStats(ToolProperty.Builder.of(6.0F, 4.0F, 1024, 3)
                        .attackSpeed(0.3F).enchantability(33)
                        .enchantment(Enchantments.MOB_LOOTING, 3)
                        .enchantment(Enchantments.BLOCK_FORTUNE, 3).build())
                .blastTemp(1500, GasTier.LOW)
                .build());

        TungstenSteel = MATERIALS.register("tungsten_steel", () -> new Material.Builder("tungsten_steel")
                .ingot(4).fluid()
                .color(0x6464A0).iconSet(METALLIC.get())
                .flags(EXT2_METAL, GENERATE_ROTOR.get(), GENERATE_SMALL_GEAR.get(), GENERATE_DENSE.get(), GENERATE_FRAME.get(), GENERATE_SPRING.get(), GENERATE_FOIL.get(), GENERATE_FINE_WIRE.get(), GENERATE_GEAR.get())
                .components(Steel, 1, Tungsten, 1)
                .toolStats(ToolProperty.Builder.of(9.0F, 7.0F, 2048, 4)
                        .enchantability(14).build())
                .rotorStats(8.0f, 4.0f, 2560)
                .fluidPipeProperties(3587, 225, true)
                .cableProperties(GTValues.V[5], 3, 2)
                .blastTemp(3000, GasTier.MID, GTValues.VA[EV], 1000)
                .build());

        CobaltBrass = MATERIALS.register("cobalt_brass", () -> new Material.Builder("cobalt_brass")
                .ingot().fluid()
                .color(0xB4B4A0).iconSet(METALLIC.get())
                .flags(EXT2_METAL, GENERATE_GEAR.get())
                .components(Brass, 7, Aluminium, 1, Cobalt, 1)
                .toolStats(ToolProperty.Builder.of(2.5F, 2.0F, 1024, 2)
                        .attackSpeed(-0.2F).enchantability(5).build())
                .rotorStats(8.0f, 2.0f, 256)
                .itemPipeProperties(2048, 1)
                .fluidTemp(1202)
                .build());

        TricalciumPhosphate = MATERIALS.register("tricalcium_phosphate", () -> new Material.Builder("tricalcium_phosphate")
                .dust().ore(3, 1)
                .color(0xFFFF00).iconSet(FLINT.get())
                .flags(NO_SMASHING.get(), NO_SMELTING.get(), FLAMMABLE.get(), EXPLOSIVE.get(), DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(Calcium, 3, Phosphate, 2)
                .build());

        GarnetRed = MATERIALS.register("garnet_red", () -> new Material.Builder("garnet_red")
                .gem().ore(4, 1)
                .color(0xC85050).iconSet(RUBY.get())
                .flags(EXT_METAL, NO_SMASHING.get(), NO_SMELTING.get(), HIGH_SIFTER_OUTPUT.get(), DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(Pyrope, 3, Almandine, 5, Spessartine, 8)
                .build());

        GarnetYellow = MATERIALS.register("garnet_yellow", () -> new Material.Builder("garnet_yellow")
                .gem().ore(4, 1)
                .color(0xC8C850).iconSet(RUBY.get())
                .flags(EXT_METAL, NO_SMASHING.get(), NO_SMELTING.get(), HIGH_SIFTER_OUTPUT.get(), DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(Andradite, 5, Grossular, 8, Uvarovite, 3)
                .build());

        Marble = MATERIALS.register("marble", () -> new Material.Builder("marble")
                .dust()
                .color(0xC8C8C8).iconSet(ROUGH.get())
                .flags(NO_SMASHING.get(), DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(Magnesium, 1, Calcite, 7)
                .build());

        GraniteBlack = MATERIALS.register("granite_black", () -> new Material.Builder("granite_black")
                .dust()
                .color(0x0A0A0A).iconSet(ROUGH.get())
                .flags(NO_SMASHING.get(), DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(SiliconDioxide, 4, Biotite, 1)
                .build());

        GraniteRed = MATERIALS.register("granite_red", () -> new Material.Builder("granite_red")
                .dust()
                .color(0xFF0080).iconSet(ROUGH.get())
                .flags(NO_SMASHING.get())
                .components(Aluminium, 2, PotassiumFeldspar, 1, Oxygen, 3)
                .build());

        // Free ID 2021

        VanadiumMagnetite = MATERIALS.register("vanadium_magnetite", () -> new Material.Builder("vanadium_magnetite")
                .dust().ore()
                .color(0x23233C).iconSet(METALLIC.get())
                .flags(DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(Magnetite, 1, Vanadium, 1)
                .build());

        QuartzSand = MATERIALS.register("quartz_sand", () -> new Material.Builder("quartz_sand")
                .dust(1)
                .color(0xC8C8C8).iconSet(SAND.get())
                .flags(DISABLE_DECOMPOSITION.get())
                .components(CertusQuartz, 1, Quartzite, 1)
                .build());

        Pollucite = MATERIALS.register("pollucite", () -> new Material.Builder("pollucite")
                .dust().ore()
                .color(0xF0D2D2)
                .components(Caesium, 2, Aluminium, 2, Silicon, 4, Water, 2, Oxygen, 12)
                .build());

        // Free ID 2025

        Bentonite = MATERIALS.register("bentonite", () -> new Material.Builder("bentonite")
                .dust().ore(3, 1)
                .color(0xF5D7D2).iconSet(ROUGH.get())
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Sodium, 1, Magnesium, 6, Silicon, 12, Hydrogen, 4, Water, 5, Oxygen, 36)
                .build());

        FullersEarth = MATERIALS.register("fullers_earth", () -> new Material.Builder("fullers_earth")
                .dust().ore(2, 1)
                .color(0xA0A078).iconSet(FINE.get())
                .components(Magnesium, 1, Silicon, 4, Hydrogen, 1, Water, 4, Oxygen, 11)
                .build());

        Pitchblende = MATERIALS.register("pitchblende", () -> new Material.Builder("pitchblende")
                .dust(3).ore(true)
                .color(0xC8D200)
                .flags(DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(Uraninite, 3, Thorium, 1, Lead, 1)
                .build()
                .setFormula("(UO2)3ThPb", true));

        Monazite = MATERIALS.register("monazite", () -> new Material.Builder("monazite")
                .gem(1).ore(4, 2, true)
                .color(0x324632).iconSet(DIAMOND.get())
                .flags(NO_SMASHING.get(), NO_SMELTING.get(), CRYSTALLIZABLE.get())
                .components(RareEarth, 1, Phosphate, 1)
                .build());

        Mirabilite = MATERIALS.register("mirabilite", () -> new Material.Builder("mirabilite")
                .dust()
                .color(0xF0FAD2)
                .components(Sodium, 2, Sulfur, 1, Water, 10, Oxygen, 4)
                .build());

        Trona = MATERIALS.register("trona", () -> new Material.Builder("trona")
                .dust(1).ore(2, 1)
                .color(0x87875F).iconSet(METALLIC.get())
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Sodium, 3, Carbon, 2, Hydrogen, 1, Water, 2, Oxygen, 6)
                .build());

        Gypsum = MATERIALS.register("gypsum", () -> new Material.Builder("gypsum")
                .dust(1).ore()
                .color(0xE6E6FA)
                .components(Calcium, 1, Sulfur, 1, Water, 2, Oxygen, 4)
                .build());

        Zeolite = MATERIALS.register("zeolite", () -> new Material.Builder("zeolite")
                .dust().ore(3, 1)
                .color(0xF0E6E6)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Sodium, 1, Calcium, 4, Silicon, 27, Aluminium, 9, Water, 28, Oxygen, 72)
                .build());

        Concrete = MATERIALS.register("concrete", () -> new Material.Builder("concrete")
                .dust().fluid()
                .color(0x646464).iconSet(ROUGH.get())
                .flags(NO_SMASHING.get(), EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES.get())
                .components(Stone, 1)
                .fluidTemp(286)
                .build());

        SteelMagnetic = MATERIALS.register("steel_magnetic", () -> new Material.Builder("steel_magnetic")
                .ingot()
                .color(0x808080).iconSet(MAGNETIC.get())
                .flags(GENERATE_ROD.get(), IS_MAGNETIC.get())
                .components(Steel.get(), 1)
                .ingotSmeltInto(Steel.get())
                .arcSmeltInto(Steel.get())
                .macerateInto(Steel.get())
                .build());
        Steel.get().getProperty(GtMaterialProperties.INGOT.get()).setMagneticMaterial(SteelMagnetic.get());

        VanadiumSteel = MATERIALS.register("vanadium_steel", () -> new Material.Builder("vanadium_steel")
                .ingot(3).fluid()
                .color(0xc0c0c0).iconSet(METALLIC.get())
                .flags(EXT2_METAL, GENERATE_FOIL.get(), GENERATE_GEAR.get())
                .components(Vanadium, 1, Chrome, 1, Steel, 7)
                .toolStats(ToolProperty.Builder.of(3.0F, 3.0F, 1536, 3)
                        .attackSpeed(-0.2F).enchantability(5).build())
                .rotorStats(7.0f, 3.0f, 1920)
                .fluidPipeProperties(2073, 50, true, true, false, false)
                .blastTemp(1453, GasTier.LOW)
                .fluidTemp(2073)
                .build());

        Potin = MATERIALS.register("potin", () -> new Material.Builder("potin")
                .ingot().fluid()
                .color(0xc99781).iconSet(METALLIC.get())
                .flags(EXT2_METAL, GENERATE_GEAR.get())
                .components(Copper, 6, Tin, 2, Lead, 1)
                .fluidPipeProperties(1456, 32, true)
                .fluidTemp(1084)
                .build());

        BorosilicateGlass = MATERIALS.register("borosilicate_glass", () -> new Material.Builder("borosilicate_glass")
                .ingot(1).fluid()
                .color(0xE6F3E6).iconSet(SHINY.get())
                .flags(GENERATE_FINE_WIRE.get(), GENERATE_PLATE.get())
                .components(Boron, 1, SiliconDioxide, 7)
                .fluidTemp(1921)
                .build());

        Andesite = MATERIALS.register("andesite", () -> new Material.Builder("andesite")
                .dust()
                .color(0xBEBEBE).iconSet(ROUGH.get())
                .flags(DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(Asbestos, 4, Saltpeter, 1)
                .build());

        // FREE ID 2040

        // FREE ID 2041

        NaquadahAlloy = MATERIALS.register("naquadah_alloy", () -> new Material.Builder("naquadah_alloy")
                .ingot(5).fluid()
                .color(0x282828).iconSet(METALLIC.get())
                .flags(EXT2_METAL, GENERATE_SPRING.get(), GENERATE_RING.get(), GENERATE_ROTOR.get(), GENERATE_SMALL_GEAR.get(), GENERATE_FRAME.get(), GENERATE_DENSE.get(), GENERATE_FOIL.get(), GENERATE_GEAR.get())
                .components(Naquadah, 2, Osmiridium, 1, Trinium, 1)
                .toolStats(ToolProperty.Builder.of(40.0F, 12.0F, 3072, 5)
                        .attackSpeed(0.3F).enchantability(33).magnetic().build())
                .rotorStats(8.0f, 5.0f, 5120)
                .cableProperties(GTValues.V[8], 2, 4)
                .blastTemp(7200, GasTier.HIGH, VA[LuV], 1000)
                .build());

        SulfuricNickelSolution = MATERIALS.register("sulfuric_nickel_solution", () -> new Material.Builder("sulfuric_nickel_solution")
                .fluid(GTFluidTypes.ACID)
                .color(0x3EB640)
                .components(Nickel, 1, Oxygen, 1, SulfuricAcid, 1)
                .build());

        SulfuricCopperSolution = MATERIALS.register("sulfuric_copper_solution", () -> new Material.Builder("sulfuric_copper_solution")
                .fluid(GTFluidTypes.ACID)
                .color(0x48A5C0)
                .components(Copper, 1, Oxygen, 1, SulfuricAcid, 1)
                .build());

        LeadZincSolution = MATERIALS.register("lead_zinc_solution", () -> new Material.Builder("lead_zinc_solution")
                .fluid()
                .flags(DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(Lead, 1, Silver, 1, Zinc, 1, Sulfur, 3, Water, 1)
                .build());

        NitrationMixture = MATERIALS.register("nitration_mixture", () -> new Material.Builder("nitration_mixture")
                .fluid(GTFluidTypes.ACID)
                .color(0xE6E2AB)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(NitricAcid, 1, SulfuricAcid, 1)
                .build());

        DilutedSulfuricAcid = MATERIALS.register("diluted_sulfuric_acid", () -> new Material.Builder("diluted_sulfuric_acid")
                .fluid(GTFluidTypes.ACID)
                .color(0xC07820)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(SulfuricAcid, 2, Water, 1)
                .build());

        DilutedHydrochloricAcid = MATERIALS.register("diluted_hydrochloric_acid", () -> new Material.Builder("diluted_hydrochloric_acid")
                .fluid(GTFluidTypes.ACID)
                .color(0x99A7A3)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(HydrochloricAcid, 1, Water, 1)
                .build());

        Flint = MATERIALS.register("flint", () -> new Material.Builder("flint")
                .gem(1)
                .color(0x002040).iconSet(FLINT.get())
                .flags(NO_SMASHING.get(), MORTAR_GRINDABLE.get(), DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(SiliconDioxide, 1)
                .toolStats(ToolProperty.Builder.of(0.0F, 1.0F, 64, 1)
                        .enchantability(5).ignoreCraftingTools()
                        .enchantment(Enchantments.FIRE_ASPECT, 2).build())
                .build());

        Air = MATERIALS.register("air", () -> new Material.Builder("air")
                .fluid(GTFluidTypes.GAS)
                .color(0xA9D0F5)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Nitrogen, 78, Oxygen, 21, Argon, 9)
                .build());

        LiquidAir = MATERIALS.register("liquid_air", () -> new Material.Builder("liquid_air")
                .fluid()
                .color(0xA9D0F5)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Nitrogen, 70, Oxygen, 22, CarbonDioxide, 5, Helium, 2, Argon, 1, Ice, 1)
                .fluidTemp(79)
                .build());

        NetherAir = MATERIALS.register("nether_air", () -> new Material.Builder("nether_air")
                .fluid(GTFluidTypes.GAS)
                .color(0x4C3434)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(CarbonMonoxide, 78, HydrogenSulfide, 21, Neon, 9)
                .build());

        LiquidNetherAir = MATERIALS.register("liquid_nether_air", () -> new Material.Builder("liquid_nether_air")
                .fluid()
                .color(0x4C3434)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(CarbonMonoxide, 144, CoalGas, 20, HydrogenSulfide, 15, SulfurDioxide, 15, Helium3, 5, Neon, 1, Ash, 1)
                .fluidTemp(58)
                .build());

        EnderAir = MATERIALS.register("ender_air", () -> new Material.Builder("ender_air")
                .fluid(GTFluidTypes.GAS)
                .color(0x283454)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(NitrogenDioxide, 78, Deuterium, 21, Xenon, 9)
                .build());

        LiquidEnderAir = MATERIALS.register("liquid_ender_air", () -> new Material.Builder("liquid_ender_air")
                .fluid()
                .color(0x283454)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(NitrogenDioxide, 122, Deuterium, 50, Helium, 15, Tritium, 10, Krypton, 1, Xenon, 1, Radon, 1, EnderPearl, 1)
                .fluidTemp(36)
                .build());

        AquaRegia = MATERIALS.register("aqua_regia", () -> new Material.Builder("aqua_regia")
                .fluid(GTFluidTypes.ACID)
                .color(0xFFB132)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(NitricAcid, 1, HydrochloricAcid, 2)
                .build());

        PlatinumSludgeResidue = MATERIALS.register("platinum_sludge_residue", () -> new Material.Builder("platinum_sludge_residue")
                .dust()
                .color(0x827951)
                .flags(DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(SiliconDioxide, 2, Gold, 3)
                .build());

        PalladiumRaw = MATERIALS.register("palladium_raw", () -> new Material.Builder("palladium_raw")
                .dust()
                .color(Palladium.get().getMaterialRGB()).iconSet(METALLIC.get())
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Palladium, 1, Ammonia, 1)
                .build());

        RarestMetalMixture = MATERIALS.register("rarest_metal_mixture", () -> new Material.Builder("rarest_metal_mixture")
                .dust()
                .color(0x832E11).iconSet(SHINY.get())
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Iridium, 1, Osmium, 1, Oxygen, 4, Water, 1)
                .build());

        AmmoniumChloride = MATERIALS.register("ammonium_chloride", () -> new Material.Builder("ammonium_chloride")
                .dust()
                .color(0x9711A6)
                .components(Ammonia, 1, HydrochloricAcid, 1)
                .build()
                .setFormula("NH4Cl", true));

        AcidicOsmiumSolution = MATERIALS.register("acidic_osmium_solution", () -> new Material.Builder("acidic_osmium_solution")
                .fluid(GTFluidTypes.ACID)
                .color(0xA3AA8A)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Osmium, 1, Oxygen, 4, Water, 1, HydrochloricAcid, 1)
                .build());

        RhodiumPlatedPalladium = MATERIALS.register("rhodium_plated_palladium", () -> new Material.Builder("rhodium_plated_palladium")
                .ingot().fluid()
                .color(0xDAC5C5).iconSet(SHINY.get())
                .flags(EXT2_METAL, GENERATE_ROTOR.get(), GENERATE_DENSE.get(), GENERATE_SMALL_GEAR.get())
                .components(Palladium, 3, Rhodium, 1)
                .rotorStats(12.0f, 3.0f, 1024)
                .blastTemp(4500, GasTier.HIGH, VA[IV], 1200)
                .build());

        Clay = MATERIALS.register("clay", () -> new Material.Builder("clay")
                .dust(1)
                .color(0xC8C8DC).iconSet(ROUGH.get())
                .flags(MORTAR_GRINDABLE.get(), EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES.get())
                .components(Sodium, 2, Lithium, 1, Aluminium, 2, Silicon, 2, Water, 6)
                .build());

        Redstone = MATERIALS.register("redstone", () -> new Material.Builder("redstone")
                .dust().ore(5, 1, true).fluid()
                .color(0xC80000).iconSet(ROUGH.get())
                .flags(GENERATE_PLATE.get(), NO_SMASHING.get(), NO_SMELTING.get(), EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES.get(),
                        EXCLUDE_PLATE_COMPRESSOR_RECIPE.get(), DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(Silicon, 1, Pyrite, 5, Ruby, 1, Mercury, 3)
                .fluidTemp(500)
                .build());
    }
}
