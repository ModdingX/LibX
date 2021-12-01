package io.github.noeppi_noeppi.libx.annotation.processor.modinit.config;

import io.github.noeppi_noeppi.libx.annotation.processor.Classes;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.ModEnv;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.ModInit;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

public class RegisterMapperProcessor {
    
    public static void processRegisterMapper(Element element, ModEnv env) {
        if (!(element instanceof TypeElement typeElem)) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Failed to get qualified name for element annotated with @RegisterMapper", element);
            return;
        }
        if (element.getEnclosingElement().getKind() != ElementKind.PACKAGE || !(element.getEnclosingElement() instanceof PackageElement parent)) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Parent of element annotated with @RegisterMapper is not a package", element);
            return;
        }
        boolean simple = env.subTypeErasure(element.asType(), env.forClass(Classes.VALUE_MAPPER));
        boolean generic = env.subTypeErasure(element.asType(), env.forClass(Classes.GENERIC_VALUE_MAPPER));
        if (simple && generic) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Can't register a value mapper that is both simple and generic.", element);
            return;
        } else if (!simple && !generic) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Can't use @RegisterMapper on class that is no value mapper.", element);
            return;
        }
        ModInit mod = env.getMod(element);
        mod.addConfigMapper(parent.getQualifiedName() + "." + element.getSimpleName(), !typeElem.getTypeParameters().isEmpty());
    }
}
