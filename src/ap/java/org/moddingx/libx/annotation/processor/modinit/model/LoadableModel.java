package org.moddingx.libx.annotation.processor.modinit.model;

import java.util.Objects;

public record LoadableModel(String classFqn, String fieldName, String modelNamespace, String modelPath) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        LoadableModel that = (LoadableModel) o;
        return Objects.equals(this.classFqn, that.classFqn) && Objects.equals(this.fieldName, that.fieldName) && Objects.equals(this.modelNamespace, that.modelNamespace) && Objects.equals(this.modelPath, that.modelPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.classFqn, this.fieldName, this.modelNamespace, this.modelPath);
    }
}
