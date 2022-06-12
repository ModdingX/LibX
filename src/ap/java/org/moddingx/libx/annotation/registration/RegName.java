package org.moddingx.libx.annotation.registration;

import org.moddingx.libx.annotation.meta.RemoveIn;

import java.lang.annotation.*;

/**
 * Set a custom name for automatic registration.
 *
 * @deprecated See https://gist.github.com/noeppi-noeppi/9de9b6af950ee02f2dee611742fe2d6d
 */
@Deprecated(forRemoval = true)
@RemoveIn(minecraft = "1.19")
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@Documented
public @interface RegName {

    String value();
}
