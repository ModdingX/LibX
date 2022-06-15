package org.moddingx.libx.config.validator;

import org.moddingx.libx.config.Config;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A config validator is used to read a custom validation annotation from a config field
 * and validate the value so it for example matches an int range.
 * Validators are applied when a config is read from a file. They are not applied if a
 * server sends a config and they'll not validate the default value.
 */
public interface ConfigValidator<T, A extends Annotation> {
    
    /**
     * Gets the class of the type that this validator can validate.
     */
    Class<T> type();
    
    /**
     * Gets the class of the annotation type used by this validator.
     */
    Class<A> annotation();

    /**
     * Validates a value. For a correct value this should return an empty Optional.
     * For an incorrect value this should return an Optional containing the corrected value.
     */
    Optional<T> validate(T value, A validator);
    
    /**
     * Returns a list of comment lines that will be added to the values specified in {@link Config @Config}.
     */
    default List<String> comment(A validator) {
        return Collections.emptyList();
    }
}
