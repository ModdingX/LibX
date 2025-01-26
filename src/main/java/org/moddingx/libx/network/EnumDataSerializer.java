package org.moddingx.libx.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import javax.annotation.Nonnull;

/**
 * {@link EntityDataSerializer Data serializers} for enums. It needs to be registered in order to be used.
 */
public class EnumDataSerializer<T extends Enum<T>> implements EntityDataSerializer<T> {

    private final StreamCodec<RegistryFriendlyByteBuf, T> codec;

    public EnumDataSerializer(Class<T> enumClass) {
        this.codec = NeoForgeStreamCodecs.enumCodec(enumClass);
    }

    @Nonnull
    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, T> codec() {
        return this.codec;
    }

    @Nonnull
    @Override
    public T copy(@Nonnull T value) {
        return value;
    }
}
