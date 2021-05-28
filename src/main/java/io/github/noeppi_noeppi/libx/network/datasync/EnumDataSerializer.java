package io.github.noeppi_noeppi.libx.network.datasync;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.IDataSerializer;

import javax.annotation.Nonnull;
import java.util.HashMap;

/**
 * Data serializers for enums.
 */
public class EnumDataSerializer<T extends Enum<T>> implements IDataSerializer<T> {

    private static final HashMap<Class<? extends Enum<?>>, EnumDataSerializer<?>> INSTANCES = new HashMap<>();

    /**
     * Gets a data serializer for an enum class. The object returned will always be
     * the same object for the same given class.
     */
    public static <T extends Enum<T>> EnumDataSerializer<T> get(Class<T> clazz) {
        if (!INSTANCES.containsKey(clazz)) {
            INSTANCES.put(clazz, new EnumDataSerializer<>(clazz));
        }
        //noinspection unchecked
        return (EnumDataSerializer<T>) INSTANCES.get(clazz);
    }
    
    private final Class<T> enumClass;

    private EnumDataSerializer(Class<T> enumClass) {
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
