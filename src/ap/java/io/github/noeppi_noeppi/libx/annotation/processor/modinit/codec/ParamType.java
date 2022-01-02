package io.github.noeppi_noeppi.libx.annotation.processor.modinit.codec;

import io.github.noeppi_noeppi.libx.annotation.codec.Param;
import io.github.noeppi_noeppi.libx.annotation.processor.Classes;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.FailureException;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.ModEnv;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.ModInit;

import javax.annotation.Nullable;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.nio.ByteBuffer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class ParamType implements CodecType {
    
    @Override
    public boolean matchesDirect(Element param, String name, ModEnv env) {
        return param.getAnnotation(Param.class) != null;
    }

    @Override
    public boolean matches(Element param, String name, ModEnv env) {
        // Last one in the list. Matches everything
        return true;
    }

    @Override
    public GeneratedCodec.CodecElement generate(Element param, String name, GetterSupplier getter, ModEnv env) throws FailureException {
        String typeFqn = param.asType().toString();
        String typeFqnBoxed = env.boxed(param.asType()).toString();
        
        CodecProcessor.ListInfo list = CodecProcessor.getNestedListInfo(param.asType(), env);
        if (list == null) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Can't get a Codec for parameterized list type: Failed to infer element type.", param);
            throw new FailureException();
        }
        
        String codecFqn = getCodecFqn(list.elementType(), param, env);
        if (codecFqn == null) {
            throw new FailureException();
        }
        return new GeneratedCodec.CodecParam(name, typeFqn, typeFqnBoxed, codecFqn, list.nesting(), getter.get());
    }

    @Nullable
    private static String getCodecFqn(TypeMirror type, Element paramElement, ModEnv env) {
        TypeMirror boxed = env.boxed(type);
        TypeMirror codecClass;
        @Nullable
        String fieldName;
        Param annotation = paramElement.getAnnotation(Param.class);
        if (annotation == null) {
            codecClass = boxed;
            fieldName = null;
        } else {
            codecClass = env.classType(annotation::value);
            if (codecClass.getKind() == TypeKind.VOID) {
                codecClass = boxed;
            }
            fieldName = annotation.field().isEmpty() ? null : annotation.field();
        }
        TypeMirror codecClassUnboxed = env.unboxed(codecClass);
        if (fieldName == null && codecClassUnboxed.getKind() == TypeKind.VOID || codecClassUnboxed.getKind() == TypeKind.NULL || codecClassUnboxed.getKind() == TypeKind.NONE) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Can't get a Codec for the void, null or none type.", paramElement);
            return null;
        } else if (fieldName == null && codecClassUnboxed.getKind() == TypeKind.BOOLEAN) {
            return Classes.sourceName(Classes.CODEC) + ".BOOL";
        } else if (fieldName == null && codecClassUnboxed.getKind() == TypeKind.BYTE) {
            return Classes.sourceName(Classes.CODEC) + ".BYTE";
        } else if (fieldName == null && codecClassUnboxed.getKind() == TypeKind.CHAR) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Can't get a Codec for the char type.", paramElement);
            return null;
        } else if (fieldName == null && codecClassUnboxed.getKind() == TypeKind.SHORT) {
            return Classes.sourceName(Classes.CODEC) + ".SHORT";
        } else if (fieldName == null && codecClassUnboxed.getKind() == TypeKind.INT) {
            return Classes.sourceName(Classes.CODEC) + ".INT";
        } else if (fieldName == null && codecClassUnboxed.getKind() == TypeKind.LONG) {
            return Classes.sourceName(Classes.CODEC) + ".LONG";
        } else if (fieldName == null && codecClassUnboxed.getKind() == TypeKind.FLOAT) {
            return Classes.sourceName(Classes.CODEC) + ".FLOAT";
        } else if (fieldName == null && codecClassUnboxed.getKind() == TypeKind.DOUBLE) {
            return Classes.sourceName(Classes.CODEC) + ".DOUBLE";
        } else {
            if (fieldName == null && env.sameErasure(codecClass, env.forClass(String.class))) {
                return Classes.sourceName(Classes.CODEC) + ".STRING";
            } else if (fieldName == null && env.sameErasure(codecClass, env.forClass(ByteBuffer.class))) {
                return Classes.sourceName(Classes.CODEC) + ".BYTE_BUFFER";
            } else if (fieldName == null && env.sameErasure(codecClass, env.forClass(IntStream.class))) {
                return Classes.sourceName(Classes.CODEC) + ".INT_STREAM";
            } else if (fieldName == null && env.sameErasure(codecClass, env.forClass(LongStream.class))) {
                return Classes.sourceName(Classes.CODEC) + ".LONG_STREAM";
            } else {
                if (fieldName == null) {
                    for (String name : ModInit.DEFAULT_PARAM_CODEC_FIELDS) {
                        String result = tryDetect(paramElement, boxed, codecClass, name, env, false);
                        if (result != null) return result;
                    }
                    env.messager().printMessage(Diagnostic.Kind.ERROR, "Can't get codec for parameter: No default codec field found. Tried [ " + String.join(", ", ModInit.DEFAULT_PARAM_CODEC_FIELDS) + " ] in class " + codecClass + ".", paramElement);
                    // Run tryDetect on the all fields again to get error messages as all of them failed.
                    for (String name : ModInit.DEFAULT_PARAM_CODEC_FIELDS) {
                        tryDetect(paramElement, boxed, codecClass, name, env, true);
                    }
                    return null;
                } else {
                    return tryDetect(paramElement, boxed, codecClass, fieldName, env, true);
                }
            }
        }
    }
    
    @Nullable
    private static String tryDetect(Element paramElement, TypeMirror type, TypeMirror codecClass, String fieldName, ModEnv env, boolean fail) {
        TypeMirror boxed = env.boxed(type);
        Element typeElem = env.types().asElement(codecClass);
        if (typeElem == null) {
            if (fail) {
                env.messager().printMessage(Diagnostic.Kind.ERROR, "Can't get type element of parameter: " + codecClass + ".", paramElement);
            }
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
            if (fail) {
                env.messager().printMessage(Diagnostic.Kind.ERROR, "Can't get codec for parameter: " + typeElem.asType() + "." + fieldName + " is not defined or inaccessible.", paramElement);
            }
            return null;
        }
        if (!env.sameErasure(fieldElem.asType(), env.forClass(Classes.CODEC))) {
            if (fail) {
                env.messager().printMessage(Diagnostic.Kind.ERROR, "Can't get codec for parameter: " + typeElem.asType() + "." + fieldName + " is defined but not a Codec.", paramElement);
            }
            return null;
        }
        if (!genericMatches(env.boxed(fieldElem.asType()), boxed, env)) {
            if (fail) {
                env.messager().printMessage(Diagnostic.Kind.ERROR, "Can't get codec for parameter: " + typeElem.asType() + "." + fieldName + " is not compatible with type " + type + ".", paramElement);
            }
            return null;
        }
        if (fieldElem.getEnclosingElement() instanceof QualifiedNameable parent) {
            return parent.getQualifiedName().toString() + "." + fieldElem.getSimpleName().toString();
        } else {
            if (fail) {
                env.messager().printMessage(Diagnostic.Kind.ERROR, "Codec field is not nameable.", paramElement);
            }
            return null;
        }
    }

    public static boolean genericMatches(TypeMirror typeWithGeneric, TypeMirror compare, ModEnv env) {
        if (typeWithGeneric.getKind() == TypeKind.DECLARED && typeWithGeneric instanceof DeclaredType declared && declared.getTypeArguments().size() == 1) {
            TypeMirror generic = declared.getTypeArguments().get(0);
            return env.sameErasure(generic, compare);
        } else {
            // Something is wrong. We assume true to let it run. It might fail later on when
            // compiling generated code but there might be cases where it succeeds.
            return true;
        }
    }
}
