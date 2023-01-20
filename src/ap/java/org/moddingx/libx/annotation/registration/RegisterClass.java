package org.moddingx.libx.annotation.registration;

import java.lang.annotation.*;

/**
 * Can be applied to a class to register all {@code public static final} fields to a {@code ModXRegistration}.
 * The target registry is set by the value from {@link #registry()}. Use an empty string for no registry at all.
 * The registry is resolved by taking the value from {@link #registryClass()} and looking for a
 * {@code public static final} field with the name from {@link #registry()} that holds a
 * {@code ResourceKey<Registry<T>>} that links to the registry to use. If {@link #registryClass()} is not given,
 * {@code ForgeRegistries.Keys} and {@code Registries} are searched.
 * 
 * To ignore a field add {@link Reg.Exclude @Exclude} to it.
 * 
 * By default the name from the field is taken and translated to snake case. To explicitly set a name use
 * {@link Reg.Name @Name}.
 *
 * 
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Documented
public @interface RegisterClass {

    String registry();
    
    Class<?> registryClass() default void.class;
    
    /**
     * Higher priorities are registered first.
     */
    int priority() default 0;

    /**
     * All elements from this class are prefixed with a special prefix. This way you can keep field names simple
     * and avoid duplicates.
     */
    String prefix() default "";
}
