package io.github.noeppi_noeppi.libx.annotation.processor.modinit.config;

import java.util.Objects;

public record RegisteredMapper(String classFqn, boolean genericType) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        RegisteredMapper that = (RegisteredMapper) o;
        return this.genericType == that.genericType && Objects.equals(this.classFqn, that.classFqn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.classFqn, this.genericType);
    }
}
