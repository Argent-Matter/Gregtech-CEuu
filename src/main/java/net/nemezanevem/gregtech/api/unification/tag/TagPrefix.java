package net.nemezanevem.gregtech.api.unification.tag;

import com.google.common.base.Preconditions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.nemezanevem.gregtech.api.unification.material.GtMaterials;
import net.nemezanevem.gregtech.api.unification.material.MarkerMaterials;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.properties.GtMaterialProperties;
import net.nemezanevem.gregtech.api.unification.material.properties.IMaterialProperty;
import net.nemezanevem.gregtech.api.unification.material.properties.PropertyKey;
import net.nemezanevem.gregtech.api.unification.material.properties.info.GtMaterialIconTypes;
import net.nemezanevem.gregtech.api.unification.material.properties.info.MaterialIconType;
import net.nemezanevem.gregtech.api.unification.stack.MaterialStack;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static net.nemezanevem.gregtech.api.unification.material.properties.info.GtMaterialFlags.*;
import static net.nemezanevem.gregtech.api.unification.tag.TagPrefix.Conditions.*;
import static net.nemezanevem.gregtech.api.unification.tag.TagPrefix.Flags.ENABLE_UNIFICATION;
import static net.nemezanevem.gregtech.api.unification.tag.TagPrefix.Flags.SELF_REFERENCING;
import static net.nemezanevem.gregtech.api.GTValues.M;

public class TagPrefix {

    private final static Map<String, TagPrefix> PREFIXES = new HashMap<>();

    // Regular Ore Prefix. Ore -> Material is a Oneway Operation! Introduced by Eloraam
    public static final TagPrefix ore = new TagPrefix("ores_in_ground", -1, null, GtMaterialIconTypes.ore.get(), ENABLE_UNIFICATION, hasOreProperty);
    public static final TagPrefix oreGranite = new TagPrefix("ores_in_ground/granite", -1, null, GtMaterialIconTypes.ore.get(), ENABLE_UNIFICATION, hasOreProperty);
    public static final TagPrefix oreDiorite = new TagPrefix("ores_in_ground/diorite", -1, null, GtMaterialIconTypes.ore.get(), ENABLE_UNIFICATION, hasOreProperty);
    public static final TagPrefix oreAndesite = new TagPrefix("ores_in_ground/andesite", -1, null, GtMaterialIconTypes.ore.get(), ENABLE_UNIFICATION, hasOreProperty);
    public static final TagPrefix oreBlackgranite = new TagPrefix("ores_in_ground/black_granite", -1, null, GtMaterialIconTypes.ore.get(), ENABLE_UNIFICATION, hasOreProperty);
    public static final TagPrefix oreRedgranite = new TagPrefix("ores_in_ground/red_granite", -1, null, GtMaterialIconTypes.ore.get(), ENABLE_UNIFICATION, hasOreProperty);
    public static final TagPrefix oreMarble = new TagPrefix("ores_in_ground/marble", -1, null, GtMaterialIconTypes.ore.get(), ENABLE_UNIFICATION, hasOreProperty);
    public static final TagPrefix oreBasalt = new TagPrefix("ores_in_ground/basalt", -1, null, GtMaterialIconTypes.ore.get(), ENABLE_UNIFICATION, hasOreProperty);

    // In case of an Sand-Ores Mod. Ore -> Material is a Oneway Operation!
    public static final TagPrefix oreSand = new TagPrefix("ores_in_ground/sand", -1, null, GtMaterialIconTypes.ore.get(), ENABLE_UNIFICATION, null);
    public static final TagPrefix oreRedSand = new TagPrefix("ores_in_ground/red_sand", -1, null, GtMaterialIconTypes.ore.get(), ENABLE_UNIFICATION, null);

    // Prefix of the Nether-Ores Mod. Causes Ores to double. Ore -> Material is a Oneway Operation!
    public static final TagPrefix oreNetherrack = new TagPrefix("ores_in_ground/netherrack", -1, null, GtMaterialIconTypes.ore.get(), ENABLE_UNIFICATION, hasOreProperty);
    // In case of an End-Ores Mod. Ore -> Material is a Oneway Operation!
    public static final TagPrefix oreEndstone = new TagPrefix("ores_in_ground/endstone", -1, null, GtMaterialIconTypes.ore.get(), ENABLE_UNIFICATION, hasOreProperty);

