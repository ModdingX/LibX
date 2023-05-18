package org.moddingx.libx.annotation;

import net.minecraftforge.fml.common.Mod;
import org.moddingx.libx.mod.ModX;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// Not in the ap source set as it needs the ModX type bound.
/**
 * Normally ModInit will infer the mod id and mod class from your {@link Mod @Mod}
 * annotation. However, if you have multiple mods in the same project, you need to put
 * {@code @ForMod} on classes or packages to tell ModInit to which mod a class or
 * package belongs. Any annotation closer to the element subject to code generation
 * will suppress all annotations further away.
 */
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface ForMod {
    
    Class<? extends ModX> value();
}
