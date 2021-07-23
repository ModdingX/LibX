package io.github.noeppi_noeppi.libx.network.datasync;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;

import javax.annotation.Nonnull;

/**
 * {@link IDataSerializer Data serializers} for enums. You need to register them manually tough.
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
