package org.moddingx.libx.config.correct;

import com.google.gson.JsonElement;
import org.apache.commons.lang3.tuple.Pair;
import org.moddingx.libx.config.mapper.ValueMapper;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * An instance of this interface is passed when a config is corrected. It can be used to best
 * restore matching parts of the config.
 */
public interface ConfigCorrection<T> {

    /**
     * Tries to get a value from a piece of json given a matching value mapper. This should be
     * used if there's no way to get a default value in any way.
     * 
     * @return The corrected value for the given json or an empty option if it could not be corrected.
     */
    <U> Optional<U> tryGet(JsonElement json, ValueMapper<U, ?> mapper);

    /**
     * Tries to get a value from a piece of json given a matching value mapper and a function to
     * extract a default value for the value that this method should retrieve when given a default
     * value for this config value.
     * 
     * For example when a {@link Pair} is corrected and it find an array with two elements, it would
     * do something like {@code correct(jsonArray.get(0), firstMapper, Pair::getLeft);} to correct
     * the first value from the pair.
     * 
     * Set json to null if you can't get a matching piece of json in any case.
     * 
     * @return The corrected value for the given json or an empty option if it could not be corrected.
     */
    default <U> Optional<U> correct(@Nullable JsonElement json, ValueMapper<U, ?> mapper, Function<T, U> extractor) {
        return this.tryCorrect(json, mapper, value -> Optional.ofNullable(extractor.apply(value)));
    }

    /**
     * Same as {@link #correct(JsonElement, ValueMapper, Function)} but with an extractor that returns
     * an {@link Optional}. It should return an empty optional to mark that there's no default value available
     * or a filled optional with an appropriate default value.
     */
    <U> Optional<U> tryCorrect(@Nullable JsonElement json, ValueMapper<U, ?> mapper, Function<T, Optional<U>> extractor);

    /**
     * Gets a {@link Function} that first passes the argument to the given {@link Predicate} to check
     * whether a value is available. If a value is available, uses the given {@link Function} to compute
     * the result. If the predicate returns {@code false}, the returned {@link Optional} will be empty.
     */
    static <X, R> Function<X, Optional<R>> check(Predicate<X> present, Function<X, R> result) {
        return value -> present.test(value) ? Optional.ofNullable(result.apply(value)) : Optional.empty();
    }
}
