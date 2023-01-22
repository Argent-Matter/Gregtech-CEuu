package net.nemezanevem.gregtech.api.item.metaitem.stats;

import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IMusicDisc extends IItemComponent {

    SoundEvent getSound();
}
