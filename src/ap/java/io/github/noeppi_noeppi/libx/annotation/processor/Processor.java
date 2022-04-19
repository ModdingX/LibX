package io.github.noeppi_noeppi.libx.annotation.processor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Processor extends AbstractProcessor implements ProcessorEnv {

    private Types types;
    private Elements elements;
    private Filer filer;
    private Messager messager;
    private Map<String, String> options;
    
    private Set<String> supported = null;
    
    private RoundEnvironment roundEnv = null;

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
                throw new IllegalArgumentException("Failed to resolve class to a type: " + clazz);
            }
        } else {
            return this.typeElement(clazz).asType();
        }
    }

    @Override
    public TypeMirror forClass(String binaryName) {
        if (binaryName.startsWith("[")) {
            return this.forDescriptor(binaryName);
        } else {
            return this.typeElement(binaryName).asType();
        }
    }
    
    private TypeMirror forDescriptor(String descriptor) {
        if (descriptor.startsWith("[")) {
            return this.types.getArrayType(this.forDescriptor(descriptor.substring(1)));
        } else if (descriptor.equals("V")) {
            return this.types.getNoType(TypeKind.VOID);
        } else if (descriptor.equals("Z")) {
            return this.types.getPrimitiveType(TypeKind.BOOLEAN);
        } else if (descriptor.equals("B")) {
            return this.types.getPrimitiveType(TypeKind.BYTE);
        } else if (descriptor.equals("C")) {
            return this.types.getPrimitiveType(TypeKind.CHAR);
        } else if (descriptor.equals("S")) {
            return this.types.getPrimitiveType(TypeKind.SHORT);
        } else if (descriptor.equals("I")) {
            return this.types.getPrimitiveType(TypeKind.INT);
        } else if (descriptor.equals("J")) {
            return this.types.getPrimitiveType(TypeKind.LONG);
        } else if (descriptor.equals("F")) {
            return this.types.getPrimitiveType(TypeKind.FLOAT);
        } else if (descriptor.equals("D")) {
            return this.types.getPrimitiveType(TypeKind.DOUBLE);
        } else if (descriptor.startsWith("L") && descriptor.endsWith(";")) {
            return this.forClass(descriptor.substring(1, descriptor.length() - 1));
        } else {
            throw new IllegalArgumentException("Invalid type descriptor: " + descriptor);
        }
    }

    @Override
    public TypeElement typeElement(Class<?> clazz) {
        TypeElement elem = this.elements.getTypeElement(clazz.getCanonicalName());
        if (elem == null) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " not found. (Source name: " + clazz.getCanonicalName() + ")");
        }
        return elem;
    }

    @Override
    public TypeElement typeElement(String binaryName) {
        TypeElement elem = this.elements.getTypeElement(Classes.sourceName(binaryName));
        if (elem == null) {
            throw new IllegalArgumentException("Type " + binaryName + " not found. (Source name: " + Classes.sourceName(binaryName) + ")");
        }
        return elem;
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
    public List<TypeElement> getAllProcessedTypes() {
        if (this.roundEnv == null) throw new IllegalStateException("round environment not initialised. This is an error in the AP " + this.getClass());
        List<TypeElement> allTypes = new ArrayList<>();
        for (Element root : this.roundEnv.getRootElements()) {
            this.addAllToList(root, allTypes);
        }
        return List.copyOf(allTypes);
    }
    
    private void addAllToList(Element element, List<TypeElement> allTypes) {
        if ((element.getKind().isClass() || element.getKind().isInterface()) && element instanceof TypeElement type) {
            allTypes.add(type);
            type.getEnclosedElements().forEach(e -> this.addAllToList(e, allTypes));
        }
    }

    @Override
    public <T> Map<String, Set<T>> getPossibleOverrideMap(TypeElement element, Function<ExecutableElement, Optional<T>> factory) {
        Map<String, Set<T>> possibleOverrides = new HashMap<>();
        List<TypeElement> parents = this.types().directSupertypes(element.asType()).stream()
                .flatMap(t -> t.getKind() == TypeKind.DECLARED && t instanceof DeclaredType declared ? Stream.of(declared.asElement()) : Stream.empty())
                .filter(e -> e.getKind().isClass() || e.getKind().isInterface())
                .flatMap(e -> e instanceof TypeElement te ? Stream.of(te) : Stream.empty())
                .toList();
        for (TypeElement parent : parents) {
            for (Element member : this.elements().getAllMembers(parent)) {
                if (member.getKind() == ElementKind.METHOD && member instanceof ExecutableElement executable) {
                    Optional<T> mapValue = factory.apply(executable);
                    mapValue.ifPresent(t -> possibleOverrides.computeIfAbsent(executable.getSimpleName().toString(), k -> new HashSet<>()).add(t));
                }
            }
        }
        return Map.copyOf(possibleOverrides);
    }

    @Override
    public List<ExecutableElement> getAllOverriddenMethods(ExecutableElement element) {
        if ((element.getEnclosingElement().getKind().isClass() || element.getEnclosingElement().getKind().isInterface()) && element.getEnclosingElement() instanceof TypeElement type) {
            String elemName = element.getSimpleName().toString();
            Map<String, Set<ExecutableElement>> map = this.getPossibleOverrideMap(type, ex -> Optional.of(ex).filter(e -> elemName.equals(e.getSimpleName().toString())));
            if (map.containsKey(elemName)) {
                return List.of();
            } else {
                return map.get(elemName).stream().filter(ex -> this.elements.overrides(element, ex, type)).toList();
            }
        } else {
            return List.of();
        }
    }

    @Override
    public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        this.roundEnv = roundEnv;
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
