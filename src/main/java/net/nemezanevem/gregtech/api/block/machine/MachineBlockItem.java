package net.nemezanevem.gregtech.api.block.machine;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.pipenet.block.BlockPipe;
import net.nemezanevem.gregtech.api.pipenet.tile.IPipeTile;
import net.nemezanevem.gregtech.api.blockentity.ITieredMetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.common.ConfigHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MachineBlockItem extends BlockItem {

    public MachineBlockItem(BlockMachine block) {
        super(block, new Item.Properties().tab(GregTech.TAB_GREGTECH));
    }

    @Nonnull
    @Override
    public String getDescriptionId(@Nonnull ItemStack stack) {
        MetaTileEntity metaTileEntity = Util.getMetaTileEntity(stack);
        return metaTileEntity == null ? "unnamed" : metaTileEntity.getMetaName();
    }

    @Override
    public InteractionResult place(BlockPlaceContext pContext) {
        MetaTileEntity metaTileEntity = Util.getMetaTileEntity(pContext.getItemInHand());
        //prevent rendering glitch before meta tile entity sync to client, but after block placement
        //set opaque property on the placing on block, instead during set of meta tile entity
        InteractionResult superVal = super.placeBlock(new BlockPlaceContext(pContext.getPlayer(), pContext.getHand(), pContext.getItemInHand(), new BlockHitResult(pContext.getClickLocation(), pContext.getClickedFace(), pContext.getClickedPos(), false)), pContext.get.withProperty(BlockMachine.OPAQUE, metaTileEntity != null && metaTileEntity.isOpaqueCube()));
        Level world = pContext.getLevel();
        if (superVal == InteractionResult.PASS && !world.isClientSide) {
            Direction face = pContext.getClickedFace();
            BlockPos possiblePipe = pContext.getClickedPos().offset(face.getOpposite().getNormal());
            Block block = world.getBlockState(possiblePipe).getBlock();
            if (block instanceof BlockPipe) {
                IPipeTile pipeTile = ((BlockPipe<?, ?, ?>) block).getPipeTileEntity(world, possiblePipe);
                if (pipeTile != null && ((BlockPipe<?, ?, ?>) block).canPipeConnectToBlock(pipeTile, face.getOpposite(), world.getBlockEntity(pContext.getClickedPos()))) {
                    pipeTile.setConnection(pContext.getClickedFace(), true, false);
                }
            }
        }
        return superVal;
    }

    @Nullable
    @Override
    public String getCreatorModId(ItemStack itemStack) {
        MetaTileEntity metaTileEntity = Util.getMetaTileEntity(itemStack);
        if (metaTileEntity == null) {
            return GregTech.MODID;
        }
        ResourceLocation metaTileEntityId = metaTileEntity.metaTileEntityId;
        return metaTileEntityId.getNamespace();
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        MetaTileEntity metaTileEntity = Util.getMetaTileEntity(stack);
        return metaTileEntity == null ? null : metaTileEntity.initItemStackCapabilities(stack);
    }

    public boolean hasContainerItem(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null).isPresent();
    }

    @Nonnull
    public ItemStack getContainerItem(ItemStack itemStack) {
        if (!hasContainerItem(itemStack)) {
            return ItemStack.EMPTY;
        }
        if (itemStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null).isPresent()) {
            LazyOptional<IFluidHandlerItem> handler = itemStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null);
            if (handler.isPresent()) {
                IFluidHandlerItem real = handler.orElse(null);
                FluidStack drained = real.drain(1000, IFluidHandler.FluidAction.SIMULATE);
                if (drained.getAmount() != 1000) {
                    return ItemStack.EMPTY;
                }
                return real.getContainer().copy();
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable Level worldIn, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn) {
        MetaTileEntity metaTileEntity = Util.getMetaTileEntity(stack);
        if (metaTileEntity == null) return;

        //item specific tooltip like: gregtech.machine.lathe.lv.tooltip
        Component tooltipLocale = Component.translatable(metaTileEntity.getMetaName() + ".tooltip");
        if (ComponentUtils.isTranslationResolvable(tooltipLocale)) {
            tooltip.add(tooltipLocale);
        }

        //tier less tooltip for a electric machine like: gregtech.machine.lathe.tooltip
        if (metaTileEntity instanceof ITieredMetaTileEntity) {
            String tierlessTooltipLocale = ((ITieredMetaTileEntity) metaTileEntity).getTierlessTooltipKey();
            //only add tierless tooltip if it's key is not equal to normal tooltip key (i.e if machine name has dot in it's name)
            //case when it's not true would be any machine extending from TieredMetaTileEntity but having only one tier
            if (!tooltipLocale.getString().equals(tierlessTooltipLocale) && I18n.exists(tierlessTooltipLocale)) {
                tooltip.add(Component.translatable(tierlessTooltipLocale));
            }
        }

        // additional tooltips that the MTE provides
        metaTileEntity.addInformation(stack, worldIn, tooltip, flagIn.isAdvanced());

        // tool usages tooltips
        if (metaTileEntity.showToolUsages()) {
            if (Screen.hasShiftDown()) {
                metaTileEntity.addToolUsages(stack, worldIn, tooltip, flagIn.isAdvanced());
            } else {
                tooltip.add(Component.translatable("gregtech.tool_action.show_tooltips"));
            }
        }

        if (ConfigHolder.misc.debug) {
            tooltip.add(Component.literal(String.format("MetaTileEntity Id: %s", metaTileEntity.metaTileEntityId.toString())));
        }
    }
}
