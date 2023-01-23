package net.nemezanevem.gregtech.api.unification.material.materials;

import net.nemezanevem.gregtech.api.unification.material.properties.GtMaterialProperties;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.OreProperty;

import static net.nemezanevem.gregtech.api.unification.material.GtMaterials.*;

public class MaterialFlagAddition {

    public static void register() {
        OreProperty oreProp = Aluminium.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Bauxite.get(), Bauxite.get(), Ilmenite.get(), Rutile.get());
        oreProp.setWashedIn(SodiumPersulfate.get());

        oreProp = Beryllium.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Emerald.get(), Emerald.get(), Thorium.get());

        oreProp = Cobalt.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(CobaltOxide.get(), Cobaltite.get());
        oreProp.setWashedIn(SodiumPersulfate.get());

        oreProp = Copper.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Cobalt.get(), Gold.get(), Nickel.get(), Gold.get());
        oreProp.setWashedIn(Mercury.get());

        oreProp = Gold.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Copper.get(), Nickel.get(), Silver.get());
        oreProp.setWashedIn(Mercury.get());

        oreProp = Iron.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Nickel.get(), Tin.get(), Tin.get(), Gold.get());
        oreProp.setWashedIn(SodiumPersulfate.get());

        oreProp = Lead.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Silver.get(), Sulfur.get());

        oreProp = Lithium.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Lithium.get());

        oreProp = Molybdenum.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Molybdenum.get());

        //oreProp = Magnesium.get().getProperty(GtMaterialProperties.ORE.get());
        //oreProp.setOreByProducts(Olivine.get());

        //oreProp = Manganese.get().getProperty(GtMaterialProperties.ORE.get());
        //oreProp.setOreByProducts(Chrome.get(), Iron.get());
        //oreProp.setSeparatedInto(Iron.get());

        oreProp = Neodymium.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(RareEarth.get());

        oreProp = Nickel.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Cobalt.get(), Iron.get(), Platinum.get());
        oreProp.setSeparatedInto(Iron.get());
        oreProp.setWashedIn(Mercury.get());

        oreProp = Platinum.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Nickel.get(), Nickel.get(), Cobalt.get(), Palladium.get());
        oreProp.setWashedIn(Mercury.get());

        oreProp = Plutonium239.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Uraninite.get(), Lead.get(), Uraninite.get());

        //oreProp = Silicon.get().getProperty(GtMaterialProperties.ORE.get());
        //oreProp.setOreByProducts(SiliconDioxide.get());

        oreProp = Silver.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Lead.get(), Sulfur.get(), Sulfur.get(), Gold.get());
        oreProp.setWashedIn(Mercury.get());

        oreProp = Sulfur.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Sulfur.get());

        oreProp = Thorium.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Uraninite.get(), Lead.get());

        oreProp = Tin.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Iron.get(), Zinc.get());
        oreProp.setSeparatedInto(Iron.get());
        oreProp.setWashedIn(SodiumPersulfate.get());

        //oreProp = Titanium.get().getProperty(GtMaterialProperties.ORE.get());
        //oreProp.setOreByProducts(Almandine.get());

        //oreProp = Tungsten.get().getProperty(GtMaterialProperties.ORE.get());
        //oreProp.setOreByProducts(Manganese.get(), Molybdenum.get());

        oreProp = Naquadah.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Sulfur.get(), Barite.get(), NaquadahEnriched.get());
        oreProp.setSeparatedInto(NaquadahEnriched.get());

        oreProp = CertusQuartz.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(NetherQuartz.get(), Barite.get());

        oreProp = Almandine.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(GarnetRed.get(), Aluminium.get());

        oreProp = Asbestos.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Diatomite.get(), Silicon.get(), Magnesium.get());

        oreProp = BlueTopaz.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Topaz.get());

        oreProp = BrownLimonite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Malachite.get(), YellowLimonite.get());
        oreProp.setSeparatedInto(Iron.get());
        oreProp.setDirectSmeltResult(Iron.get());

        oreProp = Calcite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Calcium.get(), Calcium.get(), Sodalite.get());

        oreProp = Cassiterite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Tin.get(), Bismuth.get());;
        oreProp.setDirectSmeltResult(Tin.get());

        oreProp = CassiteriteSand.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Tin.get());
        oreProp.setDirectSmeltResult(Tin.get());

        oreProp = Chalcopyrite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Pyrite.get(), Cobalt.get(), Cadmium.get(), Gold.get());
        oreProp.setWashedIn(Mercury.get());
        oreProp.setDirectSmeltResult(Copper.get());

        oreProp = Chromite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Iron.get(), Magnesium.get(), Chrome.get());
        oreProp.setSeparatedInto(Iron.get());

        oreProp = Cinnabar.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Redstone.get(), Sulfur.get(), Glowstone.get());

        oreProp = Coal.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Coal.get(), Coal.get(), Thorium.get());

        oreProp = Cobaltite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Sulfur.get(), Cobalt.get());
        oreProp.setWashedIn(SodiumPersulfate.get());
        oreProp.setDirectSmeltResult(Cobalt.get());

        oreProp = Cooperite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Nickel.get(), Nickel.get(), Cobalt.get(), Palladium.get());
        oreProp.setWashedIn(Mercury.get());

        oreProp = Diamond.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Graphite.get());

        oreProp = Emerald.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Beryllium.get(), Aluminium.get());

        oreProp = Galena.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Sulfur.get(), Silver.get());
        oreProp.setWashedIn(Mercury.get());
        oreProp.setDirectSmeltResult(Lead.get());

        oreProp = Garnierite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Iron.get(), Nickel.get());
        oreProp.setDirectSmeltResult(Nickel.get());

        oreProp = GreenSapphire.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Aluminium.get(), Sapphire.get());

        oreProp = Grossular.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(GarnetYellow.get(), Calcium.get());

        oreProp = Ilmenite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Iron.get(), Rutile.get());
        oreProp.setSeparatedInto(Iron.get());

        oreProp = Bauxite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Grossular.get(), Rutile.get(), Gallium.get());
        oreProp.setWashedIn(SodiumPersulfate.get());

        oreProp = Lazurite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Sodalite.get(), Lapis.get());

        oreProp = Magnesite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Magnesium.get(), Magnesium.get(), Cobaltite.get());
        oreProp.setDirectSmeltResult(Magnesium.get());

        oreProp = Magnetite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Iron.get(), Gold.get());
        oreProp.setSeparatedInto(Gold.get());
        oreProp.setWashedIn(Mercury.get());
        oreProp.setDirectSmeltResult(Iron.get());

        oreProp = Molybdenite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Molybdenum.get(), Sulfur.get(), Quartzite.get());
        oreProp.setDirectSmeltResult(Molybdenum.get());

        oreProp = Pyrite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Sulfur.get(), TricalciumPhosphate.get(), Iron.get());
        oreProp.setSeparatedInto(Iron.get());
        oreProp.setDirectSmeltResult(Iron.get());

        oreProp = Pyrolusite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Manganese.get(), Tantalite.get(), Niobium.get());
        oreProp.setDirectSmeltResult(Manganese.get());

        oreProp = Pyrope.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(GarnetRed.get(), Magnesium.get());

        oreProp = Realgar.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Sulfur.get(), Antimony.get(), Barite.get());

        oreProp = RockSalt.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Salt.get(), Borax.get());

        oreProp = Ruby.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Chrome.get(), GarnetRed.get(), Chrome.get());

        oreProp = Salt.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(RockSalt.get(), Borax.get());

        oreProp = Saltpeter.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Saltpeter.get(), Potassium.get(), Salt.get());

        oreProp = Sapphire.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Aluminium.get(), GreenSapphire.get());

        oreProp = Scheelite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Manganese.get(), Molybdenum.get(), Calcium.get());

        oreProp = Sodalite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Lazurite.get(), Lapis.get());

        oreProp = Tantalite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Manganese.get(), Niobium.get(), Tantalum.get());

        oreProp = Spessartine.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(GarnetRed.get(), Manganese.get());

        oreProp = Sphalerite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(GarnetYellow.get(), Gallium.get(), Cadmium.get(), Zinc.get());
        oreProp.setWashedIn(SodiumPersulfate.get());
        oreProp.setDirectSmeltResult(Zinc.get());

        oreProp = Stibnite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(AntimonyTrioxide.get(), Antimony.get(), Cinnabar.get());
        oreProp.setWashedIn(SodiumPersulfate.get());
        oreProp.setDirectSmeltResult(Antimony.get());

        oreProp = Tetrahedrite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Antimony.get(), Zinc.get(), Cadmium.get());
        oreProp.setWashedIn(SodiumPersulfate.get());
        oreProp.setDirectSmeltResult(Copper.get());

        oreProp = Topaz.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(BlueTopaz.get());

        oreProp = Tungstate.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Manganese.get(), Silver.get(), Lithium.get());
        oreProp.setWashedIn(Mercury.get());

        oreProp = Uraninite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Uraninite.get(), Thorium.get(), Silver.get());

        oreProp = YellowLimonite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Nickel.get(), BrownLimonite.get(), CobaltOxide.get());
        oreProp.setSeparatedInto(Iron.get());
        oreProp.setWashedIn(SodiumPersulfate.get());
        oreProp.setDirectSmeltResult(Iron.get());

        oreProp = NetherQuartz.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Quartzite.get());

        oreProp = Quartzite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(CertusQuartz.get(), Barite.get());

        oreProp = Graphite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Carbon.get());

        oreProp = Bornite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Pyrite.get(), Cobalt.get(), Cadmium.get(), Gold.get());
        oreProp.setWashedIn(Mercury.get());
        oreProp.setDirectSmeltResult(Copper.get());

        oreProp = Chalcocite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Sulfur.get(), Massicot.get(), Silver.get());
        oreProp.setDirectSmeltResult(Copper.get());

        oreProp = Bastnasite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Neodymium.get(), RareEarth.get());;
        oreProp.setSeparatedInto(Neodymium.get());

        oreProp = Pentlandite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Iron.get(), Sulfur.get(), Cobalt.get());
        oreProp.setSeparatedInto(Iron.get());
        oreProp.setWashedIn(SodiumPersulfate.get());
        oreProp.setDirectSmeltResult(Nickel.get());

        oreProp = Spodumene.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Aluminium.get(), Lithium.get());

        oreProp = Lepidolite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Lithium.get(), Caesium.get(), Boron.get());

        oreProp = GlauconiteSand.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Sodium.get(), Aluminium.get(), Iron.get());
        oreProp.setSeparatedInto(Iron.get());

        oreProp = Malachite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(BrownLimonite.get(), Calcite.get(), Zincite.get());
        oreProp.setWashedIn(SodiumPersulfate.get());
        oreProp.setDirectSmeltResult(Copper.get());

        oreProp = Olivine.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Pyrope.get(), Magnesium.get(), Manganese.get());

        oreProp = Opal.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Opal.get());

        oreProp = Amethyst.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Amethyst.get());

        oreProp = Lapis.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Lazurite.get(), Sodalite.get(), Pyrite.get());

        oreProp = Apatite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(TricalciumPhosphate.get(), Phosphate.get(), Pyrochlore.get());

        oreProp = TricalciumPhosphate.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Apatite.get(), Phosphate.get(), Pyrochlore.get());

        oreProp = GarnetRed.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Spessartine.get(), Pyrope.get(), Almandine.get());

        oreProp = GarnetYellow.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Andradite.get(), Grossular.get(), Uvarovite.get());

        oreProp = VanadiumMagnetite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Magnetite.get(), Magnetite.get(), Vanadium.get());
        oreProp.setSeparatedInto(Gold.get());

        oreProp = Pollucite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Caesium.get(), Aluminium.get(), Potassium.get());

        oreProp = Bentonite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Aluminium.get(), Calcium.get(), Magnesium.get());

        oreProp = FullersEarth.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Aluminium.get(), Silicon.get(), Magnesium.get());

        oreProp = Pitchblende.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Thorium.get(), Uraninite.get(), Lead.get());

        oreProp = Monazite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Thorium.get(), Neodymium.get(), RareEarth.get());;
        oreProp.setSeparatedInto(Neodymium.get());

        oreProp = Redstone.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Cinnabar.get(), RareEarth.get(), Glowstone.get());

        oreProp = Diatomite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(BandedIron.get(), Sapphire.get());

        oreProp = GraniticMineralSand.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(GraniteBlack.get(), Magnetite.get());
        oreProp.setSeparatedInto(Gold.get());
        oreProp.setDirectSmeltResult(Iron.get());

        oreProp = GarnetSand.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(GarnetRed.get(), GarnetYellow.get());

        oreProp = BasalticMineralSand.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Basalt.get(), Magnetite.get());
        oreProp.setSeparatedInto(Gold.get());
        oreProp.setDirectSmeltResult(Iron.get());

        oreProp = BandedIron.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Magnetite.get(), Calcium.get(), Magnesium.get());
        oreProp.setSeparatedInto(Iron.get());
        oreProp.setDirectSmeltResult(Iron.get());

        oreProp = Wulfenite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Iron.get(), Manganese.get(), Manganese.get(), Lead.get());

        oreProp = Soapstone.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(SiliconDioxide.get(), Magnesium.get(), Calcite.get(), Talc.get());

        oreProp = Kyanite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Talc.get(), Aluminium.get(), Silicon.get());

        oreProp = Gypsum.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Sulfur.get(), Calcium.get(), Salt.get());

        oreProp = Talc.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Clay.get(), Carbon.get(), Clay.get());

        oreProp = Powellite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Iron.get(), Potassium.get(), Molybdenite.get());

        oreProp = Trona.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Sodium.get(), SodaAsh.get(), SodaAsh.get());;

        oreProp = Mica.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Potassium.get(), Aluminium.get());

        oreProp = Zeolite.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Calcium.get(), Silicon.get(), Aluminium.get());

        oreProp = Electrotine.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Redstone.get(), Electrum.get(), Diamond.get());

        oreProp = Pyrochlore.get().getProperty(GtMaterialProperties.ORE.get());
        oreProp.setOreByProducts(Apatite.get(), Calcium.get(), Niobium.get());
    }
}
