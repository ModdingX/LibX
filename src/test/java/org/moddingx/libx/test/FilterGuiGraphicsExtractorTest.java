package org.moddingx.libx.test;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.neoforged.neoforge.client.extensions.GuiGraphicsExtractorExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.moddingx.libx.render.FilterGuiGraphicsExtractor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class FilterGuiGraphicsExtractorTest {

    @Test
    public void testThatAllMethodsAreOverridden() {
        List<Method> methodsToOverride = Stream.of(GuiGraphicsExtractor.class, GuiGraphicsExtractorExtension.class)
                .flatMap(cls -> Arrays.stream(cls.getDeclaredMethods()))
                .filter(m -> Modifier.isPublic(m.getModifiers()) || Modifier.isProtected(m.getModifiers()))
                .filter(m -> !Modifier.isStatic(m.getModifiers())) // Do not filter final methods, these need to be ATed
                .toList();
        for (Method m : methodsToOverride) {
            try {
                if (FilterGuiGraphicsExtractor.class.getDeclaredMethod(m.getName(), m.getParameterTypes()).getDeclaringClass() != FilterGuiGraphicsExtractor.class) {
                    Assertions.fail("Method should be overridden: " + m);
                }
            } catch (NoSuchMethodException e) {
                Assertions.fail("Method should be overridden: " + m);
            }
        }
    }
}
