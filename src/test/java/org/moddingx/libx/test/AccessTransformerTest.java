package org.moddingx.libx.test;

import org.junit.jupiter.api.Test;
import org.moddingx.libx.LibX;
import org.objectweb.asm.Type;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class AccessTransformerTest {
    
    private static final String ACCESS = Pattern.compile("(?:public|protected|default|private)(?:\\+f|-f)?").pattern();
    private static final String IDENT = Pattern.compile("[\\w$]+").pattern();
    private static final String CLASS_NAME = Pattern.compile(IDENT + "(?:\\." + IDENT + ")*").pattern();
    
    private static final Pattern CLASS_PATTERN = Pattern.compile("(?U)" + ACCESS + "\\s+(" + CLASS_NAME + ")");
    private static final Pattern FIELD_PATTERN = Pattern.compile("(?U)" + ACCESS + "\\s+(" + CLASS_NAME + ")\\s+(" + IDENT + ")");
    private static final Pattern CTOR_PATTERN = Pattern.compile("(?U)" + ACCESS + "\\s+(" + CLASS_NAME + ")\\s+<init>\\s*(.+?)");
    private static final Pattern METHOD_PATTERN = Pattern.compile("(?U)" + ACCESS + "\\s+(" + CLASS_NAME + ")\\s+(" + IDENT + ")\\s*(.+?)");
    
    @Test
    public void testAT() throws Throwable {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(Objects.requireNonNull(LibX.class.getResourceAsStream("/META-INF/accesstransformer.cfg"), "accesstransformer.cfg not found")))) {
            List<String> atLines = in.lines().map(String::strip).filter(str -> !str.isEmpty() && !str.startsWith("#")).toList();
            for (String line : atLines) {
                Matcher m = CLASS_PATTERN.matcher(line);
                if (m.matches()) {
                    this.testClass(m.group(1));
                    continue;
                }
                m = FIELD_PATTERN.matcher(line);
                if (m.matches()) {
                    this.testField(m.group(1), m.group(2));
                    continue;
                }
                m = CTOR_PATTERN.matcher(line);
                if (m.matches()) {
                    this.testConstructor(m.group(1), m.group(2));
                    continue;
                }
                m = METHOD_PATTERN.matcher(line);
                if (m.matches()) {
                    this.testMethod(m.group(1), m.group(2), m.group(3));
                    continue;
                }
                fail("AccessTransformer line matches no pattern. Is the comment with the mapped name missing? " + line);
            }
        }
    }
    
    private Class<?> testClass(String name) {
        return assertDoesNotThrow(() -> Class.forName(name, false, ClassLoader.getSystemClassLoader()), "Class in AccessTransformer not found: " + name);
    }
    
    private void testField(String clsName, String name) {
        Class<?> cls = this.testClass(clsName);
        assertDoesNotThrow(() -> cls.getDeclaredField(name), "Unknown field in accesstransformer: " + clsName + "#" + name);
    }
    
    private void testConstructor(String clsName, String descriptor) {
        Class<?> cls = this.testClass(clsName);
        Class<?>[] args = this.resolveArgs(descriptor);
        assertDoesNotThrow(() -> cls.getDeclaredConstructor(args), "Unknown constructor in accesstransformer: " + clsName + "#<init>" + descriptor + " (<init>)");
    }
    
    private void testMethod(String clsName, String name, String descriptor) {
        Class<?> cls = this.testClass(clsName);
        Class<?>[] args = this.resolveArgs(descriptor);
        assertDoesNotThrow(() -> cls.getDeclaredMethod(name, args), "Unknown method in accesstransformer: " + clsName + "#" + name + descriptor);
    }
    
    private Class<?>[] resolveArgs(String descriptor) {
        Type methodType = Type.getMethodType(descriptor);
        Type[] argTypes = methodType.getArgumentTypes();
        Class<?>[] args = new Class<?>[argTypes.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = this.resolveClassType(argTypes[i]);
        }
        return args;
    }
    
    private Class<?> resolveClassType(Type type) {
        return switch (type.getSort()) {
            case Type.ARRAY -> {
                int dim = type.getDimensions();
                Class<?> base = this.resolveClassType(type.getElementType());
                for (int i = 0; i < dim; i++) base = base.arrayType();
                yield base;
            }
            case Type.OBJECT -> {
                assertFalse(type.getInternalName().contains("."), "Invalid class name in method descriptor: " + type.getInternalName());
                yield this.testClass(type.getClassName());
            }
            case Type.BOOLEAN -> boolean.class;
            case Type.BYTE -> byte.class;
            case Type.CHAR -> char.class;
            case Type.SHORT -> short.class;
            case Type.INT -> int.class;
            case Type.LONG -> long.class;
            case Type.FLOAT -> float.class;
            case Type.DOUBLE -> double.class;
            default -> fail("Unknown type in method descriptor: " + type);
        };
    }
}
