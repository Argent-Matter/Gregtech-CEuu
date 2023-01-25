package net.nemezanevem.gregtech.api.unification.material;

import net.minecraftforge.registries.RegistryObject;
import net.nemezanevem.gregtech.api.unification.material.materials.*;
import net.nemezanevem.gregtech.api.unification.material.properties.info.MaterialFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.nemezanevem.gregtech.api.registry.material.MaterialRegistry.MATERIALS;
import static net.nemezanevem.gregtech.api.unification.material.properties.info.GtMaterialFlags.*;

/**
 * RegistryObject<Material> Registration.
 * <p>
 * All RegistryObject<Material> Builders should follow this general formatting:
 * <p>
 * material = new MaterialBuilder(name)
 * .ingot().fluid().ore()                <--- types
 * .color().iconSet()                    <--- appearance
 * .flags()                              <--- special generation
 * .element() / .components()            <--- composition
 * .toolStats()                          <---
 * .oreByProducts()                         | additional properties
 * ...                                   <---
 * .blastTemp()                          <--- blast temperature
 * .build();
 * <p>
 * Use defaults to your advantage! Some defaults:
 * - iconSet: DULL
 * - color: 0xFFFFFF
 */
public class GtMaterials {

    private static final AtomicBoolean INIT = new AtomicBoolean(false);

    public static Material[] CHEMICAL_DYES;

    public static void register() {
        if (INIT.getAndSet(true)) {
            return;
        }

        MarkerMaterials.register();
        ElementMaterials.register();
        FirstDegreeMaterials.register();
        OrganicChemistryMaterials.register();
        UnknownCompositionMaterials.register();
        SecondDegreeMaterials.register();
        HigherDegreeMaterials.register();
        MaterialFlagAddition.register();

        CHEMICAL_DYES = new Material[]{
                GtMaterials.DyeWhite.get(), GtMaterials.DyeOrange.get(),
                GtMaterials.DyeMagenta.get(), GtMaterials.DyeLightBlue.get(),
                GtMaterials.DyeYellow.get(), GtMaterials.DyeLime.get(),
                GtMaterials.DyePink.get(), GtMaterials.DyeGray.get(),
                GtMaterials.DyeLightGray.get(), GtMaterials.DyeCyan.get(),
                GtMaterials.DyePurple.get(), GtMaterials.DyeBlue.get(),
                GtMaterials.DyeBrown.get(), GtMaterials.DyeGreen.get(),
                GtMaterials.DyeRed.get(), GtMaterials.DyeBlack.get()
        };
    }

    public static final List<MaterialFlag> STD_METAL = new ArrayList<>();
    public static final List<MaterialFlag> EXT_METAL = new ArrayList<>();
    public static final List<MaterialFlag> EXT2_METAL = new ArrayList<>();

    static {
        STD_METAL.add(GENERATE_PLATE.get());

        EXT_METAL.addAll(STD_METAL);
        EXT_METAL.add(GENERATE_ROD.get());

        EXT2_METAL.addAll(EXT_METAL);
        EXT2_METAL.addAll(Arrays.asList(GENERATE_LONG_ROD.get(), GENERATE_BOLT_SCREW.get()));
    }

    public static final RegistryObject<Material> NULL = MATERIALS.register("null", () -> new MarkerMaterial("null"));

