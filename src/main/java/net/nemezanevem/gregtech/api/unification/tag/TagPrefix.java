package net.nemezanevem.gregtech.api.unification.tag;

import com.google.common.base.Preconditions;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.properties.GtMaterialProperties;
import net.nemezanevem.gregtech.api.unification.material.properties.info.GtMaterialIconTypes;
import net.nemezanevem.gregtech.api.unification.material.properties.info.MaterialIconType;
import net.nemezanevem.gregtech.api.unification.stack.MaterialStack;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;

import static net.nemezanevem.gregtech.api.unification.tag.TagPrefix.Conditions.hasIngotProperty;
import static net.nemezanevem.gregtech.api.unification.tag.TagPrefix.Conditions.hasOreProperty;
import static net.nemezanevem.gregtech.api.unification.tag.TagPrefix.Flags.ENABLE_UNIFICATION;
import static net.nemezanevem.gregtech.api.unification.tag.TagPrefix.Flags.SELF_REFERENCING;
import static net.nemezanevem.gregtech.api.util.GtValues.M;

public class TagPrefix {

    private final static Map<String, TagPrefix> PREFIXES = new HashMap<>();
    private final static AtomicInteger idCounter = new AtomicInteger(0);

    // Regular Ore Prefix. Ore -> Material is a Oneway Operation! Introduced by Eloraam
    public static final TagPrefix ore = new TagPrefix("ore", -1, null, GtMaterialIconTypes.ore.get(), ENABLE_UNIFICATION, hasOreProperty);
    public static final TagPrefix oreGranite = new TagPrefix("ore_granite", -1, null, GtMaterialIconTypes.ore.get(), ENABLE_UNIFICATION, hasOreProperty);
    public static final TagPrefix oreDiorite = new TagPrefix("ore_diorite", -1, null, GtMaterialIconTypes.ore.get(), ENABLE_UNIFICATION, hasOreProperty);
    public static final TagPrefix oreAndesite = new TagPrefix("oreAndesite", -1, null, GtMaterialIconTypes.ore.get(), ENABLE_UNIFICATION, hasOreProperty);
    public static final TagPrefix oreBlackgranite = new TagPrefix("oreBlackgranite", -1, null, GtMaterialIconTypes.ore.get(), ENABLE_UNIFICATION, hasOreProperty);
    public static final TagPrefix oreRedgranite = new TagPrefix("oreRedgranite", -1, null, GtMaterialIconTypes.ore.get(), ENABLE_UNIFICATION, hasOreProperty);
    public static final TagPrefix oreMarble = new TagPrefix("oreMarble", -1, null, GtMaterialIconTypes.ore.get(), ENABLE_UNIFICATION, hasOreProperty);
    public static final TagPrefix oreBasalt = new TagPrefix("oreBasalt", -1, null, GtMaterialIconTypes.ore.get(), ENABLE_UNIFICATION, hasOreProperty);

    // In case of an Sand-Ores Mod. Ore -> Material is a Oneway Operation!
    public static final TagPrefix oreSand = new TagPrefix("oreSand", -1, null, GtMaterialIconTypes.ore.get(), ENABLE_UNIFICATION, null);
    public static final TagPrefix oreRedSand = new TagPrefix("oreRedSand", -1, null, GtMaterialIconTypes.ore.get(), ENABLE_UNIFICATION, null);

    // Prefix of the Nether-Ores Mod. Causes Ores to double. Ore -> Material is a Oneway Operation!
    public static final TagPrefix oreNetherrack = new TagPrefix("oreNetherrack", -1, null, GtMaterialIconTypes.ore.get(), ENABLE_UNIFICATION, hasOreProperty);
    // In case of an End-Ores Mod. Ore -> Material is a Oneway Operation!
    public static final TagPrefix oreEndstone = new TagPrefix("oreEndstone", -1, null, GtMaterialIconTypes.ore.get(), ENABLE_UNIFICATION, hasOreProperty);

    public static final TagPrefix crushedCentrifuged = new TagPrefix("crushedCentrifuged", -1, null, MaterialIconType.crushedCentrifuged, ENABLE_UNIFICATION, hasOreProperty);
    public static final TagPrefix crushedPurified = new TagPrefix("crushedPurified", -1, null, MaterialIconType.crushedPurified, ENABLE_UNIFICATION, hasOreProperty);
    public static final TagPrefix crushed = new TagPrefix("crushed", -1, null, MaterialIconType.crushed, ENABLE_UNIFICATION, hasOreProperty, mat -> Collections.singletonList(I18n.format("metaitem.crushed.tooltip.purify")));

