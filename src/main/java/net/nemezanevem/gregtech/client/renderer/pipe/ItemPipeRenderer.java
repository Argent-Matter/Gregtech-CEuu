package net.nemezanevem.gregtech.client.renderer.pipe;

import codechicken.lib.model.PerspectiveModelState;
import codechicken.lib.texture.AtlasRegistrar;
import codechicken.lib.vec.uv.IconTransformation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.pipenet.block.BlockPipe;
import net.nemezanevem.gregtech.api.pipenet.block.IPipeType;
import net.nemezanevem.gregtech.api.pipenet.tile.IPipeTile;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import net.nemezanevem.gregtech.common.pipelike.itempipe.ItemPipeType;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class ItemPipeRenderer extends PipeRenderer {

    public static final ItemPipeRenderer INSTANCE = new ItemPipeRenderer();
    private final EnumMap<ItemPipeType, TextureAtlasSprite> pipeTextures = new EnumMap<>(ItemPipeType.class);

    private ItemPipeRenderer() {
        super("gt_item_pipe", new ResourceLocation(GregTech.MODID, "item_pipe"));
    }

    @Override
    public void registerIcons(AtlasRegistrar map) {
        pipeTextures.put(ItemPipeType.SMALL, Textures.PIPE_SMALL);
        pipeTextures.put(ItemPipeType.NORMAL, Textures.PIPE_NORMAL);
        pipeTextures.put(ItemPipeType.LARGE, Textures.PIPE_LARGE);
        pipeTextures.put(ItemPipeType.HUGE, Textures.PIPE_HUGE);
        pipeTextures.put(ItemPipeType.RESTRICTIVE_SMALL, Textures.PIPE_SMALL);
        pipeTextures.put(ItemPipeType.RESTRICTIVE_NORMAL, Textures.PIPE_NORMAL);
        pipeTextures.put(ItemPipeType.RESTRICTIVE_LARGE, Textures.PIPE_LARGE);
        pipeTextures.put(ItemPipeType.RESTRICTIVE_HUGE, Textures.PIPE_HUGE);
    }

    @Override
    public void buildRenderer(PipeRenderContext renderContext, BlockPipe<?, ?, ?> blockPipe, IPipeTile<?, ?> pipeTile, IPipeType<?> pipeType, @Nullable Material material) {
        if (material == null || !(pipeType instanceof ItemPipeType)) {
            return;
        }
        renderContext.addOpenFaceRender(new IconTransformation(pipeTextures.get(pipeType)))
                .addSideRender(new IconTransformation(Textures.PIPE_SIDE));

        if (((ItemPipeType) pipeType).isRestrictive()) {
            renderContext.addSideRender(false, new IconTransformation(Textures.RESTRICTIVE_OVERLAY));
        }
    }

    @Override
    public TextureAtlasSprite getParticleTexture(IPipeType<?> pipeType, @Nullable Material material) {
        return Textures.PIPE_SIDE;
    }

    @Override
    public @org.jetbrains.annotations.Nullable PerspectiveModelState getModelState() {
        return null;
    }

    @Override
    public boolean canHandleBlock(BlockAndTintGetter world, BlockPos pos, BlockState blockState, @org.jetbrains.annotations.Nullable RenderType renderType) {
        return false;
    }
}
