package org.moddingx.libx.annotation.codec;

import java.lang.annotation.*;

/**
 * The dynamic codec field type will create a map codec from a static method
 * 
 * The method is defined through the fields defined in the annotation.
 * 
 * The method must return a map codec with a matching type. As arguments, it
 * must accept a {@link String} which defines the field name.
 * 
 * For example using {@code @Dynamic(WorldSeedHolder.class)} on a parameter of type
 * {@link Long long} will get you a field codec for a long that defaults to the
 * current world seed.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Documented
public @interface Dynamic {

    /**
     * Class where to find the codec factory. Can be left empty to use the class of
     * the parameter type.
     */
    Class<?> value() default void.class;

    /**
     * Method name of the factory method. Can be left empty to use the default value: {@code fieldOf}
     */
    String factory() default "";
}
