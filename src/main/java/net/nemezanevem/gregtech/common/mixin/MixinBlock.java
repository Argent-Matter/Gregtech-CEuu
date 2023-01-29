package net.nemezanevem.gregtech.common.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.nemezanevem.gregtech.common.mixinutil.IMixinBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = Block.class)
public class MixinBlock implements IMixinBlock {

    private static boolean captureDrops = false;
    private static final List<ItemStack> capturedDrops = new ArrayList<>();

    @Inject(method = "Lnet/minecraft/world/level/block/Block;popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "HEAD"))
    private static void popResource(Level pLevel, BlockPos pPos, ItemStack pStack, CallbackInfo ci) {
        if (captureDrops) {
            capturedDrops.add(pStack);
            ci.cancel();
        }
    }

    @Override
    public List<ItemStack> captureDrops(boolean start) {
        if (start) {
            captureDrops = true;
            capturedDrops.clear();
            return NonNullList.create();
        } else {
            captureDrops = false;
            return capturedDrops;
        }
    }

}
