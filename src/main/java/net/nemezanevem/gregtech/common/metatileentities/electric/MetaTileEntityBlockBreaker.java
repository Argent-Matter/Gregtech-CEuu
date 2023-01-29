package net.nemezanevem.gregtech.common.metatileentities.electric;

import codechicken.lib.raytracer.VoxelShapeBlockHitResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.TieredMetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.widgets.SlotWidget;
import net.nemezanevem.gregtech.api.util.BlockUtility;
import net.nemezanevem.gregtech.api.util.GTTransferUtils;
import net.nemezanevem.gregtech.api.util.GregFakePlayer;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import net.nemezanevem.gregtech.client.util.RenderUtil;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static net.nemezanevem.gregtech.api.capability.GregtechDataCodes.UPDATE_OUTPUT_FACING;

public class MetaTileEntityBlockBreaker extends TieredMetaTileEntity {

    private Direction outputFacing;
    private int breakProgressTicksLeft;
    private float currentBlockHardness;

    public MetaTileEntityBlockBreaker(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityBlockBreaker(metaTileEntityId, getTier());
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.ROCK_BREAKER_OVERLAY.renderOrientedState(renderState, translation, pipeline, getFrontFacing(), false, false);
        Textures.PIPE_OUT_OVERLAY.renderSided(getOutputFacing(), renderState, RenderUtil.adjustTrans(translation, getOutputFacing(), 2), pipeline);
    }

    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isClientSide && getOffsetTimer() % 5 == 0) {
            pushItemsIntoNearbyHandlers(getOutputFacing());
        }
        if (!getWorld().isClientSide) {
            if (breakProgressTicksLeft > 0) {
                --this.breakProgressTicksLeft;
                if (breakProgressTicksLeft == 0 && energyContainer.getEnergyStored() >= getEnergyPerBlockBreak()) {
                    BlockPos blockPos = getPos().offset(getFrontFacing().getNormal());
                    BlockState blockState = getWorld().getBlockState(blockPos);
                    Player entityPlayer = GregFakePlayer.get((ServerLevel) getWorld());
                    float hardness = blockState.getDestroySpeed(getWorld(), blockPos);

                    if (hardness >= 0.0f && getWorld().mayInteract(entityPlayer, blockPos) &&
                            Math.abs(hardness - currentBlockHardness) < 0.5f) {
                        List<ItemStack> drops = attemptBreakBlockAndObtainDrops(blockPos, blockState, entityPlayer);
                        addToInventoryOrDropItems(drops);
                    }
                    this.breakProgressTicksLeft = 0;
                    this.currentBlockHardness = 0.0f;
                    energyContainer.removeEnergy(getEnergyPerBlockBreak());
                }
            }

            if (breakProgressTicksLeft == 0 && isBlockRedstonePowered()) {
                BlockPos blockPos = getPos().offset(getFrontFacing().getNormal());
                BlockState blockState = getWorld().getBlockState(blockPos);
                Player entityPlayer = GregFakePlayer.get((ServerLevel) getWorld());
                float hardness = blockState.getDestroySpeed(getWorld(), blockPos);
                boolean skipBlock = blockState.getMaterial() == Material.AIR ||
                        blockState.isAir();
                if (hardness >= 0.0f && !skipBlock && getWorld().mayInteract(entityPlayer, blockPos)) {
                    this.breakProgressTicksLeft = getTicksPerBlockBreak(hardness);
                    this.currentBlockHardness = hardness;
                }
            }
        }
    }

    private void addToInventoryOrDropItems(List<ItemStack> drops) {
        Direction outputFacing = getOutputFacing();
        double itemSpawnX = getPos().getX() + 0.5 + outputFacing.getStepX();
        double itemSpawnY = getPos().getY() + 0.5 + outputFacing.getStepY();
        double itemSpawnZ = getPos().getZ() + 0.5 + outputFacing.getStepZ();
        for (ItemStack itemStack : drops) {
            ItemStack remainStack = GTTransferUtils.insertItem(exportItems, itemStack, false);
            if (!remainStack.isEmpty()) {
                ItemEntity entityitem = new ItemEntity(getWorld(), itemSpawnX, itemSpawnY, itemSpawnZ, remainStack);
                entityitem.setDefaultPickUpDelay();
                getWorld().addFreshEntity(entityitem);
            }
        }
    }

    private List<ItemStack> attemptBreakBlockAndObtainDrops(BlockPos blockPos, BlockState blockState, Player entityPlayer) {
        BlockEntity tileEntity = getWorld().getBlockEntity(blockPos);
        boolean result = blockState.getBlock().onDestroyedByPlayer(blockState, getWorld(), blockPos, entityPlayer, true, getWorld().getFluidState(blockPos));
        if (result) {
            getWorld().levelEvent(null, 2001, blockPos, Block.getId(blockState));
            blockState.getBlock().destroy(getWorld(), blockPos, blockState);

            BlockUtility.startCaptureDrops();
            blockState.getBlock().playerDestroy(getWorld(), entityPlayer, blockPos, blockState, tileEntity, ItemStack.EMPTY);
            return BlockUtility.stopCaptureDrops();
        }
        return Collections.emptyList();
    }

    @Override
    public boolean onWrenchClick(Player playerIn, InteractionHand hand, Direction facing, VoxelShapeBlockHitResult hitResult) {
        if (!playerIn.isCrouching()) {
            Direction currentOutputSide = getOutputFacing();
            if (currentOutputSide == facing || getFrontFacing() == facing) return false;
            setOutputFacing(facing);
            return true;
        }
        return super.onWrenchClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.putInt("OutputFacing", getOutputFacing().ordinal());
        data.putInt("BlockBreakProgress", breakProgressTicksLeft);
        data.putFloat("BlockHardness", currentBlockHardness);
        return data;
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        this.outputFacing = Direction.values()[data.getInt("OutputFacing")];
        this.breakProgressTicksLeft = data.getInt("BlockBreakProgress");
        this.currentBlockHardness = data.getFloat("BlockHardness");
    }

    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(getOutputFacing().ordinal());
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        super.receiveInitialSyncData(buf);
        this.outputFacing = Direction.values()[buf.readByte()];
    }

    @Override
    public void receiveCustomData(int dataId, FriendlyByteBuf buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_OUTPUT_FACING) {
            this.outputFacing = Direction.values()[buf.readByte()];
            scheduleRenderUpdate();
        }
    }

    @Override
    public boolean isValidFrontFacing(Direction facing) {
        //use direct outputFacing field instead of getter method because otherwise
        //it will just return SOUTH for null output facing
        return super.isValidFrontFacing(facing) && facing != outputFacing;
    }

    public Direction getOutputFacing() {
        return outputFacing == null ? Direction.SOUTH : outputFacing;
    }

    public void setOutputFacing(Direction outputFacing) {
        this.outputFacing = outputFacing;
        if (!getWorld().isClientSide) {
            notifyBlockUpdate();
            writeCustomData(UPDATE_OUTPUT_FACING, buf -> buf.writeByte(outputFacing.ordinal()));
            markDirty();
        }
    }

    @Override
    public void setFrontFacing(Direction frontFacing) {
        super.setFrontFacing(frontFacing);
        if (this.outputFacing == null) {
            //set initial output facing as opposite to front
            setOutputFacing(frontFacing.getOpposite());
        }
    }

    private int getEnergyPerBlockBreak() {
        return (int) GTValues.V[getTier()];
    }

    private int getInventorySize() {
        int sizeRoot = (1 + getTier());
        return sizeRoot * sizeRoot;
    }

    private int getTicksPerBlockBreak(float blockHardness) {
        int ticksPerOneDurability = 5;
        int totalTicksPerBlock = (int) Math.ceil(ticksPerOneDurability * blockHardness);
        float efficiencyMultiplier = 1.0f - getEfficiencyMultiplier();
        return (int) Math.ceil(totalTicksPerBlock * efficiencyMultiplier);
    }

    private float getEfficiencyMultiplier() {
        return 1.0f - Mth.clamp(1.0f - 0.2f * (getTier() - 1.0f), 0.1f, 1.0f);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(getInventorySize());
    }

    @Override
    protected ModularUI createUI(Player entityPlayer) {
        int rowSize = (int) Math.sqrt(getInventorySize());
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 18 + 18 * rowSize + 94)
                .label(10, 5, getMetaFullName());

        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                builder.widget(new SlotWidget(exportItems, index, 89 - rowSize * 9 + x * 18, 18 + y * 18, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT));
            }
        }
        builder.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 18 + 18 * rowSize + 12);
        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level player, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.machine.block_breaker.tooltip"));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.uses_per_op", getEnergyPerBlockBreak()));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.voltage_in", energyContainer.getInputVoltage(), GTValues.VNF[getTier()]));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.item_storage_capacity", getInventorySize()));
        tooltip.add(Component.translatable("gregtech.machine.block_breaker.speed_bonus", (int) (getEfficiencyMultiplier() * 100)));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.requires_redstone"));
        super.addInformation(stack, player, tooltip, advanced);
    }

    @Override
    public boolean needsSneakToRotate() {
        return true;
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable Level world, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(Component.translatable("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }
}
