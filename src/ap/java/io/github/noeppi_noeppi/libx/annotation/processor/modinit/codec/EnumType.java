package io.github.noeppi_noeppi.libx.annotation.processor.modinit.codec;

import io.github.noeppi_noeppi.libx.annotation.processor.modinit.FailureException;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.ModEnv;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.tools.Diagnostic;

public class EnumType implements CodecType {
    
    @Override
    public boolean matchesDirect(Element param, String name, ModEnv env) {
        return false;
    }

    @Override
    public boolean matches(Element param, String name, ModEnv env) {
        CodecProcessor.ListInfo list = CodecProcessor.getNestedListInfo(param.asType(), env);
        if (list == null) return false;
        Element element = env.types().asElement(list.elementType());
        return element != null && element.getKind() == ElementKind.ENUM;
    }

    @Override
    public GeneratedCodec.CodecElement generate(Element param, String name, GetterSupplier getter, ModEnv env) throws FailureException {
        String typeFqn = param.asType().toString();
        String typeFqnBoxed = env.boxed(param.asType()).toString();

        CodecProcessor.ListInfo list = CodecProcessor.getNestedListInfo(param.asType(), env);
        if (list == null) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Can't get a Codec for parameterized list type: Failed to infer element type.", param);
            throw new FailureException();
        }

        Element element = env.types().asElement(list.elementType());
        if (element.getKind() != ElementKind.ENUM) {
            env.messager().printMessage(Diagnostic.Kind.ERROR, "Can't create enum Codec for non-enum class.", param);
            throw new FailureException();
        }

        return new GeneratedCodec.CodecEnum(name, typeFqn, typeFqnBoxed, element.asType().toString(), list.nesting(), getter.get());
    }
}
