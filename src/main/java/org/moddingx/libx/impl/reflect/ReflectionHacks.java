package org.moddingx.libx.impl.reflect;

import org.moddingx.libx.util.lazy.LazyValue;
import sun.misc.Unsafe;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

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
    
    public static void setFinalField(Field field, Object instance, @Nullable Object value) {
        Object base;
        long offset;
        if (Modifier.isStatic(field.getModifiers())) {
            base = unsafe.get().staticFieldBase(field);
            offset = unsafe.get().staticFieldOffset(field);
        } else {
            if (instance == null) {
                throw new NullPointerException("No instance for non-static field: " + field);
            } else if (!field.getDeclaringClass().isAssignableFrom(instance.getClass())) {
                throw new IllegalArgumentException("Instance has wrong type for field: " + instance + " " + field);
            } else {
                base = instance;
                offset = unsafe.get().objectFieldOffset(field);
            }
        }
        if (field.getType() == void.class) {
            throw new IllegalStateException("Field with void type");
        } else if (field.isEnumConstant()) {
            throw new IllegalStateException("Can't change enum field");
        } else if (field.getType() == boolean.class) {
            if (value == null) throw new NullPointerException("Null primitive for field: " + field);
            unsafe.get().putBooleanVolatile(base, offset, (Boolean) value);
        } else if (field.getType() == byte.class) {
            if (value == null) throw new NullPointerException("Null primitive for field: " + field);
            unsafe.get().putByteVolatile(base, offset, (Byte) value);
        } else if (field.getType() == char.class) {
            if (value == null) throw new NullPointerException("Null primitive for field: " + field);
            unsafe.get().putCharVolatile(base, offset, (Character) value);
        } else if (field.getType() == short.class) {
            if (value == null) throw new NullPointerException("Null primitive for field: " + field);
            unsafe.get().putShortVolatile(base, offset, (Short) value);
        } else if (field.getType() == int.class) {
            if (value == null) throw new NullPointerException("Null primitive for field: " + field);
            unsafe.get().putIntVolatile(base, offset, (Integer) value);
        } else if (field.getType() == long.class) {
            if (value == null) throw new NullPointerException("Null primitive for field: " + field);
            unsafe.get().putLongVolatile(base, offset, (Long) value);
        } else if (field.getType() == float.class) {
            if (value == null) throw new NullPointerException("Null primitive for field: " + field);
            unsafe.get().putFloatVolatile(base, offset, (Float) value);
        } else if (field.getType() == double.class) {
            if (value == null) throw new NullPointerException("Null primitive for field: " + field);
            unsafe.get().putDoubleVolatile(base, offset, (Double) value);
        } else if (value != null && !field.getType().isAssignableFrom(value.getClass())) {
            throw new ClassCastException("Expected value of type " + field.getType() + " for field " + field + ", got " + value.getClass());
        } else {
            unsafe.get().putObjectVolatile(base, offset, value);
        }
    }
}
