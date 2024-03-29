package org.moddingx.libx.annotation.processor;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface ProcessorEnv {

    SourceVersion TARGET = SourceVersion.RELEASE_17;
    
    Types types();
    Elements elements();
    Filer filer();
    Messager messager();
    Map<String, String> options();

    TypeMirror forClass(Class<?> clazz);
    TypeMirror forClass(String binaryName);
    TypeElement typeElement(TypeMirror type);
    TypeElement typeElement(Class<?> clazz);
    TypeElement typeElement(String binaryName);
    boolean isSuppressed(Element element, String warnings);
    <T extends Element> Optional<T> contained(Element element, Class<T> clazz);
    <T extends Element> Optional<T> contained(Element element, Class<T> clazz, Predicate<T> filter);
    boolean sameErasure(TypeMirror type1, TypeMirror type2);
    boolean subTypeErasure(TypeMirror child, TypeMirror parent);
    TypeMirror classType(Supplier<Class<?>> accessor);
    List<? extends TypeMirror> classTypes(Supplier<List<Class<?>>> accessor);
    TypeMirror boxed(TypeMirror type);
    TypeMirror unboxed(TypeMirror type);
    List<TypeElement> getAllProcessedTypes();
    <T> Map<String, Set<T>> getPossibleOverrideMap(TypeElement element, Function<ExecutableElement, Optional<T>> factory);
    List<ExecutableElement> getAllOverriddenMethods(ExecutableElement element);
}
