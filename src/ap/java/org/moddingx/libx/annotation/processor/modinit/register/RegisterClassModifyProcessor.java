package org.moddingx.libx.annotation.processor.modinit.register;

import org.moddingx.libx.annotation.processor.Classes;
import org.moddingx.libx.annotation.processor.Processor;
import org.moddingx.libx.annotation.registration.PlainRegisterable;
import org.moddingx.libx.annotation.registration.Reg;
import org.moddingx.libx.annotation.registration.RegisterClass;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.stream.Stream;

public class RegisterClassModifyProcessor extends Processor {

    private static final Set<Class<? extends Annotation>> MODIFY_ANNOTATIONS = Set.of(Reg.Exclude.class, Reg.Name.class);
    
    @Override
    public Class<?>[] getTypes() {
        return Stream.concat(Stream.of(Reg.class, PlainRegisterable.class), MODIFY_ANNOTATIONS.stream()).toArray(Class[]::new);
    }

    @Override
    public void run(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Reg.class)) {
            this.messager().printMessage(Diagnostic.Kind.ERROR, "@Reg is a container annotation an can't be directly used on elements.", element);
        }
        for (Class<? extends Annotation> modifyClass : MODIFY_ANNOTATIONS) {
            for (Element element : roundEnv.getElementsAnnotatedWith(modifyClass)) {
                if (element.getKind() != ElementKind.FIELD || !element.getModifiers().contains(Modifier.PUBLIC)
                        || !element.getModifiers().contains(Modifier.STATIC) || !element.getModifiers().contains(Modifier.FINAL)) {
                    this.messager().printMessage(Diagnostic.Kind.ERROR, "@" + modifyClass.getSimpleName() + " can only be used on public static final fields.", element);
                } else if (element.getEnclosingElement().getKind() != ElementKind.CLASS || element.getEnclosingElement().getAnnotation(RegisterClass.class) == null) {
                    this.messager().printMessage(Diagnostic.Kind.ERROR, "@" + modifyClass.getSimpleName() + " can only be used on in classes annotated with @RegisterClass.", element);
                } else if (modifyClass != Reg.Exclude.class && element.getAnnotation(Reg.Exclude.class) != null) {
                    this.messager().printMessage(Diagnostic.Kind.ERROR, "@" + modifyClass.getSimpleName() + " can't be used on fields excluded from registration.", element);
                }
            }
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(PlainRegisterable.class)) {
            if (element.getKind() == ElementKind.INTERFACE || element.getKind() == ElementKind.ANNOTATION_TYPE) {
                this.messager().printMessage(Diagnostic.Kind.ERROR, "@PlainRegisterable is disallowed on interfaces.", element);
            } else if (element.getKind() != ElementKind.CLASS && element.getKind() != ElementKind.RECORD) {
                this.messager().printMessage(Diagnostic.Kind.ERROR, "@PlainRegisterable can only be added to classes.", element);
            } else if (element instanceof TypeElement typeElement && !this.subTypeErasure(typeElement.asType(), this.forClass(Classes.REGISTERABLE))) {
                this.messager().printMessage(Diagnostic.Kind.WARNING, "Class annotated with @PlainRegisterable does not implement Registerable.", element);
            }
        }
    }
}
