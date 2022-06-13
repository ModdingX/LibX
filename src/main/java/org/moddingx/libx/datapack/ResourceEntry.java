package org.moddingx.libx.datapack;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;

/**
 * Represents a resource entry defined by its {@link ResourceLocation} used as id
 * and a {@link Resource} used to access the resource entry.
 */
public class ResourceEntry {

    private final ResourceLocation id;
    private final Resource resource;

    /**
     * Creates a new resource entry.
     * 
     * @param id The id of the resource
     * @param resource The resource
     */
    public ResourceEntry(ResourceLocation id, Resource resource) {
        this.id = id;
        this.resource = resource;
    }

    /**
     * Gets the id of this resource entry
     */
    public ResourceLocation id() {
        return this.id;
    }

    /**
     * Opens a resource for this entry. The resource must be closed later.
     */
    public Resource resource() throws IOException {
        return this.resource;
    }
}