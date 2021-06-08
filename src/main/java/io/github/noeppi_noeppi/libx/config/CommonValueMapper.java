package io.github.noeppi_noeppi.libx.config;

import com.google.gson.JsonElement;

import java.util.Collections;
import java.util.List;

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
     * Returns a list of comment lines that will be added to the values specified in @Config.
     */
    default List<String> comment() {
        return Collections.emptyList();
    }
}
