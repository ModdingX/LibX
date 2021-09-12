package io.github.noeppi_noeppi.libx.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

/**
 * Provides a way to get the seed for the current world.
 */
public class WorldSeedHolder {
    
    private static long seed = 0;

    /**
     * Gets the world seed to be used in codecs. Vanilla provides no way for this.
     */
    public static long getSeed() {
        return seed;
    }

    /**
     * You should never call this. This is therefore marked deprecated.
     */
    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    public static void setSeed(long worldSeed) {
        seed = worldSeed;
    }
    
    public static MapCodec<Long> fieldOf(String name) {
        return Codec.LONG.fieldOf(name).orElseGet(WorldSeedHolder::getSeed);
    }
}
