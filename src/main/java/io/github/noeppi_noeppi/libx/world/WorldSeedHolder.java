package io.github.noeppi_noeppi.libx.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.noeppi_noeppi.libx.LibX;
import io.github.noeppi_noeppi.libx.annotation.meta.RemoveIn;
import io.github.noeppi_noeppi.libx.impl.world.WorldSeedHolderImpl;
import io.github.noeppi_noeppi.libx.util.ClassUtil;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.ServerLevel;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * Provides a way to get the seed for the current world.
 * 
 * @deprecated Since 1.18.2, the world seed is initialised too late to be used in chunk
 *             generator and biome source codecs and will yield wrong results there. After
 *             the level has been constructed, the seed is obtainable using
 *             {@code level.getServer().getWorldData().worldGenSettings().seed()} from a
 *             {@link ServerLevel}.
 *             <b>Using this in worldgen codecs can lead to chunk errors, don't do this.</b>
 */
@Deprecated(forRemoval = true)
@RemoveIn(minecraft = "1.19")
public class WorldSeedHolder {

    /**
     * Gets the world seed to be used in codecs. Vanilla provides no way for this.
     */
    public static long getSeed() {
        try {
            // Find out if we are in a worldgen codec and log a message
            if (ClassUtil.calledBy(WorldStem.class, "m_206911_", WorldStem.InitConfig.class, WorldStem.DataPackConfigSupplier.class, WorldStem.WorldDataSupplier.class, Executor.class, Executor.class)) {
                String trace = Arrays.stream(new Exception().getStackTrace())
                        .dropWhile(t -> Objects.equals(t.getClassName(), WorldSeedHolder.class.getCanonicalName()))
                        .takeWhile(t -> !Objects.equals(t.getClassName(), WorldStem.class.getCanonicalName()))
                        .map(t -> " > " + t).collect(Collectors.joining("\n"));
                LibX.logger.error("Use of WorldSeedHolder#getSeed from worldgen codec detected. Since 1.18.2 this no longer works and will produce wrong results.\n" + trace);
            }
        } catch (Exception | NoClassDefFoundError e) {
            //
        }
        return WorldSeedHolderImpl.seed;
    }
    
    public static MapCodec<Long> fieldOf(String name) {
        return Codec.LONG.fieldOf(name).orElseGet(WorldSeedHolder::getSeed);
    }
}
