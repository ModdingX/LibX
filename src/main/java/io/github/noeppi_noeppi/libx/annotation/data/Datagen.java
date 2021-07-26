package io.github.noeppi_noeppi.libx.annotation.data;

import io.github.noeppi_noeppi.libx.mod.ModX;
import net.minecraft.data.DataProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.lang.annotation.*;

/**
 * Registers a class as a data provider to be used in datagen. A class annotated with this must
 * extend {@link DataProvider} and define exactly one constructor that must be {@code public}.
 * The arguments of the constructor must all be from the list below. Each argument type is optional.
 * Also the order of the arguments does not matter.
 * 
 * Allowed argument types for the constructor:
 * 
 * <li>
 *     <ul>{@link ModX}</ul>
 *     <ul>{@link DataProvider}</ul>
 *     <ul>{@link ExistingFileHelper}</ul>
 * </li>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Documented
public @interface Datagen {
    
}
