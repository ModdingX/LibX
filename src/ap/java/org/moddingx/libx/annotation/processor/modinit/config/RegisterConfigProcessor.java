package org.moddingx.libx.annotation.processor.modinit.config;

import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.annotation.processor.modinit.ModEnv;
import org.moddingx.libx.annotation.processor.modinit.ModInit;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.QualifiedNameable;
import javax.tools.Diagnostic;

public class RegisterConfigProcessor {
    
    public static void processRegisterConfig(Element element, ModEnv env) {
        if (element.getKind() != ElementKind.CLASS || !(element instanceof QualifiedNameable)) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Failed to get qualified name for element annotated with @RegisterConfig or element is not a class.", element);
            return;
        }
        if (element.getEnclosingElement().getKind() != ElementKind.PACKAGE || !(element.getEnclosingElement() instanceof PackageElement parent)) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Parent of element annotated with @RegisterConfig is not a package", element);
            return;
        }
        RegisterConfig registerConfig = element.getAnnotation(RegisterConfig.class);
        ModInit mod = env.getMod(element);
        mod.addConfig(registerConfig.value(), registerConfig.client(), registerConfig.requiresMod().isEmpty() ? null : registerConfig.requiresMod(), parent.getQualifiedName() + "." + element.getSimpleName());
    }
}
