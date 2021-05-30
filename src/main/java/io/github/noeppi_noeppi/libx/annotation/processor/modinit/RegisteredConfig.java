package io.github.noeppi_noeppi.libx.annotation.processor.modinit;

import java.util.Objects;

public class RegisteredConfig {
    
    public final String name;
    public final boolean client;
    public final String classFqn;
    
    public RegisteredConfig(String name, boolean client, String classFqn) {
        this.name = name;
        this.client = client;
        this.classFqn = classFqn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        RegisteredConfig that = (RegisteredConfig) o;
        return this.client == that.client && Objects.equals(this.name, that.name) && Objects.equals(this.classFqn, that.classFqn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.client, this.classFqn);
    }
}
