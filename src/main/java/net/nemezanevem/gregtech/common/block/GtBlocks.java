package net.nemezanevem.gregtech.common.block;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.block.machine.BlockMachine;
import net.nemezanevem.gregtech.api.registry.material.MaterialRegistry;
import net.nemezanevem.gregtech.api.tileentity.MetaTileEntityHolder;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.TagUnifier;
import net.nemezanevem.gregtech.api.unification.material.properties.GtMaterialProperties;
import net.nemezanevem.gregtech.api.unification.material.properties.PropertyKey;
import net.nemezanevem.gregtech.api.unification.tag.TagPrefix;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.client.model.IModelSupplier;
import net.nemezanevem.gregtech.common.block.block.BlockWireCoil;
import net.nemezanevem.gregtech.common.block.wood.*;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static net.nemezanevem.gregtech.api.unification.material.properties.info.GtMaterialFlags.FORCE_GENERATE_BLOCK;

public class GtBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, GregTech.MODID);

    private GtBlocks() {
    }

    public static final RegistryObject<Block> MACHINE = BLOCKS.register("machine", () -> new BlockMachine());
    public static final BlockCable[] CABLES = new BlockCable[10];
    public static final BlockFluidPipe[] FLUID_PIPES = new BlockFluidPipe[7];
    public static final BlockItemPipe[] ITEM_PIPES = new BlockItemPipe[8];

    public static RegistryObject<Block> BOILER_CASING = BLOCKS.register("boiler_casing", () -> new BlockBoilerCasing());
    public static final RegistryObject<Block> BOILER_FIREBOX_CASING = BLOCKS.register("boiler_firebox_casing", () -> new BlockFireboxCasing());
    public static final RegistryObject<Block> METAL_CASING = BLOCKS.register("metal_casing", () -> new BlockMetalCasing());
    public static final RegistryObject<Block> TURBINE_CASING = BLOCKS.register("turbine_casing", () -> new BlockTurbineCasing());
    public static final RegistryObject<Block> MACHINE_CASING = BLOCKS.register("machine_casing", () -> new BlockMachineCasing());
    public static final RegistryObject<Block> STEAM_CASING = BLOCKS.register("steam_casing", () -> new BlockSteamCasing());
    public static final RegistryObject<Block> MULTIBLOCK_CASING = BLOCKS.register("multiblock_casing", () -> new BlockMultiblockCasing());
    public static final RegistryObject<Block> TRANSPARENT_CASING = BLOCKS.register("transparent_casing", () -> new BlockGlassCasing());
    public static final RegistryObject<Block> WIRE_COIL = BLOCKS.register("wire_coil", () -> new BlockWireCoil());
    public static final RegistryObject<Block> FUSION_CASING = BLOCKS.register("fusion_casing", () -> new BlockFusionCasing());
    public static final RegistryObject<Block> WARNING_SIGN = BLOCKS.register("warning_sign", () -> new BlockWarningSign());
    public static final RegistryObject<Block> WARNING_SIGN_1 = BLOCKS.register("warning_sign_1", () -> new BlockWarningSign1());
    public static final RegistryObject<Block> HERMETIC_CASING = BLOCKS.register("hermetic_casing", () -> new BlockHermeticCasing());
    public static final RegistryObject<Block> CLEANROOM_CASING = BLOCKS.register("cleanroom_casing", () -> new BlockCleanroomCasing());

    public static final RegistryObject<Block> ASPHALT = BLOCKS.register("asphalt", () -> new BlockAsphalt());

    public static final RegistryObject<Block> STONE_SMOOTH = BLOCKS.register("stone_smooth", () -> new BlockStoneSmooth());
    public static final RegistryObject<Block> STONE_COBBLE = BLOCKS.register("stone_cobble", () -> new BlockStoneCobble());
    public static final RegistryObject<Block> STONE_COBBLE_MOSSY = BLOCKS.register("stone_cobble_mossy", () -> new BlockStoneCobbleMossy());
    public static final RegistryObject<Block> STONE_POLISHED = BLOCKS.register("stone_polished", () -> new BlockStonePolished());
    public static final RegistryObject<Block> STONE_BRICKS = BLOCKS.register("stone_bricks", () -> new BlockStoneBricks());
    public static final RegistryObject<Block> STONE_BRICKS_CRACKED = BLOCKS.register("stone_bricks_cracked", () -> new BlockStoneBricksCracked());
    public static final RegistryObject<Block> STONE_BRICKS_MOSSY = BLOCKS.register("stone_bricks_mossy", () -> new BlockStoneBricksMossy());
    public static final RegistryObject<Block> STONE_CHISELED = BLOCKS.register("stone_chiseled", () -> new BlockStoneChiseled());
    public static final RegistryObject<Block> STONE_TILED = BLOCKS.register("stone_tiled", () -> new BlockStoneTiled());
    public static final RegistryObject<Block> STONE_TILED_SMALL = BLOCKS.register("stone_tiled_small", () -> new BlockStoneTiledSmall());
    public static final RegistryObject<Block> STONE_BRICKS_SMALL = BLOCKS.register("stone_bricks_small", () -> new BlockStoneBricksSmall());
    public static final RegistryObject<Block> STONE_WINDMILL_A = BLOCKS.register("stone_windmill_a", () -> new BlockStoneWindmillA());
    public static final RegistryObject<Block> STONE_WINDMILL_B = BLOCKS.register("stone_windmill_b", () -> new BlockStoneWindmillB());
    public static final RegistryObject<Block> STONE_BRICKS_SQUARE = BLOCKS.register("stone_bricks_square", () -> new BlockStoneBricksSquare());

    public static final RegistryObject<Block> FOAM = BLOCKS.register("foam", () -> new BlockFoam());
    public static final RegistryObject<Block> REINFORCED_FOAM = BLOCKS.register("reinforced_foam", () -> new BlockFoam());
    public static final RegistryObject<Block> PETRIFIED_FOAM = BLOCKS.register("petrified_foam", () -> new BlockPetrifiedFoam());
    public static final RegistryObject<Block> REINFORCED_PETRIFIED_FOAM = BLOCKS.register("reinforced_petrified_foam", () -> new BlockPetrifiedFoam());

    public static final RegistryObject<Block> RUBBER_LOG = BLOCKS.register("rubber_log", () -> new BlockRubberLog());
    public static final RegistryObject<Block> RUBBER_LEAVES = BLOCKS.register("rubber_leaves", () -> new BlockRubberLeaves());
    public static final RegistryObject<Block> RUBBER_SAPLING = BLOCKS.register("rubber_sapling", () -> new BlockRubberSapling());
    public static final RegistryObject<Block> PLANKS = BLOCKS.register("planks", () -> new BlockGregPlanks());

    public static final Map<Material, BlockCompressed> COMPRESSED = new HashMap<>();
    public static final Map<Material, BlockFrame> FRAMES = new HashMap<>();
    public static final Collection<BlockOre> ORES = new ReferenceArrayList<>();
    public static final Map<Material, BlockSurfaceRock> SURFACE_ROCK = new HashMap<>();
    public static final Collection<BlockFluidBase> FLUID_BLOCKS = new ReferenceArrayList<>();

    public static void init() {
        GregTech.MACHINE = MACHINE = new BlockMachine();
        MACHINE.setRegistryName("machine");

        for (Insulation ins : Insulation.values()) {
            CABLES[ins.ordinal()] = new BlockCable(ins);
            CABLES[ins.ordinal()].setRegistryName(ins.getName());
        }
        for (FluidPipeType type : FluidPipeType.values()) {
            FLUID_PIPES[type.ordinal()] = new BlockFluidPipe(type);
            FLUID_PIPES[type.ordinal()].setRegistryName(String.format("fluid_pipe_%s", type.name));
        }
        for (ItemPipeType type : ItemPipeType.values()) {
            ITEM_PIPES[type.ordinal()] = new BlockItemPipe(type);
            ITEM_PIPES[type.ordinal()].setRegistryName(String.format("item_pipe_%s", type.name));
        }

        BOILER_CASING = new BlockBoilerCasing();
        BOILER_CASING.setRegistryName("boiler_casing");
        BOILER_FIREBOX_CASING = new BlockFireboxCasing();
        BOILER_FIREBOX_CASING.setRegistryName("boiler_firebox_casing");
        METAL_CASING = new BlockMetalCasing();
        METAL_CASING.setRegistryName("metal_casing");
        TURBINE_CASING = new BlockTurbineCasing();
        TURBINE_CASING.setRegistryName("turbine_casing");
        MACHINE_CASING = new BlockMachineCasing();
        MACHINE_CASING.setRegistryName("machine_casing");
        STEAM_CASING = new BlockSteamCasing();
        STEAM_CASING.setRegistryName("steam_casing");
        MULTIBLOCK_CASING = new BlockMultiblockCasing();
        MULTIBLOCK_CASING.setRegistryName("multiblock_casing");
        TRANSPARENT_CASING = new BlockGlassCasing();
        TRANSPARENT_CASING.setRegistryName("transparent_casing");
        WIRE_COIL = new BlockWireCoil();
        WIRE_COIL.setRegistryName("wire_coil");
        FUSION_CASING = new BlockFusionCasing();
        FUSION_CASING.setRegistryName("fusion_casing");
        WARNING_SIGN = new BlockWarningSign();
        WARNING_SIGN.setRegistryName("warning_sign");
        WARNING_SIGN_1 = new BlockWarningSign1();
        WARNING_SIGN_1.setRegistryName("warning_sign_1");
        HERMETIC_CASING = new BlockHermeticCasing();
        HERMETIC_CASING.setRegistryName("hermetic_casing");
        CLEANROOM_CASING = new BlockCleanroomCasing();
        CLEANROOM_CASING.setRegistryName("cleanroom_casing");

        ASPHALT = new BlockAsphalt();
        ASPHALT.setRegistryName("asphalt");

        STONE_SMOOTH = new BlockStoneSmooth();
        STONE_SMOOTH.setRegistryName("stone_smooth");
        STONE_COBBLE = new BlockStoneCobble();
        STONE_COBBLE.setRegistryName("stone_cobble");
        STONE_COBBLE_MOSSY = new BlockStoneCobbleMossy();
        STONE_COBBLE_MOSSY.setRegistryName("stone_cobble_mossy");
        STONE_POLISHED = new BlockStonePolished();
        STONE_POLISHED.setRegistryName("stone_polished");
        STONE_BRICKS = new BlockStoneBricks();
        STONE_BRICKS.setRegistryName("stone_bricks");
        STONE_BRICKS_CRACKED = new BlockStoneBricksCracked();
        STONE_BRICKS_CRACKED.setRegistryName("stone_bricks_cracked");
        STONE_BRICKS_MOSSY = new BlockStoneBricksMossy();
        STONE_BRICKS_MOSSY.setRegistryName("stone_bricks_mossy");
        STONE_CHISELED = new BlockStoneChiseled();
        STONE_CHISELED.setRegistryName("stone_chiseled");
        STONE_TILED = new BlockStoneTiled();
        STONE_TILED.setRegistryName("stone_tiled");
        STONE_TILED_SMALL = new BlockStoneTiledSmall();
        STONE_TILED_SMALL.setRegistryName("stone_tiled_small");
        STONE_BRICKS_SMALL = new BlockStoneBricksSmall();
        STONE_BRICKS_SMALL.setRegistryName("stone_bricks_small");
        STONE_WINDMILL_A = new BlockStoneWindmillA();
        STONE_WINDMILL_A.setRegistryName("stone_windmill_a");
        STONE_WINDMILL_B = new BlockStoneWindmillB();
        STONE_WINDMILL_B.setRegistryName("stone_windmill_b");
        STONE_BRICKS_SQUARE = new BlockStoneBricksSquare();
        STONE_BRICKS_SQUARE.setRegistryName("stone_bricks_square");

        FOAM = new BlockFoam(false);
        FOAM.setRegistryName("foam");
        REINFORCED_FOAM = new BlockFoam(true);
        REINFORCED_FOAM.setRegistryName("reinforced_foam");
        PETRIFIED_FOAM = new BlockPetrifiedFoam(false);
        PETRIFIED_FOAM.setRegistryName("petrified_foam");
        REINFORCED_PETRIFIED_FOAM = new BlockPetrifiedFoam(true);
        REINFORCED_PETRIFIED_FOAM.setRegistryName("reinforced_petrified_foam");

        RUBBER_LOG = new BlockRubberLog();
        RUBBER_LOG.setRegistryName("rubber_log");
        RUBBER_LEAVES = new BlockRubberLeaves();
        RUBBER_LEAVES.setRegistryName("rubber_leaves");
        RUBBER_SAPLING = new BlockRubberSapling();
        RUBBER_SAPLING.setRegistryName("rubber_sapling");
        PLANKS = new BlockGregPlanks();
        PLANKS.setRegistryName("planks");

        createGeneratedBlock(m -> m.hasProperty(GtMaterialProperties.DUST.get()) && m.hasFlag(GENERATE_FRAME), MetaBlocks::createFrameBlock);
        createGeneratedBlock(m -> m.hasProperty(GtMaterialProperties.ORE.get()) && m.hasProperty(PropertyKey.DUST), MetaBlocks::createSurfaceRockBlock);

        createGeneratedBlock(
                material -> (material.hasProperty(GtMaterialProperties.INGOT.get()) || material.hasProperty(GtMaterialProperties.GEM.get()) || material.hasFlag(FORCE_GENERATE_BLOCK.get()))
                        && !TagPrefix.block.isIgnored(material),
                GtBlocks::createCompressedBlock);


        registerTileEntity();

        //not sure if that's a good place for that, but i don't want to make a dedicated method for that
        //could possibly override block methods, but since these props don't depend on state why not just use nice and simple vanilla method
        Blocks.FIRE.setFireInfo(RUBBER_LOG, 5, 5);
        Blocks.FIRE.setFireInfo(RUBBER_LEAVES, 30, 60);
        Blocks.FIRE.setFireInfo(PLANKS, 5, 20);
    }

    /**
     * Deterministically populates a category of MetaBlocks based on the unique registry ID of each qualifying Material.
     *
     * @param materialPredicate a filter for determining if a Material qualifies for generation in the category.
     * @param blockGenerator    a function which accepts a Materials set to pack into a MetaBlock, and the ordinal this
     *                          MetaBlock should have within its category.
     */
    protected static void createGeneratedBlock(Predicate<Material> materialPredicate,
                                               BiConsumer<Material, ResourceLocation> blockGenerator) {

        Map<ResourceLocation, Material> blocksToGenerate = new TreeMap<>();

        for (Material material : MaterialRegistry.MATERIALS_BUILTIN.get().getValues()) {
            if (materialPredicate.test(material)) {
                blocksToGenerate.put(Util.getId(material), material);
            }
        }

        blocksToGenerate.forEach((key, value) -> blockGenerator.accept(value, key));
    }

    private static void createCompressedBlock(Material material, ResourceLocation key) {
        BlockCompressed block = new BlockCompressed(material);
        BLOCKS.register("block_compressed_" + key, () -> block);
        COMPRESSED.put(material, block);
    }

    private static void createFrameBlock(Material[] materials, int index) {
        BlockFrame block = new BlockFrame(materials);
        block.setRegistryName("meta_block_frame_" + index);
        for (Material m : materials) {
            FRAMES.put(m, block);
        }
    }

    private static void createSurfaceRockBlock(Material[] materials, int index) {
        BlockSurfaceRock block = new BlockSurfaceRock(materials);
        block.setRegistryName("meta_block_surface_rock_" + index);
        for (Material material : materials) {
            SURFACE_ROCK.put(material, block);
        }
    }

    public static void registerTileEntity() {
        GameRegistry.registerTileEntity(MetaTileEntityHolder.class, new ResourceLocation(GregTech.MODID, "machine"));
        GameRegistry.registerTileEntity(TileEntityCable.class, new ResourceLocation(GregTech.MODID, "cable"));
        GameRegistry.registerTileEntity(TileEntityCableTickable.class, new ResourceLocation(GregTech.MODID, "cable_tickable"));
        GameRegistry.registerTileEntity(TileEntityFluidPipe.class, new ResourceLocation(GregTech.MODID, "fluid_pipe"));
        GameRegistry.registerTileEntity(TileEntityItemPipe.class, new ResourceLocation(GregTech.MODID, "item_pipe"));
        GameRegistry.registerTileEntity(TileEntityFluidPipeTickable.class, new ResourceLocation(GregTech.MODID, "fluid_pipe_active"));
        GameRegistry.registerTileEntity(TileEntityItemPipeTickable.class, new ResourceLocation(GregTech.MODID, "item_pipe_active"));
    }

    public static void registerItemModels() {
        ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(MACHINE), stack -> MetaTileEntityRenderer.MODEL_LOCATION);
        for (BlockCable cable : CABLES)
            ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(cable), stack -> CableRenderer.INSTANCE.getModelLocation());
        for (BlockFluidPipe pipe : FLUID_PIPES)
            ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(pipe), stack -> FluidPipeRenderer.INSTANCE.getModelLocation());
        for (BlockItemPipe pipe : ITEM_PIPES)
            ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(pipe), stack -> ItemPipeRenderer.INSTANCE.getModelLocation());
        registerItemModel(BOILER_CASING);
        registerItemModel(METAL_CASING);
        registerItemModel(TURBINE_CASING);
        registerItemModel(MACHINE_CASING);
        registerItemModel(STEAM_CASING);
        registerItemModel(WARNING_SIGN);
        registerItemModel(WARNING_SIGN_1);
        registerItemModel(HERMETIC_CASING);
        registerItemModel(CLEANROOM_CASING);
        registerItemModel(ASPHALT);
        registerItemModel(STONE_SMOOTH);
        registerItemModel(STONE_COBBLE);
        registerItemModel(STONE_COBBLE_MOSSY);
        registerItemModel(STONE_POLISHED);
        registerItemModel(STONE_BRICKS);
        registerItemModel(STONE_BRICKS_CRACKED);
        registerItemModel(STONE_BRICKS_MOSSY);
        registerItemModel(STONE_CHISELED);
        registerItemModel(STONE_TILED);
        registerItemModel(STONE_TILED_SMALL);
        registerItemModel(STONE_BRICKS_SMALL);
        registerItemModel(STONE_WINDMILL_A);
        registerItemModel(STONE_WINDMILL_B);
        registerItemModel(STONE_BRICKS_SQUARE);
        registerItemModelWithOverride(RUBBER_LOG, ImmutableMap.of(BlockRubberLog.LOG_AXIS, EnumAxis.Y));
        registerItemModel(RUBBER_LEAVES);
        registerItemModel(RUBBER_SAPLING);
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(RUBBER_SAPLING), 0,
                new ModelResourceLocation(RUBBER_SAPLING.getRegistryName(), "inventory"));
        registerItemModel(PLANKS);

        BOILER_FIREBOX_CASING.onModelRegister();
        WIRE_COIL.onModelRegister();
        FUSION_CASING.onModelRegister();
        MULTIBLOCK_CASING.onModelRegister();
        TRANSPARENT_CASING.onModelRegister();

        COMPRESSED.values().stream().distinct().forEach(IModelSupplier::onModelRegister);
        FRAMES.values().stream().distinct().forEach(IModelSupplier::onModelRegister);
        ORES.forEach(IModelSupplier::onModelRegister);
    }

    private static void registerItemModel(Block block) {
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            //noinspection ConstantConditions
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block),
                    block.getMetaFromState(state),
                    new ModelResourceLocation(block.getRegistryName(),
                            statePropertiesToString(state.getProperties())));
        }
    }

    private static void registerItemModelWithOverride(Block block, Map<Property<?>, Comparable<?>> stateOverrides) {
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            HashMap<Property<?>, Comparable<?>> stringProperties = new HashMap<>(state.getProperties());
            stringProperties.putAll(stateOverrides);
            //noinspection ConstantConditions
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block),
                    block.getMetaFromState(state),
                    new ModelResourceLocation(Util.getId(block),
                            statePropertiesToString(stringProperties)));
        }
    }

    public static void registerStateMappers() {
        ModelLoader.setCustomStateMapper(MACHINE, new SimpleStateMapper(MetaTileEntityRenderer.MODEL_LOCATION));

        IStateMapper normalStateMapper = new SimpleStateMapper(CableRenderer.INSTANCE.getModelLocation());
        for (BlockCable cable : CABLES) {
            ModelLoader.setCustomStateMapper(cable, normalStateMapper);
        }
        normalStateMapper = new SimpleStateMapper(FluidPipeRenderer.INSTANCE.getModelLocation());
        for (BlockFluidPipe pipe : FLUID_PIPES) {
            ModelLoader.setCustomStateMapper(pipe, normalStateMapper);
        }
        normalStateMapper = new SimpleStateMapper(ItemPipeRenderer.INSTANCE.getModelLocation());
        for (BlockItemPipe pipe : ITEM_PIPES) {
            ModelLoader.setCustomStateMapper(pipe, normalStateMapper);
        }
        normalStateMapper = new SimpleStateMapper(BlockSurfaceRock.MODEL_LOCATION);
        for (BlockSurfaceRock surfaceRock : new HashSet<>(SURFACE_ROCK.values())) {
            ModelLoader.setCustomStateMapper(surfaceRock, normalStateMapper);
        }

        normalStateMapper = new StateMapperBase() {
            @Nonnull
            @Override
            protected ModelResourceLocation getModelResourceLocation(@Nonnull BlockState state) {
                return new ModelResourceLocation(Block.REGISTRY.getNameForObject(state.getBlock()), "normal");
            }
        };

        ModelLoader.setCustomStateMapper(FOAM, normalStateMapper);
        ModelLoader.setCustomStateMapper(REINFORCED_FOAM, normalStateMapper);
        ModelLoader.setCustomStateMapper(PETRIFIED_FOAM, normalStateMapper);
        ModelLoader.setCustomStateMapper(REINFORCED_PETRIFIED_FOAM, normalStateMapper);

        BakedModelHandler modelHandler = new BakedModelHandler();
        MinecraftForge.EVENT_BUS.register(modelHandler);
        FLUID_BLOCKS.forEach(modelHandler::addFluidBlock);

        ClientRegistry.bindTileEntitySpecialRenderer(MetaTileEntityHolder.class, new MetaTileEntityTESR());
    }

    @SideOnly(Side.CLIENT)
    public static void registerColors() {
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(
                FOAM_BLOCK_COLOR, FOAM, REINFORCED_FOAM, PETRIFIED_FOAM, REINFORCED_PETRIFIED_FOAM);

        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(RUBBER_LEAVES_BLOCK_COLOR, RUBBER_LEAVES);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(RUBBER_LEAVES_ITEM_COLOR, RUBBER_LEAVES);

        MetaBlocks.COMPRESSED.values().stream().distinct().forEach(block -> {
            Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(COMPRESSED_BLOCK_COLOR, block);
            Minecraft.getMinecraft().getItemColors().registerItemColorHandler(COMPRESSED_ITEM_COLOR, block);
        });

        MetaBlocks.FRAMES.values().forEach(block -> {
            Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(FRAME_BLOCK_COLOR, block);
            Minecraft.getMinecraft().getItemColors().registerItemColorHandler(FRAME_ITEM_COLOR, block);
        });

        MetaBlocks.SURFACE_ROCK.values().stream().distinct().forEach(block -> {
            Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(SURFACE_ROCK_BLOCK_COLOR, block);
        });

        MetaBlocks.ORES.stream().distinct().forEach(block -> {
            Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(ORE_BLOCK_COLOR, block);
            Minecraft.getMinecraft().getItemColors().registerItemColorHandler(ORE_ITEM_COLOR, block);
        });

        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(MACHINE_CASING_BLOCK_COLOR, MACHINE_CASING);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(MACHINE_CASING_ITEM_COLOR, MACHINE_CASING);

        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(MACHINE_CASING_BLOCK_COLOR, HERMETIC_CASING);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(MACHINE_CASING_ITEM_COLOR, HERMETIC_CASING);
    }

    public static void registerTagDict() {
        TagUnifier.registerTag(new ItemStack(RUBBER_LOG, 1, GTValues.W), OrePrefix.log, Materials.Wood);
        TagUnifier.registerTag(new ItemStack(RUBBER_LEAVES, 1, GTValues.W), "treeLeaves");
        TagUnifier.registerTag(new ItemStack(RUBBER_SAPLING, 1, GTValues.W), "treeSapling");
        TagUnifier.registerTag(PLANKS.getItemVariant(BlockGregPlanks.BlockType.RUBBER_PLANK), OrePrefix.plank, Materials.Wood);
        TagUnifier.registerTag(PLANKS.getItemVariant(BlockGregPlanks.BlockType.RUBBER_PLANK), new ItemMaterialInfo(new MaterialStack(Materials.Wood, GTValues.M)));
        TagUnifier.registerTag(PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK), OrePrefix.plank, Materials.TreatedWood);
        TagUnifier.registerTag(PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK), new ItemMaterialInfo(new MaterialStack(Materials.TreatedWood, GTValues.M)));
        GameRegistry.addSmelting(RUBBER_LOG, new ItemStack(Items.COAL, 1, 1), 0.15F);

        for (Entry<Material, BlockCompressed> entry : COMPRESSED.entrySet()) {
            Material material = entry.getKey();
            BlockCompressed block = entry.getValue();
            ItemStack itemStack = block.getItem(material);
            TagUnifier.registerTag(itemStack, OrePrefix.block, material);
        }

        for (Entry<Material, BlockFrame> entry : FRAMES.entrySet()) {
            Material material = entry.getKey();
            BlockFrame block = entry.getValue();
            ItemStack itemStack = block.getItem(material);
            TagUnifier.registerTag(itemStack, OrePrefix.frameGt, material);
        }

        for (BlockOre blockOre : ORES) {
            Material material = blockOre.material;
            for (StoneType stoneType : blockOre.STONE_TYPE.getAllowedValues()) {
                if (stoneType == null) continue;
                ItemStack normalStack = blockOre.getItem(blockOre.getDefaultState()
                        .withProperty(blockOre.STONE_TYPE, stoneType));
                TagUnifier.registerTag(normalStack, stoneType.processingPrefix, material);
            }
        }
        for (BlockCable cable : CABLES) {
            for (Material pipeMaterial : cable.getEnabledMaterials()) {
                ItemStack itemStack = cable.getItem(pipeMaterial);
                TagUnifier.registerTag(itemStack, cable.getPrefix(), pipeMaterial);
            }
        }
        for (BlockFluidPipe pipe : FLUID_PIPES) {
            for (Material pipeMaterial : pipe.getEnabledMaterials()) {
                ItemStack itemStack = pipe.getItem(pipeMaterial);
                TagUnifier.registerTag(itemStack, pipe.getPrefix(), pipeMaterial);
            }
        }
        for (BlockItemPipe pipe : ITEM_PIPES) {
            for (Material pipeMaterial : pipe.getEnabledMaterials()) {
                ItemStack itemStack = pipe.getItem(pipeMaterial);
                TagUnifier.registerTag(itemStack, pipe.getPrefix(), pipeMaterial);
            }
        }
    }

    public static String statePropertiesToString(Map<Property<?>, Comparable<?>> properties) {
        StringBuilder stringbuilder = new StringBuilder();

        List<Map.Entry<Property<?>, Comparable<?>>> entries = properties.entrySet().stream()
                .sorted(Comparator.comparing(c -> c.getKey().getName()))
                .collect(Collectors.toList());

        for (Map.Entry<Property<?>, Comparable<?>> entry : entries) {
            if (stringbuilder.length() != 0) {
                stringbuilder.append(",");
            }

            Property<?> property = entry.getKey();
            stringbuilder.append(property.getName());
            stringbuilder.append("=");
            stringbuilder.append(getPropertyName(property, entry.getValue()));
        }

        if (stringbuilder.length() == 0) {
            stringbuilder.append("normal");
        }

        return stringbuilder.toString();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String getPropertyName(Property<T> property, Comparable<?> value) {
        return property.getName((T) value);
    }
}
