package net.nemezanevem.gregtech.common.mixinutil;

public interface IMixinFluidType {
    boolean isAcid();
    void setAcid(boolean acid);
    void setTemperature(int temperature);
}
