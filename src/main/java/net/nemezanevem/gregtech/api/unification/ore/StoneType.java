package net.nemezanevem.gregtech.api.unification.ore;

import com.google.common.base.Preconditions;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.properties.GtMaterialProperties;
import net.nemezanevem.gregtech.api.unification.tag.TagPrefix;
import net.nemezanevem.gregtech.common.ConfigHolder;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * For ore generation
 */
public class StoneType implements Comparable<StoneType> {

    public final String name;

    public final TagPrefix processingPrefix;
    public final Material stoneMaterial;
    public final Supplier<BlockState> stone;
    public final SoundType soundType;
    //we are using guava predicate because isReplaceableOreGen uses it
    @SuppressWarnings("Guava")
    private final com.google.common.base.Predicate<BlockState> predicate;
    public final boolean shouldBeDroppedAsItem;

    public static final GTControlledRegistry<String, StoneType> STONE_TYPE_REGISTRY = new GTControlledRegistry<>(128);

    public StoneType(int id, String name, SoundType soundType, TagPrefix processingPrefix, Material stoneMaterial, Supplier<BlockState> stone, Predicate<BlockState> predicate, boolean shouldBeDroppedAsItem) {
        Preconditions.checkArgument(
                stoneMaterial.hasProperty(GtMaterialProperties.DUST.get()),
                "Stone type must be made with a Material with the Dust Property!"
        );
        this.name = name;
        this.soundType = soundType;
        this.processingPrefix = processingPrefix;
        this.stoneMaterial = stoneMaterial;
        this.stone = stone;
        this.predicate = predicate::test;
        this.shouldBeDroppedAsItem = shouldBeDroppedAsItem || ConfigHolder.worldgen.allUniqueStoneTypes;
        STONE_TYPE_REGISTRY.register(id, name, this);
        if (ModList.get().isLoaded(GTValues.MODID_JEI) && this.shouldBeDroppedAsItem) {
            OreByProduct.addOreByProductPrefix(this.processingPrefix);
        }
    }

    @Override
    public int compareTo(@Nonnull StoneType stoneType) {
        return STONE_TYPE_REGISTRY.getIDForObject(this) - STONE_TYPE_REGISTRY.getIDForObject(stoneType);
    }

    private static final ThreadLocal<Boolean> hasDummyPredicateRan = ThreadLocal.withInitial(() -> false);
    private static final com.google.common.base.Predicate<BlockState> dummyPredicate = state -> {
        hasDummyPredicateRan.set(true);
        return false;
    };

    public static void init() {
        //noinspection ResultOfMethodCallIgnored
        StoneTypes.STONE.name.getBytes();
    }

    public static StoneType computeStoneType(BlockState state, IBlockAccess world, BlockPos pos) {
        // First: check if this Block's isReplaceableOreGen even considers the predicate passed through
        boolean dummy$isReplaceableOreGen = state.getBlock().isReplaceableOreGen(state, world, pos, dummyPredicate);
        if (hasDummyPredicateRan.get()) {
            // Current Block's isReplaceableOreGen does indeed consider the predicate
            // Reset hasDummyPredicateRan for the next test
            hasDummyPredicateRan.set(false);
            // Pass through actual predicates and test for real
            for (StoneType stoneType : STONE_TYPE_REGISTRY) {
                if (state.getBlock().isReplaceableOreGen(state, world, pos, stoneType.predicate)) {
                    // Found suitable match
                    return stoneType;
                }
            }
        } else if (dummy$isReplaceableOreGen) {
            // It is not considered, but the test still returned true (this means the impl was probably very lazily done)
            // We have to test against the BlockState ourselves to see if there's a suitable StoneType
            for (StoneType stoneType : STONE_TYPE_REGISTRY) {
                if (stoneType.predicate.test(state)) {
                    // Found suitable match
                    return stoneType;
                }
            }
        }
        return null;
    }

}
