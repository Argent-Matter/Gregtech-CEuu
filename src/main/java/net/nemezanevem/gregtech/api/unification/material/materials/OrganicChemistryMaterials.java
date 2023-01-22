package net.nemezanevem.gregtech.api.unification.material.materials;

import net.nemezanevem.gregtech.api.fluids.GtFluidTypes;
import net.nemezanevem.gregtech.api.unification.material.Material;

import static net.nemezanevem.gregtech.api.registry.material.MaterialRegistry.MATERIALS;
import static net.nemezanevem.gregtech.api.unification.material.GtMaterials.*;
import static net.nemezanevem.gregtech.api.unification.material.properties.info.GtMaterialFlags.*;
import static net.nemezanevem.gregtech.api.unification.material.properties.info.GtMaterialIconSets.*;

public class OrganicChemistryMaterials {
    /**
     * ID RANGE: 1000-1068 (incl.)
     */
    public static void register() {
        SiliconeRubber = MATERIALS.register("silicone_rubber", () -> new Material.Builder("silicone_rubber")
                .polymer()
                .color(0xDCDCDC)
                .flags(GENERATE_GEAR.get(), GENERATE_RING.get(), GENERATE_FOIL.get())
                .components(Carbon, 2, Hydrogen, 6, Oxygen, 1, Silicon, 1)
                .fluidTemp(900)
                .build());

        Nitrobenzene = MATERIALS.register("nitrobenzene", () -> new Material.Builder("nitrobenzene")
                .fluid(GtFluidTypes.GAS.get())
                .color(0x704936)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 6, Hydrogen, 5, Nitrogen, 1, Oxygen, 2)
                .build());

        RawRubber = MATERIALS.register("raw_rubber", () -> new Material.Builder("raw_rubber")
                .polymer()
                .color(0xCCC789)
                .components(Carbon, 5, Hydrogen, 8)
                .build());

        RawStyreneButadieneRubber = MATERIALS.register("raw_styrene_butadiene_rubber", () -> new Material.Builder("raw_styrene_butadiene_rubber")
                .dust()
                .color(0x54403D).iconSet(SHINY.get())
                .flags(DISABLE_DECOMPOSITION.get(), FLAMMABLE.get())
                .components(Carbon, 20, Hydrogen, 26)
                .build()
                .setFormula("(C4H6)3C8H8", true));

        StyreneButadieneRubber = MATERIALS.register("styrene_butadiene_rubber", () -> new Material.Builder("styrene_butadiene_rubber")
                .polymer()
                .color(0x211A18).iconSet(SHINY.get())
                .flags(GENERATE_FOIL.get(), GENERATE_RING.get())
                .components(Carbon, 20, Hydrogen, 26)
                .fluidTemp(1000)
                .build()
                .setFormula("(C4H6)3C8H8", true));

