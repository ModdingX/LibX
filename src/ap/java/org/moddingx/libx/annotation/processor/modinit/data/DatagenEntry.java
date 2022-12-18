package org.moddingx.libx.annotation.processor.modinit.data;

import java.util.List;
import java.util.Objects;

public record DatagenEntry(String classFqn, List<Arg> ctorArgs) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        DatagenEntry that = (DatagenEntry) o;
        return Objects.equals(this.classFqn, that.classFqn) && Objects.equals(this.ctorArgs, that.ctorArgs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.classFqn, this.ctorArgs);
    }

    public enum Arg {
        MOD, GENERATOR, PACK_OUTPUT, FILE_HELPER
    }
}
