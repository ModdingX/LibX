package io.github.noeppi_noeppi.libx.annotation.processor;

import io.github.noeppi_noeppi.libx.config.Config;
import io.github.noeppi_noeppi.libx.util.ResourceList;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.*;

public class ConfigProcessor extends Processor {

    private Set<TypeMirror> validTypes;
    private Set<TypeMirror> validTypesWrapper;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        
        // No immutable set, we only want classes from java(x) packages here.
        Set<TypeMirror> validTypes = new HashSet<>();
        validTypes.add(this.forClass(boolean.class));
        validTypes.add(this.forClass(byte.class));
        validTypes.add(this.forClass(short.class));
        validTypes.add(this.forClass(int.class));
        validTypes.add(this.forClass(long.class));
        validTypes.add(this.forClass(float.class));
        validTypes.add(this.forClass(double.class));
        validTypes.add(this.forClass(String.class));
        validTypes.add(this.forClass(Optional.class));
        validTypes.add(this.forClass(List.class));
        validTypes.add(this.forClass(Map.class));
        validTypes.add(this.forClass(ResourceList.class));
        validTypes.add(this.elements.getTypeElement("net.minecraft.item.crafting.Ingredient").asType());
        validTypes.add(this.elements.getTypeElement("net.minecraft.util.text.IFormattableTextComponent").asType());
        validTypes.add(this.elements.getTypeElement("net.minecraft.util.ResourceLocation").asType());
        this.validTypes = Collections.unmodifiableSet(validTypes);
        
        Set<TypeMirror> validTypesWrapper = new HashSet<>();
        validTypesWrapper.add(this.forClass(Boolean.class));
        validTypesWrapper.add(this.forClass(Byte.class));
        validTypesWrapper.add(this.forClass(Short.class));
        validTypesWrapper.add(this.forClass(Integer.class));
        validTypesWrapper.add(this.forClass(Long.class));
        validTypesWrapper.add(this.forClass(Float.class));
        validTypesWrapper.add(this.forClass(Double.class));
        this.validTypesWrapper = Collections.unmodifiableSet(validTypesWrapper);
    }

    @Override
    public Class<?>[] getTypes() {
        return new Class<?>[]{ Config.class };
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Config.class)) {
            Config config = element.getAnnotation(Config.class);
            if (this.isSuppressed(element, "config")) continue;

            if (element.getKind() != ElementKind.FIELD || !element.getModifiers().contains(Modifier.STATIC) ||
                    !element.getModifiers().contains(Modifier.PUBLIC) || element.getModifiers().contains(Modifier.FINAL)) {
                this.messager.printMessage(Diagnostic.Kind.ERROR, "Only public non-final static fields can be annotated with @Config", element);
                continue;
            }

            

            if (config.mapper().isEmpty()) {
                TypeMirror type = element.asType();
                if (this.validTypes.stream().noneMatch(t -> this.sameErasure(t, type))) {
                    if (this.validTypesWrapper.stream().anyMatch(t -> this.sameErasure(t, type))) {
                        this.messager.printMessage(Diagnostic.Kind.WARNING, "@Config should not use wrapper type: " + type, element);
                    } else {
                        this.messager.printMessage(Diagnostic.Kind.ERROR, "No value mapper found for type of @Config. Register you own." + type, element);
                    }
                }
            }

            TypeMirror keyClazz;
            TypeMirror typeClazz;
            if (element.asType() instanceof DeclaredType) {
                List<? extends TypeMirror> parameters = ((DeclaredType) element.asType()).getTypeArguments();
                if (parameters.isEmpty()) {
                    keyClazz = this.forClass(String.class);
                    typeClazz = this.forClass(void.class);
                } else {
                    keyClazz = parameters.get(0);
                    typeClazz = parameters.get(parameters.size() - 1);
                }
            } else {
                keyClazz = this.forClass(String.class);
                typeClazz = this.forClass(void.class);
            }
            TypeMirror elementType = this.classType(config::elementType);
            if (!this.sameErasure(elementType, typeClazz) && !this.isSuppressed(element, "unchecked")) {
                this.messager.printMessage(Diagnostic.Kind.WARNING, "Unchecked @Config: elementType does not match type parameter.", element);
            } else if (elementType.getKind() != TypeKind.VOID && this.validTypes.stream().noneMatch(t -> this.sameErasure(t, elementType)) && !this.isSuppressed(element, "configElement")) {
                this.messager.printMessage(Diagnostic.Kind.WARNING, "Unchecked @Config: No value mapper for elementType. This is probably a bug.\nSuppress with @SuppressWarning(\"configElement\")", element);
            }
            if (this.sameErasure(this.forClass(Map.class), element.asType()) && !this.sameErasure(keyClazz, this.forClass(String.class)) && !this.isSuppressed(element, "unchecked")) {
                this.messager.printMessage(Diagnostic.Kind.WARNING, "Unchecked @Config: Map required keys of type String.", element);
            }
        }
        return true;
    }
}
