package io.github.noeppi_noeppi.libx.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import io.github.noeppi_noeppi.libx.config.correct.ConfigCorrection;
import io.github.noeppi_noeppi.libx.impl.config.ConfigImpl;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Optional;

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
    T fromJson(E json);

    /**
     * Serialises a value to JSON.
     *
     * @param value The value to serialise
     * @return The resulting json data.
     */
    E toJson(T value);

    /**
     * Reads a value from a {@link FriendlyByteBuf}. The default implementation expects a
     * JSON string and gives this string to {@link #fromJson(JsonElement) fromJSON}.
     */
    default T fromNetwork(FriendlyByteBuf buffer) {
        return this.fromJson(ConfigImpl.INTERNAL.fromJson(buffer.readUtf(0x40000), this.element()));
    }

    /**
     * Writes a value to a {@link FriendlyByteBuf}. The default implementation calls
     * {@link #toJson(Object) toJSON} and writes the resulting JSON as a string.
     */
    default void toNetwork(T value, FriendlyByteBuf buffer) {
        buffer.writeUtf(ConfigImpl.INTERNAL.toJson(this.toJson(value)), 0x40000);
    }

    /**
     * Corrects a config value. This is only called if {@link #fromJson(JsonElement)} fails.
     * Here the raw json for the config value is passed. Also a {@link ConfigCorrection} is
     * passed that allows to correct json of types contained in this value. For example a
     * list value mapper can use the {@link ConfigCorrection} to try to get as many values
     * as possible out of a json array.
     *
     * @return An {@link Optional} containing the corrected value or an empty {@link Optional}
     * the correction failed.
     */
    default Optional<T> correct(JsonElement json, ConfigCorrection<T> correction) {
        return Optional.empty();
    }
}
