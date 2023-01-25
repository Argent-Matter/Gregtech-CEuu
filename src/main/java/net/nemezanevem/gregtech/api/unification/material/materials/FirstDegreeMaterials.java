package net.nemezanevem.gregtech.api.unification.material.materials;

import net.minecraft.world.item.enchantment.Enchantments;
import net.nemezanevem.gregtech.api.fluids.GtFluidTypes;
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

public class FirstDegreeMaterials {

    public static void register() {
        Almandine = MATERIALS.register("almandine", () -> new Material.Builder("almandine")
                .gem(1).ore(3, 1)
                .color(0xFF0000)
                .components(Aluminium, 2, Iron, 3, Silicon, 3, Oxygen, 12)
                .build());

        Andradite = MATERIALS.register("andradite", () -> new Material.Builder("andradite")
                .gem(1)
                .color(0x967800).iconSet(RUBY.get())
                .components(Calcium, 3, Iron, 2, Silicon, 3, Oxygen, 12)
                .build());

        AnnealedCopper = MATERIALS.register("annealed_copper", () -> new Material.Builder("annealed_copper")
                .ingot().fluid()
                .color(0xFF8D3B).iconSet(BRIGHT.get())
                .flags(EXT2_METAL, MORTAR_GRINDABLE.get(), GENERATE_FINE_WIRE.get())
                .components(Copper, 1)
                .cableProperties(GTValues.V[2], 1, 1)
                .fluidTemp(1358)
                .build());
        Copper.get().getProperty(GtMaterialProperties.INGOT.get()).setArcSmeltingInto(AnnealedCopper.get());

        Asbestos = MATERIALS.register("asbestos", () -> new Material.Builder("asbestos")
                .dust(1).ore(3, 1)
                .color(0xE6E6E6)
                .components(Magnesium, 3, Silicon, 2, Hydrogen, 4, Oxygen, 9)
                .build());

        Ash = MATERIALS.register("ash", () -> new Material.Builder("ash")
                .dust(1)
                .color(0x969696)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 1)
                .build());

        BandedIron = MATERIALS.register("banded_iron", () -> new Material.Builder("banded_iron")
                .dust().ore()
                .color(0x915A5A)
                .components(Iron, 2, Oxygen, 3)
                .build());

        BatteryAlloy = MATERIALS.register("battery_alloy", () -> new Material.Builder("battery_alloy")
                .ingot(1).fluid()
                .color(0x9C7CA0)
                .flags(EXT_METAL)
                .components(Lead, 4, Antimony, 1)
                .fluidTemp(660)
                .build());

        BlueTopaz = MATERIALS.register("blue_topaz", () -> new Material.Builder("blue_topaz")
                .gem(3).ore(2, 1)
                .color(0x7B96DC).iconSet(GEM_HORIZONTAL.get())
                .flags(EXT_METAL, NO_SMASHING.get(), NO_SMELTING.get(), HIGH_SIFTER_OUTPUT.get())
                .components(Aluminium, 2, Silicon, 1, Fluorine, 2, Hydrogen, 2, Oxygen, 6)
                .build());

        Bone = MATERIALS.register("bone", () -> new Material.Builder("bone")
                .dust(1)
                .color(0xFAFAFA)
                .flags(MORTAR_GRINDABLE.get(), EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES.get())
                .components(Calcium, 1)
                .build());

        Brass = MATERIALS.register("brass", () -> new Material.Builder("brass")
                .ingot(1).fluid()
                .color(0xFFB400).iconSet(METALLIC.get())
                .flags(EXT2_METAL, MORTAR_GRINDABLE.get())
                .components(Zinc, 1, Copper, 3)
                .rotorStats(8.0f, 3.0f, 152)
                .itemPipeProperties(2048, 1)
                .fluidTemp(1160)
                .build());

        Bronze = MATERIALS.register("bronze", () -> new Material.Builder("bronze")
                .ingot().fluid()
                .color(0xFF8000).iconSet(METALLIC.get())
                .flags(EXT2_METAL, MORTAR_GRINDABLE.get(), GENERATE_ROTOR.get(), GENERATE_FRAME.get(), GENERATE_SMALL_GEAR.get(), GENERATE_FOIL.get(), GENERATE_GEAR.get())
                .components(Tin, 1, Copper, 3)
                .toolStats(ToolProperty.Builder.of(3.0F, 2.0F, 192, 2)
                        .enchantability(18).build())
                .rotorStats(6.0f, 2.5f, 192)
                .fluidPipeProperties(1696, 20, true)
                .fluidTemp(1357)
                .build());

        BrownLimonite = MATERIALS.register("brown_limonite", () -> new Material.Builder("brown_limonite")
                .dust(1).ore()
                .color(0xC86400).iconSet(METALLIC.get())
                .flags(DECOMPOSITION_BY_CENTRIFUGING.get(), BLAST_FURNACE_CALCITE_TRIPLE.get())
                .components(Iron, 1, Hydrogen, 1, Oxygen, 2)
                .build());

        Calcite = MATERIALS.register("calcite", () -> new Material.Builder("calcite")
                .dust(1).ore()
                .color(0xFAE6DC)
                .components(Calcium, 1, Carbon, 1, Oxygen, 3)
                .build());

        Cassiterite = MATERIALS.register("cassiterite", () -> new Material.Builder("cassiterite")
                .dust(1).ore(2, 1)
                .color(0xDCDCDC).iconSet(METALLIC.get())
                .components(Tin, 1, Oxygen, 2)
                .build());

        CassiteriteSand = MATERIALS.register("cassiterite_sand", () -> new Material.Builder("cassiterite_sand")
                .dust(1).ore(2, 1)
                .color(0xDCDCDC).iconSet(SAND.get())
                .components(Tin, 1, Oxygen, 2)
                .build());

        Chalcopyrite = MATERIALS.register("chalcopyrite", () -> new Material.Builder("chalcopyrite")
                .dust(1).ore()
                .color(0xA07828)
                .components(Copper, 1, Iron, 1, Sulfur, 2)
                .build());

        Charcoal = MATERIALS.register("charcoal", () -> new Material.Builder("charcoal")
                .gem(1, 1600) //default charcoal burn time in vanilla
                .color(0x644646).iconSet(FINE.get())
                .flags(FLAMMABLE.get(), NO_SMELTING.get(), NO_SMASHING.get(), MORTAR_GRINDABLE.get())
                .components(Carbon, 1)
                .build());

        Chromite = MATERIALS.register("chromite", () -> new Material.Builder("chromite")
                .dust(1).ore()
                .color(0x23140F).iconSet(METALLIC.get())
                .components(Iron, 1, Chrome, 2, Oxygen, 4)
                .build());

        Cinnabar = MATERIALS.register("cinnabar", () -> new Material.Builder("cinnabar")
                .gem(1).ore()
                .color(0x960000).iconSet(EMERALD.get())
                .flags(CRYSTALLIZABLE.get(), DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(Mercury, 1, Sulfur, 1)
                .build());

        Water = MATERIALS.register("water", () -> new Material.Builder("water")
                .fluid()
                .color(0x0000FF)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Hydrogen, 2, Oxygen, 1)
                .fluidTemp(300)
                .build());

