package io.github.noeppi_noeppi.libx.annotation.processor.modinit.model;

import io.github.noeppi_noeppi.libx.annotation.model.Model;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.ModEnv;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.ModInit;

import javax.lang.model.element.*;
import javax.tools.Diagnostic;

public class ModelProcessor {
    
    public static void processModel(Element element, ModEnv env) {
        if (element.getKind() != ElementKind.FIELD || !(element instanceof VariableElement) || !(element.getEnclosingElement() instanceof QualifiedNameable parent)) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "@Model can only be used on fields.");
            return;
        }
        if (!element.getModifiers().contains(Modifier.PUBLIC) || !element.getModifiers().contains(Modifier.STATIC) || element.getModifiers().contains(Modifier.FINAL)) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "@Model can only be used on public static non-final fields.");
            return;
        }
        Element typeElement = env.elements().getTypeElement(ModInit.MODEL_TYPE);
        if (typeElement == null) {
            throw new IllegalStateException("Model base class not found: " + ModInit.MODEL_TYPE);
        } else {
            if (!env.sameErasure(element.asType(), typeElement.asType())) {
                env.messager().printMessage(Diagnostic.Kind.ERROR, "Field annotated @Model needs a type of " + ModInit.MODEL_TYPE + ".");
                return;
            }
        }
        Model model = element.getAnnotation(Model.class);
        env.getMod(element).addModel(parent.getQualifiedName().toString(), element.getSimpleName().toString(), model.namespace(), model.value());
    }
}
