package org.moddingx.libx.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.server.packs.PackType;
import org.moddingx.libx.impl.datagen.registries.DatagenRegistrySet;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Map;

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
    private final PackOutput packOutput;
    
    public PackTarget(String name, DatagenSystem system, RegistrySet registries, Map<PackType, Path> outputMap) {
        this.name = name;
        this.system = system;
        if (!(registries instanceof DatagenRegistrySet drs)) throw new IllegalArgumentException("Custom RegistrySet instances are not supported.");
        if (drs.isRoot()) throw new IllegalArgumentException("Can't create pack target with root registry set");
        this.registries = drs;
        this.outputMap = Map.copyOf(outputMap);
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
     * Wraps the pack target as a vanilla {@link PackOutput}.
     */
    public PackOutput packOutput() {
        return this.packOutput;
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