    /**
     * Direct Elements
     */
    public static RegistryObject<Material> Actinium;
    public static RegistryObject<Material> Aluminium;
    public static RegistryObject<Material> Americium;
    public static RegistryObject<Material> Antimony;
    public static RegistryObject<Material> Argon;
    public static RegistryObject<Material> Arsenic;
    public static RegistryObject<Material> Astatine;
    public static RegistryObject<Material> Barium;
    public static RegistryObject<Material> Berkelium;
    public static RegistryObject<Material> Beryllium;
    public static RegistryObject<Material> Bismuth;
    public static RegistryObject<Material> Bohrium;
    public static RegistryObject<Material> Boron;
    public static RegistryObject<Material> Bromine;
    public static RegistryObject<Material> Caesium;
    public static RegistryObject<Material> Calcium;
    public static RegistryObject<Material> Californium;
    public static RegistryObject<Material> Carbon;
    public static RegistryObject<Material> Cadmium;
    public static RegistryObject<Material> Cerium;
    public static RegistryObject<Material> Chlorine;
    public static RegistryObject<Material> Chrome;
    public static RegistryObject<Material> Cobalt;
    public static RegistryObject<Material> Copernicium;
    public static RegistryObject<Material> Copper;
    public static RegistryObject<Material> Curium;
    public static RegistryObject<Material> Darmstadtium;
    public static RegistryObject<Material> Deuterium;
    public static RegistryObject<Material> Dubnium;
    public static RegistryObject<Material> Dysprosium;
    public static RegistryObject<Material> Einsteinium;
    public static RegistryObject<Material> Erbium;
    public static RegistryObject<Material> Europium;
    public static RegistryObject<Material> Fermium;
    public static RegistryObject<Material> Flerovium;
    public static RegistryObject<Material> Fluorine;
    public static RegistryObject<Material> Francium;
    public static RegistryObject<Material> Gadolinium;
    public static RegistryObject<Material> Gallium;
    public static RegistryObject<Material> Germanium;
    public static RegistryObject<Material> Gold;
    public static RegistryObject<Material> Hafnium;
    public static RegistryObject<Material> Hassium;
    public static RegistryObject<Material> Holmium;
    public static RegistryObject<Material> Hydrogen;
    public static RegistryObject<Material> Helium;
    public static RegistryObject<Material> Helium3;
    public static RegistryObject<Material> Indium;
    public static RegistryObject<Material> Iodine;
    public static RegistryObject<Material> Iridium;
    public static RegistryObject<Material> Iron;
    public static RegistryObject<Material> Krypton;
    public static RegistryObject<Material> Lanthanum;
    public static RegistryObject<Material> Lawrencium;
    public static RegistryObject<Material> Lead;
    public static RegistryObject<Material> Lithium;
    public static RegistryObject<Material> Livermorium;
    public static RegistryObject<Material> Lutetium;
    public static RegistryObject<Material> Magnesium;
    public static RegistryObject<Material> Mendelevium;
    public static RegistryObject<Material> Manganese;
    public static RegistryObject<Material> Meitnerium;
    public static RegistryObject<Material> Mercury;
    public static RegistryObject<Material> Molybdenum;
    public static RegistryObject<Material> Moscovium;
    public static RegistryObject<Material> Neodymium;
    public static RegistryObject<Material> Neon;
    public static RegistryObject<Material> Neptunium;
    public static RegistryObject<Material> Nickel;
    public static RegistryObject<Material> Nihonium;
    public static RegistryObject<Material> Niobium;
    public static RegistryObject<Material> Nitrogen;
    public static RegistryObject<Material> Nobelium;
    public static RegistryObject<Material> Oganesson;
    public static RegistryObject<Material> Osmium;
    public static RegistryObject<Material> Oxygen;
    public static RegistryObject<Material> Palladium;
    public static RegistryObject<Material> Phosphorus;
    public static RegistryObject<Material> Polonium;
    public static RegistryObject<Material> Platinum;
    public static RegistryObject<Material> Plutonium239;
    public static RegistryObject<Material> Plutonium241;
    public static RegistryObject<Material> Potassium;
    public static RegistryObject<Material> Praseodymium;
    public static RegistryObject<Material> Promethium;
    public static RegistryObject<Material> Protactinium;
    public static RegistryObject<Material> Radon;
    public static RegistryObject<Material> Radium;
    public static RegistryObject<Material> Rhenium;
    public static RegistryObject<Material> Rhodium;
    public static RegistryObject<Material> Roentgenium;
    public static RegistryObject<Material> Rubidium;
    public static RegistryObject<Material> Ruthenium;
    public static RegistryObject<Material> Rutherfordium;
    public static RegistryObject<Material> Samarium;
    public static RegistryObject<Material> Scandium;
    public static RegistryObject<Material> Seaborgium;
    public static RegistryObject<Material> Selenium;
    public static RegistryObject<Material> Silicon;
    public static RegistryObject<Material> Silver;
    public static RegistryObject<Material> Sodium;
    public static RegistryObject<Material> Strontium;
    public static RegistryObject<Material> Sulfur;
    public static RegistryObject<Material> Tantalum;
    public static RegistryObject<Material> Technetium;
    public static RegistryObject<Material> Tellurium;
    public static RegistryObject<Material> Tennessine;
    public static RegistryObject<Material> Terbium;
    public static RegistryObject<Material> Thorium;
    public static RegistryObject<Material> Thallium;
    public static RegistryObject<Material> Thulium;
    public static RegistryObject<Material> Tin;
    public static RegistryObject<Material> Titanium;
    public static RegistryObject<Material> Tritium;
    public static RegistryObject<Material> Tungsten;
    public static RegistryObject<Material> Uranium238;
    public static RegistryObject<Material> Uranium235;
    public static RegistryObject<Material> Vanadium;
    public static RegistryObject<Material> Xenon;
    public static RegistryObject<Material> Ytterbium;
    public static RegistryObject<Material> Yttrium;
    public static RegistryObject<Material> Zinc;
    public static RegistryObject<Material> Zirconium;

