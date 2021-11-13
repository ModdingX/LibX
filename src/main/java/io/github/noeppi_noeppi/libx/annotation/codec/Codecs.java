package io.github.noeppi_noeppi.libx.annotation.codec;

import com.mojang.serialization.Codec;
import io.github.noeppi_noeppi.libx.impl.ModInternal;
import io.github.noeppi_noeppi.libx.mod.ModX;

import java.util.Map;

/**
 * Class to retrieve generated {@link Codec codecs.}. They should normally be assigned to a
 * {@code public static final} field in the class that the codec is for.
 */
public class Codecs {

    /**
     * Gets a codec created by the use of the {@link PrimaryConstructor} annotation.
     * Should be assigned to a {@code public static final} named {@code CODEC} in the same file.
     *
     * @param mod   Your mods' class
     * @param clazz The class of which the codec was created
     */
    public static <T> Codec<T> get(Class<? extends ModX> mod, Class<T> clazz) {
        Map<Class<?>, Codec<?>> map = ModInternal.get(mod).getCodecMap();
        if (map == null) {
            throw new IllegalStateException("Can't get codec for " + clazz + ": No generated codecs for mod " + mod + ".");
        } else if (!map.containsKey(clazz)) {
            throw new IllegalStateException("Can't get codec for " + clazz + ": No codec found.");
        } else {
            //noinspection unchecked
            return (Codec<T>) map.get(clazz);
        }
    }
}
