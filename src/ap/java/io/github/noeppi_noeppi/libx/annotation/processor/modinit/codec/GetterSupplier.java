package io.github.noeppi_noeppi.libx.annotation.processor.modinit.codec;

import io.github.noeppi_noeppi.libx.annotation.processor.modinit.FailureException;

public interface GetterSupplier {

    String get() throws FailureException;
}
