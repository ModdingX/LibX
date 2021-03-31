package io.github.noeppi_noeppi.libx.annotation.processor.modinit;

import java.util.Objects;

public class LoadableModel {
    
    public final String classFqn;
    public final String fieldName;
    public final String modelNamespace;
    public final String modelPath;

    public LoadableModel(String classFqn, String fieldName, String modelNamespace, String modelPath) {
        this.classFqn = classFqn;
        this.fieldName = fieldName;
        this.modelNamespace = modelNamespace;
        this.modelPath = modelPath;
    }

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