    /**
     * Fantasy Elements
     */
    public static RegistryObject<Material> Naquadah;
    public static RegistryObject<Material> NaquadahEnriched;
    public static RegistryObject<Material> Naquadria;
    public static RegistryObject<Material> Neutronium;
    public static RegistryObject<Material> Tritanium;
    public static RegistryObject<Material> Duranium;
    public static RegistryObject<Material> Trinium;

    /**
     * First Degree Compounds
     */
    public static RegistryObject<Material> Almandine;
    public static RegistryObject<Material> Andradite;
    public static RegistryObject<Material> AnnealedCopper;
    public static RegistryObject<Material> Asbestos;
    public static RegistryObject<Material> Ash;
    public static RegistryObject<Material> BandedIron;
    public static RegistryObject<Material> BatteryAlloy;
    public static RegistryObject<Material> BlueTopaz;
    public static RegistryObject<Material> Bone;
    public static RegistryObject<Material> Brass;
    public static RegistryObject<Material> Bronze;
    public static RegistryObject<Material> BrownLimonite;
    public static RegistryObject<Material> Calcite;
    public static RegistryObject<Material> Cassiterite;
    public static RegistryObject<Material> CassiteriteSand;
    public static RegistryObject<Material> Chalcopyrite;
    public static RegistryObject<Material> Charcoal;
    public static RegistryObject<Material> Chromite;
    public static RegistryObject<Material> Cinnabar;
    public static RegistryObject<Material> Water;
    public static RegistryObject<Material> LiquidOxygen;
    public static RegistryObject<Material> Coal;
    public static RegistryObject<Material> Cobaltite;
    public static RegistryObject<Material> Cooperite;
    public static RegistryObject<Material> Cupronickel;
    public static RegistryObject<Material> DarkAsh;
    public static RegistryObject<Material> Diamond;
    public static RegistryObject<Material> Electrum;
    public static RegistryObject<Material> Emerald;
    public static RegistryObject<Material> Galena;
    public static RegistryObject<Material> Garnierite;
    public static RegistryObject<Material> GreenSapphire;
    public static RegistryObject<Material> Grossular;
    public static RegistryObject<Material> Ice;
    public static RegistryObject<Material> Ilmenite;
    public static RegistryObject<Material> Rutile;
    public static RegistryObject<Material> Bauxite;
    public static RegistryObject<Material> Invar;
    public static RegistryObject<Material> Kanthal;
    public static RegistryObject<Material> Lazurite;
    public static RegistryObject<Material> LiquidHelium;
    public static RegistryObject<Material> Magnalium;
    public static RegistryObject<Material> Magnesite;
    public static RegistryObject<Material> Magnetite;
    public static RegistryObject<Material> Molybdenite;
    public static RegistryObject<Material> Nichrome;
    public static RegistryObject<Material> NiobiumNitride;
    public static RegistryObject<Material> NiobiumTitanium;
    public static RegistryObject<Material> Obsidian;
    public static RegistryObject<Material> Phosphate;
    public static RegistryObject<Material> SterlingSilver;
    public static RegistryObject<Material> RoseGold;
    public static RegistryObject<Material> BlackBronze;
    public static RegistryObject<Material> BismuthBronze;
    public static RegistryObject<Material> Biotite;
    public static RegistryObject<Material> Powellite;
    public static RegistryObject<Material> Pyrite;
    public static RegistryObject<Material> Pyrolusite;
    public static RegistryObject<Material> Pyrope;
    public static RegistryObject<Material> RockSalt;
    public static RegistryObject<Material> Ruridit;
    public static RegistryObject<Material> Rubber;
    public static RegistryObject<Material> Ruby;
    public static RegistryObject<Material> Salt;
    public static RegistryObject<Material> Saltpeter;
    public static RegistryObject<Material> Sapphire;
    public static RegistryObject<Material> Scheelite;
    public static RegistryObject<Material> Sodalite;
    public static RegistryObject<Material> AluminiumSulfite;
    public static RegistryObject<Material> Tantalite;
    public static RegistryObject<Material> Coke;


