package net.nemezanevem.gregtech.api.recipe;


import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.gui.widgets.ProgressWidget;
import net.nemezanevem.gregtech.api.gui.widgets.ProgressWidget.MoveType;
import net.nemezanevem.gregtech.api.recipe.builder.SimpleRecipeBuilder;

import static net.nemezanevem.gregtech.api.GTValues.LV;
import static net.nemezanevem.gregtech.api.GTValues.VA;

public class GtRecipeTypes {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, GregTech.MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, GregTech.MODID);

    public static final RegistryObject<RecipeSerializer<GTRecipe>> SIMPLE_SERIALIZER = RECIPE_SERIALIZERS.register("simple_recipe_serializer", GTRecipe.Serializer::new);

    /**
     * Example:
     * <pre>
     * 	 	GTRecipeType.ALLOY_SMELTER_RECIPES.recipeBuilder()
     * 				.input(OrePrefix.ingot, Materials.Tin)
     * 			    .input(OrePrefix.ingot, Materials.Copper, 3)
     * 			    .output(OrePrefix.ingot, Materials.Bronze, 4)
     * 				.duration(600)
     * 				.EUt(5)
     * 				.buildAndRegister();
     * </pre>
     *
     * This is a relatively simple example for creating Bronze.
     * Note that the use of <B>OrePrefix</B> ensures that OreDictionary Entries are used for the recipe.
     */
    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> ALLOY_SMELTER_RECIPES = RECIPE_TYPES.register("alloy_smelter", () ->
            new GTRecipeType<>(1, 2, 1, 1, 0, 0, 0, 0, new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, false, GuiTextures.FURNACE_OVERLAY_1)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.FURNACE));

    /**
     * Example:
     * <pre>
     * 	 	GTRecipeType.ARC_FURNACE_RECIPES.recipeBuilder()
     * 				.input(OrePrefix.ingot, Materials.Iron)
     * 			    .output(OrePrefix.ingot, Materials.WroughtIron)
     * 				.duration(200)
     * 				.EUt(GTValues.VA[GTValues.LV])
     * 				.buildAndRegister();
     * </pre>
     *
     * The Arc Furnace has a special action that is performed when the recipe is built, designated by the <B>onRecipeBuild</B>
     * call on the Recipe Map. This action checks that there are no fluid inputs supplied for the recipe, and if true adds
     * Oxygen equal to the recipe duration. This behavior can be negated by supplying your own fluid to the recipe, such as
     *
     * <pre>
     * 	 	GTRecipeType.ARC_FURNACE_RECIPES.recipeBuilder()
     * 				.input(OrePrefix.ingot, Materials.Iron)
     * 			    .fluidInputs(Materials.Water.getFluid(100))
     * 			    .output(OrePrefix.ingot, Materials.WroughtIron)
     * 				.duration(200)
     * 				.EUt(GTValues.VA[GTValues.LV])
     * 				.buildAndRegister();
     * </pre>
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> ARC_FURNACE_RECIPES = new GTRecipeType<>("arc_furnace", 1, 1, 1, 4, 1, 1, 0, 1, new SimpleRecipeBuilder(), false)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARC_FURNACE, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.ARC)
            .onRecipeBuild(recipeBuilder -> {
                recipeBuilder.invalidateOnBuildAction();
                if (recipeBuilder.getFluidInputs().isEmpty()) {
                    recipeBuilder.fluidInputs(Materials.Oxygen.getFluid(recipeBuilder.duration));
                }
            });

    /**
     * Example:
     * <pre>
     *      GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder()
     *               .circuitMeta(2)
     *               .inputs(new ItemStack(Items.COAL, 1, GTValues.W))
     *               .input(OrePrefix.stick, Materials.Wood, 1)
     *               .outputs(new ItemStack(Blocks.TORCH, 4))
     *               .duration(100).EUt(1).buildAndRegister();
     * </pre>
     *
     * Since the Assembler is an <I>IntCircuitRecipeBuilder</I>, it has access to the <B>circuitMeta()</B> builder entry.
     * This entry adds an integrated circuit into a recipe, with configuration numbers 0 - 32 allowed
     */

    public static final RegistryObject<GTRecipeType<IntCircuitRecipeBuilder>> ASSEMBLER_RECIPES = new GTRecipeType<>("assembler", 1, 9, 1, 1, 0, 1, 0, 0, new AssemblerRecipeBuilder(), false)
            .setSlotOverlay(false, false, GuiTextures.CIRCUIT_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_CIRCUIT, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.ASSEMBLER);

    /**
     * Example:
     * <pre>
     *      GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder()
     *               .input(OrePrefix.stickLong, Materials.SamariumMagnetic)
     *               .input(OrePrefix.stickLong, Materials.HSSS, 2)
     *               .input(OrePrefix.ring, Materials.HSSS, 2)
     *               .input(OrePrefix.round, Materials.HSSS, 4)
     *               .input(OrePrefix.wireFine, Materials.Ruridit, 64)
     *               .input(OrePrefix.cableGtSingle, Materials.NiobiumTitanium, 2)
     *               .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
     *               .fluidInputs(Materials.Lubricant.getFluid(250))
     *               .output(MetaItems.ELECTRIC_MOTOR_LuV)
     *               .duration(600).EUt(6000).buildAndRegister();
     * </pre>
     *
     * The Assembly Line Recipe Builder has no special properties/build actions yet, but will in the future
     */
    public static final GTRecipeTypeAssemblyLine<SimpleRecipeBuilder> ASSEMBLY_LINE_RECIPES = (GTRecipeTypeAssemblyLine<SimpleRecipeBuilder>) new GTRecipeTypeAssemblyLine<>("assembly_line", 4, 16, 1, 1, 0, 4, 0, 0, new SimpleRecipeBuilder(), false)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.ASSEMBLER);

    /**
     * Example:
     * <pre>
     * 	 	GTRecipeType.AUTOCLAVE_RECIPES.recipeBuilder()
     * 				.inputs(OreDictUnifier.get(OrePrefix.dust, Materials.Carbon, 16))
     * 				.fluidInputs(Materials.Lutetium.getFluid(4))
     * 				.chancedOutput(MetaItems.CARBON_FIBERS.getStackForm(2), 3333, 1000)
     * 				.duration(600)
     * 				.EUt(5)
     * 				.buildAndRegister();
     * </pre>
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> AUTOCLAVE_RECIPES = new GTRecipeType<>("autoclave", 1, 2, 1, 2, 1, 1, 0, 1, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, GuiTextures.DUST_OVERLAY)
            .setSlotOverlay(true, false, GuiTextures.CRYSTAL_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_CRYSTALLIZATION, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.FURNACE);

    /**
     * Example:
     * <pre>
     * 		GTRecipeType.BENDER_RECIPES.recipeBuilder()
     * 				.input(OrePrefix.plate, Materials.Tin, 12)
     * 			    .circuitMeta(4)
     * 				.outputs(MetaItems.FLUID_CELL.getStackForm(4))
     * 				.duration(1200)
     * 				.EUt(8)
     * 				.buildAndRegister();
     * </pre>
     *
     * Just like other IntCircuitRecipeBuilder GTRecipeTypes, <B>circuitMeta</B> can be used to easily set a circuit
     */

    public static final RegistryObject<GTRecipeType<IntCircuitRecipeBuilder>> BENDER_RECIPES = new GTRecipeType<>("bender", 2, 2, 1, 1, 0, 0, 0, 0, new IntCircuitRecipeBuilder(), false)
            .setSlotOverlay(false, false, false, GuiTextures.BENDER_OVERLAY)
            .setSlotOverlay(false, false, true, GuiTextures.INT_CIRCUIT_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_BENDING, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.MOTOR);

    /**
     * Example:
     * <pre>
     * 	    GTRecipeType.BLAST_RECIPES.recipeBuilder()
     * 				.inputs(OreDictUnifier.get(OrePrefix.dust, Materials.Glass), OreDictUnifier.get(OrePrefix.dust, Materials.Carbon))
     * 				.fluidInputs(Materials.Electrum.getFluid(16))
     * 				.outputs(ItemList.Circuit_Board_Fiberglass.get(16))
     * 				.duration(80)
     * 				.EUt(480)
     * 				.blastFurnaceTemp(2600)
     * 				.buildAndRegister();
     * </pre>
     *
     * The Electric Blast Furnace requires specification of a blast furnace temperature through the builder call of
     * <B>blastFurnaceTemp</B>. This value will set the temperature required for the recipe to run, restricting recipes
     * to certain coils.
     *
     * Anything with a Blast Furnace Temperature of greater than 1750K will also autogenerate a hot ingot and a hot ingot
     * cooling recipe.
     */

    public static final RegistryObject<GTRecipeType<BlastRecipeBuilder>> BLAST_RECIPES = new GTRecipeType<>("electric_blast_furnace", 1, 3, 0, 3, 0, 1, 0, 1, new BlastRecipeBuilder(), false)
            .setSound(GTSoundEvents.FURNACE);

    /**
     * Example:
     * <pre>
     *      GTRecipeType.BREWING_RECIPES.recipeBuilder()
     *         		.input(MetaItems.BIO_CHAFF)
     *         		.fluidInput(Materials.Water.getFluid(750))
     *         		.fluidOutput(Materials.Biomass.getFluid(750))
     *         	    .duration(128).EUt(4)
     *         		.buildAndRegister();
     * </pre>
     *
     * Any Recipe added to the Brewery not specifying a <B>duration</B> value will default to 128.
     * Any Recipe added to the Brewery not specifying an <B>EUt</B> value will default 4.
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> BREWING_RECIPES = new GTRecipeType<>("brewery", 1, 1, 0, 0, 1, 1, 1, 1, new SimpleRecipeBuilder().duration(128).EUt(4), false)
            .setSlotOverlay(false, false, GuiTextures.BREWER_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    /**
     * Example:
     * <pre>
     *       GTRecipeType.CANNER_RECIPES.recipeBuilder()
     * 				.input(MetaItems.BATTERY_HULL_LV)
     * 			    .input(OrePrefix.dust, Materials.Cadmium, 2)
     * 				.outputs(MetaItems.BATTERY_LV_CADMIUM)
     * 				.duration(100)
     * 				.EUt(2)
     * 				.buildAndRegister();
     * </pre>
     *
     * The Canner combines the functionality of the item canning machine and the former fluid canning machine.
     * The Canner mostly checks its recipes when used, to prevent overpopulating the JEI page for the machine.
     * It will empty or fill any fluid handler, so there is no need to add explicit recipes for the fluid handlers.
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> CANNER_RECIPES = new GTRecipeTypeFluidCanner("canner", 1, 2, 1, 2, 0, 1, 0, 1, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, false, GuiTextures.CANNER_OVERLAY)
            .setSlotOverlay(false, false, true, GuiTextures.CANISTER_OVERLAY)
            .setSlotOverlay(true, false, GuiTextures.CANISTER_OVERLAY)
            .setSlotOverlay(false, true, GuiTextures.DARK_CANISTER_OVERLAY)
            .setSlotOverlay(true, true, GuiTextures.DARK_CANISTER_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_CANNER, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.BATH);

    /**
     * Examples:
     * <pre>
     * 		GTRecipeType.CENTRIFUGE_RECIPES.recipeBuilder()
     * 				.fluidInputs(Materials.ImpureNaquadriaSolution.getFluid(2000))
     * 				.output(OrePrefix.dust, Materials.IndiumPhosphide)
     * 			    .output(OrePrefix.dust, Materials.AntimonyTrifluoride, 2)
     * 			    .fluidOutputs(Materials.NaquadriaSolution.getFluid(1000))
     * 				.duration(400).EUt(GTValues.VA[GTValues.EV])
     * 				.buildAndRegister();
     *
     * </pre>
     *
     * Most Centrifuge recipes exist because of automatic material decomposition recipes, but non-decomposition recipes
     * can still be added to the centrifuge.
     *
     * Any Centrifuge recipe not specifying an <B>EUt</B> value will have the value default to 5.
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> CENTRIFUGE_RECIPES = new GTRecipeType<>("centrifuge", 0, 2, 0, 6, 0, 1, 0, 6, new SimpleRecipeBuilder().EUt(5), false)
            .setSlotOverlay(false, false, false, GuiTextures.EXTRACTOR_OVERLAY)
            .setSlotOverlay(false, false, true, GuiTextures.CANISTER_OVERLAY)
            .setSlotOverlay(false, true, true, GuiTextures.CENTRIFUGE_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.CENTRIFUGE);

    /**
     * Example:
     * <pre>
     * 		GTRecipeType.CHEMICAL_BATH_RECIPES.recipeBuilder()
     * 				.input(OrePrefix.gem, Materials.EnderEye)
     * 				.fluidInputs(Materials.Radon.getFluid(250))
     * 				.output(MetaItems.QUANTUM_EYE)
     * 				.duration(480).EUt(GTValues.VA[GTValues.HV])
     * 				.buildAndRegister();
     * </pre>
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> CHEMICAL_BATH_RECIPES = new GTRecipeType<>("chemical_bath", 1, 1, 1, 6, 1, 1, 0, 1, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, true, GuiTextures.BREWER_OVERLAY)
            .setSlotOverlay(true, false, false, GuiTextures.DUST_OVERLAY)
            .setSlotOverlay(true, false, true, GuiTextures.DUST_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_MIXER, MoveType.CIRCULAR)
            .setSound(GTSoundEvents.BATH);

    /**
     * Example:
     * <pre>
     *      GTRecipeType.CHEMICAL_RECIPES.recipeBuilder()
     * 				.notConsumable(new IntCircuitIngredient(1))
     * 				.fluidInputs(Materials.NitrogenDioxide.getFluid(3000))
     * 			    .fluidInputs(Materials.Water.getFluid(1000))
     * 				.fluidOutputs(Materials.NitricAcid.getFluid(2000))
     * 				.fluidOutputs(Materials.NitricOxide.getFluid(1000))
     * 				.duration(240)
     * 				.EUt(GTValues.VA[GTValues.LV])
     * 				.buildAndRegister();
     * </pre>
     *
     * The Chemical Reactor has a special action that is performed for any recipe added to its recipe map, seen in its
     * <B>onRecipeBuild</B> call. Any recipe that is added to the Chemical Reactor will also be added to the
     * Large Chemical Reactor recipe map, with matching inputs, outputs, EUt, and duration.
     *
     * This action cannot be negated, unlike special build actions for other recipe maps.
     *
     * Any recipe added to the Chemical Reactor not specifying an <B>EUt</B> value will default to 30.
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder, GTRecipe>> CHEMICAL_RECIPES = new GTRecipeType<>("chemical_reactor", 0, 2, 0, 2, 0, 3, 0, 2, new SimpleRecipeBuilder().EUt(VA[LV]), false)
            .setSlotOverlay(false, false, false, GuiTextures.MOLECULAR_OVERLAY_1)
            .setSlotOverlay(false, false, true, GuiTextures.MOLECULAR_OVERLAY_2)
            .setSlotOverlay(false, true, false, GuiTextures.MOLECULAR_OVERLAY_3)
            .setSlotOverlay(false, true, true, GuiTextures.MOLECULAR_OVERLAY_4)
            .setSlotOverlay(true, false, GuiTextures.VIAL_OVERLAY_1)
            .setSlotOverlay(true, true, GuiTextures.VIAL_OVERLAY_2)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressWidget.MoveType.HORIZONTAL)
            .setSound(GTValues.FOOLS.get() ? GTSoundEvents.SCIENCE : GTSoundEvents.CHEMICAL_REACTOR)
            .onRecipeBuild(recipeBuilder -> {
                recipeBuilder.invalidateOnBuildAction();
                GTRecipeTypes.LARGE_CHEMICAL_RECIPES.recipeBuilder()
                        .inputs(recipeBuilder.getInputs().toArray(new GTRecipeInput[0]))
                        .fluidInputs(recipeBuilder.getFluidInputs())
                        .outputs(recipeBuilder.getOutputs())
                        .chancedOutputs(recipeBuilder.getChancedOutputs())
                        .fluidOutputs(recipeBuilder.getFluidOutputs())
                        .cleanroom(recipeBuilder.getCleanroom())
                        .duration(recipeBuilder.duration)
                        .EUt(recipeBuilder.EUt)
                        .buildAndRegister();
            });

    /**
     * Example:
     * <pre>
     *      GTRecipeType.CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
     * 				.input(MetaItems.BASIC_CIRCUIT_BOARD)
     * 				.input(MetaItems.INTEGRATED_LOGIC_CIRCUIT)
     * 			    .input(OrePrefix.component, Component.Resistor, 2)
     * 				.input(OrePrefix.component, Component.Diode, 2)
     * 				.input(OrePrefix.wireFine, Materials.Copper, 2)
     * 			    .input(OrePrefix.bolt, Materials.Tin, 2)
     * 				.duration(200)
     * 				.EUt(16)
     * 				.buildAndRegister();
     * </pre>
     *
     * The Circuit Assembler has a special action that is performed for any recipe added to its recipe map, seen in its
     * <B>onRecipeBuild</B> call. Any recipe that is added to the Circuit Assembler that does not specify a fluid input
     * in the recipe will automatically have recipes generated using Soldering Alloy and Tin for the input fluids.
     *
     * The amount of these fluids is based on the Soldering Multiplier, which is a special addition to the
     * Circuit Assembler Recipe Builder. It is called through <B>.solderMultiplier(int multiplier)</B> on the Recipe Builder.
     * The Multiplier itself is limited to numbers between 1 and (64000 / 144) inclusive.
     *
     * This action can be negated by simply specifying a fluid input in the recipe.
     */

    public static final RegistryObject<GTRecipeType<CircuitAssemblerRecipeBuilder>> CIRCUIT_ASSEMBLER_RECIPES = new GTRecipeType<>("circuit_assembler",
            1, 6, 1, 1, 0, 1, 0, 0, new CircuitAssemblerRecipeBuilder(), false)
            .setSlotOverlay(false, false, GuiTextures.CIRCUIT_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_CIRCUIT_ASSEMBLER, ProgressWidget.MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.ASSEMBLER)
            .onRecipeBuild(recipeBuilder -> {
                recipeBuilder.invalidateOnBuildAction();
                if (recipeBuilder.getFluidInputs().isEmpty()) {
                    recipeBuilder.copy()
                            .fluidInputs(Materials.SolderingAlloy.getFluid(Math.max(1, (GTValues.L / 2) * ((CircuitAssemblerRecipeBuilder) recipeBuilder).getSolderMultiplier())))
                            .buildAndRegister();

                    // Don't call buildAndRegister as we are mutating the original recipe and already in the middle of a buildAndRegister call.
                    // Adding a second call will result in duplicate recipe generation attempts
                    recipeBuilder.fluidInputs(Materials.Tin.getFluid(Math.max(1, GTValues.L * ((CircuitAssemblerRecipeBuilder) recipeBuilder).getSolderMultiplier())));
                }
            });

    /**
     * Example:
     * <pre>
     *      GTRecipeType.COKE_OVEN_RECIPES.recipeBuilder()
     *         		.input(OrePrefix.log, Materials.Wood)
     *         		.output(OrePrefix.gem, Materials.Charcoal)
     *         	    .fluidOutputs(Materials.Creosote.getFluid(250))
     *         		.duration(900)
     *         		.buildAndRegister();
     * </pre>
     *
     * As a Primitive Machine, the Coke Oven does not need an <B>EUt</B> parameter specified for the Recipe Builder.
     */

    public static final RegistryObject<GTRecipeType<PrimitiveRecipeBuilder>> COKE_OVEN_RECIPES = new GTRecipeTypeCokeOven<>("coke_oven", 1, 1, 0, 1, 0, 0, 0, 1, new PrimitiveRecipeBuilder(), false)
            .setSound(GTSoundEvents.FIRE);

    /**
     * Example:
     * <pre>
     *      GTRecipeType.COMPRESSOR_RECIPES.recipeBuilder()
     *         		.input(OrePrefix.dust, Materials.Fireclay)
     *         		.outputs(MetaItems.COMPRESSED_FIRECLAY.getStackForm())
     *         		.duration(80)
     *         		.EUt(4)
     *         		.buildAndRegister();
     * </pre>
     *
     * Any Recipe added to the Compressor not specifying an <B>EUt</B> value will default to 2.
     * Any Recipe added to the Compressor not specifying a <B>duration</B> value will default to 200.
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> COMPRESSOR_RECIPES = new GTRecipeType<>("compressor", 1, 1, 1, 1, 0, 0, 0, 0, new SimpleRecipeBuilder().duration(200).EUt(2), false)
            .setSlotOverlay(false, false, GuiTextures.COMPRESSOR_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_COMPRESS, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.COMPRESSOR);

    /**
     * Example:
     * <pre>
     *      GTRecipeType.CRACKING_RECIPES.recipeBuilder()
     *              .notConsumable(new IntCircuitIngredient(1))
     *         		.fluidInputs(Materials.HeavyFuel.getFluid(1000))
     *         	    .fluidInputs(Hydrogen.getFluid(2000))
     *         		.fluidOutputs(LightlyHydroCrackedHeavyFuel.getFluid(1000))
     *         		.duration(80)
     *         		.EUt(GTValues.VA[GTValues.MV])
     *         		.buildAndRegister();
     * </pre>
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> CRACKING_RECIPES = new GTRecipeTypeCrackerUnit<>("cracker", 0, 1, 0, 0, 2, 2, 0, 2, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, true, GuiTextures.CRACKING_OVERLAY_1)
            .setSlotOverlay(true, true, GuiTextures.CRACKING_OVERLAY_2)
            .setSlotOverlay(false, false, GuiTextures.CIRCUIT_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_CRACKING, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.FIRE);

    /**
     * Example:
     * <pre>
     *      GTRecipeType.CUTTER_RECIPES.recipeBuilder()
     * 				.inputs(new ItemStack(Blocks.LOG, 1, GTValues.W))
     * 				.outputs(new ItemStack(Blocks.PLANKS), OreDictUnifier.get(OrePrefix.dust, Materials.Wood, 1L))
     * 				.duration(200)
     * 				.EUt(8)
     * 				.buildAndRegister();
     * </pre>
     *
     * The Cutting Machine has a special action that will be performed when its recipe is built, signified by the
     * <B>onRecipeBuild</B> call. If there is no fluid input specified in the passed recipe for the Cutting Machine,
     * recipes will automatically be generated using Water, Distilled Water, and Lubricant.
     *
     * The amount of these fluids used is some arcane formula, probably copied from GT5
     *
     * To negate this <B>onRecipeBuild</B> action, simply add a fluid input to the recipe passed to the Cutter Recipe Map.
     */


    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> CUTTER_RECIPES = new GTRecipeType<>("cutter", 1, 1, 1, 2, 0, 1, 0, 0, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, GuiTextures.SAWBLADE_OVERLAY)
            .setSlotOverlay(true, false, false, GuiTextures.CUTTER_OVERLAY)
            .setSlotOverlay(true, false, true, GuiTextures.DUST_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_SLICE, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.CUT)
            .onRecipeBuild(recipeBuilder -> {
                recipeBuilder.invalidateOnBuildAction();
                if (recipeBuilder.getFluidInputs().isEmpty()) {

                    recipeBuilder
                            .copy()
                            .fluidInputs(Materials.Water.getFluid(Math.max(4, Math.min(1000, recipeBuilder.duration * recipeBuilder.EUt / 320))))
                            .duration(recipeBuilder.duration * 2)
                            .buildAndRegister();

                    recipeBuilder
                            .copy()
                            .fluidInputs(Materials.DistilledWater.getFluid(Math.max(3, Math.min(750, recipeBuilder.duration * recipeBuilder.EUt / 426))))
                            .duration((int) (recipeBuilder.duration * 1.5))
                            .buildAndRegister();

                    // Don't call buildAndRegister as we are mutating the original recipe and already in the middle of a buildAndRegister call.
                    // Adding a second call will result in duplicate recipe generation attempts
                    recipeBuilder
                            .fluidInputs(Materials.Lubricant.getFluid(Math.max(1, Math.min(250, recipeBuilder.duration * recipeBuilder.EUt / 1280))))
                            .duration(Math.max(1, recipeBuilder.duration));

                }
            });

    /**
     * Examples:
     * <pre>
     * 	    GTRecipeType.DISTILLATION_RECIPES.recipeBuilder()
     * 	        	.fluidInputs(Materials.CoalTar.getFluid(1000))
     * 	        	.output(OrePrefix.dustSmall, Materials.Coke)
     * 	        	.fluidOutputs(Materials.Naphthalene.getFluid(400))
     * 	            .fluidOutputs(Materials.HydrogenSulfide.getFluid(300))
     * 	            .fluidOutputs(Materials.Creosote.getFluid(200))
     * 	            .fluidOutputs(Materials.Phenol.getFluid(100))
     * 	        	.duration(80)
     * 	        	.EUt(GTValues.VA[GTValues.MV])
     * 	        	.buildAndRegister();
     * </pre>
     *
     * The Distillation Tower is a special Multiblock, as it will create recipes in the Distillery breaking down its multi-
     * fluid output recipes. EG, a recipe in the Distillation tower outputs two different fluids from input fluid A. When
     *  this recipe is built, 2 separate recipes will be created in the Distillery. One for fluid A into the first output,
     *  and the second for fluid A into the second output.
     *
     *  This behavior can be disabled by adding a <B>.disableDistilleryRecipes()</B> onto the recipe builder.
     */

    public static final RegistryObject<GTRecipeType<UniversalDistillationRecipeBuilder>> DISTILLATION_RECIPES = new GTRecipeTypeDistillationTower("distillation_tower", 0, 0, 0, 1, 1, 1, 1, 12, new UniversalDistillationRecipeBuilder(), false)
            .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    /**
     * Example:
     * <pre>
     * 	 	GTRecipeType.DISTILLERY_RECIPES.recipeBuilder()
     * 	 			.circuitMeta(1)
     * 	 			.fluidInputs(Materials.Toluene.getFluid(30))
     * 	 			.fluidOutputs(Materials.LightFuel.getFluid(30))
     * 	 			.duration(160)
     * 	 			.EUt(24)
     * 	 			.buildAndRegister();
     * </pre>
     */

    public static final RegistryObject<GTRecipeType<IntCircuitRecipeBuilder>> DISTILLERY_RECIPES = new GTRecipeType<>("distillery", 1, 1, 0, 1, 1, 1, 1, 1, new IntCircuitRecipeBuilder(), false)
            .setSlotOverlay(false, true, GuiTextures.BEAKER_OVERLAY_1)
            .setSlotOverlay(true, true, GuiTextures.BEAKER_OVERLAY_4)
            .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
            .setSlotOverlay(false, false, GuiTextures.INT_CIRCUIT_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.BOILER);

    /**
     * Examples:
     * <pre>
     * 		GTRecipeType.ELECTROLYZER_RECIPES.recipeBuilder()
     * 				.fluidInputs(Materials.SaltWater.getFluid(1000))
     * 				.output(OrePrefix.dust, Materials.SodiumHydroxide, 3)
     * 				.fluidOutputs(Materials.Chlorine.getFluid(1000))
     * 				.fluidOutputs(Materials.Hydrogen.getFluid(1000))
     * 				.duration(720)
     * 				.EUt(GTValues.VA[GTValues.LV])
     * 				.buildAndRegister();
     * </pre>
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> ELECTROLYZER_RECIPES = new GTRecipeType<>("electrolyzer", 0, 2, 0, 6, 0, 1, 0, 6, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, false, GuiTextures.LIGHTNING_OVERLAY_1)
            .setSlotOverlay(false, false, true, GuiTextures.CANISTER_OVERLAY)
            .setSlotOverlay(false, true, true, GuiTextures.LIGHTNING_OVERLAY_2)
            .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.ELECTROLYZER);

    /**
     * Example:
     * <pre>
     * 	    GTRecipeType.ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder()
     * 				.input(OrePrefix.dustPure, Materials.Aluminium)
     * 				.outputs(OrePrefix.dust, Materials.Aluminium)
     * 				.chancedOutput(OreDictUnifier.get(OrePrefix.dustSmall, Materials.Aluminium), 4000, 850)
     * 				.chancedOutput(OreDictUnifier.get(OrePrefix.dustSmall, Materials.Aluminium), 2000, 600)
     * 				.duration(200)
     * 				.EUt(24)
     * 				.buildAndRegister();
     * </pre>
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> ELECTROMAGNETIC_SEPARATOR_RECIPES = new GTRecipeType<>("electromagnetic_separator", 1, 1, 1, 3, 0, 0, 0, 0, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, GuiTextures.CRUSHED_ORE_OVERLAY)
            .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_MAGNET, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.ARC);

    /**
     * Example:
     * <pre>
     *      GTRecipeType.EXTRACTOR_RECIPES.recipeBuilder()
     * 				.inputs(new ItemStack(MetaBlocks.RUBBER_LEAVES, 16))
     * 				.output(OrePrefix.dust, Materials.RawRubber)
     * 				.duration(300)
     * 				.buildAndRegister();
     * </pre>
     *
     * Any Recipe added to the Extractor not specifying an <B>EUt</B> value will default to 2.
     * Any Recipe added to the Extractor not specifying an <B>duration</B> value will default to 400.
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> EXTRACTOR_RECIPES = new GTRecipeType<>("extractor", 0, 1, 0, 1, 0, 0, 0, 1, new SimpleRecipeBuilder().duration(400).EUt(2), false)
            .setSlotOverlay(false, false, GuiTextures.EXTRACTOR_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.COMPRESSOR);

    /**
     * Example:
     * <pre>
     *      GTRecipeType.EXTRUDER_RECIPES.recipeBuilder()
     * 				.input(OrePrefix.ingot, Materials.BorosilicateGlass)
     * 				.notConsumable(MetaItems.SHAPE_EXTRUDER_WIRE)
     * 				.output(OrePrefix.wireFine, Materials.BorosilicateGlass, 8)
     * 				.duration(160)
     * 				.EUt(96)
     * 				.buildAndRegister();
     * </pre>
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> EXTRUDER_RECIPES = new GTRecipeType<>("extruder", 2, 2, 1, 1, 0, 0, 0, 0, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, true, GuiTextures.MOLD_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRUDER, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.ARC);

    /**
     * Example:
     * <pre>
     *      GTRecipeType.FERMENTING_RECIPES.recipeBuilder()
     * 				.fluidInputs(Materials.Biomass.getFluid(100))
     * 				.fluidOutputs(Materials.FermentedBiomass.getFluid(100))
     * 				.duration(150)
     * 				.buildAndRegister();
     * </pre>
     *
     * Any Recipe added to the Fermenter not specifying an <B>EUt</B> value will default to 2.
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> FERMENTING_RECIPES = new GTRecipeType<>("fermenter", 0, 1, 0, 1, 1, 1, 1, 1, new SimpleRecipeBuilder().EUt(2), false)
            .setSlotOverlay(false, false, true, GuiTextures.DUST_OVERLAY)
            .setSlotOverlay(true, false, true, GuiTextures.DUST_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    /**
     * Example:
     * <pre>
     * 		GTRecipeType.FLUID_HEATER_RECIPES.recipeBuilder()
     * 				.circuitMeta(1)
     * 				.fluidInputs(Materials.Water.getFluid(6))
     * 				.fluidOutputs(Materials.Steam.getFluid(960))
     * 				.duration(30)
     * 				.EUt(GTValues.VA[GTValues.LV])
     * 				.buildAndRegister();
     * </pre>
     */

    public static final RegistryObject<GTRecipeType<IntCircuitRecipeBuilder>> FLUID_HEATER_RECIPES = new GTRecipeType<>("fluid_heater", 1, 1, 0, 0, 1, 1, 1, 1, new IntCircuitRecipeBuilder(), false)
            .setSlotOverlay(false, true, GuiTextures.HEATING_OVERLAY_1)
            .setSlotOverlay(true, true, GuiTextures.HEATING_OVERLAY_2)
            .setSlotOverlay(false, false, GuiTextures.INT_CIRCUIT_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.BOILER);

    /**
     * Example:
     * <pre>
     *  	GTRecipeType.FLUID_SOLIDFICATION_RECIPES.recipeBuilder()
     * 				.notConsumable(MetaItems.SHAPE_MOLD_CYLINDER)
     * 				.fluidInputs(Materials.Polybenzimidazole.getFluid(GTValues.L / 8))
     * 				.output(MetaItems.PETRI_DISH, 2)
     * 				.duration(40)
     * 				.EUt(GTValues.VA[GTValues.HV])
     * 				.buildAndRegister();
     * </pre>
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> FLUID_SOLIDFICATION_RECIPES = new GTRecipeType<>("fluid_solidifier", 1, 1, 1, 1, 1, 1, 0, 0, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, GuiTextures.SOLIDIFIER_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.COOLING);

    /**
     * Example:
     * <pre>
     *      GTRecipeType.FORGE_HAMMER_RECIPES.recipeBuilder()
     * 				.inputs(new ItemStack(Blocks.STONE))
     * 				.outputs(new ItemStack(Blocks.COBBLESTONE))
     * 				.duration(16)
     * 				.EUt(10)
     * 				.buildAndRegister();
     * </pre>
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> FORGE_HAMMER_RECIPES = new GTRecipeType<>("forge_hammer", 1, 1, 1, 1, 0, 0, 0, 0, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, GuiTextures.HAMMER_OVERLAY)
            .setSpecialTexture(78, 42, 20, 6, GuiTextures.PROGRESS_BAR_HAMMER_BASE)
            .setProgressBar(GuiTextures.PROGRESS_BAR_HAMMER, MoveType.VERTICAL_DOWNWARDS)
            .setSound(GTSoundEvents.FORGE_HAMMER);

    /**
     * Example:
     * <pre>
     *      GTRecipeType.FORMING_PRESS_RECIPES.recipeBuilder()
     * 				.inputs(new ItemStack(Blocks.STONE))
     * 				.outputs(new ItemStack(Blocks.COBBLESTONE))
     * 				.duration(16)
     * 				.EUt(10)
     * 				.buildAndRegister();
     * </pre>
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> FORMING_PRESS_RECIPES = new GTRecipeTypeFormingPress("forming_press", 2, 6, 1, 1, 0, 0, 0, 0, new SimpleRecipeBuilder(), false)
            .setProgressBar(GuiTextures.PROGRESS_BAR_COMPRESS, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.COMPRESSOR);

    /**
     *
     * Example:
     * <pre>
     * 		GTRecipeType.FURNACE_RECIPES.recipeBuilder()
     * 				.inputs(new ItemStack(Blocks.SAND))
     * 				.outputs(new ItemStack(Blocks.COBBLESTONE))
     * 				.duration(128)
     * 				.EUt(4)
     * 				.buildAndRegister();
     * </pre>
     *
     * When looking up recipes from the GTCEu Furnaces, they will first check the Vanilla Furnace Recipe list, therefore
     * our Furnaces can perform any recipe that is in the Vanilla Furnace Recipe List. This also means there is no need
     * to add Furnace Recipes that duplicate Vanilla recipes.
     *
     * However, when adding a recipe to our Furnace Recipe Map, these new recipes are not added to the Vanilla Furnace
     * Recipe List, so any recipes added will be exclusive to the GTCEu Furnaces.
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> FURNACE_RECIPES = new GTRecipeTypeFurnace("electric_furnace", 1, 1, 1, 1, 0, 0, 0, 0, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, GuiTextures.FURNACE_OVERLAY_1)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.FURNACE);

    /**
     * Example:
     * <pre>
     * 		GTRecipeType.FUSION_RECIPES.recipeBuilder()
     * 				.fluidInputs(Materials.Lithium.getFluid(16), Materials.Tungsten.getFluid(16))
     * 				.fluidOutputs(Materials.Iridium.getFluid(16))
     * 				.duration(32)
     * 				.EUt(GTValues.VA[GTValues.LuV])
     * 				.EUToStart(300000000)
     * 				.buildAndRegister();
     * </pre>
     *
     * The Fusion Reactor requires an <B>EUToStart</B> call, which is used to gate recipes behind requiring different tier
     * Fusion Reactors. This value must be greater than 0.
     *
     * The Breakpoints for this value currently are:
     * MK1: 160MEU and lower
     * MK2: 160MEU - 320MEU
     * MK3: 320MEU - 640MEU
     */

    public static final RegistryObject<GTRecipeType<FusionRecipeBuilder>> FUSION_RECIPES = new GTRecipeType<>("fusion_reactor", 0, 0, 0, 0, 2, 2, 0, 1, new FusionRecipeBuilder(), false)
            .setProgressBar(GuiTextures.PROGRESS_BAR_FUSION, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.ARC);


    public static final RegistryObject<GTRecipeType<GasCollectorRecipeBuilder>> GAS_COLLECTOR_RECIPES = new GTRecipeType<>("gas_collector", 1, 1, 0, 0, 0, 0, 1, 1, new GasCollectorRecipeBuilder(), false)
            .setSlotOverlay(false, false, GuiTextures.INT_CIRCUIT_OVERLAY)
            .setSlotOverlay(true, true, GuiTextures.CENTRIFUGE_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_GAS_COLLECTOR, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.COOLING);

    /**
     * Example:
     * <pre>
     *      GTRecipeType.IMPLOSION_RECIPES.recipeBuilder()
     *         		.input(OreDictUnifier.get(OrePrefix.gem, Materials.Coal, 64))
     *         		.explosivesAmount(8)
     *         		.outputs(OreDictUnifier.get(OrePrefix.gem, Materials.Diamond, 1), OreDictUnifier.get(OrePrefix.dustTiny, Materials.DarkAsh, 4))
     *         	    .duration(400)
     *         	    .EUt(GTValues.VA[GTValues.HV])
     *         		.buildAndRegister();
     * </pre>
     *
     * <pre>
     *      GTRecipeType.IMPLOSION_RECIPES.recipeBuilder()
     *         		.input(OreDictUnifier.get(OrePrefix.gem, Materials.Coal, 64))
     *         		.explosivesType(MetaItems.DYNAMITE.getStackForm(4))
     *         		.outputs(OreDictUnifier.get(OrePrefix.gem, Materials.Diamond, 1), OreDictUnifier.get(OrePrefix.dustTiny, Materials.DarkAsh, 4))
     *         	    .duration(400)
     *         	    .EUt(GTValues.VA[GTValues.HV])
     *         		.buildAndRegister();
     * </pre>
     *
     * The Implosion Compressor can specify explosives used for its recipes in two different ways. The first is using
     * <B>explosivesAmount(int amount)</B>, which will generate a recipe using TNT as the explosive, with the count of TNT
     * being the passed amount. Note that this must be between 1 and 64 inclusive.
     *
     * The second method is to use <B>explosivesType(ItemStack item)</B>. In this case, the passed ItemStack will be used
     * as the explosive, with the number of explosives being the count of the passed ItemStack.
     * Note that the count must be between 1 and 64 inclusive
     */

    public static final RegistryObject<GTRecipeType<ImplosionRecipeBuilder>> IMPLOSION_RECIPES = new GTRecipeType<>("implosion_compressor", 2, 3, 0, 2, 0, 0, 0, 0, new ImplosionRecipeBuilder().duration(20).EUt(VA[LV]), false)
            .setSlotOverlay(false, false, true, GuiTextures.IMPLOSION_OVERLAY_1)
            .setSlotOverlay(false, false, false, GuiTextures.IMPLOSION_OVERLAY_2)
            .setSlotOverlay(true, false, true, GuiTextures.DUST_OVERLAY)
            .setSound(SoundEvents.ENTITY_GENERIC_EXPLODE);

    /**
     * Example:
     * <pre>
     *      GTRecipeType.LARGE_CHEMICAL_RECIPES.recipeBuilder()
     * 			    .fluidInputs(Materials.NitrogenDioxide.getFluid(4000))
     * 			    .fluidInputs(Materials.Oxygen.getFluid(1000))
     * 				.fluidInputs(Materials.Water.getFluid(2000))
     * 				.fluidOutputs(Materials.NitricAcid.getFluid(4000))
     * 				.duration(950)
     * 				.EUt(GTValues.VA[GTValues.HV])
     * 				.buildAndRegister();
     * </pre>
     *
     * Note that any recipes added to the Large Chemical Reactor recipe map will be exclusive to the LCR, unlike
     * recipes added to the Chemical Reactor, which will be mirrored to the LCR.
     *
     * Any Recipe added to the Large Chemical Reactor not specifying an <B>EUt</B> value will default to 30.
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> LARGE_CHEMICAL_RECIPES = new GTRecipeType<>("large_chemical_reactor", 0, 3, 0, 3, 0, 5, 0, 4, new SimpleRecipeBuilder().EUt(VA[LV]), false)
            .setSlotOverlay(false, false, false, GuiTextures.MOLECULAR_OVERLAY_1)
            .setSlotOverlay(false, false, true, GuiTextures.MOLECULAR_OVERLAY_2)
            .setSlotOverlay(false, true, false, GuiTextures.MOLECULAR_OVERLAY_3)
            .setSlotOverlay(false, true, true, GuiTextures.MOLECULAR_OVERLAY_4)
            .setSlotOverlay(true, false, GuiTextures.VIAL_OVERLAY_1)
            .setSlotOverlay(true, true, GuiTextures.VIAL_OVERLAY_2)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.CHEMICAL_REACTOR)
            .setSmallGTRecipeType(CHEMICAL_RECIPES);

    /**
     * Example:
     * <pre>
     * 		GTRecipeType.LASER_ENGRAVER_RECIPES.recipeBuilder()
     * 				.input(MetaItems.SILICON_WAFER)
     * 				.notConsumable(OrePrefix.craftingLens, MarkerMaterials.Color.Red)
     * 				.output(MetaItems.INTEGRATED_LOGIC_CIRCUIT_WAFER)
     * 				.duration(900)
     * 				.EUt(GTValues.VA[GTValues.MV])
     * 				.buildAndRegister();
     * </pre>
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> LASER_ENGRAVER_RECIPES = new GTRecipeType<>("laser_engraver", 2, 2, 1, 1, 0, 0, 0, 0, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, true, GuiTextures.LENS_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.ELECTROLYZER);

    /**
     * Example:
     * <pre>
     * 	     GTRecipeType.LATHE_RECIPES.recipeBuilder()
     * 				.inputs(new ItemStack(Blocks.WOODEN_SLAB, 1, GTValues.W))
     * 				.outputs(new ItemStack(Items.BOWL))
     * 				.output(OrePrefix.dustSmall, Materials.Wood)
     * 				.duration(50).EUt(GTValues.VA[GTValues.ULV])
     * 				.buildAndRegister();
     * </pre>
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> LATHE_RECIPES = new GTRecipeType<>("lathe", 1, 1, 1, 2, 0, 0, 0, 0, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, GuiTextures.PIPE_OVERLAY_1)
            .setSlotOverlay(true, false, false, GuiTextures.PIPE_OVERLAY_2)
            .setSlotOverlay(true, false, true, GuiTextures.DUST_OVERLAY)
            .setSpecialTexture(98, 24, 5, 18, GuiTextures.PROGRESS_BAR_LATHE_BASE)
            .setProgressBar(GuiTextures.PROGRESS_BAR_LATHE, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.CUT);

    /**
     * Example:
     * <pre>
     * 		GTRecipeType.MACERATOR_RECIPES.recipeBuilder()
     * 				.inputs(new ItemStack(Items.CHICKEN))
     * 				.output(OrePrefix.dust, Materials.Meat)
     * 				.output(OrePrefix.dustTiny, Materials.Bone)
     * 				.duration(102).buildAndRegister();
     * </pre>
     *
     * Any Recipe added to the Macerator not specifying an <B>EUt</B> value will default to 2.
     * Any Recipe added to the Macerator not specifying a <B>duration</B> value will default to 150.
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> MACERATOR_RECIPES = new GTRecipeType<>("macerator", 1, 1, 1, 4, 0, 0, 0, 0, new SimpleRecipeBuilder().duration(150).EUt(2), false)
            .setSlotOverlay(false, false, GuiTextures.CRUSHED_ORE_OVERLAY)
            .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_MACERATE, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.MACERATOR);


    /**
     * Currently unused
     */

    @SuppressWarnings("unused")
    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> MASS_FABRICATOR_RECIPES = new GTRecipeType<>("mass_fabricator", 0, 1, 0, 0, 0, 1, 1, 2, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, GuiTextures.ATOMIC_OVERLAY_1)
            .setSlotOverlay(false, true, GuiTextures.ATOMIC_OVERLAY_2)
            .setSlotOverlay(true, true, GuiTextures.POSITIVE_MATTER_OVERLAY)
            .setSlotOverlay(true, true, true, GuiTextures.NEUTRAL_MATTER_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_MASS_FAB, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.REPLICATOR);

    /**
     * Example:
     * <pre>
     * 		GTRecipeType.MIXER_RECIPES.recipeBuilder()
     * 				.input(OrePrefix.dust, Materials.Redstone, 5)
     * 				.input(OrePrefix.dust, Materials.Ruby, 4)
     * 				.notConsumable(new IntCircuitIngredient(1))
     * 				.output(MetaItems.ENERGIUM_DUST, 9)
     * 				.duration(600).EUt(GTValues.VA[GTValues.MV])
     * 				.buildAndRegister();
     * </pre>
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> MIXER_RECIPES = new GTRecipeType<>("mixer", 0, 6, 0, 1, 0, 2, 0, 1, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, GuiTextures.DUST_OVERLAY)
            .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_MIXER, MoveType.CIRCULAR)
            .setSound(GTSoundEvents.MIXER);

    /**
     * Example:
     * <pre>
     * 		GTRecipeType.ORE_WASHER_RECIPES.recipeBuilder()
     * 				.input(OrePrefix.crushed, Materials.Aluminum)
     * 				.notConsumable(new IntCircuitIngredient(2))
     * 				.fluidInputs(Materials.Water.getFluid(100))
     * 				.output(OrePrefix.crushedPurified, Materials.Aluminum)
     * 				.duration(8).EUt(4).buildAndRegister();
     * </pre>
     *
     * Any Recipe added to the Ore Washer not specifying an <B>EUt</B> value will default to 16.
     * Any Recipe added to the Ore Washer not specifying a <B>duration</B> value will default to 400.
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> ORE_WASHER_RECIPES = new GTRecipeType<>("ore_washer", 1, 2, 1, 3, 0, 1, 0, 0, new SimpleRecipeBuilder().duration(400).EUt(16), false)
            .setSlotOverlay(false, false, GuiTextures.CRUSHED_ORE_OVERLAY)
            .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_BATH, MoveType.CIRCULAR)
            .setSound(GTSoundEvents.BATH);

    /**
     * Example:
     * <pre>
     * 		GTRecipeType.PACKER_RECIPES.recipeBuilder()
     * 				.inputs(new ItemStack(Items.WHEAT, 9))
     * 				.notConsumable(new IntCircuitIngredient(9))
     * 				.outputs(new ItemStack(Blocks.HAY_BLOCK))
     * 				.duration(200).EUt(2)
     * 				.buildAndRegister();
     * </pre>
     *
     * Any Recipe added to the Packer not specifying an <B>EUt</B> value will default to 12.
     * Any Recipe added to the Packer not specifying a <B>duration</B> value will default to 10.
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> PACKER_RECIPES = new GTRecipeType<>("packer", 1, 2, 1, 2, 0, 0, 0, 0, new SimpleRecipeBuilder().EUt(12).duration(10), false)
            .setSlotOverlay(false, false, true, GuiTextures.BOX_OVERLAY)
            .setSlotOverlay(true, false, GuiTextures.BOXED_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_UNPACKER, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.ASSEMBLER);

    /**
     * Example:
     * <pre>
     * 		GTRecipeType.POLARIZER_RECIPES.recipeBuilder()
     * 				.inputs(OreDictUnifier.get(OrePrefix.plate, Materials.Iron))
     * 				.outputs(OreDictUnifier.get(OrePrefix.plate, Materials.IronMagnetic))
     * 				.duration(100)
     * 				.EUt(16)
     * 				.buildAndRegister();
     * </pre>
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> POLARIZER_RECIPES = new GTRecipeType<>("polarizer", 1, 1, 1, 1, 0, 0, 0, 0, new SimpleRecipeBuilder(), false)
            .setProgressBar(GuiTextures.PROGRESS_BAR_MAGNET, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.ARC);

    /**
     * Example:
     * <pre>
     *      GTRecipeType.PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder()
     *     			.input(OrePrefix.ingot, Materials.Iron)
     *     			.input(OrePrefix.gem, Materials.Coal, 2)
     *     			.output(OrePrefix.ingot, Materials.Steel)
     *     			.output(OrePrefix.dustTiny, Materials.DarkAsh, 2)))
     *     			.duration(1800)
     *     			.buildAndRegister();
     * </pre>
     *
     * As a Primitive Machine, the Primitive Blast Furnace does not need an <B>EUt</B> parameter specified for the Recipe Builder.
     */

    public static final RegistryObject<GTRecipeType<PrimitiveRecipeBuilder>> PRIMITIVE_BLAST_FURNACE_RECIPES = new GTRecipeType<>("primitive_blast_furnace", 2, 3, 0, 3, 0, 0, 0, 0, new PrimitiveRecipeBuilder(), false)
            .setSound(GTSoundEvents.FIRE);

    /**
     * Example:
     * <pre>
     *      GTRecipeType.PYROLYSE_RECIPES.recipeBuilder()
     *     			.input(OrePrefix.log, Materials.Wood, 16)
     *     			.circuitMeta(2)
     *     			.fluidInputs(Materials.Nitrogen.getFluid(1000))
     *     			.outputs(new ItemStack(Items.COAL, 20, 1))
     *     			.fluidOutputs(Materials.Creosote.getFluid(4000))
     *     			.duration(320)
     *     			.EUt(96)
     *     			.buildAndRegister();
     * </pre>
     */

    public static final RegistryObject<GTRecipeType<IntCircuitRecipeBuilder>> PYROLYSE_RECIPES = new GTRecipeType<>("pyrolyse_oven", 2, 2, 0, 1, 0, 1, 0, 1, new IntCircuitRecipeBuilder(), false)
            .setSound(GTSoundEvents.FIRE);

    /**
     * Currently unused
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> REPLICATOR_RECIPES = new GTRecipeType<>("replicator", 1, 1, 0, 1, 1, 2, 0, 1, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, GuiTextures.DATA_ORB_OVERLAY)
            .setSlotOverlay(true, false, GuiTextures.ATOMIC_OVERLAY_1)
            .setSlotOverlay(true, true, GuiTextures.ATOMIC_OVERLAY_2)
            .setSlotOverlay(false, true, GuiTextures.NEUTRAL_MATTER_OVERLAY)
            .setSlotOverlay(false, true, true, GuiTextures.POSITIVE_MATTER_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_REPLICATOR, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.REPLICATOR);


    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> ROCK_BREAKER_RECIPES = new GTRecipeType<>("rock_breaker", 1, 1, 1, 4, 0, 0, 0, 0, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, GuiTextures.DUST_OVERLAY)
            .setSlotOverlay(true, false, GuiTextures.CRUSHED_ORE_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_MACERATE, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.FIRE);

    /**
     * Currently unused
     */

    @SuppressWarnings("unused")
    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> SCANNER_RECIPES = new GTRecipeType<>("scanner", 0, 2, 1, 1, 0, 1, 0, 0, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, GuiTextures.DATA_ORB_OVERLAY)
            .setSlotOverlay(false, false, true, GuiTextures.SCANNER_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.ELECTROLYZER);

    /**
     * Example:
     * <pre>
     *     GTRecipeType.SIFTER_RECIPES.recipeBuilder()
     *     			.inputs(new ItemStack(Blocks.SAND))
     *     			.chancedOutput(OreDictUnifier.get(OrePrefix.gemExquisite, Materials.Ruby, 1L), 300)
     *     			.chancedOutput(OreDictUnifier.get(OrePrefix.gemFlawless, Materials.Ruby, 1L), 1200)
     *     			.chancedOutput(OreDictUnifier.get(OrePrefix.gemFlawed, Materials.Ruby, 1L), 4500)
     *     			.chancedOutput(OreDictUnifier.get(OrePrefix.gemChipped, Materials.Ruby, 1L), 1400)
     *     			.chancedOutput(OreDictUnifier.get(OrePrefix.dust, Materials.Ruby, 1L), 2800)
     *     			.duration(800)
     *     			.EUt(16)
     *     			.buildAndRegister();
     * </pre>
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> SIFTER_RECIPES = new GTRecipeType<>("sifter", 1, 1, 0, 6, 0, 0, 0, 0, new SimpleRecipeBuilder(), false)
            .setProgressBar(GuiTextures.PROGRESS_BAR_SIFT, MoveType.VERTICAL_DOWNWARDS)
            .setSound(SoundEvents.BLOCK_SAND_PLACE);

    /**
     * Example:
     * <pre>
     *     GTRecipeType.THERMAL_CENTRIFUGE_RECIPES.recipeBuilder()
     *     			.input(OrePrefix.crushed, Materials.Aluminum)
     *     			.outputs(OreDictUnifier.get(OrePrefix.crushedPurified, Materials.Aluminum), OreDictUnifier.get(OrePrefix.dustTiny, Materials.Bauxite, 3), OreDictUnifier.get(OrePrefix.dust, Materials.Stone))
     *     			.duration(800)
     *     			.EUt(16)
     *     			.buildAndRegister();
     * </pre>
     *
     * Any Recipe added to the Thermal Centrifuge not specifying an <B>EUt</B> value will default to 30.
     * Any Recipe added to the Thermal Centrifuge not specifying a <B>duration</B> value will default to 400.
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> THERMAL_CENTRIFUGE_RECIPES = new GTRecipeType<>("thermal_centrifuge", 1, 1, 1, 3, 0, 0, 0, 0, new SimpleRecipeBuilder().duration(400).EUt(30), false)
            .setSlotOverlay(false, false, GuiTextures.CRUSHED_ORE_OVERLAY)
            .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.CENTRIFUGE);

    /**
     * Example:
     * <pre>
     * 		GTRecipeType.VACUUM_RECIPES.recipeBuilder()
     * 				.fluidInputs(Air.getFluid(4000))
     * 				.fluidOutputs(LiquidAir.getFluid(4000))
     * 				.duration(80).EUt(GTValues.VA[GTValues.HV])
     * 				.buildAndRegister();
     * </pre>
     *
     * Any Recipe added to the Thermal Centrifuge not specifying an <B>EUt</B> value will default to 120.
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> VACUUM_RECIPES = new GTRecipeType<>("vacuum_freezer", 0, 1, 0, 1, 0, 2, 0, 1, new SimpleRecipeBuilder().EUt(VA[MV]), false)
            .setSound(GTSoundEvents.COOLING);

    /**
     * Example:
     * <pre>
     * 		GTRecipeType.WIREMILL_RECIPES.recipeBuilder()
     * 				.input(OrePrefix.ingot, Materials.Iron)
     * 				.output(OrePrefix.wireGtSingle, Materials.Iron, 2)
     * 				.duration(200)
     * 				.EUt(GTValues.VA[GTValues.ULV])
     * 				.buildAndRegister();
     * </pre>
     */

    public static final RegistryObject<GTRecipeType<SimpleRecipeBuilder>> WIREMILL_RECIPES = new GTRecipeType<>("wiremill", 1, 1, 1, 1, 0, 0, 0, 0, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, GuiTextures.WIREMILL_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_WIREMILL, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.MOTOR);


    //////////////////////////////////////
    //         Fuel Recipe Maps         //
    //////////////////////////////////////


    public static final RegistryObject<GTRecipeType<FuelRecipeBuilder>> COMBUSTION_GENERATOR_FUELS = new GTRecipeType<>("combustion_generator", 0, 0, 0, 0, 1, 1, 0, 0, new FuelRecipeBuilder(), false)
            .setSlotOverlay(false, true, true, GuiTextures.FURNACE_OVERLAY_2)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.COMBUSTION);


    public static final RegistryObject<GTRecipeType<FuelRecipeBuilder>> GAS_TURBINE_FUELS = new GTRecipeType<>("gas_turbine", 0, 0, 0, 0, 1, 1, 0, 0, new FuelRecipeBuilder(), false)
            .setSlotOverlay(false, true, true, GuiTextures.DARK_CANISTER_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_GAS_COLLECTOR, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.TURBINE);


    public static final RegistryObject<GTRecipeType<FuelRecipeBuilder>> STEAM_TURBINE_FUELS = new GTRecipeType<>("steam_turbine", 0, 0, 0, 0, 1, 1, 0, 1, new FuelRecipeBuilder(), false)
            .setSlotOverlay(false, true, true, GuiTextures.CENTRIFUGE_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_GAS_COLLECTOR, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.TURBINE);


    public static final RegistryObject<GTRecipeType<FuelRecipeBuilder>> SEMI_FLUID_GENERATOR_FUELS = new GTRecipeType<>("semi_fluid_generator", 0, 0, 0, 0, 1, 1, 0, 0, new FuelRecipeBuilder(), false)
            .setSlotOverlay(false, true, true, GuiTextures.FURNACE_OVERLAY_2)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.COMBUSTION);


    public static final RegistryObject<GTRecipeType<FuelRecipeBuilder>> PLASMA_GENERATOR_FUELS = new GTRecipeType<>("plasma_generator", 0, 0, 0, 0, 1, 1, 0, 1, new FuelRecipeBuilder(), false)
            .setSlotOverlay(false, true, true, GuiTextures.CENTRIFUGE_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_GAS_COLLECTOR, MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.TURBINE);
}
