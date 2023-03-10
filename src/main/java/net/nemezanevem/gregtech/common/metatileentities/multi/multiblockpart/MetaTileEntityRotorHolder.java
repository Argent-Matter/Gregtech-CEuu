package net.nemezanevem.gregtech.common.metatileentities.multi.multiblockpart;

import codechicken.lib.raytracer.VoxelShapeBlockHitResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IRotorHolder;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.items.behaviors.TurbineRotorBehavior;
import gregtech.common.metatileentities.multi.electric.generator.MetaTileEntityLargeTurbine;
import gregtech.core.advancement.AdvancementTriggers;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.Player;
import net.minecraft.entity.player.PlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Direction;
import net.minecraft.util.InteractionHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityRotorHolder extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IRotorHolder>, IRotorHolder {

    static final int SPEED_INCREMENT = 1;
    static final int SPEED_DECREMENT = 3;

    private final InventoryRotorHolder inventory;

    private final int maxSpeed;
    private int currentSpeed;
    private int rotorColor = -1;
    private boolean isRotorSpinning;
    private boolean frontFaceFree;

    public MetaTileEntityRotorHolder(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.inventory = new InventoryRotorHolder();
        this.maxSpeed = 2000 + 1000 * tier;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityRotorHolder(metaTileEntityId, getTier());
    }

    @Override
    protected ModularUI createUI(@Nonnull Player entityPlayer) {
        return ModularUI.defaultBuilder()
                .label(6, 6, getMetaFullName())
                .slot(inventory, 0, 79, 36, GuiTextures.SLOT, GuiTextures.TURBINE_OVERLAY)
                .bindPlayerInventory(entityPlayer.inventory)
                .build(getHolder(), entityPlayer);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level player, List<Component> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(Component.translatable("gregtech.machine.rotor_holder.tooltip1"));
        tooltip.add(Component.translatable("gregtech.machine.rotor_holder.tooltip2"));
        tooltip.add(Component.translatable("gregtech.universal.disabled"));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable Level world, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(Component.translatable("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public MultiblockAbility<IRotorHolder> getAbility() {
        return MultiblockAbility.ROTOR_HOLDER;
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClientSide) return;

        if (getOffsetTimer() % 20 == 0) {
            boolean isFrontFree = checkTurbineFaceFree();
            if (isFrontFree != this.frontFaceFree) {
                this.frontFaceFree = isFrontFree;
                writeCustomData(GregtechDataCodes.FRONT_FACE_FREE, buf -> buf.writeBoolean(this.frontFaceFree));
            }
        }

        MetaTileEntityLargeTurbine controller = (MetaTileEntityLargeTurbine) getController();

        if (controller != null && controller.isActive()) {
            if (currentSpeed < maxSpeed) {
                setCurrentSpeed(currentSpeed + SPEED_INCREMENT);
            }
            if (getOffsetTimer() % 20 == 0) {
                damageRotor(1 + controller.getNumMaintenanceProblems());
            }
        } else if (!hasRotor()) {
            setCurrentSpeed(0);
        } else if (currentSpeed > 0) {
            setCurrentSpeed(Math.max(0, currentSpeed - SPEED_DECREMENT));
        }
    }

    void setCurrentSpeed(int speed) {
        if (currentSpeed != speed) {
            currentSpeed = speed;
            setRotorSpinning(currentSpeed > 0);
            markDirty();
        }
    }

    void setRotorSpinning(boolean spinning) {
        if (isRotorSpinning != spinning) {
            isRotorSpinning = spinning;
            writeCustomData(GregtechDataCodes.IS_ROTOR_LOOPING, buf -> buf.writeBoolean(isRotorSpinning));
        }
    }

    @Override
    public void registerAbilities(@Nonnull List<IRotorHolder> abilityList) {
        abilityList.add(this);
    }

    @Override
    public boolean canPartShare() {
        return false;
    }

    /**
     * @return true if front face is free and contains only air blocks in 3x3 area
     */
    public boolean isFrontFaceFree() {
        return frontFaceFree;
    }

    private boolean checkTurbineFaceFree() {
        Direction facing = getFrontFacing();
        boolean permuteXZ = facing.getAxis() == Direction.Axis.Z;
        BlockPos centerPos = getPos().offset(facing);
        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                BlockPos blockPos = centerPos.add(permuteXZ ? x : 0, y, permuteXZ ? 0 : x);
                BlockState blockState = getWorld().getBlockState(blockPos);
                if (!blockState.getBlock().isAir(blockState, getWorld(), blockPos)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean onRotorHolderInteract(@Nonnull Player player) {
        if (player.isCreative()) return false;

        if (!getWorld().isClientSide && isRotorSpinning) {
            float damageApplied = Math.min(1, currentSpeed / 1000);
            player.attackEntityFrom(DamageSources.getTurbineDamage(), damageApplied);
            AdvancementTriggers.ROTOR_HOLDER_DEATH.trigger((PlayerMP) player);
            return true;
        }
        return isRotorSpinning;
    }

    /**
     * returns true on both the Client and Server
     *
     * @return whether there is a rotor in the holder
     */
    @Override
    public boolean hasRotor() {
        return rotorColor != -1;
    }

    protected void setRotorColor(int color) {
        this.rotorColor = color;
    }

    protected int getRotorColor() {
        return rotorColor;
    }

    @Override
    public int getRotorSpeed() {
        return this.currentSpeed;
    }

    @Override
    public int getRotorEfficiency() {
        return inventory.getRotorEfficiency();
    }

    @Override
    public int getRotorPower() {
        return inventory.getRotorPower();
    }

    @Override
    public int getRotorDurabilityPercent() {
        return inventory.getRotorDurabilityPercent();
    }

    @Override
    public void damageRotor(int amount) {
        inventory.damageRotor(amount);
    }

    @Override
    public int getMaxRotorHolderSpeed() {
        return this.maxSpeed;
    }

    /**
     * calculates the holder's power multiplier: 2x per tier above the multiblock controller
     *
     * @return the power multiplier provided by the rotor holder
     */
    @Override
    public int getHolderPowerMultiplier() {
        int tierDifference = getTierDifference();
        if (tierDifference == -1) return -1;

        return (int) Math.pow(2, getTierDifference());
    }

    @Override
    public int getHolderEfficiency() {
        int tierDifference = getTierDifference();
        if (tierDifference == -1)
            return -1;

        return 100 + 10 * tierDifference;
    }

    private int getTierDifference() {
        if (getController() instanceof ITieredMetaTileEntity) {
            return getTier() - ((ITieredMetaTileEntity) getController()).getTier();
        }
        return -1;
    }

    @Override
    public boolean onRightClick(Player playerIn, InteractionHand hand, Direction facing, VoxelShapeBlockHitResult hitResult) {
        return onRotorHolderInteract(playerIn) || super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public boolean onWrenchClick(Player playerIn, InteractionHand hand, Direction facing, VoxelShapeBlockHitResult hitResult) {
        return onRotorHolderInteract(playerIn) || super.onWrenchClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public boolean onScrewdriverClick(Player playerIn, InteractionHand hand, Direction facing, VoxelShapeBlockHitResult hitResult) {
        return onRotorHolderInteract(playerIn);
    }

    @Override
    public void onLeftClick(Player player, Direction facing, VoxelShapeBlockHitResult hitResult) {
        onRotorHolderInteract(player);
    }

    @Override
    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
        clearInventory(itemBuffer, inventory);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.put("inventory", inventory.serializeNBT());
        data.putInt("currentSpeed", currentSpeed);
        data.putBoolean("Spinning", isRotorSpinning);
        data.putBoolean("FrontFree", frontFaceFree);
        return data;
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        this.inventory.deserializeNBT(data.getCompound("inventory"));
        this.currentSpeed = data.getInt("currentSpeed");
        this.isRotorSpinning = data.getBoolean("Spinning");
        this.frontFaceFree = data.getBoolean("FrontFree");
    }

    @Override
    public void receiveCustomData(int dataId, FriendlyByteBuf buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.IS_ROTOR_LOOPING) {
            this.isRotorSpinning = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.FRONT_FACE_FREE) {
            this.frontFaceFree = buf.readBoolean();
        }
    }

    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isRotorSpinning);
        buf.writeInt(rotorColor);
        buf.writeBoolean(frontFaceFree);
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        super.receiveInitialSyncData(buf);
        this.isRotorSpinning = buf.readBoolean();
        this.rotorColor = buf.readInt();
        this.frontFaceFree = buf.readBoolean();
        scheduleRenderUpdate();
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.ROTOR_HOLDER_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        Textures.LARGE_TURBINE_ROTOR_RENDERER.renderSided(renderState, translation, pipeline, getFrontFacing(),
                getController() != null, hasRotor(), isRotorSpinning, getRotorColor());
    }

    private class InventoryRotorHolder extends ItemStackHandler {

        public InventoryRotorHolder() {
            super(1);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onLoad() {
            rotorColor = getRotorColor();
        }

        @Override
        protected void onContentsChanged(int slot) {
            setRotorColor(getRotorColor());
            scheduleRenderUpdate();
        }

        @Nullable
        private ItemStack getTurbineStack() {
            if (!hasRotor())
                return null;
            return getStackInSlot(0);
        }

        @Nullable
        private TurbineRotorBehavior getTurbineBehavior() {
            ItemStack stack = getStackInSlot(0);
            if (stack.isEmpty()) return null;

            return TurbineRotorBehavior.getInstanceFor(stack);
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        private boolean hasRotor() {
            return getTurbineBehavior() != null;
        }

        private int getRotorColor() {
            if (!hasRotor()) return -1;
            //noinspection ConstantConditions
            return getTurbineBehavior().getPartMaterial(getStackInSlot(0)).getMaterialRGB();

        }

        private int getRotorDurabilityPercent() {
            if (!hasRotor()) return 0;

            //noinspection ConstantConditions
            return getTurbineBehavior().getRotorDurabilityPercent(getStackInSlot(0));
        }

        private int getRotorEfficiency() {
            if (!hasRotor()) return -1;

            //noinspection ConstantConditions
            return getTurbineBehavior().getRotorEfficiency(getTurbineStack());
        }

        private int getRotorPower() {
            if (!hasRotor()) return -1;

            //noinspection ConstantConditions
            return getTurbineBehavior().getRotorPower(getTurbineStack());
        }

        private void damageRotor(int damageAmount) {
            if (!hasRotor()) return;
            //noinspection ConstantConditions
            getTurbineBehavior().applyRotorDamage(getStackInSlot(0), damageAmount);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return TurbineRotorBehavior.getInstanceFor(stack) != null && super.isItemValid(slot, stack);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack itemStack = super.extractItem(slot, amount, simulate);
            if (!simulate && itemStack != ItemStack.EMPTY) setRotorColor(-1);
            return itemStack;
        }
    }
}
