package org.moddingx.libx.config;

import com.google.gson.JsonElement;

import java.util.Collections;
import java.util.List;

/**
 * Base interface for everything that can map values for config entries. See
 * {@link ConfigManager} for more info.
 * 
 * @param <T> The type that this mapper can serialise.
 * @param <E> The JSON element type this mapper uses.
 */
public interface CommonValueMapper<T, E extends JsonElement> {

    /**
     * Gets the class of the type that this mapper can serialise.
     */
    Class<T> type();

    /**
     * Gets the class of the JSON element type this mapper uses.
     */
    Class<E> element();

    /**
     * Returns a list of comment lines that will be added to the values specified in {@link Config @Config}.
     */
    default List<String> comment() {
        return Collections.emptyList();
    }
}
