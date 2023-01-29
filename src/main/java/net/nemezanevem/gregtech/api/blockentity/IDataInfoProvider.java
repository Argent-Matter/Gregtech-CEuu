package net.nemezanevem.gregtech.api.blockentity;


import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.List;

public interface IDataInfoProvider {

    @Nonnull
    List<Component> getDataInfo();
}
