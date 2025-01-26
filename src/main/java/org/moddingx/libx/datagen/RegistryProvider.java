package org.moddingx.libx.datagen;

import net.minecraft.data.DataProvider;

/**
 * A registry provider is the analogue of a {@link DataProvider} for the {@link DatagenStage#REGISTRY_SETUP registry setup}
 * and {@link DatagenStage#EXTENSION_SETUP extension setup} stages. It can register elements to the relevant registries
 * which will be written to the datagen output once the datagen finishes.
 */
public interface RegistryProvider {

   /**
    * Gets the name of the provider.
    */
   String getName();

   /**
    * Runs the provider.
    */
   void run();
}
