package org.moddingx.libx.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;

import javax.annotation.Nonnull;

/**
 * {@link EntityDataSerializer Data serializers} for enums. It needs to be registered in order to be used.
 */
public class EnumDataSerializer<T extends Enum<T>> implements EntityDataSerializer<T> {
    
    private final Class<T> enumClass;

    public EnumDataSerializer(Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public void write(@Nonnull FriendlyByteBuf buffer, @Nonnull T value) {
        buffer.writeEnum(value);
    }

    @Nonnull
    @Override
    public T read(@Nonnull FriendlyByteBuf buffer) {
        return buffer.readEnum(this.enumClass);
    }

    @Nonnull
    @Override
    public T copy(@Nonnull T value) {
        return value;
    }
}
