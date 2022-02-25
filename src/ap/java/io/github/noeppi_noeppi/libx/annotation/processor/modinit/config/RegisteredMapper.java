package io.github.noeppi_noeppi.libx.annotation.processor.modinit.config;

import javax.annotation.Nullable;

public record RegisteredMapper(String classFqn, @Nullable String requiresMod, boolean genericType) {

}
