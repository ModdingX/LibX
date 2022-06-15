package org.moddingx.libx.annotation.processor.misc;

import org.moddingx.libx.annotation.processor.Classes;
import org.moddingx.libx.annotation.processor.Processor;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.HashSet;
import java.util.Set;

public class FindBugsProcessor extends Processor {

    @Override
    public Class<?>[] getTypes() {
        return new Class[]{};
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>(super.getSupportedAnnotationTypes());
        set.add(Classes.sourceName(Classes.JETBRAINS_NOTNULL));
        set.add(Classes.sourceName(Classes.JETBRAINS_NULLABLE));
        return set;
    }

    @Override
    public void run(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!this.options().containsKey("mod.properties.force_findbugs") || !Boolean.parseBoolean(this.options().get("mod.properties.force_findbugs"))) return;
        for (Element element : roundEnv.getElementsAnnotatedWith(this.typeElement(Classes.JETBRAINS_NOTNULL))) {
            this.messager().printMessage(Diagnostic.Kind.ERROR, "This should use javax.annotation.Nonnull.", element);
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(this.typeElement(Classes.JETBRAINS_NULLABLE))) {
            this.messager().printMessage(Diagnostic.Kind.ERROR, "This should use javax.annotation.Nullable.", element);
        }
    }
}
