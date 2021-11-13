package io.github.noeppi_noeppi.libx.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.noeppi_noeppi.libx.impl.world.WorldSeedHolderImpl;

/**
 * Provides a way to get the seed for the current world.
 */
public class WorldSeedHolder {

    /**
     * Gets the world seed to be used in codecs. Vanilla provides no way for this.
     */
    public static long getSeed() {
        return WorldSeedHolderImpl.seed;
    }
    
    public static MapCodec<Long> fieldOf(String name) {
        return Codec.LONG.fieldOf(name).orElseGet(WorldSeedHolder::getSeed);
    }
}
