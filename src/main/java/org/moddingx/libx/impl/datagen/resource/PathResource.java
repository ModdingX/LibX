package org.moddingx.libx.impl.datagen.resource;

import net.minecraft.server.packs.resources.ResourceMetadata;
import org.moddingx.libx.datagen.PackTarget;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class PathResource implements PackTarget.Resource {
    
    private final Path path;
    private ResourceMetadata metadata;

    public PathResource(Path path) {
        this.path = path;
        this.metadata = null;
    }

    @Override
    public InputStream open() throws IOException {
        return Files.newInputStream(this.path);
    }

    @Override
    public BufferedReader read() throws IOException {
        return Files.newBufferedReader(this.path, StandardCharsets.UTF_8);
    }

    @Override
    public ResourceMetadata meta() throws IOException {
        if (this.metadata == null) {
            Path metaPath = this.path.resolveSibling(this.path.getFileName() + ".mcmeta");
            if (!Files.isRegularFile(metaPath)) return ResourceMetadata.EMPTY;
            try (InputStream in = Files.newInputStream(metaPath)) {
                this.metadata = ResourceMetadata.fromJsonStream(in);
            }
        }
        return this.metadata;
    }
}
