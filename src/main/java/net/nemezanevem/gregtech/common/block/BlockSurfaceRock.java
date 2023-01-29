package net.nemezanevem.gregtech.common.block;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.block.DelayedStateBlock;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.TagUnifier;
import net.nemezanevem.gregtech.api.unification.tag.TagPrefix;
import net.nemezanevem.gregtech.common.block.properties.PropertyMaterial;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("deprecation")
public class BlockSurfaceRock extends DelayedStateBlock {

    private static final VoxelShape STONE_BOUNDS = Block.box(2, 0, 2, 14, 2, 14);
    public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(new ResourceLocation(GregTech.MODID, "surface_rock"), "normal");
    public final PropertyMaterial variantProperty;

    public BlockSurfaceRock(List<Material> validMaterials) {
        super(BlockBehaviour.Properties.of(net.minecraft.world.level.material.Material.VEGETABLE).strength(0.25f, 0.0f));
        this.variantProperty = PropertyMaterial.create("variant", validMaterials);
        initBlockState();
    }

    public BlockState getBlock(Material material) {
        return defaultBlockState().setValue(variantProperty, material);
    }

    @Override
    protected StateDefinition<Block, BlockState> createStateContainer() {
        return new StateDefinition.Builder<Block, BlockState>(this).add(variantProperty).create(Block::defaultBlockState, BlockState::new);
    }

    public ItemStack getItem(BlockState blockState) {
        return new ItemStack(blockState.getBlock());
    }

    public ItemStack getItem(Material material) {
        return getItem(defaultBlockState().setValue(variantProperty, material));
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        getDrops(pState, (ServerLevel) pLevel, pPos, pLevel.getBlockEntity(pPos));
        pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), 3);
        pPlayer.swing(pHand);
        return InteractionResult.PASS;
    }

    @Nonnull
    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return SoundType.STONE;
    }

    @Nonnull
    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return STONE_BOUNDS;
    }

    private ItemStack getDropStack(BlockState blockState, int amount) {
        Material material = blockState.getValue(variantProperty);
        return TagUnifier.get(TagPrefix.dustTiny, material, amount);
    }

    @Override
    @Nonnull
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return getDropStack(state, 1);
    }

    @Override
    public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
        ResourceLocation resourcelocation = this.getLootTable();
        if (resourcelocation == BuiltInLootTables.EMPTY) {
            return Collections.emptyList();
        } else {
            LootContext lootcontext = pBuilder.withParameter(LootContextParams.BLOCK_STATE, pState).create(LootContextParamSets.BLOCK);
            ServerLevel serverlevel = lootcontext.getLevel();
            LootTable loottable = serverlevel.getServer().getLootTables().get(resourcelocation);
            return loottable.getRandomItems(lootcontext);
        }
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        if (pos.above().equals(pos)) {
            if (level.getBlockState(pos).getVisualShape(level, pos, CollisionContext.empty()) != Shapes.block()) {
                level.getChunk(pos).setBlockState(pos, Blocks.AIR.defaultBlockState(), false);
            }
        }
    }
}
