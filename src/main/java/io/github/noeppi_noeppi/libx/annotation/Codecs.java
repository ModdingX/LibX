package io.github.noeppi_noeppi.libx.annotation;

import com.mojang.serialization.Codec;
import io.github.noeppi_noeppi.libx.mod.ModX;

import java.lang.reflect.Field;
import java.util.Map;

public class Codecs {

    /**
     * Gets a codec created by the use of the {@link PrimaryConstructor} annotation.
     * Should be assigned to a public static final field named {@code CODEC} in the same file.
     *
     * @param mod   Your mods' class
     * @param clazz The class of which the codec was created
     */
    public static <T> Codec<T> get(Class<? extends ModX> mod, Class<T> clazz) {
        try {
            Class<?> modInit;
            try {
                modInit = Class.forName(mod.getCanonicalName() + "$");
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Can't get codec for " + clazz + ": No generated code for mod " + mod + ".", e);
            }
            Field field;
            try {
                field = modInit.getDeclaredField("codecs");
            } catch (NoSuchFieldException e) {
                throw new IllegalStateException("Can't get codec for " + clazz + ": No generated codecs for mod " + mod + ".", e);
            }
            @SuppressWarnings("unchecked")
            Map<Class<?>, Codec<?>> codecs = (Map<Class<?>, Codec<?>>) field.get(null);
            if (!codecs.containsKey(clazz)) {
                throw new IllegalStateException("Can't get codec for " + clazz + ": No codec found.");
            }
            //noinspection unchecked
            return (Codec<T>) codecs.get(clazz);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't get codec for " + clazz, e);
        }
    }
}
