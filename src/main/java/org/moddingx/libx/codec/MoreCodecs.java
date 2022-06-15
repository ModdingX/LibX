package org.moddingx.libx.codec;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.moddingx.libx.crafting.CraftingHelper2;
import org.moddingx.libx.impl.codec.EnumCodec;
import org.moddingx.libx.impl.codec.ErrorCodec;
import org.moddingx.libx.impl.codec.OptionCodec;
import org.moddingx.libx.impl.codec.TypeMappedCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
