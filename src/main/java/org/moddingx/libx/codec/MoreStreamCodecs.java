package org.moddingx.libx.codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;

/**
 * Provides additional {@link StreamCodec stream codecs}.
 */
public class MoreStreamCodecs {

    /**
     * A {@link StreamCodec stream streamCodec} for NBT tags.
     */
    public static final StreamCodec<ByteBuf, Tag> TAG = StreamCodec.of(
            FriendlyByteBuf::writeNbt,
            buf -> Objects.requireNonNullElse(FriendlyByteBuf.readNbt(buf, NbtAccounter.create(2097152L)), EndTag.INSTANCE)
    );
    
    /**
     * A {@link StreamCodec stream streamCodec} for NBT compound tags.
     */
    public static final StreamCodec<ByteBuf, CompoundTag> COMPOUND_TAG = StreamCodec.of(
            FriendlyByteBuf::writeNbt,
            buf -> Objects.requireNonNull(FriendlyByteBuf.readNbt(buf), "Not a compound tag.")
    );
    
    /**
     * Creates a unit {@link StreamCodec}. The returned codec will never write any bytes and always use the
     * provided {@link Supplier} to produce a result value. It ignores its input while encoding.
     */
    public static <B extends ByteBuf, T> StreamCodec<B, T> unit(Supplier<T> value) {
        return new StreamCodec<>() {

            @Override
            public void encode(@Nonnull B buffer, @Nonnull T value) {}

            @Nonnull
            @Override
            public T decode(@Nonnull B buffer) {
                return value.get();
            }
        };
    }

    public static <B extends ByteBuf, T> StreamCodec<B, Optional<T>> maybe(StreamCodec<? super B, T> elementCodec) {
        return new StreamCodec<>() {

            @Override
            public void encode(@Nonnull B buffer, @Nonnull Optional<T> value) {
                buffer.writeBoolean(value.isPresent());
                value.ifPresent(element -> elementCodec.encode(buffer, element));
            }

            @Nonnull
            @Override
            public Optional<T> decode(@Nonnull B buffer) {
                boolean present = buffer.readBoolean();
                return present ? Optional.of(elementCodec.decode(buffer)) : Optional.empty();
            }
        };
    }
    
    public static <B extends ByteBuf, T> StreamCodec<B, List<T>> listOf(StreamCodec<? super B, T> elementCodec) {
        return new StreamCodec<>() {

            @Override
            public void encode(@Nonnull B buffer, @Nonnull List<T> value) {
                VarInt.write(buffer, value.size());
                for (T element : value) elementCodec.encode(buffer, element);
            }

            @Nonnull
            @Override
            public List<T> decode(@Nonnull B buffer) {
                int size = VarInt.read(buffer);
                ArrayList<T> list = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    list.add(elementCodec.decode(buffer));
                }
                return Collections.unmodifiableList(list);
            }
        };
    }
    
    public static <B extends ByteBuf, K, V> StreamCodec<B, Map<K, V>> mapOf(StreamCodec<? super B, K> keyCodec, StreamCodec<? super B, V> valueCodec) {
        return new StreamCodec<>() {

            @Override
            public void encode(@Nonnull B buffer, @Nonnull Map<K, V> value) {
                VarInt.write(buffer, value.size());
                for (Map.Entry<K, V> entry : value.entrySet()) {
                    keyCodec.encode(buffer, entry.getKey());
                    valueCodec.encode(buffer, entry.getValue());
                }
            }

            @Nonnull
            @Override
            public Map<K, V> decode(@Nonnull B buffer) {
                int size = VarInt.read(buffer);
                HashMap<K, V> map = new HashMap<>(size);
                for (int i = 0; i < size; i++) {
                    K key = keyCodec.decode(buffer);
                    V value = valueCodec.decode(buffer);
                    map.put(key, value);
                }
                return Collections.unmodifiableMap(map);
            }
        };
    }
}
