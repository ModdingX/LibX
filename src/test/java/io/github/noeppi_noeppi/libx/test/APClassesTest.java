package io.github.noeppi_noeppi.libx.test;

import io.github.noeppi_noeppi.libx.annotation.processor.Classes;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class APClassesTest {
    
    @Test
    public void testClasses() throws Throwable {
        for (Field field : Classes.class.getDeclaredFields()) {
            if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isStatic(field.getModifiers()) || !Modifier.isFinal(field.getModifiers())) {
                fail("Classes may only contain public static final fields: " + field.getName());
            } else if (field.getType() == String.class) {
                assertDoesNotThrow(() -> Class.forName((String) field.get(null), false, ClassLoader.getSystemClassLoader()), "Class not found: " + field.getName() + ": " + field.get(null));
            } else if (Collection.class.isAssignableFrom(field.getType())) {
                for (Object obj : (Collection<?>) field.get(null)) {
                    if (obj instanceof String str) {
                        assertDoesNotThrow(() -> Class.forName(str, false, ClassLoader.getSystemClassLoader()), "Class not found: " + field.getName() + ": " + str);
                    } else {
                        fail("Collection fields in the Classes class may only hold string values: " + field.getName());
                    }
                }
            } else {
                fail("Fields in the Classes class must be of type String or a collection type: " + field.getName());
            }
        }
        
        if (Classes.class.getDeclaredMethods().length != 0) {
            fail("Classes class may not define any methods.");
        }
    }
}