    public static final TagPrefix crushedCentrifuged = new TagPrefix("crushed/centrifuged", -1, null, GtMaterialIconTypes.crushedCentrifuged.get(), ENABLE_UNIFICATION, hasOreProperty);
    public static final TagPrefix crushedPurified = new TagPrefix("crushed/purified", -1, null, GtMaterialIconTypes.crushedPurified.get(), ENABLE_UNIFICATION, hasOreProperty);
    public static final TagPrefix crushed = new TagPrefix("crushed", -1, null, GtMaterialIconTypes.crushed.get(), ENABLE_UNIFICATION, hasOreProperty, mat -> Collections.singletonList(Component.translatable("metaitem.crushed.tooltip.purify")));

    // Introduced by Mekanism
    public static final TagPrefix shard = new TagPrefix("shard", -1, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix clump = new TagPrefix("clump", -1, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix reduced = new TagPrefix("reduced", -1, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix crystalline = new TagPrefix("crystalline", -1, null, null, ENABLE_UNIFICATION, null);

    public static final TagPrefix cleanGravel = new TagPrefix("gravel/clean", -1, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix dirtyGravel = new TagPrefix("gravel/dirty", -1, null, null, ENABLE_UNIFICATION, null);

    // A hot Ingot, which has to be cooled down by a Vacuum Freezer.
    public static final TagPrefix ingotHot = new TagPrefix("ingots/hot", M, null, GtMaterialIconTypes.ingotHot.get(), ENABLE_UNIFICATION, hasBlastProperty.and(mat -> mat.getProperty(GtMaterialProperties.BLAST.get()).getBlastTemperature() > 1750));
    // A regular Ingot. Introduced by Eloraam
    public static final TagPrefix ingot = new TagPrefix("ingots", M, null, GtMaterialIconTypes.ingot.get(), ENABLE_UNIFICATION, hasIngotProperty);

    // A regular Gem worth one Dust. Introduced by Eloraam
    public static final TagPrefix gem = new TagPrefix("gems", M, null, GtMaterialIconTypes.gem.get(), ENABLE_UNIFICATION, hasGemProperty);
    // A regular Gem worth one small Dust. Introduced by TerraFirmaCraft
    public static final TagPrefix gemChipped = new TagPrefix("gems/chipped", M / 4, null, GtMaterialIconTypes.gemChipped, ENABLE_UNIFICATION, hasGemProperty.and(unused -> ConfigHolder.recipes.generateLowQualityGems));
    // A regular Gem worth two small Dusts. Introduced by TerraFirmaCraft
    public static final TagPrefix gemFlawed = new TagPrefix("gems/flawed", M / 2, null, GtMaterialIconTypes.gemFlawed, ENABLE_UNIFICATION, hasGemProperty.and(unused -> ConfigHolder.recipes.generateLowQualityGems));
    // A regular Gem worth two Dusts. Introduced by TerraFirmaCraft
    public static final TagPrefix gemFlawless = new TagPrefix("gems/flawless", M * 2, null, GtMaterialIconTypes.gemFlawless.get(), ENABLE_UNIFICATION, hasGemProperty);
    // A regular Gem worth four Dusts. Introduced by TerraFirmaCraft
    public static final TagPrefix gemExquisite = new TagPrefix("gems/exquisite", M * 4, null, GtMaterialIconTypes.gemExquisite.get(), ENABLE_UNIFICATION, hasGemProperty);

    // 1/4th of a Dust.
    public static final TagPrefix dustSmall = new TagPrefix("dusts/small", M / 4, null, GtMaterialIconTypes.dustSmall.get(), ENABLE_UNIFICATION, hasDustProperty);
    // 1/9th of a Dust.
    public static final TagPrefix dustTiny = new TagPrefix("dusts/tiny", M / 9, null, GtMaterialIconTypes.dustTiny.get(), ENABLE_UNIFICATION, hasDustProperty);
    // Dust with impurities. 1 Unit of Main Material and 1/9 - 1/4 Unit of secondary Material
    public static final TagPrefix dustImpure = new TagPrefix("dusts/impure", M, null, GtMaterialIconTypes.dustImpure.get(), ENABLE_UNIFICATION, hasOreProperty, mat -> Collections.singletonList(Component.translatable("metaitem.dust.tooltip.purify")));
    // Pure Dust worth of one Ingot or Gem. Introduced by Alblaka.
    public static final TagPrefix dustPure = new TagPrefix("dusts/pure", M, null, GtMaterialIconTypes.dustPure.get(), ENABLE_UNIFICATION, hasOreProperty, mat -> Collections.singletonList(Component.translatable("metaitem.dust.tooltip.purify")));
    public static final TagPrefix dust = new TagPrefix("dusts", M, null, GtMaterialIconTypes.dust.get(), ENABLE_UNIFICATION, hasDustProperty);

    // A Nugget. Introduced by Eloraam
    public static final TagPrefix nugget = new TagPrefix("nuggets", M / 9, null, GtMaterialIconTypes.nugget.get(), ENABLE_UNIFICATION, hasIngotProperty);

    // 9 Plates combined in one Item.
    public static final TagPrefix plateDense = new TagPrefix("dense_plates", M * 9, null, GtMaterialIconTypes.plateDense.get(), ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_DENSE.get()) && !mat.hasFlag(NO_SMASHING.get()));
    // 2 Plates combined in one Item
    public static final TagPrefix plateDouble = new TagPrefix("double_plates", M * 2, null, GtMaterialIconTypes.plateDouble.get(), ENABLE_UNIFICATION, hasIngotProperty.and(mat -> mat.hasFlag(GENERATE_PLATE.get()) && !mat.hasFlag(NO_SMASHING.get())));
    // Regular Plate made of one Ingot/Dust. Introduced by Calclavia
    public static final TagPrefix plate = new TagPrefix("plates", M, null, GtMaterialIconTypes.plate.get(), ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_PLATE.get()));

    // Round made of 1 Nugget
    public static final TagPrefix round = new TagPrefix("rounds", M / 9, null, GtMaterialIconTypes.round.get(), ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_ROUND.get()));
    // Foil made of 1/4 Ingot/Dust.
    public static final TagPrefix foil = new TagPrefix("foils", M / 4, null, GtMaterialIconTypes.foil.get(), ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_FOIL.get()));

