package io.github.noeppi_noeppi.libx.test;

import io.github.noeppi_noeppi.libx.annotation.processor.Classes;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

// Tests all fields from the Classes class in the AP
// that the classes exist and are valid.
public class APClassesTest {
    
    @Test
    public void testClasses() throws Throwable {
        Method srcNameMethod = assertDoesNotThrow(() -> Classes.class.getMethod("sourceName", String.class), "sourceName method not defined in Classes class");
        
        for (Field field : Classes.class.getDeclaredFields()) {
            if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isStatic(field.getModifiers()) || !Modifier.isFinal(field.getModifiers())) {
                fail("The Classes class may only contain public static final fields: " + field.getName());
            } else if (field.getType() == String.class) {
                testClass((String) field.get(null), "Class not found: " + field.getName(), srcNameMethod);
            } else if (Collection.class.isAssignableFrom(field.getType())) {
                for (Object obj : (Collection<?>) field.get(null)) {
                    if (obj instanceof String str) {
                        testClass(str, "Class not found: " + field.getName(), srcNameMethod);
                    } else {
                        fail("Collection fields in the Classes class may only hold string values: " + field.getName());
                    }
                }
            } else {
                fail("Fields in the Classes class must be of type String or a collection type: " + field.getName());
            }
        }
        
        if (Classes.class.getDeclaredMethods().length != 1) {
            fail("Classes class may not define any additional methods.");
        }
    }
    
    private static void testClass(String cls, String msg, Method srcNameMethod) {
        Class<?> inst = assertDoesNotThrow(() -> Class.forName(cls, false, ClassLoader.getSystemClassLoader()), msg + ": " + cls);
        String srcName = assertDoesNotThrow(() -> (String) srcNameMethod.invoke(null, cls), "sourceName failed.");
        assertEquals(inst.getCanonicalName(), srcName, "sourceName produced the wrong result for " + cls + ", expected " + inst.getCanonicalName());
    }
}
