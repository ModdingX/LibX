package org.moddingx.libx.test;

import org.junit.jupiter.api.Test;
import org.moddingx.libx.impl.reflect.ReflectionHacks;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class ReflectionHacksTest {
    
    public static final WrappedString staticField = new WrappedString("Hello, world!");
    
    @Test
    public void testReflectionHacks() throws Throwable {
        A a = new A("aaa", 42);
        B b = new B("bbb");
        
        ReflectionHacks.setFinalField(ReflectionHacksTest.class.getField("staticField"), null, new WrappedString("Changed!"));
        assertEquals(new WrappedString("Changed!"), staticField, "Static final field was not changed.");
        
        ReflectionHacks.setFinalField(A.class.getField("field"), a, "Changed!");
        assertEquals("Changed!", a.field, "Final instance field was not changed.");

        ReflectionHacks.setFinalField(A.class.getField("primitive"), a, 17);
        assertEquals(17, a.primitive, "Primitive final instance field was not changed.");
        
        ReflectionHacks.setFinalField(A.class.getField("field"), a, null);
        assertNull(a.field, "Static final field was not changed.");
        
        assertThrows(NullPointerException.class, () -> ReflectionHacks.setFinalField(A.class.getField("field"), null, ""));
        assertThrows(IllegalArgumentException.class, () -> ReflectionHacks.setFinalField(A.class.getField("field"), b, ""));
        assertThrows(NullPointerException.class, () -> ReflectionHacks.setFinalField(A.class.getField("primitive"), a, null));
        assertThrows(ClassCastException.class, () -> ReflectionHacks.setFinalField(A.class.getField("field"), a, new Date()));
    }
    
    private static class A {
        
        public final String field;
        public final int primitive;

        private A(String field, int primitive) {
            this.field = field;
            this.primitive = primitive;
        }
    }
    
    private static class B {
        
        public final String field;

        private B(String field) {
            this.field = field;
        }
    }
    
    // Can't use strings in static final fields as they are inlined by the compiler.
    private record WrappedString(String value) {}
}
