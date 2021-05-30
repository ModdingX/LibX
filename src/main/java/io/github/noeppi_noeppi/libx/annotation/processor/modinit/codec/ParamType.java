package io.github.noeppi_noeppi.libx.annotation.processor.modinit.codec;

import io.github.noeppi_noeppi.libx.annotation.Param;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.GeneratedCodec;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.ModEnv;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.ModInit;

import javax.annotation.Nullable;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class ParamType implements CodecType {

    @Override
    public boolean matchesDirect(VariableElement param, String name, ModEnv env) {
        return param.getAnnotation(Param.class) != null;
    }

    @Override
    public boolean matches(VariableElement param, String name, ModEnv env) {
        // Last one in the list. Matches everything
        return true;
    }

    @Override
    public GeneratedCodec.CodecElement generate(VariableElement param, String name, GetterSupplier getter, ModEnv env) throws FailureException {
        String typeFqn = param.asType().toString();
        String typeFqnBoxed = env.boxed(param.asType()).toString();
        String codecFqn;
        boolean list;
        if (param.asType() instanceof DeclaredType && env.sameErasure(param.asType(), env.forClass(List.class))) {
            DeclaredType listType = (DeclaredType) param.asType();
            List<? extends TypeMirror> generics = listType.getTypeArguments();
            if (generics.size() != 1) {
                env.messager().printMessage(Diagnostic.Kind.ERROR, "Can't get a Codec for parameterized list type.");
                throw new FailureException();
            }
            codecFqn = getCodecFqn(generics.get(0), param, env);
            list = true;
        } else {
            codecFqn = getCodecFqn(param.asType(), param, env);
            list = false;
        }
        if (codecFqn == null) {
            throw new FailureException();
        }
        return new GeneratedCodec.CodecParam(name, typeFqn, typeFqnBoxed, codecFqn, list, getter.get());
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
