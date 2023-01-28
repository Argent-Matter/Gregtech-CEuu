package net.nemezanevem.gregtech.api.module;

public enum ModuleStage {
    C_SETUP,         // Initializing Module Containers
    M_SETUP,         // Initializing Modules
    INIT,            // MC Initialization stage
    FINISHED,        // MC LoadComplete stage
    SERVER_STARTING, // MC ServerStarting stage
    SERVER_STARTED   // MC ServerStarted stage
}
