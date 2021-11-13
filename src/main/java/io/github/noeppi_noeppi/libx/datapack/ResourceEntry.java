package io.github.noeppi_noeppi.libx.datapack;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;

/**
 * Represents a resource entry defined by its {@link ResourceLocation} used as id
 * and a {@link Resource} used to access the resource entry.
 */
public class ResourceEntry {

    private final ResourceLocation id;
    private final ResourceSupplier resource;

    /**
     * Creates a new resource entry.
     * 
     * @param id The id of the resource
     * @param resourceAccess A supplier that opens the {@link Resource}
     */
    public ResourceEntry(ResourceLocation id, ResourceSupplier resourceAccess) {
        this.id = id;
        this.resource = resourceAccess;
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
        return this.resource.get();
    }
    
    public interface ResourceSupplier {
        
        Resource get() throws IOException;
    }
}