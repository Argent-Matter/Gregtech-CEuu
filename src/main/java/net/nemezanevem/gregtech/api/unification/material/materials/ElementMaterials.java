package net.nemezanevem.gregtech.api.unification.material.materials;


import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.fluids.type.GTFluidTypes;
import net.nemezanevem.gregtech.api.unification.material.GtElements;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.BlastProperty.GasTier;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.ToolProperty;

import static net.nemezanevem.gregtech.api.GTValues.*;
import static net.nemezanevem.gregtech.api.registry.material.MaterialRegistry.MATERIALS;
import static net.nemezanevem.gregtech.api.unification.material.GtMaterials.*;
import static net.nemezanevem.gregtech.api.unification.material.properties.info.GtMaterialFlags.*;
import static net.nemezanevem.gregtech.api.unification.material.properties.info.GtMaterialIconSets.*;

public class ElementMaterials {

    public static void register() {
        Actinium = MATERIALS.register("actinium", () -> new Material.Builder("actinium")
                .color(0xC3D1FF).iconSet(METALLIC.get())
                .element(GtElements.Ac)
                .build());

        Aluminium = MATERIALS.register("aluminium", () -> new Material.Builder("aluminium")
                .ingot().fluid().ore()
                .color(0x80C8F0)
                .flags(EXT2_METAL, GENERATE_GEAR.get(), GENERATE_SMALL_GEAR.get(), GENERATE_RING.get(), GENERATE_FRAME.get(), GENERATE_SPRING.get(), GENERATE_SPRING_SMALL.get(), GENERATE_FINE_WIRE.get())
                .element(GtElements.Al)
                .toolStats(ToolProperty.Builder.of(6.0F, 7.5F, 768, 2)
                        .enchantability(14).build())
                .rotorStats(10.0f, 2.0f, 128)
                .cableProperties(GTValues.V[4], 1, 1)
                .fluidPipeProperties(1166, 100, true)
                .blastTemp(1700, GasTier.LOW)
                .fluidTemp(933)
                .build());

        Americium = MATERIALS.register("americium", () -> new Material.Builder("americium")
                .ingot(3).fluid()
                .color(0x287869).iconSet(METALLIC.get())
                .flags(EXT_METAL, GENERATE_FOIL.get(), GENERATE_FINE_WIRE.get())
                .element(GtElements.Am)
                .itemPipeProperties(64, 64)
                .fluidTemp(1449)
                .build());

        Antimony = MATERIALS.register("antimony", () -> new Material.Builder("antimony")
                .ingot().fluid()
                .color(0xDCDCF0).iconSet(SHINY.get())
                .flags(MORTAR_GRINDABLE.get())
                .element(GtElements.Sb)
                .fluidTemp(904)
                .build());

        Argon = MATERIALS.register("argon", () -> new Material.Builder("argon")
                .fluid(GTFluidTypes.GAS).plasma()
                .color(0x00FF00).iconSet(GAS.get())
                .element(GtElements.Ar)
                .build());

        Arsenic = MATERIALS.register("arsenic", () -> new Material.Builder("arsenic")
                .dust().fluid()
                .color(0x676756)
                .element(GtElements.As)
                .fluidTemp(887)
                .build());

        Astatine = MATERIALS.register("astatine", () -> new Material.Builder("astatine")
                .color(0x241A24)
                .element(GtElements.At)
                .build());

        Barium = MATERIALS.register("barium", () -> new Material.Builder("barium")
                .dust()
                .color(0x83824C).iconSet(METALLIC.get())
                .element(GtElements.Ba)
                .build());

        Berkelium = MATERIALS.register("berkelium", () -> new Material.Builder("berkelium")
                .color(0x645A88).iconSet(METALLIC.get())
                .element(GtElements.Bk)
                .build());

        Beryllium = MATERIALS.register("beryllium", () -> new Material.Builder("beryllium")
                .ingot().fluid().ore()
                .color(0x64B464).iconSet(METALLIC.get())
                .flags(STD_METAL)
                .element(GtElements.Be)
                .fluidTemp(1560)
                .build());

        Bismuth = MATERIALS.register("bismuth", () -> new Material.Builder("bismuth")
                .ingot(1).fluid()
                .color(0x64A0A0).iconSet(METALLIC.get())
                .element(GtElements.Bi)
                .fluidTemp(545)
                .build());

        Bohrium = MATERIALS.register("bohrium", () -> new Material.Builder("bohrium")
                .color(0xDC57FF).iconSet(SHINY.get())
                .element(GtElements.Bh)
                .build());

        Boron = MATERIALS.register("boron", () -> new Material.Builder("boron")
                .dust()
                .color(0xD2FAD2)
                .element(GtElements.B)
                .build());

        Bromine = MATERIALS.register("bromine", () -> new Material.Builder("bromine")
                .color(0x500A0A).iconSet(SHINY.get())
                .element(GtElements.Br)
                .build());

        Caesium = MATERIALS.register("caesium", () -> new Material.Builder("caesium")
                .dust()
                .color(0x80620B).iconSet(METALLIC.get())
                .element(GtElements.Cs)
                .build());

        Calcium = MATERIALS.register("calcium", () -> new Material.Builder("calcium")
                .dust()
                .color(0xFFF5DE).iconSet(METALLIC.get())
                .element(GtElements.Ca)
                .build());

        Californium = MATERIALS.register("californium", () -> new Material.Builder("californium")
                .color(0xA85A12).iconSet(METALLIC.get())
                .element(GtElements.Cf)
                .build());

        Carbon = MATERIALS.register("carbon", () -> new Material.Builder("carbon")
                .dust().fluid()
                .color(0x141414)
                .element(GtElements.C)
                .fluidTemp(4600)
                .build());

        Cadmium = MATERIALS.register("cadmium", () -> new Material.Builder("cadmium")
                .dust()
                .color(0x32323C).iconSet(SHINY.get())
                .element(GtElements.Cd)
                .build());

        Cerium = MATERIALS.register("cerium", () -> new Material.Builder("cerium")
                .dust().fluid()
                .color(0x87917D).iconSet(METALLIC.get())
                .element(GtElements.Ce)
                .fluidTemp(1068)
                .build());

        Chlorine = MATERIALS.register("chlorine", () -> new Material.Builder("chlorine")
                .fluid(GTFluidTypes.GAS)
                .element(GtElements.Cl)
                .build());

        Chrome = MATERIALS.register("chrome", () -> new Material.Builder("chrome")
                .ingot(3).fluid()
                .color(0xEAC4D8).iconSet(SHINY.get())
                .flags(EXT_METAL, GENERATE_ROTOR.get())
                .element(GtElements.Cr)
                .rotorStats(12.0f, 3.0f, 512)
                .fluidPipeProperties(2180, 35, true, true, false, false)
                .blastTemp(1700, GasTier.LOW)
                .fluidTemp(2180)
                .build());

        Cobalt = MATERIALS.register("cobalt", () -> new Material.Builder("cobalt")
                .ingot().fluid().ore() // leave for TiCon ore processing
                .color(0x5050FA).iconSet(METALLIC.get())
                .flags(EXT_METAL)
                .element(GtElements.Co)
                .cableProperties(GTValues.V[1], 2, 2)
                .itemPipeProperties(2560, 2.0f)
                .fluidTemp(1768)
                .build());

        Copernicium = MATERIALS.register("copernicium", () -> new Material.Builder("copernicium")
                .color(0xFFFEFF)
                .element(GtElements.Cn)
                .build());

        Copper = MATERIALS.register("copper", () -> new Material.Builder("copper")
                .ingot(1).fluid().ore()
                .color(0xFF6400).iconSet(SHINY.get())
                .flags(EXT_METAL, MORTAR_GRINDABLE.get(), GENERATE_SPRING.get(), GENERATE_SPRING_SMALL.get(), GENERATE_FINE_WIRE.get())
                .element(GtElements.Cu)
                .cableProperties(GTValues.V[2], 1, 2)
                .fluidPipeProperties(1696, 6, true)
                .fluidTemp(1358)
                .build());

        Curium = MATERIALS.register("curium", () -> new Material.Builder("curium")
                .color(0x7B544E).iconSet(METALLIC.get())
                .element(GtElements.Cm)
                .build());

        Darmstadtium = MATERIALS.register("darmstadtium", () -> new Material.Builder("darmstadtium")
                .ingot().fluid()
                .color(0x578062)
                .flags(EXT2_METAL, GENERATE_ROTOR.get(), GENERATE_DENSE.get(), GENERATE_SMALL_GEAR.get())
                .element(GtElements.Ds)
                .build());

        Deuterium = MATERIALS.register("deuterium", () -> new Material.Builder("deuterium")
                .fluid(GTFluidTypes.GAS)
                .element(GtElements.D)
                .build());

        Dubnium = MATERIALS.register("dubnium", () -> new Material.Builder("dubnium")
                .color(0xD3FDFF).iconSet(SHINY.get())
                .element(GtElements.Db)
                .build());

        Dysprosium = MATERIALS.register("dysprosium", () -> new Material.Builder("dysprosium")
                .iconSet(METALLIC.get())
                .element(GtElements.Dy)
                .build());

        Einsteinium = MATERIALS.register("einsteinium", () -> new Material.Builder("einsteinium")
                .color(0xCE9F00).iconSet(METALLIC.get())
                .element(GtElements.Es)
                .build());

        Erbium = MATERIALS.register("erbium", () -> new Material.Builder("erbium")
                .iconSet(METALLIC.get())
                .element(GtElements.Er)
                .build());

        Europium = MATERIALS.register("europium", () -> new Material.Builder("europium")
                .ingot().fluid()
                .color(0x20FFFF).iconSet(METALLIC.get())
                .flags(STD_METAL, GENERATE_LONG_ROD.get(), GENERATE_FINE_WIRE.get(), GENERATE_SPRING.get(), GENERATE_FOIL.get(), GENERATE_FRAME.get())
                .element(GtElements.Eu)
                .cableProperties(GTValues.V[GTValues.UHV], 2, 32)
                .fluidPipeProperties(7750, 300, true)
                .blastTemp(6000, GasTier.MID, VA[IV], 180)
                .fluidTemp(1099)
                .build());

        Fermium = MATERIALS.register("fermium", () -> new Material.Builder("fermium")
                .color(0x984ACF).iconSet(METALLIC.get())
                .element(GtElements.Fm)
                .build());

        Flerovium = MATERIALS.register("flerovium", () -> new Material.Builder("flerovium")
                .iconSet(SHINY.get())
                .element(GtElements.Fl)
                .build());

        Fluorine = MATERIALS.register("fluorine", () -> new Material.Builder("fluorine")
                .fluid(GTFluidTypes.GAS)
                .element(GtElements.F)
                .build());

        Francium = MATERIALS.register("francium", () -> new Material.Builder("francium")
                .color(0xAAAAAA).iconSet(SHINY.get())
                .element(GtElements.Fr)
                .build());

        Gadolinium = MATERIALS.register("gadolinium", () -> new Material.Builder("gadolinium")
                .color(0xDDDDFF).iconSet(METALLIC.get())
                .element(GtElements.Gd)
                .build());

        Gallium = MATERIALS.register("gallium", () -> new Material.Builder("gallium")
                .ingot().fluid()
                .color(0xDCDCFF).iconSet(SHINY.get())
                .flags(STD_METAL, GENERATE_FOIL.get())
                .element(GtElements.Ga)
                .fluidTemp(303)
                .build());

        Germanium = MATERIALS.register("germanium", () -> new Material.Builder("germanium")
                .color(0x434343).iconSet(SHINY.get())
                .element(GtElements.Ge)
                .build());

        Gold = MATERIALS.register("gold", () -> new Material.Builder("gold")
                .ingot().fluid().ore()
                .color(0xFFE650).iconSet(SHINY.get())
                .flags(EXT2_METAL, GENERATE_RING.get(), MORTAR_GRINDABLE.get(), EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES.get(), GENERATE_SPRING.get(), GENERATE_SPRING_SMALL.get(), GENERATE_FINE_WIRE.get(), GENERATE_FOIL.get())
                .element(GtElements.Au)
                .cableProperties(GTValues.V[3], 3, 2)
                .fluidPipeProperties(1671, 25, true, true, false, false)
                .fluidTemp(1337)
                .build());

        Hafnium = MATERIALS.register("hafnium", () -> new Material.Builder("hafnium")
                .color(0x99999A).iconSet(SHINY.get())
                .element(GtElements.Hf)
                .build());

        Hassium = MATERIALS.register("hassium", () -> new Material.Builder("hassium")
                .color(0xDDDDDD)
                .element(GtElements.Hs)
                .build());

        Holmium = MATERIALS.register("holmium", () -> new Material.Builder("holmium")
                .iconSet(METALLIC.get())
                .element(GtElements.Ho)
                .build());

        Hydrogen = MATERIALS.register("hydrogen", () -> new Material.Builder("hydrogen")
                .fluid(GTFluidTypes.GAS)
                .color(0x0000B5)
                .element(GtElements.H)
                .build());

        Helium = MATERIALS.register("helium", () -> new Material.Builder("helium")
                .fluid(GTFluidTypes.GAS).plasma()
                .element(GtElements.He)
                .build());

        Helium3 = MATERIALS.register("helium_3", () -> new Material.Builder("helium_3")
                .fluid(GTFluidTypes.GAS)
                .element(GtElements.He3)
                .build());

        Indium = MATERIALS.register("indium", () -> new Material.Builder("indium")
                .ingot().fluid()
                .color(0x400080).iconSet(SHINY.get())
                .element(GtElements.In)
                .fluidTemp(430)
                .build());

        Iodine = MATERIALS.register("iodine", () -> new Material.Builder("iodine")
                .color(0x2C344F).iconSet(SHINY.get())
                .element(GtElements.I)
                .build());

        Iridium = MATERIALS.register("iridium", () -> new Material.Builder("iridium")
                .ingot(3).fluid()
                .color(0xA1E4E4).iconSet(METALLIC.get())
                .flags(EXT2_METAL, GENERATE_FINE_WIRE.get(), GENERATE_GEAR.get())
                .element(GtElements.Ir)
                .rotorStats(7.0f, 3.0f, 2560)
                .fluidPipeProperties(3398, 250, true, false, true, false)
                .blastTemp(4500, GasTier.HIGH, VA[IV], 1100)
                .fluidTemp(2719)
                .build());

        Iron = MATERIALS.register("iron", () -> new Material.Builder("iron")
                .ingot().fluid().plasma().ore()
                .color(0xC8C8C8).iconSet(METALLIC.get())
                .flags(EXT2_METAL, MORTAR_GRINDABLE.get(), GENERATE_ROTOR.get(), GENERATE_SMALL_GEAR.get(), GENERATE_GEAR.get(), GENERATE_SPRING_SMALL.get(), GENERATE_SPRING.get(), EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES.get(), BLAST_FURNACE_CALCITE_TRIPLE.get())
                .element(GtElements.Fe)
                .toolStats(ToolProperty.Builder.of(2.0F, 2.0F, 256, 2)
                        .enchantability(14).build())
                .rotorStats(7.0f, 2.5f, 256)
                .cableProperties(GTValues.V[2], 2, 3)
                .fluidTemp(1811)
                .build());

        Krypton = MATERIALS.register("krypton", () -> new Material.Builder("krypton")
                .fluid(GTFluidTypes.GAS)
                .color(0x80FF80).iconSet(GAS.get())
                .element(GtElements.Kr)
                .build());

        Lanthanum = MATERIALS.register("lanthanum", () -> new Material.Builder("lanthanum")
                .dust().fluid()
                .color(0x5D7575).iconSet(METALLIC.get())
                .element(GtElements.La)
                .fluidTemp(1193)
                .build());

        Lawrencium = MATERIALS.register("lawrencium", () -> new Material.Builder("lawrencium")
                .iconSet(METALLIC.get())
                .element(GtElements.Lr)
                .build());

        Lead = MATERIALS.register("lead", () -> new Material.Builder("lead")
                .ingot(1).fluid().ore()
                .color(0x8C648C)
                .flags(EXT2_METAL, MORTAR_GRINDABLE.get(), GENERATE_ROTOR.get(), GENERATE_SPRING.get(), GENERATE_SPRING_SMALL.get(), GENERATE_FINE_WIRE.get())
                .element(GtElements.Pb)
                .cableProperties(GTValues.V[0], 2, 2)
                .fluidPipeProperties(1200, 8, true)
                .fluidTemp(600)
                .build());

        Lithium = MATERIALS.register("lithium", () -> new Material.Builder("lithium")
                .dust().fluid().ore()
                .color(0xBDC7DB)
                .element(GtElements.Li)
                .fluidTemp(454)
                .build());

        Livermorium = MATERIALS.register("livermorium", () -> new Material.Builder("livermorium")
                .color(0xAAAAAA).iconSet(SHINY.get())
                .element(GtElements.Lv)
                .build());

        Lutetium = MATERIALS.register("lutetium", () -> new Material.Builder("lutetium")
                .dust().fluid()
                .color(0x00AAFF).iconSet(METALLIC.get())
                .element(GtElements.Lu)
                .fluidTemp(1925)
                .build());

        Magnesium = MATERIALS.register("magnesium", () -> new Material.Builder("magnesium")
                .dust().fluid()
                .color(0xFFC8C8).iconSet(METALLIC.get())
                .element(GtElements.Mg)
                .fluidTemp(923)
                .build());

        Mendelevium = MATERIALS.register("mendelevium", () -> new Material.Builder("mendelevium")
                .color(0x1D4ACF).iconSet(METALLIC.get())
                .element(GtElements.Md)
                .build());

        Manganese = MATERIALS.register("manganese", () -> new Material.Builder("manganese")
                .ingot().fluid()
                .color(0xCDE1B9)
                .flags(STD_METAL, GENERATE_FOIL.get(), GENERATE_BOLT_SCREW.get())
                .element(GtElements.Mn)
                .rotorStats(7.0f, 2.0f, 512)
                .fluidTemp(1519)
                .build());

        Meitnerium = MATERIALS.register("meitnerium", () -> new Material.Builder("meitnerium")
                .color(0x2246BE).iconSet(SHINY.get())
                .element(GtElements.Mt)
                .build());

        Mercury = MATERIALS.register("mercury", () -> new Material.Builder("mercury")
                .fluid()
                .color(0xE6DCDC).iconSet(DULL.get())
                .element(GtElements.Hg)
                .build());

        Molybdenum = MATERIALS.register("molybdenum", () -> new Material.Builder("molybdenum")
                .ingot().fluid().ore()
                .color(0xB4B4DC).iconSet(SHINY.get())
                .element(GtElements.Mo)
                .flags(GENERATE_FOIL.get(), GENERATE_BOLT_SCREW.get())
                .rotorStats(7.0f, 2.0f, 512)
                .fluidTemp(2896)
                .build());

        Moscovium = MATERIALS.register("moscovium", () -> new Material.Builder("moscovium")
                .color(0x7854AD).iconSet(SHINY.get())
                .element(GtElements.Mc)
                .build());

        Neodymium = MATERIALS.register("neodymium", () -> new Material.Builder("neodymium")
                .ingot().fluid().ore()
                .color(0x646464).iconSet(METALLIC.get())
                .flags(STD_METAL, GENERATE_ROD.get(), GENERATE_BOLT_SCREW.get())
                .element(GtElements.Nd)
                .rotorStats(7.0f, 2.0f, 512)
                .blastTemp(1297, GasTier.MID)
                .build());

        Neon = MATERIALS.register("neon", () -> new Material.Builder("neon")
                .fluid(GTFluidTypes.GAS)
                .color(0xFAB4B4).iconSet(GAS.get())
                .element(GtElements.Ne)
                .build());

        Neptunium = MATERIALS.register("neptunium", () -> new Material.Builder("neptunium")
                .color(0x284D7B).iconSet(METALLIC.get())
                .element(GtElements.Np)
                .build());

        Nickel = MATERIALS.register("nickel", () -> new Material.Builder("nickel")
                .ingot().fluid().plasma().ore()
                .color(0xC8C8FA).iconSet(METALLIC.get())
                .flags(STD_METAL, MORTAR_GRINDABLE.get())
                .element(GtElements.Ni)
                .cableProperties(GTValues.V[GTValues.LV], 3, 3)
                .itemPipeProperties(2048, 1.0f)
                .fluidTemp(1728)
                .build());

        Nihonium = MATERIALS.register("nihonium", () -> new Material.Builder("nihonium")
                .color(0x08269E).iconSet(SHINY.get())
                .element(GtElements.Nh)
                .build());

        Niobium = MATERIALS.register("niobium", () -> new Material.Builder("niobium")
                .ingot().fluid()
                .color(0xBEB4C8).iconSet(METALLIC.get())
                .element(GtElements.Nb)
                .blastTemp(2750, GasTier.MID, VA[HV], 900)
                .build());

        Nitrogen = MATERIALS.register("nitrogen", () -> new Material.Builder("nitrogen")
                .fluid(GTFluidTypes.GAS).plasma()
                .color(0x00BFC1).iconSet(GAS.get())
                .element(GtElements.N)
                .build());

        Nobelium = MATERIALS.register("nobelium", () -> new Material.Builder("nobelium")
                .iconSet(SHINY.get())
                .element(GtElements.No)
                .build());

        Oganesson = MATERIALS.register("oganesson", () -> new Material.Builder("oganesson")
                .color(0x142D64).iconSet(METALLIC.get())
                .element(GtElements.Og)
                .build());

        Osmium = MATERIALS.register("osmium", () -> new Material.Builder("osmium")
                .ingot(4).fluid()
                .color(0x3232FF).iconSet(METALLIC.get())
                .flags(EXT2_METAL, GENERATE_FOIL.get())
                .element(GtElements.Os)
                .rotorStats(16.0f, 4.0f, 1280)
                .cableProperties(GTValues.V[6], 4, 2)
                .itemPipeProperties(256, 8.0f)
                .blastTemp(4500, GasTier.HIGH, VA[LuV], 1000)
                .fluidTemp(3306)
                .build());

        Oxygen = MATERIALS.register("oxygen", () -> new Material.Builder("oxygen")
                .fluid(GTFluidTypes.GAS).plasma()
                .color(0x4CC3FF)
                .element(GtElements.O)
                .build());

        Palladium = MATERIALS.register("palladium", () -> new Material.Builder("palladium")
                .ingot().fluid().ore()
                .color(0x808080).iconSet(SHINY.get())
                .flags(EXT_METAL, GENERATE_FOIL.get(), GENERATE_FINE_WIRE.get())
                .element(GtElements.Pd)
                .blastTemp(1828, GasTier.LOW, VA[HV], 900)
                .build());

        Phosphorus = MATERIALS.register("phosphorus", () -> new Material.Builder("phosphorus")
                .dust()
                .color(0xFFFF00)
                .element(GtElements.P)
                .build());

        Polonium = MATERIALS.register("polonium", () -> new Material.Builder("polonium")
                .color(0xC9D47E)
                .element(GtElements.Po)
                .build());

        Platinum = MATERIALS.register("platinum", () -> new Material.Builder("platinum")
                .ingot().fluid().ore()
                .color(0xFFFFC8).iconSet(SHINY.get())
                .flags(EXT2_METAL, GENERATE_FOIL.get(), GENERATE_FINE_WIRE.get(), GENERATE_RING.get())
                .element(GtElements.Pt)
                .cableProperties(GTValues.V[5], 2, 1)
                .itemPipeProperties(512, 4.0f)
                .fluidTemp(2041)
                .build());

        Plutonium239 = MATERIALS.register("plutonium", () -> new Material.Builder("plutonium")
                .ingot(3).fluid().ore(true)
                .color(0xF03232).iconSet(METALLIC.get())
                .element(GtElements.Pu239)
                .fluidTemp(913)
                .build());

        Plutonium241 = MATERIALS.register("plutonium_241", () -> new Material.Builder("plutonium_241")
                .ingot(3).fluid()
                .color(0xFA4646).iconSet(SHINY.get())
                .flags(EXT_METAL)
                .element(GtElements.Pu241)
                .fluidTemp(913)
                .build());

        Potassium = MATERIALS.register("potassium", () -> new Material.Builder("potassium")
                .dust(1).fluid()
                .color(0xBEDCFF).iconSet(METALLIC.get())
                .element(GtElements.K)
                .fluidTemp(337)
                .build());

        Praseodymium = MATERIALS.register("praseodymium", () -> new Material.Builder("praseodymium")
                .color(0xCECECE).iconSet(METALLIC.get())
                .element(GtElements.Pr)
                .build());

        Promethium = MATERIALS.register("promethium", () -> new Material.Builder("promethium")
                .iconSet(METALLIC.get())
                .element(GtElements.Pm)
                .build());

        Protactinium = MATERIALS.register("protactinium", () -> new Material.Builder("protactinium")
                .color(0xA78B6D).iconSet(METALLIC.get())
                .element(GtElements.Pa)
                .build());

        Radon = MATERIALS.register("radon", () -> new Material.Builder("radon")
                .fluid(GTFluidTypes.GAS)
                .color(0xFF39FF)
                .element(GtElements.Rn)
                .build());

        Radium = MATERIALS.register("radium", () -> new Material.Builder("radium")
                .color(0xFFFFCD).iconSet(SHINY.get())
                .element(GtElements.Ra)
                .build());

        Rhenium = MATERIALS.register("rhenium", () -> new Material.Builder("rhenium")
                .color(0xB6BAC3).iconSet(SHINY.get())
                .element(GtElements.Re)
                .build());

        Rhodium = MATERIALS.register("rhodium", () -> new Material.Builder("rhodium")
                .ingot().fluid()
                .color(0xDC0C58).iconSet(BRIGHT.get())
                .flags(EXT2_METAL, GENERATE_GEAR.get(), GENERATE_FINE_WIRE.get())
                .element(GtElements.Rh)
                .blastTemp(2237, GasTier.MID, VA[EV], 1200)
                .build());

        Roentgenium = MATERIALS.register("roentgenium", () -> new Material.Builder("roentgenium")
                .color(0xE3FDEC).iconSet(SHINY.get())
                .element(GtElements.Rg)
                .build());

        Rubidium = MATERIALS.register("rubidium", () -> new Material.Builder("rubidium")
                .color(0xF01E1E).iconSet(SHINY.get())
                .element(GtElements.Rb)
                .build());

        Ruthenium = MATERIALS.register("ruthenium", () -> new Material.Builder("ruthenium")
                .ingot().fluid()
                .color(0x50ACCD).iconSet(SHINY.get())
                .flags(GENERATE_FOIL.get(), GENERATE_GEAR.get())
                .element(GtElements.Ru)
                .blastTemp(2607, GasTier.MID, VA[EV], 900)
                .build());

        Rutherfordium = MATERIALS.register("rutherfordium", () -> new Material.Builder("rutherfordium")
                .color(0xFFF6A1).iconSet(SHINY.get())
                .element(GtElements.Rf)
                .build());

        Samarium = MATERIALS.register("samarium", () -> new Material.Builder("samarium")
                .ingot().fluid()
                .color(0xFFFFCC).iconSet(METALLIC.get())
                .flags(GENERATE_LONG_ROD.get())
                .element(GtElements.Sm)
                .blastTemp(5400, GasTier.HIGH, VA[EV], 1500)
                .fluidTemp(1345)
                .build());

        Scandium = MATERIALS.register("scandium", () -> new Material.Builder("scandium")
                .iconSet(METALLIC.get())
                .element(GtElements.Sc)
                .build());

        Seaborgium = MATERIALS.register("seaborgium", () -> new Material.Builder("seaborgium")
                .color(0x19C5FF).iconSet(SHINY.get())
                .element(GtElements.Sg)
                .build());

        Selenium = MATERIALS.register("selenium", () -> new Material.Builder("selenium")
                .color(0xB6BA6B).iconSet(SHINY.get())
                .element(GtElements.Se)
                .build());

        Silicon = MATERIALS.register("silicon", () -> new Material.Builder("silicon")
                .ingot().fluid()
                .color(0x3C3C50).iconSet(METALLIC.get())
                .flags(GENERATE_FOIL.get())
                .element(GtElements.Si)
                .blastTemp(1687) // no gas tier for silicon
                .build());

        Silver = MATERIALS.register("silver", () -> new Material.Builder("silver")
                .ingot().fluid().ore()
                .color(0xDCDCFF).iconSet(SHINY.get())
                .flags(EXT2_METAL, MORTAR_GRINDABLE.get(), GENERATE_FINE_WIRE.get(), GENERATE_RING.get())
                .element(GtElements.Ag)
                .cableProperties(GTValues.V[3], 1, 1)
                .fluidTemp(1235)
                .build());

        Sodium = MATERIALS.register("sodium", () -> new Material.Builder("sodium")
                .dust()
                .color(0x000096).iconSet(METALLIC.get())
                .element(GtElements.Na)
                .build());

        Strontium = MATERIALS.register("strontium", () -> new Material.Builder("strontium")
                .color(0xC8C8C8).iconSet(METALLIC.get())
                .element(GtElements.Sr)
                .build());

        Sulfur = MATERIALS.register("sulfur", () -> new Material.Builder("sulfur")
                .dust().ore()
                .color(0xC8C800)
                .flags(FLAMMABLE.get())
                .element(GtElements.S)
                .build());

        Tantalum = MATERIALS.register("tantalum", () -> new Material.Builder("tantalum")
                .ingot().fluid()
                .color(0x78788c).iconSet(METALLIC.get())
                .flags(STD_METAL, GENERATE_FOIL.get())
                .element(GtElements.Ta)
                .fluidTemp(3290)
                .build());

        Technetium = MATERIALS.register("technetium", () -> new Material.Builder("technetium")
                .color(0x545455).iconSet(SHINY.get())
                .element(GtElements.Tc)
                .build());

        Tellurium = MATERIALS.register("tellurium", () -> new Material.Builder("tellurium")
                .iconSet(METALLIC.get())
                .element(GtElements.Te)
                .build());

        Tennessine = MATERIALS.register("tennessine", () -> new Material.Builder("tennessine")
                .color(0x977FD6).iconSet(SHINY.get())
                .element(GtElements.Ts)
                .build());

        Terbium = MATERIALS.register("terbium", () -> new Material.Builder("terbium")
                .iconSet(METALLIC.get())
                .element(GtElements.Tb)
                .build());

        Thorium = MATERIALS.register("thorium", () -> new Material.Builder("thorium")
                .ingot().fluid().ore()
                .color(0x001E00).iconSet(SHINY.get())
                .flags(STD_METAL, GENERATE_ROD.get())
                .element(GtElements.Th)
                .fluidTemp(2023)
                .build());

        Thallium = MATERIALS.register("thallium", () -> new Material.Builder("thallium")
                .color(0xC1C1DE).iconSet(SHINY.get())
                .element(GtElements.Tl)
                .build());

        Thulium = MATERIALS.register("thulium", () -> new Material.Builder("thulium")
                .iconSet(METALLIC.get())
                .element(GtElements.Tm)
                .build());

        Tin = MATERIALS.register("tin", () -> new Material.Builder("tin")
                .ingot(1).fluid(GTFluidTypes.LIQUID, true).ore()
                .color(0xDCDCDC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE.get(), GENERATE_ROTOR.get(), GENERATE_SPRING.get(), GENERATE_SPRING_SMALL.get(), GENERATE_FINE_WIRE.get())
                .element(GtElements.Sn)
                .cableProperties(GTValues.V[1], 1, 1)
                .itemPipeProperties(4096, 0.5f)
                .fluidTemp(505)
                .build());

        Titanium = MATERIALS.register("titanium", () -> new Material.Builder("titanium") // todo Ore? Look at EBF recipe here if we do Ti ores
                .ingot(3).fluid()
                .color(0xDCA0F0).iconSet(METALLIC.get())
                .flags(EXT2_METAL, GENERATE_ROTOR.get(), GENERATE_SMALL_GEAR.get(), GENERATE_GEAR.get(), GENERATE_FRAME.get())
                .element(GtElements.Ti)
                .toolStats(ToolProperty.Builder.of(8.0F, 6.0F, 1536, 3)
                        .enchantability(14).build())
                .rotorStats(7.0f, 3.0f, 1600)
                .fluidPipeProperties(2426, 150, true)
                .blastTemp(1941, GasTier.MID, VA[HV], 1500)
                .build());

        Tritium = MATERIALS.register("tritium", () -> new Material.Builder("tritium")
                .fluid(GTFluidTypes.GAS)
                .iconSet(METALLIC.get())
                .element(GtElements.T)
                .build());

        Tungsten = MATERIALS.register("tungsten", () -> new Material.Builder("tungsten")
                .ingot(3).fluid()
                .color(0x323232).iconSet(METALLIC.get())
                .flags(EXT2_METAL, GENERATE_SPRING.get(), GENERATE_SPRING_SMALL.get(), GENERATE_FOIL.get(), GENERATE_GEAR.get())
                .element(GtElements.W)
                .rotorStats(7.0f, 3.0f, 2560)
                .cableProperties(GTValues.V[5], 2, 2)
                .fluidPipeProperties(4618, 50, true, true, false, true)
                .blastTemp(3600, GasTier.MID, VA[EV], 1800)
                .fluidTemp(3695)
                .build());

        Uranium238 = MATERIALS.register("uranium", () -> new Material.Builder("uranium")
                .ingot(3).fluid()
                .color(0x32F032).iconSet(METALLIC.get())
                .flags(EXT_METAL)
                .element(GtElements.U238)
                .fluidTemp(1405)
                .build());

        Uranium235 = MATERIALS.register("uranium_235", () -> new Material.Builder("uranium_235")
                .ingot(3).fluid()
                .color(0x46FA46).iconSet(SHINY.get())
                .flags(EXT_METAL)
                .element(GtElements.U235)
                .fluidTemp(1405)
                .build());

        Vanadium = MATERIALS.register("vanadium", () -> new Material.Builder("vanadium")
                .ingot().fluid()
                .color(0x323232).iconSet(METALLIC.get())
                .element(GtElements.V)
                .blastTemp(2183, GasTier.MID)
                .build());

        Xenon = MATERIALS.register("xenon", () -> new Material.Builder("xenon")
                .fluid(GTFluidTypes.GAS)
                .color(0x00FFFF).iconSet(GAS.get())
                .element(GtElements.Xe)
                .build());

        Ytterbium = MATERIALS.register("ytterbium", () -> new Material.Builder("ytterbium")
                .color(0xA7A7A7).iconSet(METALLIC.get())
                .element(GtElements.Yb)
                .build());

        Yttrium = MATERIALS.register("yttrium", () -> new Material.Builder("yttrium")
                .ingot().fluid()
                .color(0x76524C).iconSet(METALLIC.get())
                .element(GtElements.Y)
                .blastTemp(1799)
                .build());

        Zinc = MATERIALS.register("zinc", () -> new Material.Builder("zinc")
                .ingot(1).fluid()
                .color(0xEBEBFA).iconSet(METALLIC.get())
                .flags(STD_METAL, MORTAR_GRINDABLE.get(), GENERATE_FOIL.get(), GENERATE_RING.get(), GENERATE_FINE_WIRE.get())
                .element(GtElements.Zn)
                .fluidTemp(693)
                .build());

        Zirconium = MATERIALS.register("zirconium", () -> new Material.Builder("zirconium")
                .color(0xC8FFFF).iconSet(METALLIC.get())
                .element(GtElements.Zr)
                .build());

        Naquadah = MATERIALS.register("naquadah", () -> new Material.Builder("naquadah")
                .ingot(4).fluid().ore()
                .color(0x323232, false).iconSet(METALLIC.get())
                .flags(EXT_METAL, GENERATE_FOIL.get(), GENERATE_SPRING.get(), GENERATE_FINE_WIRE.get(), GENERATE_BOLT_SCREW.get())
                .element(GtElements.Nq)
                .rotorStats(6.0f, 4.0f, 1280)
                .cableProperties(GTValues.V[7], 2, 2)
                .fluidPipeProperties(3776, 200, true, false, true, true)
                .blastTemp(5000, GasTier.HIGH, VA[IV], 600)
                .build());

        NaquadahEnriched = MATERIALS.register("naquadah_enriched", () -> new Material.Builder("naquadah_enriched")
                .ingot(4).fluid()
                .color(0x3C3C3C, false).iconSet(METALLIC.get())
                .flags(EXT_METAL, GENERATE_FOIL.get())
                .element(GtElements.Nq1)
                .blastTemp(7000, GasTier.HIGH, VA[IV], 1000)
                .build());

        Naquadria = MATERIALS.register("naquadria", () -> new Material.Builder("naquadria")
                .ingot(3).fluid()
                .color(0x1E1E1E, false).iconSet(SHINY.get())
                .flags(EXT_METAL, GENERATE_FOIL.get(), GENERATE_GEAR.get(), GENERATE_FINE_WIRE.get(), GENERATE_BOLT_SCREW.get())
                .element(GtElements.Nq2)
                .blastTemp(9000, GasTier.HIGH, VA[ZPM], 1200)
                .build());

        Neutronium = MATERIALS.register("neutronium", () -> new Material.Builder("neutronium")
                .ingot(6).fluid()
                .color(0xFAFAFA)
                .flags(EXT_METAL, GENERATE_BOLT_SCREW.get(), GENERATE_FRAME.get())
                .element(GtElements.Nt)
                .toolStats(ToolProperty.Builder.of(180.0F, 100.0F, 65535, 6)
                        .attackSpeed(0.5F).enchantability(33).magnetic().unbreakable().build())
                .rotorStats(24.0f, 12.0f, 655360)
                .fluidPipeProperties(100_000, 5000, true, true, true, true)
                .fluidTemp(100_000)
                .build());

        Tritanium = MATERIALS.register("tritanium", () -> new Material.Builder("tritanium")
                .ingot(6).fluid()
                .color(0x600000).iconSet(METALLIC.get())
                .flags(EXT2_METAL, GENERATE_FRAME.get(), GENERATE_RING.get(), GENERATE_SMALL_GEAR.get(), GENERATE_ROUND.get(), GENERATE_FOIL.get(), GENERATE_FINE_WIRE.get(), GENERATE_GEAR.get())
                .element(GtElements.Tr)
                .cableProperties(GTValues.V[8], 1, 8)
                .rotorStats(20.0f, 6.0f, 10240)
                .fluidTemp(25000)
                .build());

        Duranium = MATERIALS.register("duranium", () -> new Material.Builder("duranium")
                .ingot(5).fluid()
                .color(0x4BAFAF).iconSet(BRIGHT.get())
                .flags(EXT_METAL, GENERATE_FOIL.get(), GENERATE_GEAR.get())
                .element(GtElements.Dr)
                .toolStats(ToolProperty.Builder.of(14.0F, 12.0F, 8192, 5)
                        .attackSpeed(0.3F).enchantability(33).magnetic().build())
                .fluidPipeProperties(9625, 500, true, true, true, true)
                .fluidTemp(7500)
                .build());

        Trinium = MATERIALS.register("trinium", () -> new Material.Builder("trinium")
                .ingot(7).fluid()
                .color(0x9973BD).iconSet(SHINY.get())
                .flags(GENERATE_FOIL.get(), GENERATE_BOLT_SCREW.get(), GENERATE_GEAR.get())
                .element(GtElements.Ke)
                .cableProperties(GTValues.V[7], 6, 4)
                .blastTemp(7200, GasTier.HIGH, VA[LuV], 1500)
                .build());

    }
}
