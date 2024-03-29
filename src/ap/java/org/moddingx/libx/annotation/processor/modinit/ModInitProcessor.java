package org.moddingx.libx.annotation.processor.modinit;

import org.moddingx.libx.annotation.codec.Param;
import org.moddingx.libx.annotation.codec.PrimaryConstructor;
import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.annotation.config.RegisterMapper;
import org.moddingx.libx.annotation.model.Model;
import org.moddingx.libx.annotation.processor.Classes;
import org.moddingx.libx.annotation.processor.Processor;
import org.moddingx.libx.annotation.processor.modinit.codec.CodecProcessor;
import org.moddingx.libx.annotation.processor.modinit.config.RegisterConfigProcessor;
import org.moddingx.libx.annotation.processor.modinit.config.RegisterMapperProcessor;
import org.moddingx.libx.annotation.processor.modinit.model.ModelProcessor;
import org.moddingx.libx.annotation.processor.modinit.register.RegisterClassProcessor;
import org.moddingx.libx.annotation.registration.RegisterClass;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.*;

public class ModInitProcessor extends Processor implements ModEnv {

    private final Map<String, ModInit> modInits = new HashMap<>();
    private String defaultModid = null;
    private Element defaultMod = null;
    
    @Override
    public Class<?>[] getTypes() {
        return new Class[]{
                RegisterClass.class,
                Model.class,
                RegisterConfig.class,
                RegisterMapper.class,
                PrimaryConstructor.class,
                Param.class
        };
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>(super.getSupportedAnnotationTypes());
        set.add(Classes.sourceName(Classes.MOD));
        set.add(Classes.sourceName(Classes.FOR_MOD));
        return set;
    }

    @Override
    public void run(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        this.modInits.clear();
        this.defaultModid = null;
        this.defaultMod = null;
        
        {
            TypeElement modAnnotation = this.typeElement(Classes.MOD);
            Set<? extends Element> elems = roundEnv.getElementsAnnotatedWith(modAnnotation);
            if (elems.size() == 1) {
                Element elem = elems.iterator().next();
                String modid = this.modidFromAnnotation(elems.iterator().next());
                if (modid != null) {
                    this.defaultModid = modid;
                    this.defaultMod = elem;
                }
            }
        }
        
        for (Element element : roundEnv.getElementsAnnotatedWith(RegisterClass.class)) {
            try {
                RegisterClassProcessor.processRegisterClass(element, this);
            } catch (FailureException e) {
                //
            }
        }
        
        for (Element element : roundEnv.getElementsAnnotatedWith(Model.class)) {
            try {
                ModelProcessor.processModel(element, this);
            } catch (FailureException e) {
                //
            }
        }
        
        for (Element element : roundEnv.getElementsAnnotatedWith(RegisterMapper.class)) {
            try {
                RegisterMapperProcessor.processRegisterMapper(element, this);
            } catch (FailureException e) {
                //
            }
        }
        
        for (Element element : roundEnv.getElementsAnnotatedWith(RegisterConfig.class)) {
            try {
                RegisterConfigProcessor.processRegisterConfig(element, this);
            } catch (FailureException e) {
                //
            }
        }
        
        for (Element element : roundEnv.getElementsAnnotatedWith(Param.class)) {
            try {
                CodecProcessor.processAnyParam(element, "Param", this);
            } catch (FailureException e) {
                //
            }
        }
        
        for (Element element : roundEnv.getElementsAnnotatedWith(PrimaryConstructor.class)) {
            try {
                CodecProcessor.processPrimaryConstructor(element, this);
            } catch (FailureException e) {
                //
            }
        }
        
        for (ModInit mod : this.modInits.values()) {
            mod.write(this.filer(), this.messager());
        }
    }

    @Override
    public ModInit getMod(Element element) {
        return this.getMod(element, element);
    }

    private ModInit getMod(Element element, Element root) {
        List<? extends AnnotationMirror> annotations = element.getAnnotationMirrors();
        for (AnnotationMirror mirror : annotations) {
            if (this.sameErasure(mirror.getAnnotationType().asElement().asType(), this.forClass(Classes.FOR_MOD))) {
                Object typeValue = null;
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {
                    if ("value".equals(entry.getKey().getSimpleName().toString())) {
                        typeValue = entry.getValue().getValue();
                        break;
                    }
                }
                if (typeValue == null) {
                    throw new IllegalStateException("Invalid @ForMod annotation: No value set.");
                } else if (!(typeValue instanceof TypeMirror modClass)) {
                    throw new IllegalStateException("Invalid @ForMod annotation: Value is not a type.");
                } else {
                    String modid = ModInitProcessor.this.modidFromAnnotation(ModInitProcessor.this.types().asElement(modClass));
                    if (modid == null) {
                        ModInitProcessor.this.messager().printMessage(Diagnostic.Kind.ERROR, "Class used in @ForMod is not annotated with @Mod");
                    }
                    if (!this.modInits.containsKey(modid)) {
                        this.modInits.put(modid, new ModInit(modid, ModInitProcessor.this.types().asElement(modClass), ModInitProcessor.this.messager()));
                    }
                    return this.modInits.get(modid);
                }
            }
        }
        if (element.getEnclosingElement() != null) {
            return this.getMod(element.getEnclosingElement(), root);
        } else if (element instanceof PackageElement pkgElem && !pkgElem.isUnnamed()) {
            String name = pkgElem.getQualifiedName().toString();
            if (!name.contains(".")) {
                return this.getMod(ModInitProcessor.this.elements().getPackageElement(""), root);
            } else {
                return this.getMod(ModInitProcessor.this.elements().getPackageElement(name.substring(0, name.lastIndexOf('.'))), root);
            }
        } else if (this.defaultModid != null && this.defaultMod != null) {
            if (!this.modInits.containsKey(this.defaultModid)) {
                this.modInits.put(this.defaultModid, new ModInit(this.defaultModid, this.defaultMod, ModInitProcessor.this.messager()));
            }
            return this.modInits.get(this.defaultModid);
        } else {
            ModInitProcessor.this.messager().printMessage(Diagnostic.Kind.ERROR, "Could not infer modid for element. Use an @ForMod annotation.", root);
            return new ModInit("", this.typeElement(Classes.MODX), ModInitProcessor.this.messager());
        }
    }
    
    private String modidFromAnnotation(Element element) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (this.sameErasure(this.forClass(Classes.MOD), mirror.getAnnotationType())) {
                //noinspection OptionalGetWithoutIsPresent
                return mirror.getElementValues().entrySet().stream()
                        .filter(e -> e.getKey().getSimpleName().contentEquals("value"))
                        .findFirst().map(Map.Entry::getValue)
                        .map(v -> v.getValue().toString()).get();
            }
        }
        return null;
    }
}
