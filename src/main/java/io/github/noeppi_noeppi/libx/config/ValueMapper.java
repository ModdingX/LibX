package io.github.noeppi_noeppi.libx.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import io.github.noeppi_noeppi.libx.impl.config.ConfigImpl;
import net.minecraft.network.FriendlyByteBuf;

/**
 * A way to serialise values of a specific type for a config file. See {@link ConfigManager} for
 * more info.
 * 
 * @param <T> The type that this mapper can serialise.
 * @param <E> The JSON element type this mapper uses.
 */
public interface ValueMapper<T, E extends JsonElement> extends CommonValueMapper<T, E> {

    /**
     * Reads an object from JSON. If the json is invalid you may either throw
     * an {@link IllegalStateException} or a {@link JsonParseException} to end
     * config parsing with an error, or just correct errors yourself and return
     * default values.
     * 
     * @param json The json data
     * @return The value read.
     */
    T fromJSON(E json);

    /**
     * Serialises a value to JSON.
     * 
     * @param value The value to serialise
     * @return The resulting json data.
     */
    E toJSON(T value);

    /**
     * Reads a value from a {@link PacketBuffer}. The default implementation expects a
     * JSON string and gives this string to {@link ValueMapper#fromJSON(JsonElement) fromJSON}.
     */
    default T read(FriendlyByteBuf buffer) {
        return this.fromJSON(ConfigImpl.INTERNAL.fromJson(buffer.readUtf(0x40000), this.element()));
    }

    /**
     * Writes a value to a {@link PacketBuffer}. The default implementation calls
     * {@link ValueMapper#toJSON(Object) toJSON} and writes the resulting JSON as a string.
     */
    default void write(T value, FriendlyByteBuf buffer) {
        buffer.writeUtf(ConfigImpl.INTERNAL.toJson(this.toJSON(value)), 0x40000);
    }
}
