package net.nemezanevem.gregtech.api.gui;

import net.nemezanevem.gregtech.api.util.IDirtyNotifiable;

public interface IUIHolder extends IDirtyNotifiable {

    boolean isValid();

    boolean isRemote();

    void markAsDirty();

}
