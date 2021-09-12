package io.github.noeppi_noeppi.libx.annotation.processor.modinit.codec;

import io.github.noeppi_noeppi.libx.annotation.codec.Dynamic;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.FailureException;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.ModEnv;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.ModInit;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

public class DynamicType implements CodecType {
    
    @Override
    public boolean matchesDirect(Element param, String name, ModEnv env) {
        return param.getAnnotation(Dynamic.class) != null;
    }

    @Override
    public boolean matches(Element param, String name, ModEnv env) {
        return this.matchesDirect(param, name, env);
    }

    @Override
    public GeneratedCodec.CodecElement generate(Element param, String name, GetterSupplier getter, ModEnv env) throws FailureException {
        String typeFqn = param.asType().toString();
        String typeFqnBoxed = env.boxed(param.asType()).toString();
        String factoryFqn = getFactoryFqn(param.asType(), param, env);
        return new GeneratedCodec.CodecDynamic(name, typeFqn, typeFqnBoxed, factoryFqn, getter.get());
    }

    private static String getFactoryFqn(TypeMirror type, Element paramElement, ModEnv env) {
        TypeMirror boxed = env.boxed(type);
        Dynamic annotation = paramElement.getAnnotation(Dynamic.class);
        if (annotation == null) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "No ΩDynamic annotation", paramElement);
            throw new FailureException();
        }
        TypeMirror codecClass = env.classType(annotation::value);
        if (codecClass.getKind() == TypeKind.VOID) {
            codecClass = boxed;
        }
        String methodName = annotation.factory().isEmpty() ? "fieldOf" : annotation.factory();
        Element typeElem = env.types().asElement(codecClass);
        if (typeElem == null) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Not a valid type element: " + codecClass, paramElement);
            throw new FailureException();
        }
        ExecutableElement method = null;
        for (Element elem : typeElem.getEnclosedElements()) {
            if (elem.getKind() == ElementKind.METHOD && elem instanceof ExecutableElement exec && elem.getSimpleName().contentEquals(methodName)
                    && elem.getModifiers().contains(Modifier.PUBLIC) && elem.getModifiers().contains(Modifier.STATIC)) {
                if (env.sameErasure(env.elements().getTypeElement(ModInit.MAP_CODEC_TYPE).asType(), exec.getReturnType())) {
                    if (exec.getParameters().size() == 1 && env.sameErasure(env.forClass(String.class), exec.getParameters().get(0).asType())) {
                        method = exec;
                    }
                }
            }
        }
        if (method == null) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, codecClass + "#" + methodName + " has invalid signature or can't be found.", paramElement);
            throw new FailureException();
        }
        if (!ParamType.genericMatches(method.getReturnType(), boxed, env)) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, codecClass + "#" + methodName + " has invalid return type: Expected " + boxed + ", got " + method.getReturnType(), paramElement);
            throw new FailureException();
        }
        if (!(method.getEnclosingElement() instanceof TypeElement parent)) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Parent of dynamic codec factory is not a class.", paramElement);
            throw new FailureException();
        }
        return parent.getQualifiedName().toString() + "." + method.getSimpleName().toString();
    }
}
