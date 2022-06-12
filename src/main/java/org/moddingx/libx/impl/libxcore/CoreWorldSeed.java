package org.moddingx.libx.impl.libxcore;

import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.moddingx.libx.impl.world.WorldSeedHolderImpl;

public class CoreWorldSeed {

    /**
     * Patched into the main constructor of {@link WorldGenSettings}
     * before any {@code return} passing the {@code seed} parameter.
     */
    public static void setWorldSeed(long seed) {
        WorldSeedHolderImpl.seed = seed;
    }
}
