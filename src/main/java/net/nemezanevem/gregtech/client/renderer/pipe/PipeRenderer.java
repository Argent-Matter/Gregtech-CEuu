package net.nemezanevem.gregtech.client.renderer.pipe;

import codechicken.lib.model.PerspectiveModelState;
import codechicken.lib.render.BlockRenderer;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.block.BlockRenderingRegistry;
import codechicken.lib.render.block.ICCBlockRenderer;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.render.lighting.LightMatrix;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.AtlasRegistrar;
import codechicken.lib.texture.SpriteRegistryHelper;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.util.TransformUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.uv.IconTransformation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.block.DelayedStateBlock;
import net.nemezanevem.gregtech.api.cover.ICoverable;
import net.nemezanevem.gregtech.api.pipenet.block.BlockPipe;
import net.nemezanevem.gregtech.api.pipenet.block.IPipeType;
import net.nemezanevem.gregtech.api.pipenet.block.ItemBlockPipe;
import net.nemezanevem.gregtech.api.pipenet.block.material.BlockMaterialPipe;
import net.nemezanevem.gregtech.api.pipenet.block.material.TileEntityMaterialPipeBase;
import net.nemezanevem.gregtech.api.pipenet.tile.IPipeTile;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.properties.info.GtMaterialIconTypes;
import net.nemezanevem.gregtech.api.unification.material.properties.info.MaterialIconType;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.client.renderer.CubeRendererState;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import net.nemezanevem.gregtech.common.pipelike.itempipe.BlockItemPipe;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class PipeRenderer implements ICCBlockRenderer, IItemRenderer {

    public final ModelResourceLocation modelLocation;
    private final String name;
    private RenderType blockRenderType;
    private static final ThreadLocal<BlockRenderer.BlockFace> blockFaces = ThreadLocal.withInitial(BlockRenderer.BlockFace::new);
    private static final Cuboid6 FRAME_RENDER_CUBOID = new Cuboid6(0.001, 0.001, 0.001, 0.999, 0.999, 0.999);

    public PipeRenderer(String name, ModelResourceLocation modelLocation) {
        this.name = name;
        this.modelLocation = modelLocation;
    }

    public PipeRenderer(String name, ResourceLocation modelLocation) {
        this(name, new ModelResourceLocation(modelLocation, "normal"));
    }

    public void preInit() {
        BlockRenderingRegistry.registerGlobalRenderer(this);
        MinecraftForge.EVENT_BUS.register(this);
        GregTech.spriteRegistryHelper.addIIconRegister(this::registerIcons);
    }

    public ModelResourceLocation getModelLocation() {
        return modelLocation;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        return ChunkRenderTypeSet.of(state.getBlock() instanceof DelayedStateBlock block ? block.getRenderType() : RenderType.cutout());
    }


    public abstract void registerIcons(AtlasRegistrar map);

    @SubscribeEvent
    public void onModelsBake(ModelEvent.BakingCompleted event) {
        event.getModels().put(modelLocation, this);
    }

    public abstract void buildRenderer(PipeRenderContext renderContext, BlockPipe<?, ?, ?> blockPipe, @Nullable IPipeTile<?, ?> pipeTile, IPipeType<?> pipeType, @Nullable Material material);

    @Override
    public void renderItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack mStack, MultiBufferSource source, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof ItemBlockPipe)) {
            return;
        }
        CCRenderState renderState = CCRenderState.instance();
        RenderSystem.enableBlend();
        renderState.reset();
        renderState.startDrawing(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        BlockPipe<?, ?, ?> blockFluidPipe = (BlockPipe<?, ?, ?>) ((ItemBlockPipe<?, ?>) stack.getItem()).getBlock();
        IPipeType<?> pipeType = blockFluidPipe.getItemPipeType(stack);
        Material material = blockFluidPipe instanceof BlockMaterialPipe ? ((BlockMaterialPipe<?, ?, ?>) blockFluidPipe).getItemMaterial(stack) : null;
        if (pipeType != null) {
            // 12 == 0b1100 is North and South connection (index 2 & 3)
            PipeRenderContext renderContext = new PipeRenderContext(12, 0, pipeType.getThickness());
            renderContext.color = Util.convertRGBtoOpaqueRGBA_CL(getPipeColor(material, -1));
            buildRenderer(renderContext, blockFluidPipe, null, pipeType, material);
            renderPipeBlock(renderState, renderContext);
        }
        renderState.draw();
        RenderSystem.disableBlend();
    }

    @Override
    public void renderBlock(BlockState state, BlockPos pos, BlockAndTintGetter world, PoseStack mStack, VertexConsumer builder, RandomSource random, ModelData data, @Nullable RenderType renderType) {
        CCRenderState renderState = CCRenderState.instance();
        renderState.reset();
        renderState.bind((BufferBuilder) builder);
        renderState.setBrightness(world, pos);

        BlockPipe<?, ?, ?> blockPipe = (BlockPipe<?, ?, ?>) state.getBlock();
        IPipeTile<?, ?> pipeTile = blockPipe.getPipeTileEntity(world, pos);

        if (pipeTile == null) {
            return;
        }

        IPipeType<?> pipeType = pipeTile.getPipeType();
        Material pipeMaterial = pipeTile instanceof TileEntityMaterialPipeBase ? ((TileEntityMaterialPipeBase<?, ?>) pipeTile).getPipeMaterial() : null;
        int paintingColor = pipeTile.getPaintingColor();
        int connectedSidesMap = pipeTile.getVisualConnections();
        int blockedConnections = pipeTile.getBlockedConnections();

        if (pipeType != null) {
            boolean[] sideMask = new boolean[Direction.values().length];
            for (Direction side : Direction.values()) {
                sideMask[side.ordinal()] = !state.skipRendering(state, side);
            }
            Textures.RENDER_STATE.set(new CubeRendererState(renderType, sideMask, world));
            if (renderType == RenderType.cutout()) {
                renderState.lightMatrix.locate(world, pos);
                PipeRenderContext renderContext = new PipeRenderContext(pos, renderState.lightMatrix, connectedSidesMap, blockedConnections, pipeType.getThickness());
                renderContext.color = Util.convertRGBtoOpaqueRGBA_CL(getPipeColor(pipeMaterial, paintingColor));
                buildRenderer(renderContext, blockPipe, pipeTile, pipeType, pipeMaterial);
                renderPipeBlock(renderState, renderContext);
                renderFrame(pipeTile, pos, renderState, connectedSidesMap);
            }

            ICoverable coverable = pipeTile.getCoverableImplementation();
            coverable.renderCovers(renderState, new Matrix4().translate(pos.getX(), pos.getY(), pos.getZ()), renderType);
            Textures.RENDER_STATE.set(null);
        }
    }

    private void renderFrame(IPipeTile<?, ?> pipeTile, BlockPos pos, CCRenderState renderState, int connections) {
        Material frameMaterial = pipeTile.getFrameMaterial();
        if (frameMaterial != null) {
            ResourceLocation texturePath = GtMaterialIconTypes.frame.get().getBlockTexturePath(frameMaterial.getMaterialIconSet());
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texturePath);
            IVertexOperation[] pipeline = {
                    new Translation(pos),
                    renderState.lightMatrix,
                    new IconTransformation(sprite),
                    new ColourMultiplier(Util.convertRGBtoOpaqueRGBA_CL(frameMaterial.getMaterialRGB()))
            };

            for (Direction side : Direction.values()) {
                // only render frame if it doesn't have a cover
                if ((connections & 1 << (12 + side.ordinal())) == 0) {
                    BlockRenderer.BlockFace blockFace = blockFaces.get();
                    blockFace.loadCuboidFace(FRAME_RENDER_CUBOID, side.ordinal());
                    renderState.setPipeline(blockFace, 0, blockFace.verts.length, pipeline);
                    renderState.render();
                }
            }
        }
    }

    private int getPipeColor(Material material, int paintingColor) {
        if (paintingColor == -1) {
            return material == null ? 0xFFFFFF : material.getMaterialRGB();
        }
        return paintingColor;
    }

    public void renderPipeBlock(CCRenderState renderState, PipeRenderContext renderContext) {
        Cuboid6 cuboid6 = BlockItemPipe.getSideBox(null, renderContext.pipeThickness);
        if ((renderContext.connections & 63) == 0) {
            // base pipe without connections
            for (Direction renderedSide : Direction.values()) {
                renderOpenFace(renderState, renderContext, renderedSide, cuboid6);
            }
        } else {
            for (Direction renderedSide : Direction.values()) {
                // if connection is blocked
                if ((renderContext.connections & 1 << renderedSide.ordinal()) == 0) {
                    int oppositeIndex = renderedSide.getOpposite().ordinal();
                    if ((renderContext.connections & 1 << oppositeIndex) > 0 && (renderContext.connections & 63 & ~(1 << oppositeIndex)) == 0) {
                        // render open texture if opposite is open and no other
                        renderOpenFace(renderState, renderContext, renderedSide, cuboid6);
                    } else {
                        // else render pipe side
                        renderPipeSide(renderState, renderContext, renderedSide, cuboid6);
                    }
                } else {
                    // else render connection cuboid
                    renderPipeCube(renderState, renderContext, renderedSide);
                }
            }
        }
    }

    private void renderPipeCube(CCRenderState renderState, PipeRenderContext renderContext, Direction side) {
        Cuboid6 cuboid = BlockItemPipe.getSideBox(side, renderContext.pipeThickness);
        boolean doRenderBlockedOverlay = (renderContext.blockedConnections & (1 << side.ordinal())) > 0;
        // render connection cuboid
        for (Direction renderedSide : Direction.values()) {
            if (renderedSide.getAxis() != side.getAxis()) {
                // render side textures
                renderPipeSide(renderState, renderContext, renderedSide, cuboid);
                if (doRenderBlockedOverlay) {
                    // render blocked connections
                    renderFace(renderState, renderContext.blockedOverlay, renderedSide, cuboid);
                }
            }
        }
        if ((renderContext.connections & 1 << (6 + side.ordinal())) > 0) {
            // if neighbour pipe is smaller, render closed texture
            renderPipeSide(renderState, renderContext, side, cuboid);
        } else {
            if ((renderContext.connections & 1 << (12 + side.ordinal())) > 0) {
                // if face has a cover offset face by 0.001 to avoid z fighting
                cuboid = BlockItemPipe.getCoverSideBox(side, renderContext.pipeThickness);
            }
            renderOpenFace(renderState, renderContext, side, cuboid);
        }
    }

    private void renderOpenFace(CCRenderState renderState, PipeRenderContext renderContext, Direction side, Cuboid6 cuboid6) {
        for (IVertexOperation[] vertexOperations : renderContext.openFaceRenderer) {
            renderFace(renderState, vertexOperations, side, cuboid6);
        }
    }

    private void renderPipeSide(CCRenderState renderState, PipeRenderContext renderContext, Direction side, Cuboid6 cuboid6) {
        for (IVertexOperation[] vertexOperations : renderContext.pipeSideRenderer) {
            renderFace(renderState, vertexOperations, side, cuboid6);
        }
    }

    private void renderFace(CCRenderState renderState, IVertexOperation[] pipeline, Direction side, Cuboid6 cuboid6) {
        BlockRenderer.BlockFace blockFace = blockFaces.get();
        blockFace.loadCuboidFace(cuboid6, side.ordinal());
        renderState.setPipeline(blockFace, 0, blockFace.verts.length, pipeline);
        renderState.render();
    }

    @Override
    public void renderBrightness(BlockState state, float brightness) {
    }

    @Override
    public void handleRenderBlockDamage(BlockAndTintGetter world, BlockPos pos, BlockState state, TextureAtlasSprite sprite, BufferBuilder buffer) {
        CCRenderState renderState = CCRenderState.instance();
        renderState.reset();
        renderState.bind(buffer);
        renderState.setPipeline(new Vector3(pos.getX(), pos.getY(), pos.getZ()).translation(), new IconTransformation(sprite));
        BlockPipe<?, ?, ?> blockPipe = (BlockPipe<?, ?, ?>) state.getBlock();
        IPipeTile<?, ?> pipeTile = blockPipe.getPipeTileEntity(world, pos);
        if (pipeTile == null) {
            return;
        }
        IPipeType<?> pipeType = pipeTile.getPipeType();
        if (pipeType == null) {
            return;
        }
        float thickness = pipeType.getThickness();
        int connectedSidesMask = pipeTile.getConnections();
        Cuboid6 baseBox = BlockItemPipe.getSideBox(null, thickness);
        BlockRenderer.renderCuboid(renderState, baseBox, 0);
        for (Direction renderSide : Direction.values()) {
            if ((connectedSidesMask & (1 << renderSide.ordinal())) > 0) {
                Cuboid6 sideBox = BlockItemPipe.getSideBox(renderSide, thickness);
                BlockRenderer.renderCuboid(renderState, sideBox, 0);
            }
        }

    }

    @Override
    public void registerTextures(AtlasRegistrar map) {
    }

    @Override
    public BakedModel applyTransform(ItemTransforms.TransformType transformType, PoseStack pStack, boolean leftFlip) {
        //return IItemRenderer.super.applyTransform(transformType, pStack, leftFlip);
        PerspectiveModelState modelState = TransformUtils.DEFAULT_BLOCK;
        Transformation transform = modelState.getTransform(transformType);

        Vector3f trans = transform.getTranslation();
        pStack.translate(trans.x(), trans.y(), trans.z());

        pStack.mulPose(transform.getLeftRotation());

        Vector3f scale = transform.getScale();
        pStack.scale(scale.x(), scale.y(), scale.z());

        pStack.mulPose(transform.getRightRotation());

        if (leftFlip) {
            TransformUtils.applyLeftyFlip(pStack);
        }
        return this;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return TextureUtils.getMissingSprite();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    public Pair<TextureAtlasSprite, Integer> getParticleTexture(IPipeTile<?, ?> pipeTile) {
        if (pipeTile == null) {
            return Pair.of(TextureUtils.getMissingSprite(), 0xFFFFFF);
        }
        IPipeType<?> pipeType = pipeTile.getPipeType();
        Material material = pipeTile instanceof TileEntityMaterialPipeBase ? ((TileEntityMaterialPipeBase<?, ?>) pipeTile).getPipeMaterial() : null;
        if (pipeType == null) {
            return Pair.of(TextureUtils.getMissingSprite(), 0xFFFFFF);
        }
        TextureAtlasSprite atlasSprite = getParticleTexture(pipeType, material);
        int pipeColor = getPipeColor(material, pipeTile.getPaintingColor());
        return Pair.of(atlasSprite, pipeColor);
    }

    public abstract TextureAtlasSprite getParticleTexture(IPipeType<?> pipeType, @Nullable Material material);

    public static class PipeRenderContext {

        private final BlockPos pos;
        private final LightMatrix lightMatrix;
        private final List<IVertexOperation[]> openFaceRenderer = new ArrayList<>();
        private final List<IVertexOperation[]> pipeSideRenderer = new ArrayList<>();
        private final IVertexOperation[] blockedOverlay;
        private final float pipeThickness;
        private int color;
        private final int connections;
        private final int blockedConnections;

        public PipeRenderContext(BlockPos pos, LightMatrix lightMatrix, int connections, int blockedConnections, float thickness) {
            this.pos = pos;
            this.lightMatrix = lightMatrix;
            this.connections = connections;
            this.blockedConnections = blockedConnections;
            this.pipeThickness = thickness;
            if (pos != null && lightMatrix != null) {
                blockedOverlay = new IVertexOperation[]{new Translation(pos), lightMatrix, new IconTransformation(Textures.PIPE_BLOCKED_OVERLAY)};
            } else {
                blockedOverlay = new IVertexOperation[]{new IconTransformation(Textures.PIPE_BLOCKED_OVERLAY)};
            }
        }

        public PipeRenderContext(int connections, int blockedConnections, float thickness) {
            this(null, null, connections, blockedConnections, thickness);
        }

        public PipeRenderContext addOpenFaceRender(IVertexOperation... vertexOperations) {
            return addOpenFaceRender(true, vertexOperations);
        }

        public PipeRenderContext addOpenFaceRender(boolean applyDefaultColor, IVertexOperation... vertexOperations) {
            IVertexOperation[] baseVertexOperation = getBaseVertexOperation();
            baseVertexOperation = ArrayUtils.addAll(baseVertexOperation, vertexOperations);
            if (applyDefaultColor) {
                baseVertexOperation = ArrayUtils.addAll(baseVertexOperation, getColorOperation());
            }
            openFaceRenderer.add(baseVertexOperation);
            return this;
        }

        public PipeRenderContext addSideRender(IVertexOperation... vertexOperations) {
            return addSideRender(true, vertexOperations);
        }

        public PipeRenderContext addSideRender(boolean applyDefaultColor, IVertexOperation... vertexOperations) {
            IVertexOperation[] baseVertexOperation = getBaseVertexOperation();
            baseVertexOperation = ArrayUtils.addAll(baseVertexOperation, vertexOperations);
            if (applyDefaultColor) {
                baseVertexOperation = ArrayUtils.addAll(baseVertexOperation, getColorOperation());
            }
            pipeSideRenderer.add(baseVertexOperation);
            return this;
        }

        public ColourMultiplier getColorOperation() {
            return new ColourMultiplier(color);
        }

        private IVertexOperation[] getBaseVertexOperation() {
            if (pos == null) {
                return lightMatrix == null ? new IVertexOperation[0] : new IVertexOperation[]{lightMatrix};
            }
            return lightMatrix == null ? new IVertexOperation[]{new Translation(pos)} : new IVertexOperation[]{new Translation(pos), lightMatrix};
        }

        public int getConnections() {
            return connections;
        }

        public int getBlockedConnections() {
            return blockedConnections;
        }
    }
}
