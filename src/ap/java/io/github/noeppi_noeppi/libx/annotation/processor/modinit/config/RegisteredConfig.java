package io.github.noeppi_noeppi.libx.annotation.processor.modinit.config;

import io.github.noeppi_noeppi.libx.annotation.meta.RemoveIn;

import java.util.Objects;

public record RegisteredConfig(String name, boolean client, String requiresMod, String classFqn) {

    @RemoveIn(minecraft = "1.19")
    @Deprecated
    public RegisteredConfig(String name, boolean client, String classFqn) {
        this(name, client, "", classFqn);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        RegisteredConfig that = (RegisteredConfig) o;
        return this.client == that.client && Objects.equals(this.name, that.name) && Objects.equals(this.requiresMod, that.requiresMod) && Objects.equals(this.classFqn, that.classFqn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.client, this.requiresMod, this.classFqn);
    }
}