    // Introduced by Mekanism
    public static final TagPrefix shard = new TagPrefix("shard", -1, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix clump = new TagPrefix("clump", -1, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix reduced = new TagPrefix("reduced", -1, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix crystalline = new TagPrefix("crystalline", -1, null, null, ENABLE_UNIFICATION, null);

    public static final TagPrefix cleanGravel = new TagPrefix("cleanGravel", -1, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix dirtyGravel = new TagPrefix("dirtyGravel", -1, null, null, ENABLE_UNIFICATION, null);

    // A hot Ingot, which has to be cooled down by a Vacuum Freezer.
    public static final TagPrefix ingotHot = new TagPrefix("ingotHot", M, null, MaterialIconType.ingotHot, ENABLE_UNIFICATION, hasBlastProperty.and(mat -> mat.getProperty(PropertyKey.BLAST).getBlastTemperature() > 1750));
    // A regular Ingot. Introduced by Eloraam
    public static final TagPrefix ingot = new TagPrefix("ingot", M, null, MaterialIconType.ingot, ENABLE_UNIFICATION, hasIngotProperty);

    // A regular Gem worth one Dust. Introduced by Eloraam
    public static final TagPrefix gem = new TagPrefix("gem", M, null, MaterialIconType.gem, ENABLE_UNIFICATION, hasGemProperty);
    // A regular Gem worth one small Dust. Introduced by TerraFirmaCraft
    public static final TagPrefix gemChipped = new TagPrefix("gemChipped", M / 4, null, MaterialIconType.gemChipped, ENABLE_UNIFICATION, hasGemProperty.and(unused -> ConfigHolder.recipes.generateLowQualityGems));
    // A regular Gem worth two small Dusts. Introduced by TerraFirmaCraft
    public static final TagPrefix gemFlawed = new TagPrefix("gemFlawed", M / 2, null, MaterialIconType.gemFlawed, ENABLE_UNIFICATION, hasGemProperty.and(unused -> ConfigHolder.recipes.generateLowQualityGems));
    // A regular Gem worth two Dusts. Introduced by TerraFirmaCraft
    public static final TagPrefix gemFlawless = new TagPrefix("gemFlawless", M * 2, null, MaterialIconType.gemFlawless, ENABLE_UNIFICATION, hasGemProperty);
    // A regular Gem worth four Dusts. Introduced by TerraFirmaCraft
    public static final TagPrefix gemExquisite = new TagPrefix("gemExquisite", M * 4, null, MaterialIconType.gemExquisite, ENABLE_UNIFICATION, hasGemProperty);

    // 1/4th of a Dust.
    public static final TagPrefix dustSmall = new TagPrefix("dustSmall", M / 4, null, MaterialIconType.dustSmall, ENABLE_UNIFICATION, hasDustProperty);
    // 1/9th of a Dust.
    public static final TagPrefix dustTiny = new TagPrefix("dustTiny", M / 9, null, MaterialIconType.dustTiny, ENABLE_UNIFICATION, hasDustProperty);
    // Dust with impurities. 1 Unit of Main Material and 1/9 - 1/4 Unit of secondary Material
    public static final TagPrefix dustImpure = new TagPrefix("dustImpure", M, null, MaterialIconType.dustImpure, ENABLE_UNIFICATION, hasOreProperty, mat -> Collections.singletonList(I18n.format("metaitem.dust.tooltip.purify")));
    // Pure Dust worth of one Ingot or Gem. Introduced by Alblaka.
    public static final TagPrefix dustPure = new TagPrefix("dustPure", M, null, MaterialIconType.dustPure, ENABLE_UNIFICATION, hasOreProperty, mat -> Collections.singletonList(I18n.format("metaitem.dust.tooltip.purify")));
    public static final TagPrefix dust = new TagPrefix("dust", M, null, MaterialIconType.dust, ENABLE_UNIFICATION, hasDustProperty);

    // A Nugget. Introduced by Eloraam
    public static final TagPrefix nugget = new TagPrefix("nugget", M / 9, null, MaterialIconType.nugget, ENABLE_UNIFICATION, hasIngotProperty);

    // 9 Plates combined in one Item.
    public static final TagPrefix plateDense = new TagPrefix("plateDense", M * 9, null, MaterialIconType.plateDense, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_DENSE) && !mat.hasFlag(NO_SMASHING));
    // 2 Plates combined in one Item
    public static final TagPrefix plateDouble = new TagPrefix("plateDouble", M * 2, null, MaterialIconType.plateDouble, ENABLE_UNIFICATION, hasIngotProperty.and(mat -> mat.hasFlag(GENERATE_PLATE) && !mat.hasFlag(NO_SMASHING)));
    // Regular Plate made of one Ingot/Dust. Introduced by Calclavia
    public static final TagPrefix plate = new TagPrefix("plate", M, null, MaterialIconType.plate, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_PLATE));

    // Round made of 1 Nugget
    public static final TagPrefix round = new TagPrefix("round", M / 9, null, MaterialIconType.round, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_ROUND));
    // Foil made of 1/4 Ingot/Dust.
    public static final TagPrefix foil = new TagPrefix("foil", M / 4, null, MaterialIconType.foil, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_FOIL));

