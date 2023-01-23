package net.nemezanevem.gregtech.api.capability.impl;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

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

    @Nullable
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        for (ICapabilityProvider provider : providers) {
            LazyOptional<T> cap = provider.getCapability(capability, facing);
            if (cap.isPresent()) {
                return cap;
            }
        }
        return null;
    }
}
