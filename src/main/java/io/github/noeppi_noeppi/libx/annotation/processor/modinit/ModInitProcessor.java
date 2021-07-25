package io.github.noeppi_noeppi.libx.annotation.processor.modinit;

import io.github.noeppi_noeppi.libx.annotation.ForMod;
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

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ModInitProcessor extends Processor {

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
                Param.class,
                PrimaryConstructor.class,
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
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<String, ModInit> modInits = new HashMap<>();
        String defaultModid = null;
        Element defaultMod = null;
        {
            TypeElement modAnnotation = this.elements.getTypeElement(ModInit.MOD_ANNOTATION_TYPE);
            Set<? extends Element> elems = roundEnv.getElementsAnnotatedWith(modAnnotation);
            if (elems.size() == 1) {
                Element elem = elems.iterator().next();
                String modid = this.modidFromAnnotation(elems.iterator().next());
                if (modid != null) {
                    defaultModid = modid;
                    defaultMod = elem;
                }
            }
        }
        String effectiveFinalDefaultModid = defaultModid;
        Element effectiveFinalDefaultMod = defaultMod;
        class Local implements ModEnv {

            @Override
            public Types types() {
                return ModInitProcessor.this.types;
            }

            @Override
            public Elements elements() {
                return ModInitProcessor.this.elements;
            }

            @Override
            public Filer filer() {
                return ModInitProcessor.this.filer;
            }

            @Override
            public Messager messager() {
                return ModInitProcessor.this.messager;
            }

            @Override
            public TypeMirror forClass(Class<?> clazz) {
                return ModInitProcessor.this.forClass(clazz);
            }

            @Override
            public boolean isSuppressed(Element element, String warnings) {
                return ModInitProcessor.this.isSuppressed(element, warnings);
            }

            @Override
            public <T extends Element> Optional<T> contained(Element element, Class<T> clazz) {
                return ModInitProcessor.this.contained(element, clazz);
            }

            @Override
            public <T extends Element> Optional<T> contained(Element element, Class<T> clazz, Predicate<T> filter) {
                return ModInitProcessor.this.contained(element, clazz, filter);
            }

            @Override
            public boolean sameErasure(TypeMirror type1, TypeMirror type2) {
                return ModInitProcessor.this.sameErasure(type1, type2);
            }

            @Override
            public boolean subTypeErasure(TypeMirror child, TypeMirror parent) {
                return ModInitProcessor.this.subTypeErasure(child, parent);
            }

            @Override
            public TypeMirror classType(Supplier<Class<?>> accessor) {
                return ModInitProcessor.this.classType(accessor);
            }

            @Override
            public List<? extends TypeMirror> classTypes(Supplier<List<Class<?>>> accessor) {
                return ModInitProcessor.this.classTypes(accessor);
            }

            @Override
            public TypeMirror boxed(TypeMirror type) {
                return ModInitProcessor.this.boxed(type);
            }

            @Override
            public TypeMirror unboxed(TypeMirror type) {
                return ModInitProcessor.this.unboxed(type);
            }

            @Override
            public ModInit getMod(Element element) {
                return this.getMod(element, element);
            }

            private ModInit getMod(Element element, Element root) {
                ForMod forMod = element.getAnnotation(ForMod.class);
                if (forMod != null) {
                    TypeMirror modClass = ModInitProcessor.this.classType(forMod::value);
                    String modid = ModInitProcessor.this.modidFromAnnotation(ModInitProcessor.this.types.asElement(modClass));
                    if (modid == null) {
                        ModInitProcessor.this.messager.printMessage(Diagnostic.Kind.ERROR, "Class used in @ForMod is not annotated with @Mod");
                    }
                    if (!modInits.containsKey(modid)) {
                        modInits.put(modid, new ModInit(modid, ModInitProcessor.this.types.asElement(modClass), ModInitProcessor.this.messager));
                    }
                    return modInits.get(modid);
                } else if (element.getEnclosingElement() != null) {
                    return this.getMod(element.getEnclosingElement(), root);
                } else if (element instanceof PackageElement && !((PackageElement) element).isUnnamed()) {
                    String name = ((PackageElement) element).getQualifiedName().toString();
                    if (!name.contains(".")) {
                        return this.getMod(ModInitProcessor.this.elements.getPackageElement(""), root);
                    } else {
                        return this.getMod(ModInitProcessor.this.elements.getPackageElement(name.substring(0, name.lastIndexOf('.'))), root);
                    }
                } else if (effectiveFinalDefaultModid != null && effectiveFinalDefaultMod != null) {
                    if (!modInits.containsKey(effectiveFinalDefaultModid)) {
                        modInits.put(effectiveFinalDefaultModid, new ModInit(effectiveFinalDefaultModid, effectiveFinalDefaultMod, ModInitProcessor.this.messager));
                    }
                    return modInits.get(effectiveFinalDefaultModid);
                } else {
                    ModInitProcessor.this.messager.printMessage(Diagnostic.Kind.ERROR, "Could not infer modid for element. Use an @ForMod annotation.", root);
                    return new ModInit("", ModInitProcessor.this.types.asElement(ModInitProcessor.this.forClass(ModX.class)), ModInitProcessor.this.messager);
                }
            }
        }
        Local local = new Local();
        for (Element element : roundEnv.getElementsAnnotatedWith(RegisterClass.class)) {
            try {
                RegisterClassProcessor.processRegisterClass(element, local);
            } catch (FailureException e) {
                //
            }
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(Model.class)) {
            try {
                ModelProcessor.processModel(element, local);
            } catch (FailureException e) {
                //
            }
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(RegisterMapper.class)) {
            try {
                RegisterMapperProcessor.processRegisterMapper(element, local);
            } catch (FailureException e) {
                //
            }
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(RegisterConfig.class)) {
            try {
                RegisterConfigProcessor.processRegisterConfig(element, local);
            } catch (FailureException e) {
                //
            }
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(Param.class)) {
            try {
                CodecProcessor.processParam(element, local);
            } catch (FailureException e) {
                //
            }
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(PrimaryConstructor.class)) {
            try {
                CodecProcessor.processPrimaryConstructor(element, local);
            } catch (FailureException e) {
                //
            }
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(Datagen.class)) {
            try {
                DatagenProcessor.processDatagen(element, local);
            } catch (FailureException e) {
                //
            }
        }
        for (ModInit mod : modInits.values()) {
            mod.write(this.filer, this.messager);
        }
        return true;
    }

    private String modidFromAnnotation(Element element) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (this.sameErasure(this.elements.getTypeElement(ModInit.MOD_ANNOTATION_TYPE).asType(), mirror.getAnnotationType())) {
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
