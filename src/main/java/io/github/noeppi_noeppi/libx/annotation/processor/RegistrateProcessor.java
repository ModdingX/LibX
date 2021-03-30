package io.github.noeppi_noeppi.libx.annotation.processor;

import io.github.noeppi_noeppi.libx.mod.registration.ModXRegistration;
import io.github.noeppi_noeppi.libx.mod.registration.NoReg;
import io.github.noeppi_noeppi.libx.mod.registration.RegName;
import io.github.noeppi_noeppi.libx.mod.registration.Registrate;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegistrateProcessor extends Processor {

    @Override
    public Class<?>[] getTypes() {
        return new Class[]{ Registrate.class };
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<String, Set<String>> registrationClasses = new HashMap<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(Registrate.class)) {
            if (!(element instanceof QualifiedNameable)) {
                this.messager.printMessage(Diagnostic.Kind.ERROR, "Failed to get qualified name for element annotaed with @Registrate", element);
                continue;
            }
            if (!(element.getEnclosingElement() instanceof PackageElement)) {
                this.messager.printMessage(Diagnostic.Kind.ERROR, "Parent of element annotaed with @Registrate is not a package", element);
                continue;
            }
            Registrate registrate = element.getAnnotation(Registrate.class);
            Element modClass = this.types.asElement(this.classType(registrate::value));
            if (!(modClass instanceof QualifiedNameable)) {
                this.messager.printMessage(Diagnostic.Kind.ERROR, "Failed to get qualified name for mod class of mod specified in @Registrate", element);
                continue;
            }
            String modid = this.modidFromAnnotation(modClass);
            if (modid == null) {
                this.messager.printMessage(Diagnostic.Kind.ERROR, "Mod class of @Registrate is not annotated with @Mod", element);
                continue;
            }
            List<RegistrationEntry> entries = element.getEnclosedElements().stream().flatMap(this::fromElement).collect(Collectors.toList());
            try {
                JavaFileObject file = this.filer.createSourceFile(((PackageElement) element.getEnclosingElement()).getQualifiedName() + "." + element.getSimpleName() + "$Registrate", element);
                Writer writer = file.openWriter();
                writer.write("package " + ((PackageElement) element.getEnclosingElement()).getQualifiedName() + ";");
                writer.write("public class " + element.getSimpleName() + "$Registrate{");
                writer.write("public static void init(){");
                writer.write(ModXRegistration.class.getCanonicalName() + " mod=(" + ModXRegistration.class.getCanonicalName() + ")net.minecraftforge.fml.ModList.get().getModObjectById(\"" + modid + "\").get();");
                for (RegistrationEntry entry : entries) {
                    writer.write("mod.register(\"" + entry.registryName + "\"," + entry.fqn + ");");
                }
                writer.write("}");
                writer.write("}\n");
                writer.close();
            } catch (IOException e) {
                this.messager.printMessage(Diagnostic.Kind.ERROR, "Failed to generate registrating source code: " + e, element);
            }
            if (!registrationClasses.containsKey(((QualifiedNameable) modClass).getQualifiedName().toString())) {
                registrationClasses.put(((QualifiedNameable) modClass).getQualifiedName().toString(), new HashSet<>());
            }
            Set<String> set = registrationClasses.get(((QualifiedNameable) modClass).getQualifiedName().toString());
            set.add(((PackageElement) element.getEnclosingElement()).getQualifiedName() + "." + element.getSimpleName() + "$Registrate");
        }
        try {
            for (Map.Entry<String, Set<String>> entry : registrationClasses.entrySet()) {
                FileObject file = this.filer.createResource(StandardLocation.CLASS_OUTPUT, "libx.register", entry.getKey());
                Writer writer = file.openWriter();
                for (String clazz : entry.getValue()) {
                    writer.write(clazz + "\n");
                }
                writer.close();
            }
        } catch (IOException e) {
            this.messager.printMessage(Diagnostic.Kind.ERROR, "Failed to generate files to store location of registration code.");
        }
        return true;
    }

    private String modidFromAnnotation(Element element) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (this.sameErasure(this.elements.getTypeElement("net.minecraftforge.fml.common.Mod").asType(), mirror.getAnnotationType())) {
                //noinspection OptionalGetWithoutIsPresent
                return mirror.getElementValues().entrySet().stream()
                        .filter(e -> e.getKey().getSimpleName().contentEquals("value"))
                        .findFirst().map(Map.Entry::getValue)
                        .map(v -> v.getValue().toString()).get();
            }
        }
        return null;
    }

    private Stream<RegistrationEntry> fromElement(Element element) {
        if (element.getKind() != ElementKind.FIELD || element.getAnnotation(NoReg.class) != null) {
            return Stream.empty();
        } else if (!(element.getEnclosingElement() instanceof QualifiedNameable)) {
            this.messager.printMessage(Diagnostic.Kind.ERROR, "Failed to get qualified name for member: " + element, element.getEnclosingElement());
            return Stream.empty();
        } else {
            if (!element.getModifiers().contains(Modifier.STATIC)) {
                this.messager.printMessage(Diagnostic.Kind.WARNING, "Skipping non-static member for automatic registration. Use @NoReg to suppress.", element);
                return Stream.empty();
            }
            if (!element.getModifiers().contains(Modifier.FINAL)) {
                this.messager.printMessage(Diagnostic.Kind.WARNING, "Skipping non-static member for automatic registration. Use @NoReg to suppress.", element);
                return Stream.empty();
            }
            if (!element.getModifiers().contains(Modifier.PUBLIC) && !element.getModifiers().contains(Modifier.PRIVATE)) {
                this.messager.printMessage(Diagnostic.Kind.WARNING, "Skipping non-public and non-private member for automatic registration. Use @NoReg to suppress.", element);
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

    private static class RegistrationEntry {

        public final String registryName;
        public final String fqn;

        public RegistrationEntry(String registryName, String fqn) {
            this.registryName = registryName;
            this.fqn = fqn;
        }
    }
}
