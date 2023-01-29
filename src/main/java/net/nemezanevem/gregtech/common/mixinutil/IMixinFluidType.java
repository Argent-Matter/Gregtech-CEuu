package net.nemezanevem.gregtech.common.mixinutil;

public interface IMixinFluidType {
    boolean isAcid();
    void setAcid(boolean acid);
    void setTemperature(int temperature);
    void setDensity(int density);
    void setViscosity(int viscosity);
    void setLightLevel(int luminosity);
}