    // Stick made of an Ingot.
    public static final TagPrefix rodLong = new TagPrefix("rods/long", M, null, GtMaterialIconTypes.rodLong.get(), ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_LONG_ROD.get()));
    // Stick made of half an Ingot. Introduced by Eloraam
    public static final TagPrefix rod = new TagPrefix("rods", M / 2, null, GtMaterialIconTypes.rod.get(), ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_ROD.get()));

    // consisting out of 1/8 Ingot or 1/4 Stick.
    public static final TagPrefix bolt = new TagPrefix("bolts", M / 8, null, GtMaterialIconTypes.bolt.get(), ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_BOLT_SCREW.get()));
    // consisting out of 1/9 Ingot.
    public static final TagPrefix screw = new TagPrefix("screws", M / 9, null, GtMaterialIconTypes.screw.get(), ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_BOLT_SCREW.get()));
    // consisting out of 1/2 Stick.
    public static final TagPrefix ring = new TagPrefix("rings", M / 4, null, GtMaterialIconTypes.ring.get(), ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_RING.get()));
    // consisting out of 1 Fine Wire.
    public static final TagPrefix springSmall = new TagPrefix("springs/small", M / 4, null, GtMaterialIconTypes.springSmall.get(), ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_SPRING_SMALL.get()) && !mat.hasFlag(NO_SMASHING.get()));
    // consisting out of 2 Sticks.
    public static final TagPrefix spring = new TagPrefix("springs", M, null, GtMaterialIconTypes.spring.get(), ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_SPRING.get()) && !mat.hasFlag(NO_SMASHING.get()));
    // consisting out of 1/8 Ingot or 1/4 Wire.
    public static final TagPrefix wireFine = new TagPrefix("wires/fine", M / 8, null, GtMaterialIconTypes.wireFine.get(), ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_FINE_WIRE.get()));
    // consisting out of 4 Plates, 1 Ring and 1 Screw.
    public static final TagPrefix rotor = new TagPrefix("rotors", M * 4, null, GtMaterialIconTypes.rotor.get(), ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_ROTOR.get()));
    public static final TagPrefix gearSmall = new TagPrefix("gears/small", M, null, GtMaterialIconTypes.gearSmall.get(), ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_SMALL_GEAR.get()));
    // Introduced by me because BuildCraft has ruined the gear Prefix...
    public static final TagPrefix gear = new TagPrefix("gears", M * 4, null, GtMaterialIconTypes.gear.get(), ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_GEAR.get()));
    // 3/4 of a Plate or Gem used to shape a Lens. Normally only used on Transparent GtMaterials.
    public static final TagPrefix lens = new TagPrefix("lenses", (M * 3) / 4, null, GtMaterialIconTypes.lens.get(), ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_LENS.get()));

    // made of 4 Ingots.
    public static final TagPrefix toolHeadBuzzSaw = new TagPrefix("tools/heads/buzzsaw", M * 4, null, GtMaterialIconTypes.toolHeadBuzzSaw.get(), ENABLE_UNIFICATION, hasNoCraftingToolProperty.and(mat -> mat.hasFlag(GENERATE_PLATE.get())));
    // made of 1 Ingots.
    public static final TagPrefix toolHeadScrewdriver = new TagPrefix("tools/heads/screwdriver", M, null, GtMaterialIconTypes.toolHeadScrewdriver.get(), ENABLE_UNIFICATION, hasNoCraftingToolProperty.and(mat -> mat.hasFlag(GENERATE_LONG_ROD.get())));
    // made of 4 Ingots.
    public static final TagPrefix toolHeadDrill = new TagPrefix("tools/heads/drill", M * 4, null, GtMaterialIconTypes.toolHeadDrill.get(), ENABLE_UNIFICATION, hasToolProperty.and(mat -> mat.hasFlag(GENERATE_PLATE.get())));
    // made of 2 Ingots.
    public static final TagPrefix toolHeadChainsaw = new TagPrefix("tools/heads/chainsaw", M * 2, null, GtMaterialIconTypes.toolHeadChainsaw.get(), ENABLE_UNIFICATION, hasNoCraftingToolProperty.and(mat -> mat.hasFlag(GENERATE_PLATE.get())));
    // made of 4 Ingots.
    public static final TagPrefix toolHeadWrench = new TagPrefix("tools/heads/wrench", M * 4, null, GtMaterialIconTypes.toolHeadWrench.get(), ENABLE_UNIFICATION, hasNoCraftingToolProperty.and(mat -> mat.hasFlag(GENERATE_PLATE.get())));
    // made of 5 Ingots.
    public static final TagPrefix turbineBlade = new TagPrefix("turbine_blades", M * 10, null, GtMaterialIconTypes.turbineBlade.get(), ENABLE_UNIFICATION, hasRotorProperty.and(m -> m.hasFlags(GENERATE_BOLT_SCREW.get(), GENERATE_PLATE.get()) && !m.hasProperty(GtMaterialProperties.GEM.get())));

    public static final TagPrefix paneGlass = new TagPrefix("glass_panes", -1, MarkerMaterials.Color.Colorless, null, SELF_REFERENCING, null);
    public static final TagPrefix blockGlass = new TagPrefix("glass", -1, MarkerMaterials.Color.Colorless, null, SELF_REFERENCING, null);

    // Storage Block consisting out of 9 Ingots/Gems/Dusts. Introduced by CovertJaguar
    public static final TagPrefix block = new TagPrefix("storage_blocks", M * 9, null, GtMaterialIconTypes.block.get(), ENABLE_UNIFICATION, null);

    // Prefix used for Logs. Usually as "logWood.get()". Introduced by Eloraam
    public static final TagPrefix log = new TagPrefix("logs", -1, null, null, 0, null);
    // Prefix for Planks. Usually "plankWood.get()". Introduced by Eloraam
    public static final TagPrefix plank = new TagPrefix("planks", -1, null, null, 0, null);

    // Prefix to determine which kind of Rock this is.
    public static final TagPrefix stone = new TagPrefix("stone", -1, GtMaterials.Stone.get(), null, SELF_REFERENCING, null);

    public static final TagPrefix frameGt = new TagPrefix("frames", M * 2, null, null, ENABLE_UNIFICATION, material -> material.hasFlag(GENERATE_FRAME.get()));

    public static final TagPrefix pipeTinyFluid = new TagPrefix("pipes/tiny/fluid", M / 2, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix pipeSmallFluid = new TagPrefix("pipes/small/fluid", M, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix pipeNormalFluid = new TagPrefix("pipes/normal/fluid", M * 3, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix pipeLargeFluid = new TagPrefix("pipes/large/fluid", M * 6, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix pipeHugeFluid = new TagPrefix("pipes/Huge/fluid", M * 12, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix pipeQuadrupleFluid = new TagPrefix("pipes/quadruple/fluid", M * 4, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix pipeNonupleFluid = new TagPrefix("pipes/nonuple/fluid", M * 9, null, null, ENABLE_UNIFICATION, null);

    public static final TagPrefix pipeTinyItem = new TagPrefix("pipes/tiny/item", M / 2, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix pipeSmallItem = new TagPrefix("pipes/small/item", M, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix pipeNormalItem = new TagPrefix("pipes/normal/item", M * 3, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix pipeLargeItem = new TagPrefix("pipes/large/item", M * 6, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix pipeHugeItem = new TagPrefix("pipes/huge/item", M * 12, null, null, ENABLE_UNIFICATION, null);

    public static final TagPrefix pipeSmallRestrictive = new TagPrefix("pipes/small/restrictive", M, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix pipeNormalRestrictive = new TagPrefix("pipes/normal/restrictive", M * 3, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix pipeLargeRestrictive = new TagPrefix("pipes/large/restrictive", M * 6, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix pipeHugeRestrictive = new TagPrefix("pipes/huge/restrictive", M * 12, null, null, ENABLE_UNIFICATION, null);

    public static final TagPrefix wireGtHex = new TagPrefix("wires/hex", M * 8, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix wireGtOctal = new TagPrefix("wires/octal", M * 4, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix wireGtQuadruple = new TagPrefix("wires/quadruple", M * 2, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix wireGtDouble = new TagPrefix("wires/double", M, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix wireGtSingle = new TagPrefix("wires/single", M / 2, null, null, ENABLE_UNIFICATION, null);

    public static final TagPrefix cableGtHex = new TagPrefix("cables/hex", M * 8, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix cableGtOctal = new TagPrefix("cables/octal", M * 4, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix cableGtQuadruple = new TagPrefix("cables/quadruple", M * 2, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix cableGtDouble = new TagPrefix("cables/double", M, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix cableGtSingle = new TagPrefix("cables/single", M / 2, null, null, ENABLE_UNIFICATION, null);

    // Special Prefix used mainly for the Crafting Handler.
    public static final TagPrefix craftingLens = new TagPrefix("lenses", -1, null, null, 0, null);
    // Used for the 16 dyes. Introduced by Eloraam
    public static final TagPrefix dye = new TagPrefix("dyes", -1, null, null, 0, null);

    /**
     * Electric Components.
     *
     * @see MarkerMaterials.Tier
     */
    // Introduced by Calclavia
    public static final TagPrefix battery = new TagPrefix("batteries", -1, null, null, 0, null);
    // Introduced by Calclavia
    public static final TagPrefix circuit = new TagPrefix("circuits", -1, null, null, ENABLE_UNIFICATION, null);
    public static final TagPrefix component = new TagPrefix("components", -1, null, null, ENABLE_UNIFICATION, null);

    public static class Flags {
        public static final long ENABLE_UNIFICATION = 1;
        public static final long SELF_REFERENCING = 1 << 1;
    }

    public static class Conditions {
        public static final Predicate<Material> hasToolProperty = mat -> mat.hasProperty(GtMaterialProperties.TOOL.get());
        public static final Predicate<Material> hasNoCraftingToolProperty = hasToolProperty.and(mat -> !mat.getProperty(GtMaterialProperties.TOOL.get()).getShouldIgnoreCraftingTools());
        public static final Predicate<Material> hasOreProperty = mat -> mat.hasProperty(GtMaterialProperties.ORE.get());
        public static final Predicate<Material> hasGemProperty = mat -> mat.hasProperty(GtMaterialProperties.GEM.get());
        public static final Predicate<Material> hasDustProperty = mat -> mat.hasProperty(GtMaterialProperties.DUST.get());
        public static final Predicate<Material> hasIngotProperty = mat -> mat.hasProperty(GtMaterialProperties.INGOT.get());
        public static final Predicate<Material> hasBlastProperty = mat -> mat.hasProperty(GtMaterialProperties.BLAST.get());
        public static final Predicate<Material> hasRotorProperty = mat -> mat.hasProperty(GtMaterialProperties.ROTOR.get());
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

        gemExquisite.setIgnored(GtMaterials.Sugar.get());

        gemFlawless.setIgnored(GtMaterials.Sugar.get());

        gem.setIgnored(GtMaterials.Diamond.get());
        gem.setIgnored(GtMaterials.Emerald.get());
        gem.setIgnored(GtMaterials.Lapis.get());
        gem.setIgnored(GtMaterials.NetherQuartz.get());
        gem.setIgnored(GtMaterials.Coal.get());

        excludeAllGems(GtMaterials.Charcoal.get());
        excludeAllGems(GtMaterials.NetherStar.get());
        excludeAllGems(GtMaterials.EnderPearl.get());
        excludeAllGems(GtMaterials.EnderEye.get());
        excludeAllGems(GtMaterials.Flint.get());
        excludeAllGemsButNormal(GtMaterials.Lapotron.get());

        dust.setIgnored(GtMaterials.Redstone.get());
        dust.setIgnored(GtMaterials.Glowstone.get());
        dust.setIgnored(GtMaterials.Gunpowder.get());
        dust.setIgnored(GtMaterials.Sugar.get());
        dust.setIgnored(GtMaterials.Bone.get());
        dust.setIgnored(GtMaterials.Blaze.get());

        rod.setIgnored(GtMaterials.Wood.get());
        rod.setIgnored(GtMaterials.Bone.get());
        rod.setIgnored(GtMaterials.Blaze.get());
        rod.setIgnored(GtMaterials.Paper.get());

        ingot.setIgnored(GtMaterials.Iron.get());
        ingot.setIgnored(GtMaterials.Gold.get());
        ingot.setIgnored(GtMaterials.Wood.get());
        ingot.setIgnored(GtMaterials.TreatedWood.get());
        ingot.setIgnored(GtMaterials.Paper.get());

        nugget.setIgnored(GtMaterials.Wood.get());
        nugget.setIgnored(GtMaterials.TreatedWood.get());
        nugget.setIgnored(GtMaterials.Gold.get());
        nugget.setIgnored(GtMaterials.Paper.get());
        nugget.setIgnored(GtMaterials.Iron.get());
        plate.setIgnored(GtMaterials.Paper.get());

        block.setIgnored(GtMaterials.Iron.get());
        block.setIgnored(GtMaterials.Gold.get());
        block.setIgnored(GtMaterials.Lapis.get());
        block.setIgnored(GtMaterials.Emerald.get());
        block.setIgnored(GtMaterials.Redstone.get());
        block.setIgnored(GtMaterials.Diamond.get());
        block.setIgnored(GtMaterials.Coal.get());
        block.setIgnored(GtMaterials.Glass.get());
        block.setIgnored(GtMaterials.Marble.get());
        block.setIgnored(GtMaterials.GraniteRed.get());
        block.setIgnored(GtMaterials.Stone.get());
        block.setIgnored(GtMaterials.Glowstone.get());
        block.setIgnored(GtMaterials.Endstone.get());
        block.setIgnored(GtMaterials.Wheat.get());
        block.setIgnored(GtMaterials.Oilsands.get());
        block.setIgnored(GtMaterials.Wood.get());
        block.setIgnored(GtMaterials.TreatedWood.get());
        block.setIgnored(GtMaterials.RawRubber.get());
        block.setIgnored(GtMaterials.Clay.get());
        block.setIgnored(GtMaterials.Brick.get());
        block.setIgnored(GtMaterials.Bone.get());
        block.setIgnored(GtMaterials.NetherQuartz.get());
        block.setIgnored(GtMaterials.Ice.get());
        block.setIgnored(GtMaterials.Netherrack.get());
        block.setIgnored(GtMaterials.Concrete.get());
        block.setIgnored(GtMaterials.Blaze.get());
        block.setIgnored(GtMaterials.Lapotron.get());

        ore.addSecondaryMaterial(new MaterialStack(GtMaterials.Stone.get(), dust.materialAmount));
        oreNetherrack.addSecondaryMaterial(new MaterialStack(GtMaterials.Netherrack.get(), dust.materialAmount));
        oreEndstone.addSecondaryMaterial(new MaterialStack(GtMaterials.Endstone.get(), dust.materialAmount));

        if (ConfigHolder.worldgen.allUniqueStoneTypes) {
            oreGranite.addSecondaryMaterial(new MaterialStack(GtMaterials.Granite.get(), dust.materialAmount));
            oreDiorite.addSecondaryMaterial(new MaterialStack(GtMaterials.Diorite.get(), dust.materialAmount));
            oreAndesite.addSecondaryMaterial(new MaterialStack(GtMaterials.Andesite.get(), dust.materialAmount));
            oreRedgranite.addSecondaryMaterial(new MaterialStack(GtMaterials.GraniteRed.get(), dust.materialAmount));
            oreBlackgranite.addSecondaryMaterial(new MaterialStack(GtMaterials.GraniteBlack.get(), dust.materialAmount));
            oreBasalt.addSecondaryMaterial(new MaterialStack(GtMaterials.Basalt.get(), dust.materialAmount));
            oreMarble.addSecondaryMaterial(new MaterialStack(GtMaterials.Marble.get(), dust.materialAmount));
            oreSand.addSecondaryMaterial(new MaterialStack(GtMaterials.SiliconDioxide.get(), dustTiny.materialAmount));
            oreRedSand.addSecondaryMaterial(new MaterialStack(GtMaterials.SiliconDioxide.get(), dustTiny.materialAmount));
        }

        crushed.addSecondaryMaterial(new MaterialStack(GtMaterials.Stone.get(), dust.materialAmount));

        toolHeadDrill.addSecondaryMaterial(new MaterialStack(GtMaterials.Steel.get(), plate.materialAmount * 4));
        toolHeadChainsaw.addSecondaryMaterial(new MaterialStack(GtMaterials.Steel.get(), plate.materialAmount * 4 + ring.materialAmount * 2));
        toolHeadWrench.addSecondaryMaterial(new MaterialStack(GtMaterials.Steel.get(), ring.materialAmount + screw.materialAmount * 2));

        pipeTinyFluid.setIgnored(GtMaterials.Wood.get());
        pipeHugeFluid.setIgnored(GtMaterials.Wood.get());
        pipeQuadrupleFluid.setIgnored(GtMaterials.Wood.get());
        pipeNonupleFluid.setIgnored(GtMaterials.Wood.get());
        pipeTinyFluid.setIgnored(GtMaterials.TreatedWood.get());
        pipeHugeFluid.setIgnored(GtMaterials.TreatedWood.get());
        pipeQuadrupleFluid.setIgnored(GtMaterials.TreatedWood.get());
        pipeNonupleFluid.setIgnored(GtMaterials.TreatedWood.get());
        pipeSmallRestrictive.addSecondaryMaterial(new MaterialStack(GtMaterials.Iron.get(), ring.materialAmount * 2));
        pipeNormalRestrictive.addSecondaryMaterial(new MaterialStack(GtMaterials.Iron.get(), ring.materialAmount * 2));
        pipeLargeRestrictive.addSecondaryMaterial(new MaterialStack(GtMaterials.Iron.get(), ring.materialAmount * 2));
        pipeHugeRestrictive.addSecondaryMaterial(new MaterialStack(GtMaterials.Iron.get(), ring.materialAmount * 2));

        cableGtSingle.addSecondaryMaterial(new MaterialStack(GtMaterials.Rubber.get(), plate.materialAmount));
        cableGtDouble.addSecondaryMaterial(new MaterialStack(GtMaterials.Rubber.get(), plate.materialAmount));
        cableGtQuadruple.addSecondaryMaterial(new MaterialStack(GtMaterials.Rubber.get(), plate.materialAmount * 2));
        cableGtOctal.addSecondaryMaterial(new MaterialStack(GtMaterials.Rubber.get(), plate.materialAmount * 3));
        cableGtHex.addSecondaryMaterial(new MaterialStack(GtMaterials.Rubber.get(), plate.materialAmount * 5));

        plateDouble.setIgnored(GtMaterials.BorosilicateGlass.get());
        plate.setIgnored(GtMaterials.BorosilicateGlass.get());
        foil.setIgnored(GtMaterials.BorosilicateGlass.get());

        dustSmall.setIgnored(GtMaterials.Lapotron.get());
        dustTiny.setIgnored(GtMaterials.Lapotron.get());
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
    public Function<Material, List<MutableComponent>> tooltipFunc;

    private String alternativeOreName = null;

    public TagPrefix(String name, long materialAmount, @Nullable Material material, @Nullable MaterialIconType materialIconType, long flags, @Nullable Predicate<Material> condition) {
        this(name, materialAmount, material, materialIconType, flags, condition, null);
    }

    public TagPrefix(String name, long materialAmount, @Nullable Material material, @Nullable MaterialIconType materialIconType, long flags, @Nullable Predicate<Material> condition, @Nullable Function<Material, List<MutableComponent>> tooltipFunc) {
        Preconditions.checkArgument(!PREFIXES.containsKey(name), "TagPrefix " + name + " already registered!");
        this.name = name;
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
            if (material == GtMaterials.Glowstone.get() ||
                    material == GtMaterials.NetherQuartz.get() ||
                    material == GtMaterials.Brick.get() ||
                    material == GtMaterials.Clay.get())
                return M * 4;
                //glass, ice and obsidian gain only one dust
            else if (material == GtMaterials.Glass.get() ||
                    material == GtMaterials.Ice.get() ||
                    material == GtMaterials.Obsidian.get() ||
                    material == GtMaterials.Concrete.get())
                return M;
        } else if (this == rod) {
            if (material == GtMaterials.Blaze.get())
                return M * 4;
            else if (material == GtMaterials.Bone.get())
                return M * 5;
        }
        return materialAmount;
    }

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
            if (material.hasProperty(propertyKey) && !material.hasFlag(NO_UNIFICATION.get())) {
                handler.accept(orePrefix, material, material.getProperty(propertyKey));
            }
        });
    }

    public void processTagRegistration(@Nullable Material material) {
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

    public String getAlternativeTagName() {
        return alternativeOreName;
    }

    public MutableComponent getLocalNameForItem(Material material) {
        var specifiedKey = Component.translatable("item." + material.toString() + "." + this.name);
        if(ComponentUtils.isTranslationResolvable(specifiedKey)) return specifiedKey;
        var unlocalized = findUnlocalizedName(material);
        var matLocalized = material.getLocalizedName();
        return unlocalized.equals(matLocalized) ? matLocalized : unlocalized;
    }

    private MutableComponent findUnlocalizedName(Material material) {
        if(material.hasProperty(GtMaterialProperties.POLYMER.get())) {
            var localizationKey = Component.translatable(String.format("item.material.tagprefix.polymer.%s", this.name));
            // Not every polymer ore prefix gets a special name
            if(ComponentUtils.isTranslationResolvable(localizationKey)) {
                return localizationKey;
            }
        }

        return Component.translatable(String.format("item.material.tagprefix.%s", this.name));
    }

    public boolean isIgnored(Material material) {
        return ignoredMaterials.contains(material);
    }

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
        return name;
    }
}
