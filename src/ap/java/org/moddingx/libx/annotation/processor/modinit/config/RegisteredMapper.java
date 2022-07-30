package org.moddingx.libx.annotation.processor.modinit.config;

import javax.annotation.Nullable;

public record RegisteredMapper(String classFqn, String targetTypeSource, @Nullable String requiresMod, boolean genericType) {

}
