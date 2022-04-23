package io.github.noeppi_noeppi.libx.annotation.processor.meta;

import io.github.noeppi_noeppi.libx.annotation.meta.SuperChainRequired;
import io.github.noeppi_noeppi.libx.annotation.processor.Classes;
import io.github.noeppi_noeppi.libx.annotation.processor.Processor;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SuperProcessor extends Processor {

    @Override
    public Class<?>[] getTypes() {
        return new Class[]{ SuperChainRequired.class };
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>(super.getSupportedAnnotationTypes());
        set.add(Classes.sourceName(Classes.OVERRIDING_METHODS_SUPER));
        return set;
    }

    @Override
    public Set<String> getSupportedOptions() {
        Set<String> set = new HashSet<>(super.getSupportedAnnotationTypes());
        set.add("mod.properties.strict_super");
        return set;
    }

    @Override
    public void run(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        boolean strictSuper = this.options().containsKey("mod.properties.strict_super") && Boolean.parseBoolean(this.options().get("mod.properties.strict_super"));
        for (Element element : roundEnv.getElementsAnnotatedWith(SuperChainRequired.class)) {
            if (element.getKind() != ElementKind.METHOD || element.getModifiers().contains(Modifier.STATIC) || (!element.getModifiers().contains(Modifier.ABSTRACT) && !element.getModifiers().contains(Modifier.DEFAULT))) {
                this.messager().printMessage(Diagnostic.Kind.ERROR, "@SuperChainRequired can only be used on non-static abstract methods.", element);
            }
        }
        for (TypeElement type : this.getAllProcessedTypes()) {
            if (!type.getModifiers().contains(Modifier.FINAL)) {
                for (Element member : type.getEnclosedElements()) {
                    if (member.getKind() == ElementKind.METHOD && member instanceof ExecutableElement executable && !this.hasSuperOverrideAnnotation(executable, true)) {
                        if (!member.getModifiers().contains(Modifier.ABSTRACT) && !member.getModifiers().contains(Modifier.NATIVE) && !member.getModifiers().contains(Modifier.FINAL)) {
                            List<ExecutableElement> overridden = this.getAllOverriddenMethods(executable);
                            if (overridden.stream().anyMatch(ov -> this.hasSuperOverrideAnnotation(ov, strictSuper))) {
                                this.messager().printMessage(strictSuper ? Diagnostic.Kind.ERROR : Diagnostic.Kind.WARNING, "Method should be annotated with @OverridingMethodsMustInvokeSuper.", executable);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private boolean hasSuperOverrideAnnotation(ExecutableElement element, boolean strict) {
        if (element.getAnnotation(SuperChainRequired.class) != null) return true;
        if (!strict) return false;
        return element.getAnnotationMirrors().stream().anyMatch(mirror -> this.sameErasure(this.forClass(Classes.OVERRIDING_METHODS_SUPER), mirror.getAnnotationType()));
    }
}
