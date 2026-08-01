package org.moddingx.libx.datapack;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;

/**
 * Represents a resource entry defined by its {@link Identifier} used as id
 * and a {@link Resource} used to access the resource entry.
 */
public class ResourceEntry {

    private final Identifier id;
    private final Resource resource;

    /**
     * Creates a new resource entry.
     * 
     * @param id The id of the resource
     * @param resource The resource
     */
    public ResourceEntry(Identifier id, Resource resource) {
        this.id = id;
        this.resource = resource;
    }

    /**
     * Gets the id of this resource entry
     */
    public Identifier id() {
        return this.id;
    }

    /**
     * Gets a resource for this entry.
     */
    public Resource resource() throws IOException {
        return this.resource;
    }
}
