package net.nemezanevem.gregtech.api.unification.material.properties.info;

import net.minecraftforge.registries.RegistryObject;
import net.nemezanevem.gregtech.api.registry.material.info.MaterialFlagRegistry;
import net.nemezanevem.gregtech.api.unification.material.properties.GtMaterialProperties;

public class GtMaterialFlags {

    /////////////////
    //   GENERIC   //
    /////////////////

    /**
     * Add to material to disable it's unification fully
     */
    public static final RegistryObject<MaterialFlag> NO_UNIFICATION = MaterialFlagRegistry.MATERIAL_FLAGS.register("no_unification", () -> new MaterialFlag.Builder().build());

    /**
     * Enables electrolyzer decomposition recipe generation
     */
    public static final RegistryObject<MaterialFlag> DECOMPOSITION_BY_ELECTROLYZING = MaterialFlagRegistry.MATERIAL_FLAGS.register("decomposition_by_electrolyzing", () ->  new MaterialFlag.Builder().build());

    /**
     * Enables centrifuge decomposition recipe generation
     */
    public static final RegistryObject<MaterialFlag> DECOMPOSITION_BY_CENTRIFUGING = MaterialFlagRegistry.MATERIAL_FLAGS.register("decomposition_by_centrifuging", () -> new MaterialFlag.Builder().build());

    /**
     * Disables decomposition recipe generation for this material
     */
    public static final RegistryObject<MaterialFlag> DISABLE_DECOMPOSITION = MaterialFlagRegistry.MATERIAL_FLAGS.register("disable_decomposition", () -> new MaterialFlag.Builder().build());

    /**
     * Add to material if it is some kind of explosive
     */
    public static final RegistryObject<MaterialFlag> EXPLOSIVE = MaterialFlagRegistry.MATERIAL_FLAGS.register("explosive", () -> new MaterialFlag.Builder().build());

    /**
     * Add to material if it is some kind of flammable
     */
    public static final RegistryObject<MaterialFlag> FLAMMABLE = MaterialFlagRegistry.MATERIAL_FLAGS.register("flammable", () -> new MaterialFlag.Builder().build());

    /**
     * Add to material if it is some kind of sticky
     */
    public static final RegistryObject<MaterialFlag> STICKY = MaterialFlagRegistry.MATERIAL_FLAGS.register("sticky", () -> new MaterialFlag.Builder().build());

    //////////////////
    //     DUST     //
    //////////////////

    /**
     * Generate a plate for this material
     * If it's dust material, dust compressor recipe into plate will be generated
     * If it's metal material, bending machine recipes will be generated
     * If block is found, cutting machine recipe will be also generated
     */
    public static final RegistryObject<MaterialFlag> GENERATE_PLATE = MaterialFlagRegistry.MATERIAL_FLAGS.register("generate_plate", () -> new MaterialFlag.Builder()
            .requireProps(GtMaterialProperties.DUST.get())
            .build()
    );

    public static final RegistryObject<MaterialFlag> GENERATE_ROD = MaterialFlagRegistry.MATERIAL_FLAGS.register("generate_rod", () -> new MaterialFlag.Builder()
            .requireProps(GtMaterialProperties.DUST.get())
            .build()
    );

    public static final RegistryObject<MaterialFlag> GENERATE_BOLT_SCREW = MaterialFlagRegistry.MATERIAL_FLAGS.register("generate_bolt_screw", () -> new MaterialFlag.Builder()
            .requireFlags(GENERATE_ROD.get())
            .requireProps(GtMaterialProperties.DUST.get())
            .build()
    );

    public static final RegistryObject<MaterialFlag> GENERATE_FRAME = MaterialFlagRegistry.MATERIAL_FLAGS.register("generate_frame", () -> new MaterialFlag.Builder()
            .requireFlags(GENERATE_ROD.get())
            .requireProps(GtMaterialProperties.DUST.get())
            .build()
    );

    public static final RegistryObject<MaterialFlag> GENERATE_GEAR = MaterialFlagRegistry.MATERIAL_FLAGS.register("generate_gear", () -> new MaterialFlag.Builder()
            .requireFlags(GENERATE_PLATE.get(), GENERATE_ROD.get())
            .requireProps(GtMaterialProperties.DUST.get())
            .build()
    );

    public static final RegistryObject<MaterialFlag> GENERATE_LONG_ROD = MaterialFlagRegistry.MATERIAL_FLAGS.register("generate_long_rod", () -> new MaterialFlag.Builder()
            .requireFlags(GENERATE_ROD.get())
            .requireProps(GtMaterialProperties.DUST.get())
            .build()
    );

