package net.nemezanevem.gregtech.api.pattern;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.nemezanevem.gregtech.api.pattern.BlockWorldState;
import net.nemezanevem.gregtech.api.pattern.TraceabilityPredicate;

import java.util.ArrayList;
import java.util.List;

public class PatternError {

    protected BlockWorldState worldState;

    public void setWorldState(BlockWorldState worldState) {
        this.worldState = worldState;
    }

    public Level getWorld() {
        return worldState.getWorld();
    }

    public BlockPos getPos() {
        return worldState.getPos();
    }

    public List<List<ItemStack>> getCandidates() {
        TraceabilityPredicate predicate = worldState.predicate;
        List<List<ItemStack>> candidates = new ArrayList<>();
        for (TraceabilityPredicate.SimplePredicate common : predicate.common) {
            candidates.add(common.getCandidates());
        }
        for (TraceabilityPredicate.SimplePredicate limited : predicate.limited) {
            candidates.add(limited.getCandidates());
        }
        return candidates;
    }

    public Component getErrorInfo() {
        List<List<ItemStack>> candidates = getCandidates();
        StringBuilder builder = new StringBuilder();
        for (List<ItemStack> candidate : candidates) {
            if (!candidate.isEmpty()) {
                builder.append(candidate.get(0).getDisplayName());
                builder.append(", ");
            }
        }
        builder.append("...");
        return Component.translatable("gregtech.multiblock.pattern.error", builder.toString(), worldState.pos);
    }
}
