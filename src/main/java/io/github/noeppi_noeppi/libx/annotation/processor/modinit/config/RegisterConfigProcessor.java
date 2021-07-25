package io.github.noeppi_noeppi.libx.annotation.processor.modinit.config;

import io.github.noeppi_noeppi.libx.annotation.config.RegisterConfig;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.ModEnv;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.ModInit;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.QualifiedNameable;
import javax.tools.Diagnostic;

public class RegisterConfigProcessor {
    
    public static void processRegisterConfig(Element element, ModEnv env) {
        if (!(element instanceof QualifiedNameable)) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Failed to get qualified name for element annotated with @RegisterConfig", element);
            return;
        }
        if (!(element.getEnclosingElement() instanceof PackageElement)) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Parent of element annotated with @RegisterConfig is not a package", element);
            return;
        }
        RegisterConfig registerConfig = element.getAnnotation(RegisterConfig.class);
        ModInit mod = env.getMod(element);
        mod.addConfig(registerConfig.value(), registerConfig.client(), ((QualifiedNameable) element.getEnclosingElement()).getQualifiedName() + "." + element.getSimpleName());
    }
}
