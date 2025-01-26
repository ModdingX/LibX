package org.moddingx.libx.test;

import org.junit.jupiter.api.Test;
import org.moddingx.libx.impl.reflect.ReflectionHacks;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ReflectionHacksTest {
    
    public static final WrappedString staticField = new WrappedString("Hello, world!");
    
    @Test
    public void testReflectionHacks() {
        assertThrows(IOException.class, () -> ReflectionHacks.throwUnchecked(new IOException()), "ReflectionHacks.throwUnchecked did not throw an IOException");

        ClassWithThrowingConstructor emptyInstance = assertDoesNotThrow(() -> ReflectionHacks.newInstance(ClassWithThrowingConstructor.class), "ReflectionHacks.newInstance threw an exception");
        //noinspection DataFlowIssue
        assertNull(emptyInstance.value, "ReflectionHacks.newInstance did not produce an empty object");
    }
    
    // Can't use strings in static final fields as they are inlined by the compiler.
    public record WrappedString(String value) {}

    public static class ClassWithThrowingConstructor {
        
        public final WrappedString value = new WrappedString("");
        
        public ClassWithThrowingConstructor() {
            throw new IllegalStateException("ReflectionHacks.newInstance called the constructor.");
        }
    }
}
