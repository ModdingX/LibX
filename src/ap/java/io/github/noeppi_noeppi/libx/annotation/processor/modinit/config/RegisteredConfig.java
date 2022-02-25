package io.github.noeppi_noeppi.libx.annotation.processor.modinit.config;

import javax.annotation.Nullable;

public record RegisteredConfig(String name, boolean client, @Nullable String requiresMod, String classFqn) {

}
