package net.nemezanevem.gregtech.api.capability.impl;

import gregtech.api.GTValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.nemezanevem.gregtech.api.capability.GregtechDataCodes;
import net.nemezanevem.gregtech.api.capability.IVentable;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeType;
import gregtech.api.util.Util;
import gregtech.common.ConfigHolder;
import gregtech.core.advancement.AdvancementTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.PlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AABB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.IFluidTank;
import net.nemezanevem.gregtech.api.recipe.GTRecipe;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.common.ConfigHolder;

import javax.annotation.Nonnull;

public class RecipeLogicSteam extends AbstractRecipeLogic implements IVentable {

    private final IFluidTank steamFluidTank;
    private final boolean isHighPressure;
    private final double conversionRate; //energy units per millibucket

    private boolean needsVenting;
    private boolean ventingStuck;
    private Direction ventingSide;

    public RecipeLogicSteam(MetaTileEntity tileEntity, RecipeType<?> recipeMap, boolean isHighPressure, IFluidTank steamFluidTank, double conversionRate) {
        super(tileEntity, recipeMap);
        this.steamFluidTank = steamFluidTank;
        this.conversionRate = conversionRate;
        this.isHighPressure = isHighPressure;
    }

    @Override
    public boolean isVentingStuck() {
        return needsVenting && ventingStuck;
    }

    @Override
    public boolean isNeedsVenting() {
        return needsVenting;
    }

    @Override
    public void onFrontFacingSet(Direction newFrontFacing) {
        if (ventingSide == null) {
            setVentingSide(newFrontFacing.getOpposite());
        }
    }

    public Direction getVentingSide() {
        return ventingSide == null ? Direction.SOUTH : ventingSide;
    }

    public void setVentingStuck(boolean ventingStuck) {
        this.ventingStuck = ventingStuck;
        if (!metaTileEntity.getWorld().isClientSide) {
            metaTileEntity.markDirty();
            writeCustomData(GregtechDataCodes.VENTING_STUCK, buf -> buf.writeBoolean(ventingStuck));
        }
    }

    @Override
    public void setNeedsVenting(boolean needsVenting) {
        this.needsVenting = needsVenting;
        if (!needsVenting && ventingStuck)
            setVentingStuck(false);
        if (!metaTileEntity.getWorld().isClientSide) {
            metaTileEntity.markDirty();
            writeCustomData(GregtechDataCodes.NEEDS_VENTING, buf -> buf.writeBoolean(needsVenting));
        }
    }

    public void setVentingSide(Direction ventingSide) {
        this.ventingSide = ventingSide;
        if (!metaTileEntity.getWorld().isClientSide) {
            metaTileEntity.markDirty();
            writeCustomData(GregtechDataCodes.VENTING_SIDE, buf -> buf.writeByte(ventingSide.ordinal()));
        }
    }

