package io.github.noeppi_noeppi.libx.annotation.processor.modinit;

import io.github.noeppi_noeppi.libx.annotation.processor.ProcessorEnv;

import javax.lang.model.element.Element;

public interface ModEnv extends ProcessorEnv {
    
    ModInit getMod(Element element);
}
