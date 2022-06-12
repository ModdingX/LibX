package org.moddingx.libx.annotation.processor.modinit.codec;

import org.moddingx.libx.annotation.processor.modinit.FailureException;

public interface GetterSupplier {

    String get() throws FailureException;
}
