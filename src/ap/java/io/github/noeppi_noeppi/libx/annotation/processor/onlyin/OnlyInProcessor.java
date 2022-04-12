package io.github.noeppi_noeppi.libx.annotation.processor.onlyin;

import io.github.noeppi_noeppi.libx.annotation.processor.Classes;
import io.github.noeppi_noeppi.libx.annotation.processor.Processor;

import javax.annotation.Nullable;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OnlyInProcessor extends Processor {


    @Override
    public Class<?>[] getTypes() {
        return new Class[]{};
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>(super.getSupportedAnnotationTypes());
        set.add(Classes.sourceName(Classes.ONLY_IN));
        set.add(Classes.sourceName(Classes.ONLY_INS));
        return set;
    }

    @Override
    public Set<String> getSupportedOptions() {
        Set<String> set = new HashSet<>(super.getSupportedAnnotationTypes());
        set.add("mod.properties.strict_onlyin");
        return set;
    }

    @Override
    public void run(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!this.options().containsKey("mod.properties.strict_onlyin") || !Boolean.parseBoolean(this.options().get("mod.properties.strict_onlyin"))) return;
        for (Element element : roundEnv.getElementsAnnotatedWithAny(this.typeElement(Classes.ONLY_IN), this.typeElement(Classes.ONLY_INS))) {
            Set<DistData> data = this.fromElement(element);
            Set<Dist> specificDists = data.stream().filter(d -> d.iface() == null).map(DistData::dist).collect(Collectors.toUnmodifiableSet());
            if (specificDists.size() > 1) {
                this.messager().printMessage(Diagnostic.Kind.ERROR, "@OnlyIn used with both client and server.", element);
                continue;
            }
            Dist availability = this.distFor(element, true);
            if (element.getKind().isClass() || element.getKind().isInterface()) {
                for (DistData d : data) {
                    if (d.iface() != null) {
                        if (availability != null) {
                            if (d.dist() == availability) {
                                this.messager().printMessage(Diagnostic.Kind.WARNING, "Unnecessary interface @OnlyIn, whole element is marked as " + availability.name(), element);
                            } else {
                                this.messager().printMessage(Diagnostic.Kind.ERROR, "Invalid @OnlyIn, element is marked as " + availability.name() + ", interface as " + d.dist(), element);
                                continue;
                            }
                        }
                        if (!this.types().isSubtype(this.types().erasure(element.asType()), this.types().erasure(d.iface().asType()))) {
                            this.messager().printMessage(Diagnostic.Kind.ERROR, "Invalid @OnlyIn, element does not implement interface " + d.iface().asType(), element);
                        }
                    }
                }
            }
        }
        for (Element root : roundEnv.getRootElements()) {
            if ((root.getKind().isClass() || root.getKind().isInterface()) && root instanceof TypeElement type) {
                Map<String, Set<DistOverride>> possibleOverrides = new HashMap<>();
                List<TypeElement> parents = this.types().directSupertypes(type.asType()).stream()
                        .flatMap(t -> t.getKind() == TypeKind.DECLARED && t instanceof DeclaredType declared ? Stream.of(declared.asElement()) : Stream.empty())
                        .filter(e -> e.getKind().isClass() || e.getKind().isInterface())
                        .flatMap(e -> e instanceof TypeElement te ? Stream.of(te) : Stream.empty())
                        .toList();
                for (TypeElement parent : parents) {
                    for (Element member : this.elements().getAllMembers(parent)) {
                        if (member.getKind() == ElementKind.METHOD && member instanceof ExecutableElement executable) {
                            Dist memberDist = this.distFor(executable, false);
                            if (memberDist != null) {
                                possibleOverrides.computeIfAbsent(executable.getSimpleName().toString(), k -> new HashSet<>()).add(new DistOverride(executable, memberDist));
                            }
                        }
                    }
                }
                for (Element member : type.getEnclosedElements()) {
                    if (member.getKind() == ElementKind.METHOD && member instanceof ExecutableElement executable && possibleOverrides.containsKey(member.getSimpleName().toString())) {
                        Set<Dist> parentDists = possibleOverrides.get(member.getSimpleName().toString()).stream()
                                .filter(ov -> this.elements().overrides(executable, ov.element(), type))
                                .map(DistOverride::dist)
                                .collect(Collectors.toUnmodifiableSet());
                        if (parentDists.size() == 1 && this.distFor(executable, true) != parentDists.iterator().next()) {
                            this.messager().printMessage(Diagnostic.Kind.WARNING, "Not annotated method overrides method annotated with @OnlyIn(" + parentDists.iterator().next().name() + ")", member);
                        }
                    }
                }
            }
        }
    }
    
    @Nullable
    private Dist distFor(Element element, boolean inherit) {
        Set<Dist> set = new HashSet<>(this.fromElement(element).stream().filter(d -> d.iface() == null).map(DistData::dist).collect(Collectors.toUnmodifiableSet()));
        if (inherit && (element.getKind().isField() || element.getKind() == ElementKind.METHOD || element.getKind() == ElementKind.CONSTRUCTOR)) {
            set.addAll(this.fromElement(element.getEnclosingElement()).stream().filter(d -> d.iface() == null).map(DistData::dist).collect(Collectors.toUnmodifiableSet()));
        }
        if (set.size() != 1) return null;
        return set.iterator().next();
    }
    
    private Set<DistData> fromElement(Element element) {
        Set<DistData> allDist = new HashSet<>();
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (this.sameErasure(mirror.getAnnotationType().asElement().asType(), this.forClass(Classes.ONLY_IN))) {
                DistData data = this.fromAnnotation(element, mirror);
                if (data != null) allDist.add(data);
            } else if (this.sameErasure(mirror.getAnnotationType().asElement().asType(), this.forClass(Classes.ONLY_INS))) {
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {
                    if (entry.getKey().getSimpleName().contentEquals("value")) {
                        if (entry.getValue().getValue() instanceof List<?> list) {
                            for (Object elem : list) {
                                if (elem instanceof AnnotationMirror subMirror) {
                                    DistData data = this.fromAnnotation(element, subMirror);if (data != null) allDist.add(data);
                                }
                            }
                        }
                    } 
                }
            }
        }
        return allDist;
    }
    
    @Nullable
    private DistData fromAnnotation(Element element, AnnotationMirror mirror) {
        if (!this.sameErasure(mirror.getAnnotationType().asElement().asType(), this.forClass(Classes.ONLY_IN))) return null;
        Dist dist = null;
        TypeElement iface = null;
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().contentEquals("value")) {
                if (entry.getValue().getValue() instanceof VariableElement var) {
                    if (var.getSimpleName().contentEquals(Dist.CLIENT.name())) {
                        dist = Dist.CLIENT;
                    } else if (var.getSimpleName().contentEquals(Dist.DEDICATED_SERVER.name())) {
                        dist = Dist.DEDICATED_SERVER;
                    }
                }
            } else if (entry.getKey().getSimpleName().contentEquals("_interface")) {
                if (entry.getValue().getValue() instanceof TypeMirror type && this.types().erasure(type) instanceof DeclaredType declared && declared.getKind() == TypeKind.DECLARED) {
                    Element declaredElem = declared.asElement();
                    if (declaredElem.getKind() == ElementKind.INTERFACE && declaredElem instanceof TypeElement typeElement) {
                        iface = typeElement;
                    } else {
                        this.messager().printMessage(Diagnostic.Kind.ERROR, "Value used in _interface of @OnlyIn is not an interface.", element);
                        return null;
                    }
                }
            }
        }
        if (dist == null) return null;
        return new DistData(dist, iface);
    }

    private record DistData(Dist dist, @Nullable TypeElement iface) {}
    private record DistOverride(ExecutableElement element, Dist dist) {}
    
    private enum Dist {
        CLIENT, DEDICATED_SERVER
    }
}
