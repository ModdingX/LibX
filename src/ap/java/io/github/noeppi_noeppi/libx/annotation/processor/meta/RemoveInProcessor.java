package io.github.noeppi_noeppi.libx.annotation.processor.meta;

import io.github.noeppi_noeppi.libx.annotation.meta.RemoveIn;
import io.github.noeppi_noeppi.libx.annotation.processor.ExternalProperties;
import io.github.noeppi_noeppi.libx.annotation.processor.Processor;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

public class RemoveInProcessor extends Processor {

    @Override
    public Class<?>[] getTypes() {
        return new Class[]{
                RemoveIn.class
        };
    }

    @Override
    public void run(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(RemoveIn.class)) {
            Deprecated deprecated = element.getAnnotation(Deprecated.class);
            if (deprecated == null || !deprecated.forRemoval()) {
                this.messager().printMessage(Diagnostic.Kind.ERROR, "Elements annotated with @RemoveIn must be annotated with @Deprecated(forRemoval = true)", element);
                continue;
            }
            RemoveIn remove = element.getAnnotation(RemoveIn.class);
            if (remove.minecraft().isEmpty() && remove.mod().isEmpty()) {
                this.messager().printMessage(Diagnostic.Kind.ERROR, "@RemoveIn has no properties set.", element);
                continue;
            }
            if (!remove.minecraft().isEmpty()) {
                ArtifactVersion minecraftVersion = ExternalProperties.minecraftVersion(this);
                if (minecraftVersion != null) {
                    ArtifactVersion ver = ArtifactVersion.parse(remove.minecraft());
                    if (minecraftVersion.compareTo(ver) >= 0) {
                        this.messager().printMessage(Diagnostic.Kind.ERROR, "Element should have been removed.", element);
                        continue;
                    }
                }
            }
            if (!remove.mod().isEmpty()) {
                ArtifactVersion modVersion = ExternalProperties.modVersion(this);
                if (modVersion != null) {
                    ArtifactVersion ver = ArtifactVersion.parse(remove.mod());
                    if (modVersion.compareTo(ver) > 0) {
                        this.messager().printMessage(Diagnostic.Kind.ERROR, "Element should have been removed.", element);
                    }
                }
            }
        }
    }
}
