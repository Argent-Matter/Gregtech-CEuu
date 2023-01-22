package net.nemezanevem.gregtech.api.capability.impl;

import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class CombinedCapabilityProvider implements ICapabilityProvider {

    private final ICapabilityProvider[] providers;

    public CombinedCapabilityProvider(ICapabilityProvider... providers) {
        this.providers = providers;
    }

    public CombinedCapabilityProvider(List<ICapabilityProvider> providers) {
        this.providers = providers.toArray(new ICapabilityProvider[0]);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable Direction facing) {
        for (ICapabilityProvider provider : providers) {
            if (provider.hasCapability(capability, facing)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        for (ICapabilityProvider provider : providers) {
            T cap = provider.getCapability(capability, facing);
            if (cap != null) {
                return cap;
            }
        }
        return null;
    }
}