    public static RegistryObject<Material> SolderingAlloy;
    public static RegistryObject<Material> Spessartine;
    public static RegistryObject<Material> Sphalerite;
    public static RegistryObject<Material> StainlessSteel;
    public static RegistryObject<Material> Steel;
    public static RegistryObject<Material> Stibnite;
    public static RegistryObject<Material> Tetrahedrite;
    public static RegistryObject<Material> TinAlloy;
    public static RegistryObject<Material> Topaz;
    public static RegistryObject<Material> Tungstate;
    public static RegistryObject<Material> Ultimet;
    public static RegistryObject<Material> Uraninite;
    public static RegistryObject<Material> Uvarovite;
    public static RegistryObject<Material> VanadiumGallium;
    public static RegistryObject<Material> WroughtIron;
    public static RegistryObject<Material> Wulfenite;
    public static RegistryObject<Material> YellowLimonite;
    public static RegistryObject<Material> YttriumBariumCuprate;
    public static RegistryObject<Material> NetherQuartz;
    public static RegistryObject<Material> CertusQuartz;
    public static RegistryObject<Material> Quartzite;
    public static RegistryObject<Material> Graphite;
    public static RegistryObject<Material> Graphene;
    public static RegistryObject<Material> TungsticAcid;
    public static RegistryObject<Material> Osmiridium;
    public static RegistryObject<Material> LithiumChloride;
    public static RegistryObject<Material> CalciumChloride;
    public static RegistryObject<Material> Bornite;
    public static RegistryObject<Material> Chalcocite;

    public static RegistryObject<Material> GalliumArsenide;
    public static RegistryObject<Material> Potash;
    public static RegistryObject<Material> SodaAsh;
    public static RegistryObject<Material> IndiumGalliumPhosphide;
    public static RegistryObject<Material> NickelZincFerrite;
    public static RegistryObject<Material> SiliconDioxide;
    public static RegistryObject<Material> MagnesiumChloride;
    public static RegistryObject<Material> SodiumSulfide;
    public static RegistryObject<Material> PhosphorusPentoxide;
    public static RegistryObject<Material> Quicklime;
    public static RegistryObject<Material> SodiumBisulfate;
    public static RegistryObject<Material> FerriteMixture;
    public static RegistryObject<Material> Magnesia;
    public static RegistryObject<Material> PlatinumGroupSludge;
    public static RegistryObject<Material> Realgar;
    public static RegistryObject<Material> SodiumBicarbonate;
    public static RegistryObject<Material> PotassiumDichromate;
    public static RegistryObject<Material> ChromiumTrioxide;
    public static RegistryObject<Material> AntimonyTrioxide;
    public static RegistryObject<Material> Zincite;
    public static RegistryObject<Material> CupricOxide;
    public static RegistryObject<Material> CobaltOxide;
    public static RegistryObject<Material> ArsenicTrioxide;
    public static RegistryObject<Material> Massicot;
    public static RegistryObject<Material> Ferrosilite;
    public static RegistryObject<Material> MetalMixture;
    public static RegistryObject<Material> SodiumHydroxide;
    public static RegistryObject<Material> SodiumPersulfate;
    public static RegistryObject<Material> Bastnasite;
    public static RegistryObject<Material> Pentlandite;
    public static RegistryObject<Material> Spodumene;
    public static RegistryObject<Material> Lepidolite;
    public static RegistryObject<Material> GlauconiteSand;
    public static RegistryObject<Material> Malachite;
    public static RegistryObject<Material> Mica;
    public static RegistryObject<Material> Barite;
    public static RegistryObject<Material> Alunite;
    public static RegistryObject<Material> Talc;
    public static RegistryObject<Material> Soapstone;
    public static RegistryObject<Material> Kyanite;
    public static RegistryObject<Material> IronMagnetic;
    public static RegistryObject<Material> TungstenCarbide;
    public static RegistryObject<Material> CarbonDioxide;
    public static RegistryObject<Material> TitaniumTetrachloride;
    public static RegistryObject<Material> NitrogenDioxide;
    public static RegistryObject<Material> HydrogenSulfide;
    public static RegistryObject<Material> NitricAcid;
    public static RegistryObject<Material> SulfuricAcid;
    public static RegistryObject<Material> PhosphoricAcid;
    public static RegistryObject<Material> SulfurTrioxide;
    public static RegistryObject<Material> SulfurDioxide;
    public static RegistryObject<Material> CarbonMonoxide;
    public static RegistryObject<Material> HypochlorousAcid;
    public static RegistryObject<Material> Ammonia;
    public static RegistryObject<Material> HydrofluoricAcid;
    public static RegistryObject<Material> NitricOxide;
    public static RegistryObject<Material> Iron3Chloride;
    public static RegistryObject<Material> UraniumHexafluoride;
    public static RegistryObject<Material> EnrichedUraniumHexafluoride;
    public static RegistryObject<Material> DepletedUraniumHexafluoride;
    public static RegistryObject<Material> NitrousOxide;
    public static RegistryObject<Material> EnderPearl;
    public static RegistryObject<Material> PotassiumFeldspar;
    public static RegistryObject<Material> NeodymiumMagnetic;
    public static RegistryObject<Material> HydrochloricAcid;
    public static RegistryObject<Material> Steam;
    public static RegistryObject<Material> DistilledWater;
    public static RegistryObject<Material> SodiumPotassium;
    public static RegistryObject<Material> SamariumMagnetic;
    public static RegistryObject<Material> ManganesePhosphide;
    public static RegistryObject<Material> MagnesiumDiboride;
    public static RegistryObject<Material> MercuryBariumCalciumCuprate;
    public static RegistryObject<Material> UraniumTriplatinum;
    public static RegistryObject<Material> SamariumIronArsenicOxide;
    public static RegistryObject<Material> IndiumTinBariumTitaniumCuprate;
    public static RegistryObject<Material> UraniumRhodiumDinaquadide;
    public static RegistryObject<Material> EnrichedNaquadahTriniumEuropiumDuranide;
    public static RegistryObject<Material> RutheniumTriniumAmericiumNeutronate;
    public static RegistryObject<Material> PlatinumRaw;
    public static RegistryObject<Material> InertMetalMixture;
    public static RegistryObject<Material> RhodiumSulfate;
    public static RegistryObject<Material> RutheniumTetroxide;
    public static RegistryObject<Material> OsmiumTetroxide;
    public static RegistryObject<Material> IridiumChloride;
    public static RegistryObject<Material> FluoroantimonicAcid;
    public static RegistryObject<Material> TitaniumTrifluoride;
    public static RegistryObject<Material> CalciumPhosphide;
    public static RegistryObject<Material> IndiumPhosphide;
    public static RegistryObject<Material> BariumSulfide;
    public static RegistryObject<Material> TriniumSulfide;
    public static RegistryObject<Material> ZincSulfide;
    public static RegistryObject<Material> GalliumSulfide;
    public static RegistryObject<Material> AntimonyTrifluoride;
    public static RegistryObject<Material> EnrichedNaquadahSulfate;
    public static RegistryObject<Material> NaquadriaSulfate;
    public static RegistryObject<Material> Pyrochlore;

