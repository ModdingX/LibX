package io.github.noeppi_noeppi.libx.annotation.processor.modinit.codec;

import io.github.noeppi_noeppi.libx.annotation.processor.modinit.GeneratedCodec;
import io.github.noeppi_noeppi.libx.annotation.processor.modinit.ModEnv;

import javax.lang.model.element.VariableElement;

public interface CodecType {
    
    boolean matchesDirect(VariableElement param, String name, ModEnv env);
    boolean matches(VariableElement param, String name, ModEnv env);
    GeneratedCodec.CodecElement generate(VariableElement param, String name, GetterSupplier getter, ModEnv env) throws FailureException;

}
