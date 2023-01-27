package net.nemezanevem.gregtech.api.tileentity;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import java.util.function.Consumer;

public abstract class MTETrait {

    protected final MetaTileEntity metaTileEntity;

    public MTETrait(MetaTileEntity metaTileEntity) {
        this.metaTileEntity = metaTileEntity;
        metaTileEntity.addMetaTileEntityTrait(this);
    }

    public MetaTileEntity getMetaTileEntity() {
        return metaTileEntity;
    }

    public abstract String getName();

    public abstract int getNetworkID();

    public abstract <T> LazyOptional<T> getCapability(Capability<T> capability);

    public void onFrontFacingSet(Direction newFrontFacing) {
    }

    public void tick() {
    }

    public CompoundTag serializeNBT() {
        return new CompoundTag();
    }

    public void deserializeNBT(CompoundTag compound) {
    }

    public void writeInitialData(FriendlyByteBuf buffer) {
    }

    public void receiveInitialData(FriendlyByteBuf buffer) {
    }

    public void receiveCustomData(int id, FriendlyByteBuf buffer) {
    }

    public final void writeCustomData(int id, Consumer<FriendlyByteBuf> writer) {
        metaTileEntity.writeTraitData(this, id, writer);
    }

    protected static final class TraitNetworkIds {
        public static final int TRAIT_ID_ENERGY_CONTAINER = 1;
        public static final int TRAIT_ID_WORKABLE = 2;
    }

}
