package org.moddingx.libx.config.mapper;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.moddingx.libx.config.Config;
import org.moddingx.libx.config.ConfigManager;
import org.moddingx.libx.config.correct.ConfigCorrection;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.validator.ValidatorInfo;
import org.moddingx.libx.impl.config.ConfigImpl;

import java.util.List;
import java.util.Optional;

/**
 * A way to serialise values of a specific type for a config file. See {@link ConfigManager} for
 * more info.
 *
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
     * Returns a list of comment lines that will be added to the values specified in {@link Config @Config}.
     */
    default List<String> comment() {
        return List.of();
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

    /**
     * Creates a {@link ConfigEditor} for this value mapper to display this config in the
     * config menu. To display that this value can't be edited through the GUI, use
     * {@link ConfigEditor#unsupported(Object)} with a default value that is used if for example
     * elements of this type are created in a list.
     * 
     * @param validator Access to the current validator used. Can be used to create different
     *                  editors based on validators.
     */
    @OnlyIn(Dist.CLIENT)
    ConfigEditor<T> createEditor(ValidatorInfo<?> validator);
}