    public static final RegistryObject<MaterialFlag> FORCE_GENERATE_BLOCK = MaterialFlagRegistry.MATERIAL_FLAGS.register("force_generate_block", () -> new MaterialFlag.Builder()
            .requireProps(GtMaterialProperties.DUST.get())
            .build()
    );

    /**
     * This will prevent material from creating Shapeless recipes for dust to block and vice versa
     * Also preventing extruding and alloy smelting recipes via SHAPE_EXTRUDING/MOLD_BLOCK
     */
    public static final RegistryObject<MaterialFlag> EXCLUDE_BLOCK_CRAFTING_RECIPES = MaterialFlagRegistry.MATERIAL_FLAGS.register("exclude_block_crafting_recipes", () -> new MaterialFlag.Builder()
            .requireProps(GtMaterialProperties.DUST.get())
            .build()
    );

    public static final RegistryObject<MaterialFlag> EXCLUDE_PLATE_COMPRESSOR_RECIPE = MaterialFlagRegistry.MATERIAL_FLAGS.register("exclude_plate_compressor_recipe", () -> new MaterialFlag.Builder()
            .requireFlags(GENERATE_PLATE.get())
            .requireProps(GtMaterialProperties.DUST.get())
            .build()
    );

    /**
     * This will prevent material from creating Shapeless recipes for dust to block and vice versa
     */
    public static final RegistryObject<MaterialFlag> EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES = MaterialFlagRegistry.MATERIAL_FLAGS.register("exclude_block_crafting_by_hand_recipes", () -> new MaterialFlag.Builder()
            .requireProps(GtMaterialProperties.DUST.get())
            .build()
    );

    public static final RegistryObject<MaterialFlag> MORTAR_GRINDABLE = MaterialFlagRegistry.MATERIAL_FLAGS.register("mortar_grindable", () -> new MaterialFlag.Builder()
            .requireProps(GtMaterialProperties.DUST.get())
            .build()
    );

    /**
     * Add to material if it cannot be worked by any other means, than smashing or smelting. This is used for coated Materials.
     */
    public static final RegistryObject<MaterialFlag> NO_WORKING = MaterialFlagRegistry.MATERIAL_FLAGS.register("no_working", () -> new MaterialFlag.Builder()
            .requireProps(GtMaterialProperties.DUST.get())
            .build()
    );

    /**
     * Add to material if it cannot be used for regular Metal working techniques since it is not possible to bend it.
     */
    public static final RegistryObject<MaterialFlag> NO_SMASHING = MaterialFlagRegistry.MATERIAL_FLAGS.register("no_smashing", () -> new MaterialFlag.Builder()
            .requireProps(GtMaterialProperties.DUST.get())
            .build()
    );

    /**
     * Add to material if it's impossible to smelt it
     */
    public static final RegistryObject<MaterialFlag> NO_SMELTING = MaterialFlagRegistry.MATERIAL_FLAGS.register("no_smelting", () -> new MaterialFlag.Builder()
            .requireProps(GtMaterialProperties.DUST.get())
            .build()
    );

    /**
     * Add this to your Material if you want to have its Ore Calcite heated in a Blast Furnace for more output. Already listed are:
     * Iron, Pyrite, PigIron, WroughtIron.
     */
    public static final RegistryObject<MaterialFlag> BLAST_FURNACE_CALCITE_DOUBLE = MaterialFlagRegistry.MATERIAL_FLAGS.register("blast_furnace_calcite_double", () -> new MaterialFlag.Builder()
            .requireProps(GtMaterialProperties.DUST.get())
            .build()
    );

    public static final RegistryObject<MaterialFlag> BLAST_FURNACE_CALCITE_TRIPLE = MaterialFlagRegistry.MATERIAL_FLAGS.register("blast_furnace_calcite_triple", () -> new MaterialFlag.Builder()
            .requireProps(GtMaterialProperties.DUST.get())
            .build()
    );

    /////////////////
    //    FLUID    //
    /////////////////

    public static final RegistryObject<MaterialFlag> SOLDER_MATERIAL = MaterialFlagRegistry.MATERIAL_FLAGS.register("solder_material", () -> new MaterialFlag.Builder()
            .requireProps(GtMaterialProperties.FLUID.get())
            .build()
    );

    public static final RegistryObject<MaterialFlag> SOLDER_MATERIAL_BAD = MaterialFlagRegistry.MATERIAL_FLAGS.register("solder_material_bad", () -> new MaterialFlag.Builder()
            .requireProps(GtMaterialProperties.FLUID.get())
            .build()
    );

    public static final RegistryObject<MaterialFlag> SOLDER_MATERIAL_GOOD = MaterialFlagRegistry.MATERIAL_FLAGS.register("solder_material_good", () -> new MaterialFlag.Builder()
            .requireProps(GtMaterialProperties.FLUID.get())
            .build()
    );

