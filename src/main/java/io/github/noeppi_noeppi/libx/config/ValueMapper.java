package io.github.noeppi_noeppi.libx.config;

import com.google.gson.JsonElement;
import io.github.noeppi_noeppi.libx.impl.config.ConfigImpl;
import net.minecraft.network.PacketBuffer;

import java.util.Collections;
import java.util.List;

/**
 * A way to serialise values of a specific type for a config file. See {@link ConfigManager} for
 * more info.
 * @param <T> The type that this mapper can serialise.
 * @param <E> The JSON element type this mapper uses.
 */
public interface ValueMapper<T, E extends JsonElement> {

    /**
     * Gets the class of the type that this mapper can serialise.
     */
    Class<T> type();

    /**
     * Gets the class of the JSON element type this mapper uses.
     */
    Class<E> element();

    /**
     * Reads an object from JSON. If the json is invalid you may either throw
     * an {@code IllegalStateException} or a {@code JsonParseException} to end
     * config parsing with an error, or just correct errors yourself and return
     * default values.
     * 
     * @param json The json data
     * @param elementType The element type specified in the {@link Config @Config}
     *                    annotation. If it's left out, this will be {@code void.class}
     * @return The value read.
     */
    T fromJSON(E json, Class<?> elementType);

    /**
     * Serialises a value to JSON.
     * @param value The value to serialise
     * @param elementType The element type specified in the {@link Config @Config}
     *                    annotation. If it's left out, this will be {@code void.class}
     * @return The resulting json data.
     */
    E toJSON(T value, Class<?> elementType);

    /**
     * Reads a value from a {@code PacketBuffer}. The default implementation expects a
     * JSON string and gives this string to {@code fromJSON}.
     */
    default T read(PacketBuffer buffer, Class<?> elementType) {
        return this.fromJSON(ConfigImpl.INTERNAL.fromJson(buffer.readString(0x40000), this.element()), elementType);
    }

    /**
     * Writes a value to a {@code PacketBuffer}. The default implementation calls
     * {@code toJSON} and writes the resulting JSON as a string.
     */
    default void write(T value, PacketBuffer buffer, Class<?> elementType) {
        buffer.writeString(ConfigImpl.INTERNAL.toJson(this.toJSON(value, elementType)), 0x40000);
    }

    /**
     * Returns a list of comment lines that will be added to the values specified in @Config.
     */
    default List<String> comment(Class<?> elementType) {
        return Collections.emptyList();
    }
}
