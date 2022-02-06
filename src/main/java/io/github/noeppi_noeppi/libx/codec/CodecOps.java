package io.github.noeppi_noeppi.libx.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.github.noeppi_noeppi.libx.annotation.meta.Experimental;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.RegistryResourceAccess;
import net.minecraft.resources.RegistryWriteOps;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * Provides some utility methods to encode and decode thing using a codec for a
 * specific {@link DynamicOps}.
 * 
 * {@link CodecOps} for {@link JsonOps json} and {@link NbtOps nbt} can be found in {@link CodecHelper}.
 */
@Experimental
public class CodecOps<E> {
    
    private final Class<E> baseClass;
    private final DynamicOps<E> ops;

    /**
     * Creates new {@link CodecOps}.
     * 
     * @param baseClass The base value class of the {@link DynamicOps} to use.
     * @param ops The base {@link DynamicOps} to use.
     */
    public CodecOps(Class<E> baseClass, DynamicOps<E> ops) {
        this.baseClass = baseClass;
        this.ops = ops;
    }

    /**
     * Writes an element using a code.
     */
    public <T> E write(Codec<T> codec, T value) {
        return this.write(codec, value, this.baseClass);
    }

    /**
     * Writes an element using a code.
     * 
     * @param registries The {@link RegistryAccess} to provide for registry
     *                   aware codecs.
     */
    public <T> E write(Codec<T> codec, T value, RegistryAccess registries) {
        return this.write(codec, value, this.baseClass, registries);
    }

    /**
     * Writes an element using a code.
     * 
     * @param resultType The class that the result is expected to have. It is ensured,
     *                   that the result is of this class. If the codec produces data
     *                   of another class, an exception will be thrown.
     */
    public <T, R extends E> R write(Codec<T> codec, T value, Class<R> resultType) {
        return encodeWith(codec, value, this.ops, this.baseClass, resultType);
    }

    /**
     * Writes an element using a code.
     * 
     * @param resultType The class that the result is expected to have. It is ensured,
     *                   that the result is of this class. If the codec produces data
     *                   of another class, an exception will be thrown.
     * @param registries The {@link RegistryAccess} to provide for registry
     *                   aware codecs.
     */
    public <T, R extends E> R write(Codec<T> codec, T value, Class<R> resultType, RegistryAccess registries) {
        return encodeWith(codec, value, RegistryWriteOps.create(this.ops, registries), this.baseClass, resultType);
    }

    /**
     * Reads a value using a codec.
     */
    public <T> T read(Codec<T> codec, E value) {
        return decodeWith(codec, value, this.ops);
    }

    /**
     * Reads a value using a codec.
     * 
     * @param registries The {@link RegistryAccess} to provide for registry
     *                   aware codecs.
     */
    public <T> T read(Codec<T> codec, E value, RegistryAccess registries, ResourceManager resources) {
        return decodeWith(codec, value, RegistryReadOps.create(this.ops, resources, registries));
    }

    /**
     * Reads a value using a codec.
     *
     * @param registries The {@link RegistryAccess} to provide for registry
     *                   aware codecs.
     */
    public <T> T read(Codec<T> codec, E value, RegistryAccess registries, RegistryResourceAccess resourceAccess) {
        return decodeWith(codec, value, RegistryReadOps.create(this.ops, resourceAccess, registries));
    }

    private static <T, E, R extends E> R encodeWith(Codec<T> codec, T value, DynamicOps<E> ops, Class<E> baseClass, Class<R> resultType) {
        return codec.encodeStart(ops, value).flatMap(result -> {
            if (resultType == baseClass || resultType.isAssignableFrom(result.getClass())) {
                //noinspection unchecked
                return DataResult.success((R) result);
            } else {
                return DataResult.error("Invalid type while encoding with value " + value + " with " + codec + ": Expected " + resultType + ", got " + result.getClass());
            }
        }).getOrThrow(false, msg -> {});
    }

    private static <T, E> T decodeWith(Codec<T> codec, E value, DynamicOps<E> ops) {
        return codec.decode(ops, value).flatMap(pair -> {
            if (ops.empty().equals(pair.getSecond())) {
                return DataResult.success(pair.getFirst());
            } else {
                return DataResult.error("Input not fully consumed.");
            }
        }).getOrThrow(false, msg -> {});
    }
}