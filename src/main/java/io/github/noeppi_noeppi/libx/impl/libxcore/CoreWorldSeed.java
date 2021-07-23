package io.github.noeppi_noeppi.libx.impl.libxcore;

import io.github.noeppi_noeppi.libx.world.WorldSeedHolder;

public class CoreWorldSeed {

    /**
     * Patched into the private constructor of {@link DimensionGeneratorSettings}
     * before any {@code return} passing the {@code seed} parameter.
     */
    public static void setWorldSeed(long seed) {
        //noinspection deprecation
        WorldSeedHolder.setSeed(seed);
    }
}
