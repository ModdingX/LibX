package io.github.noeppi_noeppi.libx.annotation.processor.modinit;

import io.github.noeppi_noeppi.libx.annotation.registration.NoReg;
import io.github.noeppi_noeppi.libx.annotation.registration.RegName;
import io.github.noeppi_noeppi.libx.annotation.registration.RegisterClass;
import io.github.noeppi_noeppi.libx.annotation.processor.Processor;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

public class RegisterClassModifyProcessor extends Processor {

    @Override
    public Class<?>[] getTypes() {
        return new Class[]{ RegisterClass.class, NoReg.class, RegName.class };
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(NoReg.class)) {
            if (element.getEnclosingElement().getAnnotation(RegisterClass.class) == null) {
                this.messager.printMessage(Diagnostic.Kind.ERROR, "Can not exclude value from automatic registration outside class annotated with @RegisterClass", element);
            }
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(RegName.class)) {
            if (element.getEnclosingElement().getAnnotation(RegisterClass.class) == null) {
                this.messager.printMessage(Diagnostic.Kind.ERROR, "Can not alter registry name outside class annotated with @RegisterClass", element);
            }
            if (element.getAnnotation(NoReg.class) != null) {
                this.messager.printMessage(Diagnostic.Kind.ERROR, "Can not alter registry name of ignored field.", element);
            }
        }
        return true;
    }
}
