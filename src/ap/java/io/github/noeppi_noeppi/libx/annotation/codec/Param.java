package io.github.noeppi_noeppi.libx.annotation.codec;

import java.lang.annotation.*;

/**
 * The parameter codec field type will try to store and load a parameter based on a
 * different codec. For that it needs to find a matching codec to use.
 * 
 * It will search  for them in this order by default:
 * 
 * <ul>
 *     <li>Look for a default codec provided by DataFixerUpper</li>
 *     <li>Try to find a matching {@code public static} field in the class of the
 *     parameter type with the name {@code CODEC} or {@code DIRECT_CODEC}.</li>
 * </ul>
 * 
 * You can customise the class and field name to look after a codec using this
 * annotation. By this you can create default values easily by creating a class
 * with a field named {@code CODEC} that holds a codec for a value and adds a
 * default to it.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Documented
public @interface Param {

    /**
     * Class where to find the codec field. Can be left empty to use the class of
     * the parameter type.
     */
    Class<?> value() default void.class;

    /**
     * Field name of the codec field. Can be left empty to use the default value
     * ModInit looks for.
     */
    String field() default "";
}
