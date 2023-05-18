package org.moddingx.libx.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import cpw.mods.modlauncher.api.INameMappingService;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.moddingx.libx.annotation.meta.RemoveIn;

import javax.annotation.Nullable;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.Objects;

/**
 * Utilities for instances of the {@link Class} class.
 */
public class ClassUtil {

    private static final StackWalker STACK = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    
    private static final BiMap<Class<?>, Class<?>> BOXED = ImmutableBiMap.of(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            char.class, Character.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            void.class, Void.class
    );
    
    /**
     * Returns the given class unless it's a primitive class in which
     * case the boxed class for that primitive is returned.
     */
    public static Class<?> boxed(Class<?> cls) {
        return BOXED.getOrDefault(cls, cls);
    }
    
    /**
     * Returns the given class unless it's a boxed primitive class in which
     * case the primitive class for that boxed class is returned.
     */
    public static Class<?> unboxed(Class<?> cls) {
        return BOXED.inverse().getOrDefault(cls, cls);
    }

    /**
     * Same as {@link Class#forName(String)} but instead of throwing a {@link ClassNotFoundException},
     * returns {@code null}.
     */
    @Nullable
    public static Class<?> forName(String cls) {
        // As the simple forName is caller sensitive, we need to get the caller ourselves
        // and forward it to a call to the non-caller-sensitive method.
        Class<?> caller = STACK.getCallerClass();
        try {
            return Class.forName(cls, true, caller.getClassLoader());
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            return null;
        }
    }

    /**
     * Gets the caller class for method call, the given amount of stack frames up. This skips
     * reflection frames.
     * 
     * @param level How many stack frames this should go up. {@code 0} means the method that
     *              called {@link #callerClass(int)}. {@code 1} is the class that called the
     *              method from which {@link #callerClass(int)} was invoked.
     */
    @Nullable
    @SuppressWarnings("JavaDoc")
    public static Class<?> callerClass(int level) {
        if (level < 0) throw new IllegalArgumentException("Negative stack frame index");
        return STACK.walk(frames -> frames
                .map(StackWalker.StackFrame::getDeclaringClass)
                .skip(level + 1).findFirst()
        ).orElse(null);
    }

    /**
     * Gets whether the given class is found somewhere in the call hierarchy from this method.
     */
    @Deprecated(forRemoval = true)
    @RemoveIn(minecraft = "1.20")
    public static boolean calledBy(Class<?> cls) {
        return STACK.walk(frames -> frames
                .map(StackWalker.StackFrame::getDeclaringClass)
                .skip(1).filter(frame -> cls == frame)
                .findFirst()
        ).isPresent();
    }
    
    /**
     * Gets whether the given method is found somewhere in the call hierarchy from this method.
     */
    @Deprecated(forRemoval = true)
    @RemoveIn(minecraft = "1.20")
    public static boolean calledBy(Class<?> cls, String srg, Class<?>... methodArgs) {
        String mappedName = ObfuscationReflectionHelper.remapName(INameMappingService.Domain.METHOD, srg);
        return STACK.walk(frames -> frames
                .skip(1).filter(frame -> {
                    try {
                        if (cls != frame.getDeclaringClass()) return false;
                        if (!Objects.equals(mappedName, frame.getMethodName())) return false;
                        MethodType type = frame.getMethodType();
                        return Arrays.equals(methodArgs, type.parameterArray());
                    } catch (UnsupportedOperationException e) {
                        return false;
                    }
                })
                .findFirst()
        ).isPresent();
    }
}
