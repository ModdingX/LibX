package io.github.noeppi_noeppi.libx.annotation.processor;

import io.github.noeppi_noeppi.libx.mod.registration.NoReg;
import io.github.noeppi_noeppi.libx.mod.registration.RegName;
import io.github.noeppi_noeppi.libx.mod.registration.Registrate;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

public class RegistrateModifyProcessor extends Processor {

    @Override
    public Class<?>[] getTypes() {
        return new Class[]{ NoReg.class, RegName.class };
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(NoReg.class)) {
            if (element.getEnclosingElement().getAnnotation(Registrate.class) == null) {
                this.messager.printMessage(Diagnostic.Kind.ERROR, "Can not exclude value from automatic registration outside class annotated with @Registrate", element);
            }
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(RegName.class)) {
            if (element.getEnclosingElement().getAnnotation(Registrate.class) == null) {
                this.messager.printMessage(Diagnostic.Kind.ERROR, "Can not alter registry name outside class annotated with @Registrate", element);
            }
            if (element.getAnnotation(NoReg.class) != null) {
                this.messager.printMessage(Diagnostic.Kind.ERROR, "Can not alter registry name of ignored field.", element);
            }
        }
        return true;
    }
}
