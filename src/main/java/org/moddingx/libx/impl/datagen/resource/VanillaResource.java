package org.moddingx.libx.impl.datagen.resource;

import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.moddingx.libx.datagen.PackTarget;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

public class VanillaResource implements PackTarget.Resource {
    
    private final Resource resource;

    public VanillaResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public InputStream open() throws IOException {
        return this.resource.open();
    }

    @Override
    public BufferedReader read() throws IOException {
        return this.resource.openAsReader();
    }

    @Override
    public ResourceMetadata meta() throws IOException {
        return this.resource.metadata();
    }
}
