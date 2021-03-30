package io.github.noeppi_noeppi.libx.annotation.processor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class Processor extends AbstractProcessor {

    protected Types types;
    protected Elements elements;
    protected Filer filer;
    protected Messager messager;
    protected PackageElement base;
    private Set<String> supported = null;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.types = processingEnv.getTypeUtils();
        this.elements = processingEnv.getElementUtils();
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
    }

    public abstract Class<?>[] getTypes();

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        if (this.supported == null) {
            Set<String> s = new HashSet<>();
            for (Class<?> clazz : this.getTypes()) {
                s.add(clazz.getCanonicalName());
            }
            this.supported = s;
        }
        return this.supported;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    protected TypeMirror forClass(Class<?> clazz) {
        if (clazz.isArray()) {
            return this.types.getArrayType(this.forClass(clazz.getComponentType()));
        } else if (clazz.isPrimitive()) {
            if (clazz == void.class) {
                return this.types.getNoType(TypeKind.VOID);
            } else if (clazz == boolean.class) {
                return this.types.getPrimitiveType(TypeKind.BOOLEAN);
            } else if (clazz == byte.class) {
                return this.types.getPrimitiveType(TypeKind.BYTE);
            } else if (clazz == char.class) {
                return this.types.getPrimitiveType(TypeKind.CHAR);
            } else if (clazz == short.class) {
                return this.types.getPrimitiveType(TypeKind.SHORT);
            } else if (clazz == int.class) {
                return this.types.getPrimitiveType(TypeKind.INT);
            } else if (clazz == long.class) {
                return this.types.getPrimitiveType(TypeKind.LONG);
            } else if (clazz == float.class) {
                return this.types.getPrimitiveType(TypeKind.FLOAT);
            } else if (clazz == double.class) {
                return this.types.getPrimitiveType(TypeKind.DOUBLE);
            } else {
                return null;
            }
        } else {
            return this.elements.getTypeElement(clazz.getCanonicalName()).asType();
        }
    }

    protected boolean isSuppressed(Element element, String warnings) {
        SuppressWarnings sw = element.getAnnotation(SuppressWarnings.class);
        if (sw != null) {
            return Arrays.asList(sw.value()).contains(warnings);
        }
        return false;
    }
    
    protected <T extends Element> Optional<T> contained(Element element, Class<T> clazz) {
        return this.contained(element, clazz, elem -> true);
    }
    
    protected <T extends Element> Optional<T> contained(Element element, Class<T> clazz, Predicate<T> filter) {
        //noinspection unchecked
        return (Optional<T>) element.getEnclosedElements().stream()
                .filter(e -> clazz.isAssignableFrom(e.getClass()))
                .filter(e -> filter.test((T) e))
                .findFirst();
    }
    
    protected boolean sameErasure(TypeMirror type1, TypeMirror type2) {
        return this.types.isSameType(this.types.erasure(type1), this.types.erasure(type2));
    }
    
    protected TypeMirror classType(Supplier<Class<?>> accessor) {
        try {
            return this.forClass(accessor.get());
        } catch (MirroredTypeException e) {
            return e.getTypeMirror();
        }
    }
    
    protected List<? extends TypeMirror> classTypes(Supplier<List<Class<?>>> accessor) {
        try {
            return accessor.get().stream().map(this::forClass).collect(Collectors.toList());
        } catch (MirroredTypesException e) {
            return e.getTypeMirrors();
        }
    }
}