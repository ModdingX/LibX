package org.moddingx.libx.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.moddingx.libx.impl.datagen.registries.DatagenRegistrySet;
import org.moddingx.libx.impl.datagen.resource.ResourceLocator;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A pack target contains output paths for the different {@link PackType pack types}. It also maintains a set of
 * registries that can be populated during the matching {@link DatagenStage stages}.
 * 
 * A pack target does not need to support output on all {@link PackType pack types}.
 * 
 * A pack target can have other pack targets as parents, which makes it inherit their registry values.
 */
public class PackTarget {
    
    private final String name;
    private final DatagenSystem system;
    private final DatagenRegistrySet registries;
    private final Map<PackType, Path> outputMap;
    private final Map<PackType, ResourceLocator> resourceMap;
    private final PackOutput packOutput;
    
    PackTarget(String name, DatagenSystem system, RegistrySet registries, Map<PackType, Path> outputMap, Map<PackType, String> prefixMap, Map<PackType, List<Path>> resourcePathMap, List<PackTarget> parents) {
        this.name = name;
        this.system = system;
        if (!(registries instanceof DatagenRegistrySet drs)) throw new IllegalArgumentException("Custom RegistrySet instances are not supported.");
        if (drs.isRoot()) throw new IllegalArgumentException("Can't create pack target with root registry set");
        this.registries = drs;
        this.outputMap = Map.copyOf(outputMap);
        boolean rootResources = prefixMap == null && resourcePathMap == null && parents == null;
        if (!rootResources) {
            Objects.requireNonNull(prefixMap);
            Objects.requireNonNull(resourcePathMap);
            Objects.requireNonNull(parents);
        }
        this.resourceMap = this.outputMap.keySet().stream().map(type -> {
            if (rootResources) return Map.entry(type, ResourceLocator.root(type));
            List<ResourceLocator> parentLocators = parents.stream().flatMap(p -> Stream.ofNullable(p.resourceMap.getOrDefault(type, null))).toList();
            ResourceLocator locator = new ResourceLocator(List.copyOf(resourcePathMap.getOrDefault(type, List.of())), prefixMap.getOrDefault(type, null), parentLocators);
            return Map.entry(type, locator);
        }).collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        this.packOutput = new Vanilla();
    }

    /**
     * The name of the pack target.
     */
    public String name() {
        return this.name;
    }

    /**
     * The {@link DatagenSystem} associated with this pack target.
     */
    public DatagenSystem system() {
        return this.system;
    }

    /**
     * Provides access to the pack targets registries.
     */
    public RegistrySet registries() {
        return this.registries;
    }
    
    public Map<PackType, Path> outputMap() {
        return this.outputMap;
    }

    /**
     * Gets the output path for this pack target.
     */
    public Path path(PackType type) {
        if (this.outputMap.containsKey(type)) return this.outputMap.get(type);
        throw new UnsupportedOperationException("The pack target '" + this.name + "' does not support output on " + type);
    }

    /**
     * Finds an existing resource in this pack target. Resources can only be located in pack types, where this pack target supports output.
     */
    public Resource find(PackType type, ResourceLocation resourceId) throws FileNotFoundException {
        if (!this.resourceMap.containsKey(type)) throw new UnsupportedOperationException("The pack target '" + this.name + "' does not support output on " + type);
        Resource resource = this.resourceMap.get(type).getResource(this.system.fileHelper(), resourceId);
        if (resource == null) throw new FileNotFoundException(this.name + "/" + resourceId);
        return resource;
    }
    
    /**
     * Wraps the pack target as a vanilla {@link PackOutput}.
     */
    public PackOutput packOutput() {
        return this.packOutput;
    }
    
    public interface Resource {
        
        InputStream open() throws IOException;
        BufferedReader read() throws IOException;
        ResourceMetadata meta() throws IOException;
    }
    
    private class Vanilla extends PackOutput {

        public Vanilla() {
            super(PackTarget.this.system.mainOutput());
        }

        @Nonnull
        @Override
        public Path getOutputFolder() {
            throw new UnsupportedOperationException("Can't get root output folder for this pack type");
        }

        @Nonnull
        @Override
        public Path getOutputFolder(@Nonnull Target target) {
            return switch (target) {
                case RESOURCE_PACK -> PackTarget.this.path(PackType.CLIENT_RESOURCES);
                case DATA_PACK -> PackTarget.this.path(PackType.SERVER_DATA);
                case REPORTS -> PackTarget.this.system.mainOutput().resolve("reports");
            };
        }
    }
}
