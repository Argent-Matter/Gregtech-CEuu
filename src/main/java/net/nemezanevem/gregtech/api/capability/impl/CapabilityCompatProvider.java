package net.nemezanevem.gregtech.api.capability.impl;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public abstract class CapabilityCompatProvider implements ICapabilityProvider {

    private final ICapabilityProvider upvalue;

    public CapabilityCompatProvider(ICapabilityProvider upvalue) {
        this.upvalue = upvalue;
    }

    protected <T> boolean hasUpvalueCapability(Capability<T> capability, Direction facing) {
        return upvalue.getCapability(capability, facing).isPresent();
    }

    @Nullable
    protected <T> LazyOptional<T> getUpvalueCapability(Capability<T> capability, Direction facing) {
        return upvalue.getCapability(capability, facing);
    }
}