    /**
     * Organic chemistry
     */
    public static RegistryObject<Material> SiliconeRubber;
    public static RegistryObject<Material> RawRubber;
    public static RegistryObject<Material> RawStyreneButadieneRubber;
    public static RegistryObject<Material> StyreneButadieneRubber;
    public static RegistryObject<Material> PolyvinylAcetate;
    public static RegistryObject<Material> ReinforcedEpoxyResin;
    public static RegistryObject<Material> PolyvinylChloride;
    public static RegistryObject<Material> PolyphenyleneSulfide;
    public static RegistryObject<Material> GlycerylTrinitrate;
    public static RegistryObject<Material> Polybenzimidazole;
    public static RegistryObject<Material> Polydimethylsiloxane;
    public static RegistryObject<Material> Polyethylene;
    public static RegistryObject<Material> Epoxy;
    public static RegistryObject<Material> Polycaprolactam;
    public static RegistryObject<Material> Polytetrafluoroethylene;
    public static RegistryObject<Material> Sugar;
    public static RegistryObject<Material> Methane;
    public static RegistryObject<Material> Epichlorohydrin;
    public static RegistryObject<Material> Monochloramine;
    public static RegistryObject<Material> Chloroform;
    public static RegistryObject<Material> Cumene;
    public static RegistryObject<Material> Tetrafluoroethylene;
    public static RegistryObject<Material> Chloromethane;
    public static RegistryObject<Material> AllylChloride;
    public static RegistryObject<Material> Isoprene;
    public static RegistryObject<Material> Propane;
    public static RegistryObject<Material> Propene;
    public static RegistryObject<Material> Ethane;
    public static RegistryObject<Material> Butene;
    public static RegistryObject<Material> Butane;
    public static RegistryObject<Material> DissolvedCalciumAcetate;
    public static RegistryObject<Material> VinylAcetate;
    public static RegistryObject<Material> MethylAcetate;
    public static RegistryObject<Material> Ethenone;
    public static RegistryObject<Material> Tetranitromethane;
    public static RegistryObject<Material> Dimethylamine;
    public static RegistryObject<Material> Dimethylhydrazine;
    public static RegistryObject<Material> DinitrogenTetroxide;
    public static RegistryObject<Material> Dimethyldichlorosilane;
    public static RegistryObject<Material> Styrene;
    public static RegistryObject<Material> Butadiene;
    public static RegistryObject<Material> Dichlorobenzene;
    public static RegistryObject<Material> AceticAcid;
    public static RegistryObject<Material> Phenol;
    public static RegistryObject<Material> BisphenolA;
    public static RegistryObject<Material> VinylChloride;
    public static RegistryObject<Material> Ethylene;
    public static RegistryObject<Material> Benzene;
    public static RegistryObject<Material> Acetone;
    public static RegistryObject<Material> Glycerol;
    public static RegistryObject<Material> Methanol;
    public static RegistryObject<Material> Ethanol;
    public static RegistryObject<Material> Toluene;
    public static RegistryObject<Material> DiphenylIsophtalate;
    public static RegistryObject<Material> PhthalicAcid;
    public static RegistryObject<Material> Dimethylbenzene;
    public static RegistryObject<Material> Diaminobenzidine;
    public static RegistryObject<Material> Dichlorobenzidine;
    public static RegistryObject<Material> Nitrochlorobenzene;
    public static RegistryObject<Material> Chlorobenzene;
    public static RegistryObject<Material> Octane;
    public static RegistryObject<Material> EthylTertButylEther;
    public static RegistryObject<Material> Ethylbenzene;
    public static RegistryObject<Material> Naphthalene;
    public static RegistryObject<Material> Nitrobenzene;
    public static RegistryObject<Material> Cyclohexane;
    public static RegistryObject<Material> NitrosylChloride;
    public static RegistryObject<Material> CyclohexanoneOxime;
    public static RegistryObject<Material> Caprolactam;
    public static RegistryObject<Material> PlatinumSludgeResidue;
    public static RegistryObject<Material> PalladiumRaw;
    public static RegistryObject<Material> RarestMetalMixture;
    public static RegistryObject<Material> AmmoniumChloride;
    public static RegistryObject<Material> AcidicOsmiumSolution;
    public static RegistryObject<Material> RhodiumPlatedPalladium;
    public static RegistryObject<Material> Butyraldehyde;
    public static RegistryObject<Material> PolyvinylButyral;

