package org.moddingx.libx.datagen;

/**
 * A stage in the LibX datagen system. These are processed in order during datagen.
 */
public enum DatagenStage {

    /**
     * In this stage all non-extension registries are writable and can be populated.
     * Generation of output is not supported.
     */
    REGISTRY_SETUP,

    /**
     * In this stage all non-extension registries have been frozen. Extension registries
     * are now writable and can be populated. Generation of output is not supported.
     */
    EXTENSION_SETUP,

    /**
     * On transition to this phase, the elements from the registries are generated.
     * In this phase, all registries have been fully set up, output can be generated.
     * 
     * For example, tags of worldgen registries can access a fully populated registry here.
     * All registries have been frozen.
     */
    DATAGEN
}
