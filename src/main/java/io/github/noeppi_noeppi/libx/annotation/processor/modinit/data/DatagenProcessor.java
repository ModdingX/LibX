package io.github.noeppi_noeppi.libx.annotation.processor.modinit.data;

import io.github.noeppi_noeppi.libx.annotation.processor.modinit.FailureException;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.ModEnv;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.ModInit;
import io.github.noeppi_noeppi.libx.mod.ModX;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.List;

public class DatagenProcessor {
    
    public static void processDatagen(Element element, ModEnv env) {
        if (element.getKind() != ElementKind.CLASS || !(element instanceof TypeElement)) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Can't use @Datagen on element that is not a class.", element);
            return;
        }
        if (element.getEnclosingElement().getKind() != ElementKind.PACKAGE || !(element.getEnclosingElement() instanceof PackageElement parent)) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Parent of element annotated with @Datagen is not a package", element);
            return;
        }
        if (!env.subTypeErasure(element.asType(), env.elements().getTypeElement(ModInit.DATA_PROVIDER_TYPE).asType())) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "@Datagen can only be used on data providers.", element);
            return;
        }
        List<ExecutableElement> ctors = element.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.CONSTRUCTOR)
                .filter(e -> e instanceof ExecutableElement)
                .map(e -> (ExecutableElement) e)
                .toList();
        List<DatagenEntry.Arg> args;
        if (ctors.isEmpty()) {
            args = List.of();
        } else if (ctors.size() != 1) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Class annotated with @Datagen can only have one constructor.", element);
            return;
        } else {
            ExecutableElement ctor = ctors.get(0);
            args = ctor.getParameters().stream()
                    .map(p -> getArg(p, env))
                    .toList();
        }
        ModInit mod = env.getMod(element);
        mod.addDatagen(parent.getQualifiedName() + "." + element.getSimpleName(), args);
    }
    
    private static DatagenEntry.Arg getArg(VariableElement param, ModEnv env) {
        TypeMirror type = param.asType();
        if (env.subTypeErasure(type, env.forClass(ModX.class))) {
            return DatagenEntry.Arg.MOD;
        } else if (env.subTypeErasure(type, env.elements().getTypeElement(ModInit.DATA_GENERATOR_TYPE).asType())) {
            return DatagenEntry.Arg.GENERATOR;
        } else if (env.subTypeErasure(type, env.elements().getTypeElement(ModInit.DATA_FILE_HELPER_TYPE).asType())) {
            return DatagenEntry.Arg.FILE_HELPER;
        } else {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Constructor in datagen class may only have specific parameters..", param);
            throw new FailureException();
        }
    }
}
