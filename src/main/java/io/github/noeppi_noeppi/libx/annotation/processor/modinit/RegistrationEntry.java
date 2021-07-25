package io.github.noeppi_noeppi.libx.annotation.processor.modinit;

import java.util.Objects;

public record RegistrationEntry(String registryName, String fqn) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        RegistrationEntry that = (RegistrationEntry) o;
        return Objects.equals(this.registryName, that.registryName) && Objects.equals(this.fqn, that.fqn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.registryName, this.fqn);
    }
}
