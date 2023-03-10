package net.nemezanevem.gregtech.api.blockentity.multiblock;

public interface IMaintenance {

    byte getMaintenanceProblems();

    int getNumMaintenanceProblems();

    boolean hasMaintenanceProblems();

    void setMaintenanceFixed(int index);

    void causeMaintenanceProblems();

    void storeTaped(boolean isTaped);

    boolean hasMaintenanceMechanics();
}
