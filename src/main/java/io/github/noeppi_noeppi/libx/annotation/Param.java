package io.github.noeppi_noeppi.libx.annotation;

import java.lang.annotation.*;

/**
 * Can be used to specify the class and field where to find codecs.
 * There's no need for this if either DFU provides a codec, there's
 * a {@code public static final} field named {@code CODEC} in the class
 * of the parameter type exists or {@code CodecHolder} has been used with
 * the parameter type.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
@Documented
public @interface Param {

    /**
     * Class where to find the codec field.
     */
    Class<?> value() default void.class;

    /**
     * Field name of the codec field.
     */
    String field() default "CODEC";
}
