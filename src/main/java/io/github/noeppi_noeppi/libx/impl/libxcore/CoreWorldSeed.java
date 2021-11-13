package io.github.noeppi_noeppi.libx.impl.libxcore;

import io.github.noeppi_noeppi.libx.impl.world.WorldSeedHolderImpl;
import net.minecraft.world.level.levelgen.WorldGenSettings;

public class CoreWorldSeed {

    /**
     * Patched into the private constructor of {@link WorldGenSettings}
     * before any {@code return} passing the {@code seed} parameter.
     */
    public static void setWorldSeed(long seed) {
        WorldSeedHolderImpl.seed = seed;
    }
}
