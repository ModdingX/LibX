package io.github.noeppi_noeppi.libx.annotation.processor.modinit;

import io.github.noeppi_noeppi.libx.annotation.ForMod;
import io.github.noeppi_noeppi.libx.annotation.codec.Lookup;
import io.github.noeppi_noeppi.libx.annotation.codec.Param;
import io.github.noeppi_noeppi.libx.annotation.codec.PrimaryConstructor;
import io.github.noeppi_noeppi.libx.annotation.config.RegisterConfig;
import io.github.noeppi_noeppi.libx.annotation.config.RegisterMapper;
import io.github.noeppi_noeppi.libx.annotation.data.Datagen;
import io.github.noeppi_noeppi.libx.annotation.model.Model;
import io.github.noeppi_noeppi.libx.annotation.processor.Processor;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.codec.CodecProcessor;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.config.RegisterConfigProcessor;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.config.RegisterMapperProcessor;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.data.DatagenProcessor;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.model.ModelProcessor;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.register.RegisterClassProcessor;
import io.github.noeppi_noeppi.libx.annotation.registration.NoReg;
import io.github.noeppi_noeppi.libx.annotation.registration.RegName;
import io.github.noeppi_noeppi.libx.annotation.registration.RegisterClass;
import io.github.noeppi_noeppi.libx.mod.ModX;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModInitProcessor extends Processor implements ModEnv {

    private final Map<String, ModInit> modInits = new HashMap<>();
    private String defaultModid = null;
    private Element defaultMod = null;
    
    @Override
    public Class<?>[] getTypes() {
        return new Class[]{
                ForMod.class,
                RegisterClass.class,
                NoReg.class,
                RegName.class,
                Model.class,
                RegisterConfig.class,
                RegisterMapper.class,
                PrimaryConstructor.class,
                Param.class,
                Lookup.class,
                Datagen.class
        };
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>(super.getSupportedAnnotationTypes());
        set.add(ModInit.MOD_ANNOTATION_TYPE);
        return set;
    }

    @Override
    public void run(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        this.modInits.clear();
        this.defaultModid = null;
        this.defaultMod = null;
        
        {
            TypeElement modAnnotation = this.elements().getTypeElement(ModInit.MOD_ANNOTATION_TYPE);
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
        for (Element element : roundEnv.getElementsAnnotatedWith(Lookup.class)) {
            try {
                CodecProcessor.processAnyParam(element, "Lookup", this);
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
        for (Element element : roundEnv.getElementsAnnotatedWith(Datagen.class)) {
            try {
                DatagenProcessor.processDatagen(element, this);
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
        ForMod forMod = element.getAnnotation(ForMod.class);
        if (forMod != null) {
            TypeMirror modClass = ModInitProcessor.this.classType(forMod::value);
            String modid = ModInitProcessor.this.modidFromAnnotation(ModInitProcessor.this.types().asElement(modClass));
            if (modid == null) {
                ModInitProcessor.this.messager().printMessage(Diagnostic.Kind.ERROR, "Class used in @ForMod is not annotated with @Mod");
            }
            if (!this.modInits.containsKey(modid)) {
                this.modInits.put(modid, new ModInit(modid, ModInitProcessor.this.types().asElement(modClass), ModInitProcessor.this.messager()));
            }
            return this.modInits.get(modid);
        } else if (element.getEnclosingElement() != null) {
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
            return new ModInit("", ModInitProcessor.this.types().asElement(ModInitProcessor.this.forClass(ModX.class)), ModInitProcessor.this.messager());
        }
    }
    
    private String modidFromAnnotation(Element element) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (this.sameErasure(this.elements().getTypeElement(ModInit.MOD_ANNOTATION_TYPE).asType(), mirror.getAnnotationType())) {
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
