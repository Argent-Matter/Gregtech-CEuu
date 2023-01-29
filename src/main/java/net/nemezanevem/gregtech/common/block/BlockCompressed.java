package net.nemezanevem.gregtech.common.block;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.block.DelayedStateBlock;
import net.nemezanevem.gregtech.api.item.toolitem.ToolClass;
import net.nemezanevem.gregtech.api.unification.material.GtMaterials;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.properties.GtMaterialProperties;
import net.nemezanevem.gregtech.api.unification.material.properties.info.GtMaterialIconTypes;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.client.model.IModelSupplier;
import net.nemezanevem.gregtech.common.block.properties.PropertyMaterial;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collection;

public final class BlockCompressed extends DelayedStateBlock implements IModelSupplier {

    public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(new ResourceLocation(GregTech.MODID, "compressed_block"), "normal");

    public final PropertyMaterial variantProperty;

    public BlockCompressed(Collection<Material> materials) {
        super(BlockBehaviour.Properties.of(net.minecraft.world.level.material.Material.METAL, (blockState -> )).strength(5.0f, 10.0f));
        this.variantProperty = PropertyMaterial.create("variant", materials);
        initBlockState();
    }

    @Override
    public ToolClass getHarvestTool(BlockState state) {
        Material material = state.getValue(variantProperty);
        if (material.isSolid()) {
            return ToolClass.PICKAXE;
        } else if (material.hasProperty(GtMaterialProperties.DUST.get())) {
            return ToolClass.SHOVEL;
        }
        return ToolClass.PICKAXE;
    }

    @Override
    public int getHarvestLevel(BlockState state) {
        Material material = state.getValue(variantProperty);
        if (material.hasProperty(GtMaterialProperties.DUST.get())) {
            return material.getBlockHarvestLevel();
        }
        return 0;
    }

    public ItemStack getItem(BlockState blockState) {
        return new ItemStack(blockState.getBlock());
    }

    public ItemStack getItem(Material material) {
        return getItem(defaultBlockState().setValue(variantProperty, material));
    }

    public Material getGtMaterial(int meta) {
        return variantProperty.getPossibleValues().get(meta);
    }

    public BlockState getBlock(Material material) {
        return defaultBlockState().setValue(variantProperty, material);
    }

    @Override
    public void fillItemCategory(CreativeModeTab pTab, NonNullList<ItemStack> pItems) {
        stateDefinition.getPossibleStates().stream()
                .filter(blockState -> blockState.getValue(variantProperty) != GtMaterials.NULL.get())
                .forEach(blockState -> pItems.add(getItem(blockState)));
    }

    @Override
    @Nonnull
    public net.minecraft.world.level.material.Material getMaterial(BlockState state) {
        Material material = state.getValue(variantProperty);
        if (material.hasProperty(GtMaterialProperties.GEM.get())) {
            return net.minecraft.world.level.material.Material.METAL;
        } else if (material.hasProperty(GtMaterialProperties.INGOT.get())) {
            return net.minecraft.world.level.material.Material.METAL;
        } else if (material.hasProperty(GtMaterialProperties.DUST.get())) {
            return net.minecraft.world.level.material.Material.SAND;
        }
        return net.minecraft.world.level.material.Material.STONE;
    }

    @Nonnull
    public MaterialColor getMapColor(@Nonnull BlockState state) {
        return getMaterial(state).getColor();
    }

    @Nonnull
    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        Material material = state.getValue(variantProperty);
        if (material.hasProperty(GtMaterialProperties.GEM.get())) {
            return SoundType.STONE;
        } else if (material.hasProperty(GtMaterialProperties.INGOT.get())) {
            return SoundType.METAL;
        } else if (material.hasProperty(GtMaterialProperties.DUST.get())) {
            return SoundType.SAND;
        }
        return SoundType.STONE;
    }

    @Override
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        for (BlockState state : this.stateDefinition.getPossibleStates()) {
            Material m = state.getValue(variantProperty);
            event.addSprite(GtMaterialIconTypes.block.get().getBlockTexturePath(m.getMaterialIconSet()));
        }
    }

    @Override
    public void onModelRegister(ModelEvent.RegisterAdditional event) {
        ModelLoader.setCustomStateMapper(this, new SimpleStateMapper(MODEL_LOCATION));
        for (BlockState state : this.stateDefinition.getPossibleStates()) {
            event.register(MODEL_LOCATION);
            ModelLoader.setCustomModelResourceLocation(this.getItem(state), this.getMetaFromState(state), MODEL_LOCATION);
        }
    }
}