    // Stick made of an Ingot.
    public static final TagPrefix stickLong = new TagPrefix("stickLong", M, null, MaterialIconType.stickLong, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_LONG_ROD));
    // Stick made of half an Ingot. Introduced by Eloraam
    public static final TagPrefix stick = new TagPrefix("stick", M / 2, null, MaterialIconType.stick, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_ROD));

    // consisting out of 1/8 Ingot or 1/4 Stick.
    public static final TagPrefix bolt = new TagPrefix("bolt", M / 8, null, MaterialIconType.bolt, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_BOLT_SCREW));
    // consisting out of 1/9 Ingot.
    public static final TagPrefix screw = new TagPrefix("screw", M / 9, null, MaterialIconType.screw, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_BOLT_SCREW));
    // consisting out of 1/2 Stick.
    public static final TagPrefix ring = new TagPrefix("ring", M / 4, null, MaterialIconType.ring, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_RING));
    // consisting out of 1 Fine Wire.
    public static final TagPrefix springSmall = new TagPrefix("springSmall", M / 4, null, MaterialIconType.springSmall, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_SPRING_SMALL) && !mat.hasFlag(NO_SMASHING));
    // consisting out of 2 Sticks.
    public static final TagPrefix spring = new TagPrefix("spring", M, null, MaterialIconType.spring, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_SPRING) && !mat.hasFlag(NO_SMASHING));
    // consisting out of 1/8 Ingot or 1/4 Wire.
    public static final TagPrefix wireFine = new TagPrefix("wireFine", M / 8, null, MaterialIconType.wireFine, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_FINE_WIRE));
    // consisting out of 4 Plates, 1 Ring and 1 Screw.
    public static final TagPrefix rotor = new TagPrefix("rotor", M * 4, null, MaterialIconType.rotor, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_ROTOR));
    public static final TagPrefix gearSmall = new TagPrefix("gearSmall", M, null, MaterialIconType.gearSmall, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_SMALL_GEAR));
    // Introduced by me because BuildCraft has ruined the gear Prefix...
    public static final TagPrefix gear = new TagPrefix("gear", M * 4, null, MaterialIconType.gear, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_GEAR));
    // 3/4 of a Plate or Gem used to shape a Lens. Normally only used on Transparent Materials.
    public static final TagPrefix lens = new TagPrefix("lens", (M * 3) / 4, null, MaterialIconType.lens, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_LENS));

    // made of 4 Ingots.
    public static final TagPrefix toolHeadBuzzSaw = new TagPrefix("toolHeadBuzzSaw", M * 4, null, MaterialIconType.toolHeadBuzzSaw, ENABLE_UNIFICATION, hasNoCraftingToolProperty.and(mat -> mat.hasFlag(GENERATE_PLATE)));
    // made of 1 Ingots.
    public static final TagPrefix toolHeadScrewdriver = new TagPrefix("toolHeadScrewdriver", M, null, MaterialIconType.toolHeadScrewdriver, ENABLE_UNIFICATION, hasNoCraftingToolProperty.and(mat -> mat.hasFlag(GENERATE_LONG_ROD)));
    // made of 4 Ingots.
    public static final TagPrefix toolHeadDrill = new TagPrefix("toolHeadDrill", M * 4, null, MaterialIconType.toolHeadDrill, ENABLE_UNIFICATION, hasToolProperty.and(mat -> mat.hasFlag(GENERATE_PLATE)));
    // made of 2 Ingots.
    public static final TagPrefix toolHeadChainsaw = new TagPrefix("toolHeadChainsaw", M * 2, null, MaterialIconType.toolHeadChainsaw, ENABLE_UNIFICATION, hasNoCraftingToolProperty.and(mat -> mat.hasFlag(GENERATE_PLATE)));
    // made of 4 Ingots.
    public static final TagPrefix toolHeadWrench = new TagPrefix("toolHeadWrench", M * 4, null, MaterialIconType.toolHeadWrench, ENABLE_UNIFICATION, hasNoCraftingToolProperty.and(mat -> mat.hasFlag(GENERATE_PLATE)));
    // made of 5 Ingots.
    public static final TagPrefix turbineBlade = new TagPrefix("turbineBlade", M * 10, null, MaterialIconType.turbineBlade, ENABLE_UNIFICATION, hasRotorProperty.and(m -> m.hasFlags(GENERATE_BOLT_SCREW, GENERATE_PLATE) && !m.hasProperty(PropertyKey.GEM)));

    public static final TagPrefix paneGlass = new TagPrefix("paneGlass", -1, MarkerMaterials.Color.Colorless, null, SELF_REFERENCING, null);
    public static final TagPrefix blockGlass = new TagPrefix("blockGlass", -1, MarkerMaterials.Color.Colorless, null, SELF_REFERENCING, null);

    // Storage Block consisting out of 9 Ingots/Gems/Dusts. Introduced by CovertJaguar
    public static final TagPrefix block = new TagPrefix("block", M * 9, null, MaterialIconType.block, ENABLE_UNIFICATION, null);

    // Prefix used for Logs. Usually as "logWood". Introduced by Eloraam
    public static final TagPrefix log = new TagPrefix("log", -1, null, null, 0, null);
    // Prefix for Planks. Usually "plankWood". Introduced by Eloraam
    public static final TagPrefix plank = new TagPrefix("plank", -1, null, null, 0, null);

    // Prefix to determine which kind of Rock this is.
    public static final TagPrefix stone = new TagPrefix("stone", -1, Materials.Stone, null, SELF_REFERENCING, null);

    public static final TagPrefix frameGt = new TagPrefix("frameGt", M * 2, null, null, ENABLE_UNIFICATION, material -> material.hasFlag(GENERATE_FRAME));

    public static final TagPrefix pipeTinyFluid = new TagPrefix("pipeTinyFluid", M / 2, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix pipeSmallFluid = new TagPrefix("pipeSmallFluid", M, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix pipeNormalFluid = new TagPrefix("pipeNormalFluid", M * 3, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix pipeLargeFluid = new TagPrefix("pipeLargeFluid", M * 6, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix pipeHugeFluid = new TagPrefix("pipeHugeFluid", M * 12, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix pipeQuadrupleFluid = new TagPrefix("pipeQuadrupleFluid", M * 4, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix pipeNonupleFluid = new TagPrefix("pipeNonupleFluid", M * 9, null, null, ENABLE_UNIFICATION, null);

    public static final TagPrefix pipeTinyItem = new TagPrefix("pipeTinyItem", M / 2, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix pipeSmallItem = new TagPrefix("pipeSmallItem", M, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix pipeNormalItem = new TagPrefix("pipeNormalItem", M * 3, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix pipeLargeItem = new TagPrefix("pipeLargeItem", M * 6, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix pipeHugeItem = new TagPrefix("pipeHugeItem", M * 12, null, null, ENABLE_UNIFICATION, null);

    public static final TagPrefix pipeSmallRestrictive = new TagPrefix("pipeSmallRestrictive", M, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix pipeNormalRestrictive = new TagPrefix("pipeNormalRestrictive", M * 3, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix pipeLargeRestrictive = new TagPrefix("pipeLargeRestrictive", M * 6, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix pipeHugeRestrictive = new TagPrefix("pipeHugeRestrictive", M * 12, null, null, ENABLE_UNIFICATION, null);

    public static final TagPrefix wireGtHex = new TagPrefix("wireGtHex", M * 8, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix wireGtOctal = new TagPrefix("wireGtOctal", M * 4, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix wireGtQuadruple = new TagPrefix("wireGtQuadruple", M * 2, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix wireGtDouble = new TagPrefix("wireGtDouble", M, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix wireGtSingle = new TagPrefix("wireGtSingle", M / 2, null, null, ENABLE_UNIFICATION, null);

    public static final TagPrefix cableGtHex = new TagPrefix("cableGtHex", M * 8, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix cableGtOctal = new TagPrefix("cableGtOctal", M * 4, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix cableGtQuadruple = new TagPrefix("cableGtQuadruple", M * 2, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix cableGtDouble = new TagPrefix("cableGtDouble", M, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix cableGtSingle = new TagPrefix("cableGtSingle", M / 2, null, null, ENABLE_UNIFICATION, null);

    // Special Prefix used mainly for the Crafting Handler.
    public static final TagPrefix craftingLens = new TagPrefix("craftingLens", -1, null, null, 0, null);
    // Used for the 16 dyes. Introduced by Eloraam
    public static final TagPrefix dye = new TagPrefix("dye", -1, null, null, 0, null);

    /**
     * Electric Components.
     *
     * @see MarkerMaterials.Tier
     */
    // Introduced by Calclavia
    public static final TagPrefix battery = new TagPrefix("battery", -1, null, null, 0, null);
    // Introduced by Calclavia
    public static final TagPrefix circuit = new TagPrefix("circuit", -1, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix component = new TagPrefix("component", -1, null, null, ENABLE_UNIFICATION, null);

    public static class Flags {
        public static final long ENABLE_UNIFICATION = 1;
        public static final long SELF_REFERENCING = 1 << 1;
    }

    public static class Conditions {
        public static final Predicate<Material> hasToolProperty = mat -> mat.hasProperty(PropertyKey.TOOL);
        public static final Predicate<Material> hasNoCraftingToolProperty = hasToolProperty.and(mat -> !mat.getProperty(PropertyKey.TOOL).getShouldIgnoreCraftingTools());
        public static final Predicate<Material> hasOreProperty = mat -> mat.hasProperty(GtMaterialProperties.ORE.get());
        public static final Predicate<Material> hasGemProperty = mat -> mat.hasProperty(PropertyKey.GEM);
        public static final Predicate<Material> hasDustProperty = mat -> mat.hasProperty(PropertyKey.DUST);
        public static final Predicate<Material> hasIngotProperty = mat -> mat.hasProperty(PropertyKey.INGOT);
        public static final Predicate<Material> hasBlastProperty = mat -> mat.hasProperty(PropertyKey.BLAST);
        public static final Predicate<Material> hasRotorProperty = mat -> mat.hasProperty(PropertyKey.ROTOR);
    }

    static {
        ingotHot.heatDamageFunction = (temp) -> ((temp - 1750) / 1000.0F) + 2;
        gemFlawless.maxStackSize = 32;
        gemExquisite.maxStackSize = 16;

        plateDouble.maxStackSize = 32;
        plateDense.maxStackSize = 7;
        rotor.maxStackSize = 16;
        gear.maxStackSize = 16;

        toolHeadBuzzSaw.maxStackSize = 16;
        toolHeadScrewdriver.maxStackSize = 16;
        toolHeadDrill.maxStackSize = 16;
        toolHeadChainsaw.maxStackSize = 16;
        toolHeadWrench.maxStackSize = 16;

        craftingLens.setMarkerPrefix(true);
        dye.setMarkerPrefix(true);
        battery.setMarkerPrefix(true);
        circuit.setMarkerPrefix(true);

        gemExquisite.setIgnored(Materials.Sugar);

        gemFlawless.setIgnored(Materials.Sugar);

        gem.setIgnored(Materials.Diamond);
        gem.setIgnored(Materials.Emerald);
        gem.setIgnored(Materials.Lapis);
        gem.setIgnored(Materials.NetherQuartz);
        gem.setIgnored(Materials.Coal);

        excludeAllGems(Materials.Charcoal);
        excludeAllGems(Materials.NetherStar);
        excludeAllGems(Materials.EnderPearl);
        excludeAllGems(Materials.EnderEye);
        excludeAllGems(Materials.Flint);
        excludeAllGemsButNormal(Materials.Lapotron);

        dust.setIgnored(Materials.Redstone);
        dust.setIgnored(Materials.Glowstone);
        dust.setIgnored(Materials.Gunpowder);
        dust.setIgnored(Materials.Sugar);
        dust.setIgnored(Materials.Bone);
        dust.setIgnored(Materials.Blaze);

        stick.setIgnored(Materials.Wood);
        stick.setIgnored(Materials.Bone);
        stick.setIgnored(Materials.Blaze);
        stick.setIgnored(Materials.Paper);

        ingot.setIgnored(Materials.Iron);
        ingot.setIgnored(Materials.Gold);
        ingot.setIgnored(Materials.Wood);
        ingot.setIgnored(Materials.TreatedWood);
        ingot.setIgnored(Materials.Paper);

        nugget.setIgnored(Materials.Wood);
        nugget.setIgnored(Materials.TreatedWood);
        nugget.setIgnored(Materials.Gold);
        nugget.setIgnored(Materials.Paper);
        nugget.setIgnored(Materials.Iron);
        plate.setIgnored(Materials.Paper);

        block.setIgnored(Materials.Iron);
        block.setIgnored(Materials.Gold);
        block.setIgnored(Materials.Lapis);
        block.setIgnored(Materials.Emerald);
        block.setIgnored(Materials.Redstone);
        block.setIgnored(Materials.Diamond);
        block.setIgnored(Materials.Coal);
        block.setIgnored(Materials.Glass);
        block.setIgnored(Materials.Marble);
        block.setIgnored(Materials.GraniteRed);
        block.setIgnored(Materials.Stone);
        block.setIgnored(Materials.Glowstone);
        block.setIgnored(Materials.Endstone);
        block.setIgnored(Materials.Wheat);
        block.setIgnored(Materials.Oilsands);
        block.setIgnored(Materials.Wood);
        block.setIgnored(Materials.TreatedWood);
        block.setIgnored(Materials.RawRubber);
        block.setIgnored(Materials.Clay);
        block.setIgnored(Materials.Brick);
        block.setIgnored(Materials.Bone);
        block.setIgnored(Materials.NetherQuartz);
        block.setIgnored(Materials.Ice);
        block.setIgnored(Materials.Netherrack);
        block.setIgnored(Materials.Concrete);
        block.setIgnored(Materials.Blaze);
        block.setIgnored(Materials.Lapotron);

        ore.addSecondaryMaterial(new MaterialStack(Materials.Stone, dust.materialAmount));
        oreNetherrack.addSecondaryMaterial(new MaterialStack(Materials.Netherrack, dust.materialAmount));
        oreEndstone.addSecondaryMaterial(new MaterialStack(Materials.Endstone, dust.materialAmount));

        if (ConfigHolder.worldgen.allUniqueStoneTypes) {
            oreGranite.addSecondaryMaterial(new MaterialStack(Materials.Granite, dust.materialAmount));
            oreDiorite.addSecondaryMaterial(new MaterialStack(Materials.Diorite, dust.materialAmount));
            oreAndesite.addSecondaryMaterial(new MaterialStack(Materials.Andesite, dust.materialAmount));
            oreRedgranite.addSecondaryMaterial(new MaterialStack(Materials.GraniteRed, dust.materialAmount));
            oreBlackgranite.addSecondaryMaterial(new MaterialStack(Materials.GraniteBlack, dust.materialAmount));
            oreBasalt.addSecondaryMaterial(new MaterialStack(Materials.Basalt, dust.materialAmount));
            oreMarble.addSecondaryMaterial(new MaterialStack(Materials.Marble, dust.materialAmount));
            oreSand.addSecondaryMaterial(new MaterialStack(Materials.SiliconDioxide, dustTiny.materialAmount));
            oreRedSand.addSecondaryMaterial(new MaterialStack(Materials.SiliconDioxide, dustTiny.materialAmount));
        }

        crushed.addSecondaryMaterial(new MaterialStack(Materials.Stone, dust.materialAmount));

        toolHeadDrill.addSecondaryMaterial(new MaterialStack(Materials.Steel, plate.materialAmount * 4));
        toolHeadChainsaw.addSecondaryMaterial(new MaterialStack(Materials.Steel, plate.materialAmount * 4 + ring.materialAmount * 2));
        toolHeadWrench.addSecondaryMaterial(new MaterialStack(Materials.Steel, ring.materialAmount + screw.materialAmount * 2));

        pipeTinyFluid.setIgnored(Materials.Wood);
        pipeHugeFluid.setIgnored(Materials.Wood);
        pipeQuadrupleFluid.setIgnored(Materials.Wood);
        pipeNonupleFluid.setIgnored(Materials.Wood);
        pipeTinyFluid.setIgnored(Materials.TreatedWood);
        pipeHugeFluid.setIgnored(Materials.TreatedWood);
        pipeQuadrupleFluid.setIgnored(Materials.TreatedWood);
        pipeNonupleFluid.setIgnored(Materials.TreatedWood);
        pipeSmallRestrictive.addSecondaryMaterial(new MaterialStack(Materials.Iron, ring.materialAmount * 2));
        pipeNormalRestrictive.addSecondaryMaterial(new MaterialStack(Materials.Iron, ring.materialAmount * 2));
        pipeLargeRestrictive.addSecondaryMaterial(new MaterialStack(Materials.Iron, ring.materialAmount * 2));
        pipeHugeRestrictive.addSecondaryMaterial(new MaterialStack(Materials.Iron, ring.materialAmount * 2));

        cableGtSingle.addSecondaryMaterial(new MaterialStack(Materials.Rubber, plate.materialAmount));
        cableGtDouble.addSecondaryMaterial(new MaterialStack(Materials.Rubber, plate.materialAmount));
        cableGtQuadruple.addSecondaryMaterial(new MaterialStack(Materials.Rubber, plate.materialAmount * 2));
        cableGtOctal.addSecondaryMaterial(new MaterialStack(Materials.Rubber, plate.materialAmount * 3));
        cableGtHex.addSecondaryMaterial(new MaterialStack(Materials.Rubber, plate.materialAmount * 5));

        plateDouble.setIgnored(Materials.BorosilicateGlass);
        plate.setIgnored(Materials.BorosilicateGlass);
        foil.setIgnored(Materials.BorosilicateGlass);

        dustSmall.setIgnored(Materials.Lapotron);
        dustTiny.setIgnored(Materials.Lapotron);
    }

    private static void excludeAllGems(Material material) {
        gem.setIgnored(material);
        excludeAllGemsButNormal(material);
    }

    private static void excludeAllGemsButNormal(Material material) {
        gemChipped.setIgnored(material);
        gemFlawed.setIgnored(material);
        gemFlawless.setIgnored(material);
        gemExquisite.setIgnored(material);
    }

    public final String name;
    public final int id;

    public final boolean isUnificationEnabled;
    public final boolean isSelfReferencing;

    private @Nullable
    Predicate<Material> generationCondition;
    public final @Nullable
    MaterialIconType materialIconType;

    private final long materialAmount;

    /**
     * Contains a default material type for self-referencing TagPrefix
     * For self-referencing prefixes, it is always guaranteed for it to be not null
     * <p>
     * NOTE: Ore registrations with self-referencing TagPrefix still can occur with other materials
     */
    public @Nullable
    Material materialType;

    private final List<IOreRegistrationHandler> oreProcessingHandlers = new ArrayList<>();
    private final Set<Material> ignoredMaterials = new HashSet<>();
    private final Set<Material> generatedMaterials = new HashSet<>();
    private boolean isMarkerPrefix = false;

    public byte maxStackSize = 64;
    public final List<MaterialStack> secondaryMaterials = new ArrayList<>();
    public Function<Integer, Float> heatDamageFunction = null; // Negative for Frost Damage
    public Function<Material, List<String>> tooltipFunc;

    private String alternativeOreName = null;

    public TagPrefix(String name, long materialAmount, @Nullable Material material, @Nullable MaterialIconType materialIconType, long flags, @Nullable Predicate<Material> condition) {
        this(name, materialAmount, material, materialIconType, flags, condition, null);
    }

    public TagPrefix(String name, long materialAmount, @Nullable Material material, @Nullable MaterialIconType materialIconType, long flags, @Nullable Predicate<Material> condition, @Nullable Function<Material, List<String>> tooltipFunc) {
        Preconditions.checkArgument(!PREFIXES.containsKey(name), "TagPrefix " + name + " already registered!");
        this.name = name;
        this.id = idCounter.getAndIncrement();
        this.materialAmount = materialAmount;
        this.isSelfReferencing = (flags & SELF_REFERENCING) != 0;
        this.isUnificationEnabled = (flags & ENABLE_UNIFICATION) != 0;
        this.materialIconType = materialIconType;
        this.generationCondition = condition;
        this.tooltipFunc = tooltipFunc;
        if (isSelfReferencing) {
            Preconditions.checkNotNull(material, "Material is null for self-referencing TagPrefix");
            this.materialType = material;
        }
        PREFIXES.put(name, this);
    }

    public String name() {
        return this.name;
    }

    public void addSecondaryMaterial(MaterialStack secondaryMaterial) {
        Preconditions.checkNotNull(secondaryMaterial, "secondaryMaterial");
        secondaryMaterials.add(secondaryMaterial);
    }

    public void setMarkerPrefix(boolean isMarkerPrefix) {
        this.isMarkerPrefix = isMarkerPrefix;
    }

    public long getMaterialAmount(@Nullable Material material) {

        if(material == null) {
            return this.materialAmount;
        }

        if (this == block) {
            //glowstone and nether quartz blocks use 4 gems (dusts)
            if (material == Materials.Glowstone ||
                    material == Materials.NetherQuartz ||
                    material == Materials.Brick ||
                    material == Materials.Clay)
                return M * 4;
                //glass, ice and obsidian gain only one dust
            else if (material == Materials.Glass ||
                    material == Materials.Ice ||
                    material == Materials.Obsidian ||
                    material == Materials.Concrete)
                return M;
        } else if (this == stick) {
            if (material == Materials.Blaze)
                return M * 4;
            else if (material == Materials.Bone)
                return M * 5;
        }
        return materialAmount;
    }

    @ZenMethod
    public static TagPrefix getPrefix(String prefixName) {
        return getPrefix(prefixName, null);
    }

    public static TagPrefix getPrefix(String prefixName, @Nullable TagPrefix replacement) {
        return PREFIXES.getOrDefault(prefixName, replacement);
    }

    public boolean doGenerateItem(Material material) {
        return !isSelfReferencing && !isIgnored(material) && (generationCondition == null || generationCondition.test(material));
    }

    public void setGenerationCondition(@Nullable Predicate<Material> in) {
        generationCondition = in;
    }

    public boolean addProcessingHandler(IOreRegistrationHandler... processingHandler) {
        Preconditions.checkNotNull(processingHandler);
        Validate.noNullElements(processingHandler);
        return oreProcessingHandlers.addAll(Arrays.asList(processingHandler));
    }

    public <T extends IMaterialProperty<T>> void addProcessingHandler(PropertyKey<T> propertyKey, TriConsumer<TagPrefix, Material, T> handler) {
        addProcessingHandler((orePrefix, material) -> {
            if (material.hasProperty(propertyKey) && !material.hasFlag(NO_UNIFICATION)) {
                handler.accept(orePrefix, material, material.getProperty(propertyKey));
            }
        });
    }

    public void processOreRegistration(@Nullable Material material) {
        if (this.isSelfReferencing && material == null) {
            material = materialType; //append default material for self-referencing TagPrefix
        }
        if (material != null) generatedMaterials.add(material);
    }

    public static void runMaterialHandlers() {
        for (TagPrefix orePrefix : PREFIXES.values()) {
            orePrefix.runGeneratedMaterialHandlers();
        }
    }

    private static final ThreadLocal<TagPrefix> currentProcessingPrefix = new ThreadLocal<>();
    private static final ThreadLocal<Material> currentMaterial = new ThreadLocal<>();

    public static TagPrefix getCurrentProcessingPrefix() {
        return currentProcessingPrefix.get();
    }

    public static Material getCurrentMaterial() {
        return currentMaterial.get();
    }

    private void runGeneratedMaterialHandlers() {
        currentProcessingPrefix.set(this);
        for (Material registeredMaterial : generatedMaterials) {
            currentMaterial.set(registeredMaterial);
            for (IOreRegistrationHandler registrationHandler : oreProcessingHandlers) {
                registrationHandler.processMaterial(this, registeredMaterial);
            }
            currentMaterial.set(null);
        }
        //clear generated materials for next pass
        generatedMaterials.clear();
        currentProcessingPrefix.set(null);
    }

    public void setAlternativeOreName(String name) {
        this.alternativeOreName = name;
    }

    public String getAlternativeOreName() {
        return alternativeOreName;
    }

    // todo clean this up
    public String getLocalNameForItem(Material material) {
        String specifiedUnlocalized = "item." + material.toString() + "." + this.name;
        if (LocalizationUtils.hasKey(specifiedUnlocalized)) return LocalizationUtils.format(specifiedUnlocalized);
        String unlocalized = findUnlocalizedName(material);
        String matLocalized = material.getLocalizedName();
        String formatted = LocalizationUtils.format(unlocalized, matLocalized);
        return formatted.equals(unlocalized) ? matLocalized : formatted;
    }

    private String findUnlocalizedName(Material material) {
        if(material.hasProperty(PropertyKey.POLYMER)) {
            String localizationKey = String.format("item.material.oreprefix.polymer.%s", this.name);
            // Not every polymer ore prefix gets a special name
            if(LocalizationUtils.hasKey(localizationKey)) {
                return localizationKey;
            }
        }

        return String.format("item.material.oreprefix.%s", this.name);
    }

    public boolean isIgnored(Material material) {
        return ignoredMaterials.contains(material);
    }

    @ZenMethod
    public void setIgnored(Material material) {
        ignoredMaterials.add(material);
    }

    public boolean isMarkerPrefix() {
        return isMarkerPrefix;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TagPrefix &&
                ((TagPrefix) o).name.equals(this.name);
    }

    public static Collection<TagPrefix> values() {
        return PREFIXES.values();
    }

    @Override
    public String toString() {
        return name + "/" + id;
    }
}
