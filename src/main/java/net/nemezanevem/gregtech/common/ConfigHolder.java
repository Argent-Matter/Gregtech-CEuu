package net.nemezanevem.gregtech.common;


import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigHolder {
    public static final ForgeConfigSpec GENERAL_SPEC;

    static {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        setupConfig(configBuilder);
        GENERAL_SPEC = configBuilder.build();
    }

    public static class ClientConfig {
        public static class GuiConfig {
            public static ForgeConfigSpec.IntValue scrollSpeed;
        }
        public static class ArmorHud {
            public static ForgeConfigSpec.IntValue hudLocation;
            public static ForgeConfigSpec.IntValue hudOffsetX;
            public static ForgeConfigSpec.IntValue hudOffsetY;
        }

        public static class ShaderOptions {

            builder.comment("Bloom config options for the fusion reactor.")
                    builder.push("Fusion Reactor")
            public FusionBloom fusionBloom = new FusionBloom();

            builder.comment("Bloom config options for the heat effect (cable burning).")
                    builder.push("Heat Effect")
            public HeatEffectBloom heatEffectBloom = new HeatEffectBloom();

            builder.comment({"Whether to use shader programs.", "Default: true"})
            public boolean useShader = true;

            builder.comment({"Whether or not to enable Emissive Textures with bloom effect.", "Default: true"})
            public boolean emissiveTexturesBloom = true;

            builder.comment({"Bloom Algorithm", "0 - Simple Gaussian Blur Bloom (Fast)", "1 - Unity Bloom", "2 - Unreal Bloom", "Default: 2"})
            @Config.RangeInt(min = 0, max = 2)
            @Config.SlidingOption
            public int bloomStyle = 2;

            builder.comment({"The brightness after bloom should not exceed this value. It can be used to limit the brightness of highlights " +
                    "(e.g., daytime).", "OUTPUT = BACKGROUND + BLOOM * strength * (base + LT + (1 - BACKGROUND_BRIGHTNESS)*({HT}-LT)))", "This value should be greater than lowBrightnessThreshold.", "Default: 0.5"})
            @Config.RangeDouble(min = 0)
            public double highBrightnessThreshold = 0.5;

            builder.comment({"The brightness after bloom should not smaller than this value. It can be used to limit the brightness of dusky parts " +
                    "(e.g., night/caves).", "OUTPUT = BACKGROUND + BLOOM * strength * (base + {LT} + (1 - BACKGROUND_BRIGHTNESS)*(HT-{LT})))", "This value should be smaller than highBrightnessThreshold.", "Default: 0.2"})
            @Config.RangeDouble(min = 0)
            public double lowBrightnessThreshold = 0.2;

            builder.comment({"The base brightness of the bloom.", "It is similar to strength", "This value should be smaller than highBrightnessThreshold.", "OUTPUT = BACKGROUND + BLOOM * strength * ({base} + LT + (1 - BACKGROUND_BRIGHTNESS)*(HT-LT)))", "Default: 0.1"})
            @Config.RangeDouble(min = 0)
            public double baseBrightness = 0.1;

            builder.comment({"Mipmap Size.", "Higher values increase quality, but are slower to render.", "Default: 5"})
            @Config.RangeInt(min = 2, max = 5)
            @Config.SlidingOption
            public int nMips = 5;

            builder.comment({"Bloom Strength", "OUTPUT = BACKGROUND + BLOOM * {strength} * (base + LT + (1 - BACKGROUND_BRIGHTNESS)*(HT-LT)))", "Default: 2"})
            @Config.RangeDouble(min = 0)
            public double strength = 1.5;

            builder.comment({"Blur Step (bloom range)", "Default: 1"})
            @Config.RangeDouble(min = 0)
            public double step = 1;
        }

        public static ForgeConfigSpec.ConfigValue<String> terminalRootPath;

        builder.comment({"Whether to hook depth texture. Has no effect on performance, but if there is a problem with rendering, try disabling it.", "Default: true"})
        public boolean hookDepthTexture = true;

        builder.comment({"Resolution level for fragment shaders.",
                "Higher values increase quality (limited by the resolution of your screen) but are more GPU intensive.", "Default: 2"})
        @Config.RangeDouble(min = 0, max = 5)
        @Config.SlidingOption
        public double resolution = 2;

        builder.comment({"Whether or not to enable Emissive Textures for GregTech Machines.", "Default: true"})
        public boolean machinesEmissiveTextures = true;

        builder.comment({"Whether or not to enable Emissive Textures for GregTech Casings " +
                "when the multiblock is working (EBF coils, Fusion Casings, etc.).", "Default: false"})
        public boolean casingsActiveEmissiveTextures = false;

        builder.comment({"Whether or not sounds should be played when using tools outside of crafting.", "Default: true"})
        public boolean toolUseSounds = true;

        builder.comment({"Whether or not sounds should be played when crafting with tools.", "Default: true"})
        public boolean toolCraftingSounds = true;

        builder.comment({"Overrides the MC total playable sounds limit. MC's default is 28, which causes problems with many machine sounds at once",
                "If sounds are causing large amounts of lag, try lowering this.",
                "If sounds are not working at all, try setting this to the lowest value (28).", "Default: 512"})
        @Config.RangeInt(min = 28, max = 2048)
        @Config.RequiresMcRestart
        public int maxNumSounds = 512;

        builder.comment({"The default color to overlay onto machines.", "16777215 (0xFFFFFF in decimal) is no coloring (like GTCE).",
                "13819135 (0xD2DCFF in decimal) is the classic blue from GT5 (default)."})
        public int defaultPaintingColor = 0xD2DCFF;

        builder.comment({"The default color to overlay onto Machine (and other) UIs.", "16777215 (0xFFFFFF) is no coloring (like GTCE).",
                "13819135 (0xD2DCFF in decimal) is the classic blue from GT5 (default)."})
        public int defaultUIColor = 0xD2DCFF;
    }

    private static void setupConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Config options for client-only features").push("Client Options");

        ClientConfig.terminalRootPath = builder.comment("Default: {.../config}/gregtech/terminal")
                .worldRestart()
                .define("Terminal Root path", "gregtech/terminal");

        builder.push("Gui Config");
        ClientConfig.GuiConfig.scrollSpeed = builder.comment("The scrolling speed of widgets", "Default: 13")
                    .worldRestart()
                    .defineInRange("Widget Scrolling Speed", 13, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("Armor HUD Location");
        ClientConfig.ArmorHud.hudLocation = builder.comment("Sets HUD location", "1 - left-upper corner", "2 - right-upper corner", "3 - left-bottom corner", "4 - right-bottom corner")
                .worldRestart()
                .defineInRange("Armor HUD Location", 1, 1, 4);
        ClientConfig.ArmorHud.hudOffsetX = builder.worldRestart()
                .defineInRange("Horizontal offset of HUD", 0, 0, 100);
        ClientConfig.ArmorHud.hudOffsetY = builder.worldRestart()
                .defineInRange("Vertical offset of HUD", 0, 0, 100);
        builder.pop();

        builder.comment("Config options for Shaders and Post-processing Effects").push("Shader Options");

        builder.comment("Config options for Mod Compatibility")
        builder.push("Compatibility Options")
        @Config.RequiresMcRestart
        public static CompatibilityOptions compat = new CompatibilityOptions();

        builder.comment("Config options for GT Machines, Pipes, Cables, and Electric Items")
        builder.push("Machine Options")
        @Config.RequiresMcRestart
        public static MachineOptions machines = new MachineOptions();

        builder.comment("Config options for miscellaneous features")
        builder.push("Miscellaneous Options")
        @Config.RequiresMcRestart
        public static MiscOptions misc = new MiscOptions();

        builder.comment("Config Options for GregTech and Vanilla Recipes")
        builder.push("Recipe Options")
        public static RecipeOptions recipes = new RecipeOptions();
        builder.comment("Config options for Tools and Armor").push("Tool and Armor Options")
        public static ToolOptions tools = new ToolOptions();

        builder.comment("Config options for World Generation features")
        builder.("Worldgen Options")
        @Config.RequiresMcRestart
        public static WorldGenOptions worldgen = new WorldGenOptions();


        public static class MachineOptions {

        builder.comment({"Whether insufficient energy supply should reset Machine recipe progress to zero.",
                    "If true, progress will reset.", "If false, progress will decrease to zero with 2x speed", "Default: false"})
            public boolean recipeProgressLowEnergy = false;

        builder.comment({"Whether to require a Wrench, Wirecutter, or other GregTech tools to break machines, casings, wires, and more.", "Default: false"})
            public boolean requireGTToolsForBlocks = false;

        builder.comment({"Whether to enable the Maintenance Hatch, required for Multiblocks.", "Default: true"})
            public boolean enableMaintenance = true;

        builder.comment({"Whether to enable High-Tier Solar Panels (IV-UV). They will not have recipes.", "Default: false"})
            public boolean enableHighTierSolars = false;

        builder.comment({"Whether to enable World Accelerators, which accelerate ticks for surrounding Tile Entities, Crops, etc.", "Default: true"})
            public boolean enableWorldAccelerators = true;

        builder.comment({"Whether to use GT6-style pipe and cable connections, meaning they will not auto-connect " +
                    "unless placed directly onto another pipe or cable.", "Default: true"})
            public boolean gt6StylePipesCables = true;

        builder.comment({"Divisor for Recipe Duration per Overclock.", "Default: 2.0"})
            @Config.RangeDouble(min = 2.0, max = 3.0)
            @Config.SlidingOption
            public double overclockDivisor = 2.0;

        builder.comment({"Whether Steam Multiblocks should use Steel instead of Bronze.", "Default: false"})
            public boolean steelSteamMultiblocks = false;

        builder.comment({"Steam to EU multiplier for Steam Multiblocks.", "1.0 means 1L Steam -> 1 EU. 0.5 means 2L Steam -> 1 EU.", "Default: 0.5"})
            public double multiblockSteamToEU = 0.5;

        builder.comment({"Whether machines or boilers damage the terrain when they explode.",
                    "Note machines and boilers always explode when overloaded with power or met with special conditions, regardless of this config.", "Default: true"})
            public boolean doesExplosionDamagesTerrain = true;

        builder.comment({"Whether machines explode in rainy weather or when placed next to certain terrain, such as fire or lava", "Default: false"})
            public boolean doTerrainExplosion = false;

        builder.comment({"Energy use multiplier for electric items.", "Default: 100"})
            public int energyUsageMultiplier = 100;

        builder.comment({"The EU/t drain for each screen of the Central Monitor.", "Default: 8"})
            @Config.RangeInt(min = 0)
            public int centralMonitorEuCost = 8;

        builder.comment({"Whether to play machine sounds while machines are active.", "Default: true"})
            public boolean machineSounds = true;

        builder.comment({"Additional Fluids to allow in GT Boilers in place of Water or Distilled Water.",
                    "Useful for mods like TerraFirmaCraft with different Fluids for Water", "Default: none"})
            public String[] boilerFluids = new String[0];

        builder.comment({"Blacklist of machines for the Processing Array.",
                    "Add the unlocalized Recipe Map name to blacklist the machine.",
                    "Default: All machines allowed"})
            public String[] processingArrayBlacklist = new String[0];

        builder.comment({"Whether to enable the cleanroom, required for various recipes.", "Default: true"})
            public boolean enableCleanroom = true;

        builder.comment({"Whether multiblocks should ignore all cleanroom requirements.",
                    "This does nothing if B:enableCleanroom is false.",
                    "Default: false"})
            public boolean cleanMultiblocks = false;

        builder.comment({"Block to replace mined ores with in the miner and multiblock miner.", "Default: minecraft:cobblestone"})
            public String replaceMinedBlocksWith = "minecraft:cobblestone";
        }

        public static class WorldGenOptions {

        builder.comment({"Specifies the minimum number of veins in a section.", "Default: 1"})
            public int minVeinsInSection = 1;

        builder.comment({"Specifies an additional random number of veins in a section.", "Default: 0"})
            public int additionalVeinsInSection = 0;

        builder.comment({"Whether veins should be generated in the center of chunks.", "Default: true"})
            public boolean generateVeinsInCenterOfChunk = true;

        builder.comment({"Whether to disable Vanilla ore generation in world.", "Default: true"})
            public boolean disableVanillaOres = true;

        builder.comment({"Whether to disable Rubber Tree world generation.", "Default: false"})
            public boolean disableRubberTreeGeneration = false;

        builder.comment({"Multiplier for the chance to spawn a Rubber Tree on any given roll. Higher values make Rubber Trees more common.", "Default: 1.0"})
            @Config.RangeDouble(min = 0)
            public double rubberTreeRateIncrease = 1.0;

        builder.comment({"Whether to increase number of rolls for dungeon chests. Increases dungeon loot drastically.", "Default: true"})
            public boolean increaseDungeonLoot = true;

        builder.comment({"Allow GregTech to add additional GregTech Items as loot in various structures.", "Default: true"})
            public boolean addLoot = true;

        builder.comment({"Should all Stone Types drop unique Ore Item Blocks?", "Default: false (meaning only Stone, Netherrack, and Endstone"})
            public boolean allUniqueStoneTypes = false;
        }

        public static class RecipeOptions {

        builder.comment({"Change the recipe of Rods in the Lathe to 1 Rod and 2 Small Piles of Dust, instead of 2 Rods.", "Default: false"})
            public boolean harderRods = false;

        builder.comment({"Whether to make Glass related recipes harder. Default: true"})
            public boolean hardGlassRecipes = true;

        builder.comment({"Whether to nerf Wood crafting to 2 Planks from 1 Log, and 2 Sticks from 2 Planks.", "Default: false"})
            public boolean nerfWoodCrafting = false;

        builder.comment({"Whether to nerf the Paper crafting recipe.", "Default: true"})
            public boolean nerfPaperCrafting = true;

        builder.comment({"Whether to make Wood related recipes harder.", "Excludes sticks and planks.", "Default: false"})
            public boolean hardWoodRecipes = false;

        builder.comment({"Whether to make Redstone related recipes harder.", "Default: false"})
            public boolean hardRedstoneRecipes = false;

        builder.comment({"Recipes for Buckets, Cauldrons, Hoppers, and Iron Bars" +
                    " require Iron Plates, Rods, and more.", "Default: true"})
            public boolean hardIronRecipes = true;

        builder.comment({"Recipes for items like Iron Doors, Trapdoors, Anvil" +
                    " require Iron Plates, Rods, and more.", "Default: false"})
            public boolean hardAdvancedIronRecipes = false;

        builder.comment({"Whether to make miscellaneous recipes harder.", "Default: false"})
            public boolean hardMiscRecipes = false;

        builder.comment({"Whether to make coloring blocks like Concrete or Glass harder.", "Default: false"})
            public boolean hardDyeRecipes = false;

        builder.comment({"Whether to remove charcoal smelting recipes from the vanilla furnace.", "Default: true"})
            public boolean harderCharcoalRecipe = true;

        builder.comment({"Whether to make the Flint and Steel recipe require steel parts.", "Default: true."})
            public boolean flintAndSteelRequireSteel = true;

        builder.comment({"Whether to make Vanilla Tools and Armor recipes harder.", "Excludes Flint and Steel, and Buckets.", "Default: false"})
            public boolean hardToolArmorRecipes = false;

        builder.comment({"Whether to disable the Vanilla Concrete from Powder with Water behavior, forcing the GT recipe.", "Default: false"})
            public boolean disableConcreteInWorld = false;

        builder.comment({"Whether to generate Flawed and Chipped Gems for materials and recipes involving them.",
                    "Useful for mods like TerraFirmaCraft.", "Default: false"})
            public boolean generateLowQualityGems = false;

        builder.comment({"Whether to remove Block/Ingot compression and decompression in the Crafting Table.", "Default: false"})
            public boolean disableManualCompression = false;

        builder.comment({"Whether to remove Vanilla Block Recipes from the Crafting Table.", "Default: false"})
            public boolean removeVanillaBlockRecipes = false;

        builder.comment({"Whether to make crafting recipes for Bricks, Firebricks, and Coke Bricks harder.", "Default: false"})
            public boolean harderBrickRecipes = false;

        builder.comment({"Whether to make the recipe for the EBF Controller harder.", "Default: false"})
            public boolean harderEBFControllerRecipe = false;
        }

        public static class CompatibilityOptions {

        builder.comment("Config options regarding GTEU compatibility with other energy systems")
                builder.push("Energy Compat Options")
            public EnergyCompatOptions energy = new EnergyCompatOptions();

        builder.comment({"Whether to hide facades of all blocks in JEI and creative search menu.", "Default: true"})
            public boolean hideFacadesInJEI = true;

        builder.comment({"Whether to hide filled cells in JEI and creative search menu.", "Default: true"})
            public boolean hideFilledCellsInJEI = true;

        builder.comment({"Specifies priorities of mods in Ore Dictionary item registration.", "First ModID has highest priority, last has lowest. " +
                    "Unspecified ModIDs follow standard sorting, but always have lower priority than the last specified ModID.", "Default: [\"minecraft\", \"gregtech\"]"})
            public String[] modPriorities = {
                    "minecraft",
                    "gregtech"
            };

        builder.comment({"Whether Gregtech should remove smelting recipes from the vanilla furnace for ingots requiring the Electric Blast Furnace.", "Default: true"})
            public boolean removeSmeltingForEBFMetals = true;

            public static class EnergyCompatOptions {

            builder.comment({"Enable Native GTEU to Forge Energy (RF and alike) on GT Cables and Wires.", "This does not enable nor disable Converters.", "Default: true"})
                public boolean nativeEUToFE = true;

            builder.comment({"Enable GTEU to FE (and vice versa) Converters.", "Default: false"})
                public boolean enableFEConverters = false;

            builder.comment({"Forge Energy to GTEU ratio for converting FE to EU.", "Only affects converters.", "Default: 4 FE == 1 EU"})
                @Config.RangeInt(min = 1, max = 16)
                public int feToEuRatio = 4;

            builder.comment({"GTEU to Forge Energy ratio for converting EU to FE.", "Affects native conversion and Converters.", "Default: 4 FE == 1 EU"})
                @Config.RangeInt(min = 1, max = 16)
                public int euToFeRatio = 4;
            }
        }

        public static class MiscOptions {

        builder.comment({"Whether to enable more verbose logging.", "Default: false"})
            public boolean debug = false;

        builder.comment({"Setting this to true makes GTCEu ignore error and invalid recipes that would otherwise cause crash.", "Default: true"})
            public boolean ignoreErrorOrInvalidRecipes = true;

        builder.comment({"Whether to enable a login message to players when they join the world.", "Default: true"})
            public boolean loginMessage = true;

        @Config.RangeInt(min = 0, max = 100)
                builder.comment({"Chance with which flint and steel will create fire.", "Default: 50"})
            @Config.SlidingOption
            public int flintChanceToCreateFire = 50;

        builder.comment({"Whether to give the terminal to new players on login", "Default: true"})
            public boolean spawnTerminal = true;

        }



        public static class FusionBloom {
        builder.comment({"Whether to use shader programs.", "Default: true"})
            public boolean useShader = true;

        builder.comment({"Bloom Strength", "OUTPUT = BACKGROUND + BLOOM * {strength} * (base + LT + (1 - BACKGROUND_BRIGHTNESS)*(HT-LT)))", "Default: 2"})
            @Config.RangeDouble(min = 0)
            public double strength = 1.5;

        builder.comment({"Bloom Algorithm", "0 - Simple Gaussian Blur Bloom (Fast)", "1 - Unity Bloom", "2 - Unreal Bloom", "Default: 2"})
            @Config.RangeInt(min = 0, max = 2)
            @Config.SlidingOption
            public int bloomStyle = 1;

        builder.comment({"The brightness after bloom should not exceed this value. It can be used to limit the brightness of highlights " +
                    "(e.g., daytime).", "OUTPUT = BACKGROUND + BLOOM * strength * (base + LT + (1 - BACKGROUND_BRIGHTNESS)*({HT}-LT)))", "This value should be greater than lowBrightnessThreshold.", "Default: 0.5"})
            @Config.RangeDouble(min = 0)
            public double highBrightnessThreshold = 1.3;

        builder.comment({"The brightness after bloom should not smaller than this value. It can be used to limit the brightness of dusky parts " +
                    "(e.g., night/caves).", "OUTPUT = BACKGROUND + BLOOM * strength * (base + {LT} + (1 - BACKGROUND_BRIGHTNESS)*(HT-{LT})))", "This value should be smaller than highBrightnessThreshold.", "Default: 0.2"})
            @Config.RangeDouble(min = 0)
            public double lowBrightnessThreshold = 0.3;

        builder.comment({"The base brightness of the bloom.", "It is similar to strength", "This value should be smaller than highBrightnessThreshold.", "OUTPUT = BACKGROUND + BLOOM * strength * ({base} + LT + (1 - BACKGROUND_BRIGHTNESS)*(HT-LT)))", "Default: 0.1"})
            @Config.RangeDouble(min = 0)
            public double baseBrightness = 0;
        }

        public static class HeatEffectBloom {
        builder.comment({"Whether to use shader programs.", "Default: true"})
            public boolean useShader = true;

        builder.comment({"Bloom Strength", "OUTPUT = BACKGROUND + BLOOM * {strength} * (base + LT + (1 - BACKGROUND_BRIGHTNESS)*(HT-LT)))", "Default: 2"})
            @Config.RangeDouble(min = 0)
            public double strength = 1.1;

        builder.comment({"Bloom Algorithm", "0 - Simple Gaussian Blur Bloom (Fast)", "1 - Unity Bloom", "2 - Unreal Bloom", "Default: 2"})
            @Config.RangeInt(min = 0, max = 2)
            @Config.SlidingOption
            public int bloomStyle = 2;

        builder.comment({"The brightness after bloom should not exceed this value. It can be used to limit the brightness of highlights " +
                    "(e.g., daytime).", "OUTPUT = BACKGROUND + BLOOM * strength * (base + LT + (1 - BACKGROUND_BRIGHTNESS)*({HT}-LT)))", "This value should be greater than lowBrightnessThreshold.", "Default: 0.5"})
            @Config.RangeDouble(min = 0)
            public double highBrightnessThreshold = 1.4;

        builder.comment({"The brightness after bloom should not smaller than this value. It can be used to limit the brightness of dusky parts " +
                    "(e.g., night/caves).", "OUTPUT = BACKGROUND + BLOOM * strength * (base + {LT} + (1 - BACKGROUND_BRIGHTNESS)*(HT-{LT})))", "This value should be smaller than highBrightnessThreshold.", "Default: 0.2"})
            @Config.RangeDouble(min = 0)
            public double lowBrightnessThreshold = 0.6;

        builder.comment({"The base brightness of the bloom.", "It is similar to strength", "This value should be smaller than highBrightnessThreshold.", "OUTPUT = BACKGROUND + BLOOM * strength * ({base} + LT + (1 - BACKGROUND_BRIGHTNESS)*(HT-LT)))", "Default: 0.1"})
            @Config.RangeDouble(min = 0)
            public double baseBrightness = 0;
        }

        public static class ToolOptions {

        builder.push("NanoSaber Options")
            public NanoSaber nanoSaber = new NanoSaber();

        builder.comment("NightVision Goggles Voltage Tier. Default: 1 (LV)")
            @Config.RangeInt(min = 0, max = 14)
            public int voltageTierNightVision = 1;

        builder.comment("NanoSuit Voltage Tier. Default: 3 (HV)")
            @Config.RangeInt(min = 0, max = 14)
            public int voltageTierNanoSuit = 3;

        builder.comment({"Advanced NanoSuit Chestplate Voltage Tier.", "Default: 3 (HV)"})
            @Config.RangeInt(min = 0, max = 14)
            public int voltageTierAdvNanoSuit = 3;

        builder.comment({"QuarkTech Suit Voltage Tier.", "Default: 5 (IV)"})
            @Config.RangeInt(min = 0, max = 14)
            @Config.SlidingOption
            public int voltageTierQuarkTech = 5;

        builder.comment({"Advanced QuarkTech Suit Chestplate Voltage Tier.", "Default: 5 (LuV)"})
            @Config.RangeInt(min = 0, max = 14)
            public int voltageTierAdvQuarkTech = 6;

        builder.comment({"Electric Impeller Jetpack Voltage Tier.", "Default: 2 (MV)"})
            @Config.RangeInt(min = 0, max = 14)
            public int voltageTierImpeller = 2;

        builder.comment({"Advanced Electric Jetpack Voltage Tier.", "Default: 3 (HV)"})
            @Config.RangeInt(min = 0, max = 14)
            public int voltageTierAdvImpeller = 3;

        builder.comment({"Random chance for electric tools to take actual damage", "Default: 10%"})
            @Config.RangeInt(min = 0, max = 100)
            @Config.SlidingOption
            public int rngDamageElectricTools = 10;

        builder.comment("Armor HUD Location")
            public ArmorHud armorHud = new ArmorHud();
        }

        public static class ArmorHud {
        builder.comment({"Sets HUD location", "1 - left-upper corner", "2 - right-upper corner", "3 - left-bottom corner", "4 - right-bottom corner"})
            public byte hudLocation = 1;
        builder.comment("Horizontal offset of HUD [0 ~ 100)")
            public byte hudOffsetX = 0;
        builder.comment("Vertical offset of HUD [0 ~ 100)")
            public byte hudOffsetY = 0;
        }

        public static class NanoSaber {

        @Config.RangeDouble(min = 0, max = 100)
                builder.comment({"The additional damage added when the NanoSaber is powered.", "Default: 20.0"})
            public double nanoSaberDamageBoost = 20;

        @Config.RangeDouble(min = 0, max = 100)
                builder.comment({"The base damage of the NanoSaber.", "Default: 5.0"})
            public double nanoSaberBaseDamage = 5;

        builder.comment({"Should Zombies spawn with charged, active NanoSabers on hard difficulty?", "Default: true"})
            public boolean zombieSpawnWithSabers = true;

        @Config.RangeInt(min = 1, max = 512)
                builder.comment({"The EU/t consumption of the NanoSaber.", "Default: 64"})
            public int energyConsumption = 64;
        }
    }
}