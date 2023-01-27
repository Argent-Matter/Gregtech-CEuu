package net.nemezanevem.gregtech.api.cover;

import codechicken.lib.raytracer.VoxelShapeBlockHitResult;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.raytracer.IndexedVoxelShape;
import codechicken.lib.raytracer.RayTracer;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.*;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.BlockPipe.PipeConnectionData;
import gregtech.api.util.Util;
import gregtech.client.utils.RenderUtil;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.entity.player.Player;
import net.minecraft.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.nemezanevem.gregtech.api.pipenet.block.BlockPipe;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.client.util.RenderUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public interface ICoverable {

    Transformation REVERSE_HORIZONTAL_ROTATION = new Rotation(Math.PI, new Vector3(0.0, 1.0, 0.0)).at(Vector3.CENTER);
    Transformation REVERSE_VERTICAL_ROTATION = new Rotation(Math.PI, new Vector3(1.0, 0.0, 0.0)).at(Vector3.CENTER);

    Level getWorld();

    BlockPos getPos();

    long getOffsetTimer();

    void markDirty();

    boolean isValid();

    <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side);

    boolean placeCoverOnSide(Direction side, ItemStack itemStack, CoverDefinition definition, Player player);

    boolean removeCover(Direction side);

    boolean canPlaceCoverOnSide(Direction side);

    CoverBehavior getCoverAtSide(Direction side);

    void writeCoverData(CoverBehavior behavior, int id, Consumer<FriendlyByteBuf> writer);

    int getInputRedstoneSignal(Direction side, boolean ignoreCover);

    ItemStack getStackForm();

    double getCoverPlateThickness();

    int getPaintingColorForRendering();

    boolean shouldRenderBackSide();

    void notifyBlockUpdate();

    void scheduleRenderUpdate();

    default boolean hasAnyCover() {
        for(Direction facing : Direction.values())
            if(getCoverAtSide(facing) != null)
                return true;
        return false;
    }

    default void renderCovers(CCRenderState renderState, Matrix4 translation, RenderType layer) {
        renderState.lightMatrix.locate(getWorld(), getPos());
        double coverPlateThickness = getCoverPlateThickness();
        IVertexOperation[] platePipeline = new IVertexOperation[]{renderState.lightMatrix, new ColourMultiplier(Util.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()))};
        IVertexOperation[] coverPipeline = new IVertexOperation[]{renderState.lightMatrix};

        for (Direction sideFacing : Direction.values()) {
            CoverBehavior coverBehavior = getCoverAtSide(sideFacing);
            if (coverBehavior == null) continue;
            Cuboid6 plateBox = getCoverPlateBox(sideFacing, coverPlateThickness);

            if (coverBehavior.canRenderInLayer(layer) && coverPlateThickness > 0) {
                renderState.preRenderWorld(getWorld(), getPos());
                coverBehavior.renderCoverPlate(renderState, translation, platePipeline, plateBox, layer);
            }

            if (coverBehavior.canRenderInLayer(layer)) {
                coverBehavior.renderCover(renderState, RenderUtil.adjustTrans(translation, sideFacing, 2), coverPipeline, plateBox, layer);
                if (coverPlateThickness == 0.0 && shouldRenderBackSide() && coverBehavior.canRenderBackside()) {
                    //machine is full block, but still not opaque - render cover on the back side too
                    Matrix4 backTranslation = translation.copy();
                    if (sideFacing.getAxis().isVertical()) {
                        REVERSE_VERTICAL_ROTATION.apply(backTranslation);
                    } else {
                        REVERSE_HORIZONTAL_ROTATION.apply(backTranslation);
                    }
                    backTranslation.translate(-sideFacing.getStepX(), -sideFacing.getStepY(), -sideFacing.getStepZ());
                    coverBehavior.renderCover(renderState, backTranslation, coverPipeline, plateBox, layer); // may need to translate the layer here as well
                }
            }
        }
    }

    default void addCoverCollisionBoundingBox(List<? super IndexedVoxelShape> collisionList) {
        double plateThickness = getCoverPlateThickness();
        if (plateThickness > 0.0) {
            for (Direction side : Direction.values()) {
                if (getCoverAtSide(side) != null) {
                    VoxelShape coverBox = getCoverPlateBox(side, plateThickness);
                    CoverSideData coverSideData = new CoverSideData(side);
                    collisionList.add(new IndexedVoxelShape(coverBox, coverSideData));
                }
            }
        }
    }

    static boolean doesCoverCollide(Direction side, List<IndexedVoxelShape> collisionBox, double plateThickness) {
        if (side == null) {
            return false;
        }
        if (plateThickness > 0.0) {
            VoxelShape coverPlateBox = getCoverPlateBox(side, plateThickness);
            for (VoxelShape collisionCuboid : collisionBox) {
                AtomicBoolean returnValue = new AtomicBoolean(false);
                collisionCuboid.forAllBoxes((double pMinX, double pMinY, double pMinZ, double pMaxX, double pMaxY, double pMaxZ) -> {
                    AABB aabb = new AABB(pMinX, pMinY, pMinZ, pMaxX, pMaxY, pMaxZ);
                    //collision box intersects with machine bounding box -
                    //cover cannot be placed on this side
                    if(aabb.intersects(coverPlateBox.bounds())) {
                        returnValue.set(true);
                    }
                });
                if(returnValue.get()) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    static Direction rayTraceCoverableSide(ICoverable coverable, Player player) {
        BlockHitResult result = RayTracer.retraceBlock(coverable.getWorld(), player, coverable.getPos());
        if (result == null || result.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        return traceCoverSide(result);
    }

    class PrimaryBoxData {
        public final boolean usePlacementGrid;

        public PrimaryBoxData(boolean usePlacementGrid) {
            this.usePlacementGrid = usePlacementGrid;
        }
    }

    class CoverSideData {
        public final Direction side;

        public CoverSideData(Direction side) {
            this.side = side;
        }
    }

    static Direction traceCoverSide(BlockHitResult result) {
        if (result instanceof VoxelShapeBlockHitResult) {
            VoxelShapeBlockHitResult rayTraceResult = (VoxelShapeBlockHitResult) result;
            if (rayTraceResult.shape.getData() == null) {
                return determineGridSideHit(result);
            } else if (rayTraceResult.shape.getData() instanceof CoverSideData) {
                return ((CoverSideData) rayTraceResult.shape.getData()).side;
            } else if (rayTraceResult.shape.getData() instanceof BlockPipe.PipeConnectionData) {
                return ((BlockPipe.PipeConnectionData) rayTraceResult.shape.getData()).side;
            } else if (rayTraceResult.shape.getData() instanceof PrimaryBoxData) {
                PrimaryBoxData primaryBoxData = (PrimaryBoxData) rayTraceResult.shape.getData();
                return primaryBoxData.usePlacementGrid ? determineGridSideHit(result) : result.getDirection();
            } //unknown hit type, fall through
        }
        //normal collision ray trace, return side hit
        return determineGridSideHit(result);
    }

    static Direction determineGridSideHit(BlockHitResult result) {
        return Util.determineWrenchingSide(result.getDirection(),
                (float) (result.getLocation().x - result.getBlockPos().getX()),
                (float) (result.getLocation().y - result.getBlockPos().getY()),
                (float) (result.getLocation().z - result.getBlockPos().getZ()));
    }

    static VoxelShape getCoverPlateBox(Direction side, double plateThickness) {
        switch (side) {
            case UP:
                return Shapes.box(0.0, 1.0 - plateThickness, 0.0, 1.0, 1.0, 1.0);
            case DOWN:
                return Shapes.box(0.0, 0.0, 0.0, 1.0, plateThickness, 1.0);
            case NORTH:
                return Shapes.box(0.0, 0.0, 0.0, 1.0, 1.0, plateThickness);
            case SOUTH:
                return Shapes.box(0.0, 0.0, 1.0 - plateThickness, 1.0, 1.0, 1.0);
            case WEST:
                return Shapes.box(0.0, 0.0, 0.0, plateThickness, 1.0, 1.0);
            case EAST:
                return Shapes.box(1.0 - plateThickness, 0.0, 0.0, 1.0, 1.0, 1.0);
            default:
                throw new UnsupportedOperationException("Cannot get cover plate box at side " + side);
        }
    }

    static boolean canPlaceCover(CoverDefinition coverDef, ICoverable coverable) {
        for (Direction facing : Direction.values()) {
            CoverBehavior cover = coverDef.createCoverBehavior(coverable, facing);
            if (coverable.canPlaceCoverOnSide(facing) && cover.canAttach())
                return true;
        }
        return false;
    }

    default boolean canRenderMachineGrid() {
        return true;
    }
}
