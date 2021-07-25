package io.github.noeppi_noeppi.libx.annotation.processor.modinit.codec;

import io.github.noeppi_noeppi.libx.annotation.processor.modinit.FailureException;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.ModEnv;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;

public interface CodecType {
    
    boolean matchesDirect(Element param, String name, ModEnv env);
    boolean matches(Element param, String name, ModEnv env);
    GeneratedCodec.CodecElement generate(Element param, String name, GetterSupplier getter, ModEnv env) throws FailureException;
}