        LiquidOxygen = MATERIALS.register("liquid_oxygen", () -> new Material.Builder("liquid_oxygen")
                .fluid()
                .color(0x6688DD)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Oxygen, 1)
                .fluidTemp(85)
                .build());

        Coal = MATERIALS.register("coal", () -> new Material.Builder("coal")
                .gem(1, 1600).ore(2, 1) //default coal burn time in vanilla
                .color(0x464646).iconSet(LIGNITE.get())
                .flags(FLAMMABLE.get(), NO_SMELTING.get(), NO_SMASHING.get(), MORTAR_GRINDABLE.get(), EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES.get(), DISABLE_DECOMPOSITION.get())
                .components(Carbon, 1)
                .build());

        Cobaltite = MATERIALS.register("cobaltite", () -> new Material.Builder("cobaltite")
                .dust(1).ore()
                .color(0x5050FA).iconSet(METALLIC.get())
                .components(Cobalt, 1, Arsenic, 1, Sulfur, 1)
                .build());

        Cooperite = MATERIALS.register("cooperite", () -> new Material.Builder("cooperite")
                .dust(1).ore()
                .color(0xFFFFC8).iconSet(METALLIC.get())
                .components(Platinum, 3, Nickel, 1, Sulfur, 1, Palladium, 1)
                .build());

        Cupronickel = MATERIALS.register("cupronickel", () -> new Material.Builder("cupronickel")
                .ingot(1).fluid()
                .color(0xE39680).iconSet(METALLIC.get())
                .flags(EXT_METAL, GENERATE_SPRING.get(), GENERATE_FINE_WIRE.get())
                .components(Copper, 1, Nickel, 1)
                .itemPipeProperties(2048, 1)
                .cableProperties(GTValues.V[2], 1, 1)
                .fluidTemp(1542)
                .build());

        DarkAsh = MATERIALS.register("dark_ash", () -> new Material.Builder("dark_ash")
                .dust(1)
                .color(0x323232)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 1)
                .build());

        Diamond = MATERIALS.register("diamond", () -> new Material.Builder("diamond")
                .gem(3).ore()
                .color(0xC8FFFF).iconSet(DIAMOND.get())
                .flags(GENERATE_BOLT_SCREW.get(), GENERATE_LENS.get(), GENERATE_GEAR.get(), NO_SMASHING.get(), NO_SMELTING.get(),
                        HIGH_SIFTER_OUTPUT.get(), DISABLE_DECOMPOSITION.get(), EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES.get())
                .components(Carbon, 1)
                .toolStats(ToolProperty.Builder.of(6.0F, 7.0F, 768, 3)
                        .attackSpeed(0.1F).enchantability(18).build())
                .build());

        Electrum = MATERIALS.register("electrum", () -> new Material.Builder("electrum")
                .ingot().fluid()
                .color(0xFFFF64).iconSet(SHINY.get())
                .flags(EXT2_METAL, MORTAR_GRINDABLE.get(), GENERATE_FINE_WIRE.get(), GENERATE_RING.get())
                .components(Silver, 1, Gold, 1)
                .itemPipeProperties(1024, 2)
                .cableProperties(GTValues.V[3], 2, 2)
                .fluidTemp(1285)
                .build());

        Emerald = MATERIALS.register("emerald", () -> new Material.Builder("emerald")
                .gem().ore(2, 1)
                .color(0x50FF50).iconSet(EMERALD.get())
                .flags(EXT_METAL, NO_SMASHING.get(), NO_SMELTING.get(), HIGH_SIFTER_OUTPUT.get(), EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES.get(), GENERATE_LENS.get())
                .components(Beryllium, 3, Aluminium, 2, Silicon, 6, Oxygen, 18)
                .build());

        Galena = MATERIALS.register("galena", () -> new Material.Builder("galena")
                .dust(3).ore()
                .color(0x643C64)
                .flags(NO_SMELTING.get())
                .components(Lead, 1, Sulfur, 1)
                .build());

        Garnierite = MATERIALS.register("garnierite", () -> new Material.Builder("garnierite")
                .dust(3).ore()
                .color(0x32C846).iconSet(METALLIC.get())
                .components(Nickel, 1, Oxygen, 1)
                .build());

        GreenSapphire = MATERIALS.register("green_sapphire", () -> new Material.Builder("green_sapphire")
                .gem().ore()
                .color(0x64C882).iconSet(GEM_HORIZONTAL.get())
                .flags(EXT_METAL, NO_SMASHING.get(), NO_SMELTING.get(), HIGH_SIFTER_OUTPUT.get())
                .components(Aluminium, 2, Oxygen, 3)
                .build());

        Grossular = MATERIALS.register("grossular", () -> new Material.Builder("grossular")
                .gem(1).ore(3, 1)
                .color(0xC86400).iconSet(RUBY.get())
                .components(Calcium, 3, Aluminium, 2, Silicon, 3, Oxygen, 12)
                .build());

        Ice = MATERIALS.register("ice", () -> new Material.Builder("ice")
                .dust(0).fluid()
                .color(0xC8C8FF, false).iconSet(SHINY.get())
                .flags(NO_SMASHING.get(), EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES.get(), DISABLE_DECOMPOSITION.get())
                .components(Hydrogen, 2, Oxygen, 1)
                .fluidTemp(273)
                .build());

        Ilmenite = MATERIALS.register("ilmenite", () -> new Material.Builder("ilmenite")
                .dust(3).ore()
                .color(0x463732).iconSet(METALLIC.get())
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Iron, 1, Titanium, 1, Oxygen, 3)
                .build());

        Rutile = MATERIALS.register("rutile", () -> new Material.Builder("rutile")
                .gem()
                .color(0xD40D5C).iconSet(GEM_HORIZONTAL.get())
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Titanium, 1, Oxygen, 2)
                .build());

        Bauxite = MATERIALS.register("bauxite", () -> new Material.Builder("bauxite")
                .dust(1).ore()
                .color(0xC86400)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Aluminium, 2, Oxygen, 3)
                .build());

        Invar = MATERIALS.register("invar", () -> new Material.Builder("invar")
                .ingot().fluid()
                .color(0xB4B478).iconSet(METALLIC.get())
                .flags(EXT2_METAL, MORTAR_GRINDABLE.get(), GENERATE_FRAME.get(), GENERATE_GEAR.get())
                .components(Iron, 2, Nickel, 1)
                .toolStats(ToolProperty.Builder.of(4.0F, 3.0F, 384, 2)
                        .enchantability(18)
                        .enchantment(Enchantments.BANE_OF_ARTHROPODS, 3)
                        .enchantment(Enchantments.BLOCK_EFFICIENCY, 1).build())
                .rotorStats(7.0f, 3.0f, 512)
                .fluidTemp(1916)
                .build());

        Kanthal = MATERIALS.register("kanthal", () -> new Material.Builder("kanthal")
                .ingot().fluid()
                .color(0xC2D2DF).iconSet(METALLIC.get())
                .flags(EXT_METAL, GENERATE_SPRING.get())
                .components(Iron, 1, Aluminium, 1, Chrome, 1)
                .cableProperties(GTValues.V[3], 4, 3)
                .blastTemp(1800, GasTier.LOW, VA[MV], 1000)
                .fluidTemp(1708)
                .build());

        Lazurite = MATERIALS.register("lazurite", () -> new Material.Builder("lazurite")
                .gem(1).ore(6, 4)
                .color(0x6478FF).iconSet(LAPIS.get())
                .flags(GENERATE_PLATE.get(), NO_SMASHING.get(), NO_SMELTING.get(), CRYSTALLIZABLE.get(), GENERATE_ROD.get(), DECOMPOSITION_BY_ELECTROLYZING.get())
                .components(Aluminium, 6, Silicon, 6, Calcium, 8, Sodium, 8)
                .build());

        Magnalium = MATERIALS.register("magnalium", () -> new Material.Builder("magnalium")
                .ingot().fluid()
                .color(0xC8BEFF)
                .flags(EXT2_METAL)
                .components(Magnesium, 1, Aluminium, 2)
                .rotorStats(6.0f, 2.0f, 256)
                .itemPipeProperties(1024, 2)
                .fluidTemp(929)
                .build());

        Magnesite = MATERIALS.register("magnesite", () -> new Material.Builder("magnesite")
                .dust().ore()
                .color(0xFAFAB4).iconSet(METALLIC.get())
                .components(Magnesium, 1, Carbon, 1, Oxygen, 3)
                .build());

        Magnetite = MATERIALS.register("magnetite", () -> new Material.Builder("magnetite")
                .dust().ore()
                .color(0x1E1E1E).iconSet(METALLIC.get())
                .components(Iron, 3, Oxygen, 4)
                .build());

        Molybdenite = MATERIALS.register("molybdenite", () -> new Material.Builder("molybdenite")
                .dust().ore()
                .color(0x191919).iconSet(METALLIC.get())
                .components(Molybdenum, 1, Sulfur, 2)
                .build());

        Nichrome = MATERIALS.register("nichrome", () -> new Material.Builder("nichrome")
                .ingot().fluid()
                .color(0xCDCEF6).iconSet(METALLIC.get())
                .flags(EXT_METAL, GENERATE_SPRING.get())
                .components(Nickel, 4, Chrome, 1)
                .cableProperties(GTValues.V[4], 4, 4)
                .blastTemp(2700, GasTier.LOW, VA[HV], 1300)
                .fluidTemp(1818)
                .build());

        NiobiumNitride = MATERIALS.register("niobium_nitride", () -> new Material.Builder("niobium_nitride")
                .ingot().fluid()
                .color(0x1D291D)
                .flags(EXT_METAL, GENERATE_FOIL.get())
                .components(Niobium, 1, Nitrogen, 1)
                .cableProperties(GTValues.V[6], 1, 1)
                .blastTemp(2846, GasTier.MID)
                .build());

        NiobiumTitanium = MATERIALS.register("niobium_titanium", () -> new Material.Builder("niobium_titanium")
                .ingot().fluid()
                .color(0x1D1D29)
                .flags(EXT2_METAL, GENERATE_SPRING.get(), GENERATE_SPRING_SMALL.get(), GENERATE_FOIL.get(), GENERATE_FINE_WIRE.get())
                .components(Niobium, 1, Titanium, 1)
                .fluidPipeProperties(5900, 175, true)
                .cableProperties(GTValues.V[6], 4, 2)
                .blastTemp(4500, GasTier.HIGH, VA[HV], 1500)
                .fluidTemp(2345)
                .build());

        Obsidian = MATERIALS.register("obsidian", () -> new Material.Builder("obsidian")
                .dust(3)
                .color(0x503264)
                .flags(NO_SMASHING.get(), EXCLUDE_BLOCK_CRAFTING_RECIPES.get(), GENERATE_PLATE.get())
                .components(Magnesium, 1, Iron, 1, Silicon, 2, Oxygen, 4)
                .build());

        Phosphate = MATERIALS.register("phosphate", () -> new Material.Builder("phosphate")
                .dust(1)
                .color(0xFFFF00)
                .flags(NO_SMASHING.get(), NO_SMELTING.get(), FLAMMABLE.get(), EXPLOSIVE.get())
                .components(Phosphorus, 1, Oxygen, 4)
                .build());

        PlatinumRaw = MATERIALS.register("platinum_raw", () -> new Material.Builder("platinum_raw")
                .dust()
                .color(0xFFFFC8).iconSet(METALLIC.get())
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Platinum, 1, Chlorine, 2)
                .build());

        SterlingSilver = MATERIALS.register("sterling_silver", () -> new Material.Builder("sterling_silver")
                .ingot().fluid()
                .color(0xFADCE1).iconSet(SHINY.get())
                .flags(EXT2_METAL)
                .components(Copper, 1, Silver, 4)
                .toolStats(ToolProperty.Builder.of(3.0F, 8.0F, 768, 2)
                        .attackSpeed(0.3F).enchantability(33)
                        .enchantment(Enchantments.SMITE, 3).build())
                .rotorStats(13.0f, 2.0f, 196)
                .itemPipeProperties(1024, 2)
                .blastTemp(1700, GasTier.LOW, VA[MV], 1000)
                .fluidTemp(1258)
                .build());

        RoseGold = MATERIALS.register("rose_gold", () -> new Material.Builder("rose_gold")
                .ingot().fluid()
                .color(0xFFE61E).iconSet(SHINY.get())
                .flags(EXT2_METAL, GENERATE_RING.get())
                .components(Copper, 1, Gold, 4)
                .toolStats(ToolProperty.Builder.of(12.0F, 2.0F, 768, 2)
                        .enchantability(33)
                        .enchantment(Enchantments.BLOCK_FORTUNE, 2).build())
                .rotorStats(14.0f, 2.0f, 152)
                .itemPipeProperties(1024, 2)
                .blastTemp(1600, GasTier.LOW, VA[MV], 1000)
                .fluidTemp(1341)
                .build());

        BlackBronze = MATERIALS.register("black_bronze", () -> new Material.Builder("black_bronze")
                .ingot().fluid()
                .color(0x64327D)
                .flags(EXT2_METAL, GENERATE_GEAR.get())
                .components(Gold, 1, Silver, 1, Copper, 3)
                .rotorStats(12.0f, 2.0f, 256)
                .itemPipeProperties(1024, 2)
                .blastTemp(2000, GasTier.LOW, VA[MV], 1000)
                .fluidTemp(1328)
                .build());

        BismuthBronze = MATERIALS.register("bismuth_bronze", () -> new Material.Builder("bismuth_bronze")
                .ingot().fluid()
                .color(0x647D7D)
                .flags(EXT2_METAL)
                .components(Bismuth, 1, Zinc, 1, Copper, 3)
                .rotorStats(8.0f, 3.0f, 256)
                .blastTemp(1100, GasTier.LOW, VA[MV], 1000)
                .fluidTemp(1036)
                .build());

        Biotite = MATERIALS.register("biotite", () -> new Material.Builder("biotite")
                .dust(1)
                .color(0x141E14).iconSet(METALLIC.get())
                .components(Potassium, 1, Magnesium, 3, Aluminium, 3, Fluorine, 2, Silicon, 3, Oxygen, 10)
                .build());

        Powellite = MATERIALS.register("powellite", () -> new Material.Builder("powellite")
                .dust().ore()
                .color(0xFFFF00)
                .components(Calcium, 1, Molybdenum, 1, Oxygen, 4)
                .build());

        Pyrite = MATERIALS.register("pyrite", () -> new Material.Builder("pyrite")
                .dust(1).ore()
                .color(0x967828).iconSet(ROUGH.get())
                .flags(BLAST_FURNACE_CALCITE_DOUBLE.get())
                .components(Iron, 1, Sulfur, 2)
                .build());

        Pyrolusite = MATERIALS.register("pyrolusite", () -> new Material.Builder("pyrolusite")
                .dust().ore()
                .color(0x9696AA)
                .components(Manganese, 1, Oxygen, 2)
                .build());

        Pyrope = MATERIALS.register("pyrope", () -> new Material.Builder("pyrope")
                .gem().ore(3, 1)
                .color(0x783264).iconSet(RUBY.get())
                .components(Aluminium, 2, Magnesium, 3, Silicon, 3, Oxygen, 12)
                .build());

        RockSalt = MATERIALS.register("rock_salt", () -> new Material.Builder("rock_salt")
                .gem(1).ore(2, 1)
                .color(0xF0C8C8).iconSet(FINE.get())
                .flags(NO_SMASHING.get())
                .components(Potassium, 1, Chlorine, 1)
                .build());

        Ruridit = MATERIALS.register("ruridit", () -> new Material.Builder("ruridit")
                .ingot(3)
                .colorAverage().iconSet(BRIGHT.get())
                .flags(GENERATE_FINE_WIRE.get(), GENERATE_GEAR.get(), GENERATE_LONG_ROD.get())
                .components(Ruthenium, 2, Iridium, 1)
                .blastTemp(4500, GasTier.HIGH, VA[EV], 1600)
                .build());

        Ruby = MATERIALS.register("ruby", () -> new Material.Builder("ruby")
                .gem().ore()
                .color(0xFF6464).iconSet(RUBY.get())
                .flags(EXT_METAL, NO_SMASHING.get(), NO_SMELTING.get(), HIGH_SIFTER_OUTPUT.get(), GENERATE_LENS.get())
                .components(Chrome, 1, Aluminium, 2, Oxygen, 3)
                .build());

        Salt = MATERIALS.register("salt", () -> new Material.Builder("salt")
                .gem(1).ore(2, 1)
                .color(0xFAFAFA).iconSet(FINE.get())
                .flags(NO_SMASHING.get())
                .components(Sodium, 1, Chlorine, 1)
                .build());

        Saltpeter = MATERIALS.register("saltpeter", () -> new Material.Builder("saltpeter")
                .dust(1).ore(2, 1)
                .color(0xE6E6E6).iconSet(FINE.get())
                .flags(NO_SMASHING.get(), NO_SMELTING.get(), FLAMMABLE.get())
                .components(Potassium, 1, Nitrogen, 1, Oxygen, 3)
                .build());

        Sapphire = MATERIALS.register("sapphire", () -> new Material.Builder("sapphire")
                .gem().ore()
                .color(0x6464C8).iconSet(GEM_VERTICAL.get())
                .flags(EXT_METAL, NO_SMASHING.get(), NO_SMELTING.get(), HIGH_SIFTER_OUTPUT.get(), GENERATE_LENS.get())
                .components(Aluminium, 2, Oxygen, 3)
                .build());

        Scheelite = MATERIALS.register("scheelite", () -> new Material.Builder("scheelite")
                .dust(3).ore()
                .color(0xC88C14)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Calcium, 1, Tungsten, 1, Oxygen, 4)
                .build()
                .setFormula("Ca(WO3)O", true));

        Sodalite = MATERIALS.register("sodalite", () -> new Material.Builder("sodalite")
                .gem(1).ore(6, 4)
                .color(0x1414FF).iconSet(LAPIS.get())
                .flags(GENERATE_PLATE.get(), GENERATE_ROD.get(), NO_SMASHING.get(), NO_SMELTING.get(), CRYSTALLIZABLE.get(), DECOMPOSITION_BY_ELECTROLYZING.get())
                .components(Aluminium, 3, Silicon, 3, Sodium, 4, Chlorine, 1)
                .build());

        AluminiumSulfite = MATERIALS.register("aluminium_sulfite", () -> new Material.Builder("aluminium_sulfite")
                .dust()
                .color(0xCC4BBB).iconSet(DULL.get())
                .components(Aluminium, 2, Sulfur, 3, Oxygen, 9)
                .build()
                .setFormula("Al2(SO3)3", true));

        Tantalite = MATERIALS.register("tantalite", () -> new Material.Builder("tantalite")
                .dust(3).ore()
                .color(0x915028).iconSet(METALLIC.get())
                .components(Manganese, 1, Tantalum, 2, Oxygen, 6)
                .build());

        Coke = MATERIALS.register("coke", () -> new Material.Builder("coke")
                .gem(2, 3200) // 2x burn time of coal
                .color(0x666666).iconSet(LIGNITE.get())
                .flags(FLAMMABLE.get(), NO_SMELTING.get(), NO_SMASHING.get(), MORTAR_GRINDABLE.get())
                .components(Carbon, 1)
                .build());

        SolderingAlloy = MATERIALS.register("soldering_alloy", () -> new Material.Builder("soldering_alloy")
                .ingot(1).fluid()
                .color(0x9696A0)
                .components(Tin, 6, Lead, 3, Antimony, 1)
                .fluidTemp(544)
                .build());

        Spessartine = MATERIALS.register("spessartine", () -> new Material.Builder("spessartine")
                .gem().ore(3, 1)
                .color(0xFF6464).iconSet(RUBY.get())
                .components(Aluminium, 2, Manganese, 3, Silicon, 3, Oxygen, 12)
                .build());

        Sphalerite = MATERIALS.register("sphalerite", () -> new Material.Builder("sphalerite")
                .dust(1).ore()
                .color(0xFFFFFF)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Zinc, 1, Sulfur, 1)
                .build());

        StainlessSteel = MATERIALS.register("stainless_steel", () -> new Material.Builder("stainless_steel")
                .ingot(3).fluid()
                .color(0xC8C8DC).iconSet(SHINY.get())
                .flags(EXT2_METAL, GENERATE_ROTOR.get(), GENERATE_SMALL_GEAR.get(), GENERATE_FRAME.get(), GENERATE_LONG_ROD.get(), GENERATE_FOIL.get(), GENERATE_GEAR.get())
                .components(Iron, 6, Chrome, 1, Manganese, 1, Nickel, 1)
                .toolStats(ToolProperty.Builder.of(7.0F, 5.0F, 1024, 3)
                        .enchantability(14).build())
                .rotorStats(7.0f, 4.0f, 480)
                .fluidPipeProperties(2428, 75, true, true, true, false)
                .blastTemp(1700, GasTier.LOW, VA[HV], 1100)
                .fluidTemp(2011)
                .build());

        Steel = MATERIALS.register("steel", () -> new Material.Builder("steel")
                .ingot(3).fluid()
                .color(0x808080).iconSet(METALLIC.get())
                .flags(EXT2_METAL, MORTAR_GRINDABLE.get(), GENERATE_ROTOR.get(), GENERATE_SMALL_GEAR.get(), GENERATE_SPRING.get(),
                        GENERATE_SPRING_SMALL.get(), GENERATE_FRAME.get(), DISABLE_DECOMPOSITION.get(), GENERATE_FINE_WIRE.get(), GENERATE_GEAR.get())
                .components(Iron, 1)
                .toolStats(ToolProperty.Builder.of(5.0F, 3.0F, 512, 3)
                        .enchantability(14).build())
                .rotorStats(6.0f, 3.0f, 512)
                .fluidPipeProperties(1855, 75, true)
                .cableProperties(GTValues.V[4], 2, 2)
                .blastTemp(1000, null, VA[MV], 800) // no gas tier for steel
                .fluidTemp(2046)
                .build());

        Stibnite = MATERIALS.register("stibnite", () -> new Material.Builder("stibnite")
                .dust().ore()
                .color(0x464646).iconSet(METALLIC.get())
                .flags(DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(Antimony, 2, Sulfur, 3)
                .build());

        // Free ID 326

        Tetrahedrite = MATERIALS.register("tetrahedrite", () -> new Material.Builder("tetrahedrite")
                .dust().ore()
                .color(0xC82000)
                .components(Copper, 3, Antimony, 1, Sulfur, 3, Iron, 1)
                .build());

        TinAlloy = MATERIALS.register("tin_alloy", () -> new Material.Builder("tin_alloy")
                .ingot().fluid()
                .color(0xC8C8C8).iconSet(METALLIC.get())
                .flags(EXT2_METAL)
                .components(Tin, 1, Iron, 1)
                .fluidPipeProperties(1572, 20, true)
                .fluidTemp(1258)
                .build());

        Topaz = MATERIALS.register("topaz", () -> new Material.Builder("topaz")
                .gem(3).ore()
                .color(0xFF8000).iconSet(GEM_HORIZONTAL.get())
                .flags(EXT_METAL, NO_SMASHING.get(), NO_SMELTING.get(), HIGH_SIFTER_OUTPUT.get())
                .components(Aluminium, 2, Silicon, 1, Fluorine, 1, Hydrogen, 2)
                .build());

        Tungstate = MATERIALS.register("tungstate", () -> new Material.Builder("tungstate")
                .dust(3).ore()
                .color(0x373223)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Tungsten, 1, Lithium, 2, Oxygen, 4)
                .build()
                .setFormula("Li2(WO3)O", true));

        Ultimet = MATERIALS.register("ultimet", () -> new Material.Builder("ultimet")
                .ingot(4).fluid()
                .color(0xB4B4E6).iconSet(SHINY.get())
                .flags(EXT2_METAL, GENERATE_GEAR.get())
                .components(Cobalt, 5, Chrome, 2, Nickel, 1, Molybdenum, 1)
                .toolStats(ToolProperty.Builder.of(10.0F, 7.0F, 2048, 4)
                        .attackSpeed(0.1F).enchantability(21).build())
                .rotorStats(9.0f, 4.0f, 2048)
                .itemPipeProperties(128, 16)
                .blastTemp(2700, GasTier.MID, VA[HV], 1300)
                .fluidTemp(1980)
                .build());

        Uraninite = MATERIALS.register("uraninite", () -> new Material.Builder("uraninite")
                .dust(3).ore(true)
                .color(0x232323).iconSet(METALLIC.get())
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Uranium238, 1, Oxygen, 2)
                .build()
                .setFormula("UO2", true));

        Uvarovite = MATERIALS.register("uvarovite", () -> new Material.Builder("uvarovite")
                .gem()
                .color(0xB4ffB4).iconSet(RUBY.get())
                .components(Calcium, 3, Chrome, 2, Silicon, 3, Oxygen, 12)
                .build());

        VanadiumGallium = MATERIALS.register("vanadium_gallium", () -> new Material.Builder("vanadium_gallium")
                .ingot().fluid()
                .color(0x80808C).iconSet(SHINY.get())
                .flags(STD_METAL, GENERATE_FOIL.get(), GENERATE_SPRING.get(), GENERATE_SPRING_SMALL.get())
                .components(Vanadium, 3, Gallium, 1)
                .cableProperties(GTValues.V[7], 4, 2)
                .blastTemp(4500, GasTier.HIGH, VA[EV], 1200)
                .fluidTemp(1712)
                .build());

        WroughtIron = MATERIALS.register("wrought_iron", () -> new Material.Builder("wrought_iron")
                .ingot().fluid()
                .color(0xC8B4B4).iconSet(METALLIC.get())
                .flags(EXT_METAL, GENERATE_GEAR.get(), GENERATE_FOIL.get(), MORTAR_GRINDABLE.get(), GENERATE_RING.get(), GENERATE_LONG_ROD.get(), GENERATE_BOLT_SCREW.get(), DISABLE_DECOMPOSITION.get(), BLAST_FURNACE_CALCITE_TRIPLE.get())
                .components(Iron, 1)
                .toolStats(ToolProperty.Builder.of(2.0F, 2.0F, 384, 2)
                        .attackSpeed(-0.2F).enchantability(5).build())
                .rotorStats(6.0f, 3.5f, 384)
                .fluidTemp(2011)
                .build());
        Iron.get().getProperty(GtMaterialProperties.INGOT.get()).setSmeltingInto(WroughtIron.get());
        Iron.get().getProperty(GtMaterialProperties.INGOT.get()).setArcSmeltingInto(WroughtIron.get());

        Wulfenite = MATERIALS.register("wulfenite", () -> new Material.Builder("wulfenite")
                .dust(3).ore()
                .color(0xFF8000)
                .components(Lead, 1, Molybdenum, 1, Oxygen, 4)
                .build());

        YellowLimonite = MATERIALS.register("yellow_limonite", () -> new Material.Builder("yellow_limonite")
                .dust().ore()
                .color(0xC8C800).iconSet(METALLIC.get())
                .flags(DECOMPOSITION_BY_CENTRIFUGING.get(), BLAST_FURNACE_CALCITE_DOUBLE.get())
                .components(Iron, 1, Hydrogen, 1, Oxygen, 2)
                .build());

        YttriumBariumCuprate = MATERIALS.register("yttrium_barium_cuprate", () -> new Material.Builder("yttrium_barium_cuprate")
                .ingot().fluid()
                .color(0x504046).iconSet(METALLIC.get())
                .flags(EXT_METAL, GENERATE_FINE_WIRE.get(), GENERATE_SPRING.get(), GENERATE_SPRING_SMALL.get(), GENERATE_FOIL.get(), GENERATE_BOLT_SCREW.get())
                .components(Yttrium, 1, Barium, 2, Copper, 3, Oxygen, 7)
                .cableProperties(GTValues.V[8], 4, 4)
                .blastTemp(4500, GasTier.HIGH) // todo redo this EBF process
                .fluidTemp(1799)
                .build());

        NetherQuartz = MATERIALS.register("nether_quartz", () -> new Material.Builder("nether_quartz")
                .gem(1).ore(2, 1)
                .color(0xE6D2D2).iconSet(QUARTZ.get())
                .flags(GENERATE_PLATE.get(), NO_SMELTING.get(), CRYSTALLIZABLE.get(), EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES.get(), DISABLE_DECOMPOSITION.get())
                .components(Silicon, 1, Oxygen, 2)
                .build());

        CertusQuartz = MATERIALS.register("certus_quartz", () -> new Material.Builder("certus_quartz")
                .gem(1).ore(2, 1)
                .color(0xD2D2E6).iconSet(CERTUS.get())
                .flags(GENERATE_PLATE.get(), NO_SMELTING.get(), CRYSTALLIZABLE.get(), DISABLE_DECOMPOSITION.get())
                .components(Silicon, 1, Oxygen, 2)
                .build());

        Quartzite = MATERIALS.register("quartzite", () -> new Material.Builder("quartzite")
                .gem(1).ore(2, 1)
                .color(0xD2E6D2).iconSet(QUARTZ.get())
                .flags(NO_SMELTING.get(), CRYSTALLIZABLE.get(), DISABLE_DECOMPOSITION.get(), GENERATE_PLATE.get())
                .components(Silicon, 1, Oxygen, 2)
                .build());

        Graphite = MATERIALS.register("graphite", () -> new Material.Builder("graphite")
                .ore()
                .color(0x808080)
                .flags(NO_SMELTING.get(), FLAMMABLE.get(), DISABLE_DECOMPOSITION.get())
                .components(Carbon, 1)
                .build());

        Graphene = MATERIALS.register("graphene", () -> new Material.Builder("graphene")
                .dust()
                .color(0x808080).iconSet(SHINY.get())
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 1)
                .cableProperties(GTValues.V[5], 1, 1)
                .build());

        TungsticAcid = MATERIALS.register("tungstic_acid", () -> new Material.Builder("tungstic_acid")
                .dust()
                .color(0xBCC800).iconSet(SHINY.get())
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Hydrogen, 2, Tungsten, 1, Oxygen, 4)
                .build());

        Osmiridium = MATERIALS.register("osmiridium", () -> new Material.Builder("osmiridium")
                .ingot(3).fluid()
                .color(0x6464FF).iconSet(METALLIC.get())
                .flags(EXT2_METAL, GENERATE_SMALL_GEAR.get(), GENERATE_RING.get(), GENERATE_ROTOR.get(), GENERATE_ROUND.get(), GENERATE_FINE_WIRE.get(), GENERATE_GEAR.get())
                .components(Iridium, 3, Osmium, 1)
                .rotorStats(9.0f, 3.0f, 3152)
                .itemPipeProperties(64, 32)
                .blastTemp(4500, GasTier.HIGH, VA[LuV], 900)
                .fluidTemp(3012)
                .build());

        LithiumChloride = MATERIALS.register("lithium_chloride", () -> new Material.Builder("lithium_chloride")
                .dust()
                .color(0xDEDEFA).iconSet(FINE.get())
                .components(Lithium, 1, Chlorine, 1)
                .build());

        CalciumChloride = MATERIALS.register("calcium_chloride", () -> new Material.Builder("calcium_chloride")
                .dust()
                .color(0xEBEBFA).iconSet(FINE.get())
                .components(Calcium, 1, Chlorine, 2)
                .build());

        Bornite = MATERIALS.register("bornite", () -> new Material.Builder("bornite")
                .dust(1).ore()
                .color(0x97662B).iconSet(METALLIC.get())
                .components(Copper, 5, Iron, 1, Sulfur, 4)
                .build());

        Chalcocite = MATERIALS.register("chalcocite", () -> new Material.Builder("chalcocite")
                .dust().ore()
                .color(0x353535).iconSet(GEM_VERTICAL.get())
                .components(Copper, 2, Sulfur, 1)
                .build());

        // Free ID 349

        // Free ID 350

        GalliumArsenide = MATERIALS.register("gallium_arsenide", () -> new Material.Builder("gallium_arsenide")
                .ingot(1).fluid()
                .color(0xA0A0A0)
                .flags(STD_METAL, DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(Arsenic, 1, Gallium, 1)
                .blastTemp(1200, GasTier.LOW, VA[MV], 1200)
                .fluidTemp(1511)
                .build());

        Potash = MATERIALS.register("potash", () -> new Material.Builder("potash")
                .dust(1)
                .color(0x784137)
                .components(Potassium, 2, Oxygen, 1)
                .build());

        SodaAsh = MATERIALS.register("soda_ash", () -> new Material.Builder("soda_ash")
                .dust(1)
                .color(0xDCDCFF)
                .components(Sodium, 2, Carbon, 1, Oxygen, 3)
                .build());

        IndiumGalliumPhosphide = MATERIALS.register("indium_gallium_phosphide", () -> new Material.Builder("indium_gallium_phosphide")
                .ingot(1).fluid()
                .color(0xA08CBE)
                .flags(STD_METAL, DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(Indium, 1, Gallium, 1, Phosphorus, 1)
                .fluidTemp(350)
                .build());

        NickelZincFerrite = MATERIALS.register("nickel_zinc_ferrite", () -> new Material.Builder("nickel_zinc_ferrite")
                .ingot(0).fluid()
                .color(0x3C3C3C).iconSet(METALLIC.get())
                .flags(GENERATE_RING.get())
                .components(Nickel, 1, Zinc, 1, Iron, 4, Oxygen, 8)
                .fluidTemp(1410)
                .build());

        SiliconDioxide = MATERIALS.register("silicon_dioxide", () -> new Material.Builder("silicon_dioxide")
                .dust(1)
                .color(0xC8C8C8).iconSet(QUARTZ.get())
                .flags(NO_SMASHING.get(), NO_SMELTING.get())
                .components(Silicon, 1, Oxygen, 2)
                .build());

        MagnesiumChloride = MATERIALS.register("magnesium_chloride", () -> new Material.Builder("magnesium_chloride")
                .dust(1)
                .color(0xD40D5C)
                .components(Magnesium, 1, Chlorine, 2)
                .build());

        SodiumSulfide = MATERIALS.register("sodium_sulfide", () -> new Material.Builder("sodium_sulfide")
                .dust(1)
                .color(0xFFE680)
                .components(Sodium, 2, Sulfur, 1)
                .build());

        PhosphorusPentoxide = MATERIALS.register("phosphorus_pentoxide", () -> new Material.Builder("phosphorus_pentoxide")
                .dust(1)
                .color(0xDCDC00)
                .flags(DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(Phosphorus, 4, Oxygen, 10)
                .build());

        Quicklime = MATERIALS.register("quicklime", () -> new Material.Builder("quicklime")
                .dust(1)
                .color(0xF0F0F0)
                .components(Calcium, 1, Oxygen, 1)
                .build());

        SodiumBisulfate = MATERIALS.register("sodium_bisulfate", () -> new Material.Builder("sodium_bisulfate")
                .dust(1)
                .color(0x004455)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Sodium, 1, Hydrogen, 1, Sulfur, 1, Oxygen, 4)
                .build());

        FerriteMixture = MATERIALS.register("ferrite_mixture", () -> new Material.Builder("ferrite_mixture")
                .dust(1)
                .color(0xB4B4B4).iconSet(METALLIC.get())
                .flags(DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(Nickel, 1, Zinc, 1, Iron, 4)
                .build());

        Magnesia = MATERIALS.register("magnesia", () -> new Material.Builder("magnesia")
                .dust(1)
                .color(0x887878)
                .components(Magnesium, 1, Oxygen, 1)
                .build());

        PlatinumGroupSludge = MATERIALS.register("platinum_group_sludge", () -> new Material.Builder("platinum_group_sludge")
                .dust(1)
                .color(0x001E00).iconSet(FINE.get())
                .flags(DISABLE_DECOMPOSITION.get())
                .build());

        Realgar = MATERIALS.register("realgar", () -> new Material.Builder("realgar")
                .gem().ore()
                .color(0x9D2123).iconSet(EMERALD.get())
                .flags(DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(Arsenic, 4, Sulfur, 4)
                .build());

        SodiumBicarbonate = MATERIALS.register("sodium_bicarbonate", () -> new Material.Builder("sodium_bicarbonate")
                .dust(1)
                .color(0x565b96).iconSet(ROUGH.get())
                .components(Sodium, 1, Hydrogen, 1, Carbon, 1, Oxygen, 3)
                .build());

        PotassiumDichromate = MATERIALS.register("potassium_dichromate", () -> new Material.Builder("potassium_dichromate")
                .dust(1)
                .color(0xFF084E)
                .components(Potassium, 2, Chrome, 2, Oxygen, 7)
                .build());

        ChromiumTrioxide = MATERIALS.register("chromium_trioxide", () -> new Material.Builder("chromium_trioxide")
                .dust(1)
                .color(0xFFE4E1)
                .components(Chrome, 1, Oxygen, 3)
                .build());

        AntimonyTrioxide = MATERIALS.register("antimony_trioxide", () -> new Material.Builder("antimony_trioxide")
                .dust(1)
                .color(0xE6E6F0)
                .components(Antimony, 2, Oxygen, 3)
                .build());

        Zincite = MATERIALS.register("zincite", () -> new Material.Builder("zincite")
                .dust(1)
                .color(0xFFFFF5)
                .components(Zinc, 1, Oxygen, 1)
                .build());

        CupricOxide = MATERIALS.register("cupric_oxide", () -> new Material.Builder("cupric_oxide")
                .dust(1)
                .color(0x0F0F0F)
                .components(Copper, 1, Oxygen, 1)
                .build());

        CobaltOxide = MATERIALS.register("cobalt_oxide", () -> new Material.Builder("cobalt_oxide")
                .dust(1)
                .color(0x788000)
                .components(Cobalt, 1, Oxygen, 1)
                .build());

        ArsenicTrioxide = MATERIALS.register("arsenic_trioxide", () -> new Material.Builder("arsenic_trioxide")
                .dust(1)
                .iconSet(ROUGH.get())
                .components(Arsenic, 2, Oxygen, 3)
                .build());

        Massicot = MATERIALS.register("massicot", () -> new Material.Builder("massicot")
                .dust(1)
                .color(0xFFDD55)
                .components(Lead, 1, Oxygen, 1)
                .build());

        Ferrosilite = MATERIALS.register("ferrosilite", () -> new Material.Builder("ferrosilite")
                .dust(1)
                .color(0x97632A)
                .components(Iron, 1, Silicon, 1, Oxygen, 3)
                .build());

        MetalMixture = MATERIALS.register("metal_mixture", () -> new Material.Builder("metal_mixture")
                .dust(1)
                .color(0x502d16).iconSet(METALLIC.get())
                .flags(DISABLE_DECOMPOSITION.get())
                .build());

        SodiumHydroxide = MATERIALS.register("sodium_hydroxide", () -> new Material.Builder("sodium_hydroxide")
                .dust(1)
                .color(0x003380)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Sodium, 1, Oxygen, 1, Hydrogen, 1)
                .build());

        SodiumPersulfate = MATERIALS.register("sodium_persulfate", () -> new Material.Builder("sodium_persulfate")
                .fluid()
                .components(Sodium, 2, Sulfur, 2, Oxygen, 8)
                .build());

        Bastnasite = MATERIALS.register("bastnasite", () -> new Material.Builder("bastnasite")
                .dust().ore(2, 1)
                .color(0xC86E2D).iconSet(FINE.get())
                .components(Cerium, 1, Carbon, 1, Fluorine, 1, Oxygen, 3)
                .build());

        Pentlandite = MATERIALS.register("pentlandite", () -> new Material.Builder("pentlandite")
                .dust().ore()
                .color(0xA59605)
                .components(Nickel, 9, Sulfur, 8)
                .build());

        Spodumene = MATERIALS.register("spodumene", () -> new Material.Builder("spodumene")
                .dust().ore()
                .color(0xBEAAAA)
                .components(Lithium, 1, Aluminium, 1, Silicon, 2, Oxygen, 6)
                .build());

        Lepidolite = MATERIALS.register("lepidolite", () -> new Material.Builder("lepidolite")
                .dust().ore(2, 1)
                .color(0xF0328C).iconSet(FINE.get())
                .components(Potassium, 1, Lithium, 3, Aluminium, 4, Fluorine, 2, Oxygen, 10)
                .build());

        // Free ID 383

        GlauconiteSand = MATERIALS.register("glauconite_sand", () -> new Material.Builder("glauconite_sand")
                .dust().ore(3, 1)
                .color(0x82B43C).iconSet(SAND.get())
                .components(Potassium, 1, Magnesium, 2, Aluminium, 4, Hydrogen, 2, Oxygen, 12)
                .build());

        Malachite = MATERIALS.register("malachite", () -> new Material.Builder("malachite")
                .gem().ore()
                .color(0x055F05).iconSet(LAPIS.get())
                .components(Copper, 2, Carbon, 1, Hydrogen, 2, Oxygen, 5)
                .build());

        Mica = MATERIALS.register("mica", () -> new Material.Builder("mica")
                .dust().ore(2, 1)
                .color(0xC3C3CD).iconSet(FINE.get())
                .components(Potassium, 1, Aluminium, 3, Silicon, 3, Fluorine, 2, Oxygen, 10)
                .build());

        Barite = MATERIALS.register("barite", () -> new Material.Builder("barite")
                .dust().ore()
                .color(0xE6EBEB)
                .components(Barium, 1, Sulfur, 1, Oxygen, 4)
                .build());

        Alunite = MATERIALS.register("alunite", () -> new Material.Builder("alunite")
                .dust().ore(3, 1)
                .color(0xE1B441).iconSet(METALLIC.get())
                .components(Potassium, 1, Aluminium, 3, Silicon, 2, Hydrogen, 6, Oxygen, 14)
                .build());

        // Free ID 389

        // Free ID 390

        // Free ID 391

        Talc = MATERIALS.register("talc", () -> new Material.Builder("talc")
                .dust().ore(2, 1)
                .color(0x5AB45A).iconSet(FINE.get())
                .components(Magnesium, 3, Silicon, 4, Hydrogen, 2, Oxygen, 12)
                .build());

        Soapstone = MATERIALS.register("soapstone", () -> new Material.Builder("soapstone")
                .dust(1).ore(3, 1)
                .color(0x5F915F)
                .components(Magnesium, 3, Silicon, 4, Hydrogen, 2, Oxygen, 12)
                .build());

        Kyanite = MATERIALS.register("kyanite", () -> new Material.Builder("kyanite")
                .dust().ore()
                .color(0x6E6EFA).iconSet(FLINT.get())
                .components(Aluminium, 2, Silicon, 1, Oxygen, 5)
                .build());

        IronMagnetic = MATERIALS.register("iron_magnetic", () -> new Material.Builder("iron_magnetic")
                .ingot()
                .color(0xC8C8C8).iconSet(MAGNETIC.get())
                .flags(GENERATE_BOLT_SCREW.get(), IS_MAGNETIC.get())
                .components(Iron, 1)
                .ingotSmeltInto(Iron.get())
                .arcSmeltInto(WroughtIron.get())
                .macerateInto(Iron.get())
                .build());
        Iron.get().getProperty(GtMaterialProperties.INGOT.get()).setMagneticMaterial(IronMagnetic.get());

        TungstenCarbide = MATERIALS.register("tungsten_carbide", () -> new Material.Builder("tungsten_carbide")
                .ingot(4).fluid()
                .color(0x330066).iconSet(METALLIC.get())
                .flags(EXT2_METAL, GENERATE_FOIL.get(), GENERATE_GEAR.get(), DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(Tungsten, 1, Carbon, 1)
                .toolStats(ToolProperty.Builder.of(60.0F, 2.0F, 1024, 4)
                        .enchantability(21).build())
                .rotorStats(12.0f, 4.0f, 1280)
                .fluidPipeProperties(3837, 200, true)
                .blastTemp(3058, GasTier.MID, VA[HV], 1500)
                .build());

        CarbonDioxide = MATERIALS.register("carbon_dioxide", () -> new Material.Builder("carbon_dioxide")
                .fluid(GtFluidTypes.GAS.get())
                .color(0xA9D0F5)
                .components(Carbon, 1, Oxygen, 2)
                .build());

        TitaniumTetrachloride = MATERIALS.register("titanium_tetrachloride", () -> new Material.Builder("titanium_tetrachloride")
                .fluid()
                .color(0xD40D5C)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Titanium, 1, Chlorine, 4)
                .build());

        NitrogenDioxide = MATERIALS.register("nitrogen_dioxide", () -> new Material.Builder("nitrogen_dioxide")
                .fluid(GtFluidTypes.GAS.get())
                .color(0x85FCFF).iconSet(GAS.get())
                .components(Nitrogen, 1, Oxygen, 2)
                .build());

        HydrogenSulfide = MATERIALS.register("hydrogen_sulfide", () -> new Material.Builder("hydrogen_sulfide")
                .fluid(GtFluidTypes.GAS.get())
                .components(Hydrogen, 2, Sulfur, 1)
                .build());

        NitricAcid = MATERIALS.register("nitric_acid", () -> new Material.Builder("nitric_acid")
                .fluid(GtFluidTypes.ACID.get())
                .color(0xCCCC00)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Hydrogen, 1, Nitrogen, 1, Oxygen, 3)
                .build());

        SulfuricAcid = MATERIALS.register("sulfuric_acid", () -> new Material.Builder("sulfuric_acid")
                .fluid(GtFluidTypes.ACID.get())
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Hydrogen, 2, Sulfur, 1, Oxygen, 4)
                .build());

        PhosphoricAcid = MATERIALS.register("phosphoric_acid", () -> new Material.Builder("phosphoric_acid")
                .fluid(GtFluidTypes.ACID.get())
                .color(0xDCDC01)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Hydrogen, 3, Phosphorus, 1, Oxygen, 4)
                .build());

        SulfurTrioxide = MATERIALS.register("sulfur_trioxide", () -> new Material.Builder("sulfur_trioxide")
                .fluid(GtFluidTypes.GAS.get())
                .color(0xA0A014)
                .components(Sulfur, 1, Oxygen, 3)
                .build());

        SulfurDioxide = MATERIALS.register("sulfur_dioxide", () -> new Material.Builder("sulfur_dioxide")
                .fluid(GtFluidTypes.GAS.get())
                .color(0xC8C819)
                .components(Sulfur, 1, Oxygen, 2)
                .build());

        CarbonMonoxide = MATERIALS.register("carbon_monoxide", () -> new Material.Builder("carbon_monoxide")
                .fluid(GtFluidTypes.GAS.get())
                .color(0x0E4880)
                .components(Carbon, 1, Oxygen, 1)
                .build());

        HypochlorousAcid = MATERIALS.register("hypochlorous_acid", () -> new Material.Builder("hypochlorous_acid")
                .fluid(GtFluidTypes.ACID.get())
                .color(0x6F8A91)
                .components(Hydrogen, 1, Chlorine, 1, Oxygen, 1)
                .build());

        Ammonia = MATERIALS.register("ammonia", () -> new Material.Builder("ammonia")
                .fluid(GtFluidTypes.GAS.get())
                .color(0x3F3480)
                .components(Nitrogen, 1, Hydrogen, 3)
                .build());

        HydrofluoricAcid = MATERIALS.register("hydrofluoric_acid", () -> new Material.Builder("hydrofluoric_acid")
                .fluid(GtFluidTypes.ACID.get())
                .color(0x0088AA)
                .components(Hydrogen, 1, Fluorine, 1)
                .build());

        NitricOxide = MATERIALS.register("nitric_oxide", () -> new Material.Builder("nitric_oxide")
                .fluid(GtFluidTypes.GAS.get())
                .color(0x7DC8F0)
                .components(Nitrogen, 1, Oxygen, 1)
                .build());

        Iron3Chloride = MATERIALS.register("iron_iii_chloride", () -> new Material.Builder("iron_iii_chloride")
                .fluid()
                .color(0x060B0B)
                .flags(DECOMPOSITION_BY_ELECTROLYZING.get())
                .components(Iron, 1, Chlorine, 3)
                .build());

        UraniumHexafluoride = MATERIALS.register("uranium_hexafluoride", () -> new Material.Builder("uranium_hexafluoride")
                .fluid(GtFluidTypes.GAS.get())
                .color(0x42D126)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Uranium238, 1, Fluorine, 6)
                .build()
                .setFormula("UF6", true));

        EnrichedUraniumHexafluoride = MATERIALS.register("enriched_uranium_hexafluoride", () -> new Material.Builder("enriched_uranium_hexafluoride")
                .fluid(GtFluidTypes.GAS.get())
                .color(0x4BF52A)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Uranium235, 1, Fluorine, 6)
                .build()
                .setFormula("UF6", true));

        DepletedUraniumHexafluoride = MATERIALS.register("depleted_uranium_hexafluoride", () -> new Material.Builder("depleted_uranium_hexafluoride")
                .fluid(GtFluidTypes.GAS.get())
                .color(0x74BA66)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Uranium238, 1, Fluorine, 6)
                .build()
                .setFormula("UF6", true));

        NitrousOxide = MATERIALS.register("nitrous_oxide", () -> new Material.Builder("nitrous_oxide")
                .fluid(GtFluidTypes.GAS.get())
                .color(0x7DC8FF)
                .components(Nitrogen, 2, Oxygen, 1)
                .build());

        EnderPearl = MATERIALS.register("ender_pearl", () -> new Material.Builder("ender_pearl")
                .gem(1)
                .color(0x6CDCC8)
                .flags(NO_SMASHING.get(), NO_SMELTING.get(), GENERATE_PLATE.get())
                .components(Beryllium, 1, Potassium, 4, Nitrogen, 5)
                .build());

        PotassiumFeldspar = MATERIALS.register("potassium_feldspar", () -> new Material.Builder("potassium_feldspar")
                .dust(1)
                .color(0x782828).iconSet(FINE.get())
                .components(Potassium, 1, Aluminium, 1, Silicon, 1, Oxygen, 8)
                .build());

        NeodymiumMagnetic = MATERIALS.register("neodymium_magnetic", () -> new Material.Builder("neodymium_magnetic")
                .ingot()
                .color(0x646464).iconSet(MAGNETIC.get())
                .flags(GENERATE_ROD.get(), IS_MAGNETIC.get())
                .components(Neodymium.get(), 1)
                .ingotSmeltInto(Neodymium.get())
                .arcSmeltInto(Neodymium.get())
                .macerateInto(Neodymium.get())
                .build());
        Neodymium.get().getProperty(GtMaterialProperties.INGOT.get()).setMagneticMaterial(NeodymiumMagnetic.get());

        HydrochloricAcid = MATERIALS.register("hydrochloric_acid", () -> new Material.Builder("hydrochloric_acid")
                .fluid(GtFluidTypes.ACID.get())
                .components(Hydrogen, 1, Chlorine, 1)
                .build());

        Steam = MATERIALS.register("steam", () -> new Material.Builder("steam")
                .fluid(GtFluidTypes.GAS.get(), true)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Hydrogen, 2, Oxygen, 1)
                .fluidTemp(373)
                .build());

        DistilledWater = MATERIALS.register("distilled_water", () -> new Material.Builder("distilled_water")
                .fluid()
                .color(0x4A94FF)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Hydrogen, 2, Oxygen, 1)
                .build());

        SodiumPotassium = MATERIALS.register("sodium_potassium", () -> new Material.Builder("sodium_potassium")
                .fluid()
                .color(0x64FCB4)
                .flags(DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(Sodium, 1, Potassium, 1)
                .build());

        SamariumMagnetic = MATERIALS.register("samarium_magnetic", () -> new Material.Builder("samarium_magnetic")
                .ingot()
                .color(0xFFFFCD).iconSet(MAGNETIC.get())
                .flags(GENERATE_LONG_ROD.get(), IS_MAGNETIC.get())
                .components(Samarium.get(), 1)
                .ingotSmeltInto(Samarium.get())
                .arcSmeltInto(Samarium.get())
                .macerateInto(Samarium.get())
                .build());
        Samarium.get().getProperty(GtMaterialProperties.INGOT.get()).setMagneticMaterial(SamariumMagnetic.get());

        ManganesePhosphide = MATERIALS.register("manganese_phosphide", () -> new Material.Builder("manganese_phosphide")
                .ingot().fluid()
                .color(0xE1B454).iconSet(METALLIC.get())
                .flags(DECOMPOSITION_BY_ELECTROLYZING.get())
                .components(Manganese, 1, Phosphorus, 1)
                .cableProperties(GTValues.V[GTValues.LV], 2, 0, true, 78)
                .blastTemp(1200, GasTier.LOW)
                .fluidTemp(1368)
                .build());

        MagnesiumDiboride = MATERIALS.register("magnesium_diboride", () -> new Material.Builder("magnesium_diboride")
                .ingot().fluid()
                .color(0x331900).iconSet(METALLIC.get())
                .flags(DECOMPOSITION_BY_ELECTROLYZING.get())
                .components(Magnesium, 1, Boron, 2)
                .cableProperties(GTValues.V[GTValues.MV], 4, 0, true, 78)
                .blastTemp(2500, GasTier.LOW, VA[HV], 1000)
                .fluidTemp(1103)
                .build());

        MercuryBariumCalciumCuprate = MATERIALS.register("mercury_barium_calcium_cuprate", () -> new Material.Builder("mercury_barium_calcium_cuprate")
                .ingot().fluid()
                .color(0x555555).iconSet(SHINY.get())
                .flags(DECOMPOSITION_BY_ELECTROLYZING.get())
                .components(Mercury, 1, Barium, 2, Calcium, 2, Copper, 3, Oxygen, 8)
                .cableProperties(GTValues.V[GTValues.HV], 4, 0, true, 78)
                .blastTemp(3300, GasTier.LOW, VA[HV], 1500)
                .fluidTemp(1075)
                .build());

        UraniumTriplatinum = MATERIALS.register("uranium_triplatinum", () -> new Material.Builder("uranium_triplatinum")
                .ingot().fluid()
                .color(0x008700).iconSet(SHINY.get())
                .flags(DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(Uranium238, 1, Platinum, 3)
                .cableProperties(GTValues.V[GTValues.EV], 6, 0, true, 30)
                .blastTemp(4400, GasTier.MID, VA[EV], 1000)
                .fluidTemp(1882)
                .build()
                .setFormula("UPt3", true));

        SamariumIronArsenicOxide = MATERIALS.register("samarium_iron_arsenic_oxide", () -> new Material.Builder("samarium_iron_arsenic_oxide")
                .ingot().fluid()
                .color(0x330033).iconSet(SHINY.get())
                .flags(DECOMPOSITION_BY_CENTRIFUGING.get())
                .components(Samarium, 1, Iron, 1, Arsenic, 1, Oxygen, 1)
                .cableProperties(GTValues.V[GTValues.IV], 6, 0, true, 30)
                .blastTemp(5200, GasTier.MID, VA[EV], 1500)
                .fluidTemp(1347)
                .build());

        IndiumTinBariumTitaniumCuprate = MATERIALS.register("indium_tin_barium_titanium_cuprate", () -> new Material.Builder("indium_tin_barium_titanium_cuprate")
                .ingot().fluid()
                .color(0x994C00).iconSet(METALLIC.get())
                .flags(DECOMPOSITION_BY_ELECTROLYZING.get(), GENERATE_FINE_WIRE.get())
                .components(Indium, 4, Tin, 2, Barium, 2, Titanium, 1, Copper, 7, Oxygen, 14)
                .cableProperties(GTValues.V[GTValues.LuV], 8, 0, true, 5)
                .blastTemp(6000, GasTier.HIGH, VA[IV], 1000)
                .fluidTemp(1012)
                .build());

        UraniumRhodiumDinaquadide = MATERIALS.register("uranium_rhodium_dinaquadide", () -> new Material.Builder("uranium_rhodium_dinaquadide")
                .ingot().fluid()
                .color(0x0A0A0A)
                .flags(DECOMPOSITION_BY_CENTRIFUGING.get(), GENERATE_FINE_WIRE.get())
                .components(Uranium238, 1, Rhodium, 1, Naquadah, 2)
                .cableProperties(GTValues.V[GTValues.ZPM], 8, 0, true, 5)
                .blastTemp(9000, GasTier.HIGH, VA[IV], 1500)
                .fluidTemp(3410)
                .build()
                .setFormula("URhNq2", true));

        EnrichedNaquadahTriniumEuropiumDuranide = MATERIALS.register("enriched_naquadah_trinium_europium_duranide", () -> new Material.Builder("enriched_naquadah_trinium_europium_duranide")
                .ingot().fluid()
                .color(0x7D9673).iconSet(METALLIC.get())
                .flags(DECOMPOSITION_BY_CENTRIFUGING.get(), GENERATE_FINE_WIRE.get())
                .components(NaquadahEnriched, 4, Trinium, 3, Europium, 2, Duranium, 1)
                .cableProperties(GTValues.V[GTValues.UV], 16, 0, true, 3)
                .blastTemp(9900, GasTier.HIGH, VA[LuV], 1000)
                .fluidTemp(5930)
                .build());

        RutheniumTriniumAmericiumNeutronate = MATERIALS.register("ruthenium_trinium_americium_neutronate", () -> new Material.Builder("ruthenium_trinium_americium_neutronate")
                .ingot().fluid()
                .color(0xFFFFFF).iconSet(BRIGHT.get())
                .flags(DECOMPOSITION_BY_ELECTROLYZING.get())
                .components(Ruthenium, 1, Trinium, 2, Americium, 1, Neutronium, 2, Oxygen, 8)
                .cableProperties(GTValues.V[GTValues.UHV], 24, 0, true, 3)
                .blastTemp(10800, GasTier.HIGHER)
                .fluidTemp(23691)
                .build());

        InertMetalMixture = MATERIALS.register("inert_metal_mixture", () -> new Material.Builder("inert_metal_mixture")
                .dust()
                .color(0xE2AE72).iconSet(METALLIC.get())
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Rhodium, 1, Ruthenium, 1, Oxygen, 4)
                .build());

        RhodiumSulfate = MATERIALS.register("rhodium_sulfate", () -> new Material.Builder("rhodium_sulfate")
                .fluid()
                .color(0xEEAA55)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Rhodium, 2, Sulfur, 3, Oxygen, 12)
                .fluidTemp(1128)
                .build().setFormula("Rh2(SO4)3", true));

        RutheniumTetroxide = MATERIALS.register("ruthenium_tetroxide", () -> new Material.Builder("ruthenium_tetroxide")
                .dust()
                .color(0xC7C7C7)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Ruthenium, 1, Oxygen, 4)
                .build());

        OsmiumTetroxide = MATERIALS.register("osmium_tetroxide", () -> new Material.Builder("osmium_tetroxide")
                .dust()
                .color(0xACAD71).iconSet(METALLIC.get())
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Osmium, 1, Oxygen, 4)
                .build());

        IridiumChloride = MATERIALS.register("iridium_chloride", () -> new Material.Builder("iridium_chloride")
                .dust()
                .color(0x013220).iconSet(METALLIC.get())
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Iridium, 1, Chlorine, 3)
                .build());

        FluoroantimonicAcid = MATERIALS.register("fluoroantimonic_acid", () -> new Material.Builder("fluoroantimonic_acid")
                .fluid(GtFluidTypes.ACID.get())
                .components(Hydrogen, 2, Antimony, 1, Fluorine, 7)
                .build());

        TitaniumTrifluoride = MATERIALS.register("titanium_trifluoride", () -> new Material.Builder("titanium_trifluoride")
                .dust()
                .color(0x8F00FF).iconSet(SHINY.get())
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Titanium, 1, Fluorine, 3)
                .build());

        CalciumPhosphide = MATERIALS.register("calcium_phosphide", () -> new Material.Builder("calcium_phosphide")
                .dust()
                .color(0xA52A2A).iconSet(METALLIC.get())
                .components(Calcium, 1, Phosphorus, 1)
                .build());

        IndiumPhosphide = MATERIALS.register("indium_phosphide", () -> new Material.Builder("indium_phosphide")
                .dust()
                .color(0x582E5C).iconSet(SHINY.get())
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Indium, 1, Phosphorus, 1)
                .build());

        BariumSulfide = MATERIALS.register("barium_sulfide", () -> new Material.Builder("barium_sulfide")
                .dust()
                .color(0xF0EAD6).iconSet(METALLIC.get())
                .components(Barium, 1, Sulfur, 1)
                .build());

        TriniumSulfide = MATERIALS.register("trinium_sulfide", () -> new Material.Builder("trinium_sulfide")
                .dust()
                .color(0xE68066).iconSet(SHINY.get())
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Trinium, 1, Sulfur, 1)
                .build());

        ZincSulfide = MATERIALS.register("zinc_sulfide", () -> new Material.Builder("zinc_sulfide")
                .color(0xFFFFF6).iconSet(DULL.get())
                .components(Zinc, 1, Sulfur, 1)
                .build());

        GalliumSulfide = MATERIALS.register("gallium_sulfide", () -> new Material.Builder("gallium_sulfide")
                .dust()
                .color(0xFFF59E).iconSet(SHINY.get())
                .components(Gallium, 1, Sulfur, 1)
                .build());

        AntimonyTrifluoride = MATERIALS.register("antimony_trifluoride", () -> new Material.Builder("antimony_trifluoride")
                .dust()
                .color(0xF7EABC).iconSet(METALLIC.get())
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Antimony, 1, Fluorine, 3)
                .build());

        EnrichedNaquadahSulfate = MATERIALS.register("enriched_naquadah_sulfate", () -> new Material.Builder("enriched_naquadah_sulfate")
                .dust()
                .color(0x2E2E1C).iconSet(METALLIC.get())
                .flags(DISABLE_DECOMPOSITION.get())
                .components(NaquadahEnriched, 1, Sulfur, 1, Oxygen, 4)
                .build());

        NaquadriaSulfate = MATERIALS.register("naquadria_sulfate", () -> new Material.Builder("naquadria_sulfate")
                .dust()
                .color(0x006633).iconSet(SHINY.get())
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Naquadria, 1, Sulfur, 1, Oxygen, 4)
                .build());

        Pyrochlore = MATERIALS.register("pyrochlore", () -> new Material.Builder("pyrochlore")
                .dust().ore()
                .color(0x2B1100).iconSet(METALLIC.get())
                .flags()
                .components(Calcium, 2, Niobium, 2, Oxygen, 7)
                .build());

        LiquidHelium = MATERIALS.register("liquid_helium", () -> new Material.Builder("liquid_helium")
                .fluid()
                .color(0xFCFF90)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Helium, 1)
                .fluidTemp(4)
                .build());
    }
}