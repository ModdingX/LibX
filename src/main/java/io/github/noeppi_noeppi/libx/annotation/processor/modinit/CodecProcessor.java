package io.github.noeppi_noeppi.libx.annotation.processor.modinit;

import io.github.noeppi_noeppi.libx.annotation.Param;
import io.github.noeppi_noeppi.libx.annotation.PrimaryConstructor;

import javax.annotation.Nullable;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class CodecProcessor {

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
        List<GeneratedCodec.CodecParam> params = new ArrayList<>();
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
            String typeFqn = param.asType().toString();
            String typeFqnBoxed = env.boxed(param.asType()).toString();
            String codecFqn;
            boolean list;
            if (param.asType() instanceof DeclaredType && env.sameErasure(param.asType(), env.forClass(List.class))) {
                DeclaredType listType = (DeclaredType) param.asType();
                List<? extends TypeMirror> generics = listType.getTypeArguments();
                if (generics.size() != 1) {
                    env.messager().printMessage(Diagnostic.Kind.ERROR, "Can't get a Codec for unparameterized list type.");
                    return;
                }
                codecFqn = getCodecFqn(generics.get(0), param, env);
                list = true;
            } else {
                codecFqn = getCodecFqn(param.asType(), param, env);
                list = false;
            }
            if (codecFqn == null) {
                return;
            }
            String getter = getGetter((TypeElement) element.getEnclosingElement(), param.asType(), name, env);
            if (getter == null) {
                return;
            }
            params.add(new GeneratedCodec.CodecParam(codecFieldName, typeFqn, typeFqnBoxed, codecFqn, list, getter));
        }
        GeneratedCodec codec = new GeneratedCodec(type.getQualifiedName().toString(), params);
        env.getMod(element).addCodec(codec);
    }

    @Nullable
    private static String getCodecFqn(TypeMirror type, Element paramElement, ModEnv env) {
        TypeMirror boxed = env.boxed(type);
        TypeMirror codecClass;
        String fieldName;
        Param annotation = paramElement.getAnnotation(Param.class);
        if (annotation == null) {
            codecClass = boxed;
            fieldName = "CODEC";
        } else {
            codecClass = env.classType(annotation::value);
            if (codecClass.getKind() == TypeKind.VOID) {
                codecClass = boxed;
            }
            fieldName = annotation.field();
        }
        TypeMirror codecClassUnboxed = env.unboxed(codecClass);
        if ("CODEC".equals(fieldName) && codecClassUnboxed.getKind() == TypeKind.VOID || codecClassUnboxed.getKind() == TypeKind.NULL || codecClassUnboxed.getKind() == TypeKind.NONE) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Can't get a Codec for the void, null or none type.", paramElement);
            return null;
        } else if ("CODEC".equals(fieldName) && codecClassUnboxed.getKind() == TypeKind.BOOLEAN) {
            return ModInit.CODEC_FQN + ".BOOL";
        } else if ("CODEC".equals(fieldName) && codecClassUnboxed.getKind() == TypeKind.BYTE) {
            return ModInit.CODEC_FQN + ".BYTE";
        } else if ("CODEC".equals(fieldName) && codecClassUnboxed.getKind() == TypeKind.CHAR) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Can't get a Codec for the char type.", paramElement);
            return null;
        } else if ("CODEC".equals(fieldName) && codecClassUnboxed.getKind() == TypeKind.SHORT) {
            return ModInit.CODEC_FQN + ".SHORT";
        } else if ("CODEC".equals(fieldName) && codecClassUnboxed.getKind() == TypeKind.INT) {
            return ModInit.CODEC_FQN + ".INT";
        } else if ("CODEC".equals(fieldName) && codecClassUnboxed.getKind() == TypeKind.LONG) {
            return ModInit.CODEC_FQN + ".LONG";
        } else if ("CODEC".equals(fieldName) && codecClassUnboxed.getKind() == TypeKind.FLOAT) {
            return ModInit.CODEC_FQN + ".FLOAT";
        } else if ("CODEC".equals(fieldName) && codecClassUnboxed.getKind() == TypeKind.DOUBLE) {
            return ModInit.CODEC_FQN + ".DOUBLE";
        } else {
            if ("CODEC".equals(fieldName) && env.sameErasure(codecClass, env.forClass(String.class))) {
                return ModInit.CODEC_FQN + ".STRING";
            } else if ("CODEC".equals(fieldName) && env.sameErasure(codecClass, env.forClass(ByteBuffer.class))) {
                return ModInit.CODEC_FQN + ".BYTE_BUFFER";
            } else if ("CODEC".equals(fieldName) && env.sameErasure(codecClass, env.forClass(IntStream.class))) {
                return ModInit.CODEC_FQN + ".INT_STREAM";
            } else if ("CODEC".equals(fieldName) && env.sameErasure(codecClass, env.forClass(LongStream.class))) {
                return ModInit.CODEC_FQN + ".LONG_STREAM";
            } else {
                Element typeElem = env.types().asElement(codecClass);
                if (typeElem == null) {
                    env.messager().printMessage(Diagnostic.Kind.ERROR, "Can't get type element of parameter: " + codecClass + ".", paramElement);
                    return null;
                }
                VariableElement fieldElem = typeElem.getEnclosedElements().stream()
                        .filter(e -> e.getKind() == ElementKind.FIELD)
                        .filter(e -> e.getModifiers().contains(Modifier.PUBLIC) && e.getModifiers().contains(Modifier.STATIC))
                        .filter(e -> e instanceof VariableElement)
                        .map(e -> (VariableElement) e)
                        .filter(e -> e.getSimpleName().contentEquals(fieldName))
                        .findFirst().orElse(null);
                if (fieldElem == null) {
                    env.messager().printMessage(Diagnostic.Kind.ERROR, "Can't get codec for parameter: " + typeElem.asType() + "." + fieldName + " is not defined or inaccessible.", paramElement);
                    return null;
                }
                if (!env.sameErasure(fieldElem.asType(), env.elements().getTypeElement(ModInit.CODEC_FQN).asType())) {
                    env.messager().printMessage(Diagnostic.Kind.ERROR, "Can't get codec for parameter: " + typeElem.asType() + "." + fieldName + " is defined but not a Codec.", paramElement);
                    return null;
                }
                if (!genericMatches(fieldElem.asType(), boxed, env)) {
                    env.messager().printMessage(Diagnostic.Kind.ERROR, "Can't get codec for parameter: " + typeElem.asType() + "." + fieldName + " is not compatible with type " + type + ".", paramElement);
                    return null;
                }
                if (!(fieldElem.getEnclosingElement() instanceof QualifiedNameable)) {
                    env.messager().printMessage(Diagnostic.Kind.ERROR, "Codec field is not nameable.", paramElement);
                    return null;
                }
                return ((QualifiedNameable) fieldElem.getEnclosingElement()).getQualifiedName().toString() + "." + fieldElem.getSimpleName().toString();
            }
        }
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

    private static boolean genericMatches(TypeMirror typeWithGeneric, TypeMirror compare, ModEnv env) {
        if (!(typeWithGeneric instanceof DeclaredType) || ((DeclaredType) typeWithGeneric).getTypeArguments().size() != 1) {
            // Something is wrong. We assume true to let it run. It might fail later on when
            // compiling generated code but there might be cases where it succeeds.
            return true;
        } else {
            TypeMirror generic = ((DeclaredType) typeWithGeneric).getTypeArguments().get(0);
            return env.sameErasure(generic, compare);
        }
    }
}