        PolyvinylAcetate = MATERIALS.register("polyvinyl_acetate", () -> new Material.Builder("polyvinyl_acetate")
                .fluid()
                .color(0xFF9955)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 4, Hydrogen, 6, Oxygen, 2)
                .build());

        ReinforcedEpoxyResin = MATERIALS.register("reinforced_epoxy_resin", () -> new Material.Builder("reinforced_epoxy_resin")
                .polymer()
                .color(0xA07A10)
                .flags(STD_METAL)
                .components(Carbon, 6, Hydrogen, 4, Oxygen, 1)
                .fluidTemp(600)
                .build());

        PolyvinylChloride = MATERIALS.register("polyvinyl_chloride", () -> new Material.Builder("polyvinyl_chloride")
                .polymer()
                .color(0xD7E6E6)
                .flags(EXT_METAL, GENERATE_FOIL.get())
                .components(Carbon, 2, Hydrogen, 3, Chlorine, 1)
                .itemPipeProperties(512, 4)
                .fluidTemp(373)
                .build());

        PolyphenyleneSulfide = MATERIALS.register("polyphenylene_sulfide", () -> new Material.Builder("polyphenylene_sulfide")
                .polymer()
                .color(0xAA8800)
                .flags(EXT_METAL, GENERATE_FOIL.get())
                .components(Carbon, 6, Hydrogen, 4, Sulfur, 1)
                .fluidTemp(500)
                .build());

        GlycerylTrinitrate = MATERIALS.register("glyceryl_trinitrate", () -> new Material.Builder("glyceryl_trinitrate")
                .fluid()
                .flags(FLAMMABLE.get(), EXPLOSIVE.get())
                .components(Carbon, 3, Hydrogen, 5, Nitrogen, 3, Oxygen, 9)
                .build());

        Polybenzimidazole = MATERIALS.register("polybenzimidazole", () -> new Material.Builder("polybenzimidazole")
                .polymer()
                .color(0x2D2D2D)
                .flags(EXCLUDE_BLOCK_CRAFTING_RECIPES.get(), GENERATE_FOIL.get())
                .components(Carbon, 20, Hydrogen, 12, Nitrogen, 4)
                .fluidPipeProperties(1000, 350, true)
                .fluidTemp(1450)
                .build());

        Polydimethylsiloxane = MATERIALS.register("polydimethylsiloxane", () -> new Material.Builder("polydimethylsiloxane")
                .dust()
                .color(0xF5F5F5)
                .flags(DISABLE_DECOMPOSITION.get(), FLAMMABLE.get())
                .components(Carbon, 2, Hydrogen, 6, Oxygen, 1, Silicon, 1)
                .build());

        Polyethylene = MATERIALS.register("plastic", () -> new Material.Builder("plastic") //todo add polyethylene oredicts
                .polymer(1)
                .color(0xC8C8C8)
                .flags(GENERATE_FOIL.get())
                .components(Carbon, 2, Hydrogen, 4)
                .fluidPipeProperties(370, 50, true)
                .fluidTemp(408)
                .build());

        Epoxy = MATERIALS.register("epoxy", () -> new Material.Builder("epoxy")
                .polymer(1)
                .color(0xC88C14)
                .flags(STD_METAL)
                .components(Carbon, 21, Hydrogen, 25, Chlorine, 1, Oxygen, 5)
                .fluidTemp(400)
                .build());

        // Free ID 1014

        Polycaprolactam = MATERIALS.register("polycaprolactam", () -> new Material.Builder("polycaprolactam")
                .polymer(1)
                .color(0x323232)
                .flags(STD_METAL, GENERATE_FOIL.get())
                .components(Carbon, 6, Hydrogen, 11, Nitrogen, 1, Oxygen, 1)
                .fluidTemp(493)
                .build());

        Polytetrafluoroethylene = MATERIALS.register("polytetrafluoroethylene", () -> new Material.Builder("polytetrafluoroethylene")
                .polymer(1)
                .color(0x646464)
                .flags(STD_METAL, GENERATE_FRAME.get(), GENERATE_FOIL.get())
                .components(Carbon, 2, Fluorine, 4)
                .fluidPipeProperties(600, 100, true, true, false, false)
                .fluidTemp(600)
                .build());

        Sugar = MATERIALS.register("sugar", () -> new Material.Builder("sugar")
                .gem(1)
                .color(0xFAFAFA).iconSet(FINE.get())
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 6, Hydrogen, 12, Oxygen, 6)
                .build());

        Methane = MATERIALS.register("methane", () -> new Material.Builder("methane")
                .fluid(GtFluidTypes.GAS.get())
                .color(0xFF0078).iconSet(GAS.get())
                .components(Carbon, 1, Hydrogen, 4)
                .build());

        Epichlorohydrin = MATERIALS.register("epichlorohydrin", () -> new Material.Builder("epichlorohydrin")
                .fluid()
                .color(0x712400)
                .components(Carbon, 3, Hydrogen, 5, Chlorine, 1, Oxygen, 1)
                .build());

        Monochloramine = MATERIALS.register("monochloramine", () -> new Material.Builder("monochloramine")
                .fluid(GtFluidTypes.GAS.get())
                .color(0x3F9F80)
                .components(Nitrogen, 1, Hydrogen, 2, Chlorine, 1)
                .build());

        Chloroform = MATERIALS.register("chloroform", () -> new Material.Builder("chloroform")
                .fluid()
                .color(0x892CA0)
                .components(Carbon, 1, Hydrogen, 1, Chlorine, 3)
                .build());

        Cumene = MATERIALS.register("cumene", () -> new Material.Builder("cumene")
                .fluid(GtFluidTypes.GAS.get())
                .color(0x552200)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 9, Hydrogen, 12)
                .build());

        Tetrafluoroethylene = MATERIALS.register("tetrafluoroethylene", () -> new Material.Builder("tetrafluoroethylene")
                .fluid(GtFluidTypes.GAS.get())
                .color(0x7D7D7D)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 2, Fluorine, 4)
                .build());

        Chloromethane = MATERIALS.register("chloromethane", () -> new Material.Builder("chloromethane")
                .fluid(GtFluidTypes.GAS.get())
                .color(0xC82CA0)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 1, Hydrogen, 3, Chlorine, 1)
                .build());

        AllylChloride = MATERIALS.register("allyl_chloride", () -> new Material.Builder("allyl_chloride")
                .fluid()
                .color(0x87DEAA)
                .components(Carbon, 2, Methane, 1, HydrochloricAcid, 1)
                .build()
                .setFormula("C3H5Cl", true));

        Isoprene = MATERIALS.register("isoprene", () -> new Material.Builder("isoprene")
                .fluid()
                .color(0x141414)
                .components(Carbon, 5, Hydrogen, 8)
                .build());

        Propane = MATERIALS.register("propane", () -> new Material.Builder("propane")
                .fluid(GtFluidTypes.GAS.get())
                .color(0xFAE250)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 3, Hydrogen, 8)
                .build());

        Propene = MATERIALS.register("propene", () -> new Material.Builder("propene")
                .fluid(GtFluidTypes.GAS.get())
                .color(0xFFDD55)
                .components(Carbon, 3, Hydrogen, 6)
                .build());

        Ethane = MATERIALS.register("ethane", () -> new Material.Builder("ethane")
                .fluid(GtFluidTypes.GAS.get())
                .color(0xC8C8FF)
                .components(Carbon, 2, Hydrogen, 6)
                .build());

        Butene = MATERIALS.register("butene", () -> new Material.Builder("butene")
                .fluid(GtFluidTypes.GAS.get())
                .color(0xCF5005)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 4, Hydrogen, 8)
                .build());

        Butane = MATERIALS.register("butane", () -> new Material.Builder("butane")
                .fluid(GtFluidTypes.GAS.get())
                .color(0xB6371E)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 4, Hydrogen, 10)
                .build());

        DissolvedCalciumAcetate = MATERIALS.register("dissolved_calcium_acetate", () -> new Material.Builder("dissolved_calcium_acetate")
                .fluid()
                .color(0xDCC8B4)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Calcium, 1, Carbon, 4, Oxygen, 4, Hydrogen, 6, Water, 1)
                .build());

        VinylAcetate = MATERIALS.register("vinyl_acetate", () -> new Material.Builder("vinyl_acetate")
                .fluid()
                .color(0xE1B380)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 4, Hydrogen, 6, Oxygen, 2)
                .build());

        MethylAcetate = MATERIALS.register("methyl_acetate", () -> new Material.Builder("methyl_acetate")
                .fluid()
                .color(0xEEC6AF)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 3, Hydrogen, 6, Oxygen, 2)
                .build());

        Ethenone = MATERIALS.register("ethenone", () -> new Material.Builder("ethenone")
                .fluid()
                .color(0x141446)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 2, Hydrogen, 2, Oxygen, 1)
                .build());

        Tetranitromethane = MATERIALS.register("tetranitromethane", () -> new Material.Builder("tetranitromethane")
                .fluid()
                .color(0x0F2828)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 1, Nitrogen, 4, Oxygen, 8)
                .build());

        Dimethylamine = MATERIALS.register("dimethylamine", () -> new Material.Builder("dimethylamine")
                .fluid(GtFluidTypes.GAS.get())
                .color(0x554469)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 2, Hydrogen, 7, Nitrogen, 1)
                .build());

        Dimethylhydrazine = MATERIALS.register("dimethylhydrazine", () -> new Material.Builder("dimethylhydrazine")
                .fluid()
                .color(0x000055)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 2, Hydrogen, 8, Nitrogen, 2)
                .build());

        DinitrogenTetroxide = MATERIALS.register("dinitrogen_tetroxide", () -> new Material.Builder("dinitrogen_tetroxide")
                .fluid(GtFluidTypes.GAS.get())
                .color(0x004184)
                .components(Nitrogen, 2, Oxygen, 4)
                .build());

        Dimethyldichlorosilane = MATERIALS.register("dimethyldichlorosilane", () -> new Material.Builder("dimethyldichlorosilane")
                .fluid(GtFluidTypes.GAS.get())
                .color(0x441650)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 2, Hydrogen, 6, Chlorine, 2, Silicon, 1)
                .build());

        Styrene = MATERIALS.register("styrene", () -> new Material.Builder("styrene")
                .fluid()
                .color(0xD2C8BE)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 8, Hydrogen, 8)
                .build());

        Butadiene = MATERIALS.register("butadiene", () -> new Material.Builder("butadiene")
                .fluid(GtFluidTypes.GAS.get())
                .color(0xB55A10)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 4, Hydrogen, 6)
                .build());

        Dichlorobenzene = MATERIALS.register("dichlorobenzene", () -> new Material.Builder("dichlorobenzene")
                .fluid()
                .color(0x004455)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 6, Hydrogen, 4, Chlorine, 2)
                .build());

        AceticAcid = MATERIALS.register("acetic_acid", () -> new Material.Builder("acetic_acid")
                .fluid(GtFluidTypes.ACID.get())
                .color(0xC8B4A0)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 2, Hydrogen, 4, Oxygen, 2)
                .build());

        Phenol = MATERIALS.register("phenol", () -> new Material.Builder("phenol")
                .fluid()
                .color(0x784421)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 6, Hydrogen, 6, Oxygen, 1)
                .build());

        BisphenolA = MATERIALS.register("bisphenol_a", () -> new Material.Builder("bisphenol_a")
                .fluid()
                .color(0xD4AA00)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 15, Hydrogen, 16, Oxygen, 2)
                .build());

        VinylChloride = MATERIALS.register("vinyl_chloride", () -> new Material.Builder("vinyl_chloride")
                .fluid(GtFluidTypes.GAS.get())
                .color(0xE1F0F0)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 2, Hydrogen, 3, Chlorine, 1)
                .build());

        Ethylene = MATERIALS.register("ethylene", () -> new Material.Builder("ethylene")
                .fluid(GtFluidTypes.GAS.get())
                .color(0xE1E1E1)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 2, Hydrogen, 4)
                .build());

        Benzene = MATERIALS.register("benzene", () -> new Material.Builder("benzene")
                .fluid()
                .color(0x1A1A1A)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 6, Hydrogen, 6)
                .build());

        Acetone = MATERIALS.register("acetone", () -> new Material.Builder("acetone")
                .fluid()
                .color(0xAFAFAF)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 3, Hydrogen, 6, Oxygen, 1)
                .build());

        Glycerol = MATERIALS.register("glycerol", () -> new Material.Builder("glycerol")
                .fluid()
                .color(0x87DE87)
                .components(Carbon, 3, Hydrogen, 8, Oxygen, 3)
                .build());

        Methanol = MATERIALS.register("methanol", () -> new Material.Builder("methanol")
                .fluid()
                .color(0xAA8800)
                .components(Carbon, 1, Hydrogen, 4, Oxygen, 1)
                .build());

        // FREE ID 1053

        Ethanol = MATERIALS.register("ethanol", () -> new Material.Builder("ethanol")
                .fluid()
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 2, Hydrogen, 6, Oxygen, 1)
                .build());

        Toluene = MATERIALS.register("toluene", () -> new Material.Builder("toluene")
                .fluid()
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 7, Hydrogen, 8)
                .build());

        DiphenylIsophtalate = MATERIALS.register("diphenyl_isophthalate", () -> new Material.Builder("diphenyl_isophthalate")
                .fluid()
                .color(0x246E57)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 20, Hydrogen, 14, Oxygen, 4)
                .build());

        PhthalicAcid = MATERIALS.register("phthalic_acid", () -> new Material.Builder("phthalic_acid")
                .fluid(GtFluidTypes.ACID.get())
                .color(0xD1D1D1)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 8, Hydrogen, 6, Oxygen, 4)
                .build()
                .setFormula("C6H4(CO2H)2", true));

        Dimethylbenzene = MATERIALS.register("dimethylbenzene", () -> new Material.Builder("dimethylbenzene")
                .fluid()
                .color(0x669C40)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 8, Hydrogen, 10)
                .build()
                .setFormula("C6H4(CH3)2", true));

        Diaminobenzidine = MATERIALS.register("diaminobenzidine", () -> new Material.Builder("diaminobenzidine")
                .fluid()
                .color(0x337D59)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 12, Hydrogen, 14, Nitrogen, 4)
                .build()
                .setFormula("(C6H3(NH2)2)2", true));

        Dichlorobenzidine = MATERIALS.register("dichlorobenzidine", () -> new Material.Builder("dichlorobenzidine")
                .fluid()
                .color(0xA1DEA6)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 12, Hydrogen, 10, Chlorine, 2, Nitrogen, 2)
                .build()
                .setFormula("(C6H3Cl(NH2))2", true));

        Nitrochlorobenzene = MATERIALS.register("nitrochlorobenzene", () -> new Material.Builder("nitrochlorobenzene")
                .fluid()
                .color(0x8FB51A)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 6, Hydrogen, 4, Chlorine, 1, Nitrogen, 1, Oxygen, 2)
                .build());

        Chlorobenzene = MATERIALS.register("chlorobenzene", () -> new Material.Builder("chlorobenzene")
                .fluid()
                .color(0x326A3E)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 6, Hydrogen, 5, Chlorine, 1)
                .build());

        Octane = MATERIALS.register("octane", () -> new Material.Builder("octane")
                .fluid()
                .flags(DISABLE_DECOMPOSITION.get())
                .color(0x8A0A09)
                .components(Carbon, 8, Hydrogen, 18)
                .build());

        EthylTertButylEther = MATERIALS.register("ethyl_tertbutyl_ether", () -> new Material.Builder("ethyl_tertbutyl_ether")
                .fluid()
                .flags(DISABLE_DECOMPOSITION.get())
                .color(0xB15C06)
                .components(Carbon, 6, Hydrogen, 14, Oxygen, 1)
                .build());

        Ethylbenzene = MATERIALS.register("ethylbenzene", () -> new Material.Builder("ethylbenzene")
                .fluid()
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 8, Hydrogen, 10)
                .build());

        Naphthalene = MATERIALS.register("naphthalene", () -> new Material.Builder("naphthalene")
                .fluid()
                .flags(DISABLE_DECOMPOSITION.get())
                .color(0xF4F4D7)
                .components(Carbon, 10, Hydrogen, 8)
                .build());

        Rubber = MATERIALS.register("rubber", () -> new Material.Builder("rubber")
                .polymer(0)
                .color(0x000000).iconSet(SHINY.get())
                .flags(GENERATE_GEAR.get(), GENERATE_RING.get(), GENERATE_FOIL.get(), GENERATE_BOLT_SCREW.get())
                .components(Carbon, 5, Hydrogen, 8)
                .fluidTemp(400)
                .build());

        Cyclohexane = MATERIALS.register("cyclohexane", () -> new Material.Builder("cyclohexane")
                .fluid()
                .color(0xF2F2F2E7)
                .components(Carbon, 6, Hydrogen, 12)
                .build());

        NitrosylChloride = MATERIALS.register("nitrosyl_chloride", () -> new Material.Builder("nitrosyl_chloride")
                .fluid(GtFluidTypes.GAS.get())
                .flags(FLAMMABLE.get())
                .color(0xF3F100)
                .components(Nitrogen, 1, Oxygen, 1, Chlorine, 1)
                .build());

        CyclohexanoneOxime = MATERIALS.register("cyclohexanone_oxime", () -> new Material.Builder("cyclohexanone_oxime")
                .dust()
                .flags(DISABLE_DECOMPOSITION.get(), FLAMMABLE.get())
                .color(0xEBEBF0).iconSet(ROUGH.get())
                .components(Carbon, 6, Hydrogen, 11, Nitrogen, 1, Oxygen, 1)
                .build()
                .setFormula("C6H11NO", true));

        Caprolactam = MATERIALS.register("caprolactam", () -> new Material.Builder("caprolactam")
                .dust()
                .flags(DISABLE_DECOMPOSITION.get(), FLAMMABLE.get())
                .color(0x676768)
                .components(Carbon, 6, Hydrogen, 11, Nitrogen, 1, Oxygen, 1)
                .build()
                .setFormula("(CH2)5C(O)NH", true));

        Butyraldehyde = MATERIALS.register("butyraldehyde", () -> new Material.Builder("butyraldehyde")
                .fluid()
                .color(0x554A3F)
                .flags(DISABLE_DECOMPOSITION.get())
                .components(Carbon, 4, Hydrogen, 8, Oxygen, 1)
                .build());

        PolyvinylButyral = MATERIALS.register("polyvinyl_butyral", () -> new Material.Builder("polyvinyl_butyral")
                .ingot().fluid()
                .color(0x347D41)
                .flags(GENERATE_PLATE.get(), DISABLE_DECOMPOSITION.get(), NO_SMASHING.get())
                .components(Butyraldehyde, 1, PolyvinylAcetate, 1)
                .build());
    }
}
