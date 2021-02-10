package io.github.noeppi_noeppi.libx.util;

public class ClassUtil {

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
}
