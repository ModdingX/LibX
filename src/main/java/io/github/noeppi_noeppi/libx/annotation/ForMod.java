package io.github.noeppi_noeppi.libx.annotation;

import io.github.noeppi_noeppi.libx.mod.ModX;

/**
 * Normally the code generator will infer the mod id and mod class from your `@Mod`
 * annotation. However, if you have multiple mods in the same project you need to put
 * `@ForMod` on classes or packages to tell the code generator to which mod a class
 * /package belongs. Any annotation closer to the element subject to code generation
 * will suppress all annotations further away.
 */
public @interface ForMod {
    
    Class<? extends ModX> value();
}