    /**
     * Not possible to determine exact Components
     */
    public static RegistryObject<Material> WoodGas;
    public static RegistryObject<Material> WoodVinegar;
    public static RegistryObject<Material> WoodTar;
    public static RegistryObject<Material> CharcoalByproducts;
    public static RegistryObject<Material> Biomass;
    public static RegistryObject<Material> BioDiesel;
    public static RegistryObject<Material> FermentedBiomass;
    public static RegistryObject<Material> Creosote;
    public static RegistryObject<Material> Diesel;
    public static RegistryObject<Material> RocketFuel;
    public static RegistryObject<Material> Glue;
    public static RegistryObject<Material> Lubricant;
    public static RegistryObject<Material> McGuffium239;
    public static RegistryObject<Material> IndiumConcentrate;
    public static RegistryObject<Material> SeedOil;
    public static RegistryObject<Material> DrillingFluid;
    public static RegistryObject<Material> ConstructionFoam;

    public static RegistryObject<Material> Oil;
    public static RegistryObject<Material> OilHeavy;
    public static RegistryObject<Material> RawOil;
    public static RegistryObject<Material> OilLight;
    public static RegistryObject<Material> NaturalGas;
    public static RegistryObject<Material> SulfuricHeavyFuel;
    public static RegistryObject<Material> HeavyFuel;
    public static RegistryObject<Material> LightlyHydroCrackedHeavyFuel;
    public static RegistryObject<Material> SeverelyHydroCrackedHeavyFuel;
    public static RegistryObject<Material> LightlySteamCrackedHeavyFuel;
    public static RegistryObject<Material> SeverelySteamCrackedHeavyFuel;
    public static RegistryObject<Material> SulfuricLightFuel;
    public static RegistryObject<Material> LightFuel;
    public static RegistryObject<Material> LightlyHydroCrackedLightFuel;
    public static RegistryObject<Material> SeverelyHydroCrackedLightFuel;
    public static RegistryObject<Material> LightlySteamCrackedLightFuel;
    public static RegistryObject<Material> SeverelySteamCrackedLightFuel;
    public static RegistryObject<Material> SulfuricNaphtha;
    public static RegistryObject<Material> Naphtha;
    public static RegistryObject<Material> LightlyHydroCrackedNaphtha;
    public static RegistryObject<Material> SeverelyHydroCrackedNaphtha;
    public static RegistryObject<Material> LightlySteamCrackedNaphtha;
    public static RegistryObject<Material> SeverelySteamCrackedNaphtha;
    public static RegistryObject<Material> SulfuricGas;
    public static RegistryObject<Material> RefineryGas;
    public static RegistryObject<Material> LightlyHydroCrackedGas;
    public static RegistryObject<Material> SeverelyHydroCrackedGas;
    public static RegistryObject<Material> LightlySteamCrackedGas;
    public static RegistryObject<Material> SeverelySteamCrackedGas;
    public static RegistryObject<Material> HydroCrackedEthane;
    public static RegistryObject<Material> HydroCrackedEthylene;
    public static RegistryObject<Material> HydroCrackedPropene;
    public static RegistryObject<Material> HydroCrackedPropane;
    public static RegistryObject<Material> HydroCrackedButane;
    public static RegistryObject<Material> HydroCrackedButene;
    public static RegistryObject<Material> HydroCrackedButadiene;
    public static RegistryObject<Material> SteamCrackedEthane;
    public static RegistryObject<Material> SteamCrackedEthylene;
    public static RegistryObject<Material> SteamCrackedPropene;
    public static RegistryObject<Material> SteamCrackedPropane;
    public static RegistryObject<Material> SteamCrackedButane;
    public static RegistryObject<Material> SteamCrackedButene;
    public static RegistryObject<Material> SteamCrackedButadiene;
    public static RegistryObject<Material> LPG;