    @Override
    public void receiveCustomData(int dataId, FriendlyByteBuf buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.NEEDS_VENTING) {
            this.needsVenting = buf.readBoolean();
        } else if (dataId == GregtechDataCodes.VENTING_SIDE) {
            this.ventingSide = Direction.values()[buf.readByte()];
            getMetaTileEntity().scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.VENTING_STUCK) {
            this.ventingStuck = buf.readBoolean();
        }
    }

    @Override
    public void writeInitialData(@Nonnull FriendlyByteBuf buf) {
        super.writeInitialData(buf);
        buf.writeByte(getVentingSide().ordinal());
        buf.writeBoolean(needsVenting);
        buf.writeBoolean(ventingStuck);
    }

    @Override
    public void receiveInitialData(@Nonnull FriendlyByteBuf buf) {
        super.receiveInitialData(buf);
        this.ventingSide = Direction.values()[buf.readByte()];
        this.needsVenting = buf.readBoolean();
        this.ventingStuck = buf.readBoolean();
    }

    @Override
    public void tryDoVenting() {
        BlockPos machinePos = metaTileEntity.getPos();
        Direction ventingSide = getVentingSide();
        BlockPos ventingBlockPos = machinePos.offset(ventingSide.getNormal());
        BlockState blockOnPos = metaTileEntity.getWorld().getBlockState(ventingBlockPos);
        if (blockOnPos.getCollisionShape(metaTileEntity.getWorld(), ventingBlockPos) == Shapes.empty()) {
            performVentingAnimation(ventingBlockPos, machinePos);
        } else if (Util.tryBreakSnowLayer(metaTileEntity.getWorld(), ventingBlockPos, blockOnPos, false)) {
            performVentingAnimation(ventingBlockPos, machinePos);
        } else if (!ventingStuck) {
            setVentingStuck(true);
        }
    }

    private void performVentingAnimation(BlockPos ventingBlockPos, BlockPos machinePos) {
        metaTileEntity.getWorld()
                .getEntities(EntityTypeTest.forClass(LivingEntity.class), new AABB(ventingBlockPos), EntitySelector.LIVING_ENTITY_STILL_ALIVE)
                .forEach(entity -> {
                    entity.hurt(DamageSources.getHeatDamage(), this.isHighPressure ? 12.0f : 6.0f);
                    if (entity instanceof ServerPlayer serverPlayer) {
                        AdvancementTriggers.STEAM_VENT_DEATH.trigger(serverPlayer);
                    }
                });
        ServerLevel world = (ServerLevel) metaTileEntity.getWorld();
        double posX = machinePos.getX() + 0.5 + ventingSide.getStepX() * 0.6;
        double posY = machinePos.getY() + 0.5 + ventingSide.getStepY() * 0.6;
        double posZ = machinePos.getZ() + 0.5 + ventingSide.getStepZ() * 0.6;

        world.sendParticles(ParticleTypes.CLOUD, posX, posY, posZ,
                7 + world.random.nextInt(3),
                ventingSide.getStepX() / 2.0,
                ventingSide.getStepY() / 2.0,
                ventingSide.getStepZ() / 2.0, 0.1);
        if (ConfigHolder.machines.machineSounds && !metaTileEntity.isMuffled()){
            world.playSound(null, posX, posY, posZ, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        setNeedsVenting(false);

    }

    @Override
    public void tick() {
        if (getMetaTileEntity().getWorld().isClientSide)
            return;
        if (this.needsVenting && metaTileEntity.getOffsetTimer() % 10 == 0) {
            tryDoVenting();
        }
        super.tick();
    }

    @Override
    protected boolean checkRecipe(@Nonnull GTRecipe<?> recipe) {
        return super.checkRecipe(recipe) && !this.needsVenting;
    }

    @Override
    protected void completeRecipe() {
        super.completeRecipe();
        setNeedsVenting(true);
        tryDoVenting();
    }

    @Override
    protected int[] calculateOverclock(@Nonnull Recipe recipe) {

        //EUt, Duration
        int[] result = new int[2];

        result[0] = isHighPressure ? recipe.getEUt() * 2 : recipe.getEUt();
        result[1] = isHighPressure ? recipe.getDuration() : recipe.getDuration() * 2;

        return result;
    }

    @Override
    protected long getEnergyInputPerSecond() {
        return 0;
    }

    @Override
    protected long getEnergyStored() {
        return (long) Math.ceil(steamFluidTank.getFluidAmount() * conversionRate);
    }

    @Override
    protected long getEnergyCapacity() {
        return (long) Math.floor(steamFluidTank.getCapacity() * conversionRate);
    }

    @Override
    protected boolean drawEnergy(int recipeEUt, boolean simulate) {
        int resultDraw = (int) Math.ceil(recipeEUt / conversionRate);
        return resultDraw >= 0 && steamFluidTank.getFluidAmount() >= resultDraw &&
                steamFluidTank.drain(resultDraw, !simulate) != null;
    }

    @Override
    protected long getMaxVoltage() {
        return GTValues.V[GTValues.LV];
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compound = super.serializeNBT();
        compound.setInteger("VentingSide", getVentingSide().getIndex());
        compound.setBoolean("NeedsVenting", needsVenting);
        compound.setBoolean("VentingStuck", ventingStuck);
        return compound;
    }

    @Override
    public void deserializeNBT(@Nonnull CompoundTag compound) {
        super.deserializeNBT(compound);
        this.ventingSide = Direction.values()[compound.getInteger("VentingSide")];
        this.needsVenting = compound.getBoolean("NeedsVenting");
        this.ventingStuck = compound.getBoolean("VentingStuck");
    }
}
