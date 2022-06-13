package org.moddingx.libx.annotation.processor.modinit.register;

import org.moddingx.libx.annotation.processor.Classes;
import org.moddingx.libx.annotation.processor.modinit.FailureException;
import org.moddingx.libx.annotation.processor.modinit.ModEnv;
import org.moddingx.libx.annotation.processor.modinit.ModInit;
import org.moddingx.libx.annotation.registration.Reg;
import org.moddingx.libx.annotation.registration.RegisterClass;

import javax.annotation.Nullable;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegisterClassProcessor {

    public static void processRegisterClass(Element element, ModEnv env) {
        if (element.getKind() != ElementKind.CLASS || !element.getModifiers().contains(Modifier.PUBLIC)) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Element annotated with @RegisterClass is not a public class.", element);
            return;
        }
        if (element.getEnclosingElement().getKind() != ElementKind.PACKAGE || !(element.getEnclosingElement() instanceof PackageElement)) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Parent of element annotated with @RegisterClass is not a package", element);
            return;
        }
        
        RegisterClass registerClass = element.getAnnotation(RegisterClass.class);
        ModInit mod = env.getMod(element);

        if (!env.subTypeErasure(mod.modClass.asType(), env.forClass(Classes.MODX_REGISTRATION))) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "@RegisterClass used with a mod that is not a subtype of ModXRegistration", element);
            return;
        }
        
        TargetRegistry target = resolveRegistry(element, registerClass, env);
        
        List<RegistrationEntry> entries = element.getEnclosedElements().stream().flatMap(e -> fromElement(registerClass, e, target, env)).collect(Collectors.toList());
        mod.addRegistration(registerClass.priority(), entries);
    }
    
    private static TargetRegistry resolveRegistry(Element classElem, RegisterClass classAnnotation, ModEnv env) {
        if (classAnnotation.registry().isEmpty()) {
            return new TargetRegistry(null, null);
        }
        
        List<TypeElement> classesToCheck;
        
        TypeMirror registryClass = env.classType(classAnnotation::registryClass);
        if (registryClass.getKind() == TypeKind.VOID) {
            classesToCheck = List.of(
                    env.typeElement(Classes.FORGE_KEYS),
                    env.typeElement(Classes.REGISTRY)
            );
        } else {
            classesToCheck = List.of(env.typeElement(registryClass));
        }
        for (TypeElement cls : classesToCheck) {
            for (Element elem : cls.getEnclosedElements()) {
                if (elem.getKind() == ElementKind.FIELD && elem.getModifiers().contains(Modifier.PUBLIC)
                        && elem.getModifiers().contains(Modifier.STATIC) && elem.getModifiers().contains(Modifier.FINAL)
                        && elem instanceof VariableElement field && field.getSimpleName().contentEquals(classAnnotation.registry())) {
                    if (env.sameErasure(env.forClass(Classes.RESOURCE_KEY), field.asType())) {
                        TypeMirror generic = generic(field, field.asType(), "Registry key has invalid type", env);
                        if (!env.sameErasure(env.forClass(Classes.REGISTRY), generic)) {
                            env.messager().printMessage(Diagnostic.Kind.ERROR, "Registry key is not a root key or has too generic type: " + generic);
                            throw new FailureException();
                        }
                        TypeMirror elemType = generic(field, generic, "Registry key has invalid element type", env);
                        return new TargetRegistry(elemType, cls.getQualifiedName().toString() + "." + elem.getSimpleName().toString());
                    }
                }
            }
        }
        env.messager().printMessage(Diagnostic.Kind.ERROR, "Failed to resolve target registry: " + (registryClass.getKind() == TypeKind.VOID ? "" : registryClass + ".") + classAnnotation.registry(), classElem);
        throw new FailureException();
    }
    
    private static TypeMirror generic(Element elem, TypeMirror type, String error, ModEnv env) {
        if (type.getKind() == TypeKind.DECLARED && type instanceof DeclaredType declared && declared.getTypeArguments().size() == 1) {
            TypeMirror genericType = declared.getTypeArguments().get(0);
            if (genericType.getKind() == TypeKind.WILDCARD && genericType instanceof WildcardType wildcard && wildcard.getExtendsBound() != null) {
                return wildcard.getExtendsBound();
            } else {
                return genericType;
            }
        } else {
            env.messager().printMessage(Diagnostic.Kind.ERROR, error + ": " + type, elem);
            throw new FailureException();
        }
    }
    
    private static Stream<RegistrationEntry> fromElement(RegisterClass classAnnotation, Element element, TargetRegistry target, ModEnv env) {
        if (element.getKind() != ElementKind.FIELD || element.getAnnotation(Reg.class) != null) {
            return Stream.empty();
        } else if (!(element.getEnclosingElement() instanceof QualifiedNameable qualified)) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Failed to get qualified name for member: " + element, element.getEnclosingElement());
            return Stream.empty();
        } else {
            if (!element.getModifiers().contains(Modifier.STATIC)) {
                env.messager().printMessage(Diagnostic.Kind.WARNING, "Skipping non-static field for automatic registration. Use @Reg.Exclude to suppress.", element);
                return Stream.empty();
            }
            if (!element.getModifiers().contains(Modifier.FINAL)) {
                env.messager().printMessage(Diagnostic.Kind.WARNING, "Skipping non-final field for automatic registration. Use @Reg.Exclude to suppress.", element);
                return Stream.empty();
            }
            if (!element.getModifiers().contains(Modifier.PUBLIC) && !element.getModifiers().contains(Modifier.PRIVATE)) {
                env.messager().printMessage(Diagnostic.Kind.WARNING, "Skipping non-public and non-private member for automatic registration. Use @Reg.Exclude to suppress.", element);
                return Stream.empty();
            }
            
            
            
            boolean hasMultiType = env.types().isSubtype(element.asType(), env.forClass(Classes.MULTI_REGISTERABLE));
            boolean multi = element.getAnnotation(Reg.Multi.class) != null;
            boolean maybeMissingMultiAnnotation = !multi && hasMultiType;
            
            if (target.baseType != null && !env.types().isSubtype(element.asType(), target.baseType)) {
                env.messager().printMessage(Diagnostic.Kind.ERROR, "Field has invalid type for target registry" + (maybeMissingMultiAnnotation ? " (Missing @Reg.Multi ?)" : "") + ": expected " + target.baseType, element);
                return Stream.empty();
            } else if (maybeMissingMultiAnnotation && !env.isSuppressed(element, "registration")) {
                env.messager().printMessage(Diagnostic.Kind.WARNING, "Field has is an instance of MultiRegisterable. Missing @Reg.Multi ?", element);
            }
            
            String name;
            if (element.getAnnotation(Reg.Name.class) != null) {
                name = element.getAnnotation(Reg.Name.class).value();
            } else {
                StringBuilder sb = new StringBuilder();
                for (char chr : element.getSimpleName().toString().toCharArray()) {
                    if (Character.isUpperCase(chr)) {
                        sb.append('_');
                    }
                    sb.append(Character.toLowerCase(chr));
                }
                name = sb.toString();
            }
            
            if (!classAnnotation.prefix().isEmpty()) {
                name = classAnnotation.prefix() + "_" + name;
            }
            
            return Stream.of(new RegistrationEntry(target.fqn(), name, qualified.getQualifiedName().toString(), element.getSimpleName().toString(), multi));
        }
    }
    
    record TargetRegistry(@Nullable TypeMirror baseType, @Nullable String fqn) {}
}