    public static RegistryObject<Material> RawGrowthMedium;
    public static RegistryObject<Material> SterileGrowthMedium;
    public static RegistryObject<Material> Bacteria;
    public static RegistryObject<Material> BacterialSludge;
    public static RegistryObject<Material> EnrichedBacterialSludge;
    public static RegistryObject<Material> Mutagen;
    public static RegistryObject<Material> GelatinMixture;
    public static RegistryObject<Material> RawGasoline;
    public static RegistryObject<Material> Gasoline;
    public static RegistryObject<Material> HighOctaneGasoline;
    public static RegistryObject<Material> CoalGas;
    public static RegistryObject<Material> CoalTar;
    public static RegistryObject<Material> Gunpowder;
    public static RegistryObject<Material> Oilsands;
    public static RegistryObject<Material> RareEarth;
    public static RegistryObject<Material> Stone;
    public static RegistryObject<Material> Lava;
    public static RegistryObject<Material> Glowstone;
    public static RegistryObject<Material> NetherStar;
    public static RegistryObject<Material> Endstone;
    public static RegistryObject<Material> Netherrack;
    public static RegistryObject<Material> CetaneBoostedDiesel;
    public static RegistryObject<Material> Collagen;
    public static RegistryObject<Material> Gelatin;
    public static RegistryObject<Material> Agar;
    public static RegistryObject<Material> Andesite;
    public static RegistryObject<Material> Milk;
    public static RegistryObject<Material> Cocoa;
    public static RegistryObject<Material> Wheat;
    public static RegistryObject<Material> Meat;
    public static RegistryObject<Material> Wood;
    public static RegistryObject<Material> TreatedWood;
    public static RegistryObject<Material> Paper;
    public static RegistryObject<Material> FishOil;
    public static RegistryObject<Material> RubySlurry;
    public static RegistryObject<Material> SapphireSlurry;
    public static RegistryObject<Material> GreenSapphireSlurry;
    public static RegistryObject<Material> DyeBlack;
    public static RegistryObject<Material> DyeRed;
    public static RegistryObject<Material> DyeGreen;
    public static RegistryObject<Material> DyeBrown;
    public static RegistryObject<Material> DyeBlue;
    public static RegistryObject<Material> DyePurple;
    public static RegistryObject<Material> DyeCyan;
    public static RegistryObject<Material> DyeLightGray;
    public static RegistryObject<Material> DyeGray;
    public static RegistryObject<Material> DyePink;
    public static RegistryObject<Material> DyeLime;
    public static RegistryObject<Material> DyeYellow;
    public static RegistryObject<Material> DyeLightBlue;
    public static RegistryObject<Material> DyeMagenta;
    public static RegistryObject<Material> DyeOrange;
    public static RegistryObject<Material> DyeWhite;

