package org.moddingx.libx.codec;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.moddingx.libx.crafting.CraftingHelper2;
import org.moddingx.libx.impl.codec.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Provides additional {@link Codec codecs}.
 */
public class MoreCodecs {

    /**
     * A codec for the {@link Unit} constant that encodes to nothing.
     */
    public static final Codec<Unit> UNIT = new Codec<>() {

        @Override
        public <T> DataResult<T> encode(Unit input, DynamicOps<T> ops, T prefix) {
            return DataResult.success(prefix);
        }

        @Override
        public <T> DataResult<Pair<Unit, T>> decode(DynamicOps<T> ops, T input) {
            return DataResult.success(Pair.of(Unit.INSTANCE, input));
        }
    };

    /**
     * A {@link Codec} for {@link ItemStack item stacks} that will encode the stack as NBT when using
     * NBT dynamic ops, as recipe JSON when using JSON dynamic ops and as a string containing the NBT tag
     * if using some other dynamic ops.
     */
    public static final Codec<ItemStack> SAFE_ITEM_STACK = typeMapped(
            Codec.STRING.flatXmap(
                    str -> CodecHelper.doesNotThrow(() -> ItemStack.of(TagParser.parseTag(str))),
                    stack -> CodecHelper.doesNotThrow(() -> stack.save(new CompoundTag()).toString())
            ),
            TypedEncoder.of(Tag.class, stack -> stack.save(new CompoundTag()), tag -> ItemStack.of((CompoundTag) tag)),
            TypedEncoder.of(JsonElement.class, stack -> CraftingHelper2.serializeItemStack(stack, true), json -> CraftingHelper.getItemStack(json.getAsJsonObject(), true))
    );

    /**
     * Gets a codec that always errors with the given message.
     */
    public static <T> Codec<T> error(String msg) {
        return error(msg, msg);
    }

    /**
     * Gets a codec that always errors with the given messages.
     */
    public static <T> Codec<T> error(String encodeMsg, String decodeMsg) {
        return new ErrorCodec<>(encodeMsg, decodeMsg);
    }
    
    /**
     * Gets a codec that encodes an {@link Optional} with a given child codec.
     */
    public static <T> Codec<Optional<T>> option(Codec<T> codec) {
        return new OptionCodec<>(codec);
    }
    
    /**
     * Gets a codec that encodes an {@link Enum enum} as a string.
     */
    public static <T extends Enum<T>> Codec<T> enumCodec(Class<T> clazz) {
        return EnumCodec.get(clazz);
    }
    
    /**
     * Extends the give {@link Codec} with some new fields defined by the given {@link MapCodec}. The given
     * codec <b>must</b> encode to a {@link MapLike}.
     */
    public static <M, E> Codec<Pair<M, E>> extend(Codec<M> codec, MapCodec<E> extension) {
        return extend(codec, extension, Function.identity(), Pair::of);
    }
    
    /**
     * Extends the give {@link Codec} with some new fields defined by the given {@link MapCodec}. The given
     * codec <b>must</b> encode to a {@link MapLike}.
     */
    public static <A, M, E> Codec<A> extend(Codec<M> codec, MapCodec<E> extension, Function<A, Pair<M, E>> decompose, BiFunction<M, E, A> construct) {
        return mapDispatch(extension, key -> DataResult.success(codec), decompose.andThen(Pair::swap), (e, m) -> DataResult.success(construct.apply(m, e)));
    }
    
    /**
     * Creates a map dispatched codec. When encoding an element, it ist first decomposed into key and value.
     * The key is used to obtain a codec to encode the value using the passed {@code valueCodecs} function.
     * The {@link Codec} returned from that function <b>must</b> encode to a {@link MapLike}.
     * After that, the key is encoded and merged into the {@link MapLike} from the value codec.
     * 
     * Decoding works the other way round in that the key is read first. Then the {@code valueCodecs} function
     * is used to obtain a {@link Codec} to decode the value. In the end, the codec uses both key and value to
     * construct the resulting element. Both the {@link MapCodec} and the codecs returned from {@code valueCodecs}
     * <b>must</b> be able to work with additional values, they don't know about.
     */
    public static <A, K, V> Codec<A> mapDispatch(MapCodec<K> keyCodec, Function<K, DataResult<Codec<? extends V>>> valueCodecs, Function<A, Pair<K, V>> decompose, BiFunction<K, V, DataResult<A>> construct) {
        return new MapDispatchedCodec<>(keyCodec, valueCodecs, decompose, construct);
    }

    /**
     * Lazily wraps the given {@link Codec}. Useful when codecs need to reference each other to recurse.
     */
    public static <T> Codec<T> lazy(Supplier<Codec<T>> codec) {
        return new LazyCodec<>(codec);
    }
    
    /**
     * Gets a type mapped codec that will try to encode and decode values with the first
     * matching {@link TypedEncoder}.
     * If no {@link TypedEncoder} matches, an error will be returned.
     */
    @SafeVarargs
    @SuppressWarnings({"ManualArrayToCollectionCopy", "UseBulkOperation"})
    public static <T> Codec<T> typeMapped(TypedEncoder<T, ?>... encoders) {
        if (encoders.length == 0) return error("Empty type mapped codec");
        List<TypedEncoder<T, ?>> list = new ArrayList<>();
        for (TypedEncoder<T, ?> encoder : encoders) list.add(encoder);
        return new TypeMappedCodec<>(list, null);
    }

    /**
     * Gets a type mapped codec that will try to encode and decode values with the first
     * matching {@link TypedEncoder}.
     * If no {@link TypedEncoder} matches, the fallback is used.
     */
    @SafeVarargs
    @SuppressWarnings({"ManualArrayToCollectionCopy", "UseBulkOperation"})
    public static <T> Codec<T> typeMapped(Codec<T> fallback, TypedEncoder<T, ?>... encoders) {
        if (encoders.length == 0) return fallback;
        List<TypedEncoder<T, ?>> list = new ArrayList<>();
        for (TypedEncoder<T, ?> encoder : encoders) list.add(encoder);
        return new TypeMappedCodec<>(list, fallback);
    }
}
