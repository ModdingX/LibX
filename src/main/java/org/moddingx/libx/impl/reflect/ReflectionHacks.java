package org.moddingx.libx.impl.reflect;

import org.moddingx.libx.util.lazy.LazyValue;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class ReflectionHacks {
    
    private static final LazyValue<Unsafe> unsafe = new LazyValue<>(() -> {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (Exception e) {
            throw new IllegalStateException("ReflectionHacks: Couldn't get the Unsafe.", e);
        }
    });
    
    public static void throwUnchecked(Throwable t) {
        unsafe.get().throwException(t);
    }
    
    public static <T> T newInstance(Class<T> cls) throws InstantiationException {
        //noinspection unchecked
        return (T) unsafe.get().allocateInstance(cls);
    }
}
