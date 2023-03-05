package org.moddingx.libx.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;

/**
 * Provides some utility methods to encode and decode thing using a codec for a
 * specific {@link DynamicOps}.
 * 
 * {@link CodecOps} for {@link JsonOps json} and {@link NbtOps nbt} can be found in {@link CodecHelper}.
 */
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
    public <T> E write(Codec<T> codec, T value, HolderLookup.Provider registries) {
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
    public <T, R extends E> R write(Codec<T> codec, T value, Class<R> resultType, HolderLookup.Provider registries) {
        return encodeWith(codec, value, RegistryOps.create(this.ops, registries), this.baseClass, resultType);
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
     * @param registries The {@link RegistryAccess} to provide for registry aware codecs.
     */
    public <T> T read(Codec<T> codec, E value, HolderLookup.Provider registries) {
        return decodeWith(codec, value, RegistryOps.create(this.ops, registries));
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
        return codec.decode(ops, value).map(Pair::getFirst).getOrThrow(false, msg -> {});
    }
}
