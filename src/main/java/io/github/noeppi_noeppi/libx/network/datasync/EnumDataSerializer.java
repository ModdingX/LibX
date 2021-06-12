package io.github.noeppi_noeppi.libx.network.datasync;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.IDataSerializer;

import javax.annotation.Nonnull;

/**
 * {@link IDataSerializer Data serializers} for enums. You need to register them manually tough.
 */
public class EnumDataSerializer<T extends Enum<T>> implements IDataSerializer<T> {
    
    private final Class<T> enumClass;

    public EnumDataSerializer(Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public void write(@Nonnull PacketBuffer buf, @Nonnull T value) {
        buf.writeEnumValue(value);
    }

    @Nonnull
    @Override
    public T read(@Nonnull PacketBuffer buf) {
        return buf.readEnumValue(this.enumClass);
    }

    @Nonnull
    @Override
    public T copyValue(@Nonnull T value) {
        return value;
    }
}
