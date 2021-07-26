package io.github.noeppi_noeppi.libx.annotation.processor.modinit.codec;

import io.github.noeppi_noeppi.libx.annotation.codec.Lookup;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.FailureException;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.ModEnv;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.ModInit;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.List;

public class RegistryType implements CodecType {
    
    @Override
    public boolean matchesDirect(Element param, String name, ModEnv env) {
        return param.getAnnotation(Lookup.class) != null;
    }

    @Override
    public boolean matches(Element param, String name, ModEnv env) {
        Element element = env.types().asElement(param.asType());
        if (element instanceof TypeElement) {
            return ((TypeElement) element).getQualifiedName().contentEquals(ModInit.REGISTRY_TYPE);
        } else {
            return false;
        }
    }

    @Override
    public GeneratedCodec.CodecElement generate(Element param, String name, GetterSupplier getter, ModEnv env) throws FailureException {
        String typeFqn = param.asType().toString();
        String typeFqnBoxed = env.boxed(param.asType()).toString();
        String namespace;
        String path;
        Lookup lookup = param.getAnnotation(Lookup.class);
        if (lookup == null) {
            namespace = "minecraft";
            path = null;
        } else {
            namespace = lookup.namespace();
            path = lookup.value().isEmpty() ? null : lookup.value();
        }
        TypeMirror mirror = param.asType();
        TypeElement generic = null;
        TypeMirror genericType = null;
        if (mirror instanceof DeclaredType declared) {
            List<? extends TypeMirror> generics = declared.getTypeArguments();
            if (generics != null && generics.size() == 1) {
                genericType = generics.get(0);
                Element elem = env.types().asElement(genericType);
                if (elem instanceof TypeElement typeElem) {
                    generic = typeElem;
                }
            }
        }
        if (genericType == null || generic == null) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Could not infer registry type for registry codec.", param);
            throw new FailureException();
        }
        if (path == null && !ModInit.ALLOWED_REGISTRY_CODEC_TYPES.contains(generic.getQualifiedName().toString())) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Can't infer registry key for type '" + generic.getQualifiedName() + "'. Set it by annotation value.", param);
            throw new FailureException();
        }
        return new GeneratedCodec.CodecRegistry(typeFqn, typeFqnBoxed, path == null ? null : namespace, path, genericType.toString(), generic.getQualifiedName().toString(), getter.get());
    }
}
