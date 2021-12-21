package io.github.noeppi_noeppi.libx.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.github.noeppi_noeppi.libx.impl.codec.EnumCodec;
import io.github.noeppi_noeppi.libx.impl.codec.ForgeRegistryCodec;
import io.github.noeppi_noeppi.libx.impl.codec.OptionCodec;
import net.minecraft.util.Unit;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

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
     * Gets a codec that encodes a {@link IForgeRegistryEntry} as a string using its registry name.
     */
    public static <T extends IForgeRegistryEntry<T>> Codec<T> registry(IForgeRegistry<T> registry) {
        return ForgeRegistryCodec.get(registry);
    }
}
