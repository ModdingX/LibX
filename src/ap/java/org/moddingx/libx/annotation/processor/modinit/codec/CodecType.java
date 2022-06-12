package org.moddingx.libx.annotation.processor.modinit.codec;

import org.moddingx.libx.annotation.processor.modinit.FailureException;
import org.moddingx.libx.annotation.processor.modinit.ModEnv;

import javax.lang.model.element.Element;

public interface CodecType {
    
    boolean matchesDirect(Element param, String name, ModEnv env);
    boolean matches(Element param, String name, ModEnv env);
    GeneratedCodec.CodecElement generate(Element param, String name, GetterSupplier getter, ModEnv env) throws FailureException;
}
