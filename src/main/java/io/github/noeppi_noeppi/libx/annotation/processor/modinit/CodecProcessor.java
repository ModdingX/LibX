package io.github.noeppi_noeppi.libx.annotation.processor.modinit;

import io.github.noeppi_noeppi.libx.annotation.codec.PrimaryConstructor;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.codec.*;

import javax.annotation.Nullable;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CodecProcessor {

    public static final List<CodecType> CODECS = Collections.unmodifiableList(Arrays.asList(
            new RegistryType(),
            new ParamType()
    ));
    
    public static void processParam(Element element, ModEnv env) {
        if (element.getEnclosingElement().getKind() != ElementKind.CONSTRUCTOR || element.getEnclosingElement().getAnnotation(PrimaryConstructor.class) == null) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "@Param can only be used on parameters of the primary constructor.");
        }
    }

    public static void processPrimaryConstructor(Element rawElement, ModEnv env) {
        if (rawElement.getKind() != ElementKind.CONSTRUCTOR || !(rawElement instanceof ExecutableElement)) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "@PrimaryConstructor can only be used on constructors.", rawElement);
            return;
        }
        ExecutableElement element = (ExecutableElement) rawElement;
        if (!(element.getEnclosingElement() instanceof TypeElement)) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Element annotated with @PrimaryConstructor is not a TypeElement.", element);
            return;
        }
        if (!element.getModifiers().contains(Modifier.PUBLIC)) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "The primary constructor of a class must be public.", element);
            return;
        }
        TypeElement type = (TypeElement) element.getEnclosingElement();
        if (type.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.CONSTRUCTOR)
                .filter(e -> e.getAnnotation(PrimaryConstructor.class) != null)
                .count() >= 2) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "A class can only have one primary constructor.", type);
            return;
        }
        if (element.getParameters().size() > 16) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "The primary constructor may not have more than 16 parameters. This is a limitation of DataFixerUpper.", type);
            return;
        }
        List<GeneratedCodec.CodecElement> params = new ArrayList<>();
        for (VariableElement param : element.getParameters()) {
            String name = param.getSimpleName().toString();
            String codecFieldName;
            {
                StringBuilder sb = new StringBuilder();
                for (char chr : name.toCharArray()) {
                    if (Character.isUpperCase(chr)) {
                        sb.append('_');
                    }
                    sb.append(Character.toLowerCase(chr));
                }
                codecFieldName = sb.toString();
            }
            
            GetterSupplier getter = () -> {
                String g = getGetter((TypeElement) element.getEnclosingElement(), param.asType(), name, env);
                if (g == null) throw new FailureException();
                return g;
            };
            
            CodecType codecType = null;
            for (CodecType c : CODECS) {
                if (c.matchesDirect(param, codecFieldName, env)) {
                    if (codecType != null) {
                        env.messager().printMessage(Diagnostic.Kind.ERROR, "Can't use multiple codec parameter annotations on the same element.", param);
                        return;
                    }
                    codecType = c;
                }
            }
            if (codecType == null) {
                for (CodecType c : CODECS) {
                    if (c.matches(param, codecFieldName, env)) {
                        codecType = c;
                        break;
                    }
                }
                if (codecType == null) {
                    env.messager().printMessage(Diagnostic.Kind.ERROR, "Can't infer codec type for parameter. Add an explicit annotation.", param);
                    return;
                }
            } else {
                if (!codecType.matches(param, codecFieldName, env)) {
                    env.messager().printMessage(Diagnostic.Kind.ERROR, "Parameter is not valid for applied annotation.", param);
                    return;
                }
            }
            try {
                params.add(codecType.generate(param, codecFieldName, getter, env));
            } catch (FailureException e) {
                return;
            }
        }
        GeneratedCodec codec = new GeneratedCodec(type.getQualifiedName().toString(), params);
        env.getMod(element).addCodec(codec);
    }

    @Nullable
    private static String getGetter(TypeElement typeElem, TypeMirror type, String name, ModEnv env) {
        String elementFqn = typeElem.getQualifiedName().toString();
        // 1st attempt: Get a public non-static field with the same name.
        VariableElement fieldElem = typeElem.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.FIELD)
                .filter(e -> e.getModifiers().contains(Modifier.PUBLIC) && !e.getModifiers().contains(Modifier.STATIC))
                .filter(e -> e instanceof VariableElement)
                .map(e -> (VariableElement) e)
                .filter(e -> e.getSimpleName().contentEquals(name))
                .findFirst().orElse(null);
        if (fieldElem != null) {
            String getter = GeneratedCodec.fieldGetter(elementFqn, fieldElem.getSimpleName().toString());
            return withCheckType(getter, fieldElem.asType(), type, fieldElem, env);
        }
        // 2nd attempt: Getter method
        ExecutableElement methodElem = getGetterMethod(typeElem, elementFqn, "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1), env);
        if (methodElem == null) {
            methodElem = getGetterMethod(typeElem, elementFqn, name, env);
            if (methodElem == null && env.unboxed(type).getKind() == TypeKind.BOOLEAN) {
                methodElem = getGetterMethod(typeElem, elementFqn, "is" + Character.toUpperCase(name.charAt(0)) + name.substring(1), env);
            }
        }
        if (methodElem != null) {
            String getter = GeneratedCodec.methodGetter(elementFqn, methodElem.getSimpleName().toString());
            return withCheckType(getter, methodElem.getReturnType(), type, methodElem, env);
        }
        env.messager().printMessage(Diagnostic.Kind.ERROR, "Can't infer getter for parameter: Neither a public field / no-arg method named '" + name + "' nor a no no-arg method named 'get" + Character.toUpperCase(name.charAt(0)) + name.substring(1) + "' found.", typeElem);
        return null;
    }

    @Nullable
    private static ExecutableElement getGetterMethod(TypeElement typeElem, String elementFqn, String methodName, ModEnv env) {
        return typeElem.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.METHOD)
                .filter(e -> e.getModifiers().contains(Modifier.PUBLIC) && !e.getModifiers().contains(Modifier.STATIC))
                .filter(e -> e instanceof ExecutableElement)
                .map(e -> (ExecutableElement) e)
                .filter(e -> e.getSimpleName().contentEquals(methodName))
                .filter(e -> e.getParameters().isEmpty())
                .findFirst().orElse(null);
    }
    
    private static String withCheckType(String getter, TypeMirror got, TypeMirror expected, Element at, ModEnv env) {
        if (!env.types().isAssignable(got, expected)) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Getter that was found for parameter has wrong type: " + got + " is not assignable to " + expected, at);
            return null;
        } else {
            return getter;
        }
    }
}
