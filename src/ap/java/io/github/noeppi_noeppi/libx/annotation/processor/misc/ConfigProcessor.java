package io.github.noeppi_noeppi.libx.annotation.processor.misc;

import io.github.noeppi_noeppi.libx.annotation.processor.Classes;
import io.github.noeppi_noeppi.libx.annotation.processor.Processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigProcessor extends Processor {

    private Set<TypeMirror> wrapperTypes;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        this.wrapperTypes = Set.of(
                this.forClass(Boolean.class),
                this.forClass(Byte.class),
                this.forClass(Character.class),
                this.forClass(Short.class),
                this.forClass(Integer.class),
                this.forClass(Long.class),
                this.forClass(Float.class),
                this.forClass(Double.class)
        );
    }

    @Override
    public Class<?>[] getTypes() {
        return new Class<?>[]{};
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>(super.getSupportedAnnotationTypes());
        set.add(Classes.CONFIG);
        return set;
    }

    @Override
    public void run(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(this.elements().getTypeElement(Classes.CONFIG))) {
            if (this.isSuppressed(element, "config")) continue;

            if (element.getKind() != ElementKind.FIELD || !element.getModifiers().contains(Modifier.STATIC) ||
                    !element.getModifiers().contains(Modifier.PUBLIC) || element.getModifiers().contains(Modifier.FINAL)) {
                this.messager().printMessage(Diagnostic.Kind.ERROR, "Only public static non-final fields can be annotated with @Config", element);
                continue;
            }

            TypeMirror firstGeneric = this.forClass(String.class);
            if (element.asType().getKind() == TypeKind.DECLARED && element.asType() instanceof DeclaredType declared) {
                List<? extends TypeMirror> parameters = declared.getTypeArguments();
                if (!parameters.isEmpty()) {
                    firstGeneric = parameters.get(0);
                }
            }
            
            for (TypeMirror wrapper : this.wrapperTypes) {
                if (this.sameErasure(element.asType(), wrapper)) {
                    this.messager().printMessage(Diagnostic.Kind.WARNING, "Unchecked @Config: Config should use primitive instead of wrapper type.", element);
                    break;
                }
            }
            
            if (this.sameErasure(this.forClass(Map.class), element.asType())
                    && !this.sameErasure(firstGeneric, this.forClass(String.class))
                    && !this.isSuppressed(element, "unchecked")) {
                this.messager().printMessage(Diagnostic.Kind.WARNING, "Unchecked @Config: Map required keys of type String.", element);
            }
        }
    }
}
