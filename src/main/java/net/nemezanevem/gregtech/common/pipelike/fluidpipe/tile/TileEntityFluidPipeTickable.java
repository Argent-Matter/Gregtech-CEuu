package net.nemezanevem.gregtech.common.pipelike.fluidpipe.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.cover.CoverBehavior;
import net.nemezanevem.gregtech.api.blockentity.IDataInfoProvider;
import net.nemezanevem.gregtech.common.pipelike.fluidpipe.net.PipeTankList;
import org.apache.commons.lang3.tuple.MutableTriple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class TileEntityFluidPipeTickable extends TileEntityFluidPipe implements BlockEntityTicker<TileEntityFluidPipeTickable>, IDataInfoProvider {

    public byte lastReceivedFrom = 0, oldLastReceivedFrom = 0;
    private PipeTankList pipeTankList;
    private final EnumMap<Direction, PipeTankList> tankLists = new EnumMap<>(Direction.class);
    private FluidTank[] fluidTanks;
    private long timer = 0L;
    private final int offset = GTValues.RNG.nextInt(20);

    public long getOffsetTimer() {
        return timer + offset;
    }

    @Nullable
    @Override
    public <T> LazyOptional<T> getCapabilityInternal(Capability<T> capability, @Nullable Direction facing) {
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            PipeTankList tankList = getTankList(facing);
            if (tankList == null)
                return null;
            return ForgeCapabilities.FLUID_HANDLER.orEmpty(capability, tanksLazy);
        }
        return super.getCapabilityInternal(capability, facing);
    }

    @Override
    public void tick(Level pLevel, BlockPos pPos, BlockState pState, TileEntityFluidPipeTickable pBlockEntity) {
        timer++;
        getCoverableImplementation().tick();
        if (!level.isClientSide && getOffsetTimer() % FREQUENCY == 0) {
            lastReceivedFrom &= 63;
            if (lastReceivedFrom == 63) {
                lastReceivedFrom = 0;
            }

            boolean shouldDistribute = (oldLastReceivedFrom == lastReceivedFrom);
            int tanks = getNodeData().getTanks();
            for (int i = 0, j = GTValues.RNG.nextInt(tanks); i < tanks; i++) {
                int index = (i + j) % tanks;
                FluidTank tank = getFluidTanks()[index];
                FluidStack fluid = tank.getFluid();
                if (fluid.isEmpty())
                    continue;
                if (fluid.getAmount() <= 0) {
                    tank.setFluid(FluidStack.EMPTY);
                    continue;
                }

                if (shouldDistribute) {
                    distributeFluid(index, tank, fluid);
                    lastReceivedFrom = 0;
                }
            }
            oldLastReceivedFrom = lastReceivedFrom;
        }
    }

    @Override
    public boolean supportsTicking() {
        return true;
    }

    private void distributeFluid(int channel, FluidTank tank, FluidStack fluid) {
        // Tank, From, Amount to receive
        List<MutableTriple<IFluidHandler, IFluidHandler, Integer>> tanks = new ArrayList<>();
        int amount = fluid.getAmount();

        FluidStack maxFluid = fluid.copy();
        double availableCapacity = 0;

        for (byte side, i = 0, j = (byte) GTValues.RNG.nextInt(6); i < 6; i++) {
            // Get a list of tanks accepting fluids, and what side they're on
            side = (byte) ((i + j) % 6);
            Direction facing = Direction.values()[side];
            if (!isConnected(facing) || (lastReceivedFrom & (1 << side)) != 0)
                continue;
            Direction oppositeSide = facing.getOpposite();

            IFluidHandler fluidHandler = getFluidHandlerAt(facing, oppositeSide);
            if (fluidHandler == null)
                continue;

            LazyOptional<IFluidHandler> pipeTank = LazyOptional.of(() -> tank);
            CoverBehavior cover = getCoverableImplementation().getCoverAtSide(facing);
            if (cover != null) {
                LazyOptional<IFluidHandler> capability = cover.getCapability(ForgeCapabilities.FLUID_HANDLER, pipeTank);
                // Shutter covers return null capability when active, so check here to prevent NPE
                if (capability == null) {
                    continue;
                }
                pipeTank = capability;

            }

            IFluidHandler pipeTankReal = pipeTank.resolve().get();
            FluidStack drainable = pipeTankReal.drain(maxFluid, IFluidHandler.FluidAction.SIMULATE);
            if (drainable.isEmpty() || drainable.getAmount() <= 0) {
                continue;
            }

            int filled = Math.min(fluidHandler.fill(maxFluid, IFluidHandler.FluidAction.SIMULATE), drainable.getAmount());

            if (filled > 0) {
                tanks.add(MutableTriple.of(fluidHandler, pipeTankReal, filled));
                availableCapacity += filled;
            }
            maxFluid.setAmount(amount); // Because some mods do actually modify input fluid stack
        }

        if (availableCapacity <= 0)
            return;

        // How much of this fluid is available for distribution?
        final double maxAmount = Math.min(getCapacityPerTank() / 2, fluid.getAmount());

        // Now distribute
        for (MutableTriple<IFluidHandler, IFluidHandler, Integer> triple : tanks) {
            if (availableCapacity > maxAmount) {
                triple.setRight((int) Math.floor(triple.getRight() * maxAmount / availableCapacity)); // Distribute fluids based on percentage available space at destination
            }
            if (triple.getRight() == 0) {
                if (tank.getFluidAmount() <= 0) break; // If there is no more stored fluid, stop transferring to prevent dupes
                triple.setRight(1); // If the percent is not enough to give at least 1L, try to give 1L
            } else if (triple.getRight() < 0) {
                continue;
            }

            FluidStack toInsert = fluid.copy();
            toInsert.setAmount(triple.getRight());

            int inserted = triple.getLeft().fill(toInsert, IFluidHandler.FluidAction.EXECUTE);
            if (inserted > 0) {
                triple.getMiddle().drain(inserted, IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }

    public void checkAndDestroy(@Nonnull FluidStack stack) {
        Fluid fluid = stack.getFluid();
        FluidType type = fluid.getFluidType();
        boolean burning = getNodeData().getMaxFluidTemperature() < type.getTemperature(stack);
        boolean leaking = !getNodeData().isGasProof() && type.isLighterThanAir();
        boolean shattering = !getNodeData().isCryoProof() && type.getTemperature() < 120; // fluids less than 120K are cryogenic
        boolean corroding = false;
        boolean melting = false;
        if (fluid instanceof MaterialFluid) {
            MaterialFluid materialFluid = (MaterialFluid) fluid;
            corroding = !getNodeData().isAcidProof() && materialFluid.getFluidType().equals(FluidTypes.ACID);
            melting = !getNodeData().isPlasmaProof() && materialFluid.getFluidType().equals(FluidTypes.PLASMA);

            // carrying plasmas which are too hot when plasma proof does not burn pipes
            if (burning && getNodeData().isPlasmaProof() && materialFluid.getFluidType().equals(FluidTypes.PLASMA))
                burning = false;
        }

        if (burning || leaking || corroding || shattering || melting) {
            destroyPipe(stack, burning, leaking, corroding, shattering, melting);
        }
    }

    public void destroyPipe(FluidStack stack, boolean isBurning, boolean isLeaking, boolean isCorroding, boolean isShattering, boolean isMelting) {
        // prevent the sound from spamming when filled from anything not a pipe
        if (getOffsetTimer() % 10 == 0) {
            world.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }

        if (isLeaking) {
            TileEntityFluidPipe.spawnParticles(world, pos, Direction.UP, EnumParticleTypes.SMOKE_NORMAL, 7 + GTValues.RNG.nextInt(2));

            // voids 10%
            stack.amount = Math.max(0, stack.amount * 9 / 10);

            // apply heat damage in area surrounding the pipe
            if (getOffsetTimer() % 20 == 0) {
                List<LivingEntity> entities = getPipeWorld().getEntitiesWithinAABB(LivingEntity.class, new AABB(getPipePos()).grow(2));
                for (LivingEntity entityLivingBase : entities) {
                    EntityDamageUtil.applyTemperatureDamage(entityLivingBase, stack.getFluid().getTemperature(stack), 2.0F, 10);
                }
            }

            // chance to do a small explosion
            if (GTValues.RNG.nextInt(isBurning ? 3 : 7) == 0) {
                this.doExplosion(1.0f + GTValues.RNG.nextFloat());
            }
        }

        if (isCorroding) {
            TileEntityFluidPipe.spawnParticles(world, pos, Direction.UP, EnumParticleTypes.CRIT_MAGIC, 3 + GTValues.RNG.nextInt(2));

            // voids 25%
            stack.amount = Math.max(0, stack.amount * 3 / 4);

            // apply chemical damage in area surrounding the pipe
            if (getOffsetTimer() % 20 == 0) {
                List<LivingEntity> entities = getPipeWorld().getEntitiesWithinAABB(LivingEntity.class, new AABB(getPipePos()).grow(1));
                for (LivingEntity entityLivingBase : entities) {
                    EntityDamageUtil.applyChemicalDamage(entityLivingBase, 2);
                }
            }

            // 1/10 chance to void everything and destroy the pipe
            if (GTValues.RNG.nextInt(10) == 0) {
                stack.amount = 0;
                world.setBlockToAir(pos);
            }
        }

        if (isBurning || isMelting) {
            TileEntityFluidPipe.spawnParticles(world, pos, Direction.UP, EnumParticleTypes.FLAME, (isMelting ? 7 : 3) + GTValues.RNG.nextInt(2));

            // voids 75%
            stack.amount = Math.max(0, stack.amount / 4);

            // 1/4 chance to burn everything around it
            if (GTValues.RNG.nextInt(4) == 0) {
                TileEntityFluidPipe.setNeighboursToFire(world, pos);
            }

            // apply heat damage in area surrounding the pipe
            if (isMelting && getOffsetTimer() % 20 == 0) {
                List<LivingEntity> entities = getPipeWorld().getEntitiesWithinAABB(LivingEntity.class, new AABB(getPipePos()).grow(2));
                for (LivingEntity entityLivingBase : entities) {
                    EntityDamageUtil.applyTemperatureDamage(entityLivingBase, stack.getFluid().getTemperature(stack), 2.0F, 10);
                }
            }

            // 1/10 chance to void everything and burn the pipe
            if (GTValues.RNG.nextInt(10) == 0) {
                stack.amount = 0;
                world.setBlockState(pos, Blocks.FIRE.getDefaultState());
            }
        }

        if (isShattering) {
            TileEntityFluidPipe.spawnParticles(world, pos, Direction.UP, EnumParticleTypes.CLOUD, 3 + GTValues.RNG.nextInt(2));

            // voids 75%
            stack.amount = Math.max(0, stack.amount / 4);

            // apply frost damage in area surrounding the pipe
            if (getOffsetTimer() % 20 == 0) {
                List<LivingEntity> entities = getPipeWorld().getEntitiesWithinAABB(LivingEntity.class, new AABB(getPipePos()).grow(2));
                for (LivingEntity entityLivingBase : entities) {
                    EntityDamageUtil.applyTemperatureDamage(entityLivingBase, stack.getFluid().getTemperature(stack), 2.0F, 10);
                }
            }

            // 1/10 chance to void everything and freeze the pipe
            if (GTValues.RNG.nextInt(10) == 0) {
                stack.amount = 0;
                world.setBlockToAir(pos);
            }
        }
    }

    private IFluidHandler getFluidHandlerAt(Direction facing, Direction oppositeSide) {
        BlockEntity tile = world.getBlockEntity(pos.offset(facing));
        if (tile == null) {
            return null;
        }
        return tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, oppositeSide);
    }

    public void receivedFrom(Direction facing) {
        if (facing != null) {
            lastReceivedFrom |= (1 << facing.getIndex());
        }
    }

    public FluidStack getContainedFluid(int channel) {
        if (channel < 0 || channel >= getFluidTanks().length) return null;
        return getFluidTanks()[channel].getFluid();
    }

    private void createTanksList() {
        fluidTanks = new FluidTank[getNodeData().getTanks()];
        for (int i = 0; i < getNodeData().getTanks(); i++) {
            fluidTanks[i] = new FluidTank(getCapacityPerTank());
        }
        pipeTankList = new PipeTankList(this, null, fluidTanks);
        for (Direction facing : Direction.values()) {
            tankLists.put(facing, new PipeTankList(this, facing, fluidTanks));
        }
    }

    public PipeTankList getTankList() {
        if (pipeTankList == null || fluidTanks == null) {
            createTanksList();
        }
        return pipeTankList;
    }

    public LazyOptional<IFluidHandler> tanksLazy = LazyOptional.of(() -> getTankList());

    public PipeTankList getTankList(Direction facing) {
        if (tankLists.isEmpty() || fluidTanks == null) {
            createTanksList();
        }
        return tankLists.getOrDefault(facing, pipeTankList);
    }



    public FluidTank[] getFluidTanks() {
        if (pipeTankList == null || fluidTanks == null) {
            createTanksList();
        }
        return fluidTanks;
    }

    public FluidStack[] getContainedFluids() {
        FluidStack[] fluids = new FluidStack[getFluidTanks().length];
        for (int i = 0; i < fluids.length; i++) {
            fluids[i] = fluidTanks[i].getFluid();
        }
        return fluids;
    }

    @Nonnull
    @Override
    public CompoundTag writeToNBT(@Nonnull CompoundTag nbt) {
        super.writeToNBT(nbt);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < getFluidTanks().length; i++) {
            FluidStack stack1 = getContainedFluid(i);
            CompoundTag fluidTag = new CompoundTag();
            if (stack1 == null || stack1.amount <= 0)
                fluidTag.putBoolean("isNull", true);
            else
                stack1.writeToNBT(fluidTag);
            list.appendTag(fluidTag);
        }
        nbt.put("Fluids", list);
        return nbt;
    }

    @Override
    public void readFromNBT(@Nonnull CompoundTag nbt) {
        super.readFromNBT(nbt);
        NBTTagList list = (NBTTagList) nbt.getTag("Fluids");
        createTanksList();
        for (int i = 0; i < list.tagCount(); i++) {
            CompoundTag tag = list.getCompoundAt(i);
            if (!tag.getBoolean("isNull")) {
                fluidTanks[i].setFluid(FluidStack.loadFluidStackFromNBT(tag));
            }
        }
    }

    @Nonnull
    @Override
    public List<Component> getDataInfo() {
        List<Component> list = new ArrayList<>();

        FluidStack[] fluids = this.getContainedFluids();
        if (fluids != null) {
            boolean allTanksEmpty = true;
            for (int i = 0; i < fluids.length; i++) {
                if (fluids[i] != null) {
                    if (fluids[i].getFluid() == null)
                        continue;

                    allTanksEmpty = false;
                    list.add(Component.translatable("behavior.tricorder.tank", i,
                            Component.translatable(Util.formatNumbers(fluids[i].amount)).setStyle(new Style().setColor(TextFormatting.GREEN)),
                            Component.translatable(Util.formatNumbers(this.getCapacityPerTank())).setStyle(new Style().setColor(TextFormatting.YELLOW)),
                            Component.translatable(fluids[i].getFluid().getLocalizedName(fluids[i])).setStyle(new Style().setColor(TextFormatting.GOLD))
                    ));
                }
            }

            if (allTanksEmpty)
                list.add(Component.translatable("behavior.tricorder.tanks_empty"));
        }
        return list;
    }
}
