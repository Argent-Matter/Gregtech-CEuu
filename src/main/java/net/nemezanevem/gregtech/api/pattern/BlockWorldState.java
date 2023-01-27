package net.nemezanevem.gregtech.api.pattern;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Map;

public class BlockWorldState {

    protected Level world;
    public BlockPos pos;
    protected BlockState state;
    protected BlockEntity blockEntity;
    protected boolean BlockEntityInitialized;
    protected PatternMatchContext matchContext;
    protected Map<TraceabilityPredicate.SimplePredicate, Integer> globalCount;
    protected Map<TraceabilityPredicate.SimplePredicate, Integer> layerCount;
    public TraceabilityPredicate predicate;
    protected PatternError error;

    public void update(Level worldIn, BlockPos posIn, PatternMatchContext matchContext, Map<TraceabilityPredicate.SimplePredicate, Integer> globalCount, Map<TraceabilityPredicate.SimplePredicate, Integer> layerCount, TraceabilityPredicate predicate) {
        this.world = worldIn;
        this.pos = posIn;
        this.state = null;
        this.blockEntity = null;
        this.BlockEntityInitialized = false;
        this.matchContext = matchContext;
        this.globalCount = globalCount;
        this.layerCount = layerCount;
        this.predicate = predicate;
        this.error = null;
    }

    public boolean hasError() {
        return error != null;
    }

    public void setError(PatternError error) {
        this.error = error;
        if (error != null) {
            error.setWorldState(this);
        }
    }

    public PatternMatchContext getMatchContext() {
        return matchContext;
    }

    public BlockState getBlockState() {
        if (this.state == null) {
            this.state = this.world.getBlockState(this.pos);
        }

        return this.state;
    }

    @Nullable
    public BlockEntity getBlockEntity() {
        if (this.blockEntity == null && !this.BlockEntityInitialized) {
            this.blockEntity = this.world.getBlockEntity(this.pos);
            this.BlockEntityInitialized = true;
        }

        return this.blockEntity;
    }

    public BlockPos getPos() {
        return this.pos.immutable();
    }

    public BlockState getOffsetState(Direction face) {
        if (pos instanceof BlockPos.MutableBlockPos mutableBlockPos) {
            mutableBlockPos.move(face);
            BlockState blockState = world.getBlockState(pos);
            mutableBlockPos.move(face.getOpposite());
            return blockState;
        }
        return world.getBlockState(this.pos.offset(face.getNormal()));
    }

    public Level getWorld() {
        return world;
    }
}
