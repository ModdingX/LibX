package io.github.noeppi_noeppi.libx.annotation.processor;

import io.github.noeppi_noeppi.libx.config.Config;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.*;

public class ConfigProcessor extends Processor {

    private Set<TypeMirror> wrapperTypes;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        
        Set<TypeMirror> wrapperTypesMutable = new HashSet<>();
        wrapperTypesMutable.add(this.forClass(Boolean.class));
        wrapperTypesMutable.add(this.forClass(Byte.class));
        wrapperTypesMutable.add(this.forClass(Character.class));
        wrapperTypesMutable.add(this.forClass(Short.class));
        wrapperTypesMutable.add(this.forClass(Integer.class));
        wrapperTypesMutable.add(this.forClass(Long.class));
        wrapperTypesMutable.add(this.forClass(Float.class));
        wrapperTypesMutable.add(this.forClass(Double.class));
        this.wrapperTypes = Collections.unmodifiableSet(wrapperTypesMutable);
    }

    @Override
    public Class<?>[] getTypes() {
        return new Class<?>[]{ Config.class };
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Config.class)) {
            if (this.isSuppressed(element, "config")) continue;

            if (element.getKind() != ElementKind.FIELD || !element.getModifiers().contains(Modifier.STATIC) ||
                    !element.getModifiers().contains(Modifier.PUBLIC) || element.getModifiers().contains(Modifier.FINAL)) {
                this.messager.printMessage(Diagnostic.Kind.ERROR, "Only public static non-final fields can be annotated with @Config", element);
                continue;
            }

            TypeMirror firstGeneric = this.forClass(String.class);
            if (element.asType() instanceof DeclaredType) {
                List<? extends TypeMirror> parameters = ((DeclaredType) element.asType()).getTypeArguments();
                if (!parameters.isEmpty()) {
                    firstGeneric = parameters.get(0);
                }
            }
            
            for (TypeMirror wrapper : this.wrapperTypes) {
                if (this.sameErasure(element.asType(), wrapper)) {
                    this.messager.printMessage(Diagnostic.Kind.WARNING, "Unchecked @Config: Config should use primitive instead of wrapper type.", element);
                    break;
                }
            }
            
            if (this.sameErasure(this.forClass(Map.class), element.asType())
                    && !this.sameErasure(firstGeneric, this.forClass(String.class))
                    && !this.isSuppressed(element, "unchecked")) {
                this.messager.printMessage(Diagnostic.Kind.WARNING, "Unchecked @Config: Map required keys of type String.", element);
            }
        }
        return true;
    }
}
