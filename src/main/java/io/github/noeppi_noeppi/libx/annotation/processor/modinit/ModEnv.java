package io.github.noeppi_noeppi.libx.annotation.processor.modinit;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface ModEnv {
    
    ModInit getMod(Element element);
    Types types();
    Elements elements();
    Filer filer();
    Messager messager();
    
    TypeMirror forClass(Class<?> clazz);
    boolean isSuppressed(Element element, String warnings);
    <T extends Element> Optional<T> contained(Element element, Class<T> clazz);
    <T extends Element> Optional<T> contained(Element element, Class<T> clazz, Predicate<T> filter);
    boolean sameErasure(TypeMirror type1, TypeMirror type2);
    TypeMirror classType(Supplier<Class<?>> accessor);
    List<? extends TypeMirror> classTypes(Supplier<List<Class<?>>> accessor);
    TypeMirror boxed(TypeMirror type);
    TypeMirror unboxed(TypeMirror type);
}