    /////////////////
    //    INGOT    //
    /////////////////

    public static final RegistryObject<MaterialFlag> GENERATE_FOIL = MaterialFlagRegistry.MATERIAL_FLAGS.register("generate_foil", () -> new MaterialFlag.Builder()
            .requireFlags(GENERATE_PLATE.get())
            .requireProps(GtMaterialProperties.INGOT.get())
            .build()
    );

    public static final RegistryObject<MaterialFlag> GENERATE_RING = MaterialFlagRegistry.MATERIAL_FLAGS.register("generate_ring", () -> new MaterialFlag.Builder()
            .requireFlags(GENERATE_ROD.get())
            .requireProps(GtMaterialProperties.INGOT.get())
            .build()
    );

    public static final RegistryObject<MaterialFlag> GENERATE_SPRING = MaterialFlagRegistry.MATERIAL_FLAGS.register("generate_spring", () -> new MaterialFlag.Builder()
            .requireFlags(GENERATE_LONG_ROD.get())
            .requireProps(GtMaterialProperties.INGOT.get())
            .build()
    );

    public static final RegistryObject<MaterialFlag> GENERATE_SPRING_SMALL = MaterialFlagRegistry.MATERIAL_FLAGS.register("generate_spring_small", () -> new MaterialFlag.Builder()
            .requireFlags(GENERATE_ROD.get())
            .requireProps(GtMaterialProperties.INGOT.get())
            .build()
    );

    public static final RegistryObject<MaterialFlag> GENERATE_SMALL_GEAR = MaterialFlagRegistry.MATERIAL_FLAGS.register("generate_small_gear", () -> new MaterialFlag.Builder()
            .requireFlags(GENERATE_PLATE.get(), GENERATE_ROD.get())
            .requireProps(GtMaterialProperties.INGOT.get())
            .build()
    );

    public static final RegistryObject<MaterialFlag> GENERATE_FINE_WIRE = MaterialFlagRegistry.MATERIAL_FLAGS.register("generate_fine_wire", () -> new MaterialFlag.Builder()
            .requireFlags(GENERATE_FOIL.get())
            .requireProps(GtMaterialProperties.INGOT.get())
            .build()
    );

    public static final RegistryObject<MaterialFlag> GENERATE_ROTOR = MaterialFlagRegistry.MATERIAL_FLAGS.register("generate_rotor", () -> new MaterialFlag.Builder()
            .requireFlags(GENERATE_BOLT_SCREW.get(), GENERATE_RING.get(), GENERATE_PLATE.get())
            .requireProps(GtMaterialProperties.INGOT.get())
            .build()
    );

    public static final RegistryObject<MaterialFlag> GENERATE_DENSE = MaterialFlagRegistry.MATERIAL_FLAGS.register("generate_dense", () -> new MaterialFlag.Builder()
            .requireFlags(GENERATE_PLATE.get())
            .requireProps(GtMaterialProperties.INGOT.get())
            .build()
    );

    public static final RegistryObject<MaterialFlag> GENERATE_ROUND = MaterialFlagRegistry.MATERIAL_FLAGS.register("generate_round", () -> new MaterialFlag.Builder()
            .requireProps(GtMaterialProperties.INGOT.get())
            .build()
    );

    /**
     * Add this to your Material if it is a magnetized form of another Material.
     */
    public static final RegistryObject<MaterialFlag> IS_MAGNETIC = MaterialFlagRegistry.MATERIAL_FLAGS.register("is_magnetic", () -> new MaterialFlag.Builder()
            .requireProps(GtMaterialProperties.INGOT.get())
            .build()
    );

    /////////////////
    //     GEM     //
    /////////////////

    /**
     * If this material can be crystallized.
     */
    public static final RegistryObject<MaterialFlag> CRYSTALLIZABLE = MaterialFlagRegistry.MATERIAL_FLAGS.register("crystallizable", () -> new MaterialFlag.Builder()
            .requireProps(GtMaterialProperties.GEM.get())
            .build()
    );

    public static final RegistryObject<MaterialFlag> GENERATE_LENS = MaterialFlagRegistry.MATERIAL_FLAGS.register("generate_lens", () -> new MaterialFlag.Builder()
            .requireFlags(GENERATE_PLATE.get())
            .requireProps(GtMaterialProperties.GEM.get())
            .build()
    );

    /////////////////
    //     ORE     //
    /////////////////

    public static final RegistryObject<MaterialFlag> HIGH_SIFTER_OUTPUT = MaterialFlagRegistry.MATERIAL_FLAGS.register("high_sifter_output", () -> new MaterialFlag.Builder()
            .requireProps(GtMaterialProperties.GEM.get(), GtMaterialProperties.ORE.get())
            .build()
    );
}
