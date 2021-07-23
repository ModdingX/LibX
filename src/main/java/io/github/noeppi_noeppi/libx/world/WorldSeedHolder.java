package io.github.noeppi_noeppi.libx.world;

import com.mojang.serialization.Codec;

/**
 * Provides a way to get the {@link World} seed in {@link Codec codecs}.
 */
public class WorldSeedHolder {
    
    public static final Codec<Long> CODEC = Codec.LONG.orElseGet(WorldSeedHolder::getSeed);
    
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
    public static void setSeed(long worldSeed) {
        seed = worldSeed;
    }
}
