package io.github.noeppi_noeppi.libx.world;

/**
 * Provides a way to get the world seed in codecs.
 */
public class WorldSeedHolder {
    
    private static long seed = 0;

    /**
     * Gets the world seet to be used in codecs. Vanilla provides no way for this.
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
