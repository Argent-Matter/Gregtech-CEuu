package net.nemezanevem.gregtech.api.gui;

public class BlankUIHolder implements IUIHolder {

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public void markAsDirty() {
    }
}
