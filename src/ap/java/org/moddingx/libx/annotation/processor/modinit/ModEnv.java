package org.moddingx.libx.annotation.processor.modinit;

import org.moddingx.libx.annotation.processor.ProcessorEnv;

import javax.lang.model.element.Element;

public interface ModEnv extends ProcessorEnv {
    
    ModInit getMod(Element element);
}
