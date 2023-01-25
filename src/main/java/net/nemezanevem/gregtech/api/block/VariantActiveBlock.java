package net.nemezanevem.gregtech.api.block;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.client.model.IModelSupplier;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class VariantActiveBlock<T extends Enum<T> & StringRepresentable> extends VariantBlock<T> implements IModelSupplier {

    public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(new ResourceLocation(GregTech.MODID, "active_blocks"), "inventory");
    public static final Object2ObjectOpenHashMap<Integer, ObjectSet<BlockPos>> ACTIVE_BLOCKS = new Object2ObjectOpenHashMap<>();
    private static final List<VariantActiveBlock<?>> INSTANCES = new ArrayList<>();
    public static final Object2ObjectOpenHashMap<Block, ObjectOpenHashSet<RenderType>> block2blockRenderLayerMap = new Object2ObjectOpenHashMap<>();
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public VariantActiveBlock(BlockBehaviour.Properties properties) {
        super(properties);
        INSTANCES.add(this);
    }

    @Override
    public BlockState getState(T variant) {
        return super.getState(variant).setValue(ACTIVE, false);
    }

    @Override
    public boolean canRenderInLayer(BlockState state, RenderType layer) {
        return block2blockRenderLayerMap.containsKey(state.getBlock()) && block2blockRenderLayerMap.get(state.getBlock()).contains(layer);
    }

    @Override
    public void onTextureStitch(TextureStitchEvent.Pre event) {

    }

    @Override
    public void onModelRegister() {
        for (BlockState state : this.getStateDefinition().getPossibleStates()) {
            //ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), this.getMetaFromState(state), new ModelResourceLocation(this.getRegistryName(), "active=true," + statePropertiesToString(state.getProperties())));
            //ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), this.getMetaFromState(state), new ModelResourceLocation(this.getRegistryName(), "active=false," + statePropertiesToString(state.getProperties())));
            ModelLoader.setCustomModelResourceLocation(this, this.getMetaFromState(state), new ModelResourceLocation(this.getRegistryName(), statePropertiesToString(state.getProperties())));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST) // low priority to capture all event-registered models
    public static void onModelBake(ModelEvent.BakingCompleted event) {
        block2blockRenderLayerMap.clear();
        //Go over all VariantActiveBlock instances, then going over their model, and if they have a quad
        //to render on that render layer, add it to the map.
        for (Block b : INSTANCES) {
            for (BlockState state : b.getStateDefinition().getPossibleStates()) {
                BakedModel bakedModel = event.getModelManager().getModel(new ModelResourceLocation(Util.getId(b), statePropertiesToString(state.getProperties())));
                if (bakedModel != null) {
                    for (RenderType layer : bakedModel.getRenderTypes(state, RandomSource.create(), ModelData.EMPTY)) {
                        for (Direction facing : Direction.values()) {
                            if (bakedModel.getQuads(state, facing, RandomSource.create(), ModelData.EMPTY, layer).size() > 0) {
                                block2blockRenderLayerMap.putIfAbsent(b, new ObjectOpenHashSet<>());
                                block2blockRenderLayerMap.get(b).add(layer);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}