    public static RegistryObject<Material> ImpureEnrichedNaquadahSolution;
    public static RegistryObject<Material> EnrichedNaquadahSolution;
    public static RegistryObject<Material> AcidicEnrichedNaquadahSolution;
    public static RegistryObject<Material> EnrichedNaquadahWaste;
    public static RegistryObject<Material> ImpureNaquadriaSolution;
    public static RegistryObject<Material> NaquadriaSolution;
    public static RegistryObject<Material> AcidicNaquadriaSolution;
    public static RegistryObject<Material> NaquadriaWaste;
    public static RegistryObject<Material> Lapotron;
    public static RegistryObject<Material> UUMatter;

    /**
     * Second Degree Compounds
     */
    public static RegistryObject<Material> Glass;
    public static RegistryObject<Material> Perlite;
    public static RegistryObject<Material> Borax;
    public static RegistryObject<Material> Olivine;
    public static RegistryObject<Material> Opal;
    public static RegistryObject<Material> Amethyst;
    public static RegistryObject<Material> Lapis;
    public static RegistryObject<Material> Blaze;
    public static RegistryObject<Material> Apatite;
    public static RegistryObject<Material> BlackSteel;
    public static RegistryObject<Material> DamascusSteel;
    public static RegistryObject<Material> TungstenSteel;
    public static RegistryObject<Material> CobaltBrass;
    public static RegistryObject<Material> TricalciumPhosphate;
    public static RegistryObject<Material> GarnetRed;
    public static RegistryObject<Material> GarnetYellow;
    public static RegistryObject<Material> Marble;
    public static RegistryObject<Material> GraniteBlack;
    public static RegistryObject<Material> GraniteRed;
    public static RegistryObject<Material> VanadiumMagnetite;
    public static RegistryObject<Material> QuartzSand;
    public static RegistryObject<Material> Pollucite;
    public static RegistryObject<Material> Bentonite;
    public static RegistryObject<Material> FullersEarth;
    public static RegistryObject<Material> Pitchblende;
    public static RegistryObject<Material> Monazite;
    public static RegistryObject<Material> Mirabilite;
    public static RegistryObject<Material> Trona;
    public static RegistryObject<Material> Gypsum;
    public static RegistryObject<Material> Zeolite;
    public static RegistryObject<Material> Concrete;
    public static RegistryObject<Material> SteelMagnetic;
    public static RegistryObject<Material> VanadiumSteel;
    public static RegistryObject<Material> Potin;
    public static RegistryObject<Material> BorosilicateGlass;
    public static RegistryObject<Material> NaquadahAlloy;
    public static RegistryObject<Material> SulfuricNickelSolution;
    public static RegistryObject<Material> SulfuricCopperSolution;
    public static RegistryObject<Material> LeadZincSolution;
    public static RegistryObject<Material> NitrationMixture;
    public static RegistryObject<Material> DilutedSulfuricAcid;
    public static RegistryObject<Material> DilutedHydrochloricAcid;
    public static RegistryObject<Material> Flint;
    public static RegistryObject<Material> Air;
    public static RegistryObject<Material> LiquidAir;
    public static RegistryObject<Material> NetherAir;
    public static RegistryObject<Material> LiquidNetherAir;
    public static RegistryObject<Material> EnderAir;
    public static RegistryObject<Material> LiquidEnderAir;
    public static RegistryObject<Material> AquaRegia;
    public static RegistryObject<Material> SaltWater;
    public static RegistryObject<Material> Clay;
    public static RegistryObject<Material> Redstone;

    /**
     * Third Degree Materials
     */
    public static RegistryObject<Material> Electrotine;
    public static RegistryObject<Material> EnderEye;
    public static RegistryObject<Material> Diatomite;
    public static RegistryObject<Material> RedSteel;
    public static RegistryObject<Material> BlueSteel;
    public static RegistryObject<Material> Basalt;
    public static RegistryObject<Material> GraniticMineralSand;
    public static RegistryObject<Material> Redrock;
    public static RegistryObject<Material> GarnetSand;
    public static RegistryObject<Material> HSSG;
    public static RegistryObject<Material> IridiumMetalResidue;
    public static RegistryObject<Material> Granite;
    public static RegistryObject<Material> Brick;
    public static RegistryObject<Material> Fireclay;
    public static RegistryObject<Material> Diorite;

    /**
     * Fourth Degree Materials
     */
    public static RegistryObject<Material> RedAlloy;
    public static RegistryObject<Material> BlueAlloy;
    public static RegistryObject<Material> BasalticMineralSand;
    public static RegistryObject<Material> HSSE;
    public static RegistryObject<Material> HSSS;
}
