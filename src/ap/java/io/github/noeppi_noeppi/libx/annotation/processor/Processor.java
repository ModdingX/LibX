package io.github.noeppi_noeppi.libx.annotation.processor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
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

public abstract class Processor extends AbstractProcessor implements ProcessorEnv {

    private Types types;
    private Elements elements;
    private Filer filer;
    private Messager messager;
    private Map<String, String> options;
    
    private Set<String> supported = null;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.types = processingEnv.getTypeUtils();
        this.elements = processingEnv.getElementUtils();
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
        this.options = Map.copyOf(processingEnv.getOptions());
    }

    public abstract Class<?>[] getTypes();
    
    @Override
    public Types types() {
        return Objects.requireNonNull(this.types);
    }

    @Override
    public Elements elements() {
        return Objects.requireNonNull(this.elements);
    }

    @Override
    public Filer filer() {
        return Objects.requireNonNull(this.filer);
    }

    @Override
    public Messager messager() {
        return Objects.requireNonNull(this.messager);
    }

    @Override
    public Map<String, String> options() {
        return Objects.requireNonNull(this.options);
    }

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
        return TARGET;
    }

    @Override
    public TypeMirror forClass(Class<?> clazz) {
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

    @Override
    public boolean isSuppressed(Element element, String warnings) {
        SuppressWarnings sw = element.getAnnotation(SuppressWarnings.class);
        if (sw != null) {
            return Arrays.asList(sw.value()).contains(warnings);
        }
        return false;
    }

    @Override
    public  <T extends Element> Optional<T> contained(Element element, Class<T> clazz) {
        return this.contained(element, clazz, elem -> true);
    }

    @Override
    public  <T extends Element> Optional<T> contained(Element element, Class<T> clazz, Predicate<T> filter) {
        //noinspection unchecked
        return (Optional<T>) element.getEnclosedElements().stream()
                .filter(e -> clazz.isAssignableFrom(e.getClass()))
                .filter(e -> filter.test((T) e))
                .findFirst();
    }

    @Override
    public boolean sameErasure(TypeMirror type1, TypeMirror type2) {
        return this.types.isSameType(this.types.erasure(type1), this.types.erasure(type2));
    }

    @Override
    public boolean subTypeErasure(TypeMirror child, TypeMirror parent) {
        try {
            return this.types.isSubtype(this.types.erasure(child), this.types.erasure(parent));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public TypeMirror classType(Supplier<Class<?>> accessor) {
        try {
            return this.forClass(accessor.get());
        } catch (MirroredTypeException e) {
            return e.getTypeMirror();
        }
    }

    @Override
    public List<? extends TypeMirror> classTypes(Supplier<List<Class<?>>> accessor) {
        try {
            return accessor.get().stream().map(this::forClass).collect(Collectors.toList());
        } catch (MirroredTypesException e) {
            return e.getTypeMirrors();
        }
    }

    @Override
    public TypeMirror boxed(TypeMirror type) {
        if (type.getKind() == TypeKind.VOID) {
            return this.forClass(Void.class);
        } else if (type.getKind() == TypeKind.BOOLEAN) {
            return this.forClass(Boolean.class);
        } else if (type.getKind() == TypeKind.BYTE) {
            return this.forClass(Byte.class);
        } else if (type.getKind() == TypeKind.CHAR) {
            return this.forClass(Character.class);
        } else if (type.getKind() == TypeKind.SHORT) {
            return this.forClass(Short.class);
        } else if (type.getKind() == TypeKind.INT) {
            return this.forClass(Integer.class);
        } else if (type.getKind() == TypeKind.LONG) {
            return this.forClass(Long.class);
        } else if (type.getKind() == TypeKind.FLOAT) {
            return this.forClass(Float.class);
        } else if (type.getKind() == TypeKind.DOUBLE) {
            return this.forClass(Double.class);
        } else {
            return type;
        }
    }
    
    @Override
    public TypeMirror unboxed(TypeMirror type) {
        if (this.sameErasure(type, this.forClass(Void.class))) {
            return this.forClass(void.class);
        } else if (this.sameErasure(type, this.forClass(Boolean.class))) {
            return this.forClass(boolean.class);
        } else if (this.sameErasure(type, this.forClass(Byte.class))) {
            return this.forClass(byte.class);
        } else if (this.sameErasure(type, this.forClass(Character.class))) {
            return this.forClass(char.class);
        } else if (this.sameErasure(type, this.forClass(Short.class))) {
            return this.forClass(short.class);
        } else if (this.sameErasure(type, this.forClass(Integer.class))) {
            return this.forClass(int.class);
        } else if (this.sameErasure(type, this.forClass(Long.class))) {
            return this.forClass(long.class);
        } else if (this.sameErasure(type, this.forClass(Float.class))) {
            return this.forClass(float.class);
        } else if (this.sameErasure(type, this.forClass(Double.class))) {
            return this.forClass(double.class);
        } else {
            return type;
        }
    }

    @Override
    public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        this.run(annotations, roundEnv);
        return false;
    }
    
    public abstract void run(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv);

    @Override
    public Set<String> getSupportedOptions() {
        return Set.of(
                "mod.properties.mod_id",
                "mod.properties.mc_version",
                "mod.properties.mod_version",
                "mod.properties.java_version",
                "mod.properties.release"
        );
    }
}
