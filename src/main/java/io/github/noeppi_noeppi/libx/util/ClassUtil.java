package io.github.noeppi_noeppi.libx.util;

import javax.annotation.Nullable;

/**
 * Utilities for instances of the {@link Class} class.
 */
public class ClassUtil {

    private static final StackWalker STACK = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    
    /**
     * Returns the given class unless it's a primitive class in which
     * case the boxed class for that primitive is returned.
     */
    public static Class<?> boxed(Class<?> clazz) {
        if (boolean.class.equals(clazz)) {
            return Boolean.class;
        } else if (byte.class.equals(clazz)) {
            return Byte.class;
        } else if (char.class.equals(clazz)) {
            return Character.class;
        } else if (short.class.equals(clazz)) {
            return Short.class;
        } else if (int.class.equals(clazz)) {
            return Integer.class;
        } else if (long.class.equals(clazz)) {
            return Long.class;
        } else if (float.class.equals(clazz)) {
            return Float.class;
        } else if (double.class.equals(clazz)) {
            return Double.class;
        } else if (void.class.equals(clazz)) {
            return Void.class;
        } else {
            return clazz;
        }
    }
    
    /**
     * Returns the given class unless it's a boxed primitive class in which
     * case the primitive class for that boxed class is returned.
     */
    public static Class<?> unboxed(Class<?> clazz) {
        if (Boolean.class.equals(clazz)) {
            return boolean.class;
        } else if (Byte.class.equals(clazz)) {
            return byte.class;
        } else if (Character.class.equals(clazz)) {
            return char.class;
        } else if (Short.class.equals(clazz)) {
            return short.class;
        } else if (Integer.class.equals(clazz)) {
            return int.class;
        } else if (Long.class.equals(clazz)) {
            return long.class;
        } else if (Float.class.equals(clazz)) {
            return float.class;
        } else if (Double.class.equals(clazz)) {
            return double.class;
        } else if (Void.class.equals(clazz)) {
            return void.class;
        } else {
            return clazz;
        }
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
}
