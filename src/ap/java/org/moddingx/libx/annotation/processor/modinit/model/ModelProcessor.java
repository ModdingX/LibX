package org.moddingx.libx.annotation.processor.modinit.model;

import org.moddingx.libx.annotation.model.Model;
import org.moddingx.libx.annotation.processor.Classes;
import org.moddingx.libx.annotation.processor.modinit.ModEnv;

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
        if (!element.getEnclosingElement().getModifiers().contains(Modifier.PUBLIC)) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "@Model can't be used in private type.");
            return;
        }
        Element typeElement = env.typeElement(Classes.BAKED_MODEL);
        if (!env.sameErasure(element.asType(), typeElement.asType())) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Field annotated @Model needs a type of " + Classes.BAKED_MODEL + ".");
            return;
        }
        Model model = element.getAnnotation(Model.class);
        env.getMod(element).addModel(parent.getQualifiedName().toString(), element.getSimpleName().toString(), model.namespace(), model.value());
    }
}
