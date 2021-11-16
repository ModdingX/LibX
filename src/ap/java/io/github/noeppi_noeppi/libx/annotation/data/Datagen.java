package io.github.noeppi_noeppi.libx.annotation.data;

import java.lang.annotation.*;

/**
 * Registers a class as a data provider to be used in datagen. A class annotated with this must
 * extend {@code DataProvider} and define exactly one constructor that must be {@code public}.
 * The arguments of the constructor must all be from the list below. Each argument type is optional.
 * Also the order of the arguments does not matter.
 * 
 * Allowed argument types for the constructor:
 * 
 * <li>
 *     <ul>{@code ModX}</ul>
 *     <ul>{@code DataProvider}</ul>
 *     <ul>{@code ExistingFileHelper}</ul>
 * </li>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Documented
public @interface Datagen {
    
}
