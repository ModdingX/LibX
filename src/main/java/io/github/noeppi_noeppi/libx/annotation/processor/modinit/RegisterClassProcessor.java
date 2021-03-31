package io.github.noeppi_noeppi.libx.annotation.processor.modinit;

import io.github.noeppi_noeppi.libx.annotation.NoReg;
import io.github.noeppi_noeppi.libx.annotation.RegName;
import io.github.noeppi_noeppi.libx.annotation.RegisterClass;

import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegisterClassProcessor {

    public static void processRegisterClass(Element element, ModEnv env) {
        if (!(element instanceof QualifiedNameable)) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Failed to get qualified name for element annotaed with @RegisterClass", element);
            return;
        }
        if (!(element.getEnclosingElement() instanceof PackageElement)) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Parent of element annotated with @RegisterClass is not a package", element);
            return;
        }
        RegisterClass registerClass = element.getAnnotation(RegisterClass.class);
        ModInit mod = env.getMod(element);
        List<RegistrationEntry> entries = element.getEnclosedElements().stream().flatMap(e -> fromElement(e, env)).collect(Collectors.toList());
        mod.addRegistration(registerClass.priority(), entries);
    }
    
    private static Stream<RegistrationEntry> fromElement(Element element, ModEnv env) {
        if (element.getKind() != ElementKind.FIELD || element.getAnnotation(NoReg.class) != null) {
            return Stream.empty();
        } else if (!(element.getEnclosingElement() instanceof QualifiedNameable)) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Failed to get qualified name for member: " + element, element.getEnclosingElement());
            return Stream.empty();
        } else {
            if (!element.getModifiers().contains(Modifier.STATIC)) {
                env.messager().printMessage(Diagnostic.Kind.WARNING, "Skipping non-static member for automatic registration. Use @NoReg to suppress.", element);
                return Stream.empty();
            }
            if (!element.getModifiers().contains(Modifier.FINAL)) {
                env.messager().printMessage(Diagnostic.Kind.WARNING, "Skipping non-static member for automatic registration. Use @NoReg to suppress.", element);
                return Stream.empty();
            }
            if (!element.getModifiers().contains(Modifier.PUBLIC) && !element.getModifiers().contains(Modifier.PRIVATE)) {
                env.messager().printMessage(Diagnostic.Kind.WARNING, "Skipping non-public and non-private member for automatic registration. Use @NoReg to suppress.", element);
                return Stream.empty();
            }
            String registryName;
            if (element.getAnnotation(RegName.class) != null) {
                registryName = element.getAnnotation(RegName.class).value();
            } else {
                StringBuilder sb = new StringBuilder();
                for (char chr : element.getSimpleName().toString().toCharArray()) {
                    if (Character.isUpperCase(chr)) {
                        sb.append('_');
                    }
                    sb.append(Character.toLowerCase(chr));
                }
                registryName = sb.toString();
            }
            String fqn = ((QualifiedNameable) element.getEnclosingElement()).getQualifiedName() + "." + element.getSimpleName();
            return Stream.of(new RegistrationEntry(registryName, fqn));
        }
    }
}
